package thut.concrete.common.blocks.technical;
import static thut.api.ThutBlocks.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import codechicken.multipart.minecraft.McBlockPart;
import codechicken.multipart.minecraft.McMetaPart;
import scala.collection.Iterator;
import thut.api.ThutBlocks;
import thut.api.blocks.IRebar;
import thut.api.blocks.multiparts.parts.PartRebar;
import thut.api.render.RenderRebar;
import thut.concrete.common.ConcreteCore;
import thut.concrete.common.blocks.fluids.BlockLiquidConcrete;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

public class BlockRebar extends Block implements IRebar
{
	@SideOnly(Side.CLIENT)
	public IIcon theIcon;
	@SideOnly(Side.CLIENT)
	public IIcon itemIcon;
	boolean[] side = new boolean[6];
	public static boolean first = true;
	
	public static int MAX_PLACEMENT_RANGE = 64;
	
	public BlockRebar()
	{
		super(Material.iron);
		setHardness((float) 10.0);
		setBlockName("rebar");
		setCreativeTab(ConcreteCore.tabThut);
		this.setBlockBounds(0, 0, 0, 0, 0, 0);
		setResistance(10.0f);
		if(first)
		{
			rebar = this;
			first = false;
		}
		setLightOpacity(0);
	}
	
	
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float par7, float par8, float par9)
    {
    	boolean placed = false;
    	ItemStack item = player.getHeldItem();
    	
    	if(item!=null)
    	{
        	BlockCoord pos = new BlockCoord(x, y, z);
	    	if(Block.getBlockFromItem(item.getItem()) instanceof IRebar)
	    	{
	    		pos = placeBlock(world, x, y, z, Block.getBlockFromItem(item.getItem()), item.getItemDamage(), ForgeDirection.getOrientation(side));
		    	if((pos) != null)
		    	{
	    			McBlockPart part = ThutBlocks.getPart(this);
	    			try
					{
						if(!world.isRemote && TileMultipart.canPlacePart(world, pos, part))
						{
							TileMultipart.addPart(world, pos, part);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
	                if(!player.capabilities.isCreativeMode)
	                {
	                    item.stackSize--;
	                    if (item.stackSize == 0)
	                    {
	                        player.inventory.mainInventory[player.inventory.currentItem] = null;
	                        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, item));
	                    }
	                }
		    		placed = true;
	    				if(!player.capabilities.isCreativeMode)
	    					player.inventory.consumeInventoryItem(item.getItem());
		    	}
	    	}
	    	if(Block.getBlockFromItem(item.getItem()) instanceof BlockLiquidConcrete)
	    	{
	    		placed = true;
		    	world.setBlock(x, y, z, liquidREConcrete,15,3);
				world.scheduledUpdatesAreImmediate = true;
				liquidREConcrete.updateTick(world, x,y,z, world.rand);
				world.scheduledUpdatesAreImmediate = false;
				if(!player.capabilities.isCreativeMode)
					item.splitStack(1);
		    	
	    	}
	    	
    	}
        return placed;
    }
    
    /**
     * Called when the block is clicked by a player. Args: x, y, z, entityPlayer
     */
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) 
    {
    	boolean placed = false;
    	ItemStack item = player.getHeldItem();

    	BlockCoord pos;
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
			    		placed = (pos = placeBlock(world, x, y, z, Block.getBlockFromItem(item.getItem()), item.getItemDamage(), ForgeDirection.UP))!=null;
			    		done = !placed;
			    		if(placed)
			    		{
			    			McBlockPart part = ThutBlocks.getPart(this);
			    			if(!world.isRemote)
			                TileMultipart.addPart(world, pos, part);
			                if(!player.capabilities.isCreativeMode)
			                {
			                    item.stackSize--;
			                    if (item.stackSize == 0)
			                    {
			                        player.inventory.mainInventory[player.inventory.currentItem] = null;
			                        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, item));
			                    }
			                }
			    			num--;
			    		}
	    				if(!player.capabilities.isCreativeMode)
	    				{
	    					player.inventory.consumeInventoryItem(item.getItem());
	    				}
	        		}
	        	}
	        	else
		    	if((pos = placeBlock(world, x, y, z, Block.getBlockFromItem(item.getItem()), item.getItemDamage(), ForgeDirection.UP))!=null)
		    	{
		    		placed = true;
	    			McBlockPart part = ThutBlocks.getPart(this);
	    			if(!world.isRemote)
	                TileMultipart.addPart(world, pos, part);
	                if(!player.capabilities.isCreativeMode)
	                {
	                    item.stackSize--;
	                    if (item.stackSize == 0)
	                    {
	                        player.inventory.mainInventory[player.inventory.currentItem] = null;
	                        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, item));
	                    }
	                }
	    				if(!player.capabilities.isCreativeMode)
	    				{
	    					player.inventory.consumeInventoryItem(item.getItem());
	    				}
		    	}
	    	}
    	}
	    	
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
	

    public BlockCoord placeBlock(World worldObj, int x, int y, int z, Block block2, int rebarMeta, ForgeDirection side)
    {
    	int dx = side.offsetX, dy = side.offsetY, dz = side.offsetZ;
    	BlockCoord ret = new BlockCoord();
    	while(Math.abs(dx)<MAX_PLACEMENT_RANGE&&Math.abs(dy)<MAX_PLACEMENT_RANGE&&Math.abs(dz)<MAX_PLACEMENT_RANGE)
    	{
    		if(dy+y>worldObj.getActualHeight()) return null;
        	Block block = worldObj.getBlock(x+dx, y+dy, z+dz);

    		ret.set(x+dx, y+dy, z+dz);
        	boolean rightBlock = block2==block;
        	TileMultipart tile = TileMultipart.getOrConvertTile(worldObj, ret);
        	
        	if(tile!=null)
        	{
        		Iterator<TMultiPart> it = tile.partList().iterator();
        		while(it.hasNext())
        		{
        			TMultiPart p = it.next();
        			if(p instanceof PartRebar)
        			{
        				rightBlock = true;
        				break;
        			}
        		}
        	}
        	
        	if(block.isAir(worldObj, x+dx, y+dy, z+dz)||block.getMaterial().isReplaceable())
    		{
    			return ret;
    		}
        	else if (!rightBlock)
        	{
        		return null;
        	}
        	
			dy+=side.offsetY;
			dx+=side.offsetX;
			dz+=side.offsetZ;
		
    	}
    	return null;
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

	
	
    /**
     * Checks if a player or entity can use this block to 'climb' like a ladder.
     *
     * @param world The current world
     * @param x X Position
     * @param y Y position
     * @param z Z position
     * @return True if the block should act like a ladder
     */
	@Override
    public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity)
    {
    	
		if(true)//TODO figure out how to do this with multi-parts.
			return false;
		
		
    	side = sides(world,x,y,z);
		
		if(!(side[0]||side[1]||side[2]||side[3]||side[4]||side[5]))
			side = new boolean[] {true, true, true, true, false, false};
		
        return side[0]||side[1]||side[2]||side[3];
    }
	
	
	
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int x, int y, int z)
    {
		
		side = sides(par1IBlockAccess,x,y,z);
		
		if(!(side[0]||side[1]||side[2]||side[3]||side[4]||side[5]))
			side = new boolean[] {true, true, true, true, false, false};
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
     * The type of render function that is called for this block
     */
    @Override
    public int getRenderType()
    {
        return RenderRebar.ID;
    }
	
    
    @SideOnly(Side.CLIENT)

    /**
     * When this method is called, your block should register all the icons it needs with the given IconRegister. This
     * is the only chance you get to register icons.
     */
    public void registerBlockIcons(IIconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon("concrete:" + "rebar");
        this.theIcon = par1IconRegister.registerIcon("concrete:" + "rebar");
    }

	public boolean[] sides(IBlockAccess worldObj, int x, int y, int z) 
	{
		boolean[] side = new boolean[]{false, false, false, false, false, false};
    	//int[][]sides = {{1,0,0},{-1,0,0},{0,0,1},{0,0,-1},{0,1,0},{0,-1,0}};
		for(int i = 0; i<6; i++)
		{
			EnumFacing dir = EnumFacing.getFront(i);
			Block block = worldObj.getBlock(x+dir.getFrontOffsetX(), y+dir.getFrontOffsetY(), z+dir.getFrontOffsetZ());
        	boolean rightBlock = false;
        	TileEntity te = worldObj.getTileEntity(x+dir.getFrontOffsetX(), y+dir.getFrontOffsetY(), z+dir.getFrontOffsetZ());
        	
        	TileMultipart tile = (TileMultipart) ((te instanceof TileMultipart)?te:null);
        	
        	if(tile!=null)
        	{
        		Iterator<TMultiPart> it = tile.partList().iterator();
        		while(it.hasNext())
        		{
        			TMultiPart p = it.next();
        			if(p instanceof PartRebar)
        			{
        				rightBlock = true;
        				break;
        			}
        		}
        	}
			
			side[i] = (block instanceof IRebar) || rightBlock;
			
			
			
		}
		
		return side;
	}
	
	/**
     * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
     * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
     */
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }
    
	
	@Override
	public IIcon getIcon(Block block) {
		return this.blockIcon;
	}


	@Override
	public boolean[] getInventorySides() {
		return new boolean[] {false, false, true, true, true, true};
	}
}
