package thut.mobcapper;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Config
{
    public static boolean debug = false;
    public static boolean logspawns = false;
    public static boolean logCulls = false;
    public static boolean client = false;
    public static int     number;
    public static int     number2;

    public Config(FMLPreInitializationEvent e)
    {
        loadConfig(e);
    }

    void loadConfig(FMLPreInitializationEvent e)
    {
        Configuration config = new Configuration(e.getSuggestedConfigurationFile());
        config.load();

        number2 = config.getInt("range", Configuration.CATEGORY_GENERAL, 64, 1, 128, "range to stop spawning");
        number = config.getInt("maxnumber", Configuration.CATEGORY_GENERAL, 8, 1, 128,
                "the maximum number of entities allowed in a cube of radius range");
        debug = config.getBoolean("debug", Configuration.CATEGORY_GENERAL, false, "debug prints");
        logspawns = config.getBoolean("logspawns", Configuration.CATEGORY_GENERAL, false, "log spawns (WARNING LOG SPAM CAUSES LAG)");
        logCulls = config.getBoolean("logculls", Configuration.CATEGORY_GENERAL, false, "log culls (WARNING LOG SPAM CAUSES LAG)");
        client = config.getBoolean("client", Configuration.CATEGORY_GENERAL, false, "works on clients (this should be false if in a server's modpack)");

        config.save();
    }

}
