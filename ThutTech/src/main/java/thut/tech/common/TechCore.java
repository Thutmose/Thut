package thut.tech.common;

import static thut.tech.common.network.PacketPipeline.packetPipeline;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import thut.api.ThutBlocks;
import thut.api.maths.Vector3;
import thut.api.network.PacketHandler;
import thut.core.common.CreativeTabThut;
import thut.lib.CompatWrapper;
import thut.tech.Reference;
import thut.tech.common.blocks.lift.BlockLift;
import thut.tech.common.blocks.lift.BlockLift.EnumType;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.blocks.lift.TileIDFixer;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.handlers.BlockHandler;
import thut.tech.common.handlers.ConfigHandler;
import thut.tech.common.handlers.EnergyHandler;
import thut.tech.common.handlers.ItemHandler;
import thut.tech.common.network.PacketPipeline.ClientPacket;
import thut.tech.common.network.PacketPipeline.ClientPacket.MessageHandlerClient;
import thut.tech.common.network.PacketPipeline.ServerPacket;
import thut.tech.common.network.PacketPipeline.ServerPacket.MessageHandlerServer;

@SuppressWarnings("deprecation")
@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, acceptableRemoteVersions = Reference.MINVERSION, dependencies = Reference.DEPSTRING, version = Reference.VERSION)
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
            stack.setTag(JsonToNBT.getTagFromJson(name));
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
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void livingRender(RenderLivingEvent.Post evt)
    {
        if (!Minecraft.getInstance().getRenderManager().isDebugBoundingBox()) return;
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

        // Register the energy handler to bus.
        MinecraftForge.EVENT_BUS.register(new EnergyHandler());
    }

    @EventHandler
    public void preInit(FMLCommonSetupEvent e)
    {
        proxy.preinit(e);

        Configuration config = new Configuration(e.getSuggestedConfigurationFile());
        ConfigHandler.load(config);
        packetPipeline = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);

        MinecraftForge.EVENT_BUS.register(this);

        packetPipeline.registerMessage(MessageHandlerClient.class, ClientPacket.class, 0, Dist.CLIENT);
        packetPipeline.registerMessage(MessageHandlerServer.class, ServerPacket.class, 1, Dist.DEDICATED_SERVER);

        EntityRegistry.registerModEntity(new ResourceLocation(Reference.MOD_ID, "lift"), EntityLift.class, "lift", 0,
                this, 256, 1, true);
        FMLCommonHandler.instance().getDataFixer().init(Reference.MOD_ID, 88888).registerFix(FixTypes.BLOCK_ENTITY,
                new TileIDFixer());
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

    @SubscribeEvent
    public void interactRightClickBlock(PlayerInteractEvent.RightClickBlock evt)
    {
        if (evt.getHand() == Hand.OFF_HAND || evt.getWorld().isRemote || !CompatWrapper.isValid(evt.getItemStack())
                || !evt.getPlayerEntity().isSneaking() || evt.getItemStack().getItem() != Items.STICK
                || evt.getFace() == Direction.DOWN || evt.getFace() == Direction.UP)
            return;
        BlockState state = evt.getWorld().getBlockState(evt.getPos());
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
                if (te.lift.hasFloors[button - 1] && button == te.floor)
                {
                    te.lift.hasFloors[button - 1] = false;
                    te.lift.floors[button - 1] = 0;
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
}
