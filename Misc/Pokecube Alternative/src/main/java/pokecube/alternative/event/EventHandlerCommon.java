package pokecube.alternative.event;

import java.util.HashSet;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.adventures.items.ItemBadge;
import pokecube.alternative.Config;
import pokecube.alternative.Reference;
import pokecube.alternative.container.belt.BeltPlayerData;
import pokecube.alternative.container.belt.IPokemobBelt;
import pokecube.alternative.container.card.CardPlayerData;
import pokecube.alternative.network.PacketHandler;
import pokecube.alternative.network.PacketSyncBelt;
import pokecube.core.blocks.pc.InventoryPC;
import pokecube.core.events.RecallEvent;
import pokecube.core.events.handlers.PCEventsHandler;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.maths.Vector3;
import thut.core.common.handlers.PlayerDataHandler;
import thut.lib.CompatWrapper;

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
        if (player.world.isRemote) return;
        if (CompatWrapper.isValid(item) && item.hasTagCompound())
        {
        }
        if (CompatWrapper.isValid(item) && item.getItem() instanceof ItemBadge && item.hasTagCompound())
        {
            CardPlayerData data = PlayerDataHandler.getInstance().getPlayerData(player).getData(CardPlayerData.class);
            NBTTagCompound tag = item.getTagCompound();
            String type = tag.getString("type");
            int index = -1;
            switch (type)
            {
            case "badgeelectric":
                index = 0;
                break;
            case "badgerock":
                index = 1;
                break;
            case "badgegrass":
                index = 2;
                break;
            case "badgenormal":
                index = 3;
                break;
            case "badgefire":
                index = 4;
                break;
            case "badgepsychic":
                index = 5;
                break;
            case "badgedragon":
                index = 6;
                break;
            case "badgewater":
                index = 7;
                break;
            }
            if (index == -1 || CompatWrapper.isValid(data.inventory.getStackInSlot(index))) return;
            int slotIndex = event.getEntityPlayer().inventory.currentItem;
            data.inventory.setInventorySlotContents(index, item.copy());
            player.inventory.setInventorySlotContents(slotIndex, CompatWrapper.nullStack);
            PlayerDataHandler.getInstance().save(player.getCachedUniqueIdString(), data.getIdentifier());
            event.setCanceled(true);
            return;
        }

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
                        ItemStack stack = cap.getCube(i);
                        if (!CompatWrapper.isValid(stack)) continue;
                        if (PokecubeManager.getUUID(item).equals(PokecubeManager.getUUID(stack)))
                        {
                            cap.setCube(i, item);
                            toBelt = true;
                            break;
                        }
                    }
                    if (!toBelt) for (int i = 0; i < 6; i++)
                    {
                        if (!CompatWrapper.isValid(cap.getCube(i)))
                        {
                            cap.setCube(i, item);
                            player.inventory.setInventorySlotContents(slotIndex, CompatWrapper.nullStack);
                            syncPokemon(player);
                            toBelt = true;
                            break;
                        }
                    }
                    if (toBelt)
                    {
                        player.sendMessage(new TextComponentTranslation(Reference.MODID + ".pokebelt.tobelt",
                                item.getDisplayName()));
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
        if (Config.instance.autoThrow && event.getEntityLiving() instanceof EntityPlayer
                && event.getSource().getImmediateSource() instanceof IPokemob)
        {
            if (PCEventsHandler.getOutMobs(event.getEntityLiving()).isEmpty())
            {
                IPokemobBelt cap = BeltPlayerData.getBelt(event.getEntityLiving());
                if (CompatWrapper.isValid(cap.getCube(cap.getSlot())))
                {
                    ItemStack cube = cap.getCube(cap.getSlot());
                    if (PokecubeManager.isFilled(cube))
                    {
                        Entity target = event.getSource().getImmediateSource();
                        Vector3 here = Vector3.getNewVector().set(this);
                        Vector3 t = Vector3.getNewVector().set(target);
                        t.set(t.subtractFrom(here).scalarMultBy(0.5).addTo(here));
                        ((IPokecube) cube.getItem()).throwPokecubeAt(target.getEntityWorld(), event.getEntityLiving(),
                                cube, t, null);
                        ITextComponent text = new TextComponentTranslation("pokecube.trainer.toss",
                                event.getEntityLiving().getDisplayName(), cube.getDisplayName());
                        cap.setCube(cap.getSlot(), CompatWrapper.nullStack);
                        syncPokemon((EntityPlayer) event.getEntityLiving());
                        target.sendMessage(text);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void playerTick(PlayerEvent.LivingUpdateEvent event)
    {
        if (event.getEntityLiving().world.isRemote) return;
        if (event.getEntity() instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) event.getEntity();

            if (!syncSchedule.isEmpty() && syncSchedule.contains(player.getUniqueID()) && player.ticksExisted > 20)
            {
                syncPokemon(player);
                for (EntityPlayer player2 : event.getEntity().world.playerEntities)
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void recallPokemon(RecallEvent event)
    {
        IPokemob pokemon = event.recalled;
        if (pokemon == null) return;
        if (pokemon.getPokemonOwner() instanceof EntityPlayer)
        {
            if (pokemon.getPokemonAIState(IPokemob.MEGAFORME))
                pokemon = pokemon.megaEvolve(pokemon.getPokedexEntry().getBaseForme());
            ItemStack pokemonStack = PokecubeManager.pokemobToItem(pokemon);
            EntityPlayer player = (EntityPlayer) pokemon.getPokemonOwner();
            IPokemobBelt cap = BeltPlayerData.getBelt(player);
            boolean added = false;
            for (int i = 0; i < 6; i++)
            {
                ItemStack stack = cap.getCube(i);
                if (!CompatWrapper.isValid(stack)) continue;
                if (PokecubeManager.getUUID(pokemonStack).equals(PokecubeManager.getUUID(stack)))
                {
                    cap.setCube(i, pokemonStack);
                    added = true;
                    break;
                }
            }
            if (!added) for (int i = 0; i < 6; i++)
            {
                ItemStack stack = cap.getCube(i);
                if (!CompatWrapper.isValid(stack)
                        || PokecubeManager.getUUID(pokemonStack).equals(PokecubeManager.getUUID(stack)))
                {
                    cap.setCube(i, pokemonStack);
                    added = true;
                    break;
                }
            }
            if (added)
            {
                ITextComponent mess = new TextComponentTranslation("pokemob.action.return",
                        pokemon.getPokemonDisplayName().getFormattedText());
                pokemon.displayMessageToOwner(mess);
            }
            else
            {
                InventoryPC.addPokecubeToPC(pokemonStack, player.getEntityWorld());
            }
            syncPokemon(player);
            ((Entity) pokemon).setDead();
            pokemon.setPokemonOwner((UUID) null);
            MinecraftForge.EVENT_BUS.post(new AddCube.OnRecall(player, pokemonStack));
            event.setCanceled(true);
        }
    }
}
