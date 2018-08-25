package thut.tech.common.handlers;

import net.minecraftforge.common.config.Configuration;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.entity.LiftInteractHandler;

public class ConfigHandler
{
    public static double  LiftSpeedUp           = 0.3;
    public static double  LiftSpeedDown         = 0.35;
    public static double  LiftSpeedDownOccupied = 0;
    public static int     controllerProduction  = 16;
    public static int     maxHeight             = 5;
    public static int     maxRadius             = 2;
    public static boolean hackyRender           = false;
    public static boolean jitterfix             = true;
    public static int     maxLiftEnergy         = 5000000;

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
        EntityLift.ENERGYUSE = conf.getBoolean("energyUse", "Lift Settings", false, "Do Lifts use energy");
        EntityLift.ENERGYCOST = conf.getInt("energyCost", "Lift Settings", 100, 0, 1000, "Base Energy use for Lifts");
        maxLiftEnergy = conf.getInt("maxLiftEnergy", "Lift Settings", 500000, 0, Integer.MAX_VALUE,
                "Total amount of energy a lift can store.");
        controllerProduction = conf.getInt("controllerProduction", "Lift Settings", 16, 0, 5000,
                "T/t produced by the controller blocks");
        maxHeight = conf.get("Lift Settings", "maxHeight", 5, "Max allowed height of a lift.").getInt();
        maxRadius = conf.get("Lift Settings", "maxRadius", 2, "Max allowed radius of a lift (2 gives 5x5 as maximum).")
                .getInt();
        LiftInteractHandler.DROPSPARTS = conf.getBoolean("dropsParts", "Lift Settings", true,
                "Does lift drop elevator blocks on disassemble");
        hackyRender = conf.getBoolean("hackyRender", "Lift Settings", hackyRender,
                "Does Lift use abnormal rendering, if true, this fixes a vanilla bug with it going invisible at certain locations, "
                        + "this is bad for FPS if there are lots of them though.");
        conf.save();
    }

}
