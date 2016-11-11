package thut.core.common.handlers;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import thut.api.boom.ExplosionCustom;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.terrain.TerrainSegment;
import thut.core.common.config.ConfigBase;
import thut.core.common.config.Configure;

public class ConfigHandler extends ConfigBase
{

    private static final String BOOMS           = "explosions";
    private static final String BIOMES          = "biomes";
    private static final String BLOCKENTITY     = "blockentity";
    private static final String AI              = "ai";

    @Configure(category = BOOMS)
    private int                 explosionRadius = 127;
    @Configure(category = BOOMS)
    private int[]               explosionRate   = { 2000, 10000 };
    @Configure(category = BOOMS)
    private boolean             affectAir       = true;
    @Configure(category = BOOMS)
    private double              minBlastEffect  = 0.25;
    @Configure(category = BIOMES)
    public boolean              resetAllTerrain = false;
    @Configure(category = AI, needsMcRestart = true)
    public int                  threadCount     = 1;

    @Configure(category = BLOCKENTITY)
    private String[]            whitelist       = { "Chest", "DLDetector", "FlowerPot", "EnchantTable", "warppad",
            "Comparator", "pokecube:pokecube_table", "tradingtable", "EndGateway", "Control", "Piston", "pokecenter",
            "EnderChest", "MobSpawner", "cloner", "pokecube:berries", "Airportal", "Banner", "Trap", "Furnace",
            "Dropper", "Cauldron", "repel", "pc", "Music", "multiblockpart", "multiblockpartfluids", "Beacon", "Skull",
            "Hopper", "Sign", "pokesiphon", "pokemobnest", "RecordPlayer" };

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
        IBlockEntity.TEWHITELIST.clear();
        for (String s : whitelist)
            IBlockEntity.TEWHITELIST.add(s);
    }
}
