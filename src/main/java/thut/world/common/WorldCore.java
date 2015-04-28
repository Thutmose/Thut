package thut.world.common;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import thut.api.ThutBlocks;
import thut.api.explosion.ExplosionCustom.Cruncher;
import thut.api.maths.Vector3;
import thut.api.network.PacketPipeline;
import thut.core.common.CreativeTabThut;
import thut.world.common.blocks.fluids.solids.BlockSolidLava;
import thut.world.common.corehandlers.BlockHandler;
import thut.world.common.corehandlers.ConfigHandler;
import thut.world.common.corehandlers.ItemHandler;
import thut.world.common.corehandlers.LiquidHandler;
import thut.world.common.corehandlers.RecipeHandler;
import thut.world.common.corehandlers.WorldEventHandler;
import thut.world.common.worldgen.BiomeGenChalk;
import thut.world.common.worldgen.BiomeVolcano;
import thut.world.common.worldgen.TrassWorldGen;
import thut.world.common.worldgen.VolcanoWorldGen;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod( modid = "thutworld", name="Thut's WorldGen", version="0.0.1")
public class WorldCore {

	@SidedProxy(clientSide = "thut.world.client.ClientProxy", serverSide = "thut.world.common.CommonProxy")
	public static CommonProxy proxy;
	public static final String TEXTURE_PATH = "thutworld:";
		
	@Instance("thutworld")
	public static WorldCore instance;
	
	public static String modid = "thutworld";
    
    public static CreativeTabThut tabThut = CreativeTabThut.tabThut;
    
	public static Block[] blocks;
	public static Item[] items;
	
	public static Class test;
	
	public WorldEventHandler loader;
	
	public LiquidHandler liquidHndlr;
	public BlockHandler blockList;
	public ItemHandler itemList;
	public RecipeHandler recipes;
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
		
		loader = new WorldEventHandler(config.ChunkSize);
		MinecraftForge.EVENT_BUS.register(loader);
		

