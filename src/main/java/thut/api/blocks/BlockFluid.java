package thut.api.blocks;

import static net.minecraft.init.Blocks.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockRedstoneComparator;
import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockStem;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import thut.api.ThutBlocks;
import thut.api.ThutCore;
import thut.api.maths.Vector3;
import thut.concrete.common.blocks.fluids.BlockLiquidConcrete;

public abstract class BlockFluid extends BlockFluidBase
{
	public static List<Block> defaultReplacements = new ArrayList<Block>();

	public static void init()
	{
		defaultReplacements.add(fire);
		defaultReplacements.add(snow);
		defaultReplacements.add(wheat);
		defaultReplacements.add(lever);
		defaultReplacements.add(rail);
		defaultReplacements.add(torch);
		defaultReplacements.add(golden_rail);
		defaultReplacements.add(detector_rail);
		defaultReplacements.add(potatoes);
		defaultReplacements.add(carrots);
		defaultReplacements.add(waterlily);
		defaultReplacements.add(activator_rail);
		defaultReplacements.add(web);
		defaultReplacements.add(vine);
		defaultReplacements.add(reeds);
		defaultReplacements.add(tripwire);

		for (Block b : ThutBlocks.getAllBlocks())
		{
			if (b instanceof BlockFlower || b instanceof BlockSign || b instanceof BlockRedstoneTorch
					|| b instanceof BlockLeaves || b instanceof BlockRedstoneComparator || b instanceof BlockStem
					|| b instanceof BlockCarpet || b.getMaterial().isReplaceable())
			{
				defaultReplacements.add(b);
			}
		}
	}

	public boolean solidifiable = false;

	public BlockFluid(Fluid fluid, Material material)
	{
		super(fluid, material);
		FluidRegistry.registerFluid(fluid);
		setQuantaPerBlock(16);
		if (fluid.getViscosity() == Integer.MAX_VALUE)
		{
			setRenderPass(0);
			setTickRandomly(false);
		}
	}

	/** Called when a block is placed using its ItemBlock. Args: World, X, Y, Z,
	 * side, hitX, hitY, hitZ, block metadata */
	@Override
	public int onBlockPlaced(World worldObj, int x, int y, int z, int side, float hitX, float hitY, float hitZ,
			int meta)
	{
		int placeamount = 1;
		if (getFluid().getViscosity() < Integer.MAX_VALUE) placeamount = 16;

		return (placeamount - 1);
	}

	@Override
	public boolean onBlockActivated(World worldObj, int x, int y, int z, EntityPlayer player, int side, float par7,
			float par8, float par9)
	{

		ItemStack item = player.getHeldItem();
		int meta = worldObj.getBlockMetadata(x, y, z);
		boolean ret = false;

		if (item != null)
		{
			Block itemID = Block.getBlockFromItem(item.getItem());
			int itemMeta = item.getItemDamage();
			Block id = worldObj.getBlock(x, y, z);
			if (id instanceof BlockFluid)
			{
				BlockFluid block = (BlockFluid) id;
				Vector3 vec = Vector3.getNewVectorFromPool().set(x, y, z);
				if (meta != 15 && itemID == this)
				{
					ret = placedStack(worldObj, item, x, y, z, EnumFacing.getFront(side), block, player);
				}
				// doHardenTick(worldObj, vec);
				vec.freeVectorFromPool();
			}
			if (!worldObj.isRemote)
			{
				player.addChatMessage(new ChatComponentText("" + meta));
			}
		}
		worldObj.scheduledUpdatesAreImmediate = true;
		worldObj.getBlock(x, y, z).updateTick(worldObj, x, y, z, worldObj.rand);
		worldObj.scheduledUpdatesAreImmediate = false;
		return ret;

	}

