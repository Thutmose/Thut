package thut.world.common.worldgen;

import java.util.Random;

import thut.api.Blocks;
import thut.world.common.WorldCore;
import thut.world.common.blocks.world.BlockWorldGen;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import cpw.mods.fml.common.IWorldGenerator;

public class BiomeGenChalk extends BiomeGenBase
{

	public BiomeGenChalk(int par1) {
		super(par1);
        this.theBiomeDecorator.treesPerChunk = -999;
        this.theBiomeDecorator.flowersPerChunk = 4;
        this.theBiomeDecorator.grassPerChunk = 10;
        this.fillerBlock = (byte)Blocks.worldGen.blockID;
        this.topBlock = (byte)Block.grass.blockID;
        this.biomeName = "chalk";
	}

	
}
