package thut.lootcrates;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class CompatWrapper
{
    public static final ItemStack nullStack = ItemStack.EMPTY;

    public static ItemStack fromTag(NBTTagCompound tag)
    {
        return new ItemStack(tag);
    }

    public static ItemStack copy(ItemStack in)
    {
        return in.copy();
    }

    public static ItemStack setStackSize(ItemStack stack, int amount)
    {
        stack.setCount(amount);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        return stack;
    }

    public static int getStackSize(ItemStack stack)
    {
        return stack.getCount();
    }

    public static boolean isValid(ItemStack stack)
    {
        return !stack.isEmpty();
    }

    public static ItemStack validate(ItemStack in)
    {
        if (!isValid(in)) return nullStack;
        return in;
    }

    public static int increment(ItemStack in, int amt)
    {
        in.grow(amt);
        return in.getCount();
    }

    public static List<ItemStack> makeList(int size)
    {
        List<ItemStack> ret = Lists.newArrayList();
        for (int i = 0; i < size; i++)
            ret.add(nullStack);
        return ret;
    }

}
