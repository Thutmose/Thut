package thut.tech.common;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import thut.core.common.ThutCore;
import thut.core.common.config.Config;
import thut.core.common.network.PacketHandler;
import thut.tech.Reference;
import thut.tech.client.ClientProxy;
import thut.tech.common.blocks.lift.BlockLift;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.handlers.ConfigHandler;
import thut.tech.common.items.ItemLinker;
import thut.tech.common.items.RecipeReset;

@Mod(value = Reference.MOD_ID)
public class TechCore
{
    // You can use EventBusSubscriber to automatically subscribe events on the
    // contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents
    {

        @SubscribeEvent
        public static void registerBlocks(final RegistryEvent.Register<Block> event)
        {
            if (!ModLoadingContext.get().getActiveContainer().getModId().equals(Reference.MOD_ID)) return;
            event.getRegistry().register(TechCore.LIFTCONTROLLER);
        }

        @SubscribeEvent
        public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event)
        {
            if (!ModLoadingContext.get().getActiveContainer().getModId().equals(Reference.MOD_ID)) return;
            // register a new mob here
            EntityLift.TYPE.setRegistryName(Reference.MOD_ID, "lift");
            event.getRegistry().register(EntityLift.TYPE);
        }

        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event)
        {
            if (!ModLoadingContext.get().getActiveContainer().getModId().equals(Reference.MOD_ID)) return;
            event.getRegistry().register(TechCore.LIFT);
            event.getRegistry().register(TechCore.LINKER);
            final BlockItem controller = new BlockItem(TechCore.LIFTCONTROLLER, new Item.Properties().group(
                    ThutCore.THUTITEMS));
            controller.setRegistryName(TechCore.LIFTCONTROLLER.getRegistryName());
            event.getRegistry().register(controller);
            ThutCore.THUTICON = new ItemStack(TechCore.LINKER);
        }

        @SubscribeEvent
        public static void registerRecipes(final RegistryEvent.Register<IRecipeSerializer<?>> event)
        {
            if (!ModLoadingContext.get().getActiveContainer().getModId().equals(Reference.MOD_ID)) return;
            event.getRegistry().register(RecipeReset.SERIALIZER);
        }

        @SubscribeEvent
        public static void registerTiles(final RegistryEvent.Register<TileEntityType<?>> event)
        {
            if (!ModLoadingContext.get().getActiveContainer().getModId().equals(Reference.MOD_ID)) return;
            // register a new mob here
            TileEntityLiftAccess.TYPE.setRegistryName(Reference.MOD_ID, "controller");
            event.getRegistry().register(TileEntityLiftAccess.TYPE);
        }
    }

    public final static PacketHandler packets        = new PacketHandler(new ResourceLocation(Reference.MOD_ID,
            "comms"), Reference.NETVERSION);
    public static final CommonProxy   proxy          = DistExecutor.runForDist(() -> () -> new ClientProxy(),
            () -> () -> new CommonProxy());
    public static final Block         LIFTCONTROLLER = new BlockLift(Block.Properties.create(Material.IRON)
            .hardnessAndResistance(3.5f)).setRegistryName(Reference.MOD_ID, "controller");
    public static final Item          LIFT           = new Item(new Item.Properties().group(ThutCore.THUTITEMS))
            .setRegistryName(Reference.MOD_ID, "lift");
    public static final Item          LINKER         = new ItemLinker(new Item.Properties().group(ThutCore.THUTITEMS))
            .setRegistryName(Reference.MOD_ID, "linker");

    public static TechCore instance;

    // public static Logger logger = Logger.getLogger("thuttech");
    // protected static FileHandler logHandler = null;
    //
    // private static void initLogger()
    // {
    // logger.setLevel(Level.ALL);
    // try
    // {
    // File logfile = new File("." + File.separator + "logs", "thuttech.log");
    // if ((logfile.exists() || logfile.createNewFile()) && logfile.canWrite()
    // && logHandler == null)
    // {
    // logHandler = new FileHandler(logfile.getPath());
    // logHandler.setFormatter(new LogFormatter());
    // logger.addHandler(logHandler);
    // }
    // }
    // catch (SecurityException | IOException e)
    // {
    // e.printStackTrace();
    // }
    // }
    //
    // public static ItemStack getInfoBook()
    // {
    // String name = I18n.translateToLocal("ttinfobook.json");
    // ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
    // try
    // {
    // stack.setTag(JsonToNBT.getTagFromJson(name));
    // }
    // catch (NBTException e)
    // {
    // e.printStackTrace();
    // }
    // return stack;
    // }

    public static final ConfigHandler config = new ConfigHandler(Reference.MOD_ID);

    public TechCore()
    {
        TechCore.instance = this;
        MinecraftForge.EVENT_BUS.register(this);

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(TechCore.proxy::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(TechCore.proxy::setupClient);

        // Register Config stuff
        Config.setupConfigs(TechCore.config, Reference.MOD_ID, Reference.MOD_ID);
    }

    @SubscribeEvent
    public void interactRightClickBlock(final PlayerInteractEvent.RightClickBlock evt)
    {
        // if (evt.getHand() == Hand.OFF_HAND || evt.getWorld().isRemote ||
        // !CompatWrapper.isValid(evt.getItemStack())
        // || !evt.getPlayerEntity().isSneaking() ||
        // evt.getItemStack().getItem() != Items.STICK
        // || evt.getFace() == Direction.DOWN || evt.getFace() == Direction.UP)
        // return;
        // BlockState state = evt.getWorld().getBlockState(evt.getPos());
        // if (state.getBlock() == ThutBlocks.lift &&
        // state.getValue(BlockLift.VARIANT) == EnumType.CONTROLLER)
        // {
        // TileEntityLiftAccess te = (TileEntityLiftAccess)
        // evt.getWorld().getTileEntity(evt.getPos());
        // Vector3 hit = Vector3.getNewVector().set(evt.getHitVec());
        // hit.x -= evt.getPos().getX();
        // hit.y -= evt.getPos().getY();
        // hit.z -= evt.getPos().getZ();
        // if (te.lift != null)
        // {
        // int button = te.getButtonFromClick(evt.getFace(), (float) hit.x,
        // (float) hit.y, (float) hit.z);
        // if (te.lift.hasFloors[button - 1] && button == te.floor)
        // {
        // te.lift.hasFloors[button - 1] = false;
        // te.lift.floors[button - 1] = 0;
        // Entity lift = te.lift;
        // te.lift = null;
        // te.liftID = null;
        // te.floor = 0;
        // PacketHandler.sendTileUpdate(te);
        // EntityUpdate.sendEntityUpdate(lift);
        // }
        // }
        // }
    }

    @SuppressWarnings("rawtypes")
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void livingRender(final RenderLivingEvent.Post evt)
    {
        if (!Minecraft.getInstance().getRenderManager().isDebugBoundingBox()) return;
    }
}
