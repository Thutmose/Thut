package thut.world.common.finiteWorld;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;


public class WorldTypeCustom extends WorldType
{       
    public WorldTypeCustom(int id, String string) {
	        super(id, string);         
	}
	
	@Override
	public WorldChunkManager getChunkManager(World world) {
	        // This is our ChunkManager class, it controls rain, temp, biomes, and spawn location
	        return new WorldChunkManager(world);
	}
	
	@Override
	public IChunkProvider getChunkGenerator(World world, String generatorOptions) {
	        return new WorldChunkProviderFinite(world);
	}

}