package thut.world.common.blocks.fluids.gases;

import java.util.Random;

import thut.api.Blocks;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

public class BlockCO2Cool extends BlockGas
{

	public BlockCO2Cool(int id) {
		super(id, getFluid(10, "coolCO2"));
		Blocks.coolCO2 = this;
		this.setUnlocalizedName("coolCO2");
		this.setTemperature(295);
		this.setDensity(10);
	}

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
    	int warmsides = 0;
    	Random r = new Random();
    	warmsides += countSides(world, x, y, z, Blocks.warmCO2.blockID);
    	warmsides += countSides(world, x, y, z, Block.fire.blockID);
    	if(Math.random()>0.9)
    	{
        	int meta = world.getBlockMetadata(x, y, z);
        	if(meta==0)
        		world.setBlockToAir(x, y, z);
        	else if(meta<3)
        		world.setBlockMetadataWithNotify(x, y, z, meta-1, 3);
	    	
    	}
    	else if(r.nextInt(6)>=warmsides)
    		super.updateTick(world, x, y, z, rand);
    	else
    	{
    		world.setBlock(x, y, z, Blocks.warmCO2.blockID, world.getBlockMetadata(x, y, z), 3);
    	}
    }
}
