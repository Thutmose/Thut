package thut.tech.common.entity;

import java.util.logging.Level;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import thut.api.entity.blockentity.BlockEntityInteractHandler;
import thut.tech.common.TechCore;
import thut.tech.common.items.ItemLinker;

public class LiftInteractHandler extends BlockEntityInteractHandler
{
    public static boolean DROPSPARTS = true;

    final EntityLift      lift;

    public LiftInteractHandler(EntityLift lift)
    {
        super(lift);
        this.lift = lift;
    }

    public boolean processInitialInteract(PlayerEntity player, @Nullable ItemStack stack, Hand hand)
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
        if (lift.owner == null)
        {
            TechCore.logger.log(Level.SEVERE, "Killing unowned Lift: " + lift);
            if (!lift.getEntityWorld().isRemote)
            {
                String message = "msg.lift.killed";
                player.sendMessage(new TranslationTextComponent(message));
                if (DROPSPARTS)
                {
                    BlockPos max = lift.boundMax;
                    BlockPos min = lift.boundMin;
                    int dw = Math.max(max.getX() - min.getX(), max.getZ() - min.getZ());
                    int num = (dw + 1) * (max.getY() - min.getY() + 1);
                    stack = ItemLinker.liftblocks.copy();
                    stack.setCount(num);
                    player.dropItem(stack, false, true);
                }
                lift.setHealth(0);
                lift.setDead();
            }
            return true;
        }

        if (player.isSneaking() && stack != null && stack.getItem() instanceof ItemLinker
                && ((lift.owner != null && player.getUniqueID().equals(lift.owner))
                        || player.capabilities.isCreativeMode))
        {
            if (stack.getTag() == null)
            {
                stack.setTag(new CompoundNBT());
            }
            stack.getTag().putString("lift", lift.getCachedUniqueIdString());

            String message = "msg.liftSet.name";

            if (!lift.getEntityWorld().isRemote) player.sendMessage(new TranslationTextComponent(message));
            return true;
        }
        else if (stack != null && stack.getItem() instanceof ItemLinker
                && ((lift.owner != null && player.getUniqueID().equals(lift.owner))
                        || player.capabilities.isCreativeMode))
        {
            if (!lift.getEntityWorld().isRemote && lift.owner != null)
            {
                Entity ownerentity = lift.getEntityWorld().getPlayerEntityByUUID(lift.owner);
                String message = "msg.lift.owner";

                player.sendMessage(new TranslationTextComponent(message, ownerentity.getName()));
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
            if (!lift.getEntityWorld().isRemote)
            {
                String message = "msg.lift.killed";
                player.sendMessage(new TranslationTextComponent(message));
                if (DROPSPARTS)
                {
                    BlockPos max = lift.boundMax;
                    BlockPos min = lift.boundMin;
                    int dw = Math.max(max.getX() - min.getX(), max.getZ() - min.getZ());
                    int num = (dw + 1) * (max.getY() - min.getY() + 1);
                    stack = ItemLinker.liftblocks.copy();
                    stack.setCount(num);
                    player.dropItem(stack, false, true);
                }
                lift.setHealth(0);
                lift.setDead();
            }
            return true;
        }
        return false;
    }
}
