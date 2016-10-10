package thut.bling.bag;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import thut.wearables.ThutWearables;

public class ContainerBag extends ContainerChest
{
    final ItemStack      bag;
    final InventoryBasic inventory;

    public ContainerBag(EntityPlayer player, InventoryBasic bagInventory, final ItemStack bag)
    {
        super(player.inventory, bagInventory, player);
        this.inventory = bagInventory;
        this.bag = bag;
        this.inventorySlots.clear();
        this.inventoryItemStacks.clear();

        int i = (3 - 4) * 18;

        for (int j = 0; j < 3; ++j)
        {
            for (int k = 0; k < 9; ++k)
            {
                this.addSlotToContainer(new Slot(bagInventory, k + j * 9, 8 + k * 18, 18 + j * 18)
                {
                    @Override
                    public boolean isItemValid(@Nullable ItemStack stack)
                    {
                        System.out.println(stack + " " + bag+" "+(stack!=bag));
                        return stack != bag;
                    }
                    
                    @Override 
                    public boolean canTakeStack(EntityPlayer playerIn)
                    {
                        return this.getStack() != bag;
                    }
                });
            }
        }

        for (int l = 0; l < 3; ++l)
        {
            for (int j1 = 0; j1 < 9; ++j1)
            {
                this.addSlotToContainer(new Slot(player.inventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + i)
                {
                    @Override
                    public boolean isItemValid(@Nullable ItemStack stack)
                    {
                        System.out.println(stack + " " + bag+" "+(stack!=bag));
                        return stack != bag;
                    }
                    
                    @Override 
                    public boolean canTakeStack(EntityPlayer playerIn)
                    {
                        return this.getStack() != bag;
                    }
                });
            }
        }

        for (int i1 = 0; i1 < 9; ++i1)
        {
            this.addSlotToContainer(new Slot(player.inventory, i1, 8 + i1 * 18, 161 + i)
            {
                @Override
                public boolean isItemValid(@Nullable ItemStack stack)
                {
                    System.out.println(stack + " " + bag+" "+(stack!=bag));
                    return stack != bag;
                }
                
                @Override 
                public boolean canTakeStack(EntityPlayer playerIn)
                {
                    return this.getStack() != bag;
                }
            });
        }
    }

    public static InventoryBasic init(ItemStack bag)
    {
        InventoryBasic inventory = new InventoryBasic("bling.bag", false, 27);
        if (bag.hasTagCompound())
        {
            NBTTagList nbttaglist = bag.getTagCompound().getTagList("Inventory", 10);
            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
                int j = nbttagcompound1.getByte("Slot") & 255;
                if (j < inventory.getSizeInventory())
                {
                    inventory.setInventorySlotContents(j, ItemStack.loadItemStackFromNBT(nbttagcompound1));
                }
            }
        }
        return inventory;
    }

    private void save(EntityPlayer playerIn)
    {
        if (playerIn.worldObj.isRemote) return;
        if (!bag.hasTagCompound()) bag.setTagCompound(new NBTTagCompound());
        NBTTagCompound inventoryTag = bag.getTagCompound();
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < inventory.getSizeInventory(); ++i)
        {
            ItemStack itemstack = inventory.getStackInSlot(i);
            if (itemstack != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte) i);
                itemstack.writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }
        inventoryTag.setTag("Inventory", nbttaglist);
        ThutWearables.syncWearables(playerIn);
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);
        save(playerIn);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn)
    {
        return true;
    }

}
