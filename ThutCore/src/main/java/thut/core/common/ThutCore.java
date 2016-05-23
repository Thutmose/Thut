package thut.core.common;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
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

    public static Biome    volcano;
    public static Biome    chalk;

    // Configuration Handler that handles the config file
    public ConfigHandler          config;

    public ThutCore()
    {
        BiomeDatabase.getNameFromType(0);
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
}