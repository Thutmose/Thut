package thut.core.common.handlers;

import java.io.File;

import thut.api.explosion.ExplosionCustom;
import thut.api.maths.Vector3;
import net.minecraftforge.common.config.Configuration;

public class ConfigHandler {

	public static boolean collisionDamage;
	public static boolean paneFix;
	
	public ConfigHandler(File configFile){
		// Loads The Configuration File into Forges Configuration
		Configuration conf = new Configuration(configFile);
		try{
			conf.load();
			
			paneFix = conf.get("Misc", "fix panes", true, "Do glass panes of mods using outdated methods connect to concrete?").getBoolean(true);

			byte rad = (byte) conf.get("Misc", "explosionRadius", 63, "The radius of the volume affected by explosions, max value of 127, higher settings make the game take longer to load at startup").getInt();
			ExplosionCustom.MAX_RADIUS = (byte) Math.min(rad,127);
		}catch(RuntimeException e){
			
		}finally{
			conf.save();
		}
	}
}
