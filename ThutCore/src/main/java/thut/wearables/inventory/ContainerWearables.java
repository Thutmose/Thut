package thut.wearables.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thut.core.common.handlers.PlayerDataHandler;

public class ContainerWearables extends Container
{
    /** The crafting matrix inventory. */
    public PlayerWearables slots;
    /** Determines if inventory manipulation should be handled. */
    final EntityPlayer     thePlayer;

    public ContainerWearables(EntityPlayer player)
    {
        this.thePlayer = player;
        slots = PlayerDataHandler.getInstance().getPlayerData(player).getData(PlayerWearables.class);
        int xOffset = -26;
        int yOffset = 8;
        int xWidth = 18;
        int yHeight = 18;

        // First row of ear - hat - ear
        this.addSlotToContainer(new Slot(slots, 9, xOffset, yOffset));
        this.addSlotToContainer(new Slot(slots, 12, xOffset + xWidth, yOffset));
        this.addSlotToContainer(new Slot(slots, 10, xOffset + 2 * xWidth, yOffset));

        // Second row of arm - neck - arm
        this.addSlotToContainer(new Slot(slots, 2, xOffset, yOffset + yHeight));
        this.addSlotToContainer(new Slot(slots, 11, xOffset + xWidth, yOffset + yHeight));
        this.addSlotToContainer(new Slot(slots, 3, xOffset + 2 * xWidth, yOffset + yHeight));

        // Third row of finger - back - finger
        this.addSlotToContainer(new Slot(slots, 0, xOffset, yOffset + yHeight * 2));
        this.addSlotToContainer(new Slot(slots, 6, xOffset + xWidth, yOffset + yHeight * 2));
        this.addSlotToContainer(new Slot(slots, 1, xOffset + 2 * xWidth, yOffset + yHeight * 2));

        // Fourth row of ankle - waist - ankle
        this.addSlotToContainer(new Slot(slots, 4, xOffset, yOffset + yHeight * 3));
        this.addSlotToContainer(new Slot(slots, 8, xOffset + xWidth, yOffset + yHeight * 3));
        this.addSlotToContainer(new Slot(slots, 5, xOffset + 2 * xWidth, yOffset + yHeight * 3));

        // Eye slot
        this.addSlotToContainer(new Slot(slots, 7, xOffset + xWidth, yOffset + yHeight * 4));
    }

    /** Callback for when the crafting matrix is changed. */
    @Override
    public void onCraftMatrixChanged(IInventory par1IInventory)
    {
    }

    /** Called when the container is closed. */
    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);
        if (!player.worldObj.isRemote)
        {
            slots.closeInventory(player);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer par1EntityPlayer)
    {
        return true;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player)
    {
        System.out.println("TesT " + slotId);
        if (slotId > 12) return null;
        return super.slotClick(slotId, dragType, clickTypeIn, player);
    }

    /** Called when a player shift-clicks on a slot. You must override this or
     * you will crash when someone does that. */
    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
    {
        Slot slot = this.inventorySlots.get(par2);
        return slot.getStack();
    }

    @Override
    public boolean canMergeSlot(ItemStack par1ItemStack, Slot par2Slot)
    {
        return super.canMergeSlot(par1ItemStack, par2Slot);
    }

    public ItemStack getStackInSlot(int index)
    {
        return slots.getStackInSlot(index);
    }

}
