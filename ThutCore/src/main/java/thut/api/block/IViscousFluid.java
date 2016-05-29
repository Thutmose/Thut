package thut.api.block;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public interface IViscousFluid
{
    /**
     * How much difference is needed for this fluid to flow.
     * @return
     */
    public int getFlowDifferential(World world, BlockPos pos, IBlockState state, Random rand);
}
