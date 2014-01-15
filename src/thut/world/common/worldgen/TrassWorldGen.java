package thut.world.common.worldgen;

import java.util.Random;

import thut.api.Blocks;
import thut.world.common.WorldCore;
import thut.world.common.blocks.world.BlockWorldGen;
import thut.world.common.corehandlers.ConfigHandler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import cpw.mods.fml.common.IWorldGenerator;

public class TrassWorldGen implements IWorldGenerator
{

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if(world.provider.isSurfaceWorld()&&Math.random()>0.99)//ConfigHandler.trass)
		{
			if(random.nextInt(2)==1){
			int x = chunkX*16 + random.nextInt(16);
			int y = chunkZ*16 + random.nextInt(16);
			int z = 3;//+random.nextInt(80);
			//TODO 
			
		//	ComponentArena arena = new ComponentArena();
		//	arena.generate(world, random, x, z, y);
			
			
			 // (new WorldGen(Blocks.worldGen.blockID,1, 32)).generateSheet(world, random, x, z, y,1,2);
			}
		}

	}
}
