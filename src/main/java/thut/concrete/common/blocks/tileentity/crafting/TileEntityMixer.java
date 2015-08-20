package thut.concrete.common.blocks.tileentity.crafting;

import static net.minecraft.init.Blocks.stonebrick;
import thut.api.ThutBlocks;
import thut.api.ThutItems;
import thut.api.blocks.tileentity.TileEntityMultiBlockPartFluids;
import thut.api.blocks.tileentity.TileEntityMultiCoreFluids;
import thut.concrete.common.handlers.RecipeHandler;
import thut.concrete.common.handlers.RecipeHandler.MixerRecipe;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityMixer extends TileEntityMultiCoreFluids{

	private static final int[] liquidSlot = new int[] {8};
	private static final int[] gravelSlots = new int[] {8,9,10,11,12,13,14,15};
	private static final int[] sandSlots = new int[] {0,1,2,3,4,5,6};
	private static final int[] cementSlots = new int[] {7, 16};
	private static final int[] allSides = new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
	private static final int[] itemSlots = new int[] {0,1,2,3,4,5,6,7,9,10,11,12,13,14,15,16};
	private ItemStack[] furnaceItems = new ItemStack[17];
	
	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		return allSides;//var1 == 0 ? sidedSlotBottom : (var1 == 1 ? sidedSlotTop : sidedSlotSides);
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j) {
		return this.isItemValidForSlot(i, itemstack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
		return itemstack.getItem() == Items.bucket;
	}

	@Override
	public int getSizeInventory() {
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

	@Override
	public String getInventoryName() {
		return "thutconcrete.container.mixer";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return true;
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
	public void openInventory() {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeInventory() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		if(var1!=8)
		{
			System.out.println(var1);
			for(ItemStack s: RecipeHandler.validMixerInputs)
			{
				if(s.isItemEqual(var2))
					return true;
			}
		}
		
		if(var1 == 8)
		{
			return var2.getItem() instanceof IFluidContainerItem || var2.getItem() == Items.water_bucket; //TODO check if it is a water container instead
		}
		return false;//Block.getBlockFromItem(var2.getItem()) == Blocks.gravel;
	}

	@Override
	public boolean checkIfProperlyFormed() {
		for(int i = -1; i<=1; i++)
			for(int j = -1; j<=1; j++)
				for(int k = -2; k<1; k++)
				{
					if(i==j && i == k && i == 0) continue;
					int x = i + xCoord;
					int z = j + zCoord;
					int y = k + yCoord;
					
				//	worldObj.setBlock(x, y, z, Blocks.stonebrick);
					
					Block b = worldObj.getBlock(x, y, z);
					if(b!= Blocks.stonebrick)
					{
						return false;
					}
				}
		return true;
	}

	@Override
	public void convertDummies() {
		for(int i = -1; i<=1; i++)
			for(int j = -1; j<=1; j++)
				for(int k = -2; k<1; k++)
				{
					if(i==j && i == k  && i == 0) continue;
					int x = i + xCoord;
					int z = j + zCoord;
					int y = k + yCoord;
					worldObj.setBlock(x, y, z, Blocks.stonebrick);
					TileEntityMultiBlockPartFluids dummyTE;
					TileEntity te = worldObj.getTileEntity(x, y, z);
					if(te instanceof TileEntityMultiBlockPartFluids)
						dummyTE = (TileEntityMultiBlockPartFluids)te;
					else
					{
						if(te!=null)
						{
							te.invalidate();
							worldObj.removeTileEntity(x, y, z);
						}
						dummyTE = new TileEntityMultiBlockPartFluids();
						dummyTE.type = 1;
						dummyTE.revertID = Blocks.stonebrick;
						worldObj.setTileEntity(x, y, z, dummyTE);
					}
					dummyTE.type = 1;
					dummyTE.revertID = Blocks.stonebrick;
					dummyTE.setCore(this);
				}
		this.isValidMultiblock = true;
	}

	@Override
	protected void revertDummies() {
		for(int i = -1; i<=1; i++)
			for(int j = -1; j<=1; j++)
				for(int k = -2; k<1; k++)
				{
					if(i==j && i == k  && i == 0) continue;
					int x = i + xCoord;
					int z = j + zCoord;
					int y = k + yCoord;
					Block b = worldObj.getBlock(x, y, z);
					TileEntity te = worldObj.getTileEntity(x, y, z);
					if(b == ThutBlocks.mixer)
						worldObj.setBlock(x, y, z, Blocks.stonebrick);
				}
		this.isValidMultiblock = false;
	}

	public int[] tankCapacities()
	{
		return new int[] {tankWater.getFluidAmount(), tankConcrete.getFluidAmount()};
	}
	
	protected FluidTank tankWater = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME*64);
	protected FluidTank tankConcrete = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME*64);
    /* IFluidHandler */
    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        return from==ForgeDirection.UP?tankWater.fill(resource, doFill):0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
    {
    	
        if (resource == null || !resource.isFluidEqual(tankWater.getFluid()))
        {
            return null;
        }
        return tankWater.drain(resource.amount, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
        return from!=ForgeDirection.UP?tankConcrete.drain(maxDrain, doDrain):tankWater.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid)
    {
    	boolean valid = false;
    	for(FluidStack f: RecipeHandler.validInputFluids)
    	{
    		valid = f.getFluid().getName().equalsIgnoreCase(fluid.getName());
    		if(valid) break;
    	}
    	
        return from==ForgeDirection.UP && valid;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)
    {
        return from==ForgeDirection.UP? new FluidTankInfo[] { tankWater.getInfo() }:new FluidTankInfo[] { tankConcrete.getInfo() };
    }

	@Override
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		super.readFromNBT(tagCompound);

		isValidMultiblock = tagCompound.getBoolean("isValidMultiblock");
		
		NBTTagList itemsTag = (NBTTagList) tagCompound.getTag("Items");
		furnaceItems = new ItemStack[17];
		if(itemsTag!=null)
		for(int i = 0; i < itemsTag.tagCount(); i++)
		{
			NBTTagCompound slotTag = (NBTTagCompound)itemsTag.getCompoundTagAt(i);
			if(slotTag==null) continue;
			byte slot = slotTag.getByte("Slot");
			
			if(slot >= 0 && slot < furnaceItems.length)
				furnaceItems[slot] = ItemStack.loadItemStackFromNBT(slotTag);
		}
		NBTTagCompound concrete = tagCompound.getCompoundTag("concrete");
		NBTTagCompound water = tagCompound.getCompoundTag("water");
		tankConcrete.readFromNBT(concrete);
		tankWater.readFromNBT(water);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tagCompound)
	{
		super.writeToNBT(tagCompound);
		
		tagCompound.setBoolean("isValidMultiblock", isValidMultiblock);
		NBTTagList itemsList = new NBTTagList();
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
		NBTTagCompound concrete = new NBTTagCompound();
		NBTTagCompound water = new NBTTagCompound();
		tankConcrete.writeToNBT(concrete);
		tankWater.writeToNBT(water);
		tagCompound.setTag("concrete", concrete);
		tagCompound.setTag("water", water);
	}
	
	long time = 0;
	@Override
	public void updateEntity()
	{
		if(!isValidMultiblock || time%20!=0)
		{
			time++;
			return;
		}
		checkFluidSlot();
		if(tankConcrete.getFluidAmount()<64000)
		for(MixerRecipe rec : RecipeHandler.mixerRecipes)
		{
			if(checkInventory(rec))
				makeRecipe(rec);
		}
		time++;
	}
	
	void makeRecipe(MixerRecipe rec)
	{
		for(ItemStack s: rec.solids)
		{
			consume(itemSlots, s);
		}
		tankWater.drain(rec.liquid.amount, true);
		tankConcrete.fill(rec.output.copy(), true);
	}
	
	boolean checkInventory(MixerRecipe rec)
	{
		boolean hasItems = true;
		for(ItemStack s: rec.solids)
		{
			if(!hasItems) break;
			int num = count(itemSlots, s);
			hasItems = hasItems && num >= s.stackSize;
		}
		if(!hasItems) return false;
		if(rec.liquid==null) return true;
		FluidStack f = tankWater.getFluid();
		
		if(f==null) return false;
		if(!f.isFluidEqual(rec.liquid)) return false;
		
		f = tankConcrete.getFluid();
		
		if(f==null) return true;
		//System.out.println(rec.output.amount);
		
		if(f.amount <= 64000 - rec.output.amount )
			return tankWater.getFluid().amount >= rec.liquid.amount;
		return false;
	}
	
	void checkFluidSlot()
	{
		ItemStack fluid = getStackInSlot(8);
		if(fluid!=null && ( fluid.getItem() == Items.water_bucket || fluid.getItem() instanceof IFluidContainerItem))
		{
			if(fluid.getItem() == Items.water_bucket)
			{
				if(tankWater.getFluidAmount()<64000)
				{
					tankWater.fill(new FluidStack(FluidRegistry.WATER, FluidContainerRegistry.BUCKET_VOLUME), true);
					this.setInventorySlotContents(8, new ItemStack(Items.bucket));
				}
			}
			if(fluid.getItem() instanceof IFluidContainerItem)
			{
				IFluidContainerItem container = (IFluidContainerItem) fluid.getItem();
				
				FluidStack temp = container.getFluid(fluid);
				
				if((temp == null 
						|| temp.isFluidEqual(tankConcrete.getFluid()))
						&& tankConcrete.getFluidAmount()>0)
				{
					int existing = temp!=null?temp.amount:0;
					int freeSpace = container.getCapacity(fluid) - existing;
					FluidStack out = tankConcrete.drain(freeSpace, true);
					container.fill(fluid, out, true);
				}
				else if(temp != null)
				{
					for(FluidStack f: RecipeHandler.validInputFluids)
					{
						if(f.isFluidEqual(temp))
						{
							if(tankWater.getFluidAmount()<= (64000 - temp.amount))
							{
								tankWater.fill(temp.copy(), true);
								container.drain(fluid, temp.amount, true);
							}
						}
					}
				}
			}
		}
	}
	
	int count(int[] slots, ItemStack item)
	{
		int ret = 0;
		
		for(int i: slots)
		{
			ItemStack stack = this.getStackInSlot(i);
			
			if(stack!=null && stack.isItemEqual(item))
			{
				ret += stack.stackSize;
			}
			
		}
		
		return ret;
	}
	
	boolean consume(int[] slots, ItemStack item)
	{
		return consume(slots, item, item.stackSize);
	}
	
	boolean consume(int[] slots, ItemStack item, int amount)
	{
		for(int i: slots)
		{
			ItemStack stack = this.getStackInSlot(i);
			if(amount==0) return true;
			
			if(stack!=null && stack.isItemEqual(item))
			{
				
				if(stack.stackSize>=amount)
				{
					stack.splitStack(amount);
					if(stack.stackSize==0) this.setInventorySlotContents(i, null);
					return true;
				}
				else
				{
					amount = amount - stack.stackSize;
					this.setInventorySlotContents(i, null);
				}
			}
		}
		
		return amount == 0;
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
}
