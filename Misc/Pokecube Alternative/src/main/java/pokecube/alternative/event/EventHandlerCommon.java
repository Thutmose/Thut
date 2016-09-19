package pokecube.alternative.event;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;

import com.google.common.io.Files;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.alternative.Reference;
import pokecube.alternative.capabilities.IBeltCapability;
import pokecube.alternative.capabilities.PokeBeltCapability;
import pokecube.alternative.network.PacketHandler;
import pokecube.alternative.network.PacketSyncBelt;
import pokecube.alternative.player.PlayerHandler;
import pokecube.alternative.utility.LogHelper;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.commands.CommandTools;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.events.RecallEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;

public class EventHandlerCommon
{

    static HashSet<UUID>                            syncSchedule = new HashSet<UUID>();

    @CapabilityInject(IBeltCapability.class)
    public static final Capability<IBeltCapability> BELTAI_CAP   = null;

    public static IBeltCapability.Storage           storage;

    public EventHandlerCommon()
    {
        CapabilityManager.INSTANCE.register(IBeltCapability.class, storage = new IBeltCapability.Storage(),
                PokeBeltCapability.class);
    }

    @SubscribeEvent
    public void PlayerUseItemEvent(PlayerInteractEvent.RightClickItem event)
    {
        ItemStack item = event.getItemStack();
        EntityPlayer player = event.getEntityPlayer();
        int slotIndex = event.getEntityPlayer().inventory.currentItem;
        if (slotIndex != 0)
        {
            if (PokecubeManager.isFilled(item))
            {
                if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
                {
                    IBeltCapability cap = player.getCapability(BELTAI_CAP, null);
                    boolean toBelt = false;
                    for (int i = 0; i < 6; i++)
                    {
                        if (cap.getCube(i) == null)
                        {
                            cap.setCube(i, item);
                            player.inventory.setInventorySlotContents(slotIndex, null);
                            syncPokemon(player);
                            toBelt = true;
                            break;
                        }
                    }
                    if(toBelt)
                    {
                        player.addChatMessage(new TextComponentString(I18n.format(Reference.MODID + ".pokebelt.tobelt")));
                    }
                    else
                    {
                        player.addChatMessage(new TextComponentString(I18n.format(Reference.MODID + ".pokebelt.useBelt")));
                        EntityItem entityitem = player.dropItem(item, false);
                        if (entityitem != null)
                        {
                            entityitem.setNoPickupDelay();
                            entityitem.setOwner(player.getName());
                        }
                        player.inventory.setInventorySlotContents(slotIndex, null);
                    }
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void PlayerLoggedInEvent(PlayerLoggedInEvent event)
    {
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        EntityPlayer player = event.player;
        IBeltCapability cap = player.getCapability(BELTAI_CAP, null);
        cap.getCube(0);// Gets first cube
        if (side == Side.SERVER)
        {
            EventHandlerCommon.syncSchedule.add(event.player.getUniqueID());
        }
        if (player.getEntityData().getBoolean("firstTime"))
        {
            player.getEntityData().setBoolean("firstTime", false);
        }
        else
        {
        }
    }

    @SubscribeEvent
    public void EntityDie(LivingDeathEvent event)
    {
        if (event.getEntity() instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            IBeltCapability cap = player.getCapability(BELTAI_CAP, null);
            for (int counter = 0; counter > 5; counter++)
            {
                cap.setCube(counter, cap.getCube(counter));
            }
        }
    }

    @SubscribeEvent
    public void onEntityCapabilityAttach(AttachCapabilitiesEvent.Entity event)
    {
        if (event.getEntity() instanceof EntityPlayer)
        {
            class Provider extends PokeBeltCapability implements ICapabilitySerializable<NBTTagCompound>
            {
                @Override
                public void deserializeNBT(NBTTagCompound nbt)
                {
                    storage.readNBT(BELTAI_CAP, this, null, nbt);
                }

                @SuppressWarnings("unchecked") // There isnt anything sane we
                                               // can do about this.
                @Override
                public <T> T getCapability(Capability<T> capability, EnumFacing facing)
                {
                    if (BELTAI_CAP != null && capability == BELTAI_CAP) return (T) this;
                    return null;
                }

                @Override
                public boolean hasCapability(Capability<?> capability, EnumFacing facing)
                {
                    return BELTAI_CAP != null && capability == BELTAI_CAP;
                }

                @Override
                public NBTTagCompound serializeNBT()
                {
                    return (NBTTagCompound) storage.writeNBT(BELTAI_CAP, this, null);
                }
            }
            event.addCapability(new ResourceLocation(Reference.MODID, "pokebelt"), new Provider());
        }
    }

    @SubscribeEvent
    public void playerTick(PlayerEvent.LivingUpdateEvent event)
    {
        if (event.getEntity() instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) event.getEntity();

            if (!syncSchedule.isEmpty() && syncSchedule.contains(player.getUniqueID()) && player.ticksExisted > 20)
            {
                syncPokemon(player);
                syncSchedule.remove(player.getUniqueID());
            }
        }
    }

    public static void syncPokemon(EntityPlayer player)
    {
        IBeltCapability cap = player.getCapability(EventHandlerCommon.BELTAI_CAP, null);
        PacketHandler.INSTANCE.sendToAll(new PacketSyncBelt(cap, player.getEntityId()));
    }

    @SubscribeEvent
    public void playerDeath(PlayerDropsEvent event)
    {
        if (event.getEntity() instanceof EntityPlayer && !event.getEntity().worldObj.isRemote
                && !event.getEntity().worldObj.getGameRules().getBoolean("keepInventory"))
        {
            PlayerHandler.getPlayerPokemon(event.getEntityPlayer()).dropItemsAt(event.getDrops(),
                    event.getEntityPlayer());
        }

    }

    @SubscribeEvent
    public void recallPokemon(RecallEvent event)
    {
        IPokemob pokemon = event.recalled;
        System.out.println(pokemon + " " + pokemon.getPokemonOwner());
        ItemStack pokemonStack = PokecubeManager.pokemobToItem(pokemon);
        if (pokemon.getPokemonOwner() instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) pokemon.getPokemonOwner();
            IBeltCapability cap = player.getCapability(EventHandlerCommon.BELTAI_CAP, null);
            boolean added = true;
            if (cap.getCube(0) == null)
            {
                cap.setCube(0, pokemonStack);
            }
            else if (cap.getCube(1) == null)
            {
                cap.setCube(1, pokemonStack);
            }
            else if (cap.getCube(2) == null)
            {
                cap.setCube(2, pokemonStack);
            }
            else if (cap.getCube(3) == null)
            {
                cap.setCube(3, pokemonStack);
            }
            else if (cap.getCube(4) == null)
            {
                cap.setCube(4, pokemonStack);
            }
            else if (cap.getCube(5) == null)
            {
                cap.setCube(5, pokemonStack);
            }
            else
            {
                InventoryPC.addPokecubeToPC(pokemonStack, player.getEntityWorld());
                added = false;
            }
            if (added)
            {
                ITextComponent mess = CommandTools.makeTranslatedMessage("pokemob.action.return", "green",
                        pokemon.getPokemonDisplayName().getFormattedText());
                pokemon.displayMessageToOwner(mess);
            }
            if (!player.isSneaking() && !((Entity) pokemon).isDead)
            {
                boolean has = player.hasAchievement(PokecubeMod.catchAchievements.get(pokemon.getPokedexEntry()));
                has = has || player.hasAchievement(PokecubeMod.hatchAchievements.get(pokemon.getPokedexEntry()));
                if (!has)
                {
                    StatsCollector.addCapture(pokemon);
                }
            }
            syncPokemon(player);
            ((Entity) pokemon).setDead();
            pokemon.setPokemonOwner(null);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void playerLoad(PlayerEvent.LoadFromFile event)
    {
        PlayerHandler.clearPlayerPokemon(event.getEntityPlayer());

        File file1 = getPlayerFile("poke", event.getPlayerDirectory(), event.getEntityPlayer().getDisplayNameString());
        if (!file1.exists())
        {
            File filep = event.getPlayerFile("poke");
            if (filep.exists())
            {
                try
                {
                    Files.copy(filep, file1);
                    LogHelper.info("Using and converting UUID PokecubeExtras savefile for "
                            + event.getEntityPlayer().getDisplayNameString());
                    filep.delete();
                    File fb = event.getPlayerFile("pokeback");
                    if (fb.exists()) fb.delete();
                }
                catch (IOException e)
                {
                }
            }
            else
            {
                File fileq = getLegacy1110FileFromPlayer("poke", event.getPlayerDirectory(),
                        event.getEntityPlayer().getDisplayNameString());
                if (fileq.exists())
                {
                    try
                    {
                        Files.copy(fileq, file1);
                        fileq.deleteOnExit();
                        LogHelper.info("Using pre 1.0.0 PokecubeExtras savefile for "
                                + event.getEntityPlayer().getDisplayNameString());
                    }
                    catch (IOException e)
                    {
                    }
                }
                else
                {
                    File filet = getLegacy1710FileFromPlayer(event.getEntityPlayer());
                    if (filet.exists())
                    {
                        try
                        {
                            Files.copy(filet, file1);
                            filet.deleteOnExit();
                            LogHelper.info("Using pre MC 1.10 PokecubeExtras savefile for "
                                    + event.getEntityPlayer().getDisplayNameString());
                        }
                        catch (IOException e)
                        {
                        }
                    }
                }
            }
        }
        syncSchedule.add(event.getEntityPlayer().getUniqueID());
    }

    public File getPlayerFile(String suffix, File playerDirectory, String playername)
    {
        if ("dat".equals(suffix)) throw new IllegalArgumentException("The suffix 'dat' is reserved");
        return new File(playerDirectory, "_" + playername + "." + suffix);
    }

    public static File getLegacy1710FileFromPlayer(EntityPlayer player)
    {
        try
        {
            File playersDirectory = new File(player.worldObj.getSaveHandler().getWorldDirectory(), "players");
            return new File(playersDirectory, player.getDisplayNameString() + ".pokecube");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public File getLegacy1110FileFromPlayer(String suffix, File playerDirectory, String playername)
    {
        if ("dat".equals(suffix)) throw new IllegalArgumentException("The suffix 'dat' is reserved");
        return new File(playerDirectory, playername + "." + suffix);
    }

}
