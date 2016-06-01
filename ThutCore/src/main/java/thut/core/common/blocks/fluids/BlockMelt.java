package thut.core.common.blocks.fluids;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import thut.api.maths.ExplosionCustom;
import thut.core.common.ThutCore;
import thut.core.common.blocks.BlockFluid;

public class BlockMelt extends BlockFluid
{
    public static Fluid MELT = new Fluid("thutcore:melt", new ResourceLocation(ThutCore.modid, "blocks/melt"),
            new ResourceLocation(ThutCore.modid, "blocks/melt"));
    public static Block INSTANCE;

    public BlockMelt()
    {
        super(MELT, Material.lava);
        INSTANCE = this;
        ExplosionCustom.melt = this;
        this.setQuantaPerBlock(16);
        setCreativeTab(ThutCore.tabThut);
    }

    @Override
    public IBlockState getSolidState(World worldObj, BlockPos location)
    {
        IBlockState original = worldObj.getBlockState(location);
        if (original.getBlock() != this) return null;
        return BlockSolidMelt.INSTANCE.getDefaultState().withProperty(LEVEL, original.getValue(LEVEL));
    }

    @Override
    public void tryHarden(World worldObj, BlockPos vec)
    {
        IBlockState original = worldObj.getBlockState(vec);
        IBlockState down = worldObj.getBlockState(vec.down());
        if (down.getBlock() == BlockDust.INSTANCE)
        {
            worldObj.setBlockToAir(vec.down());
        }

        if (original.getBlock() != this || !down.getBlock().getMaterial().isSolid()) return;
        worldObj.setBlockState(vec, getSolidState(worldObj, vec));
    }

    @Override
    public int getFlowDifferential(World world, BlockPos pos, IBlockState state, Random rand)
    {
        return 0;
    }

}
