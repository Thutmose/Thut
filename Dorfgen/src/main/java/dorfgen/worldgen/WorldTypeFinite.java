package dorfgen.worldgen;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.IChunkGenerator;

public class WorldTypeFinite extends WorldType {

	public WorldTypeFinite(String name) {
		super(name);
	}

    @Override
	public BiomeProvider getBiomeProvider(World world)
    {
    //	new Exception().printStackTrace();
    	return new BiomeProviderFinite(world);
    }
    
    @Override
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions)
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