	public boolean placedStack(World worldObj, ItemStack stack, int x, int y, int z, EnumFacing side, BlockFluid block,
			EntityPlayer player)
	{
		Block id = worldObj.getBlock(x, y, z);
		Block id1 = worldObj.getBlock(x + side.getFrontOffsetX(), y + side.getFrontOffsetY(),
				z + side.getFrontOffsetZ());

		Block itemID = Block.getBlockFromItem(stack.getItem());
		if (itemID == null) { return false; }

		int meta = worldObj.getBlockMetadata(x, y, z);

		int meta1 = worldObj.getBlockMetadata(x + side.getFrontOffsetX(), y + side.getFrontOffsetY(),
				z + side.getFrontOffsetZ());
		int placementamount = 1;

		int initialamount = meta;

		int newMeta = (placementamount + initialamount);

		int remainder = (placementamount - (15 - meta));

		Block block1 = id1;

		if (id1 == Blocks.air || block1.getMaterial().isReplaceable())
		{
			worldObj.setBlock(x, y, z, this, Math.min(newMeta, 15), 3);
			if (newMeta < 0)
			{
				worldObj.setBlock(x + side.getFrontOffsetX(), y + side.getFrontOffsetY(), z + side.getFrontOffsetZ(),
						itemID, remainder, 3);
			}

			if (!player.capabilities.isCreativeMode)
			{
				stack.splitStack(1);
			}
			return true;
		}

		return false;
	}

