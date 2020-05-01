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
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import thut.core.common.network.TileUpdate;
import thut.tech.common.TechCore;

public class ControllerBlock extends Block
{
    public static final BooleanProperty CALLED  = BooleanProperty.create("called");
    public static final BooleanProperty MASKED  = BooleanProperty.create("masked");
    public static final BooleanProperty CURRENT = BooleanProperty.create("current");

    public ControllerBlock(final Block.Properties props)
    {
        super(props);
        this.setDefaultState(this.stateContainer.getBaseState().with(ControllerBlock.CALLED, false).with(
                ControllerBlock.MASKED, false).with(ControllerBlock.CURRENT, false));
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new ControllerTile();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(ControllerBlock.CALLED);
        builder.add(ControllerBlock.CURRENT);
        builder.add(ControllerBlock.MASKED);
    }

    // RedStone stuff
    @Override
    public int getWeakPower(final BlockState blockState, final IBlockReader blockAccess, final BlockPos pos,
            Direction side)
    {
        final ControllerTile te = (ControllerTile) blockAccess.getTileEntity(pos);
        final boolean here = blockState.get(ControllerBlock.CURRENT);
        final boolean called = blockState.get(ControllerBlock.CALLED);
        side = side.getOpposite();
        if (te.isSideOn(side))
        {
            if (te.isCallPanel(side)) return called ? 15 : 0;
            if (te.isFloorDisplay(side)) return here ? 15 : 0;
            return here ? 5 : 0;
        }
        return 0;
    }

    @Override
    public int getStrongPower(final BlockState blockState, final IBlockReader blockAccess, final BlockPos pos,
            Direction side)
    {
        final ControllerTile te = (ControllerTile) blockAccess.getTileEntity(pos);
        final boolean here = blockState.get(ControllerBlock.CURRENT);
        final boolean called = blockState.get(ControllerBlock.CALLED);
        side = side.getOpposite();
        if (te.isSideOn(side))
        {
            if (te.isCallPanel(side)) return called ? 15 : 0;
            if (te.isFloorDisplay(side)) return here ? 15 : 0;
        }
        return 0;
    }

    @Override
    public boolean shouldCheckWeakPower(final BlockState state, final IWorldReader world, final BlockPos pos,
            Direction side)
    {
        final ControllerTile te = (ControllerTile) world.getTileEntity(pos);
        side = side.getOpposite();
        final boolean called = state.get(ControllerBlock.CALLED);
        // Note that we do not check if the side is on, as this allows
        // only buttons, with no display number!
        if (te.isCallPanel(side) && !called) return true;
        return false;
    }

    /**
     * Can this block provide power. Only wire currently seems to have this
     * change based on its state.
     */
    @Override
    public boolean canProvidePower(final BlockState state)
    {
        return true;
    }

    // End of Redstone

    @Override
    public ActionResultType onBlockActivated(final BlockState state, final World worldIn, final BlockPos pos,
            final PlayerEntity playerIn, final Hand handIn, final BlockRayTraceResult hit)
    {
        final ItemStack heldItem = playerIn.getHeldItem(handIn);
        final Direction side = hit.getFace();
        final boolean linkerOrStick = heldItem.getItem() == Items.STICK || heldItem.getItem() == TechCore.LINKER;
        if (linkerOrStick && playerIn.isShiftKeyDown())
        {
            final ControllerTile te = (ControllerTile) worldIn.getTileEntity(pos);
            if (te == null) return ActionResultType.PASS;
            if (te.isSideOn(side))
            {
                te.setSide(side, false);
                if (!te.getWorld().isRemote) TileUpdate.sendUpdate(te);
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.PASS;
        }
        final ControllerTile te = (ControllerTile) worldIn.getTileEntity(pos);
        if (te == null) return ActionResultType.PASS;

        if (!linkerOrStick && side == Direction.DOWN)
        {
            if (heldItem.getItem() instanceof BlockItem)
            {
                final BlockItemUseContext context = new BlockItemUseContext(new ItemUseContext(playerIn, handIn, hit));
                te.copiedState = ((BlockItem) heldItem.getItem()).getBlock().getStateForPlacement(context);
                worldIn.setBlockState(pos, state.with(ControllerBlock.MASKED, true));
                if (!te.getWorld().isRemote) TileUpdate.sendUpdate(te);
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.PASS;
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
                return ActionResultType.SUCCESS;
            }
        }
        else if (te.isSideOn(side)) if (heldItem.getItem() == TechCore.LINKER)
        {
            if (!worldIn.isRemote && !te.isEditMode(side) && !te.isFloorDisplay(side))
            {
                te.setSidePage(side, (te.getSidePage(side) + 1) % 8);
                if (playerIn instanceof ServerPlayerEntity) te.sendUpdate((ServerPlayerEntity) playerIn);
                TileUpdate.sendUpdate(te);
            }
            return ActionResultType.SUCCESS;
        }
        else if (!playerIn.isShiftKeyDown())
        {
            final float hitX = (float) hit.getHitVec().x;
            final float hitY = (float) hit.getHitVec().y;
            final float hitZ = (float) hit.getHitVec().z;
            return te.doButtonClick(playerIn, side, hitX, hitY, hitZ) ? ActionResultType.SUCCESS
                    : ActionResultType.PASS;
        }

        if (playerIn.isShiftKeyDown() && handIn == Hand.MAIN_HAND && playerIn instanceof ServerPlayerEntity)
        {
            final boolean sideOn = !te.isSideOn(side);
            playerIn.sendMessage(new TranslationTextComponent("msg.lift.side." + (sideOn ? "on" : "off")));
            if (sideOn)
            {
                final boolean call = te.isCallPanel(side);
                final boolean edit = te.isEditMode(side);
                final boolean display = te.isFloorDisplay(side);
                if (edit) playerIn.sendMessage(new TranslationTextComponent("msg.lift.side.edit"));
                else if (call) playerIn.sendMessage(new TranslationTextComponent("msg.lift.side.call"));
                else if (display) playerIn.sendMessage(new TranslationTextComponent("msg.lift.side.display"));
                else
                {
                    final int page = te.getSidePage(side);
                    playerIn.sendMessage(new TranslationTextComponent("msg.lift.side.page", page));
                }
            }
        }
        return ActionResultType.PASS;
    }
}
