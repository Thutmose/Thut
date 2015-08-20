package thut.tech.common.blocks.railgun;

import thut.api.maths.Vector3;
import thut.tech.common.TechCore;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockRailgun extends Block implements ITileEntityProvider{

	public BlockRailgun() {
		super(Material.iron);
		//this.setCreativeTab(TechCore.tabThut);
		this.setBlockName("railgun");
		this.setHardness(10);
		this.setResistance(100);
	}

	public boolean onBlockActivated(World worldObj, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
		TileEntityRailgun railgun = (TileEntityRailgun) worldObj.getTileEntity(x, y, z);
		Vector3 dir = Vector3.getNewVectorFromPool().set(player.getLookVec());
		dir.y = 0;
		railgun.setDir(dir);
		railgun.fire();
		return true;
    }
	
    /**
     * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
     * their own) Args: x, y, z, neighbor Block
     */
    public void onNeighborBlockChange(World worldObj, int x, int y, int z, Block p_149695_5_)
    {
        if (worldObj.isBlockIndirectlyGettingPowered(x, y, z))
        {
    		TileEntityRailgun railgun = (TileEntityRailgun) worldObj.getTileEntity(x, y, z);
    		railgun.fire();
        }
    }
	
	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		// TODO Auto-generated method stub
		return new TileEntityRailgun();
	}

}
