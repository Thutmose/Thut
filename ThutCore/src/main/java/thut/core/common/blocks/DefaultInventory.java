package thut.core.common.blocks;

import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import thut.lib.CompatWrapper;

public interface DefaultInventory extends IInventory
{
    List<ItemStack> getInventory();

    @Override
    default public void clear()
    {
        for (int i = 0; i < getSizeInventory(); i++)
            setInventorySlotContents(i, ItemStack.EMPTY);
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
        if (CompatWrapper.isValid(getStackInSlot(slot)))
        {
            ItemStack itemStack = getStackInSlot(slot).splitStack(count);
            setInventorySlotContents(slot, getStackInSlot(slot));
            return itemStack;
        }
        setInventorySlotContents(slot, ItemStack.EMPTY);
        return ItemStack.EMPTY;
    }

    @Override
    default public ItemStack removeStackFromSlot(int slot)
    {
        if (CompatWrapper.isValid(getStackInSlot(slot)))
        {
            ItemStack stack = getStackInSlot(slot);
            setInventorySlotContents(slot, ItemStack.EMPTY);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    default public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (!CompatWrapper.isValid(stack)) getInventory().set(index, ItemStack.EMPTY);
        else getInventory().set(index, stack);
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
    default public boolean isUsableByPlayer(PlayerEntity player)
    {
        return true;
    }

    @Override
    default public void openInventory(PlayerEntity player)
    {

    }

    @Override
    default public void closeInventory(PlayerEntity player)
    {

    }

    // 1.11
    @Override
    default public boolean isEmpty()
    {
        return true;
    }

}
