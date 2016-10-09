package thut.wearables;

import java.util.HashSet;
import java.util.UUID;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import thut.wearables.client.gui.GuiEvents;
import thut.wearables.client.gui.GuiWearables;
import thut.wearables.client.render.WearableEventHandler;
import thut.wearables.inventory.ContainerWearables;
import thut.wearables.inventory.PlayerWearables;
import thut.wearables.inventory.WearableHandler;
import thut.wearables.network.PacketGui;
import thut.wearables.network.PacketSyncWearables;

@Mod(modid = ThutWearables.MODID, name = "Thut Wearables", version = ThutWearables.VERSION)
public class ThutWearables
{
    public static final String MODID   = "thut_wearables";
    public static final String VERSION = "1.0.0";

    public static PlayerWearables getWearables(EntityLivingBase wearer)
    {
        return WearableHandler.getInstance().getPlayerData(wearer.getCachedUniqueIdString());
    }

    public static void saveWearables(EntityLivingBase wearer)
    {
        WearableHandler.getInstance().save(wearer.getCachedUniqueIdString());
    }

    public static SimpleNetworkWrapper packetPipeline = new SimpleNetworkWrapper(MODID);

    @SidedProxy
    public static CommonProxy          proxy;
    @Instance(value = MODID)
    public static ThutWearables        instance;

    private boolean                    overworldRules = true;

    @Method(modid = "Baubles")
    @EventHandler
    public void baubles_old(FMLPreInitializationEvent e)
    {
        MinecraftForge.EVENT_BUS.register(new thut.wearables.baubles.BaublesCompat());
    }

    @Method(modid = "baubles")
    @EventHandler
    public void baubles(FMLPreInitializationEvent e)
    {
        MinecraftForge.EVENT_BUS.register(new thut.wearables.baubles.BaublesCompat());
    }

    @Method(modid = "Botania")
    @EventHandler
    public void botania_old(FMLPreInitializationEvent e)
    {
        MinecraftForge.EVENT_BUS.register(new thut.wearables.baubles.BotaniaCompat());
    }

    @Method(modid = "botania")
    @EventHandler
    public void botania(FMLPreInitializationEvent e)
    {
        MinecraftForge.EVENT_BUS.register(new thut.wearables.baubles.BotaniaCompat());
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        proxy.preInit(e);
        Configuration config = new Configuration(e.getSuggestedConfigurationFile());
        config.load();
        overworldRules = config.getBoolean("overworldGamerules", "general", overworldRules,
                "whether to use overworld gamerules for keep inventory");
        config.save();
        packetPipeline.registerMessage(PacketGui.class, PacketGui.class, 1, Side.SERVER);
        packetPipeline.registerMessage(PacketSyncWearables.class, PacketSyncWearables.class, 2, Side.CLIENT);
        MinecraftForge.EVENT_BUS.register(this);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
        CapabilityManager.INSTANCE.register(IActiveWearable.class, new Capability.IStorage<IActiveWearable>()
        {
            @Override
            public NBTBase writeNBT(Capability<IActiveWearable> capability, IActiveWearable instance, EnumFacing side)
            {
                return null;
            }

            @Override
            public void readNBT(Capability<IActiveWearable> capability, IActiveWearable instance, EnumFacing side,
                    NBTBase nbt)
            {
            }
        }, new IActiveWearable()
        {
            @Override
            public void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks)
            {
            }

            @Override
            public EnumWearable getSlot(ItemStack stack)
            {
                return null;
            }

            @Override
            public void onUpdate(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
            {
            }

            @Override
            public void onTakeOff(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
            {
            }

            @Override
            public void onPutOn(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
            {
            }
        }.getClass());
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
            packetPipeline.sendTo(new PacketSyncWearables((EntityPlayer) event.getTarget()),
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
        PlayerWearables cap = ThutWearables.getWearables(player);
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
    public void playerTick(LivingUpdateEvent event)
    {
        if (event.getEntity() instanceof EntityPlayer)
        {
            EntityPlayer wearer = (EntityPlayer) event.getEntity();
            PlayerWearables wearables = getWearables(wearer);
            for (int i = 0; i < 13; i++)
            {
                EnumWearable.tick(wearer, wearables.getStackInSlot(i), i);
            }
        }
        if (event.getEntityLiving().worldObj.isRemote) return;
        if (event.getEntity() instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) event.getEntity();

            if (!syncSchedule.isEmpty() && syncSchedule.contains(player.getUniqueID()) && player.ticksExisted > 20)
            {
                syncWearables(player);
                for (EntityPlayer player2 : event.getEntity().worldObj.playerEntities)
                {
                    packetPipeline.sendTo(new PacketSyncWearables(player2), (EntityPlayerMP) player);
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
        packetPipeline.sendToAll(new PacketSyncWearables(player));
        saveWearables(player);
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
