package thut.lib;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;

public interface IDefaultRecipe extends IRecipe
{
    @Override
    default NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
    {
        NonNullList<ItemStack> nonnulllist = NonNullList.<ItemStack> withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < nonnulllist.size(); ++i)
        {
            ItemStack itemstack = inv.getStackInSlot(i);
            nonnulllist.set(i, net.minecraftforge.common.ForgeHooks.getContainerItem(itemstack));
        }
        return nonnulllist;
    }

    default ItemStack toKeep(int slot, ItemStack stackIn, InventoryCrafting inv)
    {
        return net.minecraftforge.common.ForgeHooks.getContainerItem(stackIn);
    }

    @Override
    default boolean canFit(int width, int height)
    {
        return width >= 3 && height >= 3;
    }
}
