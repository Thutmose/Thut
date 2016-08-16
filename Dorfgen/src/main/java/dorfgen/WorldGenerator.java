package dorfgen;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import dorfgen.conversion.Config;
import dorfgen.conversion.DorfMap;
import dorfgen.conversion.DorfMap.Site;
import dorfgen.conversion.DorfMap.SiteType;
import dorfgen.conversion.FileLoader;
import dorfgen.conversion.SiteMapColours;
import dorfgen.conversion.SiteStructureGenerator;
import dorfgen.worldgen.BiomeProviderFinite;
import dorfgen.worldgen.MapGenSites.Start;
import dorfgen.worldgen.WorldTypeFinite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, dependencies = Reference.DEPSTRING, acceptableRemoteVersions = "*")
public class WorldGenerator
{
    @Mod.Instance(Reference.MOD_ID)
    public static WorldGenerator        instance;

    public BufferedImage                elevationMap;
    public BufferedImage                elevationWaterMap;
    public BufferedImage                biomeMap;
    public BufferedImage                evilMap;
    public BufferedImage                temperatureMap;
    public BufferedImage                rainMap;
    public BufferedImage                drainageMap;
    public BufferedImage                vegitationMap;
    public BufferedImage                structuresMap;

    public final DorfMap                dorfs        = new DorfMap();
    public final SiteStructureGenerator structureGen = new SiteStructureGenerator(dorfs);

    public static int                   scale;
    public static boolean               finite;
    public static BlockPos              spawn;
    public static BlockPos              shift;
    public static String                spawnSite    = "";
    public static boolean               randomSpawn;

    public WorldType                    finiteWorldType;

    public static String                configLocation;
    public static String                biomes;
    public static Biome                 roadBiome;

    private final boolean[]             done         = { false };

    public WorldGenerator()
    {
        instance = this;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.TERRAIN_GEN_BUS.register(this);
        new Config(e);
        File file = e.getSuggestedConfigurationFile();
        String seperator = System.getProperty("file.separator");

        String folder = file.getAbsolutePath();
        String name = file.getName();
        FileLoader.biomes = folder.replace(name, Reference.MOD_ID + seperator + "biomes.csv");
        //
        MapGenStructureIO.registerStructure(Start.class, "dorfsitestart");
        //
        roadBiome = Biome.REGISTRY.getObjectById(1);
    }

    @EventHandler
    public void load(FMLInitializationEvent evt)
    {
        finiteWorldType = new WorldTypeFinite("finite");

        Thread dorfProcess = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                new FileLoader();
                dorfs.init();
                structureGen.init();
                System.out.println("DF MAP LOADING COMPLETE");
                done[0] = true;
            }
        });
        dorfProcess.setName("dorfgen image processor");
        dorfProcess.start();
        try
        {
            // chunkClass = Class.forName("bigworld.storage.BigChunk");
        }
        catch (Exception e)
        {
        }
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new Commands());
    }

    @SubscribeEvent
    public void genEvent(Load evt)
    {
        if (evt.getWorld().provider.getBiomeProvider() instanceof BiomeProviderFinite)
        {
            if (!spawnSite.isEmpty())
            {
                ArrayList<Site> sites = new ArrayList<Site>(DorfMap.sitesById.values());
                for (Site s : sites)
                {
                    if (s.name.equalsIgnoreCase(spawnSite))
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
                            System.out.println(s + " " + dorfs.elevationMap.length);
                            e.printStackTrace();
                        }
                        evt.getWorld().setSpawnPoint(new BlockPos(x + scale / 2, y, z + scale / 2));
                        return;
                    }
                }
            }
            if (randomSpawn)
            {
                ArrayList<Site> sites = new ArrayList<Site>(DorfMap.sitesById.values());

                Collections.shuffle(sites, evt.getWorld().rand);

                for (Site s : sites)
                {
                    if (s.type.isVillage() && s.type != SiteType.HIPPYHUTS)
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
                            System.out.println(s + " " + dorfs.elevationMap.length);
                            e.printStackTrace();
                        }
                        evt.getWorld().setSpawnPoint(new BlockPos(x + scale / 2, y, z + scale / 2));
                        return;
                    }
                }
            }
            else
            {
                evt.getWorld().setSpawnPoint(spawn);
            }
        }
    }

    @EventHandler
    public void LoadComplete(FMLLoadCompleteEvent event)
    {
        while (!done[0])
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public void decorate(Decorate event)
    {
        Collection<Site> sites = dorfs.getSiteForCoords(event.getPos().getX(), event.getPos().getZ());
        if (sites != null && event.getType() == EventType.TREE)
        {
            for (Site site : sites)
            {
                if (site != null && site.rgbmap != null)
                {
                    for (int x = event.getPos().getX(); x < event.getPos().getX() + 16; x++)
                        for (int z = event.getPos().getZ(); z < event.getPos().getZ() + 16; z++)
                        {
                            int width = (scale / SiteStructureGenerator.SITETOBLOCK);
                            int pixelX = (x - site.corners[0][0] * scale - scale / 2 - width / 2) / width;
                            int pixelY = (z - site.corners[0][1] * scale - scale / 2 - width / 2) / width;
                            if (pixelX >= site.rgbmap.length || pixelY >= site.rgbmap[0].length)
                            {
                                continue;
                            }
                            if (SiteMapColours.getMatch(site.rgbmap[pixelX][pixelY]) != SiteMapColours.GENERIC)
                            {
                                event.setResult(Result.DENY);
                                return;
                            }
                        }
                }
            }
        }
    }

}
