package pokecube.alternative.event;

import java.util.HashSet;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.alternative.Reference;
import pokecube.alternative.container.BeltPlayerData;
import pokecube.alternative.container.IPokemobBelt;
import pokecube.alternative.network.PacketHandler;
import pokecube.alternative.network.PacketSyncBelt;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.commands.CommandTools;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.events.RecallEvent;
import pokecube.core.events.handlers.PCEventsHandler;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.maths.Vector3;

public class EventHandlerCommon
{

    static HashSet<UUID> syncSchedule = new HashSet<UUID>();

    public EventHandlerCommon()
    {
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
                    IPokemobBelt cap = BeltPlayerData.getBelt(player);
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
                    if (toBelt)
                    {
                        player.addChatMessage(new TextComponentTranslation(Reference.MODID + ".pokebelt.tobelt"));
                    }
                    else
                    {
                        player.addChatMessage(new TextComponentTranslation(Reference.MODID + ".pokebelt.useBelt"));
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
        if (side == Side.SERVER)
        {
            EventHandlerCommon.syncSchedule.add(event.player.getUniqueID());
        }
    }

    @SubscribeEvent
    public void EntityHurt(LivingHurtEvent event)
    {
        if (event.getEntityLiving() instanceof EntityPlayer && event.getSource().getEntity() instanceof IPokemob)
        {
            System.out.println(PCEventsHandler.getOutMobs(event.getEntityLiving()));
            if (PCEventsHandler.getOutMobs(event.getEntityLiving()).isEmpty())
            {
                IPokemobBelt cap = BeltPlayerData.getBelt(event.getEntityLiving());
                if (cap.getCube(cap.getSlot()) != null)
                {
                    ItemStack cube = cap.getCube(cap.getSlot());
                    if (PokecubeManager.isFilled(cube))
                    {
                        Entity target = event.getSource().getEntity();
                        Vector3 here = Vector3.getNewVector().set(this);
                        Vector3 t = Vector3.getNewVector().set(target);
                        t.set(t.subtractFrom(here).scalarMultBy(0.5).addTo(here));
                        ((IPokecube) cube.getItem()).throwPokecubeAt(target.getEntityWorld(), event.getEntityLiving(),
                                cube, t, null);
                        ITextComponent text = new TextComponentTranslation("pokecube.trainer.toss",
                                event.getEntityLiving().getDisplayName(), cube.getDisplayName());
                        cap.setCube(cap.getSlot(), null);
                        syncPokemon((EntityPlayer) event.getEntityLiving());
                        target.addChatMessage(text);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void playerTick(PlayerEvent.LivingUpdateEvent event)
    {
        if (event.getEntityLiving().worldObj.isRemote) return;
        if (event.getEntity() instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) event.getEntity();

            if (!syncSchedule.isEmpty() && syncSchedule.contains(player.getUniqueID()) && player.ticksExisted > 20)
            {
                syncPokemon(player);
                for (EntityPlayer player2 : event.getEntity().worldObj.playerEntities)
                {
                    IPokemobBelt cap = BeltPlayerData.getBelt(player2);
                    PacketHandler.INSTANCE.sendTo(new PacketSyncBelt(cap, player2.getEntityId()),
                            (EntityPlayerMP) player);
                }
                syncSchedule.remove(player.getUniqueID());
            }
        }
    }

    public static void syncPokemon(EntityPlayer player)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            Thread.dumpStack();
            return;
        }
        IPokemobBelt cap = BeltPlayerData.getBelt(player);
        BeltPlayerData.save(player);
        PacketHandler.INSTANCE.sendToAll(new PacketSyncBelt(cap, player.getEntityId()));
    }

    @SubscribeEvent
    public void startTracking(StartTracking event)
    {
        if (event.getTarget() instanceof EntityPlayer && event.getEntityPlayer().isServerWorld())
        {
            IPokemobBelt cap = BeltPlayerData.getBelt(event.getTarget());
            PacketHandler.INSTANCE.sendTo(new PacketSyncBelt(cap, event.getTarget().getEntityId()),
                    (EntityPlayerMP) event.getEntityPlayer());
        }
    }

    @SubscribeEvent
    public void recallPokemon(RecallEvent event)
    {
        IPokemob pokemon = event.recalled;
        ItemStack pokemonStack = PokecubeManager.pokemobToItem(pokemon);
        if (pokemon.getPokemonOwner() instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) pokemon.getPokemonOwner();
            IPokemobBelt cap = BeltPlayerData.getBelt(player);
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
}
