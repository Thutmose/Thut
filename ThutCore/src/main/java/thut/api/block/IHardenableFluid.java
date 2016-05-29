package thut.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public interface IHardenableFluid
{
    public IBlockState getSolidState(World worldObj, BlockPos location);
    
    public void tryHarden(World worldObj, BlockPos vec);
}
