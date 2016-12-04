package thut.core.common.blocks;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import thut.lib.CompatWrapper;

public interface DefaultInventory extends IInventory
{
    List<ItemStack> getInventory();

    @Override
    default public void clear()
    {
        for (int i = 0; i < getInventory().size(); i++)
            getInventory().set(i, CompatWrapper.nullStack);
    }

    @Override
    default public int getSizeInventory()
    {
        return getInventory().size();
    }

    @Override
    default public ItemStack getStackInSlot(int index)
    {
        return getInventory().get(index);
    }

    @Override
    default public ItemStack decrStackSize(int slot, int count)
    {
        if (CompatWrapper.isValid(getInventory().get(slot)))
        {
            ItemStack itemStack;
            itemStack = getInventory().get(slot).splitStack(count);
            if (!CompatWrapper.isValid(getInventory().get(slot)))
            {
                getInventory().set(slot, CompatWrapper.nullStack);
            }
            return itemStack;
        }
        return CompatWrapper.nullStack;
    }

    @Override
    default public ItemStack removeStackFromSlot(int slot)
    {
        if (CompatWrapper.isValid(getInventory().get(slot)))
        {
            ItemStack stack = getInventory().get(slot);
            getInventory().set(slot, CompatWrapper.nullStack);
            return stack;
        }
        return CompatWrapper.nullStack;
    }

    @Override
    default public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (CompatWrapper.isValid(stack)) getInventory().set(index, CompatWrapper.nullStack);
        getInventory().set(index, stack);
    }

    @Override
    default public boolean hasCustomName()
    {
        return false;
    }

    @Override
    default public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    default public boolean isUseableByPlayer(EntityPlayer player)
    {
        return true;
    }

    @Override
    default public void openInventory(EntityPlayer player)
    {

    }

    @Override
    default public void closeInventory(EntityPlayer player)
    {

    }

    // 1.11
    default public boolean func_191420_l()
    {
        return true;
    }

}
