package thut.world.common.blocks.fluids;

import static net.minecraftforge.common.util.ForgeDirection.DOWN;
import static net.minecraftforge.common.util.ForgeDirection.EAST;
import static net.minecraftforge.common.util.ForgeDirection.NORTH;
import static net.minecraftforge.common.util.ForgeDirection.SOUTH;
import static net.minecraftforge.common.util.ForgeDirection.UP;
import static net.minecraftforge.common.util.ForgeDirection.WEST;
import static net.minecraft.init.Blocks.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import thut.api.ThutBlocks;
import thut.api.explosion.Vector3;
import thut.world.client.ClientProxy;
import thut.world.client.render.RenderFluid;
import thut.world.common.WorldCore;
import thut.world.common.corehandlers.ConfigHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.*;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.*;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.RenderBlockFluid;

public abstract class BlockFluid extends BlockFluidBase {

	private Random r = new Random();
	public boolean solidifiable = false;
	public double rate = 0.9;
	public boolean wanderer = false;
	public boolean hasFloatState = false;
	public boolean solid = false;
	public int placeamount = 1;
	public boolean stampable = false;
	public int maxMeta = 15;
	public static int renderID;

	private static boolean init = true;
	public static List<Block> defaultReplacements = new ArrayList<Block>();

	static void init() {
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
		
		 for(Block b: ThutBlocks.getAllBlocks())
		 {
		 if(b instanceof BlockFlower||b instanceof BlockSign|| b instanceof
		 BlockRedstoneTorch
		 ||b instanceof BlockLeaves|| b instanceof BlockRedstoneComparator||b
		 instanceof BlockStem
		 ||b instanceof BlockCarpet
		 ||b.getMaterial().isReplaceable())
		 defaultReplacements.add(b);
		 }
	}

	public static IIcon[] iconArray = new IIcon[16];

	public static double SOLIDIFY_CHANCE = 0.0004;

	public static Map<Block, FluidInfo> fluidBlocks = new HashMap<Block, FluidInfo>();

	public BlockFluid(Fluid fluid, Material par2) {
		super(fluid, par2);
		FluidRegistry.registerFluid(fluid);
		setQuantaPerBlock(16);
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		this.setLightOpacity(255);
		if (init) {
			init = false;
			init();
		}
	}

	public void setSolid() {
		if (ConfigHandler.paneFix)
			this.opaque = true;
		solid = true;

	}
	
    /**
     * Should block use the brightest neighbor light value as its own
     */
	@Override
    public boolean getUseNeighborBrightness()
    {
        return true;
    }

	/**
	 * Called when a block is placed using its ItemBlock. Args: World, X, Y, Z,
	 * side, hitX, hitY, hitZ, block metadata
	 */
	public int onBlockPlaced(World worldObj, int x, int y, int z, int side,
			float hitX, float hitY, float hitZ, int meta) {
		return (placeamount - 1);
	}

	public static Fluid getFluidType(String type) {
		if (type.equals("solidRock")) {
			return new Fluid("solidRock").setDensity(5000).setViscosity(
					Integer.MAX_VALUE);
		}
		if (type.equals("liquidRock")) {
			return new Fluid("liquidRock").setDensity(4000).setViscosity(2000);
		}
		if (type.equals("REliquidRock")) {
			return new Fluid("REliquidRock").setDensity(Integer.MAX_VALUE)
					.setViscosity(2000);
		}
		if (type.equals("moltenRock")) {
			return new Fluid("moltenRock").setDensity(5000).setViscosity(2500);
		}
		if (type.equals("dust")) {
			return new Fluid("dust").setDensity(900).setViscosity(3000);
		}
		if (type.equals("wetdust")) {
			return new Fluid("wetdust").setDensity(2000).setViscosity(1500);
		}

		return new Fluid("genericRock").setDensity(4000).setViscosity(2000);
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		return 0;
	}

