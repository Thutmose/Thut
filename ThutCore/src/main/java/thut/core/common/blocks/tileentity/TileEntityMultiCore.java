package thut.core.common.blocks.tileentity;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.tileentity.TileEntity;

public abstract class TileEntityMultiCore extends TileEntity  implements ISidedInventory
{
	protected boolean isValidMultiblock = false;
	
	public abstract boolean checkIfProperlyFormed();
	 
	public abstract void convertDummies();
	
    public boolean getIsValid()
	{
	    return isValidMultiblock;
	}
	
	/**
     * invalidates a tile entity
     */
    @Override
    public void invalidate()
    {
        invalidateMultiblock();
    }
	
	public void invalidateMultiblock()
	{
	    isValidMultiblock = false;
	    revertDummies();
	}
	
	protected abstract void revertDummies();
}
