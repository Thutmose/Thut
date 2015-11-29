package thut.tech.common.blocks.railgun;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import thut.api.maths.Vector3;
import thut.tech.common.entity.EntityProjectile;

public class TileEntityRailgun extends TileEntity {

	Vector3 dir = Vector3.getNewVectorFromPool();
	double length = 1;
	long lastFired = 0;

	public TileEntityRailgun() {
	}

	public void fire()
	{
		long time = worldObj.getTotalWorldTime();
		if(time - 5 < lastFired)
			return;
		lastFired = time;
		
		Vector3 here = Vector3.getNewVectorFromPool().set(this);
		Block up = here.getBlock(worldObj, EnumFacing.UP);
		length = 10;
		if(up!=Blocks.air)
		{
	    	EntityProjectile p = new EntityProjectile(worldObj, here.x, here.y, here.z, up.getDefaultState());
			System.out.println("pew "+up);
	    	here.addTo(dir).offsetBy(EnumFacing.UP).addTo(0, 1, 0);
	    	here.moveEntity(p);
	    	here.set(dir).scalarMultBy(length);
	    	here.setVelocities(p);
	    	here.set(this).offsetBy(EnumFacing.UP).setBlock(worldObj, Blocks.air);
	    	worldObj.spawnEntityInWorld(p);
		}
		else
		{
			TileEntity down = here.getTileEntity(worldObj, EnumFacing.DOWN);
			if(down!=null && down instanceof IInventory)
			{
				Block b = null;
				IInventory inventory = (IInventory) down;
				for(int i = 0; i< inventory.getSizeInventory(); i++)
				{
					ItemStack stack = inventory.getStackInSlot(i);
					if(stack!=null)
						b = Block.getBlockFromItem(stack.getItem());
					if(b!=null)
					{
						inventory.decrStackSize(i, 1);
						here.offsetBy(EnumFacing.UP).setBlock(worldObj, b, stack.getItemDamage());
						return;
					}
				}
			}
		}
		
	}
	
	public void writeToNBT(NBTTagCompound par1) {
		super.writeToNBT(par1);
		dir.writeToNBT(par1, "dir");
		par1.setDouble("length", length);
	}

	public void readFromNBT(NBTTagCompound par1) {
		super.readFromNBT(par1);
		dir.freeVectorFromPool();
		dir = Vector3.readFromNBT(par1, "dir");
		length = par1.getDouble("length");
	}
	
	public void setDir(Vector3 dir)
	{
		this.dir.set(dir);
	}
	
	public Vector3 getDir()
	{
		return dir;
	}
	
	public void setLength(double l)
	{
		length = l;
	}
	
	public double getLength()
	{
		return length;
	}
}