	@Override
	public int getQuantaValue(IBlockAccess world, int x, int y, int z)
	{
		if (world.getBlock(x, y, z).isAir(world, x, y, z)) { return 0; }

		if (!(world.getBlock(x, y, z) instanceof BlockFluid)) { return -1; }
		if (world.getBlock(x, y, z) != this && getTemperature(world, x, y, z) > this.temperature) { return -1; }

		int quantaRemaining = world.getBlockMetadata(x, y, z) + 1;
		return quantaRemaining;
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

	@Override
	public int getMaxRenderHeightMeta()
	{
		return quantaPerBlock;
	}

	/** Ticks the block if it's been scheduled */
	@Override
	public void updateTick(World world, int x, int y, int z, Random rand)
	{

		if (getFluid().getViscosity() == Integer.MAX_VALUE) return;

		for (EnumFacing dir : EnumFacing.values())
		{
			Vector3 vec = Vector3.getNewVectorFromPool().set(x, y, z);

			Block block = vec.getBlock(world, dir);
			if (block.getTickRandomly() && !world.isBlockTickScheduledThisTick(x, y, z, block))
			{
				world.scheduleBlockUpdate(vec.intX() + dir.getFrontOffsetX(), vec.intY() + dir.getFrontOffsetY(),
						vec.intZ() + dir.getFrontOffsetZ(), block, block.tickRate(world));
			}
			vec.freeVectorFromPool();
		}

		int quanta = getQuantaValue(world, x, y, z);
		
		int original = quanta;

		// Try to flow straight down
		quanta = tryToFlowVerticallyInto(world, x, y, z, quanta);
		if (quanta <= 1)
		{
			if (original == quanta)
			{
				Vector3 vec = Vector3.getNewVectorFromPool().set(x, y, z);
				doHardenTick(world, vec);
				vec.freeVectorFromPool();
			}
			return;
		}
		// Displace blocks to the sides
		if (displaceIfPossible(world, x, y, z - 1)) world.setBlock(x, y, z - 1, Blocks.air);
		if (displaceIfPossible(world, x, y, z + 1)) world.setBlock(x, y, z + 1, Blocks.air);
		if (displaceIfPossible(world, x - 1, y, z)) world.setBlock(x - 1, y, z, Blocks.air);
		if (displaceIfPossible(world, x + 1, y, z)) world.setBlock(x + 1, y, z, Blocks.air);

		// Flow out to sides
		int[] dir = new int[4];
		dir[0] = getQuantaValue(world, x, y, z - 1);
		dir[1] = getQuantaValue(world, x, y, z + 1);
		dir[2] = getQuantaValue(world, x - 1, y, z);
		dir[3] = getQuantaValue(world, x + 1, y, z);

		for (int i = 0; i < 4; i++)
		{
			if (dir[i] < 0)
			{
				dir[i] = 16;
			}
		}

		int[][] dirs = { { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 } };
		int n = Math.abs(new Random().nextInt()) % 4;
		int lowest = n;
		int low = 16;
		for (int i = 0; i < 4; i++)
		{
			if (dir[((n + i) % 4)] < low)
			{
				lowest = ((n + i) % 4);
				low = dir[((n + i) % 4)];
			}
		}
		boolean odd = (quanta + low) % 2 == 1;
		int diff = (quanta + low + (odd ? 1 : 0)) / 2 - low;
		
		if (low < 16 && diff > getFlowDifferential() && quanta != low)
		{
			dir = dirs[lowest];
			Vector3 from = Vector3.getNewVectorFromPool().set(x, y, z);
			Vector3 to = Vector3.getNewVectorFromPool().set(from).add(dir[0], 0, dir[1]);

			int oldMetaHere = (quanta - 1);
			int oldMetaThere = (low - 1);

			int newMetaHere = oldMetaHere - diff;
			int newMetaThere = oldMetaThere + diff;

			flowInto(world, from, to, newMetaThere, newMetaHere);
			quanta = newMetaHere + 1;
			to.freeVectorFromPool();
			from.freeVectorFromPool();
		}

		if (original == quanta)// || Math.random() > 0.95)
		{
			Vector3 vec = Vector3.getNewVectorFromPool().set(x, y, z);
			doHardenTick(world, vec);
			vec.freeVectorFromPool();
		}
	}

	public int tryToFlowVerticallyInto(World world, int x, int y, int z, int amtToInput)
	{
		int otherY = y + densityDir;
		if (otherY < 0 || otherY >= world.getHeight())
		{
			world.setBlock(x, y, z, Blocks.air);
			return 0;
		}

		int amt = getQuantaValue(world, x, otherY, z);

		if (amt == quantaPerBlock) { return amtToInput; }

		if (amt >= 0)
		{
			Vector3 to = Vector3.getNewVectorFromPool().set(x, otherY, z);
			Vector3 from = Vector3.getNewVectorFromPool().set(x, y, z);
			to.y = otherY;
			int diff = 16 - (amt);

			diff = Math.min(diff, amtToInput);

			int oldMetaHere = (amtToInput - 1);
			int oldMetaThere = (amt - 1);

			int newMetaHere = oldMetaHere - diff;
			int newMetaThere = oldMetaThere + diff;

			int[] dir = new int[5];
			dir[0] = getQuantaValue(world, x, otherY, z - 1);
			dir[1] = getQuantaValue(world, x, otherY, z + 1);
			dir[2] = getQuantaValue(world, x - 1, otherY, z);
			dir[3] = getQuantaValue(world, x + 1, otherY, z);
			dir[4] = getQuantaValue(world, x, otherY, z);

			boolean sideways = false;
			for (int i = 0; i < 5; i++)
			{
				if (dir[i] > 0)
				{
					sideways = true;
				}
			}

			flowInto(world, from, to, newMetaThere, newMetaHere, !sideways);
			amtToInput -= diff;
			to.freeVectorFromPool();
			from.freeVectorFromPool();
			return amtToInput;
		}
		else
		{
			int density_other = getDensity(world, x, otherY, z);
			if (density_other == Integer.MAX_VALUE)
			{
				if (displaceIfPossible(world, x, otherY, z))
				{

					Vector3 to = Vector3.getNewVectorFromPool().set(x, otherY, z);
					Vector3 from = Vector3.getNewVectorFromPool().set(x, y, z);

					flowInto(world, from, to, amtToInput - 1, -1);

					to.freeVectorFromPool();
					from.freeVectorFromPool();

					return 0;
				}
				else
				{
					return amtToInput;
				}
			}

			if (densityDir < 0)
			{
				if (density_other < density) // then swap
				{
					BlockFluidBase block = (BlockFluidBase) world.getBlock(x, otherY, z);
					int otherData = world.getBlockMetadata(x, otherY, z);
					world.setBlock(x, otherY, z, this, amtToInput - 1, 3);
					world.setBlock(x, y, z, block, otherData, 3);
					return 0;
				}
			}
			else
			{
				if (density_other > density)
				{
					BlockFluidBase block = (BlockFluidBase) world.getBlock(x, otherY, z);
					int otherData = world.getBlockMetadata(x, otherY, z);
					world.setBlock(x, otherY, z, this, amtToInput - 1, 3);
					world.setBlock(x, y, z, block, otherData, 3);
					return 0;
				}
			}
			return amtToInput;
		}
	}

	/* IFluidBlock */
	@Override
	public FluidStack drain(World world, int x, int y, int z, boolean doDrain)
	{
		if (doDrain)
		{
			world.setBlock(x, y, z, Blocks.air);
		}

		return new FluidStack(getFluid(),
				MathHelper.floor_float(getQuantaPercentage(world, x, y, z) * FluidContainerRegistry.BUCKET_VOLUME));
	}

	@Override
	public boolean canDrain(World world, int x, int y, int z)
	{
		return true;
	}

	/** Attempt to displace the block at (x, y, z), return true if it was
	 * displaced. */
	public boolean displaceIfPossible(World world, int x, int y, int z)
	{
		if (world.getBlock(x, y, z).isAir(world, x, y, z)) { return true; }

		Block block = world.getBlock(x, y, z);
		if (block == this) { return false; }
		if (defaultReplacements.contains(block)) { return true; }
		if (displacements.containsKey(block))
		{
			if (displacements.get(block))
			{
				block.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
				return true;
			}
			return false;
		}

		Material material = block.getMaterial();
		if (material.blocksMovement() || material == Material.portal) { return false; }

		int density = getDensity(world, x, y, z);
		if (density == Integer.MAX_VALUE)
		{
			block.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
			return true;
		}

		if (this.density > density)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public void flowInto(World world, Vector3 from, Vector3 to, int metaTo, int metaFrom)
	{
		flowInto(world, from, to, metaTo, metaFrom, false);
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
		if (instant)
		{
			to.setBlock(world, this, metaTo, 2);
			world.scheduledUpdatesAreImmediate = true;
			this.updateTick(world, to.intX(), to.intY(), to.intZ(), world.rand);
			world.scheduledUpdatesAreImmediate = false;
			if (metaFrom >= 0)
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
			to.setBlock(world, this, metaTo, 3);

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

	/** Checks if the block is a solid face on the given side, used by placement
	 * logic.
	 *
	 * @param world
	 *            The current world
	 * @param x
	 *            X Position
	 * @param y
	 *            Y position
	 * @param z
	 *            Z position
	 * @param side
	 *            The side to check
	 * @return True if the block is solid on the specified side. */
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		int meta = world.getBlockMetadata(x, y, z);
		if (getFluid().getViscosity() != Integer.MAX_VALUE) return false;

		if (meta == 15) return true;
		return side == ForgeDirection.DOWN;
	}

	public AxisAlignedBB[] getBoxes(World worldObj, int x, int y, int z)
	{
		double[] heights = getCornerHeights(worldObj, x, y, z);

		double hN = (heights[0] + heights[3]) / 2;
		double hS = (heights[1] + heights[2]) / 2;
		double hE = (heights[2] + heights[3]) / 2;
		double hW = (heights[0] + heights[1]) / 2;

		double hM = (hN + hS + hE + hW) / 4;

		// AxisAlignedBB M = AxisAlignedBB.getBoundingBox(0.25, 0, 0.25, 0.75,
		// hM, 0.75).offset(x, y, z);

		AxisAlignedBB NW = AxisAlignedBB.getBoundingBox(0.0, 0, 0.0, 0.25, heights[0], 0.25).offset(x, y, z);
		AxisAlignedBB NW1 = AxisAlignedBB.getBoundingBox(0.25, 0, 0.0, 0.5, hN, 0.25).offset(x, y, z);
		AxisAlignedBB NW2 = AxisAlignedBB.getBoundingBox(0.0, 0, 0.25, 0.25, hW, 0.5).offset(x, y, z);
		AxisAlignedBB NW3 = AxisAlignedBB.getBoundingBox(0.25, 0, 0.25, 0.5, hM, 0.5).offset(x, y, z);

		AxisAlignedBB NE = AxisAlignedBB.getBoundingBox(0.75, 0, 0.0, 1.0, heights[3], 0.25).offset(x, y, z);
		AxisAlignedBB NE1 = AxisAlignedBB.getBoundingBox(0.75, 0, 0.25, 1.0, hE, 0.5).offset(x, y, z);
		AxisAlignedBB NE2 = AxisAlignedBB.getBoundingBox(0.5, 0, 0.0, 0.75, hN, 0.25).offset(x, y, z);
		AxisAlignedBB NE3 = AxisAlignedBB.getBoundingBox(0.5, 0, 0.25, 0.75, hM, 0.5).offset(x, y, z);

		AxisAlignedBB SW = AxisAlignedBB.getBoundingBox(0.0, 0, 0.75, 0.25, heights[1], 1.0).offset(x, y, z);
		AxisAlignedBB SW1 = AxisAlignedBB.getBoundingBox(0.25, 0, 0.75, 0.5, hS, 1.0).offset(x, y, z);
		AxisAlignedBB SW2 = AxisAlignedBB.getBoundingBox(0.0, 0, 0.5, 0.25, hW, 0.75).offset(x, y, z);
		AxisAlignedBB SW3 = AxisAlignedBB.getBoundingBox(0.25, 0, 0.5, 0.5, hM, 0.75).offset(x, y, z);

		AxisAlignedBB SE = AxisAlignedBB.getBoundingBox(0.75, 0, 0.75, 1.0, heights[2], 1.0).offset(x, y, z);
		AxisAlignedBB SE1 = AxisAlignedBB.getBoundingBox(0.75, 0, 0.5, 1.0, hE, 0.75).offset(x, y, z);
		AxisAlignedBB SE2 = AxisAlignedBB.getBoundingBox(0.5, 0, 0.75, 0.75, hS, 1.0).offset(x, y, z);
		AxisAlignedBB SE3 = AxisAlignedBB.getBoundingBox(0.5, 0, 0.5, 0.75, hM, 0.75).offset(x, y, z);

		// return new AxisAlignedBB[] {M, NW, NE, SW, SE};
		return new AxisAlignedBB[] { NW, NW1, NW2, NW3, NE, NE1, NE2, NE3, SW, SW1, SW2, SW3, SE, SE1, SE2, SE3 };
	}

	public double[] getCornerHeights(IBlockAccess world, int x, int y, int z)
	{
		double heightNW, heightSW, heightSE, heightNE;

		float flow11 = getFluidHeightForCollision(world, x, y, z);

		if (flow11 != 1)
		{
			float flow00 = getFluidHeightForCollision(world, x - 1, y, z - 1);
			float flow01 = getFluidHeightForCollision(world, x - 1, y, z);
			float flow02 = getFluidHeightForCollision(world, x - 1, y, z + 1);
			float flow10 = getFluidHeightForCollision(world, x, y, z - 1);
			float flow12 = getFluidHeightForCollision(world, x, y, z + 1);
			float flow20 = getFluidHeightForCollision(world, x + 1, y, z - 1);
			float flow21 = getFluidHeightForCollision(world, x + 1, y, z);
			float flow22 = getFluidHeightForCollision(world, x + 1, y, z + 1);

			heightNW = getFluidHeightAverage(new float[] { flow00, flow01, flow10, flow11 });
			heightSW = getFluidHeightAverage(new float[] { flow01, flow02, flow12, flow11 });
			heightSE = getFluidHeightAverage(new float[] { flow12, flow21, flow22, flow11 });
			heightNE = getFluidHeightAverage(new float[] { flow10, flow20, flow21, flow11 });
		}
		else
		{
			heightNW = flow11;
			heightSW = flow11;
			heightSE = flow11;
			heightNE = flow11;
		}

		return new double[] { heightNW, heightSW, heightSE, heightNE };
	}

	public float getFluidHeightAverage(float[] flow)
	{

		float total = 0;
		int count = 0;

		for (float aFlow : flow)
		{
			if (aFlow >= 1F) { return aFlow; }
			if (aFlow >= 0)
			{ // xTODO maybe revert back to >=0?
				total += aFlow;
				count++;
			}
		}
		return total / count;
	}

	public float getFluidHeightForCollision(IBlockAccess world, int x, int y, int z)
	{

		int meta = world.getBlockMetadata(x, y, z);
		Block id = world.getBlock(x, y, z);
		if (id instanceof BlockFluid)
		{
			if (world.getBlock(x, y + 1, z) instanceof BlockFluid) { return 1; }
			if (meta == getMaxRenderHeightMeta()) { return 1F; }
			return ((float) (meta + 1)) / 16;
		}
		return 0;
	}

	@Override
	public void addCollisionBoxesToList(World worldObj, int x, int y, int z, AxisAlignedBB aaBB, List list,
			Entity par7Entity)
	{

		int l = worldObj.getBlockMetadata(x, y, z);
		if (getFluid().getViscosity() == Integer.MAX_VALUE)
		{
			float f = 0.0625F;
			// System.out.println(l*f);
			if (aaBB.intersectsWith(AxisAlignedBB.getBoundingBox(0, 0, 0, 1, l * f, 1).offset(x, y, z)))
			{
				list.add(AxisAlignedBB.getBoundingBox(0, 0, 0, 1, l * f, 1).offset(x, y, z));
			}
			return;
		}

		if (worldObj.getBlockMetadata(x, y, z) == 15)
		{
			if (aaBB.intersectsWith(AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1).offset(x, y, z)))
			{
				list.add(AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1).offset(x, y, z));
			}
			return;
		}
		for (AxisAlignedBB box : getBoxes(worldObj, x, y, z))
		{
			if (aaBB.intersectsWith(box))
			{
				list.add(box);
			}
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int x, int y, int z)
	{
		int meta = par1World.getBlockMetadata(x, y, z);
		int l = par1World.getBlockMetadata(x, y, z);
		float f = 0.0625F;

		if (getFluid().getViscosity() == Integer.MAX_VALUE) { return AxisAlignedBB.getBoundingBox(0, 0, 0, 1, f * l, 1)
				.getBoundingBox(0, 0, 0, 1, f * l, 1).offset(x, y, z); }

		if (!((Vector3.getNewVectorFromPool().set(x, y - 1, z)).isFluid(par1World)
				|| par1World.isAirBlock(x, y - 1, z)))
		{
			return AxisAlignedBB.getBoundingBox((double) x + this.minX, (double) y + this.minY, (double) z + this.minZ,
					(double) x + this.maxX, (double) ((float) y + (float) l * f), (double) z + this.maxZ);
		}
		else
		{
			return AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0).offset(x, y, z);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	/** Returns the bounding box of the wired rectangular prism to render. */
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World worldObj, int x, int y, int z)
	{

		if (getFluid().getViscosity() == Integer.MAX_VALUE)
		{
			int l = worldObj.getBlockMetadata(x, y, z) + 1;
			float f = 0.0625F;
			return AxisAlignedBB.getBoundingBox(0, 0, 0, 1, f * l, 1).offset(x, y, z);
		}
		if (ThutCore.proxy
				.getPlayer() == null) { return AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1).offset(x, y, z); }

		Vector3 here = Vector3.getNewVectorFromPool().set(x, y, z);

		Vector3 playerloc = Vector3.getNewVectorFromPool().set(ThutCore.proxy.getPlayer());

		Vector3 hit = playerloc.findNextSolidBlock(worldObj,
				Vector3.getNewVectorFromPool().set(ThutCore.proxy.getPlayer().getLookVec()), 5);
		if (hit != null)
		{
			hit.freeVectorFromPool();
			here.freeVectorFromPool();
			playerloc.freeVectorFromPool();
			return AxisAlignedBB.getBoundingBox(0, 0, 0, 1, Math.min(0.0625 + hit.y - y, 1), 1).offset(x, y, z);
		}
		return AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1).offset(x, y, z);
	}

