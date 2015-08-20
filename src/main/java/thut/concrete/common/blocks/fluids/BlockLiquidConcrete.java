package thut.concrete.common.blocks.fluids;

import static net.minecraft.init.Blocks.dirt;
import static net.minecraft.init.Blocks.flowing_water;
import static net.minecraft.init.Blocks.grass;
import static net.minecraft.init.Blocks.water;
import static net.minecraft.util.EnumFacing.DOWN;
import static thut.api.ThutBlocks.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import thut.api.ThutBlocks;
import thut.api.blocks.BlockFluid;
import thut.api.blocks.multiparts.parts.PartFluid;
import thut.api.blocks.multiparts.parts.PartRebar;
import thut.api.maths.Vector3;
import thut.concrete.common.ConcreteCore;
import thut.concrete.common.blocks.technical.BlockRebar;
import thut.concrete.common.blocks.tileentity.worldBlocks.TileEntityBlockFluid;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;
import net.minecraft.item.*;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import scala.collection.Iterator;

public class BlockLiquidConcrete extends BlockFluid
{
	public static int	hardenRate	= 5;
	static Material		wetConcrete	= (new WetConcrete(MapColor.stoneColor));

	Integer[][]		data;
	@SideOnly(Side.CLIENT)
	private IIcon[]	iconArray;

	public BlockLiquidConcrete()
	{
		super(new Fluid("liquidRock").setDensity(4000).setViscosity(2000).setTemperature(310), wetConcrete);
		setBlockName("concreteLiquid");
		setCreativeTab(ConcreteCore.tabThut);
		this.setResistance((float) 10.0);
		this.setHardness((float) 1.0);
		ThutBlocks.liquidConcrete = this;
		ThutBlocks.addPart(this, LiquidConcretePart.class);
		ThutBlocks.parts2.put("tc_liquidConcrete", LiquidConcretePart.class);
		this.setTemperature(310);
		this.solidifiable = true;
		this.setTickRandomly(true);
	}

	@Override
	public void updateTick(World worldObj, int x, int y, int z, Random par5Random)
	{

		boolean val = !worldObj.isRemote;
		if (val)
		{
			worldObj.theProfiler.startSection("Liquid Concrete");
			super.updateTick(worldObj, x, y, z, par5Random);
			worldObj.theProfiler.endSection();
			return;
		}
	}

	///////////////////////////////////////// Block Bounds
	///////////////////////////////////////// Stuff//////////////////////////////////////////////////////////
	/** Returns a bounding box from the pool of bounding boxes (this means this
	 * box can change after the pool has been cleared to be reused) */
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
	{
		return null;
	}

	/** Adds all intersecting collision boxes to a list. (Be sure to only add
	 * boxes to the list if they intersect the mask.) Parameters: World, X, Y,
	 * Z, mask, list, colliding entity */
	@Override
	public void addCollisionBoxesToList(World worldObj, int x, int y, int z, AxisAlignedBB aaBB, List list,
			Entity par7Entity)
	{

	}

	@Override
	public boolean isNormalCube(IBlockAccess world, int x, int y, int z)
	{
		return false;
	}

	@Override
	public void onBlockAdded(World worldObj, int x, int y, int z)
	{

	}

	/** Called when a block is placed using its ItemBlock. Args: World, X, Y, Z,
	 * side, hitX, hitY, hitZ, block metadata */
	public int onBlockPlaced(World worldObj, int x, int y, int z, int side, float hitX, float hitY, float hitZ,
			int meta)
	{
		if (!worldObj.isRemote)
		{
			worldObj.scheduleBlockUpdate(x, y, z, this, tickRate);
		}
		return 15;
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

		if (down.getBlock(worldObj) instanceof BlockFluid && meta != 15 || below == ThutBlocks.volcano) { return; }

		if (below == water || below == flowing_water || down.isAir(worldObj) || below == this) { return; }

		vec.setBlock(worldObj, ThutBlocks.concrete, vec.getBlockMetadata(worldObj), 2);
		return;

	}
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onEntityCollidedWithBlock(World par1World, int par2, int par3, int par4, Entity entity)
	{
		entity.motionX *= 0.5;
		entity.motionZ *= 0.5;
		if (par1World.getBlockMetadata(par2, par3, par4) < 7) entity.motionY *= 0.5;
	}

	@Override
	public int quantityDropped(int meta, int fortune, Random random)
	{
		return 0;
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
		
		//TODO make newBTo microblock aware
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
			newBTo.updateTick(world, to.intX(), to.intY(), to.intZ(), world.rand);
			world.scheduledUpdatesAreImmediate = false;
			if(metaFrom >= 0)
			{
				world.setBlockMetadataWithNotify(from.intX(), from.intY(), from.intZ(), metaFrom, 3);
			}
			else
			{
				from.setAir(world);
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
				from.setAir(world);
			}
		}
	}

	@Override
	public int getQuantaValue(IBlockAccess world, int x, int y, int z)
	{
		//TODO microblock awareness
		TileEntity te = world.getTileEntity(x, y, z);
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
		if (world.getBlock(x, y, z).isAir(world, x, y, z)||rightBlock) { return 0; }
		if ((world.getBlock(x, y, z) instanceof BlockRebar)) { return 0; }

		if (!(world.getBlock(x, y, z) instanceof BlockFluid)) { return -1; }
		if (world.getBlock(x, y, z) != this && getTemperature(world, x, y, z) > this.temperature) { return -1; }

		int quantaRemaining = world.getBlockMetadata(x, y, z) + 1;
		return quantaRemaining;
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister)
	{
		this.iconArray = new IIcon[16];
		this.blockIcon = par1IconRegister.registerIcon("concrete:" + "wetConcrete_" + 8);
		for (int i = 0; i < this.iconArray.length; ++i)
		{
			this.iconArray[i] = par1IconRegister.registerIcon("concrete:" + "wetConcrete_" + i);
		}
	}

	public static class LiquidConcretePart extends PartFluid
	{
		public LiquidConcretePart()
		{
			this(0);
		}

		public LiquidConcretePart(int meta)
		{
			super(meta);
			name = "tc_liquidConcrete";
			block = ThutBlocks.liquidConcrete;
		}
	}

}
