package thut.tech.common.blocks.lift;

import java.util.List;
import java.util.Random;

//import powercrystals.minefactoryreloaded.api.rednet.IConnectableRedNet;
//import powercrystals.minefactoryreloaded.api.rednet.RedNetConnectionType;




















import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import scala.collection.Iterator;
import thut.api.ThutBlocks;
import thut.api.blocks.multiparts.parts.PartRebar;
import thut.tech.common.TechCore;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.items.ItemLinker;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.util.RotationHelper;

public class BlockLift extends Block implements ITileEntityProvider//, IConnectableRedNet
{

	public int size = 5;
	
	public BlockLift() 
	{
		super(Material.iron);
		setHardness(3.5f);
		this.setTickRandomly(true);
		this.setCreativeTab(TechCore.tabThut);
		ThutBlocks.lift = this;
	}
	
    /**
     * Called when a block is placed using its ItemBlock. Args: World, X, Y, Z, side, hitX, hitY, hitZ, block metadata
     */
    public int onBlockPlaced(World worldObj, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta)
    {
        return meta;
    }
	
	public ForgeDirection getFacingfromEntity(EntityLiving e)
	{
		ForgeDirection side = null;
		double angle = e.rotationYaw%360;
			
		if(angle>315||angle<=45)
		{
			return ForgeDirection.SOUTH;
		}
		if(angle>45&&angle<=135)
		{
			return ForgeDirection.WEST;
		}
		if(angle>135&&angle<=225)
		{
			return ForgeDirection.NORTH;
		}
		if(angle>225&&angle<=315)
		{
			return ForgeDirection.EAST;
		}
		
		return side;
	}
	
	public boolean onBlockActivated(World worldObj, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
		 int meta = worldObj.getBlockMetadata(x, y, z);
		 
		 if(worldObj.isRemote)
		 {
			 return true;
		 }
		 if(meta==1)
		 {
			 TileEntityLiftAccess te = (TileEntityLiftAccess)worldObj.getTileEntity(x, y, z);
			 if(te!=null&&(!te.isSideOn(side)||
					 (player.getHeldItem()!=null && player.getHeldItem().getItem() == Items.stick)))
			 {
				 if(player.getHeldItem()!=null&&(
						 player.getHeldItem().getItem().getUnlocalizedName().toLowerCase().contains("wrench")
						 ||player.getHeldItem().getItem().getUnlocalizedName().toLowerCase().contains("screwdriver")
						 ||player.getHeldItem().getItem() instanceof ItemLinker
						 ||player.getHeldItem().getItem() == Items.stick
						 ))
					 {
						 te.setSide(side, !te.isSideOn(side));
						 if(te!=null)
							 te.updateBlock();
						 return true;
					 }
			 }
			 else if(te!=null && te.isSideOn(side))
			 {
				 if(player.getHeldItem()!=null&&(
						 player.getHeldItem().getItem().getUnlocalizedName().toLowerCase().contains("wrench")
						 ||player.getHeldItem().getItem().getUnlocalizedName().toLowerCase().contains("screwdriver")
						 ||player.getHeldItem().getItem() instanceof ItemLinker
						 ))
					 {
						 te.setSidePage(side, (te.getSidePage(side) + 1)%4);
						 if(te!=null)
							 te.updateBlock();
						 return true;
					 }
			 }
			 if(te!=null)
				 te.updateBlock();
		 }
		 boolean ret = false;
		 int id;
		 boolean rails = false;
		 
		 if(meta == 0)
		 {
			 ret = checkRailsForSpawn(worldObj, true, x, y, z);
			 if(!ret)
			 {
				 ret = checkRailsForSpawn(worldObj, false, x, y, z);
				 if(ret)
					 rails = true;
			 }
			 if(!ret&&worldObj.isRemote)
			 {
				 player.addChatMessage(new ChatComponentText("complete rails not found"));
			 }
			 ItemStack[][] blocks = null;
			 if(ret)
				 blocks = checkBlocks(worldObj, x, y, z);
			 ret = blocks != null;
			if(ret&&!worldObj.isRemote)
			{
				removeBlocks(worldObj, x, y, z);
				EntityLift lift = new EntityLift(worldObj, x+0.5, y, z+0.5, size);
				lift.blocks = blocks;
				worldObj.spawnEntityInWorld(lift);
			}
			if(!ret&&rails&&worldObj.isRemote)
			{
				player.addChatMessage(new ChatComponentText("complete base not found"));
			}
		 }
		 else if(meta == 1 && !worldObj.isRemote)
		 {
			 TileEntityLiftAccess te = (TileEntityLiftAccess)worldObj.getTileEntity(x, y, z);
			 if(te!=null)
				 te.doButtonClick(side, hitX, hitY, hitZ);
			 ret = true;
		 }
		 
		return ret;
    }
	

