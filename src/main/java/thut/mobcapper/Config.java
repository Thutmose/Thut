package thut.mobcapper;

import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class Config {

	public static boolean debug = false;
	public static int number;
	public static int number2;
	
	public Config(FMLPreInitializationEvent e) {
		loadConfig(e);
	}

	void loadConfig(FMLPreInitializationEvent e)
	{
		Configuration config = new Configuration(e.getSuggestedConfigurationFile());
		config.load();

		number2 = config.getInt("range", config.CATEGORY_GENERAL, 64, 1, 128, "range to stop spawning");
		number = config.getInt("maxnumber", config.CATEGORY_GENERAL, 8, 1, 128, "the maximum number of entities allowed in a cube of radius range");
		debug = config.getBoolean("debug", config.CATEGORY_GENERAL, false, "debug prints");
		
		config.save();
	}
	
}
