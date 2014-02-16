package thut.world.common.worldgen;

import java.util.Random;

import thut.api.ThutBlocks;
import thut.world.common.Volcano;
import thut.world.common.blocks.world.BlockVolcano;
import thut.world.common.blocks.world.BlockWorldGen;
import thut.world.common.corehandlers.ConfigHandler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import cpw.mods.fml.common.IWorldGenerator;

public class VolcanoWorldGen implements IWorldGenerator{
	
	Random r = new Random();
	long seed = 0;

	public void setSeed(long seed)
	{
		r.setSeed(seed);
	}
	
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world,
			IChunkProvider chunkGenerator, IChunkProvider chunkProvider) 
	{
		int[] loc = Volcano.volcanoGen(chunkX, chunkZ, world);
		if(loc!=null)
		{
			world.setBlock(loc[0], 5, loc[1], ThutBlocks.volcano, 0, 3);
		}
	}

}
