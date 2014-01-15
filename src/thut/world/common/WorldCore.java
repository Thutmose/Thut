package thut.world.common;

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
import thut.api.explosion.ExplosionCustom.Cruncher;
import thut.api.utils.Vector3;
import thut.world.common.blocks.fluids.solids.BlockSolidLava;
import thut.world.common.corehandlers.BlockHandler;
import thut.world.common.corehandlers.ConfigHandler;
import thut.world.common.corehandlers.ItemHandler;
import thut.world.common.corehandlers.LiquidHandler;
import thut.world.common.corehandlers.RecipeHandler;
import thut.world.common.corehandlers.TSaveHandler;
import thut.world.common.corehandlers.WorldEventHandler;
import thut.world.common.finiteWorld.WorldTypeCustom;
import thut.world.common.multipart.Content;
import thut.world.common.multipart.PlayerInteractHandler;
import thut.world.common.network.TCPacket;
import thut.world.common.ticks.ThreadSafeWorldOperations;
import thut.world.common.ticks.TickHandler;
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
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod( modid = "thutWorldgen", name="Thut's WorldGen", version="0.0.1")
@NetworkMod(clientSideRequired = true, serverSideRequired = true, 
channels={"Thut's WorldGen"
		,"multiPartPackets"},
packetHandler = TCPacket.class
)

public class WorldCore {

	@SidedProxy(clientSide = "thut.world.client.ClientProxy", serverSide = "thut.world.common.CommonProxy")
	public static CommonProxy proxy;
	public static TickHandler tickHandler = new TickHandler();
	public static final String TEXTURE_PATH = "worldgen:";
	
	public Logger log = FMLLog.getLogger();
		
	@Instance("thutWorldgen")
	public static WorldCore instance;
	
	public static String modid = "thutWorldgen";
	
	private static final String[]  LANGUAGES_SUPPORTED	= new String[] { "en_UK", "en_US" , "de_DE"};
    
    public static CreativeTabWorldgen tabThut = new CreativeTabWorldgen();
    
	public static Block[] blocks;
	public static Item[] items;
	
	public static WorldType customWorldType;
	
    private static final String[] colourNames = { "White",
        "Orange", "Magenta", "Light Blue",
        "Yellow", "Light Green", "Pink",
        "Dark Grey", "Light Grey", "Cyan",
        "Purple", "Blue", "Brown", "Green",
        "Red", "Black" };
	
	
	public TSaveHandler saveList;
	public TCPacket pkthandler;
	
	public WorldEventHandler loader;
	
	public LiquidHandler liquidHndlr;
	public BlockHandler blockList;
	public ItemHandler itemList;
	public RecipeHandler recipes;
	public static BiomeGenBase volcano;
	public static BiomeGenBase chalk;
	public ThreadSafeWorldOperations ops;

	// Configuration Handler that handles the config file
	public ConfigHandler config;

	@EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
		config = new ConfigHandler(e.getSuggestedConfigurationFile());
		proxy.loadSounds();
		customWorldType = new WorldTypeCustom(config.worldID, "FINITE");
		
		saveList = new TSaveHandler();
		MinecraftForge.EVENT_BUS.register(saveList);
		MinecraftForge.EVENT_BUS.register(this);
		
		loader = new WorldEventHandler(config.ChunkSize);
		MinecraftForge.EVENT_BUS.register(loader);
		
	    if(PlayerInteractHandler.handler==null)
		{
			PlayerInteractHandler.handler = new PlayerInteractHandler();
			MinecraftForge.EVENT_BUS.register(PlayerInteractHandler.handler);
		}

