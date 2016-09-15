package thut.core.common.handlers;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import thut.api.boom.ExplosionCustom;
import thut.core.common.config.ConfigBase;
import thut.core.common.config.Configure;

public class ConfigHandler extends ConfigBase
{
    private static final String BOOMS           = "explosions";

    // @Configure(category = "items")
    // private boolean spout = false;
    // @Configure(category = "items")
    // private boolean tank = false;
    @Configure(category = BOOMS)
    private int                 explosionRadius = 127;
    @Configure(category = BOOMS)
    private int[]               explosionRate   = { 2000, 10000 };
    @Configure(category = BOOMS)
    private boolean             affectAir       = true;
    @Configure(category = BOOMS)
    private double              minBlastEffect  = 0.1;

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
    }
}
