package thut.core.common.handlers;

import java.io.File;
import java.util.Locale;
import java.util.function.Predicate;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thut.api.boom.ExplosionCustom;
import thut.api.entity.ai.AIThreadManager;
import thut.api.entity.blockentity.BlockEntityUpdater;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.terrain.TerrainSegment;
import thut.api.terrain.TerrainSegment.ISubBiomeChecker;
import thut.core.common.config.ConfigBase;
import thut.core.common.config.Configure;
import thut.core.common.terrain.ConfigTerrainBuilder;
import thut.core.common.terrain.ConfigTerrainChecker;
import thut.reference.Reference;

public class ConfigHandler extends ConfigBase
{

    private static final String BOOMS                   = "explosions";
    private static final String BIOMES                  = "biomes";
    private static final String BLOCKENTITY             = "blockentity";
    private static final String AI                      = "ai";
    private static final String MISC                    = "misc";

    @Configure(category = BOOMS)
    private int                 explosionRadius         = 127;
    @Configure(category = BOOMS)
    private int[]               explosionRate           = { 2000, 10000 };
    @Configure(category = BOOMS)
    private boolean             affectAir               = true;
    @Configure(category = BOOMS)
    private double              minBlastEffect          = 0.25;
    @Configure(category = BIOMES)
    public boolean              resetAllTerrain         = false;
    @Configure(category = BIOMES)
    public String[]             customBiomeMappings     = {};
    @Configure(category = AI, needsMcRestart = true)
    public int                  threadCount             = 1;
    @Configure(category = AI, needsMcRestart = true)
    public boolean              multithreadedAI         = false;
    @Configure(category = AI)
    public int                  aiTickRate              = 1;

    @Configure(category = BLOCKENTITY)
    public String[]             teblacklist             = {};
    @Configure(category = BLOCKENTITY)
    public String[]             blockblacklist          = { "minecraft:bedrock" };
    @Configure(category = BLOCKENTITY)
    public boolean              autoBlacklistErroredTEs = true;
    @Configure(category = MISC)
    public boolean              debug                   = true;

    public ConfigHandler()
    {
        super(null);
    }

    public ConfigHandler(File configFile)
    {
        super(configFile, new ConfigHandler());
        MinecraftForge.EVENT_BUS.register(this);
        populateSettings();
        applySettings();
        save();
    }

    @Override
    protected void applySettings()
    {
        ExplosionCustom.MAX_RADIUS = explosionRadius;
        ExplosionCustom.AFFECTINAIR = affectAir;
        if (explosionRate.length == 2) ExplosionCustom.MAXPERTICK = explosionRate;
        ExplosionCustom.MINBLASTDAMAGE = (float) minBlastEffect;
        TerrainSegment.noLoad = resetAllTerrain;
        IBlockEntity.TEBLACKLIST.clear();
        BlockEntityUpdater.autoBlacklist = autoBlacklistErroredTEs;
        AIThreadManager.AIStuff.tickRate = aiTickRate;
        for (String s : teblacklist)
        {
            if (!s.contains(":")) s = "minecraft:" + s;
            IBlockEntity.TEBLACKLIST.add(s);
            IBlockEntity.TEBLACKLIST.add(s.toLowerCase(Locale.ENGLISH));
        }
        for (String s : blockblacklist)
        {
            IBlockEntity.BLOCKBLACKLIST.add(new ResourceLocation(s));
        }
        TerrainSegment.biomeCheckers.removeIf(new Predicate<ISubBiomeChecker>()
        {
            @Override
            public boolean test(ISubBiomeChecker t)
            {
                return t instanceof ConfigTerrainChecker;
            }
        });
        ConfigTerrainBuilder.process(customBiomeMappings);

        // TODO figure out what is wrong with this and fix it.
        // multithreadedAI = false;
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs)
    {
        if (eventArgs.getModID().equals(Reference.MOD_ID))
        {
            populateSettings();
            applySettings();
            save();
        }
    }
}
