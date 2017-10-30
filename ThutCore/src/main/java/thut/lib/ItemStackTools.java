package thut.lib;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ReportedException;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

public class ItemStackTools
{
    public static boolean addItemStackToInventory(ItemStack itemStackIn, IInventory toAddTo, int minIndex)
    {
        return addItemStackToInventory(itemStackIn, new InvWrapper(toAddTo), minIndex);
    }
    /** Adds the item stack to the inventory, returns false if it is
     * impossible. */
    public static boolean addItemStackToInventory(ItemStack itemStackIn, IItemHandlerModifiable toAddTo, int minIndex)
    {
        if (CompatWrapper.isValid(itemStackIn))
        {
            try
            {
                if (itemStackIn.isItemDamaged())
                {
                    int j = getFirstEmptyStack(toAddTo, minIndex);

                    if (j >= 0)
                    {
                        toAddTo.setStackInSlot(j, CompatWrapper.copy(itemStackIn));
                        CompatWrapper.setAnimationToGo(toAddTo.getStackInSlot(j), 5);
                        CompatWrapper.setStackSize(itemStackIn, 0);
                        return true;
                    }
                    return false;
                }
                int i;

                while (true)
                {
                    i = CompatWrapper.getStackSize(itemStackIn);
                    int num = storePartialItemStack(itemStackIn, toAddTo, minIndex);
                    itemStackIn = CompatWrapper.setStackSize(itemStackIn, num);
                    int size = CompatWrapper.getStackSize(itemStackIn);
                    if (size <= 0 || size >= i)
                    {
                        break;
                    }
                }
                return CompatWrapper.getStackSize(itemStackIn) < i;
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Adding item to inventory");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being added");
                crashreportcategory.addCrashSection("Item ID",
                        Integer.valueOf(Item.getIdFromItem(itemStackIn.getItem())));
                crashreportcategory.addCrashSection("Item data", Integer.valueOf(itemStackIn.getMetadata()));
                throw new ReportedException(crashreport);
            }
        }
        return false;
    }

    private static boolean canMergeStacks(ItemStack stack1, ItemStack stack2)
    {
        return CompatWrapper.isValid(stack1) && stackEqualExact(stack1, stack2) && stack1.isStackable()
                && CompatWrapper.getStackSize(stack1) < stack1.getMaxStackSize()
                && CompatWrapper.getStackSize(stack1) < 64;
    }

    /** Checks item, NBT, and meta if the item is not damageable */
    private static boolean stackEqualExact(ItemStack stack1, ItemStack stack2)
    {
        return stack1.getItem() == stack2.getItem()
                && (!stack1.getHasSubtypes() || stack1.getMetadata() == stack2.getMetadata())
                && ItemStack.areItemStackTagsEqual(stack1, stack2);
    }
    public static int getFirstEmptyStack(IInventory inventory, int minIndex)
    {
        return getFirstEmptyStack(new InvWrapper(inventory), minIndex);
    }

    /** Returns the first item stack that is empty. */
    public static int getFirstEmptyStack(IItemHandlerModifiable inventory, int minIndex)
    {
        for (int i = minIndex; i < inventory.getSlots(); ++i)
        {
            if (!CompatWrapper.isValid(inventory.getStackInSlot(i))) { return i; }
        }
        return -1;
    }

    /** stores an itemstack in the users inventory */
    private static int storeItemStack(ItemStack itemStackIn, IItemHandlerModifiable inventory, int minIndex)
    {
        for (int i = minIndex; i < inventory.getSlots(); ++i)
        {
            if (canMergeStacks(inventory.getStackInSlot(i), itemStackIn)) { return i; }
        }
        return -1;
    }

    /** This function stores as many items of an ItemStack as possible in a
     * matching slot and returns the quantity of left over items. */
    private static int storePartialItemStack(ItemStack itemStackIn, IItemHandlerModifiable inventory, int minIndex)
    {
        int i = CompatWrapper.getStackSize(itemStackIn);
        int j = storeItemStack(itemStackIn, inventory, minIndex);

        if (j < 0)
        {
            j = getFirstEmptyStack(inventory, minIndex);
        }

        if (j < 0) { return i; }
        ItemStack itemstack = inventory.getStackInSlot(j);

        if (!CompatWrapper.isValid(itemstack))
        {
            itemstack = itemStackIn.copy();
            CompatWrapper.setStackSize(itemstack, 0);
            if (itemStackIn.hasTagCompound())
            {
                itemstack.setTagCompound((NBTTagCompound) itemStackIn.getTagCompound().copy());
            }
            inventory.setStackInSlot(j, itemstack);
        }

        int k = i;
        int size = CompatWrapper.getStackSize(inventory.getStackInSlot(j));
        if (i > inventory.getStackInSlot(j).getMaxStackSize() - size)
        {
            k = inventory.getStackInSlot(j).getMaxStackSize() - size;
        }

        if (k > inventory.getSlotLimit(j) - size)
        {
            k = inventory.getSlotLimit(j) - size;
        }

        if (k == 0) { return i; }
        i = i - k;
        CompatWrapper.setStackSize(inventory.getStackInSlot(j), size + k);
        CompatWrapper.setAnimationToGo(inventory.getStackInSlot(j), 5);
        return i;
    }
}