		Cruncher sort = new Cruncher();

//		VillagerRegistry.instance().registerVillageCreationHandler(new PokeCentreCreationHandler());
//		VillagerRegistry.instance().registerVillageCreationHandler(new PokeMartCreationHandler());
//		VillagerRegistry.instance().registerVillageCreationHandler(new ArenaCreationHandler());
//		
//		try
//        {
//            // if (new CallableMinecraftVersion(null).minecraftVersion().equals("1.6.4"))
//            // {
//            MapGenStructureIO.func_143031_a(ComponentPokeCentre.class, "thutWorldgen:PokeCentreStructure");
//            MapGenStructureIO.func_143031_a(ComponentPokeMart.class, "thutWorldgen:PokeMartStructure");
//            MapGenStructureIO.func_143031_a(ComponentArena.class, "thutWorldgen:ArenaStructure");
//            // }
//        }
//        catch (Throwable e1)
//        {
//        	System.out.println("Error registering Structures with Vanilla Minecraft: this is expected in versions earlier than 1.6.4");
//            //logger.severe("Error registering TConstruct Structures with Vanilla Minecraft: this is expected in versions earlier than 1.6.4");
//        }
	}
	
	  @EventHandler
	    public void load(FMLInitializationEvent evt)
	    {
		pkthandler = new TCPacket();
		proxy.initClient();

		blockList = new BlockHandler(config);
		itemList = new ItemHandler(config);
		
		(new Content()).init();

		liquidHndlr = new LiquidHandler();
		MinecraftForge.EVENT_BUS.register(liquidHndlr);
		
		TickRegistry.registerTickHandler(tickHandler, Side.SERVER);
		
		LanguageRegistry.instance().addStringLocalization("generator.FINITE", "en_US", "Finite World");

		
		GameRegistry.registerWorldGenerator(new TrassWorldGen());
		GameRegistry.registerWorldGenerator(new VolcanoWorldGen());
		
		proxy.registerEntities();
		proxy.registerTEs();
		
		populateMap();
		LanguageRegistry.instance().addStringLocalization("thutconcrete.container.limekiln", "Lime Kiln");
		NetworkRegistry.instance().registerGuiHandler(this, proxy);
		
		recipes = new RecipeHandler(config);
		items = itemList.items;
		blocks = blockList.blocks;
	//*	
		chalk = new BiomeGenChalk(config.IDBiome);
		volcano = new BiomeVolcano(config.IDBiome+1);
		GameRegistry.addBiome(chalk);
		BiomeDictionary.registerBiomeType(volcano, Type.NETHER, Type.MOUNTAIN);
		BiomeDictionary.registerBiomeType(chalk, Type.PLAINS);
		BiomeManager.addVillageBiome(chalk, true);
		initOreMap();
		registerLanguages();
	//*/	
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent e)
	{
		ops = new ThreadSafeWorldOperations();
		if(PlayerInteractHandler.handler==null)
		{
			PlayerInteractHandler.handler = new PlayerInteractHandler();
			MinecraftForge.EVENT_BUS.register(PlayerInteractHandler.handler);
		}
		initHardens();
		recipes.registerRecipes();
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
	
	public void registerLanguages()
	{
		/**
		 * Handle language support
		 */
		int languages = 0;

		for (String language : LANGUAGES_SUPPORTED)
		{
			LanguageRegistry.instance().loadLocalization("/assets/worldgen/language/" + language + ".properties", language, false);

			if (LanguageRegistry.instance().getStringLocalization("children", language) != "")
			{
				try
				{
					String[] children = LanguageRegistry.instance().getStringLocalization("children", language).split(",");

					for (String child : children)
					{
						if (child != "" && child != null)
						{
							LanguageRegistry.instance().loadLocalization("/assets/worldgen/language/"+ language + ".properties", child, false);
							languages++;
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

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
		for(Block b:Block.blocksList)
		{
			for(int meta = 0; meta<16; meta++)
			{
				if(b!=null&&oreDictName(b.blockID,meta)!="Unknown")
				{
					name = oreDictName(b.blockID,meta);
					for(String s:oreMap0.keySet())
					{
						if(!s.contains("nether")&&!b.getUnlocalizedName().toLowerCase().contains("nether")&&(s.equals("quartz")||!ores.contains(s)))
						if(!BlockSolidLava.getInstance(0).turnto.contains(b.blockID + 4096*WorldCore.oreMap0.get(s) + 4096*1024*meta))
						if(name.toLowerCase().contains(s)&&name.toLowerCase().contains("ore"))
						{
							BlockSolidLava.getInstance(0).turnto.add(b.blockID + 4096*WorldCore.oreMap0.get(s) + 4096*1024*meta);
							
							BlockSolidLava.getInstance(1).turnto.add(b.blockID + 4096*WorldCore.oreMap1.get(s) + 4096*1024*meta);
							
							BlockSolidLava.getInstance(2).turnto.add(b.blockID + 4096*WorldCore.oreMap2.get(s) + 4096*1024*meta);
							
							BlockSolidLava.getInstance(3).turnto.add(b.blockID + 4096*WorldCore.oreMap2.get(s) + 4096*1024*meta);
							
							ores.add(s);
						}
					}
				}
			}
		}
	}

	public static String oreDictName(int id, int meta)
	{
		return OreDictionary.getOreName(OreDictionary.getOreID(new ItemStack(id,1,meta)));
	}
	
}
