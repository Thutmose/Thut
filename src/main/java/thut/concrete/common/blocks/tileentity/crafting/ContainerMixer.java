package thut.concrete.common.blocks.tileentity.crafting;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerMixer  extends Container
{
	private TileEntityMixer tileEntity;
	
	public ContainerMixer(InventoryPlayer playerInventory, TileEntityMixer tileEntity)
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
	}
	
	@Override
	public void detectAndSendChanges()
    {
        super.detectAndSendChanges();
    }
	
	@Override
	public void updateProgressBar(int par1, int par2)
	{
		super.updateProgressBar(par1, par2);
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
}
