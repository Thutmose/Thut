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
import dorfgen.worldgen.MapGenSites.Start;
import dorfgen.worldgen.WorldChunkManagerFinite;
import dorfgen.worldgen.WorldTypeFinite;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = WorldGenerator.MODID, name = WorldGenerator.NAME, version = "1.8", acceptableRemoteVersions = "*")
public class WorldGenerator
{

    public static final String MODID = "dorfgen";
    public static final String NAME  = "DF World Generator";

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

    public final DorfMap                dorfs        = new DorfMap();
    public final SiteStructureGenerator structureGen = new SiteStructureGenerator(this.dorfs);

    public static int      scale;
    public static boolean  finite;
    public static BlockPos spawn;
    public static BlockPos shift;
    public static String   spawnSite = "";
    public static boolean  randomSpawn;

    public WorldType finiteWorldType;

    public static String configLocation;
    public static String biomes;

    private final boolean[] done = { false };

    public WorldGenerator()
    {
        WorldGenerator.instance = this;
    }

    Block roadgravel;

    @EventHandler
    public void preInit(final FMLPreInitializationEvent e)
    {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.TERRAIN_GEN_BUS.register(this);
        new Config(e);
        final File file = e.getSuggestedConfigurationFile();
        final String seperator = System.getProperty("file.separator");

        GameRegistry.registerItem(new ItemDebug(), "debugItem");// TODO texture
        GameRegistry.registerBlock(this.roadgravel = new BlockRoadSurface(), "roadgravel");

        final String folder = file.getAbsolutePath();
        final String name = file.getName();
        FileLoader.biomes = folder.replace(name, WorldGenerator.MODID + seperator + "biomes.csv");
        //
        MapGenStructureIO.registerStructure(Start.class, "dorfsitestart");

        final Thread dorfProcess = new Thread(() ->
        {
            new FileLoader();
            WorldGenerator.this.dorfs.init();
            WorldGenerator.this.structureGen.init();
            WorldGenerator.this.done[0] = true;
        });
        dorfProcess.setName("dorfgen image processor");
        dorfProcess.start();
        //
    }

    @EventHandler
    public void load(final FMLInitializationEvent evt)
    {
        this.finiteWorldType = new WorldTypeFinite("finite");
        try
        {
            // chunkClass = Class.forName("bigworld.storage.BigChunk");
        }
        catch (final Exception e)
        {
        }
        if (evt.getSide() == Side.CLIENT) Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(Item
                .getItemFromBlock(this.roadgravel), 0, new ModelResourceLocation("dorfgen:roadgravel", "inventory"));
    }

    @EventHandler
    public void serverLoad(final FMLServerStartingEvent event)
    {
        event.registerServerCommand(new Commands());
    }

    @SubscribeEvent
    public void genEvent(final Load evt)
    {
        if (evt.getWorld().provider.getWorldChunkManager() instanceof WorldChunkManagerFinite)
        {

            if (!WorldGenerator.spawnSite.isEmpty())
            {
                final ArrayList<Site> sites = new ArrayList<>(DorfMap.sitesById.values());
                for (final Site s : sites)
                    if (s.name.equalsIgnoreCase(WorldGenerator.spawnSite))
                    {
                        final int x = s.x * WorldGenerator.scale;
                        int y = 0;
                        final int z = s.z * WorldGenerator.scale;
                        try
                        {
                            y = this.dorfs.elevationMap[(x - WorldGenerator.shift.getX()) / WorldGenerator.scale][(z
                                    - WorldGenerator.shift.getZ()) / WorldGenerator.scale];
                        }
                        catch (final Exception e)
                        {
                            System.out.println(s + " " + this.dorfs.elevationMap.length);
                            e.printStackTrace();
                        }
                        evt.world.setSpawnPoint(new BlockPos(x + WorldGenerator.scale / 2, y, z + WorldGenerator.scale
                                / 2));
                        return;
                    }
            }
            if (WorldGenerator.randomSpawn)
            {
                final ArrayList<Site> sites = new ArrayList<>(DorfMap.sitesById.values());

                Collections.shuffle(sites, evt.getWorld().rand);

                for (final Site s : sites)
                    if (s.type.isVillage() && s.type != SiteType.HIPPYHUTS)
                    {
                        final int x = s.x * WorldGenerator.scale;
                        int y = 0;
                        final int z = s.z * WorldGenerator.scale;
                        try
                        {
                            y = this.dorfs.elevationMap[(x - WorldGenerator.shift.getX()) / WorldGenerator.scale][(z
                                    - WorldGenerator.shift.getZ()) / WorldGenerator.scale];
                        }
                        catch (final Exception e)
                        {
                            System.out.println(s + " " + this.dorfs.elevationMap.length);
                            e.printStackTrace();
                        }
                        evt.world.setSpawnPoint(new BlockPos(x + WorldGenerator.scale / 2, y, z + WorldGenerator.scale
                                / 2));
                        return;
                    }
            }
            else evt.world.setSpawnPoint(WorldGenerator.spawn);
        }
    }

    @SubscribeEvent
    public void decorate(final Decorate event)
    {
        final Collection<Site> sites = this.dorfs.getSiteForCoords(event.pos.getX(), event.pos.getZ());
        if (sites != null && event.type == EventType.TREE) for (final Site site : sites)
            if (site != null && site.rgbmap != null) for (int x = event.pos.getX(); x < event.pos.getX() + 16; x++)
                for (int z = event.pos.getZ(); z < event.pos.getZ() + 16; z++)
                {
                    final int width = WorldGenerator.scale / SiteStructureGenerator.SITETOBLOCK;
                    final int pixelX = (x - site.corners[0][0] * WorldGenerator.scale - WorldGenerator.scale / 2 - width
                            / 2) / width;
                    final int pixelY = (z - site.corners[0][1] * WorldGenerator.scale - WorldGenerator.scale / 2 - width
                            / 2) / width;
                    if (pixelX >= site.rgbmap.length || pixelY >= site.rgbmap[0].length) continue;
                    if (SiteMapColours.getMatch(site.rgbmap[pixelX][pixelY]) != SiteMapColours.GENERIC)
                    {
                        event.setResult(Result.DENY);
                        return;
                    }
                }
    }

}
