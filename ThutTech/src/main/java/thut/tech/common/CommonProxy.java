package thut.tech.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.maths.Vector3;
import thut.core.common.Proxy;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.network.PacketLift;

public class CommonProxy implements Proxy
{

    @SubscribeEvent
    public void interactRightClickBlock(final PlayerInteractEvent.RightClickBlock evt)
    {
        if (evt.getHand() == Hand.OFF_HAND || evt.getWorld().isRemote || evt.getItemStack().isEmpty() || evt
                .getItemStack().getItem() != TechCore.LIFT) return;

        final ItemStack itemstack = evt.getItemStack();
        final PlayerEntity playerIn = evt.getPlayer();
        final World worldIn = evt.getWorld();
        if (!evt.getPlayer().isShiftKeyDown())
        {
            if (itemstack.hasTag())
            {
                itemstack.getTag().remove("min");
                itemstack.getTag().remove("time");
                if (itemstack.getTag().isEmpty()) itemstack.setTag(null);
                final String message = "msg.lift.reset";
                if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message));
                evt.setCanceled(true);
            }
            return;
        }

        final BlockPos pos = evt.getPos();
        if (itemstack.hasTag() && playerIn.isShiftKeyDown() && itemstack.getTag().contains("min"))
        {
            final CompoundNBT minTag = itemstack.getTag().getCompound("min");
            itemstack.getTag().putLong("time", worldIn.getGameTime());
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
            itemstack.getTag().putLong("time", worldIn.getGameTime());
        }
    }

    @SubscribeEvent
    public void interactRightClickBlock(final PlayerInteractEvent.RightClickItem evt)
    {
        if (evt.getHand() == Hand.OFF_HAND || evt.getWorld().isRemote || evt.getItemStack().isEmpty() || evt
                .getItemStack().getItem() != TechCore.LIFT) return;
        final ItemStack itemstack = evt.getItemStack();
        final PlayerEntity playerIn = evt.getPlayer();
        final World worldIn = evt.getWorld();

        if (!evt.getPlayer().isShiftKeyDown())
        {
            if (itemstack.hasTag())
            {
                itemstack.getTag().remove("min");
                itemstack.getTag().remove("time");
                if (itemstack.getTag().isEmpty()) itemstack.setTag(null);
            }
            final String message = "msg.lift.reset";
            if (!worldIn.isRemote) playerIn.sendMessage(new TranslationTextComponent(message));
            return;
        }

        final boolean validTag = itemstack.hasTag() && itemstack.getTag().contains("min");
        final boolean alreadyUsed = validTag && itemstack.getTag().getLong("time") - worldIn.getGameTime() == 0;
        if (validTag && !alreadyUsed)
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

    @Override
    public void setup(final FMLCommonSetupEvent event)
    {
        TechCore.packets.registerMessage(PacketLift.class, PacketLift::new);
        MinecraftForge.EVENT_BUS.register(this);
    }
}
