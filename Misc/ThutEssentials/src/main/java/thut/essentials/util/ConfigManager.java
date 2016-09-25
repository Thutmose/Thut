package thut.essentials.util;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import thut.essentials.itemcontrol.ItemControl;

public class ConfigManager extends ConfigBase
{
    private static final String SPAWN              = "spawn";
    private static final String RULES              = "rules";
    private static final String WARPS              = "warps";
    private static final String STAFF              = "staff";
    private static final String NAMES              = "names";
    private static final String LAND               = "land";
    private static final String ITEM               = "itemcontrol";
    private static final String MISC               = "misc";

    public static ConfigManager INSTANCE;

    @Configure(category = SPAWN)
    public int                  spawnDimension     = 0;

    @Configure(category = RULES)
    public String[]             rules              = {};
    @Configure(category = RULES)
    public String               ruleHeader         = "List of Rules:";

    @Configure(category = WARPS)
    public String[]             warps              = {};
    @Configure(category = WARPS)
    public int                  backDelay          = 10;
    @Configure(category = WARPS)
    public int                  warpDelay          = 10;
    @Configure(category = WARPS)
    public int                  spawnDelay         = 10;
    @Configure(category = WARPS)
    public int                  homeDelay          = 10;

    @Configure(category = STAFF)
    public String[]             staff              = {};

    @Configure(category = ITEM)
    public String[]             itemBlacklist      = {};
    @Configure(category = ITEM)
    public boolean              itemControlEnabled = false;
    @Configure(category = ITEM)
    public double               blacklistDamage    = 5;

    @Configure(category = MISC)
    public double               speedCap           = 10;

    @Configure(category = MISC)
    public int                  maxHomes           = 2;

    @Configure(category = NAMES)
    public boolean              name               = true;
    @Configure(category = NAMES)
    public boolean              suffix             = true;
    @Configure(category = NAMES)
    public boolean              prefix             = true;

    @Configure(category = LAND)
    public boolean              landEnabled        = false;
    @Configure(category = LAND)
    public boolean              denyExplosions     = false;
    @Configure(category = LAND)
    public int                  teamLandPerPlayer  = 125;
    @Configure(category = LAND)
    public String               defaultTeamName    = "Plebs";

    public ConfigManager()
    {
        super(null);
    }

    public ConfigManager(File configFile)
    {
        super(configFile, new ConfigManager());
        MinecraftForge.EVENT_BUS.register(this);
        INSTANCE = this;
        populateSettings();
        applySettings();
        save();
    }

    @Override
    protected void applySettings()
    {
        WarpManager.init();
        ItemControl.init();
    }

}
