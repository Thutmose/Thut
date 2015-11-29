package thut.core.common.blocks;

import static net.minecraft.init.Blocks.activator_rail;
import static net.minecraft.init.Blocks.carrots;
import static net.minecraft.init.Blocks.detector_rail;
import static net.minecraft.init.Blocks.fire;
import static net.minecraft.init.Blocks.golden_rail;
import static net.minecraft.init.Blocks.lever;
import static net.minecraft.init.Blocks.potatoes;
import static net.minecraft.init.Blocks.rail;
import static net.minecraft.init.Blocks.reeds;
import static net.minecraft.init.Blocks.snow;
import static net.minecraft.init.Blocks.torch;
import static net.minecraft.init.Blocks.vine;
import static net.minecraft.init.Blocks.waterlily;
import static net.minecraft.init.Blocks.web;
import static net.minecraft.init.Blocks.wheat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockRedstoneComparator;
import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockStem;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.PropertyFloat;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.api.ThutBlocks;
import thut.api.block.IHardenableFluid;
import thut.api.block.IViscousFluid;
import thut.api.block.PropertyInteger;
import thut.api.maths.Vector3;
import thut.core.common.blocks.tileentity.TileEntityBlockFluid;

@SuppressWarnings("rawtypes")
public class BlockFluid extends BlockFluidFinite implements IViscousFluid
{
    public static final PropertyInteger COLOUR = new PropertyInteger("colour");

    public static final IUnlistedProperty[] FLUID_RENDER_PROPS;

    static
    {
        ImmutableList.Builder<IUnlistedProperty> builder = ImmutableList.builder();
        builder.add(FLOW_DIRECTION);
        for (int i = 0; i < 4; i++)
        {
            LEVEL_CORNERS[i] = new PropertyFloat("level_corner_" + i);
            builder.add(LEVEL_CORNERS[i]);
        }
        builder.add(COLOUR);
        FLUID_RENDER_PROPS = builder.build().toArray(new IUnlistedProperty[0]);
    }

    public boolean    solidifiable  = false;
    public double     rate          = 0.9;
    public boolean    wanderer      = false;
    public boolean    hasFloatState = false;
    public boolean    solid         = false;
    public boolean    fluid         = true;
    public int        placeamount   = 1;
    public boolean    stampable     = false;
    public int        maxMeta       = 15;
    public static int renderID;
    protected int     tickrate      = 20;
    protected Block   hardenTo      = null;

    private static boolean    init                = true;
    public static List<Block> defaultReplacements = new ArrayList<Block>();

    static void init()
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

