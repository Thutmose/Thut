package thut.tech.common;

import static thut.tech.common.network.PacketPipeline.packetPipeline;

import net.minecraft.block.Block;
import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.api.ThutBlocks;
import thut.api.maths.Vector3;
import thut.api.network.PacketHandler;
import thut.core.common.CreativeTabThut;
import thut.lib.CompatWrapper;
import thut.tech.Reference;
import thut.tech.common.blocks.lift.BlockLift;
import thut.tech.common.blocks.lift.BlockLift.EnumType;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.handlers.BlockHandler;
import thut.tech.common.handlers.ConfigHandler;
import thut.tech.common.handlers.ItemHandler;
import thut.tech.common.network.PacketPipeline.ClientPacket;
import thut.tech.common.network.PacketPipeline.ClientPacket.MessageHandlerClient;
import thut.tech.common.network.PacketPipeline.ServerPacket;
import thut.tech.common.network.PacketPipeline.ServerPacket.MessageHandlerServer;
import thut.tech.common.tesla.TeslaHandler;

@SuppressWarnings("deprecation")
@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, dependencies = Reference.DEPSTRING, version = Reference.VERSION)
public class TechCore
{
    @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.COMMON_PROXY_CLASS)
    public static CommonProxy     proxy;

    @Instance(Reference.MOD_ID)
    public static TechCore        instance;

    public static CreativeTabThut tabThut    = CreativeTabThut.tabThut;
    public static Logger          logger     = Logger.getLogger("thuttech");
    protected static FileHandler  logHandler = null;

    private static void initLogger()
    {
        logger.setLevel(Level.ALL);
        try
        {
            File logfile = new File("." + File.separator + "logs", "thuttech.log");
            if ((logfile.exists() || logfile.createNewFile()) && logfile.canWrite() && logHandler == null)
            {
                logHandler = new FileHandler(logfile.getPath());
                logHandler.setFormatter(new LogFormatter());
                logger.addHandler(logHandler);
            }
        }
        catch (SecurityException | IOException e)
        {
            e.printStackTrace();
        }
    }

    public static ItemStack getInfoBook()
    {
        String name = I18n.translateToLocal("ttinfobook.json");
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        try
        {
            stack.setTagCompound(JsonToNBT.getTagFromJson(name));
        }
        catch (NBTException e)
        {
            e.printStackTrace();
        }
        return stack;
    }

    public TechCore()
    {
        BlockLift.init();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SuppressWarnings("rawtypes")
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void livingRender(RenderLivingEvent.Post evt)
    {
        if (!Minecraft.getMinecraft().getRenderManager().isDebugBoundingBox()) return;
    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        initLogger();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e)
    {
        ItemHandler.registerRecipes();
        proxy.initClient();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        proxy.preinit(e);

        Configuration config = new Configuration(e.getSuggestedConfigurationFile());
        ConfigHandler.load(config);
        if (!Loader.isModLoaded("tesla")) EntityLift.ENERGYUSE = false;
        packetPipeline = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);

        MinecraftForge.EVENT_BUS.register(this);

        packetPipeline.registerMessage(MessageHandlerClient.class, ClientPacket.class, 0, Side.CLIENT);
        packetPipeline.registerMessage(MessageHandlerServer.class, ServerPacket.class, 1, Side.SERVER);

        EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID, "lift"), EntityLift.class, "lift", 0,
                this, 32, 1, true);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event)
    {
        ItemHandler.registerItems(event);
        proxy.registerItemModels();
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event)
    {
        BlockHandler.registerBlocks(event);
        proxy.registerBlockModels();
    }

    @Optional.Method(modid = "tesla")
    @EventHandler
    public void preInitTesla(FMLPreInitializationEvent e)
    {
        System.out.println("TESLA LOCATED");
        new TeslaHandler();
    }

    @SubscribeEvent
    public void interactRightClickBlock(PlayerInteractEvent.RightClickBlock evt)
    {
        if (evt.getHand() == EnumHand.OFF_HAND || evt.getWorld().isRemote || !CompatWrapper.isValid(evt.getItemStack())
                || !evt.getEntityPlayer().isSneaking() || evt.getItemStack().getItem() != Items.STICK
                || evt.getFace() == EnumFacing.DOWN || evt.getFace() == EnumFacing.UP)
            return;
        IBlockState state = evt.getWorld().getBlockState(evt.getPos());
        if (state.getBlock() == ThutBlocks.lift && state.getValue(BlockLift.VARIANT) == EnumType.CONTROLLER)
        {
            TileEntityLiftAccess te = (TileEntityLiftAccess) evt.getWorld().getTileEntity(evt.getPos());
            Vector3 hit = Vector3.getNewVector().set(evt.getHitVec());
            hit.x -= evt.getPos().getX();
            hit.y -= evt.getPos().getY();
            hit.z -= evt.getPos().getZ();
            if (te.lift != null)
            {
                int button = te.getButtonFromClick(evt.getFace(), (float) hit.x, (float) hit.y, (float) hit.z);
                if (te.lift.floors[button - 1] != -1 && button == te.floor)
                {
                    te.lift.floors[button - 1] = -1;
                    Entity lift = te.lift;
                    te.lift = null;
                    te.liftID = null;
                    te.floor = 0;
                    PacketHandler.sendTileUpdate(te);
                    PacketHandler.sendEntityUpdate(lift);
                }
            }
        }
    }

    @EventHandler
    public void serverStop(FMLServerStoppedEvent e)
    {
    }
}
