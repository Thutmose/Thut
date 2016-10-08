package thut.bling.bag;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import thut.wearables.ThutWearables;

public class ContainerBag extends ContainerChest
{
    final ItemStack      bag;
    final InventoryBasic inventory;

    public ContainerBag(EntityPlayer player, InventoryBasic bagInventory, ItemStack bag)
    {
        super(player.inventory, bagInventory, player);
        this.inventory = bagInventory;
        this.bag = bag;
    }

    public static InventoryBasic init(ItemStack bag)
    {
        InventoryBasic inventory = new InventoryBasic(new TextComponentTranslation("bling.bag"), 27);
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
