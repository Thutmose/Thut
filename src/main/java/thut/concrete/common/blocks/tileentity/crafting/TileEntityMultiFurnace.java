package thut.concrete.common.blocks.tileentity.crafting;

import static thut.api.ThutBlocks.*;
import thut.api.ThutItems;
import thut.api.maths.Vector3;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityMultiFurnace extends TileEntity implements ISidedInventory
{
	private static final int[] sidedSlotSides = new int[] {8};
	private static final int[] sidedSlotBottom = new int[] {8,9,10,11,12,13,14,15,16};
	private static final int[] sidedSlotTop = new int[] {0,1,2,3,4,5,6,7};
	
	public ForgeDirection facing = ForgeDirection.UP;
	public boolean cooking = false;
	Vector3 hole = new Vector3();
	
	public int COOKTIME = 100;
	public int type = 0;
	
	public Block partBlocks = brick_block;
//	public int[] validBlocks = {Block.brick.blockID};
	
	private ItemStack[] furnaceItems = new ItemStack[17];
	public int furnaceBurnTime = 0;
	public int currentItemBurnTime = 0;
	public int furnaceCookTime = 0;
	
	boolean first = false;
	private boolean isValidMultiblock = false;
	 
	public boolean getIsValid()
	{
	    return isValidMultiblock;
	}
	 
	public void invalidateMultiblock()
	{
	    isValidMultiblock = false;
	    
//	    furnaceBurnTime = 0;
//	    currentItemBurnTime = 0;
//	    furnaceCookTime = 0;
	     
	    worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 2);
	  //  if(!worldObj.isRemote)
	    revertDummies();
	}
	
	public boolean checkIfProperlyFormed()
	{
		boolean beak = false;
		{
			int n=0;
			boolean axis = (facing.equals(ForgeDirection.EAST)||facing.equals(ForgeDirection.WEST));
			int dir = (facing.equals(ForgeDirection.WEST)||facing.equals(ForgeDirection.NORTH))?-1:1;
			
	
			for(int k = -1; k<5;k++)
			{
				for(int i=0;i<7;i++)
				{
					for(int j=-3;j<4;j++)
					{
						int r = Vector3.Int((Math.sqrt(j*j+(i-3)*(i-3)+(k+1)*(k+1))));
	
						if(r!=3&&!(k==-1&&r<=3))
							continue;
						if(j==0&&k==0&&(i==0||i==1))
							continue;
						if(k==2&&i==3&&j==0)
						{
							hole = new Vector3(xCoord + (axis?i*dir:j), zCoord + (axis?j:i*dir), yCoord + k);
							continue;
						}
						
						int x = xCoord + (axis?i*dir:j);
						int z = zCoord + (axis?j:i*dir);
						int y = yCoord + k;
						
						if(x == xCoord&&y==yCoord&&z==zCoord)
							continue;
						Block id = worldObj.getBlock(x,y,z);
						if(!(id==partBlocks||id==limekiln))
						{
							type = 1;
							partBlocks = stonebrick;
							beak = true;
							break;
						}
						n++;
					}
					if(beak)
						break;
				}
				if(beak)
					break;
			}
		}
		
		if(beak)
		{
			int n=0;
			boolean axis = (facing.equals(ForgeDirection.EAST)||facing.equals(ForgeDirection.WEST));
			int dir = (facing.equals(ForgeDirection.WEST)||facing.equals(ForgeDirection.NORTH))?-1:1;
	
			for(int k = -1; k<3;k++)
				for(int i=0;i<7;i++)
					for(int j=-3;j<4;j++)
					{
						int r = Vector3.Int((Math.sqrt(j*j+(i-3)*(i-3)+(k+1)*(k+1))));
	
						if(r!=3&&!(k==-1&&r<=3))
							continue;
						if(j==0&&k==0&&(i==0||i==1))
							continue;
						if(k==2&&i==3&&j==0)
						{
							hole = new Vector3(xCoord + (axis?i*dir:j), zCoord + (axis?j:i*dir), yCoord + k);
							continue;
						}
						
						int x = xCoord + (axis?i*dir:j);
						int z = zCoord + (axis?j:i*dir);
						int y = yCoord + k;
						
						if(x == xCoord&&y==yCoord&&z==zCoord)
							continue;
						Block id = worldObj.getBlock(x,y,z);
						if(!(id==partBlocks||id==limekiln))
						{
							type = 1;
							partBlocks = stonebrick;
							return false;
						}
						n++;
					}
		}
		
		return checkInside();
	}

	public void convertDummies()
	{

		int n=0;
		boolean axis = (facing.equals(ForgeDirection.EAST)||facing.equals(ForgeDirection.WEST));
		int dir = (facing.equals(ForgeDirection.WEST)||facing.equals(ForgeDirection.NORTH))?-1:1;

		for(int k = -1; k<3;k++)
			for(int i=0;i<7;i++)
				for(int j=-3;j<4;j++)
				{
					int r = Vector3.Int((Math.sqrt(j*j+(i-3)*(i-3)+(k+1)*(k+1))));

					if(r!=3&&!(k==-1&&r<=3))
						continue;
					if(j==0&&k==0&&(i==0||i==1))
						continue;
					if(k==2&&i==3&&j==0)
						continue;
					
					int x = xCoord + (axis?i*dir:j);
					int z = zCoord + (axis?j:i*dir);
					int y = yCoord + k;
					
					if(x == xCoord&&y==yCoord&&z==zCoord)
						continue;
					
					worldObj.setBlock(x, y, z, limekiln, 2+type, 3);
					TileEntityMultiBlockPart dummyTE;
					TileEntity te = worldObj.getTileEntity(x, y, z);
					if(te instanceof TileEntityMultiBlockPart)
						dummyTE = (TileEntityMultiBlockPart)te;
					else
					{
						if(te!=null)
						{
							te.invalidate();
							worldObj.removeTileEntity(x, y, z);
						}
						dummyTE = new TileEntityMultiBlockPart();
						worldObj.setTileEntity(x, y, z, dummyTE);
					}
					
					dummyTE.type = type;
					dummyTE.revertID = partBlocks;
					dummyTE.setCore(this);
					worldObj.markBlockForUpdate(x, y, z);
				}
		isValidMultiblock = true;
	}
	
	private void revertDummies()
	{
		int n=0;
		boolean axis = (facing.equals(ForgeDirection.EAST)||facing.equals(ForgeDirection.WEST));
		int dir = (facing.equals(ForgeDirection.WEST)||facing.equals(ForgeDirection.NORTH))?-1:1;
		boolean derp = false;
		for(int k = -1; k<3;k++)
			for(int i=0;i<7;i++)
				for(int j=-3;j<4;j++)
				{
					int r = Vector3.Int((Math.sqrt(j*j+(i-3)*(i-3)+(k+1)*(k+1))));

					if(r!=3&&!(k==-1&&r<=3))
						continue;
					if(j==0&&k==0&&(i==0||i==1))
						continue;
					if(k==2&&i==3&&j==0)
						continue;
					
					int x = xCoord + (axis?i*dir:j);
					int z = zCoord + (axis?j:i*dir);
					int y = yCoord + k;
					
					if(x == xCoord&&y==yCoord&&z==zCoord)
						continue;
					
					Block id = worldObj.getBlock(x,y,z);
					if(id != limekiln)//BlockLimekilnDummy.instance.blockID)
						continue;
					
					worldObj.setBlock(x, y, z, derp?brick_block:partBlocks);
					worldObj.markBlockForUpdate(x, y, z);
				}
		
		isValidMultiblock = false;
	}
	
	boolean checkInside()
	{
		
		boolean axis = (facing.equals(ForgeDirection.EAST)||facing.equals(ForgeDirection.WEST));
		int dir = (facing.equals(ForgeDirection.WEST)||facing.equals(ForgeDirection.NORTH))?-1:1;

		for(int k = 0; k<4;k++)
			for(int i=0;i<7;i++)
				for(int j=-3;j<4;j++)
				{
					int r = Vector3.Int((Math.sqrt(j*j+(i-3)*(i-3)+(k+1)*(k+1))));
					if(r>=3&&!(k==2&&i==3&&j==0))
						continue;
					if(j==0&&k==0&&i==0)
						continue;
					
					int x = xCoord + (axis?i*dir:j);
					int z = zCoord + (axis?j:i*dir);
					int y = yCoord + k;
					
					if(x == xCoord&&y==yCoord&&z==zCoord)
						continue;
					
					Vector3 loc = new Vector3(x,y,z);
					Block id = loc.getBlock(worldObj);
					if(!(loc.isAir(worldObj)||id==fire||id==warmCO2||id==coolCO2))
						return false;
				}
		
		
		
		return true;
	}
	
	public void setInside()
	{
		if(worldObj.isRemote)
			return;
		
		boolean axis = (facing.equals(ForgeDirection.EAST)||facing.equals(ForgeDirection.WEST));
		int dir = (facing.equals(ForgeDirection.WEST)||facing.equals(ForgeDirection.NORTH))?-1:1;
	//	hole.setBlock(worldObj, Blocks.warmCO2.blockID, 7);
		for(int k = 0; k<4;k++)
			for(int i=0;i<7;i++)
				for(int j=-3;j<4;j++)
				{
					int r = Vector3.Int((Math.sqrt(j*j+(i-3)*(i-3)+(k+1)*(k+1))));
					if(r>=3)
						continue;
					if(j==0&&k==0&&i==0)
						continue;
					
					int x = xCoord + (axis?i*dir:j);
					int z = zCoord + (axis?j:i*dir);
					int y = yCoord + k;
					
					if(x == xCoord&&y==yCoord&&z==zCoord)
						continue;
					
					Vector3 loc = new Vector3(x,y,z);
					Block id = loc.getBlock(worldObj);
					if(!(id==fire||id==warmCO2)&&loc.isAir(worldObj))
						if(k==0)
							loc.setBlock(worldObj, fire);
						else
							loc.setBlock(worldObj, warmCO2, 7);
				}
	}
	
	@Override
	public void updateEntity()
	{
		if(first&&isValidMultiblock)
		{
			first = false;
			convertDummies();
		}
		if(!isValidMultiblock)
			return;
		boolean flag = furnaceBurnTime > 0;
		boolean flag1 = false;
		
		int metadata = getBlockMetadata();
		int isActive = (metadata >> 3);
		
		if(furnaceBurnTime > 0)
			furnaceBurnTime--;
		if(furnaceBurnTime==0&&metadata!=0)
		{
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 3);
		}
		
		if(!this.worldObj.isRemote)
		{
			boolean canSmelt = false;
			
			for(int i = 0; i<8; i++)
			{
				canSmelt = canSmelt || canSmelt(i);
			}
			if(furnaceBurnTime == 0 && canSmelt)
			{
				currentItemBurnTime = furnaceBurnTime = TileEntityFurnace.getItemBurnTime(furnaceItems[8]);

				if(furnaceBurnTime > 0)
				{
					flag1 = true;
					
					if(furnaceItems[8] != null)
					{
						furnaceItems[8].stackSize--;
						
						if(furnaceItems[8].stackSize == 0)
							furnaceItems[8] = furnaceItems[8].getItem().getContainerItem(furnaceItems[8]);
					}
				}
			}
			canSmelt = false;
			for(int i = 0; i<8; i++)
			{
				canSmelt = canSmelt || canSmelt(i);
			}
			if(isBurning() && canSmelt)
			{
				furnaceCookTime++;
				
				if(furnaceCookTime == getCookTime())
				{
					furnaceCookTime = 0;
					for(int i = 0; i<8; i++)
						smeltItem(i);
					flag1 = true;
				}
			}
			else
			{
				furnaceCookTime = 0;
			}
			
			
			if(isActive == 0 && furnaceBurnTime > 0)
			{
				flag1 = true;
				isActive = 1;
				metadata = 1;
				setInside();
				if(metadata!=1)
				worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, metadata, 3);
			}
		}
		
		if(flag1)
			closeInventory();
	}
	
	@Override
	public int getSizeInventory()
	{
		return furnaceItems.length;
	}
	
	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return furnaceItems[slot];
	}
	
	@Override
	public ItemStack decrStackSize(int slot, int count)
	{		
		if(this.furnaceItems[slot] != null)
		{
			ItemStack itemStack;
			
			itemStack = furnaceItems[slot].splitStack(count);
				
			if(furnaceItems[slot].stackSize <= 0)
				furnaceItems[slot] = null;
				
			return itemStack;
		}
		
		return null;
	}
	
	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		if(furnaceItems[slot] != null)
		{
			ItemStack stack = furnaceItems[slot];
			furnaceItems[slot] = null;
			return stack;
		}
		
		return null;
	}
	
	@Override
	public void setInventorySlotContents(int slot, ItemStack itemStack)
	{
		furnaceItems[slot] = itemStack;
		
		if(itemStack != null && itemStack.stackSize > getInventoryStackLimit())
			itemStack.stackSize = getInventoryStackLimit();
	}
	
	public int getCookTime()
	{
		return type==0?COOKTIME:3*COOKTIME;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityPlayer)
	{
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) != this ? false : entityPlayer.getDistanceSq((double)xCoord + 0.5, (double)yCoord + 0.5, (double)zCoord + 0.5) <= 64.0;
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		super.readFromNBT(tagCompound);

		facing = ForgeDirection.getOrientation(tagCompound.getInteger("direction"));
		
		isValidMultiblock = tagCompound.getBoolean("isValidMultiblock");
		
		NBTTagList itemsTag = (NBTTagList) tagCompound.getTag("Items");
		furnaceItems = new ItemStack[getSizeInventory()];
		
		for(int i = 0; i < itemsTag.tagCount(); i++)
		{
			NBTTagCompound slotTag = (NBTTagCompound)itemsTag.getCompoundTagAt(i);
			byte slot = slotTag.getByte("Slot");
			
			if(slot >= 0 && slot < furnaceItems.length)
				furnaceItems[slot] = ItemStack.loadItemStackFromNBT(slotTag);
		}
		
		furnaceBurnTime = tagCompound.getShort("BurnTime");
		furnaceCookTime = tagCompound.getShort("CookTime");
		currentItemBurnTime = TileEntityFurnace.getItemBurnTime(furnaceItems[1]);
		cooking = tagCompound.getBoolean("active");
		type = tagCompound.getInteger("type");
		if(type==1)
			partBlocks = stonebrick;
		hole = hole.readFromNBT(tagCompound, "hole");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tagCompound)
	{
		super.writeToNBT(tagCompound);
		
		tagCompound.setBoolean("isValidMultiblock", isValidMultiblock);
		tagCompound.setInteger("type", type);
		tagCompound.setShort("BurnTime", (short)furnaceBurnTime);
		tagCompound.setShort("CookTime", (short)furnaceCookTime);
		tagCompound.setBoolean("active", cooking);
		tagCompound.setInteger("direction", facing.ordinal());
		NBTTagList itemsList = new NBTTagList();
		hole.writeToNBT(tagCompound, "hole");
		for(int i = 0; i < furnaceItems.length; i++)
		{
			if(furnaceItems[i] != null)
			{
				NBTTagCompound slotTag = new NBTTagCompound();
				slotTag.setByte("Slot", (byte)i);
				furnaceItems[i].writeToNBT(slotTag);
				itemsList.appendTag(slotTag);
			}
			
			tagCompound.setTag("Items", itemsList);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public int getCookProgressScaled(int scaleVal)
	{
		return furnaceCookTime * scaleVal / getCookTime();
	}
	
	@SideOnly(Side.CLIENT)
	public int getBurnTimeRemainingScaled(int scaleVal)
	{
		if(currentItemBurnTime == 0)
			currentItemBurnTime = getCookTime();
		
		return furnaceBurnTime * scaleVal / currentItemBurnTime;
	}
	
	public boolean isBurning()
	{
		return furnaceBurnTime > 0;
	}
	
	public boolean cookable(ItemStack stack)
	{
		if(stack==null)
		{
			return false;
		}
		
		if(type==0)
		for(ItemStack cook:ThutItems.cookable)
		{
		//	System.out.println(cook.itemID + " " + stack.itemID);
			if(cook.isItemEqual(stack))
			{
				return true;
			}
		}
		if(type==1)
			if(stack.getItem()==Item.getItemFromBlock(stone))
				return true;
		
		return false;
	}
	
	private boolean canSmelt(int stack)
	{
		boolean hasItem = false;
		if(furnaceItems[stack] == null||!cookable(furnaceItems[stack]))
		{
		//	System.out.println("not cookable"+stack+" "+cookable(furnaceItems[stack])+" "+furnaceItems[stack]);
			return false;
		}
		else
		{
			ItemStack itemStack = null;
			if(type==0)
				itemStack = ThutItems.lime.copy();
			if(type==1)
				itemStack = ThutItems.dust.copy();
			
			if(itemStack == null)
				return false;
			
			if(furnaceItems[stack+9] == null)
				return true;
			if(!furnaceItems[stack+9].isItemEqual(itemStack))
				return false;
			
			int resultingStackSize = furnaceItems[stack+9].stackSize + itemStack.stackSize;
			return (resultingStackSize <= getInventoryStackLimit() && resultingStackSize <= itemStack.getMaxStackSize());
		}
	}
	
	public void smeltItem(int stack)
	{
		if(canSmelt(stack))
		{
			ItemStack itemStack = null;
			if(type==0)
				itemStack = ThutItems.lime.copy();
			if(type==1)
				itemStack = ThutItems.dust.copy();
			if(itemStack == null)
				return;
			
			if(furnaceItems[stack+9] == null)
				furnaceItems[stack+9] = itemStack.copy();
			else if(furnaceItems[stack+9].isItemEqual(itemStack))
				furnaceItems[stack+9].stackSize += itemStack.stackSize;
			
			furnaceItems[stack].stackSize--;
			if(furnaceItems[stack].stackSize <= 0)
				furnaceItems[stack] = null;
		}
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		return var1 == 0 ? sidedSlotBottom : (var1 == 1 ? sidedSlotTop : sidedSlotSides);
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j) {
		return this.isItemValidForSlot(i, itemstack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
		return j!=0 || i != 8 || itemstack.getItem() == Items.bucket;
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
	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
		return slot > 8 && !TileEntityFurnace.isItemFuel(itemstack)? false : (slot == 8 ? TileEntityFurnace.isItemFuel(itemstack) : true);
	}

	@Override
	public String getInventoryName() {
		if(type==0)
			return "thutconcrete.container.limekiln";
		return "thutconcrete.container.dustkiln";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return true;
	}

	@Override
	public void openInventory() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeInventory() {
		// TODO Auto-generated method stub
		
	}

}
