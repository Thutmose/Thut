package thut.world.common.blocks.fluids.gases;

import java.util.Random;

import thut.api.ThutBlocks;
import thut.core.common.blocks.BlockGas;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

public class BlockCO2Warm extends BlockGas
{
	public BlockCO2Warm() {
		super(getFluid(-10, "warmCO2"));
		ThutBlocks.warmCO2 = this;
		this.setBlockName("warmCO2");
		this.setTemperature(373);
		this.setDensity(-10);
	}

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
    	int warmsides = 4;

    	Random r = new Random();
    	warmsides += countSides(world, x, y, z, Blocks.fire);
    	if(r.nextInt(6)<warmsides)
    		super.updateTick(world, x, y, z, rand);
    	else
    		world.setBlock(x, y, z, ThutBlocks.coolCO2, world.getBlockMetadata(x, y, z), 3);
    }
}
