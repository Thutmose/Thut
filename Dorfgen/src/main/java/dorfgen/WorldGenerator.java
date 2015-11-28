package dorfgen;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import dorfgen.conversion.Config;
import dorfgen.conversion.DorfMap;
import dorfgen.conversion.DorfMap.Site;
import dorfgen.conversion.DorfMap.SiteType;
import dorfgen.conversion.FileLoader;
import dorfgen.conversion.SiteStructureGenerator;
import dorfgen.worldgen.MapGenSites.Start;
import dorfgen.worldgen.WorldChunkManagerFinite;
import dorfgen.worldgen.WorldTypeFinite;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = WorldGenerator.MODID, name = WorldGenerator.NAME, version = "1.8", acceptableRemoteVersions = "*")
public class WorldGenerator {

	public static final String MODID = "dorfgen";
	public static final String NAME = "DF World Generator";

	@Mod.Instance(MODID)
	public static WorldGenerator instance;

	public BufferedImage elevationMap;
	public BufferedImage elevationWaterMap;
	public BufferedImage biomeMap;
	public BufferedImage evilMap;
	public BufferedImage temperatureMap;
	public BufferedImage rainMap;
	public BufferedImage drainageMap;
	public BufferedImage vegitationMap;
	public BufferedImage structuresMap;

	public final DorfMap dorfs = new DorfMap();
	public final SiteStructureGenerator structureGen = new SiteStructureGenerator(dorfs);

	public static int scale;
	public static boolean finite;
	public static BlockPos spawn;
	public static BlockPos shift;
	public static String spawnSite = "";
	public static boolean randomSpawn;

	public WorldType finiteWorldType;

	public static String configLocation;
	public static String biomes;
	
	private final boolean[] done = {false};

	public WorldGenerator() {
		instance = this;
	}
	Block roadgravel;
	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.TERRAIN_GEN_BUS.register(this);
		new Config(e);
		File file = e.getSuggestedConfigurationFile();
		String seperator = System.getProperty("file.separator");
		
		GameRegistry.registerItem(new ItemDebug(), "debugItem");//TODO texture
		GameRegistry.registerBlock(roadgravel = new BlockRoadSurface(), "roadgravel");
		

		String folder = file.getAbsolutePath();
		String name = file.getName();
		FileLoader.biomes = folder.replace(name, MODID + seperator + "biomes.csv");
		//
		MapGenStructureIO.registerStructure(Start.class, "dorfsitestart");
		
		Thread dorfProcess = new Thread(new Runnable() {
			
			@Override
			public void run() {
				new FileLoader();
				dorfs.init();
				structureGen.init();
				done[0] = true;
			}
		});
		dorfProcess.setName("dorfgen image processor");
		dorfProcess.start();
		//
	}

	@EventHandler
	public void load(FMLInitializationEvent evt) {
		finiteWorldType = new WorldTypeFinite("finite");
		try {
			//chunkClass = Class.forName("bigworld.storage.BigChunk");
		} catch (Exception e) {
		}
		if(evt.getSide() == Side.CLIENT)
		{
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
		    .register(Item.getItemFromBlock(roadgravel), 0, new ModelResourceLocation("dorfgen:roadgravel", "inventory"));
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {

//		for (BiomeGenBase b : BiomeGenBase.getBiomeGenArray()) {//TODO see if this is still needed
//			if (b != null && !MapGenVillage.villageSpawnBiomes.contains(b)) {
//				BiomeManager.addVillageBiome(b, true);
//			}
//		}
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register
			(Item.getItemFromBlock(BlockRoadSurface.uggrass), 0, new ModelResourceLocation("dorfgen:roadgravel", "inventory"));
			
		}
	}
	
    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
    	event.registerServerCommand(new Commands());
    }

	@SubscribeEvent
	public void genEvent(Load evt) {
		if (evt.world.provider.getWorldChunkManager() instanceof WorldChunkManagerFinite) {

			if(!spawnSite.isEmpty())
			{
				ArrayList<Site> sites = new ArrayList<Site>(DorfMap.sitesById.values());
				for(Site s: sites)
				{
					if(s.name.equalsIgnoreCase(spawnSite))
					{
						int x = s.x * scale;
						int y = 0;
						int z = s.z * scale;
						try
						{
							y = dorfs.elevationMap[(x - shift.getX()) / scale][(z - shift.getZ()) / scale];
						}
						catch (Exception e)
						{
							System.out.println(s+" "+dorfs.elevationMap.length);
							e.printStackTrace();
						}
						evt.world.setSpawnPoint(new BlockPos(x + scale / 2, y, z + scale / 2));
						return;
					}
				}
			}
			if (randomSpawn) {
				ArrayList<Site> sites = new ArrayList<Site>(DorfMap.sitesById.values());
				
				Collections.shuffle(sites, evt.world.rand);
				
				for (Site s : sites) {
					if (s.type.isVillage() && s.type != SiteType.HIPPYHUTS) {
						int x = s.x * scale;
						int y = 0;
						int z = s.z * scale;
						try
						{
							y = dorfs.elevationMap[(x - shift.getX()) / scale][(z - shift.getZ()) / scale];
						}
						catch (Exception e)
						{
							System.out.println(s+" "+dorfs.elevationMap.length);
							e.printStackTrace();
						}
						evt.world.setSpawnPoint(new BlockPos(x + scale / 2, y, z + scale / 2));
						return;
					}
				}
			} else {
				evt.world.setSpawnPoint(spawn);
			}
		}
	}
	
	@EventHandler
	public void LoadComplete(FMLLoadCompleteEvent event)
	{
		while(!done[0])
		{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Done Loading");
	}

}