    /**
     * Ticks the block if it's been scheduled
     */
	public void updateTick(World world, int x, int y, int z, Random rand)
    {
    	TileEntity te = world.getTileEntity(x, y, z);
    	if(te instanceof TileEntityLiftAccess)
    	{
    		((TileEntityLiftAccess)te).notifySurroundings();
    	}
    }
	 
	public boolean checkRailsForSpawn(World worldObj, boolean axis, int x, int y, int z)
	{
		int[] sizes = {5,3,1};
		
		boolean ret = false;
		BlockCoord pos = new BlockCoord();
		
		for(int j = 0; j<3; j++)
		{
			boolean bool = true;
			boolean rightBlock = false;
			int rail =(int)(1+Math.floor(sizes[j]/2));
			int colmn = (int)(sizes[j]/2);
			
			int[][] sides = {{rail,0},{-rail,0},{0,rail},{0,-rail}};
			
			
			for(int i = 0; i<3; i++)
			{
				pos.set(x+sides[axis?2:0][0],y+i,z+sides[axis?2:0][1]);
				rightBlock = PartRebar.isRebar(worldObj, pos);
				bool = bool && rightBlock;
				pos.set(x+sides[axis?3:1][0],y+i,z+sides[axis?3:1][1]);
				rightBlock = PartRebar.isRebar(worldObj, pos);
				bool = bool && rightBlock;
				
			}
			if(bool)
			{
				size = sizes[j];
				ret = bool;
				break;
			}
		}
		
		return ret;
	}
	
	public ItemStack[][] checkBlocks(World worldObj, int x, int y, int z)
	{
		ItemStack[][] ret = new ItemStack[size][size];
		
		int rad = (size/2);
		
		for(int i = -rad; i<=rad;i++)
			for(int j = -rad;j<=rad;j++)
			{
				if(!(i==0&&j==0))
				{
					Block b;
					if((b = worldObj.getBlock(x+i, y, z+j)).isNormalCube())
					{
						ret[i + rad][j+rad] = new ItemStack(b, 1, worldObj.getBlockMetadata(x+i, y, z+j));
					}
					else
					{
						return null;
					}
				}
				else
				{
					ret[i + rad][j+rad] = new ItemStack(this);
				}
			}
		return ret;
	}
	
	public void removeBlocks(World worldObj, int x, int y, int z)
	{
		int rad = (size/2);
		
		for(int i = -rad; i<=rad;i++)
			for(int j = -rad;j<=rad;j++)
				for(int k = 0; k<3; k++)
				{
					worldObj.setBlock(x+i, y+k, z+j,Blocks.air,0,3);
				}
		
	}

