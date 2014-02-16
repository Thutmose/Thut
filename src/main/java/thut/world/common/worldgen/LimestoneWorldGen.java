package thut.world.common.worldgen;

import java.util.Random;

import thut.api.ThutBlocks;
import thut.world.common.WorldCore;
import thut.world.common.blocks.world.BlockWorldGen;
import thut.world.common.corehandlers.ConfigHandler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import cpw.mods.fml.common.IWorldGenerator;

public class LimestoneWorldGen implements IWorldGenerator
{

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.isSurfaceWorld()&&ConfigHandler.limestone){
			if(random.nextInt(10)==1){
			int x = chunkX*16 + random.nextInt(16);
			int y = chunkZ*16 + random.nextInt(16);
			int z = 20+random.nextInt(80);
			//TODO 
			  (new WorldGen(ThutBlocks.worldGen,2, 64)).generateSheet(world, random, x, z, y,1,3);
			}
		}

	}
}
