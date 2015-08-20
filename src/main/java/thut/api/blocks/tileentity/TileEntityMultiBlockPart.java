package thut.api.blocks.tileentity;

import static thut.api.ThutBlocks.*;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityMultiBlockPart extends TileEntity implements ISidedInventory
{
	TileEntityMultiCore tileEntityCore;
	int coreX = 0;
	int coreY = -1;
	int coreZ = 0;
	
	public Block revertID = brick_block;
	public int type = 0;
	
	public boolean canUpdate()
	{
		return false;
	}
	
	public void setCore(TileEntityMultiCore core)
	{
		coreX = core.xCoord;
		coreY = core.yCoord;
		coreZ = core.zCoord;
		tileEntityCore = core;
	}
	
	public TileEntityMultiCore getCore()
	{
		if(tileEntityCore == null)
			tileEntityCore = (TileEntityMultiCore)worldObj.getTileEntity(coreX, coreY, coreZ);
		
		return tileEntityCore;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		super.readFromNBT(tagCompound);
		
		coreX = tagCompound.getInteger("CoreX");
		coreY = tagCompound.getInteger("CoreY");
		coreZ = tagCompound.getInteger("CoreZ");
		int name = tagCompound.getInteger("revert");
		revertID = Block.getBlockById(name);
		type = tagCompound.getInteger("type");
		if(revertID==null)
			revertID = brick_block;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tagCompound)
	{
		super.writeToNBT(tagCompound);
		tagCompound.setInteger("CoreX", coreX);
		tagCompound.setInteger("CoreY", coreY);
		tagCompound.setInteger("CoreZ", coreZ);
		tagCompound.setInteger("revert", Block.getIdFromBlock(revertID));
		tagCompound.setInteger("type", type);
	}
	
	public void revert()
	{
		worldObj.setBlock(xCoord, yCoord, zCoord, revertID);
	}
	
	@Override
	public void invalidate()
	{
	//	(new Exception()).printStackTrace();
		
		if(tileEntityCore!=null)
			tileEntityCore.invalidateMultiblock();
		super.invalidate();
	}
	
	@Override
	public int getSizeInventory() {
		return tileEntityCore!=null?tileEntityCore.getSizeInventory():0;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return tileEntityCore!=null?tileEntityCore.getStackInSlot(i):null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		return tileEntityCore!=null?tileEntityCore.decrStackSize(i, j):null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		return tileEntityCore!=null?tileEntityCore.getStackInSlotOnClosing(i):null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		if(tileEntityCore!=null)
		tileEntityCore.setInventorySlotContents(i, itemstack);
	}

	@Override
	public int getInventoryStackLimit() {
		return tileEntityCore!=null?tileEntityCore.getInventoryStackLimit():0;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) != this ? false : entityplayer.getDistanceSq((double)xCoord + 0.5, (double)yCoord + 0.5, (double)zCoord + 0.5) <= 64.0;
	}


	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return tileEntityCore.isItemValidForSlot(i, itemstack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		if(tileEntityCore==null)
			getCore();
		if(tileEntityCore!=null)
			return tileEntityCore.getAccessibleSlotsFromSide(var1);
		revert();
		return new int[0];
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j) {
		
		return tileEntityCore.canInsertItem(i, itemstack, j);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
		return tileEntityCore.canExtractItem(i, itemstack, j);
	}
	
    /**
     * Overriden in a sign to provide the text.
     */
	@Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 3, nbttagcompound);
    }
    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
    	NBTTagCompound nbttagcompound = pkt.func_148857_g();
    	this.readFromNBT(nbttagcompound);
    }

	@Override
	public String getInventoryName() {
		return tileEntityCore!=null?tileEntityCore.getInventoryName():null;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return true;
	}

	@Override
	public void openInventory() {}

	@Override
	public void closeInventory() {}
}
