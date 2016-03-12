package thut.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import thut.api.maths.Vector3;

public interface IHardenableFluid
{
    public IBlockState getSolidState(World worldObj, Vector3 location);
    
    public void tryHarden(World worldObj, Vector3 vec);
}
