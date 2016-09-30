package thut.tech.common.entity;

import javax.annotation.Nullable;

import io.netty.buffer.Unpooled;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import thut.api.entity.blockentity.IBlockEntity;
import thut.tech.common.items.ItemLinker;
import thut.tech.common.network.PacketPipeline;

public class LiftInteractHandler
{
    final EntityLift lift;

    public LiftInteractHandler(EntityLift lift)
    {
        this.lift = lift;
    }

    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, @Nullable ItemStack stack,
            EnumHand hand)
    {
        vec = vec.addVector(vec.xCoord > 0 ? -0.01 : 0.01, vec.yCoord > 0 ? -0.01 : 0.01,
                vec.zCoord > 0 ? -0.01 : 0.01);
        Vec3d playerPos = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
        Vec3d start = playerPos.subtract(lift.getPositionVector());
        RayTraceResult trace = IBlockEntity.BlockEntityFormer.rayTraceInternal(start.add(lift.getPositionVector()),
                vec.add(lift.getPositionVector()), lift);
        BlockPos pos;
        float hitX, hitY, hitZ;
        EnumFacing side = EnumFacing.DOWN;
        if (trace == null)
        {
            pos = new BlockPos(0, 0, 0);
            hitX = hitY = hitZ = 0;
        }
        else
        {
            pos = trace.getBlockPos();
            hitX = (float) (trace.hitVec.xCoord - pos.getX());
            hitY = (float) (trace.hitVec.yCoord - pos.getY());
            hitZ = (float) (trace.hitVec.zCoord - pos.getZ());
            side = trace.sideHit;
        }
        IBlockState state = lift.getFakeWorld().getBlockState(pos);
        boolean activate = state.getBlock().onBlockActivated(lift.getFakeWorld(), pos, state, player, hand, stack, side,
                hitX, hitY, hitZ);
        if (activate) return EnumActionResult.SUCCESS;
        else if (trace == null || !state.getMaterial().isSolid())
        {
            Vec3d playerLook = playerPos.add(player.getLookVec().scale(4));
            RayTraceResult result = lift.worldObj.rayTraceBlocks(playerPos, playerLook, false, true, false);
            if (result != null && result.typeOfHit == Type.BLOCK)
            {
                pos = result.getBlockPos();
                state = lift.worldObj.getBlockState(pos);
                hitX = (float) (result.hitVec.xCoord - pos.getX());
                hitY = (float) (result.hitVec.yCoord - pos.getY());
                hitZ = (float) (result.hitVec.zCoord - pos.getZ());
                activate = state.getBlock().onBlockActivated(lift.getEntityWorld(), pos, state, player, hand, stack,
                        result.sideHit, hitX, hitY, hitZ);
                if (activate && lift.worldObj.isRemote)
                {
                    PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(25));
                    buffer.writeFloat(hitX);
                    buffer.writeFloat(hitY);
                    buffer.writeFloat(hitZ);
                    buffer.writeByte(result.sideHit.ordinal());
                    buffer.writeBlockPos(pos);
                    PacketPipeline.sendToServer(new PacketPipeline.ServerPacket(buffer));
                    return EnumActionResult.SUCCESS;
                }
            }
            return EnumActionResult.PASS;
        }
        return EnumActionResult.PASS;
    }

    public boolean processInitialInteract(EntityPlayer player, @Nullable ItemStack stack, EnumHand hand)
    {
        if (stack != null && stack.getItem() == Items.STICK)
        {
            if (stack.getDisplayName().equals("x"))
            {
                lift.setDestX((float) (lift.posX + 10));
                return true;
            }
            else if (stack.getDisplayName().equals("-x"))
            {
                lift.setDestX((float) (lift.posX - 10));
                return true;
            }
            else if (stack.getDisplayName().equals("z"))
            {
                lift.setDestZ((float) (lift.posZ + 10));
                return true;
            }
            else if (stack.getDisplayName().equals("-z"))
            {
                lift.setDestZ((float) (lift.posZ - 10));
                return true;
            }
            else if (stack.getDisplayName().equals("y"))
            {
                lift.setDestY((float) (lift.posY + 10));
                return true;
            }
            else if (stack.getDisplayName().equals("-y"))
            {
                lift.setDestY((float) (lift.posY - 10));
                return true;
            }
        }

        if (player.isSneaking() && stack != null && stack.getItem() instanceof ItemLinker
                && ((lift.owner != null && player.getUniqueID().equals(lift.owner))
                        || player.capabilities.isCreativeMode))
        {
            if (stack.getTagCompound() == null)
            {
                stack.setTagCompound(new NBTTagCompound());
            }
            stack.getTagCompound().setString("lift", lift.getCachedUniqueIdString());

            String message = "msg.liftSet.name";

            if (lift.worldObj.isRemote) player.addChatMessage(new TextComponentTranslation(message));
            return true;
        }
        else if (stack != null && stack.getItem() instanceof ItemLinker
                && ((lift.owner != null && player.getUniqueID().equals(lift.owner))
                        || player.capabilities.isCreativeMode))
        {
            if (!lift.worldObj.isRemote && lift.owner != null)
            {
                Entity ownerentity = lift.worldObj.getPlayerEntityByUUID(lift.owner);
                String message = "msg.lift.owner";

                player.addChatMessage(new TextComponentTranslation(message, ownerentity.getName()));
            }
            return true;
        }
        if ((player.isSneaking() && stack != null
                && (player.getHeldItem(hand).getItem().getUnlocalizedName().toLowerCase().contains("wrench")
                        || player.getHeldItem(hand).getItem().getUnlocalizedName().toLowerCase().contains("screwdriver")
                        || player.getHeldItem(hand).getItem().getUnlocalizedName()
                                .equals(Items.STICK.getUnlocalizedName())))
                && ((lift.owner != null && player.getUniqueID().equals(lift.owner))
                        || player.capabilities.isCreativeMode))
        {
            if (!lift.worldObj.isRemote)
            {
                String message = "msg.lift.killed";
                player.addChatMessage(new TextComponentTranslation(message));
                lift.setHealth(0);
                lift.setDead();
            }
            return true;
        }
        return false;
    }
}