	/*
	 * Updates the blocks bounds based on its current state. Args: world, x, y,
	 * z
	 */
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		// if (getFluid().getViscosity() == Integer.MAX_VALUE)
		{
			int l = world.getBlockMetadata(x, y, z);
			float f = ((l + 1) / 16f);
			setBlockBounds(0, 0, 0, 1, f, 1);
		}
	}

	/** Sets the block's bounds for rendering it as an item */
	public void setBlockBoundsForItemRender()
	{
		setBlockBounds(0, 0, 0, 1, 1, 1);
	}

	/** Returns true if the given side of this block type should be rendered, if
	 * the adjacent block is at the given coordinates. Args: blockAccess, x, y,
	 * z, side */
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess p_149646_1_, int p_149646_2_, int p_149646_3_, int p_149646_4_,
			int p_149646_5_)
	{
		return p_149646_5_ == 0
				&& this.minY > 0.0D
						? true
						: (p_149646_5_ == 1
								&& this.maxY < 1.0D
										? true
										: (p_149646_5_ == 2 && this.minZ > 0.0D ? true
												: (p_149646_5_ == 3 && this.maxZ < 1.0D ? true
														: (p_149646_5_ == 4 && this.minX > 0.0D ? true
																: (p_149646_5_ == 5 && this.maxX < 1.0D ? true
																		: !p_149646_1_
																				.getBlock(p_149646_2_, p_149646_3_,
																						p_149646_4_)
																				.isOpaqueCube())))));
	}

	public void doHardenTick(World worldObj, Vector3 vec)
	{
	}

	/** The type of render function that is called for this block */
	@Override
	public int getRenderType()
	{
		return 0;// s getFluid().getViscosity() == Integer.MAX_VALUE ? 0 :
					// FluidRegistry.renderIdFluid;
	}
	
	public int getFlowDifferential()
	{
		return 0;
	}

	public static class WetConcrete extends Material
	{

		public WetConcrete(MapColor par1MapColor)
		{
			super(par1MapColor);
		}

		/** Returns if blocks of these materials are liquids. */
		public boolean isLiquid()
		{
			return true;
		}

		public boolean isSolid()
		{
			return false;
		}

		public boolean isReplaceable()
		{
			return true;
		}

		public boolean isOpaque()
		{
			return true;
		}

		/** Returns if this material is considered solid or not */
		public boolean blocksMovement()
		{
			return false;
		}
	}
}
