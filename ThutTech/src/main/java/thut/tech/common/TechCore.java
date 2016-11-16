package thut.tech.common;

import static thut.tech.common.network.PacketPipeline.packetPipeline;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.core.common.CreativeTabThut;
import thut.tech.ThutTechReference;
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
@Mod(modid = ThutTechReference.MOD_ID, name = ThutTechReference.MOD_NAME, dependencies = ThutTechReference.DEPSTRING, version = ThutTechReference.VERSION, acceptedMinecraftVersions = ThutTechReference.MCVERSIONS)
public class TechCore
{
    @SidedProxy(clientSide = ThutTechReference.CLIENT_PROXY_CLASS, serverSide = ThutTechReference.COMMON_PROXY_CLASS)
    public static CommonProxy     proxy;

    @Instance(ThutTechReference.MOD_ID)
    public static TechCore        instance;

    public static CreativeTabThut tabThut = CreativeTabThut.tabThut;

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
        proxy.initClient();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e)
    {
        ItemHandler.registerRecipes();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        BlockHandler.registerBlocks(e);
        ItemHandler.registerItems();
        proxy.preinit(e);

        Configuration config = new Configuration(e.getSuggestedConfigurationFile());
        ConfigHandler.load(config);
        if (!Loader.isModLoaded("tesla")) EntityLift.ENERGYUSE = false;
        packetPipeline = NetworkRegistry.INSTANCE.newSimpleChannel(ThutTechReference.MOD_ID);

        MinecraftForge.EVENT_BUS.register(this);

        packetPipeline.registerMessage(MessageHandlerClient.class, ClientPacket.class, 0, Side.CLIENT);
        packetPipeline.registerMessage(MessageHandlerServer.class, ServerPacket.class, 1, Side.SERVER);

    }

    @Optional.Method(modid = "tesla")
    @EventHandler
    public void preInitTesla(FMLPreInitializationEvent e)
    {
        System.out.println("TESLA LOCATED");
        new TeslaHandler();
    }

    @Optional.Method(modid = "EnderIO")
    @EventHandler
    public void postInitEIO(FMLPostInitializationEvent e)
    {
        // try
        // {
        // System.out.println("ADDING ELEVATOR TO EIO BLACKLIST");
        // Class<?> soulVessel =
        // Class.forName("crazypants.enderio.item.ItemSoulVessel");
        // Class<?> enderIO = Class.forName("crazypants.enderio.EnderIO");
        // Field soulVesselItemField =
        // enderIO.getDeclaredField("itemSoulVessel");
        // Object soulVesselItem = soulVesselItemField.get(null);
        // Field blacklistField = soulVessel.getDeclaredField("blackList");
        // blacklistField.setAccessible(true);
        // String liftName =
        // EntityList.getEntityStringFromClass(EntityLift.class);
        // @SuppressWarnings("unchecked")
        // List<String> blacklist = (List<String>)
        // blacklistField.get(soulVesselItem);
        // boolean has = false;
        // for (String s : blacklist)
        // {
        // if (s != null && s.equals(liftName))
        // {
        // has = true;
        // break;
        // }
        // }
        // if (!has)
        // {
        // blacklist.add(liftName);
        // }
        // }
        // catch (Exception e1)
        // {
        // System.err.println("ERROR ADDING ELEVATOR TO EIO BLACKLIST");
        // e1.printStackTrace();
        // }
    }

    @EventHandler
    public void serverStop(FMLServerStoppedEvent e)
    {
    }
}
