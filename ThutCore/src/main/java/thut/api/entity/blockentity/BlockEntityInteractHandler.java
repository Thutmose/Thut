package thut.api.entity.blockentity;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.ForgeHooks;

public class BlockEntityInteractHandler
{
    final IBlockEntity blockEntity;
    final Entity       theEntity;

    public BlockEntityInteractHandler(IBlockEntity entity)
    {
        blockEntity = entity;
        theEntity = (Entity) entity;
    }

    public ActionResultType applyPlayerInteraction(PlayerEntity player, Vec3d vec, @Nullable ItemStack stack, Hand hand)
    {
        vec = vec.add(vec.x > 0 ? -0.01 : 0.01, vec.y > 0 ? -0.01 : 0.01, vec.z > 0 ? -0.01 : 0.01);
        Vec3d playerPos = player.getPositionVector().add(0, player.isServerWorld() ? player.getEyeHeight() : 0, 0);
        Vec3d start = playerPos;
        Vec3d end = playerPos.add(player.getLookVec().scale(4.5));
        BlockRayTraceResult trace = null;
        RayTraceResult trace2 = IBlockEntity.BlockEntityFormer.rayTraceInternal(start, end, blockEntity);
        if (trace2 instanceof BlockRayTraceResult)
        {
            trace = (BlockRayTraceResult) trace2;
        }
        BlockPos pos;
        if (trace == null)
        {
            pos = theEntity.getPosition();
        }
        else
        {
            pos = trace.getPos();
        }
        BlockState state = blockEntity.getFakeWorld().getBlockState(pos);
        boolean activate = state.onBlockActivated(blockEntity.getFakeWorld(), player, hand, trace);
        if (activate) return ActionResultType.SUCCESS;
        else if (trace == null || !state.getMaterial().isSolid())
        {
            Vec3d playerLook = playerPos.add(player.getLookVec().scale(4));

            RayTraceContext context = new RayTraceContext(playerPos, playerLook, RayTraceContext.BlockMode.COLLIDER,
                    RayTraceContext.FluidMode.NONE, player);

            RayTraceResult result = theEntity.getEntityWorld().func_217299_a(context);

            if (result instanceof BlockRayTraceResult)
            {
                trace = (BlockRayTraceResult) result;
                pos = trace.getPos();
                state = theEntity.getEntityWorld().getBlockState(pos);
                ItemUseContext context2 = new ItemUseContext(player, hand, trace);
                if (player.isSneaking() && !stack.isEmpty())
                {
                    ActionResultType itemUse = ForgeHooks.onPlaceItemIntoWorld(context2);
                    if (itemUse != ActionResultType.PASS) return itemUse;
                }
                activate = state.onBlockActivated(theEntity.getEntityWorld(), player, hand, trace);
                if (activate) return ActionResultType.SUCCESS;
                else if (!player.isSneaking() && !stack.isEmpty())
                {
                    ActionResultType itemUse = ForgeHooks.onPlaceItemIntoWorld(context2);
                    if (itemUse != ActionResultType.PASS) return itemUse;
                }
            }
            return ActionResultType.PASS;
        }
        return ActionResultType.PASS;
    }

    public boolean processInitialInteract(PlayerEntity player, @Nullable ItemStack stack, Hand hand)
    {
        return false;
    }
}
