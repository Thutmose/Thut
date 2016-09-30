package thut.essentials.land;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemFood;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;
import thut.essentials.events.DenyItemUseEvent;
import thut.essentials.events.DenyItemUseEvent.UseType;
import thut.essentials.land.LandManager.LandTeam;
import thut.essentials.util.ConfigManager;

public class LandEventsHandler
{
    Map<UUID, Long> lastLeaveMessage = Maps.newHashMap();
    Map<UUID, Long> lastEnterMessage = Maps.newHashMap();

    public LandEventsHandler()
    {
    }

    @SubscribeEvent
    public void BreakBlock(BreakEvent evt)
    {
        EntityPlayer player = evt.getPlayer();
        if (ConfigManager.INSTANCE.landEnabled && player != null && player.getTeam() != null)
        {
            LandChunk c = LandChunk.getChunkCoordFromWorldCoord(evt.getPos(), player.dimension);
            if (!LandManager.getInstance().isOwned(c)) return;
            if (!player.getEntityWorld().isRemote)
            {
                UserListOpsEntry userentry = ((EntityPlayerMP) player).mcServer.getPlayerList().getOppedPlayers()
                        .getEntry(player.getGameProfile());

                if (userentry != null
                        || !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()) { return; }
            }
            if (LandManager.getInstance().isOwned(c)
                    && !LandManager.getInstance().isTeamLand(c, player.getTeam().getRegisteredName()))
            {
                player.addChatMessage(getDenyMessage(LandManager.getInstance().getLandOwner(c)));
                evt.setCanceled(true);
                return;
            }
            LandChunk block = new LandChunk(evt.getPos(), evt.getWorld().provider.getDimension());
            LandManager.getInstance().unsetPublic(block);
        }
    }

    @SubscribeEvent
    public void PlayerLoggin(PlayerLoggedInEvent evt)
    {
        EntityPlayer entityPlayer = evt.player;
        if (entityPlayer.getTeam() == null)
        {
            if (entityPlayer.getEntityWorld().getScoreboard().getTeam(ConfigManager.INSTANCE.defaultTeamName) == null)
            {
                entityPlayer.getEntityWorld().getScoreboard().createTeam(ConfigManager.INSTANCE.defaultTeamName);
            }
            entityPlayer.getEntityWorld().getScoreboard().addPlayerToTeam(entityPlayer.getName(),
                    ConfigManager.INSTANCE.defaultTeamName);
        }
    }

