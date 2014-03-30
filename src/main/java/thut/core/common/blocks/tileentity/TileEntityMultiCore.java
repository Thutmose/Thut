package thut.core.common.blocks.tileentity;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.tileentity.TileEntity;

public abstract class TileEntityMultiCore extends TileEntity  implements ISidedInventory
{
	protected boolean isValidMultiblock = false;
	
	public boolean getIsValid()
	{
	    return isValidMultiblock;
	}
	 
	public void invalidateMultiblock()
	{
	    isValidMultiblock = false;
	    revertDummies();
	}
	
	public abstract boolean checkIfProperlyFormed();
	
	public abstract void convertDummies();
	
	protected abstract void revertDummies();
}