		blockList = new BlockHandler(config);
		itemList = new ItemHandler(config);
	}
	
	  @EventHandler
	    public void load(FMLInitializationEvent evt)
	    {


//			blockList = new BlockHandler(config);
//			itemList = new ItemHandler(config);
		liquidHndlr = new LiquidHandler();
		MinecraftForge.EVENT_BUS.register(liquidHndlr);
		GameRegistry.registerWorldGenerator(new TrassWorldGen(), 10);
		GameRegistry.registerWorldGenerator(new VolcanoWorldGen(), 10);
		
		proxy.registerEntities();
		proxy.registerTEs();
		
		populateMap();
		NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
		
		recipes = new RecipeHandler(config);
		items = itemList.items;
		blocks = blockList.blocks;
	//*	
		chalk = new BiomeGenChalk(config.IDBiome);
		volcano = new BiomeVolcano(config.IDBiome+1);
	//	GameRegistry.addBiome(chalk);TODO biome adding
		BiomeDictionary.registerBiomeType(volcano, Type.NETHER, Type.MOUNTAIN);
		BiomeDictionary.registerBiomeType(chalk, Type.PLAINS);
		BiomeManager.addVillageBiome(chalk, true);
		initOreMap();
		
		PacketPipeline.packetPipeline.initalise();
	//*/	
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent e)
	{
		initHardens();
		recipes.registerRecipes();
		PacketPipeline.packetPipeline.postInitialise();
		
		initPokecubeCompat();
	}
	
	public static void initPokecubeCompat()
	{
		try {
			Class<?> pokecubeCoreHelper = Class.forName("pokecube.core.Mod_Pokecube_Helper");
			Class<?> pokecubeCore = Class.forName("pokecube.core.mod_Pokecube");
			
        	Method m = pokecubeCoreHelper.getDeclaredMethod("getSurfaceBlocks");
        	List<Block> blocks = (List<Block>) m.invoke(null);
        	
        	if(blocks!=null)
        	{
        		for(Block b: ThutBlocks.solidLavas)
        		if(!blocks.contains(b))
        			blocks.add(b);
        	}
        	
        	m = pokecubeCoreHelper.getDeclaredMethod("getCaveBlocks");
        	blocks = (List<Block>) m.invoke(null);

        	if(blocks!=null)
        	{
        		for(Block b: ThutBlocks.solidLavas)
        		if(!blocks.contains(b))
        			blocks.add(b);
        	}
        	
        	m = pokecubeCore.getDeclaredMethod("getEntityClassFromPokedexNumber", int.class);
        	
        	test = (Class) m.invoke(null, Integer.valueOf(41));
			
		} catch (ClassNotFoundException ex) {
		} catch (Exception ex)
		{
			ex.printStackTrace();//TODO remove this
		}
	}
	
	static int entityID=0;
	public static void registerEntity(Class<? extends Entity> clas, String name, int freq){
		EntityRegistry.registerGlobalEntityID(clas, name, EntityRegistry.findGlobalUniqueEntityId());
		EntityRegistry.registerModEntity(clas, name, entityID++, WorldCore.instance, 50, 1, true);
	}
	public static void registerEntity(Class<? extends Entity> clas, String name){
		registerEntity(clas, name, 1);
	}
    
	public static Map<Short, Byte> colourMap = new HashMap<Short, Byte>();
	public static Map<String, Integer> oreMap0 = new HashMap<String, Integer>();
	public static Map<String, Integer> oreMap1 = new HashMap<String, Integer>();
	public static Map<String, Integer> oreMap2 = new HashMap<String, Integer>();
	public static List<String> ores = new ArrayList<String>();
	public static final Vector3 g = new Vector3(0,-0.06,0);
	
	void populateMap(){
		
		//White gives
		colourMap.put((short)(0 + 14 * 16),(byte) 6);
		colourMap.put((short)(0 + 15 * 16),(byte) 7);
		colourMap.put((short)(0 + 13 * 16),(byte) 5);
		colourMap.put((short)(0 + 11 * 16),(byte) 3);
		colourMap.put((short)(0 + 7 * 16),(byte) 8);
		
		// Pink Gives:
		colourMap.put((short)(6 + 10 * 16),(byte) 2);
		
		//Yellow gives
		colourMap.put((short)(4 + 14 * 16),(byte) 1);
		colourMap.put((short)(4 + 11 * 16),(byte) 13);
		
		//Light Blue gives
		colourMap.put((short)(3 + 4 * 16),(byte) 5);

		//Dark Blue gives
		colourMap.put((short)(11 + 14 * 16),(byte) 10);
		colourMap.put((short)(11 + 13 * 16),(byte) 9);
		
		//Dark green gives
		colourMap.put((short)(13 + 14 * 16), (byte)12);
		
		
	}
	
	
	public void initOreMap()
	{
		oreMap0.put("copper", 500);
		oreMap0.put("tin", 500);
		oreMap0.put("thorium", 120);
		oreMap0.put("uranium", 40);
		oreMap0.put("tungsten", 40);
		oreMap0.put("iron", 1000);
		oreMap0.put("chromium", 500);
		oreMap0.put("osmium", 10);
		oreMap0.put("iridium", 10);
		oreMap0.put("silver", 20);
		oreMap0.put("aluminium", 150);
		oreMap0.put("platinum", 20);
		oreMap0.put("diamond", 10);
		oreMap0.put("emerald", 30);
		oreMap0.put("ruby", 30);
		oreMap0.put("sapphire", 30);
		oreMap0.put("quartz", 100);
		oreMap0.put("lead", 100);
		oreMap0.put("zinc", 500);
		oreMap0.put("redstone", 20);
		
		oreMap1.put("copper", 500);
		oreMap1.put("tin", 250);
		oreMap1.put("thorium", 120);
		oreMap1.put("uranium", 40);
		oreMap1.put("tungsten", 40);
		oreMap1.put("iron", 1000);
		oreMap1.put("chromium", 500);
		oreMap1.put("osmium", 10);
		oreMap1.put("iridium", 10);
		oreMap1.put("silver", 20);
		oreMap1.put("aluminium", 150);
		oreMap1.put("platinum", 20);
		oreMap1.put("diamond", 10);
		oreMap1.put("emerald", 30);
		oreMap1.put("ruby", 30);
		oreMap1.put("sapphire", 30);
		oreMap1.put("quartz", 300);
		oreMap1.put("lead", 1000);
		oreMap1.put("zinc", 500);
		oreMap1.put("redstone", 20);
		
		oreMap2.put("copper", 200);
		oreMap2.put("tin", 200);
		oreMap2.put("thorium", 120);
		oreMap2.put("uranium", 40);
		oreMap2.put("tungsten", 40);
		oreMap2.put("iron", 500);
		oreMap2.put("chromium", 200);
		oreMap2.put("osmium", 10);
		oreMap2.put("iridium", 10);
		oreMap2.put("silver", 20);
		oreMap2.put("aluminium", 1000);
		oreMap2.put("platinum", 20);
		oreMap2.put("diamond", 10);
		oreMap2.put("emerald", 30);
		oreMap2.put("ruby", 30);
		oreMap2.put("sapphire", 30);
		oreMap2.put("quartz", 1000);
		oreMap2.put("lead", 100);
		oreMap2.put("zinc", 500);
		oreMap2.put("redstone", 20);
		
	}

	public void initHardens()
	{
		String name;
		for(Block b:ThutBlocks.getAllBlocks())
		{
			for(int meta = 0; meta<16; meta++)
			{
				if(b!=null&&oreDictName(b,meta)!="Unknown")
				{
					name = oreDictName(b,meta);
					for(String s:oreMap0.keySet())
					{
//						if(!s.contains("nether")&&!b.getUnlocalizedName().toLowerCase().contains("nether")&&(s.equals("quartz")||!ores.contains(s)))
//						if(!BlockSolidLava.getInstance(0).turnto.contains(b.blockID + 4096*WorldCore.oreMap0.get(s) + 4096*1024*meta))TODO solid lava harden to
//						if(name.toLowerCase().contains(s)&&name.toLowerCase().contains("ore"))
//						{
//							BlockSolidLava.getInstance(0).turnto.add(b.blockID + 4096*WorldCore.oreMap0.get(s) + 4096*1024*meta);
//							
//							BlockSolidLava.getInstance(1).turnto.add(b.blockID + 4096*WorldCore.oreMap1.get(s) + 4096*1024*meta);
//							
//							BlockSolidLava.getInstance(2).turnto.add(b.blockID + 4096*WorldCore.oreMap2.get(s) + 4096*1024*meta);
//							
//							BlockSolidLava.getInstance(3).turnto.add(b.blockID + 4096*WorldCore.oreMap2.get(s) + 4096*1024*meta);
//							
//							ores.add(s);
//						}
					}
				}
			}
		}
	}

	public static String oreDictName(Block id, int meta)
	{
		return OreDictionary.getOreName(OreDictionary.getOreID(new ItemStack(id,1,meta)));
	}
	
}
