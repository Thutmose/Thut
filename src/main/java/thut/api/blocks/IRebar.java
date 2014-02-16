package thut.api.blocks;

import net.minecraft.block.Block;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface IRebar {
	abstract boolean[] sides(IBlockAccess worldObj, int x, int y, int z);
	
	abstract IIcon getIcon(Block block);
	
	abstract boolean[] getInventorySides();
}
