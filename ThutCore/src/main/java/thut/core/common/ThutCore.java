package thut.core.common;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.oredict.OreDictionary;
import thut.api.ThutBlocks;
import thut.api.TickHandler;
import thut.api.maths.Cruncher;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.core.client.render.model.ModelFluid;
import thut.core.common.handlers.ConfigHandler;
import thut.reference.ThutCoreReference;

@Mod(modid = ThutCoreReference.MOD_ID, name = ThutCoreReference.MOD_NAME, version = ThutCoreReference.VERSION)

public class ThutCore
{

    @SidedProxy(clientSide = ThutCoreReference.CLIENT_PROXY_CLASS, serverSide = ThutCoreReference.COMMON_PROXY_CLASS)
    public static CommonProxy proxy;

    @Instance(ThutCoreReference.MOD_ID)
    public static ThutCore instance;

    public static final String modid = ThutCoreReference.MOD_ID;

    public static CreativeTabThut tabThut = CreativeTabThut.tabThut;

    public static Block[] blocks;
    public static Item[]  items;

    public static Class test;

    public static BiomeGenBase volcano;
    public static BiomeGenBase chalk;

    // Configuration Handler that handles the config file
    public ConfigHandler config;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        proxy.preinit(e);
        config = new ConfigHandler(e.getSuggestedConfigurationFile());
        proxy.loadSounds();
        TerrainManager.getInstance();
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(new TickHandler());
        Cruncher sort = new Cruncher();
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
}