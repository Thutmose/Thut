package dorfgen.conversion;

import dorfgen.WorldGenerator;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Config
{

    public Config(FMLPreInitializationEvent e)
    {
        loadConfig(e);
    }

    void loadConfig(FMLPreInitializationEvent e)
    {
        Configuration config = new Configuration(e.getSuggestedConfigurationFile());
        config.load();

        WorldGenerator.scale = config.getInt("scale", Configuration.CATEGORY_GENERAL, 51, 1, 256,
                "number of blocks per pixel, for best results, use a multiple of 51");
        WorldGenerator.finite = config.getBoolean("finite", Configuration.CATEGORY_GENERAL, true,
                "Whether everything outside the bounds of the image is deep ocean");
        boolean spawnpixel = config.getBoolean("pixel", Configuration.CATEGORY_GENERAL, false,
                "Whether the x and z coordinates for spawn given are pixel or block locations");
        String[] spawnLoc = config.getStringList("worldspawn", Configuration.CATEGORY_GENERAL,
                new String[] { "0", "64", "0" }, "spawn location for the world");
        WorldGenerator.randomSpawn = config.getBoolean("randomSpawn", Configuration.CATEGORY_GENERAL, true,
                "Whether spawn will be set to a random village, if this is true, worldspawn and pixel are ignored");
        int x = Integer.parseInt(spawnLoc[0]);
        int z = Integer.parseInt(spawnLoc[2]);

        if (spawnpixel)
        {
            x *= WorldGenerator.scale;
            z *= WorldGenerator.scale;
        }

        WorldGenerator.spawn = new BlockPos(x, Integer.parseInt(spawnLoc[1]), z);

        WorldGenerator.spawnSite = config.getString("spawnSite", Configuration.CATEGORY_GENERAL, "",
                "Default Site for Spawning in, overrides random spawn and coord based spawn.");

        spawnLoc = config.getStringList("imageShift", Configuration.CATEGORY_GENERAL, new String[] { "0", "0" },
                "offset of the image in world in blocks");
        WorldGenerator.shift = new BlockPos(Integer.parseInt(spawnLoc[0]), 0, Integer.parseInt(spawnLoc[1]));

        config.save();
    }

}
