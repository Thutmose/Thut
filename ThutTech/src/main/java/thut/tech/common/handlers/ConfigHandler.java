package thut.tech.common.handlers;

import net.minecraftforge.common.config.Configuration;
import thut.tech.common.entity.EntityLift;

public class ConfigHandler
{

    public static double  LiftSpeedUp           = 0.3;
    public static double  LiftSpeedDown         = 0.35;
    public static double  LiftSpeedDownOccupied = 0;
    public static boolean jitterfix             = true;

    public static void load(Configuration conf)
    {
        conf.load();
        LiftSpeedUp = conf
                .get("Lift Settings", "Upward speed", 0.3, "The speed in blocks/tick for the lift going upwards")
                .getDouble(0.3);
        LiftSpeedDown = conf
                .get("Lift Settings", "Downward speed", 0.35, "The speed in blocks/tick for the lift going downwards")
                .getDouble(0.35);
        jitterfix = conf.getBoolean("fixJitter", "Lift Settings", true,
                "Client only setting, if true, will disable view bobbing on lift to fix jitter.");
        EntityLift.ACCELERATIONTICKS = conf
                .get("Lift Settings", "stopping ticks", 20,
                        "This corresponds to how slowly the lift stops, setting this to 0 will result in very jerky lift.")
                .getInt();
        conf.save();
    }

}
