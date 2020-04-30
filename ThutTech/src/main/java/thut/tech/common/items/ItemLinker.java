package thut.tech.common.items;

import java.util.UUID;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.lift.ControllerTile;
import thut.tech.common.entity.EntityLift;

public class ItemLinker extends Item
{
    public ItemLinker(final Item.Properties props)
    {
        super(props);
    }

    @Override
    public ActionResultType onItemUse(final ItemUseContext context)
    {
        final ItemStack stack = context.getItem();
        final PlayerEntity playerIn = context.getPlayer();
        final BlockPos pos = context.getPos();
        final World worldIn = context.getWorld();
        final BlockState state = worldIn.getBlockState(pos);
        final Direction face = context.getFace();

        final boolean linked = stack.hasTag() && stack.getTag().contains("lift");
        if (!linked && state.getBlock() == TechCore.LIFTCONTROLLER)
        {
            final ControllerTile te = (ControllerTile) worldIn.getTileEntity(pos);
            te.editFace[face.ordinal()] = !te.editFace[face.ordinal()];
            return ActionResultType.SUCCESS;
        }

        if (!stack.hasTag()) return ActionResultType.PASS;
        else
        {
            if (state.getBlock() == TechCore.LIFTCONTROLLER && !playerIn.isShiftKeyDown())
            {
                final ControllerTile te = (ControllerTile) worldIn.getTileEntity(pos);
                te.setSide(face, true);
                return ActionResultType.SUCCESS;
            }

            UUID liftID;
            try
            {
                liftID = UUID.fromString(stack.getTag().getString("lift"));
            }
            catch (final Exception e)
            {
                liftID = new UUID(0000, 0000);
            }
            final EntityLift lift = EntityLift.getLiftFromUUID(liftID, worldIn);
            if (playerIn.isShiftKeyDown() && lift != null && state.getBlock() == TechCore.LIFTCONTROLLER)
            {
                if (face != Direction.UP && face != Direction.DOWN)
                {
                    final ControllerTile te = (ControllerTile) worldIn.getTileEntity(pos);
                    te.setLift(lift);
                    int floor = te.getButtonFromClick(face, context.getHitVec().x, context.getHitVec().y, context
                            .getHitVec().z);
                    te.setFloor(floor);
                    if (floor >= 64) floor = 64 - floor;
                    final String message = "msg.floorSet";
                    if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message, floor));
                    return ActionResultType.SUCCESS;
                }
            }
            else if (playerIn.isShiftKeyDown() && state.getBlock() == TechCore.LIFTCONTROLLER)
            {
                if (face != Direction.UP && face != Direction.DOWN)
                {
                    final ControllerTile te = (ControllerTile) worldIn.getTileEntity(pos);
                    te.editFace[face.ordinal()] = !te.editFace[face.ordinal()];
                    te.setSidePage(face, 0);
                    final String message = "msg.editMode";
                    if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message, te.editFace[face
                            .ordinal()]));
                    return ActionResultType.SUCCESS;
                }
            }
            else if (playerIn.isShiftKeyDown())
            {
                stack.setTag(new CompoundNBT());
                final String message = "msg.linker.reset";
                if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message));
            }
        }
        return ActionResultType.PASS;
    }

    public void setLift(final EntityLift lift, final ItemStack stack)
    {
        if (stack.getTag() == null) stack.setTag(new CompoundNBT());
        stack.getTag().putString("lift", lift.getCachedUniqueIdString());
    }

    @Override
    public ITextComponent getDisplayName(final ItemStack stack)
    {
        if (stack.hasTag() && stack.getTag().contains("lift")) return new TranslationTextComponent(
                "item.thuttech.linker.linked");
        return super.getDisplayName(stack);
    }

    @Override
    public boolean shouldSyncTag()
    {
        return true;
    }
}
