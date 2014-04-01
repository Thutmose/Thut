package thut.core.common.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;
import thut.api.explosion.ExplosionCustom;
import thut.api.maths.Vector3;
import thut.core.common.items.ItemSpout;
import thut.core.common.items.ItemTank;
import net.minecraft.item.Item;
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
		

		items.add(new ItemSpout());
		items.add(new ItemTank());
		
		for(Item item: items){
			GameRegistry.registerItem(item, item.getUnlocalizedName().substring(5));
		}
	}
	
	private static List<Item> items = new ArrayList<Item>();
}
