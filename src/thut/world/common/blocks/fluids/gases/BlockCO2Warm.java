package thut.world.common.blocks.fluids.gases;

import java.util.Random;

import thut.api.Blocks;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

public class BlockCO2Warm extends BlockGas
{
	public BlockCO2Warm(int id) {
		super(id, getFluid(-10, "warmCO2"));
		Blocks.warmCO2 = this;
		this.setUnlocalizedName("warmCO2");
		this.setTemperature(373);
		this.setDensity(-10);
	}

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
    	int warmsides = 4;

    	Random r = new Random();
    	warmsides += countSides(world, x, y, z, Block.fire.blockID);
    	if(r.nextInt(6)<warmsides)
    		super.updateTick(world, x, y, z, rand);
    	else
    		world.setBlock(x, y, z, Blocks.coolCO2.blockID, world.getBlockMetadata(x, y, z), 3);
    }
}