	public boolean isInWater(IBlockAccess world, int x, int y, int z) {
		Vector3 vec = new Vector3(x, y, z);
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			Block id = vec.getBlock(world, side);
			if (side != DOWN && (id == water || id == flowing_water)) {
				return true;
			}
		}
		int[][] corners = { { 1, 1 }, { -1, 1 }, { 1, -1 }, { -1, -1 } };
		for (int[] i : corners) {
			Block id = world.getBlock(x + i[0], y, z + i[1]);
			if ((id == water || id == flowing_water)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Location aware and overrideable version of the lightOpacity array, return
	 * the number to subtract from the light value when it passes through this
	 * block.
	 * 
	 * This is not guaranteed to have the tile entity in place before this is
	 * called, so it is Recommended that you have your tile entity call relight
	 * after being placed if you rely on it for light info.
	 * 
	 * @param world
	 *            The current world
	 * @param x
	 *            X Position
	 * @param y
	 *            Y Position
	 * @param z
	 *            Z position
	 * @return The amount of light to block, 0 for air, 255 for fully opaque.
	 */
	@Override
	public int getLightOpacity(IBlockAccess world, int x, int y, int z) {
		if (isFloating(world, x, y, z)) {
			return 0;
		}
		if (world.getBlockMetadata(x, y, z) == 15)
			return 255;
		else
			return 0;
	}

	@Override
	public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world,
			int x, int y, int z) {
		return true;
	}

	@Override
	public boolean isNormalCube(IBlockAccess world, int x, int y, int z) {
		return world.getBlockMetadata(x, y, z) == 15;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World,
			int x, int y, int z) {
		int meta = par1World.getBlockMetadata(x, y, z);
		int l = par1World.getBlockMetadata(x, y, z);
		float f = 0.0625F;

		if (solid) {
			return AxisAlignedBB.getAABBPool().getAABB(0, 0, 0, 1, f * l, 1)
					.offset(x, y, z);
		}

		if (!((new Vector3(x, y - 1, z)).isFluid(par1World) || par1World
				.isAirBlock(x, y - 1, z))) {
			return AxisAlignedBB.getAABBPool().getAABB((double) x + this.minX,
					(double) y + this.minY, (double) z + this.minZ,
					(double) x + this.maxX,
					(double) ((float) y + (float) l * f),
					(double) z + this.maxZ);
		} else {
			return AxisAlignedBB.getAABBPool().getAABB(0, 0, 0, 0, 0, 0)
					.offset(x, y, z);
		}
	}

	public boolean isFloating(IBlockAccess world, int x, int y, int z) {
		Vector3 vec = new Vector3(x, y - 1, z);
		if (!hasFloatState)
			return false;

		return hasFloatState
				&& (vec.getBlock(world) instanceof BlockFluid && (vec
						.getBlockMetadata(world) != 15 || ((BlockFluid) vec
						.getBlock(world)).isFloating(world, x, y - 1, z)))
				|| vec.isAir(world);
	}

	/**
	 * Adds all intersecting collision boxes to a list. (Be sure to only add
	 * boxes to the list if they intersect the mask.) Parameters: World, X, Y,
	 * Z, mask, list, colliding entity
	 */
	@Override
	public void addCollisionBoxesToList(World worldObj, int x, int y, int z,
			AxisAlignedBB aaBB, List list, Entity par7Entity) {

		if (solid) {
			int l = worldObj.getBlockMetadata(x, y, z);
			float f = 0.0625F;
			// System.out.println(l*f);
			if (aaBB.intersectsWith(AxisAlignedBB.getBoundingBox(0, 0, 0, 1,
					l * f, 1).offset(x, y, z)))
				list.add(AxisAlignedBB.getBoundingBox(0, 0, 0, 1, l * f, 1)
						.offset(x, y, z));
			return;
		}

		if (hasFloatState && isFloating(worldObj, x, y, z))
			return;
		if (worldObj.getBlockMetadata(x, y, z) == 15) {
			if (aaBB.intersectsWith(AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1,
					1).offset(x, y, z)))
				list.add(AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1).offset(
						x, y, z));
			return;
		}
		for (AxisAlignedBB box : getBoxes(worldObj, x, y, z)) {
			if (aaBB.intersectsWith(box))
				list.add(box);
		}
	}

	public AxisAlignedBB[] getBoxes(World worldObj, int x, int y, int z) {
		double[] heights = getCornerHeights(worldObj, x, y, z);

		double hN = (heights[0] + heights[3]) / 2;
		double hS = (heights[1] + heights[2]) / 2;
		double hE = (heights[2] + heights[3]) / 2;
		double hW = (heights[0] + heights[1]) / 2;

		double hM = (hN + hS + hE + hW) / 4;

		// AxisAlignedBB M = AxisAlignedBB.getBoundingBox(0.25, 0, 0.25, 0.75,
		// hM, 0.75).offset(x, y, z);

		AxisAlignedBB NW = AxisAlignedBB.getBoundingBox(0.0, 0, 0.0, 0.25,
				heights[0], 0.25).offset(x, y, z);
		AxisAlignedBB NW1 = AxisAlignedBB.getBoundingBox(0.25, 0, 0.0, 0.5, hN,
				0.25).offset(x, y, z);
		AxisAlignedBB NW2 = AxisAlignedBB.getBoundingBox(0.0, 0, 0.25, 0.25,
				hW, 0.5).offset(x, y, z);
		AxisAlignedBB NW3 = AxisAlignedBB.getBoundingBox(0.25, 0, 0.25, 0.5,
				hM, 0.5).offset(x, y, z);

		AxisAlignedBB NE = AxisAlignedBB.getBoundingBox(0.75, 0, 0.0, 1.0,
				heights[3], 0.25).offset(x, y, z);
		AxisAlignedBB NE1 = AxisAlignedBB.getBoundingBox(0.75, 0, 0.25, 1.0,
				hE, 0.5).offset(x, y, z);
		AxisAlignedBB NE2 = AxisAlignedBB.getBoundingBox(0.5, 0, 0.0, 0.75, hN,
				0.25).offset(x, y, z);
		AxisAlignedBB NE3 = AxisAlignedBB.getBoundingBox(0.5, 0, 0.25, 0.75,
				hM, 0.5).offset(x, y, z);

		AxisAlignedBB SW = AxisAlignedBB.getBoundingBox(0.0, 0, 0.75, 0.25,
				heights[1], 1.0).offset(x, y, z);
		AxisAlignedBB SW1 = AxisAlignedBB.getBoundingBox(0.25, 0, 0.75, 0.5,
				hS, 1.0).offset(x, y, z);
		AxisAlignedBB SW2 = AxisAlignedBB.getBoundingBox(0.0, 0, 0.5, 0.25, hW,
				0.75).offset(x, y, z);
		AxisAlignedBB SW3 = AxisAlignedBB.getBoundingBox(0.25, 0, 0.5, 0.5, hM,
				0.75).offset(x, y, z);

		AxisAlignedBB SE = AxisAlignedBB.getBoundingBox(0.75, 0, 0.75, 1.0,
				heights[2], 1.0).offset(x, y, z);
		AxisAlignedBB SE1 = AxisAlignedBB.getBoundingBox(0.75, 0, 0.5, 1.0, hE,
				0.75).offset(x, y, z);
		AxisAlignedBB SE2 = AxisAlignedBB.getBoundingBox(0.5, 0, 0.75, 0.75,
				hS, 1.0).offset(x, y, z);
		AxisAlignedBB SE3 = AxisAlignedBB.getBoundingBox(0.5, 0, 0.5, 0.75, hM,
				0.75).offset(x, y, z);

		// return new AxisAlignedBB[] {M, NW, NE, SW, SE};
		return new AxisAlignedBB[] { NW, NW1, NW2, NW3, NE, NE1, NE2, NE3, SW,
				SW1, SW2, SW3, SE, SE1, SE2, SE3 };
	}

	public double[] getCornerHeights(IBlockAccess world, int x, int y, int z) {
		double heightNW, heightSW, heightSE, heightNE;

		float flow11 = getFluidHeightForCollision(world, x, y, z);

		if (flow11 != 1) {
			float flow00 = getFluidHeightForCollision(world, x - 1, y, z - 1);
			float flow01 = getFluidHeightForCollision(world, x - 1, y, z);
			float flow02 = getFluidHeightForCollision(world, x - 1, y, z + 1);
			float flow10 = getFluidHeightForCollision(world, x, y, z - 1);
			float flow12 = getFluidHeightForCollision(world, x, y, z + 1);
			float flow20 = getFluidHeightForCollision(world, x + 1, y, z - 1);
			float flow21 = getFluidHeightForCollision(world, x + 1, y, z);
			float flow22 = getFluidHeightForCollision(world, x + 1, y, z + 1);

			heightNW = getFluidHeightAverage(new float[] { flow00, flow01,
					flow10, flow11 });
			heightSW = getFluidHeightAverage(new float[] { flow01, flow02,
					flow12, flow11 });
			heightSE = getFluidHeightAverage(new float[] { flow12, flow21,
					flow22, flow11 });
			heightNE = getFluidHeightAverage(new float[] { flow10, flow20,
					flow21, flow11 });
		} else {
			heightNW = flow11;
			heightSW = flow11;
			heightSE = flow11;
			heightNE = flow11;
		}

		return new double[] { heightNW, heightSW, heightSE, heightNE };
	}

	public float getFluidHeightAverage(float[] flow) {

		float total = 0;
		int count = 0;

		for (int i = 0; i < flow.length; i++) {
			if (flow[i] >= 1F) {
				return flow[i];
			}
			if (flow[i] >= 0) { // TODO maybe revert back to >=0?
				total += flow[i];
				count++;
			}
		}
		return total / count;
	}

	public float getFluidHeightForCollision(IBlockAccess world, int x, int y,
			int z) {

		int meta = world.getBlockMetadata(x, y, z);
		Block id = world.getBlock(x, y, z);
		if (id instanceof BlockFluid) {
			if (world.getBlock(x, y + 1, z) instanceof BlockFluid) {
				// if (world.getBlock(x, y, z) == block.blockID) {
				// if (world.getBlock(x, y + 1, z) == block.blockID) {
				return 1;
			}
			if (meta == getMaxRenderHeightMeta()) {
				return 1F;
			}
			return ((float) (meta + 1)) / 16;
		}
		return 0;
	}

	/**
	 * Is this block (a) opaque and (b) a full 1m cube? This determines whether
	 * or not to render the shared face of two adjacent blocks and also whether
	 * the player can attach torches, redstone wire, etc to this block.
	 */
	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	public void setData() {
	}

	/**
	 * If this block doesn't render as an ordinary block it will return False
	 * (examples: signs, buttons, stairs, etc)
	 */
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public void onBlockAdded(World worldObj, int x, int y, int z) {
		worldObj.scheduleBlockUpdate(x, y, z, this, 10);
	}

	/**
	 * Sets the block's bounds for rendering it as an item
	 */
	@Override
	public void setBlockBoundsForItemRender() {
		this.setBoundsByMeta(15);
	}

	@Override
	@SideOnly(Side.CLIENT)
	/**
	 * Returns the bounding box of the wired rectangular prism to render.
	 */
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World worldObj, int x,
			int y, int z) {

		if (solid) {
			int l = worldObj.getBlockMetadata(x, y, z) + 1;
			float f = 0.0625F;
			return AxisAlignedBB.getAABBPool().getAABB(0, 0, 0, 1, f * l, 1)
					.offset(x, y, z);
		}
		if (WorldCore.proxy.getPlayer() == null) {
			return AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1).offset(x, y,
					z);
		}

		Vector3 here = new Vector3(x, y, z);

		Vector3 playerloc = new Vector3(WorldCore.proxy.getPlayer());

		Vector3 hit = playerloc.findNextSolidBlock(worldObj, new Vector3(
				WorldCore.proxy.getPlayer().getLookVec()), 5);
		if (hit != null)
			return AxisAlignedBB.getBoundingBox(0, 0, 0, 1,
					Math.min(0.0625 + hit.y - y, 1), 1).offset(x, y, z);
		return AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1).offset(x, y, z);
	}

	@SideOnly(Side.CLIENT)
	public EntityPlayer getPlayer() {
		return WorldCore.proxy.getPlayer();
	}

	/**
	 * Updates the blocks bounds based on its current state. Args: world, x, y,
	 * z
	 */
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess worldObj, int x, int y,
			int z) {
		this.setBoundsByMeta(worldObj.getBlockMetadata(x, y, z));
	}

	protected void setBoundsByMeta(int par1) {
		int j = par1;
		float f = (float) ((1 + j)) / 16.0F;
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, f, 1.0F);
	}

	@Override
	public boolean onBlockActivated(World worldObj, int x, int y, int z,
			EntityPlayer player, int side, float par7, float par8, float par9) {
		ItemStack item = player.getHeldItem();
		int meta = worldObj.getBlockMetadata(x, y, z);
		boolean ret = false;
		// System.out.println(this.isBlockNormalCube(worldObj, x, y,
		// z)+" "+this.getBlockBrightness(worldObj, x, y,
		// z)+" "+this.getLightOpacity(worldObj, x, y, z));
		if (item != null) {
			Block itemID = Block.getBlockFromItem(item.getItem());
			int itemMeta = item.getItemDamage();
			Block id = worldObj.getBlock(x, y, z);
			BlockFluid block = (BlockFluid) id;
			Vector3 vec = new Vector3(x, y, z);
			if (meta != 15 && itemID instanceof BlockFluid
					&& block.willCombine(itemID, vec, worldObj)) {
				ret = placedStack(worldObj, item, x, y, z,
						ForgeDirection.getOrientation(side), block, player);
			}
		}
		worldObj.scheduleBlockUpdate(x, y, z, worldObj.getBlock(x, y, z), 5);
		return ret;

	}

	// /////////////////////////////////////Fluid On Update
	// Stuff////////////////////////////////////////////////////////////

	@Override
	public void updateTick(World worldObj, int x, int y, int z,
			Random par5Random) {
		worldObj.theProfiler.startSection("fluid Blocks");
		if (!solid) {
			Vector3 here = new Vector3(x, y, z);
			doFluidTick(worldObj, here);
			doHardenTick(worldObj, here);
			if (isHardenable(worldObj, here))
				here.scheduleUpdate(worldObj);
		}
		worldObj.theProfiler.endSection();
	}

	public int tickRate(World worldObj) {
		return 20;
	}

	// //////////////////////////////////////Fluid Block Logic Below
	// Here////////////////////////////////////////////////////

	/**
	 * TODO More Optimization Checks if the block should spread to the side
	 * 
	 * @param worldObj
	 * @param x
	 * @param y
	 * @param z
	 */
	public boolean trySpread(World worldObj, Vector3 vec) {
		boolean moved = false;
		int viscosity = viscosity(vec.getBlock(worldObj));

		Block block = vec.getBlock(worldObj);

		if (!(block instanceof BlockFluid) || viscosity >= 15)// maxMeta)
		{
			return false;
		}

		ForgeDirection[] sides = { NORTH, SOUTH, EAST, WEST, };

		int n = sides.length;
		int i = r.nextInt(n);
		int highestMeta = 16;
		int k = 0;
		for (int j = 0; j < n; j++) {
			block = vec.getBlock(worldObj, sides[i]);
			int meta = vec.getBlockMetadata(worldObj, sides[i]);
			if (!(block instanceof BlockFluid))
				moved = moved || equalize(worldObj, vec, vec.offset(sides[i]));
			else if (meta < highestMeta) {
				highestMeta = meta;
				k = i;
			}
			i = (i + 1) % n;
		}

		if (highestMeta != 16)
			moved = moved || equalize(worldObj, vec, vec.offset(sides[k]));

		if (block instanceof BlockFluid && ((BlockFluid) block).wanderer) {
			int meta = vec.getBlockMetadata(worldObj);
			if (meta == 0 && viscosity == 0) {
				moved = moved || merge(worldObj, vec, vec.offset(sides[i]));
			}

		}
		return moved;
	}

	/**
	 * TODO More Optimization Checks if the block should fall down
	 * 
	 * @param par1World
	 * @param x
	 * @param y
	 * @param z
	 */
	public boolean tryFall(World worldObj, Vector3 vec){
    	Block id = vec.getBlock(worldObj);
    	if(!(fluidBlocks.containsKey(id))) return false;
    	int h = vec.intY();
    	Vector3 vec1 = vec.offset(DOWN);
    	Block id1 = vec1.getBlock(worldObj);
    	double dx=0,dz=0;
    	boolean combineDown = willCombine(id, vec1, worldObj);
    	
    	double density = ((BlockFluid)id).getFluid().getDensity();
    	
    	double factor = vec.y>80?1/(density*density/1E6):0;
    	
        boolean fallOff = willFallOffEdges(id);
        
        boolean falldown = true;
    	boolean fell = false;
    	
    	if(combineDown)
    	{
	    	
    		dx = getWind(worldObj, vec.x, vec.z).x*factor;
    		dz = getWind(worldObj, vec.x, vec.z).y*factor;
    		
	    	boolean combine;
	    	while(h>1)
	    	{
	    		vec1.set(vec.x+(int)(dx*(1+vec.y-h)), h-1, vec.z+(int)(dz*(1+vec.y-h)));
	    		dx = getWind(worldObj, vec.x+(int)(dx*(1+vec.y-h)), vec.z+(int)(dz*(1+vec.y-h))).x*factor;
	    		dz = getWind(worldObj, vec.x+(int)(dx*(1+vec.y-h)), vec.z+(int)(dz*(1+vec.y-h))).y*factor;
	    		id1 = vec1.getBlock(worldObj);
	    		
	    		combine = willCombine(id, vec1, worldObj);
	    		if(!(combine))
	    		{
	    			break;
	    		}
	    		h--;
	    	}
	    	fell = fell||merge(worldObj,vec, vec1.offset(UP));
    	}
        boolean trySide = false;
        if(!fell&&(fallOff))
        {
        	int[][]sides = {{1,0},{-1,0},{0,1},{0,-1}};//,{1,1},{-1,1},{1,-1},{-1,-1}};
        	
            int i = r.nextInt(sides.length);
            
            int lowestMeta = 16;
            int k = 0;
            int dh = 0;
            for(int j = 0; j<sides.length; j++){
            		h = vec.intY();
            		vec1.set(vec.x+sides[i][0], vec.y, vec.z+sides[i][1]);
            		
            		if(!willCombine(id, vec1, worldObj))
            			continue;
            		
                    Block idSideDown;
                    trySide = true;
                	while(h>0)
                	{
                		vec1.set(vec.x+sides[i][0], h-1, vec.z+sides[i][1]);
                		idSideDown = vec1.getBlock(worldObj);
                    	boolean combine = willCombine(id, vec1, worldObj);
                		if(!(combine))
                		{
                			
                			break;
                		}
                		h--;
                	}
                    int metaSideDown = vec1.getBlockMetadata(worldObj, UP);
                    if (metaSideDown < lowestMeta)
                    {
                		lowestMeta = metaSideDown;
                		k=i;
                		dh = h;
                	}
                    
                    i = (i+1)%sides.length;
            }
            if(trySide&&lowestMeta!=16&&h<=vec.intY()-1)
            {
            	vec1.set(vec.x+sides[k][0], h, vec.z+sides[k][1]);
            	fell = fell||fallOff?merge(worldObj, vec,vec1):equalize(worldObj, vec,vec1);
            }
        }
        if(!fell&&combineDown)
        {
        	fell = merge(worldObj, vec, vec.offset(DOWN));
        }
        return fell;
    }

	public boolean merge(World worldObj, Vector3 vec, Vector3 vec1){
    	if(vec.sameBlock(vec1)) return false;
    	 int metaOld, meta1Old;
         Block idOld, id1Old;
         Block id = idOld = vec.getBlock(worldObj);
         
         if(!fluidBlocks.containsKey(id))
     	 {
         	return false;
     	 }
         int meta = metaOld = vec.getBlockMetadata(worldObj);
         
         Block id1 = id1Old = vec1.getBlock(worldObj);
         int meta1 = meta1Old = vec1.getBlockMetadata(worldObj);
         
         boolean combine = willCombine(id, vec1, worldObj);
         if(combine)
         {
             boolean changed = false;
             Block block1 = id1;
             boolean oneOfUs = (block1 instanceof BlockFluid);

             Block idCombine = getCombineID(worldObj,id, id1);
             Block returnToID = getReturnTo(id);
             Block idHarden = getTurnToID(id);

         	if(!oneOfUs)meta1 = meta1Old = (-1);
         	if(vec1.isAir(worldObj))
         	{
         		vec1.setBlock(worldObj, idCombine, meta, 2);
         		id1 = idCombine;
         		changed = true;
         		vec.setBlock(worldObj, returnToID, 0, 2);
         		id = returnToID;
         		changed = true;
         	}
         	else
         	{
 	        	while(meta>=0&&meta1<15)
 	        	{
 	        		meta--;
 	        		meta1++;
 	        		changed = true;
 	        	}
 	        	if(meta == metaOld && meta1 == meta1Old && id == idOld && id1 == id1Old)
 	        	{
 	        		return false;
 	        	}
         		vec1.setBlock( worldObj, idCombine, Math.min(meta1, 15), 2);
         		if(meta==(-1))
         		{
         			vec.setBlock(worldObj,returnToID, 0, 2);
         			id = returnToID;
         			changed = true;
         		}
         		else
         		{
         			vec.setBlock(worldObj,id, meta, 2);
         			if(meta!=metaOld)
         				changed = true;
         		}
 	        	
         	}
         	if(changed)
         	{
         	//	tickSides(worldObj, vec.intX(), vec.intY(), vec.intZ(), 30);
         	//	vec.scheduleUpdate(worldObj);
         	//	vec1.scheduleUpdate(worldObj);
         	}
         	return changed;
         }
         return false;
    }

	public boolean equalize(World worldObj, Vector3 vec, Vector3 vec1){
    	if(vec.sameBlock(vec1)){return false;}
    	
    	int metaOld, meta1Old;
        Block id = vec.getBlock(worldObj);
        int meta = metaOld = vec.getBlockMetadata(worldObj);
        boolean additionalMeta = true;
        int spread = viscosity(id);
        int diff = hardenDifferential(id) + spread;
        if(meta==0||!fluidBlocks.containsKey(id))
        {
        	return false;
        }
        
        Block id1 = vec1.getBlock(worldObj);
        int meta1 = meta1Old = vec1.getBlockMetadata(worldObj);
        
        if(id==id1&&meta==meta1) return false;

        boolean combine = willCombine(id, vec1, worldObj);

        if(combine)
        {
            Block block1 = id1;

            boolean changed = false;
            boolean oneOfUs = (block1 instanceof BlockFluid);
        	
            Block idCombine = getCombineID(worldObj,id, id1);
            Block idHarden = getTurnToID(id);
        	
        	if(!oneOfUs)meta1 = meta1Old = (-1);
        	
        	while(meta>0&&meta1<15&&meta>=((id1==idHarden)?meta1+diff:meta1+spread)){//TODO
        		meta--;
        		meta1++;
        		changed = true;
        	}

        	if(meta == metaOld && meta1 == meta1Old)
        	{
        		return false;
        	}
    		vec1.setBlock( worldObj, idCombine, Math.min(15, meta1), 2);
    		vec.setBlock(worldObj, id, meta, 2);
         	if(changed)
         	{
         	//	tickSides(worldObj, vec.intX(), vec.intY(), vec.intZ(), 30);
         	//	vec.scheduleUpdate(worldObj);
         	//	vec1.scheduleUpdate(worldObj);
         	}
        	return changed;
        	
        }
        return false;
    }

	// ////////////////////////////////////////////////Item placement related
	// stuff///////////////////////////////////////////////

	public boolean placedStack(World worldObj, ItemStack stack, int x, int y, int z, ForgeDirection side, BlockFluid block, EntityPlayer player)
    {
    	Block id = worldObj.getBlock(x, y, z);
    	Block id1 = worldObj.getBlock(x+side.offsetX,y+side.offsetY, z+side.offsetZ);
    	
    	Block itemID = Block.getBlockFromItem(stack.getItem());
    	if(itemID == null) return false;
    	
    	int meta = worldObj.getBlockMetadata(x, y, z);
    	
    	int meta1 = worldObj.getBlockMetadata(x+side.offsetX,y+side.offsetY, z+side.offsetZ);
    	int placementamount = block.placeamount;
    	
    	int initialamount = meta;
    			
    	int newMeta = (placementamount + initialamount);
    	
    	int remainder = (placementamount - (15-meta));
    	
    	Block block1 = id1;
    	
    	if(id1==Blocks.air||block1.getMaterial().isReplaceable())
    	{
    		worldObj.setBlock(x,y, z, getCombineID(worldObj, itemID, id), Math.min(newMeta,15), 3);
    		if(newMeta<0)
    		worldObj.setBlock(x+side.offsetX,y+side.offsetY, z+side.offsetZ, itemID, remainder, 3);
    		
    		if(!player.capabilities.isCreativeMode)
		    	{
		    		stack.splitStack(1);
		    	}
    		return true;
    	}
    	
    	return false;
    }

	// ///////////////////////////////////////////////Checks used in the fluid
	// code////////////////////////////////////////////////////

	private boolean canHardenNextTo(Block idFrom, Block idTo) {
		if (fluidBlocks.get(idFrom) == null)
			return false;
		return fluidBlocks.get(idFrom).desiccants.containsKey(idTo);
	}

	public boolean combineFromList(Block itemID, Block id1) {
		if (fluidBlocks.get(itemID) == null)
			return false;
		return fluidBlocks.get(itemID).combinationBlocks.containsKey(id1);
	}

	public boolean willCombine(Block itemID, Vector3 vec, World worldObj) {
		Block id1 = vec.getBlock(worldObj);

		if (vec.getBlock(worldObj) instanceof BlockFluid
				&& vec.getBlockMetadata(worldObj) == 15) {
			return false;
		}

		boolean ret = combineFromList(itemID, id1);
		boolean flag = willCombineAir(itemID);

		if (flag) {
			ret = ret || vec.isAir(worldObj);
		}

		return ret;
	}

	private boolean willCombineAir(Block itemID) {
		if (fluidBlocks.get(itemID) == null)
			return false;
		return fluidBlocks.get(itemID).combinationBlocks.containsKey(Blocks.air);
	}

	private Block getReturnTo(Block id) {
		if (fluidBlocks.get(id) == null)
			return Blocks.air;
		return fluidBlocks.get(id).returnTo;
	}

	private boolean willFallOffEdges(Block id) {
		if (fluidBlocks.get(id) == null)
			return false;
		return fluidBlocks.get(id).fallOfEdge;
	}

	public Block getTurnToID(Block block) {
		if(fluidBlocks.get(block)!=null)
			return fluidBlocks.get(block).hardenTo;
		return null;
	}

	private int viscosity(Block block) {
		Random r = new Random();
		if (fluidBlocks.get(block) == null)
			return 0;
		int v = fluidBlocks.get(block).viscosity;
		int dv = (int) (fluidBlocks.get(block).randomFactor * Math.random());
		return v + dv;// Math.max(v+dv,15);
	}

	private int hardenDifferential(Block id) {
		Random r = new Random();
		if (fluidBlocks.get(id) == null)
			return 0;
		int dv = (int) (fluidBlocks.get(id).hardenDiff * Math.random());
		return dv;// Math.min(fluidBlocks.get(id)[0][3]+dv,15);
	}

	private Block getCombineID(World worldObj, Vector3 vec, Vector3 vec1) {
		Block idFrom = vec.getBlock(worldObj);
		if (fluidBlocks.get(idFrom) == null)
			return idFrom;
		Block idTo = vec1.getBlock(worldObj);
		Block combineID = fluidBlocks.get(idFrom).combinationBlocks.containsKey(idTo)?fluidBlocks.get(idFrom).combinationBlocks.get(idTo):idFrom;
		fluidBlocks.get(idFrom);
		
		return combineID;
	}

	public Block getCombineID(World worldObj, Block idFrom, Block idTo) {
		if (fluidBlocks.get(idFrom) == null)
			return idFrom;

		Block combineID = fluidBlocks.get(idFrom).combinationBlocks.containsKey(idTo)?fluidBlocks.get(idFrom).combinationBlocks.get(idTo):idFrom;

		
		return combineID;
	}

	// //////////////////////////////////////Fluid Block Logic Above Here,
	// special data Below///////////////////////////////////////////
	public int countSides(World worldObj, Vector3 vec, Block id) {
		int num = 0;
		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			if (vec.getBlock(worldObj, side) == id)
				num++;
		}
		return num;
	}

	public int canHarden(World worldObj, Vector3 vec) {
		int num = 0;
		Block id = vec.getBlock(worldObj);
		int val;
		if (fluidBlocks.get(id) == null)
			return 0;
		if (fluidBlocks.get(id).desiccants == null)
			return 0;
		for (Block i : fluidBlocks.get(id).desiccants.keySet()) {
			val = fluidBlocks.get(id).desiccants.get(i);
			num += val * countSides(worldObj, vec, i);
		}
		return num;
	}

	public boolean isHardenable(World worldObj, Vector3 vec) {

		Block id = vec.getBlock(worldObj);
		Vector3 down = vec.subtract(new Vector3(0, 1, 0));
		if (fluidBlocks.get(id) == null)
			return false;
		if (fluidBlocks.get(id).hardenTo == null)
			return false;

		Block idDown = down.getBlock(worldObj);

		boolean flag = fluidBlocks.get(id).combinationBlocks.containsKey(Blocks.air) && down.isAir(worldObj);

		if (flag || willCombine(id, down, worldObj)) {
			return false;
		}
		if (down.getBlock(worldObj) instanceof BlockFluid
				&& willCombine(id, down, worldObj)
				&& down.getBlockMetadata(worldObj) != 15) {
			return false;
		}

		return true;
	}
	
	// /////////////////////////////////////////////////////////////////Block
	// effects/ticking Stuff Above Here///////////////////////////////////////

	@Override
	@SideOnly(Side.CLIENT)
	/**
	 * Retrieves the block texture to use based on the display side. Args: iBlockAccess, x, y, z, side
	 */
	public IIcon getIcon(IBlockAccess par1IBlockAccess, int par2,
			int par3, int par4, int par5) {
		return this.blockIcon;
	}

	@Override
	public int quantityDropped(int meta, int fortune, Random random) {
		return (meta & 15) + 1;
	}


	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	@SideOnly(Side.CLIENT)

    /**
     * Returns true if the given side of this block type should be rendered, if the adjacent block is at the given
     * coordinates.  Args: blockAccess, x, y, z, side
     */
    public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int x, int y, int z, int dir)
    {

    	ForgeDirection side = ForgeDirection.getOrientation(dir);
    	
    	Block id1 = par1IBlockAccess.getBlock(x, y, z);

    	int meta = par1IBlockAccess.getBlockMetadata(x-side.offsetX, y-side.offsetX, z-side.offsetX);
    	int metaThere =  par1IBlockAccess.getBlockMetadata(x, y, z);
    	
    	if(id1 instanceof BlockFluid&&metaThere!=15)
    		return true;
    	

    	Block id = par1IBlockAccess.getBlock(x, y, z);
    	
    	if(id instanceof BlockFluid && ((BlockFluid)id).isFloating(par1IBlockAccess, x, y, z))
    	{
    		return true;
    	}
    	
    	if(meta != 15)
    	{
    		return true;
    	}
    	
    	int metaDown = par1IBlockAccess.getBlockMetadata(x-side.offsetX, y-side.offsetX-1, z-side.offsetX);
    	
    	if(metaDown!=15)
    		return true;
    	
    	if(id1.isOpaqueCube()&&meta==15)
    	{
    		return false;
    	}
    	
    	Block block1 = id1;
    	
    	
    	if(block1 instanceof BlockFluid)
    	{
	    	int meta1 = par1IBlockAccess.getBlockMetadata(x, y, z);
	    	if(((BlockFluid)block1).hasFloatState) return true;
	    	if(meta==15&&meta1==15)
	    	{
	    		return false;
	    	}
    	}
        return true;
        //*/
    }

	/**
	 * Checks if the block is a solid face on the given side, used by placement
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
	 * @return True if the block is solid on the specified side.
	 */
	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		int meta = world.getBlockMetadata(x, y, z);
		return (meta == 15) || (side == UP && solid);
	}

	/**
	 * Returns Returns true if the given side of this block type should be
	 * rendered (if it's solid or not), if the adjacent block is at the given
	 * coordinates. Args: blockAccess, x, y, z, side
	 */
	@Override
	public boolean isBlockSolid(IBlockAccess world, int x, int y, int z,
			int par5) {

		ForgeDirection side = ForgeDirection.getOrientation(par5);

		return world.getBlockMetadata(x - side.offsetX, y - side.offsetY, z
				- side.offsetZ) == 15;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////

	public void doFluidTick(World worldObj, Vector3 vec) {
		if (!worldObj.isRemote && vec.getBlock(worldObj) instanceof BlockFluid) {
			tryFall(worldObj, vec);
			//System.out.println(vec.offset(DOWN).getBlock(worldObj)+" "+fluidBlocks.get(this).combinationBlocks);
			trySpread(worldObj, vec);
		}
	}

	public void doHardenTick(World worldObj, Vector3 vec) {
		if (!isHardenable(worldObj, vec))
			return;
		int num = canHarden(worldObj, vec);
	//	System.out.println(num+" "+(1 - (SOLIDIFY_CHANCE * num))+" "+getTurnToID(vec.getBlock(worldObj)));
		if (!worldObj.isRemote && Math.random() > (1 - (SOLIDIFY_CHANCE * num))) {
			vec.setBlock(worldObj, getTurnToID(vec.getBlock(worldObj)),
					vec.getBlockMetadata(worldObj), 2);
		}
	}

	public void tickSides(World worldObj, int x, int y, int z, int rate) {
		int[][] sides = { { 1, 0, 0 }, { -1, 0, 0 }, { 0, 0, 1 }, { 0, 0, -1 },
				{ 0, 1, 0 }, { 0, -1, 0 } };
		for (int i = 0; i < 6; i++) {
			Block id = worldObj.getBlock(x + sides[i][0], y + sides[i][1], z
					+ sides[i][2]);
			Block blocki = id;

			if (blocki instanceof BlockFluid
					&& ((BlockFluid) blocki).solidifiable || id == water
					|| id == lava || id == flowing_lava || id == flowing_water) {
				worldObj.scheduleBlockUpdate(x + sides[i][0], y + sides[i][1],
						z + sides[i][2], id, rate);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	/**
	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	 */
	public IIcon getIcon(int par1, int par2) {
		return blockIcon;
	}

	/**
	 * Return true from this function if the player with silk touch can harvest
	 * this block directly, and not it's normal drops.
	 * 
	 * @param world
	 *            The world
	 * @param player
	 *            The player doing the harvesting
	 * @param x
	 *            X Position
	 * @param y
	 *            Y Position
	 * @param z
	 *            Z Position
	 * @param metadata
	 *            The metadata
	 * @return True if the block can be directly harvested using silk touch
	 */
	public boolean canSilkHarvest(World world, EntityPlayer player, int x,
			int y, int z, int metadata) {
		return true;
	}

	@Override
	public FluidStack drain(World world, int x, int y, int z, boolean doDrain) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canDrain(World world, int x, int y, int z) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getQuantaValue(IBlockAccess world, int x, int y, int z) {
		return world.getBlockMetadata(x, y, z) + 1;
	}

	@Override
	public boolean canCollideCheck(int meta, boolean fullHit) {
		return true;
	}

	@Override
	public int getMaxRenderHeightMeta() {
		return 15;
	}

	@Override
	public int getRenderType() {

		return solid ? 0 : RenderBlockFluid.instance.getRenderId();// renderID;
	}

	/**
	 * Determines if this block should render in this pass.
	 * 
	 * @param pass
	 *            The pass in question
	 * @return True to render
	 */
	public boolean canRenderInPass(int pass) {
		ClientProxy.renderPass = pass;
		if (solid)
			return pass == 0;

		return true;
	}

	public int getViscosity() {
		return getFluid().getViscosity();
	}

	public static double getFlowDirection(IBlockAccess world, int x, int y,
			int z) {

		Block block = world.getBlock(x, y, z);

		if (!(world.getBlock(x, y, z) instanceof BlockFluidBase)) {
			return -1000.0;
		}
		Vec3 vec = ((BlockFluidBase) block).getFlowVector(world, x, y, z);
		return vec.xCoord == 0.0D && vec.zCoord == 0.0D ? -1000.0D : Math
				.atan2(vec.zCoord, vec.xCoord) - Math.PI / 2D;
	}

	public static class FluidInfo {

		/**
		 * The block that this returns to when it hits -1 meta
		 */
		public Block returnTo = Blocks.air;
		/**
		 * The block this hardens to, null if it doesn't harden
		 */
		public Block hardenTo;
		public boolean fallOfEdge = false;
		public int viscosity = 0;
		public int randomFactor = 0;
		public int hardenDiff = 1;
		//public List<Block> willReplace = new ArrayList<Block>();
		public Map<Block, Integer> desiccants = new HashMap<Block, Integer>();
		/**
		 * key = flow into block, object = combination result.
		 */
		public Map<Block, Block> combinationBlocks = new HashMap<Block, Block>();

	}

	public static class WetConcrete extends Material {

		public WetConcrete(MapColor par1MapColor) {
			super(par1MapColor);
		}

		/**
		 * Returns if blocks of these materials are liquids.
		 */
		public boolean isLiquid() {
			return true;
		}

		public boolean isSolid() {
			return false;
		}

		public boolean isReplaceable() {
			return true;
		}

		public boolean isOpaque() {
			return true;
		}

		/**
		 * Returns if this material is considered solid or not
		 */
		public boolean blocksMovement() {
			return false;
		}
	}
	
	public static Vector3 getWind(World worldObj, double x, double z){
		double[] tempWind = new double[] {0,0};
		double windFactor = 1;
		double frequencyFactor = 0.00015;
		tempWind[0] = windFactor*(sin(frequencyFactor*((x/16)+worldObj.getTotalWorldTime()))+
				sin(frequencyFactor*((x/16)+worldObj.getTotalWorldTime())*frequencyFactor*((x/16)+worldObj.getTotalWorldTime()))+
				sin(frequencyFactor*worldObj.getTotalWorldTime()*(x/16))*cos(frequencyFactor*worldObj.getTotalWorldTime()*(x/16))+
				sin(frequencyFactor*worldObj.getTotalWorldTime()*(x/16))*cos(frequencyFactor*worldObj.getTotalWorldTime()*(x/16))*
				sin(frequencyFactor*worldObj.getTotalWorldTime()*(x/16))*cos(frequencyFactor*worldObj.getTotalWorldTime()*(x/16)));
	
		tempWind[1] = windFactor*(cos(frequencyFactor*((z/16)+worldObj.getTotalWorldTime()))+
				cos(frequencyFactor*((z/16)+worldObj.getTotalWorldTime())*frequencyFactor*((z/16)+worldObj.getTotalWorldTime()))+
				sin(frequencyFactor*((z/16)+worldObj.getTotalWorldTime()))*cos(frequencyFactor*((z/16)+worldObj.getTotalWorldTime()))+
				sin(frequencyFactor*((z/16)+worldObj.getTotalWorldTime()))*cos(frequencyFactor*((z/16)+worldObj.getTotalWorldTime()))*
				sin(frequencyFactor*((z/16)+worldObj.getTotalWorldTime()))*cos(frequencyFactor*((z/16)+worldObj.getTotalWorldTime())));
		Vector3 wind = new Vector3(tempWind[0], tempWind[1], 0);
		return wind;
	}
	
	
    private static double cos(double d) {
		return MathHelper.cos((float)d);
	}

	private static double sin(double d) {
		return MathHelper.sin((float)d);
	}

}