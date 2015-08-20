package thut.api.blocks;

import codechicken.lib.vec.BlockCoord;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public interface IRebar {
	abstract boolean[] sides(IBlockAccess worldObj, int x, int y, int z);
	
	abstract IIcon getIcon(Block block);
	
	abstract boolean[] getInventorySides();
	
	abstract public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float par7, float par8, float par9);
	
	abstract public BlockCoord placeBlock(World worldObj, int x, int y, int z, Block block2, int rebarMeta, ForgeDirection side);
	
	abstract public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player);
}
