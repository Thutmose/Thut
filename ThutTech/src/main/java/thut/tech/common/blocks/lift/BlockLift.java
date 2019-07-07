package thut.tech.common.blocks.lift;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.World;
import thut.core.common.network.TileUpdate;
import thut.tech.common.TechCore;

public class BlockLift extends Block
{
    public static final BooleanProperty CALLED = BooleanProperty.create("called");

    public static final BooleanProperty CURRENT = BooleanProperty.create("current");

    public BlockLift(Block.Properties props)
    {
        super(props);
        this.setDefaultState(this.stateContainer.getBaseState().with(BlockLift.CALLED, false).with(BlockLift.CURRENT,
                false));
    }

    /**
     * Can this block provide power. Only wire currently seems to have this
     * change based on its state.
     */
    @Override
    public boolean canProvidePower(BlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new TileEntityLiftAccess();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(BlockLift.CALLED);
        builder.add(BlockLift.CURRENT);
    }

    @Override
    public BlockState getExtendedState(BlockState state, IBlockReader world, BlockPos pos)
    {
        final TileEntityLiftAccess tile = (TileEntityLiftAccess) world.getTileEntity(pos);
        if (tile != null && tile.copiedState != null && tile.getWorld().isRemote) return tile.copiedState;
        return state;
    }

    @Override
    public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
    {
        return 0;
    }

    ////////////////////////////////////////////////////// RedStone
    ////////////////////////////////////////////////////// stuff/////////////////////////////////////////////////
    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
    {
        return blockState.get(BlockLift.CURRENT) ? 15 : 0;
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand handIn,
            BlockRayTraceResult hit)
    {
        final ItemStack heldItem = playerIn.getHeldItem(handIn);
        final Direction side = hit.getFace();
        final boolean linkerOrStick = heldItem.getItem() == Items.STICK || heldItem.getItem() == TechCore.LINKER;
        if (linkerOrStick && playerIn.isSneaking()) return false;
        final TileEntityLiftAccess te = (TileEntityLiftAccess) worldIn.getTileEntity(pos);
        if (te == null) return false;

        if (!linkerOrStick && side == Direction.DOWN)
        {
            if (heldItem.getItem() instanceof BlockItem)
            {
                final BlockItemUseContext context = new BlockItemUseContext(new ItemUseContext(playerIn, handIn, hit));
                te.copiedState = ((BlockItem) heldItem.getItem()).getBlock().getStateForPlacement(context);
                if (!te.getWorld().isRemote) TileUpdate.sendUpdate(te);
                return true;
            }
            return false;
        }
        if (!te.isSideOn(side) || heldItem.getItem() == Items.STICK)
        {
            if (linkerOrStick)
            {
                if (!worldIn.isRemote)
                {
                    te.setSide(side, !te.isSideOn(side));
                    if (worldIn instanceof ServerWorld) te.sendUpdate((ServerPlayerEntity) playerIn);
                }
                return true;
            }
        }
        else if (te.isSideOn(side)) if (heldItem.getItem() == TechCore.LINKER)
        {
            if (!worldIn.isRemote && !te.editFace[side.ordinal()] && !te.floorDisplay[side.ordinal()])
            {
                te.setSidePage(side, (te.getSidePage(side) + 1) % 8);
                if (playerIn instanceof ServerPlayerEntity) te.sendUpdate((ServerPlayerEntity) playerIn);
                TileUpdate.sendUpdate(te);
            }
            return true;
        }
        else
        {
            final float hitX = (float) hit.getHitVec().x;
            final float hitY = (float) hit.getHitVec().y;
            final float hitZ = (float) hit.getHitVec().z;
            return te.doButtonClick(playerIn, side, hitX, hitY, hitZ);
        }
        return false;
    }
}