    @SubscribeEvent
    public void EntityUpdate(LivingUpdateEvent evt)
    {
        if (evt.getEntityLiving() instanceof EntityPlayer
                && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
        {
            EntityPlayer player = (EntityPlayer) evt.getEntityLiving();
            if (player.getTeam() == null)
            {
                player.getEntityWorld().getScoreboard().addPlayerToTeam(player.getName(),
                        ConfigManager.INSTANCE.defaultTeamName);
            }
            BlockPos here;
            BlockPos old;
            here = new BlockPos(player.chasingPosX, player.chasingPosY, player.chasingPosZ);
            old = new BlockPos(player.prevChasingPosX, player.prevChasingPosY, player.prevChasingPosZ);
            LandChunk c = LandChunk.getChunkCoordFromWorldCoord(here, player.dimension);
            LandChunk c1 = LandChunk.getChunkCoordFromWorldCoord(old, player.dimension);
            if (c.equals(c1) || !ConfigManager.INSTANCE.landEnabled) return;
            if (LandManager.getInstance().isOwned(c) || LandManager.getInstance().isOwned(c1))
            {
                String team = LandManager.getInstance().getLandOwner(c);
                String team1 = LandManager.getInstance().getLandOwner(c1);
                if (!lastLeaveMessage.containsKey(evt.getEntity().getUniqueID()))
                    lastLeaveMessage.put(evt.getEntity().getUniqueID(), System.currentTimeMillis() - 1);
                if (!lastEnterMessage.containsKey(evt.getEntity().getUniqueID()))
                    lastEnterMessage.put(evt.getEntity().getUniqueID(), System.currentTimeMillis() - 1);
                if (team != null)
                {
                    if (team.equals(team1)) return;
                    if (team1 != null)
                    {
                        long last = lastLeaveMessage.get(evt.getEntity().getUniqueID());
                        if (last < System.currentTimeMillis())
                        {
                            evt.getEntity().addChatMessage(getExitMessage(team1));
                            lastLeaveMessage.put(evt.getEntity().getUniqueID(), System.currentTimeMillis() + 100);
                        }
                    }
                    long last = lastEnterMessage.get(evt.getEntity().getUniqueID());
                    if (last < System.currentTimeMillis())
                    {
                        evt.getEntity().addChatMessage(getEnterMessage(team));
                        lastLeaveMessage.put(evt.getEntity().getUniqueID(), System.currentTimeMillis() + 100);
                    }
                }
                else
                {
                    long last = lastLeaveMessage.get(evt.getEntity().getUniqueID());
                    if (last < System.currentTimeMillis())
                    {
                        evt.getEntity().addChatMessage(getExitMessage(team1));
                        lastLeaveMessage.put(evt.getEntity().getUniqueID(), System.currentTimeMillis() + 100);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void ExplosionEvent(ExplosionEvent.Detonate evt)
    {
        List<BlockPos> toRemove = Lists.newArrayList();
        if (ConfigManager.INSTANCE.denyExplosions && ConfigManager.INSTANCE.landEnabled)
        {
            int dimension = evt.getWorld().provider.getDimension();
            for (BlockPos pos : evt.getAffectedBlocks())
            {
                LandChunk c = LandChunk.getChunkCoordFromWorldCoord(pos, dimension);
                String owner = LandManager.getInstance().getLandOwner(c);
                if (evt.getExplosion().getExplosivePlacedBy() instanceof EntityPlayerMP)
                {
                    Team playerTeam = LandManager.getTeam((EntityPlayer) evt.getExplosion().getExplosivePlacedBy());
                    if (playerTeam != null)
                    {
                        String team = playerTeam.getRegisteredName();
                        if (owner.equals(team))
                        {
                            owner = null;
                        }
                    }
                }
                if (owner == null) continue;
                toRemove.add(pos);
            }
        }
        evt.getAffectedBlocks().removeAll(toRemove);
    }

    /** Uses player interact here to also prevent opening of inventories.
     * 
     * @param evt */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void interactLeftClickBlock(PlayerInteractEvent.LeftClickBlock evt)
    {
        LandChunk c = LandChunk.getChunkCoordFromWorldCoord(evt.getPos(), evt.getEntityPlayer().dimension);
        String owner = LandManager.getInstance().getLandOwner(c);
        if (owner == null || !ConfigManager.INSTANCE.landEnabled) return;
        Team playerTeam = LandManager.getTeam(evt.getEntityPlayer());

        String team = playerTeam.getRegisteredName();
        if (owner.equals(team)) { return; }
        LandChunk blockLoc = new LandChunk(evt.getPos(), evt.getEntityPlayer().dimension);
        LandManager.getInstance().isPublic(blockLoc);
        if (!team.equals(owner))
        {
            if (!LandManager.getInstance().isPublic(blockLoc))
            {
                evt.setUseBlock(Result.DENY);
                evt.setCanceled(true);
                if (!evt.getWorld().isRemote) evt.getEntity().addChatMessage(getDenyMessage(owner));
            }
            evt.setUseItem(Result.DENY);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void interactRightClickItem(PlayerInteractEvent.RightClickItem evt)
    {
        LandChunk c = LandChunk.getChunkCoordFromWorldCoord(evt.getPos(), evt.getEntityPlayer().dimension);
        String owner = LandManager.getInstance().getLandOwner(c);
        if (owner == null || evt.getItemStack().getItem() instanceof ItemFood || !ConfigManager.INSTANCE.landEnabled)
            return;
        Team playerTeam = LandManager.getTeam(evt.getEntityPlayer());
        String team = playerTeam.getRegisteredName();
        if (owner.equals(team))
        {
            return;
        }
        else if (MinecraftForge.EVENT_BUS
                .post(new DenyItemUseEvent(evt.getEntity(), evt.getItemStack(), UseType.RIGHTCLICKBLOCK))) { return; }
        if (evt.getItemStack() == null) return;

        LandChunk blockLoc = new LandChunk(evt.getPos(), evt.getEntityPlayer().dimension);
        LandManager.getInstance().isPublic(blockLoc);
        if (!team.equals(owner))
        {
            if (!LandManager.getInstance().isPublic(blockLoc))
            {
                evt.setResult(Result.DENY);
                evt.setCanceled(true);
            }
            evt.setResult(Result.DENY);
        }
    }

    /** Uses player interact here to also prevent opening of inventories.
     * 
     * @param evt */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void interactRightClickBlock(PlayerInteractEvent.RightClickBlock evt)
    {
        LandChunk c = LandChunk.getChunkCoordFromWorldCoord(evt.getPos(), evt.getEntityPlayer().dimension);
        String owner = LandManager.getInstance().getLandOwner(c);
        if (owner == null || !ConfigManager.INSTANCE.landEnabled) return;

        Block block = null;
        IBlockState state = evt.getWorld().getBlockState(evt.getPos());
        block = evt.getWorld().getBlockState(evt.getPos()).getBlock();
        boolean b = true;
        Team playerTeam = LandManager.getTeam(evt.getEntityPlayer());
        String team = playerTeam.getRegisteredName();
        boolean shouldPass = true;
        if (owner.equals(team) && !evt.getWorld().isRemote)
        {
            if (evt.getItemStack() != null && evt.getItemStack().getDisplayName().equals("Public Toggle")
                    && evt.getEntityPlayer().isSneaking())
            {
                if (LandManager.getInstance().isAdmin(evt.getEntityPlayer().getName(), team))
                {
                    LandChunk blockLoc = new LandChunk(evt.getPos(), evt.getEntityPlayer().dimension);
                    if (LandManager.getInstance().isPublic(blockLoc))
                    {
                        evt.getEntityPlayer().addChatMessage(new TextComponentString("Set Block to Team Only"));
                        LandManager.getInstance().unsetPublic(blockLoc);
                    }
                    else
                    {
                        evt.getEntityPlayer().addChatMessage(new TextComponentString("Set Block to Public Use"));
                        LandManager.getInstance().setPublic(blockLoc);
                    }
                    evt.setCanceled(true);
                }
            }
            return;
        }
        else if (block != null && !(block.hasTileEntity(state)) && evt.getWorld().isRemote)
        {
            shouldPass = MinecraftForge.EVENT_BUS
                    .post(new DenyItemUseEvent(evt.getEntity(), evt.getItemStack(), UseType.RIGHTCLICKBLOCK));

            if (shouldPass) b = block.onBlockActivated(evt.getWorld(), evt.getPos(), state, evt.getEntityPlayer(),
                    evt.getHand(), null, evt.getFace(), (float) evt.getHitVec().xCoord, (float) evt.getHitVec().yCoord,
                    (float) evt.getHitVec().zCoord);
        }
        if (!b && shouldPass) return;

        LandChunk blockLoc = new LandChunk(evt.getPos(), evt.getEntityPlayer().dimension);
        LandManager.getInstance().isPublic(blockLoc);
        if (!team.equals(owner))
        {
            if (!LandManager.getInstance().isPublic(blockLoc))
            {
                evt.setUseBlock(Result.DENY);
                evt.setCanceled(true);
                if (!evt.getWorld().isRemote && evt.getHand() == EnumHand.MAIN_HAND)
                {
                    evt.getEntity().addChatMessage(getDenyMessage(owner));
                }
            }
            evt.setUseItem(Result.DENY);
        }
    }

    public void onServerStarted()
    {
        LandSaveHandler.loadGlobalData();
    }

    public void onServerStopped()
    {
        LandManager.clearInstance();
    }

    private static ITextComponent getDenyMessage(String theteam)
    {
        LandTeam team = LandManager.getInstance().getTeam(theteam, false);
        if (team != null && !team.denyMessage.isEmpty()) { return new TextComponentString(team.denyMessage); }
        return new TextComponentTranslation("msg.team.deny", theteam);
    }

    private static ITextComponent getEnterMessage(String theteam)
    {
        LandTeam team = LandManager.getInstance().getTeam(theteam, false);
        if (team != null && !team.enterMessage.isEmpty()) { return new TextComponentString(team.enterMessage); }
        return new TextComponentTranslation("msg.team.enterLand", theteam);
    }

    private static ITextComponent getExitMessage(String theteam)
    {
        LandTeam team = LandManager.getInstance().getTeam(theteam, false);
        if (team != null && !team.exitMessage.isEmpty()) { return new TextComponentString(team.exitMessage); }
        return new TextComponentTranslation("msg.team.exitLand", theteam);
    }
}
