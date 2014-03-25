package thut.tech.common.handlers;

import thut.api.maths.Vector3;
import thut.tech.common.entity.EntityLift;
import net.minecraftforge.common.config.Configuration;

public class ConfigHandler 
{

	public static double LiftSpeedUp = 0.3;
	public static double LiftSpeedDown = 0.35;
	public static double LiftSpeedDownOccupied = 0;

	public static void load(Configuration conf)
	{
		conf.load();
		
		LiftSpeedUp = conf.get("Lift Settings", "Upward speed", 0.3,"The speed in blocks/tick for the lift going upwards").getDouble(0.3);
		LiftSpeedDown = conf.get("Lift Settings", "Downward speed", 0.35,"The speed in blocks/tick for the lift going downwards").getDouble(0.35);
		EntityLift.ENERGYCOST = 0;//conf.get("Lift Settings", "energyCost", 0,"the amount of energy (Joules) per IronBlock-distance traveled.").getDouble(5);
		EntityLift.ACCELERATIONTICKS = conf.get("Lift Settings", "stopping ticks", 20,"This corresponds to how slowly the lift stops, setting this to 0 will result in very jerky lift.").getInt();
		EntityLift.AUGMENTG = conf.get("Lift Settings", "smoothdown", true,"Does the lift smooth your downward motion? if set to true will inhibit jumping while lift is moving down.").getBoolean(true);
		
		Vector3.collisionDamage = conf.get("Misc", "collisionDamage", false).getBoolean(false);
	}
	
	

	
}
