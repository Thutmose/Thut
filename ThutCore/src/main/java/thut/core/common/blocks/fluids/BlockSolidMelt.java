package thut.core.common.blocks.fluids;

import static net.minecraftforge.fluids.BlockFluidBase.FLUID_RENDER_PROPS;
import static net.minecraftforge.fluids.BlockFluidBase.LEVEL;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fluids.BlockFluidBase;
import thut.api.maths.ExplosionCustom;

public class BlockSolidMelt extends Block
{
    public static Block INSTANCE;

    public BlockSolidMelt()
    {
        super(Material.rock);
        ExplosionCustom.solidmelt = this;
        INSTANCE = this;
        this.setDefaultState(blockState.getBaseState().withProperty(LEVEL, 0));
    }

    @Override
    protected BlockState createBlockState()
    {
        return new ExtendedBlockState(this, new IProperty[] { LEVEL }, FLUID_RENDER_PROPS);
    }

    /** Convert the BlockState into the correct metadata value */
    @Override
    public int getMetaFromState(IBlockState state)
    {
        return ((Integer) state.getValue(BlockFluidBase.LEVEL)).intValue();
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return true;
    }

    @Override
    public IBlockState getExtendedState(IBlockState oldState, IBlockAccess worldIn, BlockPos pos)
    {
        IExtendedBlockState state = (IExtendedBlockState) oldState;
        return state;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean isFullCube()
    {
        return false;
    }
}
