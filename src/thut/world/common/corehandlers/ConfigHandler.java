package thut.world.common.corehandlers;


import java.io.File;
import java.util.logging.Level;

import thut.api.explosion.ExplosionCustom;
import thut.api.utils.Vector3;
import thut.world.common.WorldCore;
import thut.world.common.blocks.fluids.liquids.BlockLava;
import thut.world.common.blocks.fluids.solids.BlockSolidLava;
import thut.world.common.blocks.tileentity.TileEntityVolcano;
import net.minecraft.nbt.NBTBase;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

public class ConfigHandler {

    private int blockRange = 800;
    private int blockWorldRange = 159;
    private int biomeID = 159;
    private int itemRange = 7000;

    private int chunkSize = 500;
    private double coolrate = 3.5;
    private int ashamount = 0000;

    private int volcanoRate = 10000;

	// Blocks
    public static boolean deleteAsh;
	public static int IDBlock;
	public static int IDWorldBlock;
	public static int IDBiome;
	public static int ChunkSize;
	public static double CoolRate;
	public static boolean volcanos;
	public static boolean trass;
	public static boolean limestone;
	public static int VolcRate;
	public static int worldID;
	public static int ashAmount;
	public static boolean debugPrints;
	public static boolean volcanosActive;
	public static boolean collisionDamage;
	public static boolean paneFix;
	
	public static boolean defaultTypeFinite;
	public static boolean vanillaDust;

	public static double LiftSpeedUp;
	public static double LiftSpeedDown;
	public static double LiftSpeedDownOccupied;
	
	// Items
	public static int IDItem;
	// Misc
	public static int renderId;

	public ConfigHandler(File configFile){
		// Loads The Configuration File into Forges Configuration
		Configuration conf = new Configuration(configFile);
		try{
			conf.load();
			
			// Load Block Ids

			Property blockOre = conf.getBlock("Block", blockRange);
			blockOre.comment = "the initial Block ID";
			IDBlock = blockOre.getInt();
			
			Property blockWorld = conf.getTerrainBlock("BlockWorld", "Block", blockWorldRange, "the Block ID for Worldgen blocks");
			IDWorldBlock = blockWorld.getInt();

			Property biome = conf.get("biomeID", "biomeID", biomeID,"the initial biome ID");
			IDBiome = biome.getInt();
			
			Property WorldID = conf.get("finiteID", "finiteID", 15,"The id of the Finite WorldType");
			worldID = WorldID.getInt();
			//////////////Volcano Stuff///////////////////////////////////////////////
			BlockLava.HardenRate = conf.get("Volcano Stuff", "Hardening Rate", 5,"this is an arbitrary rate of the conversion of lava to solid lava, scales inversely with viscosity").getInt();
			BlockSolidLava.oreProb = conf.get("Volcano Stuff", "Ore Drop Rate", 0.25,"the chance that solid lava drops ore rather than itself or dust.").getDouble(0.25);
			volcanos = conf.get("Volcano Stuff", "volcano", true,"do volcanoes spawn?" ).getBoolean(true);
			VolcRate = conf.get("Volcano Stuff", "Volcano occurance Rate", volcanoRate,"volcanos occur once every this many chunks").getInt();
			TileEntityVolcano.tickRate = conf.get("Volcano Stuff", "Volcano tick Rate", 10,"volcanos tick once every this many ticks").getInt();
			ashAmount= conf.get("Volcano Stuff", "Ash Volume", ashamount, "The base amount of ash from large explosions, scales with lava type, set below 1000 to completely disable ash").getInt();
			volcanosActive = conf.get("Volcano Stuff", "volcano grow", true,"do volcanos grow?" ).getBoolean(true);
			TileEntityVolcano.majorExplosionRate = conf.get("Volcano Stuff", "Major Explosion Rate", 40000,"the Average number of Volcano Ticks for a large explosion, this causes ash").getInt();
			TileEntityVolcano.minorExplosionRate = conf.get("Volcano Stuff", "Minor Explosion Rate", 4000,"the Average number of Volcano Ticks for a small explosion, no ash, small amounts of Lava").getInt();
			TileEntityVolcano.dormancyRate = conf.get("Volcano Stuff", "Dormancy Rate", 5,"the number of standard deviations needed for the volcano to go dormant").getDouble(5);
			TileEntityVolcano.activityRate = conf.get("Volcano Stuff", "Activity Rate", 4.5,"the number of standard deviations needed for the volcano to go active").getDouble(4.5);
			TileEntityVolcano.eruptionStartRate = conf.get("Volcano Stuff", "Eruption start Rate", 2,"the number of standard deviations needed for the volcano to enter an eruptive period").getDouble(3);
			TileEntityVolcano.eruptionStopRate = conf.get("Volcano Stuff", "Eruption stop Rate", 2,"the number of standard deviations needed for the volcano to exit an eruptive period").getDouble(2);
			debugPrints = conf.get("Misc", "debug Prints", false,"Do Printouts of stuff happen?").getBoolean(false);

			
			trass = conf.get("World Gen", "trass", false).getBoolean(false);
			limestone = conf.get("World Gen", "limestone", false).getBoolean(false);
			

			deleteAsh = conf.get("Misc", "deleteAsh", false).getBoolean(false);
			byte rad = (byte) conf.get("Misc", "explosionRadius", 63, "The radius of the volume affected by explosions, max value of 127, higher settings make the game take longer to load at startup").getInt();
			ExplosionCustom.MAX_RADIUS = (byte) Math.min(rad,127);
			
			paneFix = conf.get("Misc", "fix panes", true, "Do glass panes of mods using outdated methods connect to concrete?").getBoolean(true);
			Vector3.collisionDamage = collisionDamage;
			
			if(deleteAsh)
				conf.get("World Gen", "deleteAsh", false).set(false);
			
			defaultTypeFinite = conf.get("Misc", "defaultFinite", false).getBoolean(false);
				
			
			// Load Item Ids
			Property item = conf.getItem("Item", itemRange);
			item.comment = "The initial Item ID";
			IDItem = item.getInt();

			Property chunksize = conf.get("Chunk Size", "chunksize", chunkSize,"the size of wrapping chunks, If you set this less than 20 it will disabe the wrapping.  This only applies to the new worldtype added by this mod.  it has no effect on any other world type (like Flat or LargeBiomes)");
			ChunkSize = chunksize.getInt();

		}catch(RuntimeException e){
			WorldCore.instance.log.log(Level.INFO, "Config file not found, creating new one");
		}finally{
			conf.save();
		}
	}
	
	
	public static class GUIIDs
	{
		public static int limekiln = 0;
	}

}