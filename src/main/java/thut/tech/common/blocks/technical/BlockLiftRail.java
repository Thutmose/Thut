package thut.tech.common.blocks.technical;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

//import appeng.api.me.tiles.IGridTileEntity;















import thut.api.ThutBlocks;
import thut.api.blocks.IRebar;
import thut.api.render.RenderRebar;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.tileentity.TileEntityLiftAccess;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Direction;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockLiftRail extends Block implements ITileEntityProvider, IRebar
{

	public IIcon[] iconArray;

	boolean[] side = new boolean[6];
	public static int MAX_PLACEMENT_RANGE = 64;
	
	public BlockLiftRail() 
	{
		super(Material.iron);
		ThutBlocks.liftRail = this;
		this.setBlockName("liftRail");
		setCreativeTab(TechCore.tabThut);
		this.setBlockBounds(0, 0, 0, 0, 0, 0);
		setHardness((float) 10.0);
		setResistance(10.0f);
	}

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float par7, float par8, float par9)
    {
    	boolean placed = false;
    	ItemStack item = player.getHeldItem();
    	
    	world.scheduleBlockUpdate(x, y, z, this, 5);
    	if(item!=null)
    	{

	    	if(Block.getBlockFromItem(item.getItem()) instanceof IRebar)
	    	{
		    	if(placeBlock(world, x, y, z, Block.getBlockFromItem(item.getItem()), item.getItemDamage(), ForgeDirection.getOrientation(side)))
		    	{
		    		placed = true;
	    				if(!player.capabilities.isCreativeMode)
	    					player.inventory.consumeInventoryItem(item.getItem());
		    	}
	    	}
    	}
    	else
    	{
    		TileEntity te = world.getTileEntity(x, y, z);
    		if(te!=null && te instanceof TileEntityLiftAccess)
    		{
    			String test = ""+((TileEntityLiftAccess) te).connectionInfo();
    			player.addChatMessage(new ChatComponentText(test));
    		}
    	}
        return placed;
    }
    
    /**
     * Adds all intersecting collision boxes to a list. (Be sure to only add boxes to the list if they intersect the
     * mask.) Parameters: World, X, Y, Z, mask, list, colliding entity
     */
	@Override
    public void addCollisionBoxesToList(World worldObj, int x, int y, int z, AxisAlignedBB aaBB, List list, Entity par7Entity)
    {
		side = sides(worldObj,x,y,z);
		
		if(!(side[0]||side[1]||side[2]||side[3]||side[4]||side[5]))
			side = new boolean[] {true, true, true, true, false, false};
		
    	AxisAlignedBB aabb;
    	int n = 5;
    	
        for (ForgeDirection fside : ForgeDirection.VALID_DIRECTIONS)
        {
                AxisAlignedBB coll = getBoundingBoxForSide(fside).offset(x, y, z);
                if (aaBB.intersectsWith(coll)&&this.side[n])
                        list.add(coll);
                n--;
        }
    }


	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int x, int y, int z)
    {
		
		side = sides(par1IBlockAccess,x,y,z);
		
		if(!(side[0]||side[1]||side[2]||side[3]||side[4]||side[5]))
			side = new boolean[] {false, false, false, false, true, true};
		setBlockBounds(0.35F, 0.35F, 0.35F, 0.65F, 0.65F, 0.65F);
    }

    public AxisAlignedBB getBoundingBoxForSide(ForgeDirection fside)
    {
            switch (fside)
            {
                    case UP:
                    {
                            return AxisAlignedBB.getBoundingBox(0.35F, 0.4F, 0.35F, 0.65F, 1F, 0.65F);
                    }
                    case DOWN:
                    {
                            return AxisAlignedBB.getBoundingBox(0.35F, 0.0F, 0.35F, 0.65F, 0.6F, 0.65F);
                    }
                    case NORTH:
                    {
                            return AxisAlignedBB.getBoundingBox(0.35F, 0.35F, 0.0F, 0.65F, 0.65F, 0.6F);
                    }
                    case SOUTH:
                    {
                            return AxisAlignedBB.getBoundingBox(0.35F, 0.35F, 0.4F, 0.65F, 0.65F, 1F);
                    }
                    case EAST:
                    {
                            return AxisAlignedBB.getBoundingBox(0.4F, 0.35F, 0.35F, 1F, 0.65F, 0.65F);
                    }
                    case WEST:
                    {
                            return AxisAlignedBB.getBoundingBox(0.0F, 0.35F, 0.35F, 0.60F, 0.65F, 0.65F);
                    }
                    default:
                    {
                            return AxisAlignedBB.getBoundingBox(0f, 0f, 0f, 1f, 1f, 1f);
                    }
            }
    }
    
    private void setBlockBoundsForSide(int x, int y, int z, ForgeDirection side)
    {
            switch (side)
	        {
		            case UP:
		            {
		                    setBlockBounds(0.35F, 0.4F, 0.35F, 0.65F, 1F, 0.65F);
		                    break;
		            }
		            case DOWN:
		            {
		                    setBlockBounds(0.35F, 0.0F, 0.35F, 0.65F, 0.6F, 0.65F);
		                    break;
		            }
		            case NORTH:
		            {
		                    setBlockBounds(0.35F, 0.35F, 0.0F, 0.65F, 0.65F, 0.6F);
		                    break;
		            }
		            case SOUTH:
		            {
		                    setBlockBounds(0.35F, 0.35F, 0.4F, 0.65F, 0.65F, 1F);
		                    break;
		            }
		            case EAST:
		            {
		                    setBlockBounds(0.4F, 0.35F, 0.35F, 1F, 0.65F, 0.65F);
		                    break;
		            }
		            case WEST:
		            {
		                    setBlockBounds(0.0F, 0.35F, 0.35F, 0.60F, 0.65F, 0.65F);
		                    break;
		            }
		            default:
		            {
		                    setBlockBounds(0f, 0f, 0f, 1f, 1f, 1f);
		                    break;
		            }
            }
    }
    
    
    /**
     * Called when the block is clicked by a player. Args: x, y, z, entityPlayer
     */
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) 
    {
    	boolean placed = false;
    	ItemStack item = player.getHeldItem();

    	
    	if(item!=null)
    	{

	    	if(Block.getBlockFromItem(item.getItem()) instanceof IRebar)
	    	{
	        	if(player.isSneaking())
	        	{
	        		boolean done = false;
	        		int num = item.stackSize;
	        		
	        		while(!done&&num>0)
	        		{
			    		placed = placeBlock(world, x, y, z, Block.getBlockFromItem(item.getItem()), item.getItemDamage(), ForgeDirection.UP);
			    		done = !placed;
			    		if(placed)
			    		{
			    			num--;
			    		}
	    				if(!player.capabilities.isCreativeMode)
	    				{
	    					//player.addChatMessage("split");
	    					player.inventory.consumeInventoryItem(item.getItem());
	    				}
	        		}
	        	}
	        	else
		    	if(placeBlock(world, x, y, z, Block.getBlockFromItem(item.getItem()), item.getItemDamage(), ForgeDirection.UP))
		    	{
		    		placed = true;
	    				if(!player.capabilities.isCreativeMode)
	    				{
	    					//player.addChatMessage("split");
	    					player.inventory.consumeInventoryItem(item.getItem());
	    				}
		    	}
	    	}
    	}
	    	
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

		TileEntityLiftAccess controller = (TileEntityLiftAccess)par1IBlockAccess.getTileEntity(par2, par3, par4);
		if(controller!=null)
		{
	//		System.out.println(controller.called);
			return controller.called?15:0;
		}
    
        return 0;
    }
    
    /**
     * Returns true if the block is emitting direct/strong redstone power on the specified side. Args: World, X, Y, Z,
     * side. Note that the side is reversed - eg it is 1 (up) when checking the bottom of the block.
     */
    public int isProvidingStrongPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
    {
        return isProvidingWeakPower(par1IBlockAccess, par2, par3, par4, par5);
    }
	
	
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon("thuttech:liftRails");
	}

	
	public boolean[] sides(IBlockAccess worldObj, int x, int y, int z) 
	{
		boolean[] side = new boolean[]{false, false, false, false, true, true};
    	int[][]sides = {{1,0,0},{-1,0,0},{0,0,1},{0,0,-1},{0,1,0},{0,-1,0}};
		for(int i = 0; i<6; i++)
		{
			Block block  = worldObj.getBlock(x+sides[i][0], y+sides[i][1], z+sides[i][2]);
			TileEntity te = worldObj.getTileEntity(x+sides[i][0], y+sides[i][1], z+sides[i][2]);
			if(i>3)
			{
				side[i] = (block instanceof IRebar);//||(te instanceof IGridTileEntity);
			}
			else
			{
			//	side[i] = (te instanceof IGridTileEntity);
			}
		}
		return side;
	}

    public boolean placeBlock(World worldObj, int x, int y, int z, Block block2, int rebarMeta, ForgeDirection side)
    {
    	int dx = side.offsetX, dy = side.offsetY, dz = side.offsetZ;
    	while(Math.abs(dx)<MAX_PLACEMENT_RANGE&&Math.abs(dy)<MAX_PLACEMENT_RANGE&&Math.abs(dz)<MAX_PLACEMENT_RANGE)
    	{
    		if(dy+y>worldObj.getActualHeight()) return false;
        	Block block = worldObj.getBlock(x+dx, y+dy, z+dz);
        	if(block.isAir(worldObj, x+dx, y+dy, z+dz)||block.getMaterial().isReplaceable())
    		{
    			worldObj.setBlock(x+dx, y+dy, z+dz, block2, rebarMeta, 3);
    			return true;
    		}
        	else if (block!=block2)
        	{
        		return false;
        	}
        	
			dy+=side.offsetY;
			dx+=side.offsetX;
			dz+=side.offsetZ;
		
    	}
    	return false;
    }
	
	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileEntityLiftAccess();
	}

	@Override
	public IIcon getIcon(Block block) {
		return blockIcon;
	}

	@Override
	public boolean[] getInventorySides() {
		return new boolean[] {false, false, false, false, true, true};
	}
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }
	 /**
     * The type of render function that is called for this block
     */
    @Override
    public int getRenderType()
    {
        return RenderRebar.ID;
    }
}
