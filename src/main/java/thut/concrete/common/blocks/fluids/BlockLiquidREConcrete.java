package thut.concrete.common.blocks.fluids;

import static net.minecraft.init.Blocks.dirt;
import static net.minecraft.init.Blocks.flowing_water;
import static net.minecraft.init.Blocks.grass;
import static net.minecraft.init.Blocks.water;
import static net.minecraft.util.EnumFacing.DOWN;
import static net.minecraftforge.common.util.ForgeDirection.*;
import static thut.api.ThutBlocks.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import thut.api.ThutBlocks;
import thut.api.blocks.*;
import thut.api.blocks.multiparts.parts.PartRebar;
import thut.api.maths.Vector3;
import thut.api.render.RenderRebar;
import thut.concrete.client.render.RenderFluid;
import thut.concrete.common.blocks.technical.BlockRebar;
// import atomicscience.api.IAntiPoisonBlock;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import scala.collection.Iterator;

public class BlockLiquidREConcrete extends BlockFluid implements IRebar// ,
																		// IAntiPoisonBlock
{
	public int		colourid;
	static Material	wetConcrete	= (new WetConcrete(MapColor.stoneColor));
	Integer[][]		data;
	boolean[]		side		= new boolean[6];
	@SideOnly(Side.CLIENT)
	public IIcon	theIcon;
	public boolean	solidifiable;

	public BlockLiquidREConcrete()
	{
		super(new Fluid("REliquidRock").setDensity(4000).setViscosity(2000), Material.iron);
		setBlockName("REconcreteLiquid");
		this.setResistance((float) 10.0);
		this.setHardness((float) 10.0);
		// this.instance = this;
		liquidREConcrete = this;
		this.setTemperature(310);
		this.solidifiable = true;
		this.setTickRandomly(true);
	}

	@Override
	public boolean isNormalCube(IBlockAccess world, int x, int y, int z)
	{
		return false;
	}

	/** Returns the ID of the items to drop on destruction. */
	public Item getItemDropped(int par1, Random par2Random, int par3)
	{
		return Item.getItemFromBlock(rebar);
	}

	@Override
	public int quantityDropped(int meta, int fortune, Random random)
	{
		return 1;
	}
	
	/** Returns whether this block is collideable based on the arguments passed
	 * in
	 * 
	 * @param par1
	 *            block metaData
	 * @param par2
	 *            whether the player right-clicked while holding a boat */
	@Override
	public boolean canCollideCheck(int meta, boolean fullHit)
	{
		if (getFluid().getViscosity() == Integer.MAX_VALUE) return true;
		return fullHit && meta == quantaPerBlock - 1;
	}

	///////////////////////////////////////// Block Bounds
	///////////////////////////////////////// Stuff//////////////////////////////////////////////////////////
	/** Adds all intersecting collision boxes to a list. (Be sure to only add
	 * boxes to the list if they intersect the mask.) Parameters: World, X, Y,
	 * Z, mask, list, colliding entity */
	@Override
	public void addCollisionBoxesToList(World worldObj, int x, int y, int z, AxisAlignedBB aaBB, List list,
			Entity par7Entity)
	{
		side = sides(worldObj, x, y, z);

		if (!(side[0] || side[1] || side[2] || side[3] || side[4] || side[5]))
			side = new boolean[] { true, true, true, true, false, false };

		for (ForgeDirection fside : ForgeDirection.VALID_DIRECTIONS)
		{
			AxisAlignedBB coll = getBoundingBoxForSide(fside).offset(x, y, z);
			if (aaBB.intersectsWith(coll) && this.side[fside.ordinal()]) list.add(coll);
		}

	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		int meta = world.getBlockMetadata(x, y, z);
		setBlockBounds(0, 0 , 0, 1, (meta + 1)/16f, 1);
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

	/** @param world
	 * @param from
	 * @param to
	 * @param metaTo
	 * @param metaFrom
	 * @param instant:
	 *            does this instantly call tick on the block below */
	public void flowInto(World world, Vector3 from, Vector3 to, int metaTo, int metaFrom, boolean instant)
	{
		Block bTo = to.getBlock(world);

		TileEntity te = to.getTileEntity(world);
    	TileMultipart tile = (TileMultipart) ((te instanceof TileMultipart)?te:null);
    	boolean rightBlock = false;
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
		
		Block newBTo = (bTo instanceof BlockRebar) || (bTo instanceof BlockLiquidREConcrete) || (bTo instanceof BlockREConcrete) || rightBlock
				? ThutBlocks.liquidREConcrete : ThutBlocks.liquidConcrete;

		if (instant)
		{
			to.setBlock(world, newBTo, metaTo, 2);
			world.scheduledUpdatesAreImmediate = true;
			this.updateTick(world, to.intX(), to.intY(), to.intZ(), world.rand);
			world.scheduledUpdatesAreImmediate = false;
			if(metaFrom >= 0)
			{
				world.scheduleBlockUpdate(from.intX(), from.intY(), from.intZ(), this, tickRate);
			}
			else
			{
				from.setBlock(world, ThutBlocks.rebar);
			}
		}
		else
		{
			to.setBlock(world, newBTo, metaTo, 3);
			if (metaFrom >= 0)
			{
				world.setBlockMetadataWithNotify(from.intX(), from.intY(), from.intZ(), metaFrom, 3);
			}
			else
			{
				from.setBlock(world, ThutBlocks.rebar);
			}
		}
	}

	@Override
	public int getQuantaValue(IBlockAccess world, int x, int y, int z)
	{

		TileEntity te = world.getTileEntity(x, y, z);
    	TileMultipart tile = (TileMultipart) ((te instanceof TileMultipart)?te:null);
		Block b = world.getBlock(x, y, z);
    	boolean rightBlock = false;
    	if(tile!=null)
    	{
    		Iterator<TMultiPart> it = tile.partList().iterator();
    		while(it.hasNext())
    		{
    			TMultiPart p = it.next();
    			if(p instanceof PartRebar)
    			{
    				rightBlock = true;
    				b = ((PartRebar) p).getBlock();
    				break;
    			}
    		}
    	}
	//	if (world.getBlock(x, y, z).isAir(world, x, y, z)) { return 0; }
		if ((b instanceof BlockRebar)||rightBlock) { return 0; }

		if (!(b == this||b==ThutBlocks.reConcrete)) { return -1; }
		//if (world.getBlock(x, y, z) != this && getTemperature(world, x, y, z) > this.temperature) { return -1; }

		int quantaRemaining = world.getBlockMetadata(x, y, z) + 1;
		return quantaRemaining;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon("concrete:wetConcrete_" + 8);
		this.theIcon = par1IconRegister.registerIcon("concrete:" + "rebar");
		this.iconArray = new IIcon[16];
		for (int i = 0; i < this.iconArray.length; ++i)
		{
			this.iconArray[i] = par1IconRegister.registerIcon("concrete:" + "wetConcrete_" + i);
		}

	}

	public boolean[] sides(IBlockAccess worldObj, int x, int y, int z)
	{
		boolean[] side = new boolean[6];
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

	public void doHardenTick(World worldObj, Vector3 vec)
	{
		Vector3 down = vec.offset(DOWN);

		Block below = down.getBlock(worldObj);
		int meta = down.getBlockMetadata(worldObj);

		if (below == grass)
		{
			down.setBlock(worldObj, dirt, 0, 2);
		}

		if (down.getBlock(worldObj) instanceof BlockFluid && meta != 15) { return; }


		if (below == this) { return; }

		vec.setBlock(worldObj, ThutBlocks.reConcrete, vec.getBlockMetadata(worldObj), 2);
		return;

	}
	
	@Override
	public IIcon getIcon(Block block)
	{
		return this.blockIcon;
	}

	/** The type of render function that is called for this block */
	@Override
	public int getRenderType()
	{
		return RenderFluid.ID;
	}

	@Override
	public boolean[] getInventorySides()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BlockCoord placeBlock(World worldObj, int x, int y, int z, Block block2, int rebarMeta, ForgeDirection side)
	{
		// TODO Auto-generated method stub
		return null;
	}

}