    @SideOnly(Side.CLIENT)
	public IIcon icon;
    @SideOnly(Side.CLIENT)
	public IIcon icon2;
    
	
	@Override
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		blockIcon = iconRegister.registerIcon("thuttech:blockLift");
		icon = iconRegister.registerIcon("thuttech:liftControl");
		icon2 = iconRegister.registerIcon("thuttech:controlPanel_1");
	}
	
    @SideOnly(Side.CLIENT)
    /**
     * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
     */
    public IIcon getIcon(int par1, int par2)
    {
        return par2==0?blockIcon:par1==3?icon2:icon;
    }
    
	 @SideOnly(Side.CLIENT)

	    /**
	     * Retrieves the block texture to use based on the display side. Args: iBlockAccess, x, y, z, side
	     */
	    public IIcon getIcon(IBlockAccess par1IBlockAccess, int x, int y, int z, int par5)
	    {
		 int meta = par1IBlockAccess.getBlockMetadata(x, y, z);
		 if(meta==0)
		 {
			 return blockIcon;
		 }
		 return this.icon;
	    }

	  /**
	     * Called throughout the code as a replacement for block instanceof BlockContainer
	     * Moving this to the Block base class allows for mods that wish to extend vinella
	     * blocks, and also want to have a tile entity on that block, may.
	     *
	     * Return true from this function to specify this block has a tile entity.
	     *
	     * @param metadata Metadata of the current block
	     * @return True if block has a tile entity, false otherwise
	     */
	    public boolean hasTileEntity(int metadata)
	    {
	        return metadata==1;
	    }

	    /**
	     * Called throughout the code as a replacement for ITileEntityProvider.createNewTileEntity
	     * Return the same thing you would from that function.
	     * This will fall back to ITileEntityProvider.createNewTileEntity(World) if this block is a ITileEntityProvider
	     *
	     * @param metadata The Metadata of the current block
	     * @return A instance of a class extending TileEntity
	     */
	    public TileEntity createTileEntity(World world, int metadata)
	    {
	        if (metadata==1)
	        {
	            return new TileEntityLiftAccess();
	        }
	        return null;
	    }

	
	//////////////////////////////////////////////////////RedStone stuff/////////////////////////////////////////////////
    /**
     * Can this block provide power. Only wire currently seems to have this change based on its state.
     */
    public boolean canProvidePower()
    {
        return true;
    }

    /**
     * Returns true if the block is emitting indirect/weak redstone power on the specified side. If isBlockNormalCube
     * returns true, standard redstone propagation rules will apply instead and this will not be called. Args: World, X,
     * Y, Z, side. Note that the side is reversed - eg it is 1 (up) when checking the bottom of the block.
     */
    public int isProvidingWeakPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
    {

    	int meta = par1IBlockAccess.getBlockMetadata(par2, par3, par4);
    	if(meta==1)
    	{
    		TileEntityLiftAccess controller = (TileEntityLiftAccess)par1IBlockAccess.getTileEntity(par2, par3, par4);
    		if(controller!=null)
    		{
    			boolean cof = controller.called;
    			if(controller.lift!=null)
    			{
    				cof = controller.lift.posY == par3 - 2 && controller.lift.getCurrentFloor() == controller.floor;
    			}
    			
    			return cof?15:0;
    		}
    	}
        return 0;
    }
    
    /**
     * Returns true if the block is emitting direct/strong redstone power on the specified side. Args: World, X, Y, Z,
     * side. Note that the side is reversed - eg it is 1 (up) when checking the bottom of the block.
     */
    public int isProvidingStrongPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
    {
        return 0;// isProvidingWeakPower(par1IBlockAccess, par2, par3, par4, par5);
    }
	
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    @SideOnly(Side.CLIENT)

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (int j = 0; j < 2; j++)
        {
            par3List.add(new ItemStack(par1, 1, j));
        }
    }

    protected ItemStack createStackedBlock(int par1)
    {
        return new ItemStack(this, 1, par1);
    }
   
    /**
     * Determines the damage on the item the block drops. Used in cloth and wood.
     */
    public int damageDropped(int par1)
    {
        return par1;
    }
    
    /**
     * Rotate the block. For vanilla blocks this rotates around the axis passed in (generally, it should be the "face" that was hit).
     * Note: for mod blocks, this is up to the block and modder to decide. It is not mandated that it be a rotation around the
     * face, but could be a rotation to orient *to* that face, or a visiting of possible rotations.
     * The method should return true if the rotation was successful though.
     *
     * @param worldObj The world
     * @param x X position
     * @param y Y position
     * @param z Z position
     * @param axis The axis to rotate around
     * @return True if the rotation was successful, False if the rotation failed, or is not possible
     */
    public boolean rotateBlock(World worldObj, int x, int y, int z, ForgeDirection axis)
    {
        return RotationHelper.rotateVanillaBlock(this, worldObj, x, y, z, axis);
    }

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileEntityLiftAccess();
	}
//	@Override
//	public String getUnlocalizedName(ItemStack stack) {
//		return stack.getItemDamage()==1?"tile.control":"tile.lift";
//	}

}
