package dorfgen;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;

import dorfgen.conversion.DorfMap;
import dorfgen.conversion.FileLoader;
import dorfgen.world.gen.DorfWorldType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(value = Dorfgen.MODID)
public class Dorfgen
{
    // You can use EventBusSubscriber to automatically subscribe events on the
    // contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Dorfgen.MODID)
    public static class RegistryEvents
    {

    }

    public static final String MODID = "dorfgen";
    public static final String NAME  = "DF World Generator";

    public static boolean  finit       = false;
    public static BlockPos spawn       = new BlockPos(0, 64, 0);
    public static BlockPos shift       = BlockPos.ZERO;
    public static String   spawnSite   = "";
    public static boolean  randomSpawn = false;

    public static Dorfgen instance;

    public DorfWorldType finiteWorldType = new DorfWorldType("dorfgen");

    public static String configLocation;
    public static String biomes;

    public static final Logger LOGGER = LogManager.getLogger(Dorfgen.MODID);

    Map<String, DorfMap> maps    = new HashMap<>();
    List<DorfMap>        mapList = new ArrayList<>();

    public Dorfgen()
    {
        Dorfgen.instance = this;

        final File logfile = FMLPaths.GAMEDIR.get().resolve("logs").resolve(Dorfgen.MODID + ".log").toFile();
        if (logfile.exists()) logfile.delete();
        final org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) Dorfgen.LOGGER;
        final FileAppender appender = FileAppender.newBuilder().withFileName(logfile.getAbsolutePath()).setName(
                Dorfgen.MODID).build();
        logger.addAppender(appender);
        appender.start();

        final File configDir = FMLPaths.CONFIGDIR.get().resolve(Dorfgen.MODID).toFile();
        configDir.mkdirs();
        new FileLoader(configDir, new File(configDir, "biomes.csv"));
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loaded);
    }

    public void addDorfMap(final DorfMap map)
    {
        synchronized (this.maps)
        {
            Dorfgen.LOGGER.info("Addng DorfMap {} ({})", map.name, map.altName);
            this.maps.put(map.name, map);
            this.mapList.add(map);
        }
    }

    public DorfMap getDorfs(final IWorld world)
    {
        return this.mapList.get(0);
    }

    public void setup(final FMLCommonSetupEvent event)
    {
    }

    public void loaded(final FMLLoadCompleteEvent event)
    {

    }

    @SubscribeEvent
    public void serverStarting(final FMLServerStartingEvent event)
    {
        DorfCommand.register(event.getCommandDispatcher());
    }
}
