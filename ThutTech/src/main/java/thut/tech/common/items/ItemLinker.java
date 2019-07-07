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
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.maths.Vector3;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.entity.EntityLift;

@Mod.EventBusSubscriber
public class ItemLinker extends Item
{
    @SubscribeEvent
    public static void interactRightClickBlock(final PlayerInteractEvent.RightClickBlock evt)
    {
        if (evt.getHand() == Hand.OFF_HAND || evt.getWorld().isRemote || evt.getItemStack().isEmpty() || !evt
                .getEntityPlayer().isSneaking() || evt.getItemStack().getItem() != TechCore.LIFT) return;
        final ItemStack itemstack = evt.getItemStack();
        final PlayerEntity playerIn = evt.getEntityPlayer();
        final World worldIn = evt.getWorld();
        final BlockPos pos = evt.getPos();

        final boolean hasLift = itemstack.hasTag() && itemstack.getTag().contains("lift");
        if (hasLift) return;

        if (itemstack.hasTag() && playerIn.isSneaking() && itemstack.getTag().contains("min"))
        {
            final CompoundNBT minTag = itemstack.getTag().getCompound("min");
            BlockPos min = pos;
            BlockPos max = Vector3.readFromNBT(minTag, "").getPos();
            final AxisAlignedBB box = new AxisAlignedBB(min, max);
            min = new BlockPos(box.minX, box.minY, box.minZ);
            max = new BlockPos(box.maxX, box.maxY, box.maxZ);
            final BlockPos mid = min.add((max.getX() - min.getX()) / 2, 0, (max.getZ() - min.getZ()) / 2);
            min = min.subtract(mid);
            max = max.subtract(mid);
            final int dw = Math.max(max.getX() - min.getX(), max.getZ() - min.getZ());
            if (max.getY() - min.getY() > TechCore.config.maxHeight || dw > 2 * TechCore.config.maxRadius + 1)
            {
                final String message = "msg.lift.toobig";
                if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message));
                return;
            }
            final int num = (dw + 1) * (max.getY() - min.getY() + 1);
            int count = 0;
            for (final ItemStack item : playerIn.inventory.mainInventory)
                if (item.getItem() == TechCore.LIFT) count += item.getCount();
            if (!playerIn.abilities.isCreativeMode && count < num)
            {
                final String message = "msg.lift.noblock";
                if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message, num));
                return;
            }
            else if (!playerIn.abilities.isCreativeMode) playerIn.inventory.clearMatchingItems(b -> b
                    .getItem() == TechCore.LIFT, num);
            if (!worldIn.isRemote)
            {
                final EntityLift lift = IBlockEntity.BlockEntityFormer.makeBlockEntity(worldIn, min, max, mid,
                        EntityLift.TYPE);
                if (lift != null) lift.owner = playerIn.getUniqueID();
                final String message = lift != null ? "msg.lift.create" : "msg.lift.fail";
                playerIn.sendMessage(new TranslationTextComponent(message));
            }
            itemstack.getTag().remove("min");
            evt.setCanceled(true);
        }
        else
        {
            if (!itemstack.hasTag()) itemstack.setTag(new CompoundNBT());
            final CompoundNBT min = new CompoundNBT();
            Vector3.getNewVector().set(pos).writeToNBT(min, "");
            itemstack.getTag().put("min", min);
            final String message = "msg.lift.setcorner";
            if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message, pos));
            evt.setCanceled(true);
            itemstack.getTag().putLong("time", worldIn.getDayTime());
        }
    }

    @SubscribeEvent
    public static void interactRightClickBlock(final PlayerInteractEvent.RightClickItem evt)
    {
        if (evt.getHand() == Hand.OFF_HAND || evt.getWorld().isRemote || evt.getItemStack().isEmpty() || !evt
                .getEntityPlayer().isSneaking() || evt.getItemStack().getItem() != TechCore.LIFT) return;
        final ItemStack itemstack = evt.getItemStack();
        final PlayerEntity playerIn = evt.getEntityPlayer();
        final World worldIn = evt.getWorld();
        if (itemstack.hasTag() && playerIn.isSneaking() && itemstack.getTag().contains("min") && itemstack.getTag()
                .getLong("time") != worldIn.getDayTime())
        {
            final CompoundNBT minTag = itemstack.getTag().getCompound("min");
            final Vec3d loc = playerIn.getPositionVector().add(0, playerIn.getEyeHeight(), 0).add(playerIn.getLookVec()
                    .scale(2));
            final BlockPos pos = new BlockPos(loc);
            BlockPos min = pos;
            BlockPos max = Vector3.readFromNBT(minTag, "").getPos();
            final AxisAlignedBB box = new AxisAlignedBB(min, max);
            min = new BlockPos(box.minX, box.minY, box.minZ);
            max = new BlockPos(box.maxX, box.maxY, box.maxZ);
            final BlockPos mid = min.add((max.getX() - min.getX()) / 2, 0, (max.getZ() - min.getZ()) / 2);
            min = min.subtract(mid);
            max = max.subtract(mid);
            final int dw = Math.max(max.getX() - min.getX(), max.getZ() - min.getZ());
            if (max.getY() - min.getY() > TechCore.config.maxHeight || dw > 2 * TechCore.config.maxRadius + 1)
            {
                final String message = "msg.lift.toobig";
                if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message));
                return;
            }
            final int num = (dw + 1) * (max.getY() - min.getY() + 1);
            int count = 0;
            for (final ItemStack item : playerIn.inventory.mainInventory)
                if (item.getItem() == TechCore.LIFT) count += item.getCount();
            if (!playerIn.abilities.isCreativeMode && count < num)
            {
                final String message = "msg.lift.noblock";
                if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message, num));
                return;
            }
            else if (!playerIn.abilities.isCreativeMode) playerIn.inventory.clearMatchingItems(i -> i
                    .getItem() == TechCore.LIFT, num);
            if (!worldIn.isRemote)
            {
                final EntityLift lift = IBlockEntity.BlockEntityFormer.makeBlockEntity(worldIn, min, max, mid,
                        EntityLift.TYPE);
                if (lift != null) lift.owner = playerIn.getUniqueID();
                final String message = lift != null ? "msg.lift.create" : "msg.lift.fail";
                playerIn.sendMessage(new TranslationTextComponent(message));
            }
            itemstack.getTag().remove("min");
        }
    }

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
        if (!stack.hasTag()) return ActionResultType.PASS;
        else
        {
            final BlockState state = worldIn.getBlockState(pos);
            final Direction face = context.getFace();

            if (state.getBlock() == TechCore.LIFTCONTROLLER && !playerIn.isSneaking())
            {
                final TileEntityLiftAccess te = (TileEntityLiftAccess) worldIn.getTileEntity(pos);
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
            if (playerIn.isSneaking() && lift != null && state.getBlock() == TechCore.LIFTCONTROLLER)
            {
                if (face != Direction.UP && face != Direction.DOWN)
                {
                    final TileEntityLiftAccess te = (TileEntityLiftAccess) worldIn.getTileEntity(pos);

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
            else if (playerIn.isSneaking() && state.getBlock() == TechCore.LIFTCONTROLLER)
            {
                if (face != Direction.UP && face != Direction.DOWN)
                {
                    final TileEntityLiftAccess te = (TileEntityLiftAccess) worldIn.getTileEntity(pos);
                    te.editFace[face.ordinal()] = !te.editFace[face.ordinal()];
                    te.setSidePage(face, 0);
                    final String message = "msg.editMode";
                    if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message, te.editFace[face
                            .ordinal()]));
                    return ActionResultType.SUCCESS;
                }
            }
            else if (playerIn.isSneaking())
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
    public boolean shouldSyncTag()
    {
        return true;
    }
}
