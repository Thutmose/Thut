package thut.tech.common.blocks.railgun;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import thut.api.maths.Vector3;
import thut.tech.common.TechCore;

public class BlockRailgun extends Block implements ITileEntityProvider{

    public static BlockRailgun instance;
    
	public BlockRailgun() {
		super(Material.iron);
		this.setCreativeTab(TechCore.tabThut);
		this.setUnlocalizedName("railgun");
		this.setHardness(10);
		this.setResistance(100);
		instance = this;
	}

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
		TileEntityRailgun railgun = (TileEntityRailgun) worldIn.getTileEntity(pos);
		Vector3 dir = Vector3.getNewVector().set(playerIn.getLookVec());
		dir.y = 0;
		railgun.setDir(dir);
		railgun.fire();
		return true;
    }
	
	@Override
    /**
     * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
     * their own) Args: x, y, z, neighbor Block
     */
    public void onNeighborBlockChange(World worldObj, BlockPos pos, IBlockState state, Block p_149695_5_)
    {
        if (worldObj.isBlockIndirectlyGettingPowered(pos)>0)
        {
    		TileEntityRailgun railgun = (TileEntityRailgun) worldObj.getTileEntity(pos);
    		railgun.fire();
        }
    }
	
	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		// TODO Auto-generated method stub
		return new TileEntityRailgun();
	}

}
