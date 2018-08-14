package thut.api.entity.blockentity;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeHooks;
import thut.lib.CompatWrapper;

public class BlockEntityInteractHandler
{
    final IBlockEntity blockEntity;
    final Entity       theEntity;

    public BlockEntityInteractHandler(IBlockEntity entity)
    {
        blockEntity = entity;
        theEntity = (Entity) entity;
    }

    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, @Nullable ItemStack stack,
            EnumHand hand)
    {
        vec = vec.addVector(vec.x > 0 ? -0.01 : 0.01, vec.y > 0 ? -0.01 : 0.01,
                vec.z > 0 ? -0.01 : 0.01);
        Vec3d playerPos = player.getPositionVector().addVector(0, player.isServerWorld() ? player.getEyeHeight() : 0,
                0);
        Vec3d start = playerPos;
        Vec3d end = playerPos.add(player.getLookVec().scale(4.5));
        RayTraceResult trace = IBlockEntity.BlockEntityFormer.rayTraceInternal(start, end, blockEntity);
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
            hitX = (float) (trace.hitVec.x - pos.getX());
            hitY = (float) (trace.hitVec.y - pos.getY());
            hitZ = (float) (trace.hitVec.z - pos.getZ());
            side = trace.sideHit;
        }
        IBlockState state = blockEntity.getFakeWorld().getBlockState(pos);
        boolean activate = CompatWrapper.interactWithBlock(state.getBlock(), blockEntity.getFakeWorld(), pos, state,
                player, hand, stack, side, hitX, hitY, hitZ);
        if (activate) return EnumActionResult.SUCCESS;
        else if (trace == null || !state.getMaterial().isSolid())
        {
            Vec3d playerLook = playerPos.add(player.getLookVec().scale(4));
            RayTraceResult result = theEntity.getEntityWorld().rayTraceBlocks(playerPos, playerLook, false, true,
                    false);
            if (result != null && result.typeOfHit == Type.BLOCK)
            {
                pos = result.getBlockPos();
                state = theEntity.getEntityWorld().getBlockState(pos);
                hitX = (float) (result.hitVec.x - pos.getX());
                hitY = (float) (result.hitVec.y - pos.getY());
                hitZ = (float) (result.hitVec.z - pos.getZ());
                if (player.isSneaking() && !stack.isEmpty())
                {
                    EnumActionResult itemUse = ForgeHooks.onPlaceItemIntoWorld(stack, player, player.getEntityWorld(),
                            pos, result.sideHit, hitX, hitY, hitZ, hand);
                    if (itemUse != EnumActionResult.PASS) return itemUse;
                }
                activate = CompatWrapper.interactWithBlock(state.getBlock(), theEntity.getEntityWorld(), pos, state,
                        player, hand, stack, result.sideHit, hitX, hitY, hitZ);
                if (activate) return EnumActionResult.SUCCESS;
                else if (!player.isSneaking() && !stack.isEmpty())
                {
                    EnumActionResult itemUse = ForgeHooks.onPlaceItemIntoWorld(stack, player, player.getEntityWorld(),
                            pos, result.sideHit, hitX, hitY, hitZ, hand);
                    if (itemUse != EnumActionResult.PASS) return itemUse;
                }
            }
            return EnumActionResult.PASS;
        }
        return EnumActionResult.PASS;
    }

    public boolean processInitialInteract(EntityPlayer player, @Nullable ItemStack stack, EnumHand hand)
    {
        return false;
    }
}
