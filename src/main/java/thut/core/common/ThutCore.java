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
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.oredict.OreDictionary;
import thut.api.CreativeTabThut;
import thut.api.ThutBlocks;
import thut.api.explosion.ExplosionCustom.Cruncher;
import thut.api.maths.Vector3;
import thut.api.network.PacketPipeline;
import thut.core.common.handlers.ConfigHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod( modid = "ThutCore", name="ThutCore", version="0.0.1")

public class ThutCore {

	@SidedProxy(clientSide = "thut.core.client.ClientProxy", serverSide = "thut.core.common.CommonProxy")
	public static CommonProxy proxy;
	public static final String TEXTURE_PATH = "core:";
		
	@Instance("ThutCore")
	public static ThutCore instance;
	
	public static String modid = "ThutCore";
	
	private static final String[]  LANGUAGES_SUPPORTED	= new String[] { "en_UK", "en_US" , "de_DE"};
    
    public static CreativeTabThut tabThut = CreativeTabThut.tabThut;
    
	public static Block[] blocks;
	public static Item[] items;
	
	public static Class test;
	
	public static BiomeGenBase volcano;
	public static BiomeGenBase chalk;

	// Configuration Handler that handles the config file
	public ConfigHandler config;

	@EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
		config = new ConfigHandler(e.getSuggestedConfigurationFile());
		proxy.loadSounds();
		
		MinecraftForge.EVENT_BUS.register(this);
		
		Cruncher sort = new Cruncher();
	}
	
	  @EventHandler
	    public void load(FMLInitializationEvent evt)
	    {
		proxy.initClient();
		proxy.registerEntities();
		proxy.registerTEs();
		
		PacketPipeline.packetPipeline.initalise();
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent e)
	{
		PacketPipeline.packetPipeline.postInitialise();
	}
}