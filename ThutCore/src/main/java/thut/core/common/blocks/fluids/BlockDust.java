package thut.core.common.blocks.fluids;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import thut.api.maths.ExplosionCustom;
import thut.core.common.ThutCore;
import thut.core.common.blocks.BlockFluid;

public class BlockDust extends BlockFluid
{
    public static Fluid DUST = new Fluid("thutcore:dust", new ResourceLocation(ThutCore.modid, "blocks/dust"),
            new ResourceLocation(ThutCore.modid, "blocks/dust"));
    public static Block INSTANCE;

    public BlockDust()
    {
        super(DUST, Material.snow);
        INSTANCE = this;
        ExplosionCustom.dust = this;
        this.setQuantaPerBlock(16);
        setCreativeTab(ThutCore.tabThut);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state)
    {
        float h = getFluidHeightForRender(world, pos);
        return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + h, pos.getZ() + 1);
    }

    @Override
    public boolean canCollideCheck(IBlockState state, boolean fullHit)
    {
        return true;
    }

    @Override
    public IBlockState getSolidState(World worldObj, BlockPos location)
    {
        return null;
    }

    @Override
    public void tryHarden(World worldObj, BlockPos vec)
    {
    }

    @Override
    public int getFlowDifferential(World world, BlockPos pos, IBlockState state, Random rand)
    {
        return 5;
    }

    @Override
    public boolean canCreatureSpawn(IBlockAccess world, BlockPos pos,
            net.minecraft.entity.EntityLiving.SpawnPlacementType type)
    {
        return true;
    }

}