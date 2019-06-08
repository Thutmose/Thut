package thut.core.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

public class BlockRotatable extends Block
{
    public static final PropertyDirection FACING = PropertyDirection.create("facing", Direction.Plane.HORIZONTAL);

    public BlockRotatable(Material blockMaterialIn, MapColor blockMapColorIn)
    {
        super(blockMaterialIn, blockMapColorIn);
    }

    public BlockRotatable(Material blockMaterialIn)
    {
        super(blockMaterialIn);
    }

    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
     * blockstate.
     */
    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

}
