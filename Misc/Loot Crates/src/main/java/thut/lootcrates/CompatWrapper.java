package thut.lootcrates;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;

public class CompatWrapper
{
    public static final ItemStack nullStack = null;

    public static ItemStack fromTag(NBTTagCompound tag)
    {
        return ItemStack.loadItemStackFromNBT(tag);
    }

    public static ItemStack copy(ItemStack in)
    {
        return ItemStack.copyItemStack(in);
    }

    public static ItemStack setStackSize(ItemStack stack, int amount)
    {
        if (amount <= 0) { return nullStack; }
        stack.stackSize = amount;
        return stack;
    }

    public static int getStackSize(ItemStack stack)
    {
        if (stack == nullStack || stack.stackSize < 0 || stack.getItem() == null) { return 0; }
        return stack.stackSize;
    }

    public static boolean isValid(ItemStack stack)
    {
        return getStackSize(stack) > 0;
    }

    public static ItemStack validate(ItemStack in)
    {
        if (!isValid(in)) return nullStack;
        return in;
    }

    public static int increment(ItemStack in, int amt)
    {
        in.stackSize += amt;
        return in.stackSize;
    }

    public static List<ItemStack> makeList(int size)
    {
        List<ItemStack> ret = Lists.newArrayList();
        for (int i = 0; i < size; i++)
            ret.add(nullStack);
        return ret;
    }

    public static void rightClickWith(ItemStack stack, EntityPlayer player, EnumHand hand)
    {
        stack.getItem().onItemRightClick(stack, player.worldObj, player, hand);
    }
    
    public static NBTTagCompound getTag(ItemStack stack, String name, boolean create)
    {
        return stack.getSubCompound(name, create);
    }

}
