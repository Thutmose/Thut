package dorfgen.worldgen;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldTypeFinite extends WorldType {

	public WorldTypeFinite(String name) {
		super(name);
	}

    @Override
	public WorldChunkManager getChunkManager(World world)
    {
    //	new Exception().printStackTrace();
    	return new WorldChunkManagerFinite(world);
    }
    
    @Override
    public IChunkProvider getChunkGenerator(World world, String generatorOptions)
    {
    //	new Exception().printStackTrace();
        return new ChunkProviderFinite(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions);
    }
    
    /**
     * Get the height to render the clouds for this world type
     * @return The height to render clouds at
     */
    public float getCloudHeight()
    {
        return 200.0F;
    }
}