        for (Block b : ThutBlocks.getAllBlocks())
        {
            if (b instanceof BlockFlower || b instanceof BlockSign || b instanceof BlockRedstoneTorch
                    || b instanceof BlockLeaves || b instanceof BlockRedstoneComparator || b instanceof BlockStem
                    || b instanceof BlockCarpet || b.getMaterial().isReplaceable())
                defaultReplacements.add(b);
        }
    }

    public static double SOLIDIFY_CHANCE = 0.0004;

    public BlockFluid(Fluid fluid, Material par2)
    {
        super(fluid, par2);
        setQuantaPerBlock(16);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        if (fluid.getViscosity() == Integer.MAX_VALUE) this.setLightOpacity(255);
        else this.setLightOpacity(0);
        this.fluid = true;
        this.placeamount = 16;
        if (init)
        {
            init = false;
            init();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass)
    {
        int red = 0x00010000;
        int green = 0x00000100;
        int blue = 0x00000001;
        int alpha = 0xFF000000;

        red *= 255;
        green *= 255;
        blue *= 255;

        return red + green + blue + alpha;
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ,
            int meta, EntityLivingBase placer)
    {
        int placeamount = 1;
        if (getFluid().getViscosity() < Integer.MAX_VALUE) placeamount = 16;
        IBlockState state = this.getDefaultState().withProperty(LEVEL, placeamount - 1);
        return state;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumFacing side, float hitX, float hitY, float hitZ)
    {
        System.out.println(state);
        return false;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        if (getFluid().getViscosity() < Integer.MAX_VALUE) super.onBlockAdded(worldIn, pos, state);

        TileEntity te = worldIn.getTileEntity(pos);

        if (!(te instanceof TileEntityBlockFluid)) return;

        float flow = (float) getFlowDirection(worldIn, pos);
        float[][] height = new float[3][3];
        float[][] corner = new float[2][2];
        height[1][1] = getFluidHeightForRender(worldIn, pos);
        if (height[1][1] == 1)
        {
            for (int i = 0; i < 2; i++)
            {
                for (int j = 0; j < 2; j++)
                {
                    corner[i][j] = 1;
                }
            }
        }
        else
        {
            for (int i = 0; i < 3; i++)
            {
                for (int j = 0; j < 3; j++)
                {
                    if (i != 1 || j != 1)
                    {
                        height[i][j] = getFluidHeightForRender(worldIn, pos.add(i - 1, 0, j - 1));
                    }
                }
            }
            for (int i = 0; i < 2; i++)
            {
                for (int j = 0; j < 2; j++)
                {
                    corner[i][j] = getFluidHeightAverage(height[i][j], height[i][j + 1], height[i + 1][j],
                            height[i + 1][j + 1]);
                }
            }
        }

        TileEntityBlockFluid tile = (TileEntityBlockFluid) te;
        tile.flowDir = flow;
        tile.corner = corner;

    }

    @Override
    public boolean canCollideCheck(IBlockState state, boolean fullHit)
    {
        if (getFluid().getViscosity() == Integer.MAX_VALUE) return true;
        return fullHit && ((Integer) state.getValue(LEVEL)) == quantaPerBlock - 1;
    }

    @Override
    public int getMaxRenderHeightMeta()
    {
        return quantaPerBlock - 1;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
    {
        if (getFluid().getViscosity() == Integer.MAX_VALUE) return;

        boolean changed = changed(world, pos, state, rand);

        if (!changed && this instanceof IHardenableFluid)
        {
            Vector3 vec = Vector3.getNewVectorFromPool().set(pos);
            ((IHardenableFluid) this).tryHarden(world, vec);
            vec.freeVectorFromPool();
        }
    }

    public boolean changed(World world, BlockPos pos, IBlockState state, Random rand)
    {
        boolean changed = false;
        int quantaRemaining = ((Integer) state.getValue(LEVEL)) + 1;

        // Flow vertically if possible
        int prevRemaining = quantaRemaining;
        quantaRemaining = tryToFlowVerticallyInto(world, pos, quantaRemaining);

        if (quantaRemaining < getFlowDifferential() + 1)
        {
            return changed;
        }
        else if (quantaRemaining != prevRemaining)
        {
            changed = true;
            if (quantaRemaining == 1)
            {
                world.setBlockState(pos, state.withProperty(LEVEL, quantaRemaining - 1), 2);
                return changed;
            }
        }
        else if (quantaRemaining == 1) { return changed; }

        // Flow out if possible
        int lowerthan = quantaRemaining - 1 - getFlowDifferential();
        int total = quantaRemaining;
        int count = 1;

        int start = rand.nextInt(100);

        for (int i = 0; i < 4; i++)
        {
            int index = (i + start) % 4;
            EnumFacing side = EnumFacing.Plane.HORIZONTAL.facings()[index];
            BlockPos off = pos.offset(side);
            if (displaceIfPossible(world, off)) world.setBlockToAir(off);

            int quanta = getQuantaValueBelow(world, off, lowerthan);
            if (quanta >= 0)
            {
                count++;
                total += quanta;
            }
        }

        if (count == 1)
        {
            if (changed)
            {
                world.setBlockState(pos, state.withProperty(LEVEL, quantaRemaining - 1), 2);
            }
            return changed;
        }

        int each = total / count;
        int rem = total % count;

        for (int i = 0; i < 4; i++)
        {
            int index = (i + start) % 4;
            EnumFacing side = EnumFacing.Plane.HORIZONTAL.facings()[index];
            BlockPos off = pos.offset(side);
            int quanta = getQuantaValueBelow(world, off, lowerthan);
            if (quanta >= 0)
            {
                int newquanta = each;
                if (rem == count || rem > 1 && rand.nextInt(count - rem) != 0)
                {
                    ++newquanta;
                    --rem;
                }

                if (newquanta != quanta)
                {
                    if (newquanta == 0)
                    {
                        world.setBlockToAir(off);
                    }
                    else
                    {
                        world.setBlockState(off, getDefaultState().withProperty(LEVEL, newquanta - 1), 2);
                    }
                    world.scheduleUpdate(off, this, tickRate);
                    changed = true;
                }
                --count;
            }
        }

        if (rem > 0)
        {
            ++each;
        }
        world.setBlockState(pos, state.withProperty(LEVEL, each - 1), 2);

        return changed;
    }

    public int tryToFlowVerticallyInto(World world, BlockPos pos, int amtToInput)
    {
        IBlockState myState = world.getBlockState(pos);
        Vector3 temp = Vector3.getNewVectorFromPool().set(pos);
        BlockPos other = temp.addTo(0, densityDir, 0).getPos();
        int n = 0;
        while (other.getY() > 0)
        {
            if (temp.isAir(world))
            {
                other = temp.addTo(0, densityDir, 0).getPos();
            }
            else if (n > 0)
            {
                other = temp.addTo(0, -densityDir, 0).getPos();
                break;
            }
            else
            {
                break;
            }
            n++;
        }
        other = new BlockPos(other.getX(), other.getY(), other.getZ());
        temp.freeVectorFromPool();

        if (other.getY() < 0 || other.getY() >= world.getHeight())
        {
            System.out.println("out of bounds, setting air");
            world.setBlockState(pos, Blocks.air.getDefaultState(), 2);
            return 0;
        }

        int amt = getQuantaValueBelow(world, other, quantaPerBlock);
        if (amt >= 0)
        {
            amt += amtToInput;
            if (amt > quantaPerBlock)
            {
                world.setBlockState(other, myState.withProperty(LEVEL, quantaPerBlock - 1), 3);
                return amt - quantaPerBlock;
            }
            else if (amt > 0)
            {
                world.setBlockState(other, myState.withProperty(LEVEL, amt - 1), 3);
                world.setBlockState(pos, Blocks.air.getDefaultState(), 2);
                return 0;
            }
            return amtToInput;
        }
        else
        {
            int density_other = getDensity(world, other);
            if (density_other == Integer.MAX_VALUE)
            {
                if (displaceIfPossible(world, other))
                {
                    world.setBlockState(other, myState.withProperty(LEVEL, amtToInput - 1), 3);
                    world.setBlockState(pos, Blocks.air.getDefaultState(), 2);
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
                    IBlockState state = world.getBlockState(other);
                    world.setBlockState(other, myState.withProperty(LEVEL, amtToInput - 1), 3);
                    world.setBlockState(pos, state, 2);
                    return 0;
                }
            }
            else
            {
                if (density_other > density)
                {
                    IBlockState state = world.getBlockState(other);
                    world.setBlockState(other, myState.withProperty(LEVEL, amtToInput - 1), 3);
                    world.setBlockState(pos, state, 2);
                    return 0;
                }
            }
            return amtToInput;
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
        to.setBlock(world, this, metaTo, 2);

        if (metaFrom >= 0)
        {
            world.setBlockState(from.getPos(), from.getBlockState(world).getBlock().getStateFromMeta(metaFrom), 2);
        }
        else
        {
            from.setAir(world);
        }
    }

    /* IFluidBlock */
    @Override
    public FluidStack drain(World world, BlockPos pos, boolean doDrain)
    {
        if (doDrain)
        {
            world.setBlockToAir(pos);
        }

        return new FluidStack(getFluid(),
                MathHelper.floor_float(getQuantaPercentage(world, pos) * FluidContainerRegistry.BUCKET_VOLUME));
    }

    @Override
    public boolean canDrain(World world, BlockPos pos)
    {
        return true;
    }

    @Override
    public int getQuantaValue(IBlockAccess world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock().isAir(world, pos)) { return 0; }
        if (!(state.getBlock() instanceof BlockFluid)) { return -1; }
        if (state.getBlock() != this && getTemperature(world, pos) > this.temperature) { return -1; }

        return ((Integer) state.getValue(LEVEL)) + 1;
    }

    @Override
    public int getFlowDifferential()
    {
        return 0;
    }

    @Override
    public float getFluidHeightForRender(IBlockAccess world, BlockPos pos)
    {
        IBlockState here = world.getBlockState(pos);
        IBlockState up = world.getBlockState(pos.down(densityDir));
        float quantaPercentage;
        if (here.getBlock() instanceof BlockFluid)
        {
            BlockFluid block = (BlockFluid) here.getBlock();
            if (up.getBlock().getMaterial().isLiquid() || up.getBlock() instanceof IFluidBlock) { return 1; }

            if (block.getMetaFromState(here) == block.getMaxRenderHeightMeta()) { return 0.999F; }
            quantaPercentage = block.getQuantaPercentage(world, pos);
        }
        else
        {
            quantaPercentage = getQuantaPercentage(world, pos);
        }

        return up.getBlock() == this ? 1// !here.getBlock().getMaterial().isSolid()
                                        // &&
                : quantaPercentage * 0.999F;
    }

    @Override
    /** Convert the given metadata into a BlockState for this Block */
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(LEVEL, meta);
    }

    @Override
    protected BlockState createBlockState()
    {
        return new ExtendedBlockState(this, new IProperty[] { LEVEL }, FLUID_RENDER_PROPS);
    }

    @Override
    public IBlockState getExtendedState(IBlockState oldState, IBlockAccess worldIn, BlockPos pos)
    {
        IExtendedBlockState state = (IExtendedBlockState) oldState;

        TileEntity te = worldIn.getTileEntity(pos);
        if (te != null && te instanceof TileEntityBlockFluid)
        {
            TileEntityBlockFluid tile = (TileEntityBlockFluid) te;
            if (tile.corner != null)
            {
                state = state.withProperty(FLOW_DIRECTION, tile.flowDir);
                float[][] corner = tile.corner;
                int colour = colorMultiplier(worldIn, pos);
                state = state.withProperty(LEVEL_CORNERS[0], corner[0][0]);
                state = state.withProperty(LEVEL_CORNERS[1], corner[0][1]);
                state = state.withProperty(LEVEL_CORNERS[2], corner[1][1]);
                state = state.withProperty(LEVEL_CORNERS[3], corner[1][0]);
                state = state.withProperty(COLOUR, colour);
                return state;
            }
        }
        state = state.withProperty(FLOW_DIRECTION, (float) getFlowDirection(worldIn, pos));
        float[][] height = new float[3][3];
        float[][] corner = new float[2][2];
        height[1][1] = getFluidHeightForRender(worldIn, pos);
        if (height[1][1] == 1)
        {
            for (int i = 0; i < 2; i++)
            {
                for (int j = 0; j < 2; j++)
                {
                    corner[i][j] = 1;
                }
            }
        }
        else
        {
            for (int i = 0; i < 3; i++)
            {
                for (int j = 0; j < 3; j++)
                {
                    if (i != 1 || j != 1)
                    {
                        height[i][j] = getFluidHeightForRender(worldIn, pos.add(i - 1, 0, j - 1));
                    }
                }
            }
            for (int i = 0; i < 2; i++)
            {
                for (int j = 0; j < 2; j++)
                {
                    corner[i][j] = getFluidHeightAverage(height[i][j], height[i][j + 1], height[i + 1][j],
                            height[i + 1][j + 1]);
                }
            }
        }
        int colour = colorMultiplier(worldIn, pos);

        state = state.withProperty(LEVEL_CORNERS[0], corner[0][0]);
        state = state.withProperty(LEVEL_CORNERS[1], corner[0][1]);
        state = state.withProperty(LEVEL_CORNERS[2], corner[1][1]);
        state = state.withProperty(LEVEL_CORNERS[3], corner[1][0]);
        state = state.withProperty(COLOUR, colour);

        return state;
    }

    public static double getFlowDirection(IBlockAccess world, BlockPos pos)
    {
        Block block = world.getBlockState(pos).getBlock();
        if (!(block instanceof IFluidBlock)) { return -1000.0; }
        Vec3 vec = ((BlockFluidBase) block).getFlowVector(world, pos);
        return vec.xCoord == 0.0D && vec.zCoord == 0.0D ? -1000.0D : Math.atan2(vec.zCoord, vec.xCoord) - Math.PI / 2D;
    }

    public Vec3 getFlowVector(IBlockAccess world, BlockPos pos)
    {
        return super.getFlowVector(world, pos);
    }

    public float getFluidHeightAverage(float... flow)
    {
        float total = 0;
        int count = 0;

        float end = 0;

        for (int i = 0; i < flow.length; i++)
        {
            if (flow[i] >= 0.999F && end != 1F)
            {
                end = flow[i];
            }

            if (flow[i] >= 0)
            {
                total += flow[i];
                count++;
            }
        }

        if (end == 0) end = total / count;

        return end;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state)
    {
        int meta = getQuantaValue(world, pos);
        int l = meta;
        float f = 0.0625F;

        if (getFluid().getViscosity() >= 6000) { return new AxisAlignedBB(0 + pos.getX(), 0 + pos.getY(),
                0 + pos.getZ(), 1 + pos.getX(), f * l + pos.getY(), 1 + pos.getZ()); }

        return new AxisAlignedBB(pos, pos);

    }

    /** Add all collision boxes of this Block to the list that intersect with
     * the given mask.
     * 
     * @param collidingEntity
     *            the Entity colliding with this Block */
    public void addCollisionBoxesToList(World worldIn, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list,
            Entity collidingEntity)
    {
        AxisAlignedBB[] boxes = getBoxes(worldIn, pos);

        if(getFluid().getViscosity() < 6000) return;
        
        
        if (boxes != null)
        {
            for (AxisAlignedBB box : boxes)
                if (box.intersectsWith(mask)) list.add(box);
        }
    }

    public AxisAlignedBB[] getBoxes(World worldObj, BlockPos pos)
    {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        double[] heights;

        TileEntity te = worldObj.getTileEntity(pos);
        if(te instanceof TileEntityBlockFluid)
        {
            TileEntityBlockFluid tile = (TileEntityBlockFluid) te;
            if(tile.corner == null)
            {
                heights = getCornerHeights(worldObj, x, y, z);
            }
            else
            {
                heights = new double[4];
                heights[0] = tile.corner[0][0];
                heights[1] = tile.corner[0][1];
                heights[2] = tile.corner[1][1];
                heights[3] = tile.corner[1][0];
            }
        }
        else
        heights = getCornerHeights(worldObj, x, y, z);
        
        double hN = (heights[0] + heights[3]) / 2;
        double hS = (heights[1] + heights[2]) / 2;
        double hE = (heights[2] + heights[3]) / 2;
        double hW = (heights[0] + heights[1]) / 2;

        double hM = (hN + hS + hE + hW) / 4;

        AxisAlignedBB NW = new AxisAlignedBB(0.0, 0, 0.0, 0.25, heights[0], 0.25).offset(x, y, z);
        AxisAlignedBB NW1 = new AxisAlignedBB(0.25, 0, 0.0, 0.5, hN, 0.25).offset(x, y, z);
        AxisAlignedBB NW2 = new AxisAlignedBB(0.0, 0, 0.25, 0.25, hW, 0.5).offset(x, y, z);
        AxisAlignedBB NW3 = new AxisAlignedBB(0.25, 0, 0.25, 0.5, hM, 0.5).offset(x, y, z);

        AxisAlignedBB NE = new AxisAlignedBB(0.75, 0, 0.0, 1.0, heights[3], 0.25).offset(x, y, z);
        AxisAlignedBB NE1 = new AxisAlignedBB(0.75, 0, 0.25, 1.0, hE, 0.5).offset(x, y, z);
        AxisAlignedBB NE2 = new AxisAlignedBB(0.5, 0, 0.0, 0.75, hN, 0.25).offset(x, y, z);
        AxisAlignedBB NE3 = new AxisAlignedBB(0.5, 0, 0.25, 0.75, hM, 0.5).offset(x, y, z);

        AxisAlignedBB SW = new AxisAlignedBB(0.0, 0, 0.75, 0.25, heights[1], 1.0).offset(x, y, z);
        AxisAlignedBB SW1 = new AxisAlignedBB(0.25, 0, 0.75, 0.5, hS, 1.0).offset(x, y, z);
        AxisAlignedBB SW2 = new AxisAlignedBB(0.0, 0, 0.5, 0.25, hW, 0.75).offset(x, y, z);
        AxisAlignedBB SW3 = new AxisAlignedBB(0.25, 0, 0.5, 0.5, hM, 0.75).offset(x, y, z);

        AxisAlignedBB SE = new AxisAlignedBB(0.75, 0, 0.75, 1.0, heights[2], 1.0).offset(x, y, z);
        AxisAlignedBB SE1 = new AxisAlignedBB(0.75, 0, 0.5, 1.0, hE, 0.75).offset(x, y, z);
        AxisAlignedBB SE2 = new AxisAlignedBB(0.5, 0, 0.75, 0.75, hS, 1.0).offset(x, y, z);
        AxisAlignedBB SE3 = new AxisAlignedBB(0.5, 0, 0.5, 0.75, hM, 0.75).offset(x, y, z);

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

    public float getFluidHeightForCollision(IBlockAccess world, int x, int y, int z)
    {
        Vector3 vec = Vector3.getNewVectorFromPool().set(x, y, z);
        int meta = vec.getBlockMetadata(world);
        Block id = vec.getBlock(world);
        if (id instanceof BlockFluid)
        {
            if (vec.getBlock(world, EnumFacing.UP) instanceof BlockFluid)
            {
                // if (world.getBlock(x, y, z) == block.blockID) {
                // if (world.getBlock(x, y + 1, z) == block.blockID) {
                return 1;
            }
            if (meta == getMaxRenderHeightMeta()) { return 1F; }
            return ((float) (meta + 1)) / 16;
        }
        return 0;
    }
}