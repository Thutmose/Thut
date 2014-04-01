package thut.concrete.common.blocks.tileentity.crafting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;

public class ContainerLimekiln extends Container
{
	private TileEntityKiln tileEntity;
	private int lastCookTime = 0;
	private int lastBurnTime = 0;
	private int lastItemBurnTime = 0;
	
	public ContainerLimekiln(InventoryPlayer playerInventory, TileEntityKiln tileEntity)
	{
		this.tileEntity = tileEntity;
		
		//Input
		for(int x = 0; x < 8; x++)
		{
			addSlotToContainer(new Slot(tileEntity, x, 6+(20*x), 17));
		}
		
		//Fuel
		addSlotToContainer(new Slot(tileEntity, 8, 146, 39));
		
		//Output
		for(int x = 0; x < 8; x++)
		{
			addSlotToContainer(new Slot(tileEntity, x+9, 6+(20*x), 63));
		}
		
		bindPlayerInventory(playerInventory);
		
	}
	
	@Override
	public void addCraftingToCrafters(ICrafting par1ICrafting)
	{
		super.addCraftingToCrafters(par1ICrafting);
        par1ICrafting.sendProgressBarUpdate(this, 0, this.tileEntity.furnaceCookTime);
        par1ICrafting.sendProgressBarUpdate(this, 1, this.tileEntity.furnaceBurnTime);
        par1ICrafting.sendProgressBarUpdate(this, 2, this.tileEntity.currentItemBurnTime);
	}
	
	@Override
	public void detectAndSendChanges()
    {
        super.detectAndSendChanges();

        for (int i = 0; i < this.crafters.size(); ++i)
        {
            ICrafting icrafting = (ICrafting)this.crafters.get(i);

            if (this.lastCookTime != this.tileEntity.furnaceCookTime)
                icrafting.sendProgressBarUpdate(this, 0, this.tileEntity.furnaceCookTime);

            if (this.lastBurnTime != this.tileEntity.furnaceBurnTime)
                icrafting.sendProgressBarUpdate(this, 1, this.tileEntity.furnaceBurnTime);

            if (this.lastItemBurnTime != this.tileEntity.currentItemBurnTime)
                icrafting.sendProgressBarUpdate(this, 2, this.tileEntity.currentItemBurnTime);
        }

        this.lastCookTime = this.tileEntity.furnaceCookTime;
        this.lastBurnTime = this.tileEntity.furnaceBurnTime;
        this.lastItemBurnTime = this.tileEntity.currentItemBurnTime;
    }
	
	@Override
	public void updateProgressBar(int par1, int par2)
	{
		if (par1 == 0)
        {
            this.tileEntity.furnaceCookTime = par2;
        }

        if (par1 == 1)
        {
            this.tileEntity.furnaceBurnTime = par2;
        }

        if (par1 == 2)
        {
            this.tileEntity.currentItemBurnTime = par2;
        }
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer entityPlayer)
	{
		return tileEntity.isUseableByPlayer(entityPlayer);
	}
	
	private void bindPlayerInventory(InventoryPlayer playerInventory)
	{
		// Inventory
		for(int y = 0; y < 3; y++)
			for(int x = 0; x < 9; x++)
				addSlotToContainer(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
		
		// Action Bar
		for(int x = 0; x < 9; x++)
			addSlotToContainer(new Slot(playerInventory, x, 8 + x * 18, 142));
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		ItemStack stack = null;
		Slot slotObject = (Slot)inventorySlots.get(slot);
		
		
		
		if(slotObject != null && slotObject.getHasStack())
		{
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();
			
			// Merges the item into the player inventory
			if(slot < 9)
			{
				if(!this.mergeItemStack(stackInSlot, 3, 39, true))
					return null;
			}
			else if(!this.mergeItemStack(stackInSlot, 0, 9, false))
				return null;
			
			if(stackInSlot.stackSize == 0)
				slotObject.putStack(null);
			else
				slotObject.onSlotChanged();
			
			if(stackInSlot.stackSize == stack.stackSize)
				return null;
			
			slotObject.onPickupFromSlot(player, stackInSlot);
		}
		
		return stack;
	}
	
    public boolean func_94530_a(ItemStack par1ItemStack, Slot par2Slot)
    {
    	if(par1ItemStack==null) return true;
        return tileEntity.isItemValidForSlot(par2Slot.slotNumber, par1ItemStack);
    }
}
