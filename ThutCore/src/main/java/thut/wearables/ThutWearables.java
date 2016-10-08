package thut.wearables;

import java.util.HashSet;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import thut.api.network.PacketHandler;
import thut.core.common.handlers.PlayerDataHandler;
import thut.reference.ThutCoreReference;
import thut.wearables.client.gui.GuiEvents;
import thut.wearables.client.gui.GuiWearables;
import thut.wearables.client.render.WearableEventHandler;
import thut.wearables.inventory.ContainerWearables;
import thut.wearables.inventory.PlayerWearables;
import thut.wearables.network.PacketGui;
import thut.wearables.network.PacketSyncWearables;

@Mod(modid = ThutWearables.MODID, name = "Thut Wearables", version = ThutWearables.VERSION)
public class ThutWearables
{
    public static final String  MODID          = "thut_wearables";
    public static final String  VERSION        = "1.0.0";

    @SidedProxy
    public static CommonProxy   proxy;
    @Instance(value = MODID)
    public static ThutWearables instance;

    private boolean             overworldRules = true;

    private void doMetastuff()
    {
        ModMetadata meta = FMLCommonHandler.instance().findContainerFor(this).getMetadata();
        meta.parent = ThutCoreReference.MOD_ID;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        doMetastuff();
        proxy.preInit(e);

        Configuration config = new Configuration(e.getSuggestedConfigurationFile());
        config.load();
        overworldRules = config.getBoolean("overworldGamerules", "general", overworldRules,
                "whether to use overworld gamerules for keep inventory");
        config.save();

        PlayerDataHandler.dataMap.add(PlayerWearables.class);
        PacketHandler.packetPipeline.registerMessage(PacketGui.class, PacketGui.class, PacketHandler.getMessageID(),
                Side.SERVER);
        PacketHandler.packetPipeline.registerMessage(PacketSyncWearables.class, PacketSyncWearables.class,
                PacketHandler.getMessageID(), Side.CLIENT);
        MinecraftForge.EVENT_BUS.register(this);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
    }

    static HashSet<UUID> syncSchedule = new HashSet<UUID>();

    @SubscribeEvent
    public void PlayerLoggedInEvent(PlayerLoggedInEvent event)
    {
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        if (side == Side.SERVER)
        {
            syncSchedule.add(event.player.getUniqueID());
        }
    }

    @SubscribeEvent
    public void startTracking(StartTracking event)
    {
        if (event.getTarget() instanceof EntityPlayer && event.getEntityPlayer().isServerWorld())
        {
            PacketHandler.packetPipeline.sendTo(new PacketSyncWearables((EntityPlayer) event.getTarget()),
                    (EntityPlayerMP) event.getEntityPlayer());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void dropLoot(PlayerDropsEvent event)
    {
        EntityPlayer player = event.getEntityPlayer();
        GameRules rules = overworldRules ? player.getServer().worldServerForDimension(0).getGameRules()
                : player.getEntityWorld().getGameRules();
        if (rules.getBoolean("keepInventory")) return;
        PlayerWearables cap = PlayerDataHandler.getInstance().getPlayerData(player).getData(PlayerWearables.class);
        for (int i = 0; i < 13; i++)
        {
            ItemStack stack = cap.getStackInSlot(i);
            if (stack != null)
            {
                player.dropItem(stack.copy(), true, false);
                cap.setInventorySlotContents(i, null);
            }
        }
        syncWearables(player);
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
                syncWearables(player);
                for (EntityPlayer player2 : event.getEntity().worldObj.playerEntities)
                {
                    PacketHandler.packetPipeline.sendTo(new PacketSyncWearables(player2), (EntityPlayerMP) player);
                }
                syncSchedule.remove(player.getUniqueID());
            }
        }
    }

    public static void syncWearables(EntityPlayer player)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            Thread.dumpStack();
            return;
        }
        PacketHandler.packetPipeline.sendToAll(new PacketSyncWearables(player));
    }

    public static class CommonProxy implements IGuiHandler
    {
        public void preInit(FMLPreInitializationEvent event)
        {
        }

        @Override
        public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
        {
            return new ContainerWearables(player);
        }

        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
        {
            return null;
        }
    }

    public static class ServerProxy extends CommonProxy
    {
        @Override
        public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
        {
            return super.getServerGuiElement(ID, player, world, x, y, z);
        }
    }

    public static class ClientProxy extends CommonProxy
    {
        @Override
        public void preInit(FMLPreInitializationEvent event)
        {
            GuiEvents.init();
            MinecraftForge.EVENT_BUS.register(new WearableEventHandler());
        }

        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
        {
            return new GuiWearables(player);
        }
    }
}
