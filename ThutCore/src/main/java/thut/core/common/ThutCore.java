package thut.core.common;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.ForgeVersion.CheckResult;
import net.minecraftforge.common.ForgeVersion.Status;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import thut.api.TickHandler;
import thut.api.maths.Cruncher;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.TerrainManager;
import thut.core.common.handlers.ConfigHandler;
import thut.reference.ThutCoreReference;

@Mod(modid = ThutCoreReference.MOD_ID, name = ThutCoreReference.MOD_NAME, version = ThutCoreReference.VERSION, updateJSON = ThutCoreReference.UPDATEURL, acceptedMinecraftVersions = ThutCoreReference.MCVERSIONS)
public class ThutCore
{

    @SidedProxy(clientSide = ThutCoreReference.CLIENT_PROXY_CLASS, serverSide = ThutCoreReference.COMMON_PROXY_CLASS)
    public static CommonProxy     proxy;

    @Instance(ThutCoreReference.MOD_ID)
    public static ThutCore        instance;

    public static final String    modid   = ThutCoreReference.MOD_ID;
    public static CreativeTabThut tabThut = CreativeTabThut.tabThut;

    public static Block[]         blocks;
    public static Item[]          items;

    public static BiomeGenBase    volcano;
    public static BiomeGenBase    chalk;

    // Configuration Handler that handles the config file
    public ConfigHandler          config;

    public ThutCore()
    {
        BiomeDatabase.getNameFromType(0);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        proxy.preinit(e);
        config = new ConfigHandler(e.getSuggestedConfigurationFile());
        proxy.loadSounds();
        TerrainManager.getInstance();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new TickHandler());
        new Cruncher();
    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        proxy.initClient();
        proxy.registerEntities();
        proxy.registerTEs();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e)
    {
        new UpdateNotifier();
    }

    public static class UpdateNotifier
    {
        public UpdateNotifier()
        {
            MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onPlayerJoin(TickEvent.PlayerTickEvent event)
        {
            if (event.player.worldObj.isRemote && event.player == FMLClientHandler.instance().getClientPlayerEntity())
            {
                MinecraftForge.EVENT_BUS.unregister(this);
                Object o = Loader.instance().getIndexedModList().get(ThutCoreReference.MOD_ID);
                CheckResult result = ForgeVersion.getResult(((ModContainer) o));
                if (result.status == Status.OUTDATED)
                {
                    IChatComponent mess = getOutdatedMessage(result, "Thut Core ");
                    (event.player).addChatMessage(mess);
                }

            }
        }
    }

    public static IChatComponent getOutdatedMessage(CheckResult result, String name)
    {
        String linkName = "[" + EnumChatFormatting.GREEN + name + result.target + EnumChatFormatting.WHITE;
        String link = "" + result.url;
        String linkComponent = "{\"text\":\"" + linkName + "\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\""
                + link + "\"}}";

        String info = "\"" + EnumChatFormatting.RED + "New " + name
                + " version available, please update before reporting bugs.\nClick the green link for the page to download.\n"
                + "\"";
        String mess = "[" + info + "," + linkComponent + ",\"]\"]";
        return IChatComponent.Serializer.jsonToComponent(mess);
    }
}