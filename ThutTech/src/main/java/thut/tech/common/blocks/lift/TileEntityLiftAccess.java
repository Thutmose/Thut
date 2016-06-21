package thut.tech.common.blocks.lift;

import static net.minecraft.util.EnumFacing.DOWN;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.api.ThutBlocks;
import thut.api.maths.Vector3;
import thut.tech.common.entity.EntityLift;

@net.minecraftforge.fml.common.Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")
public class TileEntityLiftAccess extends TileEntity implements ITickable//, SimpleComponent
{
    public int                          power        = 0;
    public int                          prevPower    = 1;
    public EntityLift                   lift;

    boolean                             listNull     = false;
    List<Entity>                        list         = new ArrayList<Entity>();
    Vector3                             here;

    public Vector3                      root         = Vector3.getNewVector();
    public TileEntityLiftAccess         rootNode;
    public Vector<TileEntityLiftAccess> connected    = new Vector<TileEntityLiftAccess>();
    EnumFacing                          sourceSide;
    public double                       energy;

    boolean                             isLift       = false;
    public Block                        blockID      = Blocks.AIR;

    // public int[][] corners = new int[2][2];
    public Vector3                      boundMin     = Vector3.getNewVector();
    public Vector3                      boundMax     = Vector3.getNewVector();

    boolean                             loaded       = false;

    public int                          floor        = 0;
    public int                          calledYValue = -1;
    public int                          calledFloor  = 0;
    public int                          currentFloor = 0;
    UUID                                liftID       = null;
    UUID                                empty        = new UUID(0, 0);
    private byte[]                      sides        = new byte[6];
    private byte[]                      sidePages    = new byte[6];

    int                                 tries        = 0;

    public boolean                      toClear      = false;

    public boolean                      first        = true;
    public boolean                      read         = false;
    public boolean                      redstone     = true;
    public boolean                      powered      = false;

    public TileEntityLiftAccess()
    {
    }

    public void buttonPress(int button)
    {
        if (button != 0 && button <= 64 && lift != null && lift.floors[button - 1] > 0)
        {
            if (button == floor)
            {
            }
            else
            {
                if (lift.getCurrentFloor() == floor) lift.setCurrentFloor(-1);
            }

            lift.call(button);
        }
    }

    /*
     * Calls lift to specified Floor
     */
    @Callback(doc = "function(floor:number) -- Calls the Lift to the specified Floor")
    @Optional.Method(modid = "OpenComputers")
    public Object[] callFloor(Context context, Arguments args) throws Exception
    {
        if (lift != null)
        {
            lift.call(args.checkInteger(0));
            return new Object[] {};
        }
        throw new Exception("no connected lift");
    }

    /*
     * Calls lift to specified Y value
     */
    @Callback(doc = "function(yValue:number) -- Calls the Lift to the specified Y level")
    @Optional.Method(modid = "OpenComputers")
    public Object[] callYValue(Context context, Arguments args) throws Exception
    {
        if (lift != null)
        {
            lift.callYValue(args.checkInteger(0));
            return new Object[] {};
        }
        throw new Exception("no connected lift");
    }

    public void callYValue(int yValue)
    {
        if (lift != null)
        {
            lift.callYValue(yValue);
        }
    }

    public void checkPower()
    {

    }

    public boolean checkSides()
    {
        List<EntityLift> check = worldObj.getEntitiesWithinAABB(EntityLift.class,
                new AxisAlignedBB(getPos().getX() + 0.5 - 1, getPos().getY(), getPos().getZ() + 0.5 - 1,
                        getPos().getX() + 0.5 + 1, getPos().getY() + 1, getPos().getZ() + 0.5 + 1));
        if (check != null && check.size() > 0)
        {
            lift = check.get(0);
            liftID = lift.id;
        }
        return !(check == null || check.isEmpty());
    }

    public void clearConnections()
    {

    }

    public String connectionInfo()
    {
        String ret = "";
        return ret;
    }

    public void doButtonClick(EntityLivingBase clicker, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (liftID != null && !liftID.equals(empty) && lift != EntityLift.getLiftFromUUID(liftID, worldObj.isRemote))
        {
            lift = EntityLift.getLiftFromUUID(liftID, worldObj.isRemote);
        }
        int button = getButtonFromClick(side, hitX, hitY, hitZ);
        if (isLift && blockID == ThutBlocks.lift)
        {
            if (button == 2)
            {
                boundMin.x = Math.max(-2, boundMin.x - 1);
            }
            else if (button == 3)
            {
                boundMin.x = Math.min(0, boundMin.x + 1);
            }
            else if (button == 6)
            {
                boundMin.z = Math.max(-2, boundMin.z - 1);
            }
            else if (button == 7)
            {
                boundMin.z = Math.min(0, boundMin.z + 1);
            }
            else if (button == 10)
            {
                boundMax.x = Math.max(0, boundMax.x - 1);
            }
            else if (button == 11)
            {
                boundMax.x = Math.min(2, boundMax.x + 1);
            }
            else if (button == 14)
            {
                boundMax.z = Math.max(0, boundMax.z - 1);
            }
            else if (button == 15)
            {
                boundMax.z = Math.min(2, boundMax.z + 1);
            }
            else
            {
                if (button == 4 || button == 8 || button == 12 || button == 16)
                {
                    boundMax.y = Math.max(0, boundMax.y - 1);
                }
                else if (button == 1 || button == 5 || button == 9 || button == 13)
                {
                    boundMax.y = Math.min(5, boundMax.y + 1);
                }
            }
        }
        if (!worldObj.isRemote && lift != null)
        {
            if (isSideOn(side))
            {
                buttonPress(button);
                calledFloor = lift.getDestinationFloor();
            }
        }
        if (clicker instanceof EntityPlayerMP) sendUpdate((EntityPlayerMP) clicker);
        markDirty();
    }

    public int getButtonFromClick(EnumFacing side, float hitX, float hitY, float hitZ)
    {
        int ret = 0;
        int page = getSidePage(side);
        switch (side.getIndex())
        {
        case 0:
        {
            return 0 + 16 * page;
        }
        case 1:
        {
            ret = 1 + (int) (((1 - hitX) * 4) % 4) + 4 * (int) (((1 - hitZ) * 4) % 4);
            return ret + 16 * page;
        }
        case 2:
        {
            ret = 1 + (int) (((1 - hitX) * 4) % 4) + 4 * (int) (((1 - hitY) * 4) % 4);
            return ret + 16 * page;
        }
        case 3:
        {
            ret = 1 + (int) (((hitX) * 4) % 4) + 4 * (int) (((1 - hitY) * 4) % 4);
            return ret + 16 * page;
        }
        case 4:
        {
            ret = 1 + 4 * (int) (((1 - hitY) * 4) % 4) + (int) (((hitZ) * 4) % 4);
            return ret + 16 * page;
        }
        case 5:
        {
            ret = 1 + 4 * (int) (((1 - hitY) * 4) % 4) + (int) (((1 - hitZ) * 4) % 4);
            return ret + 16 * page;
        }
        default:
        {
            return 0 + 16 * page;
        }

        }

    }

//    @Override //TODO re-add SimpleComponent when it is fixed.
    public String getComponentName()
    {
        return "lift";
    }

    /** Overriden in a sign to provide the text. */
    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(this.getPos(), 3, getUpdateTag());
    }

    public double getEnergy()
    {
        return 0;
    }

    /*
     * Returns floor associated with this block
     */
    @Callback(doc = "returns the Floor assigned to the Controller")
    @Optional.Method(modid = "OpenComputers")
    public Object[] getFloor(Context context, Arguments args)
    {
        return new Object[] { floor };
    }

    /*
     * Returns the Y value of the controller for the specified floor
     */
    @Callback(doc = "function(floor:number) -- returns the y value of the specified floor")
    @Optional.Method(modid = "OpenComputers")
    public Object[] getFloorYValue(Context context, Arguments args) throws Exception
    {
        if (lift != null)
        {
            int floor = args.checkInteger(0);

            if (floor > 0 && floor <= 64)
            {
                int value = lift.floors[floor - 1];
                if (value == -1) throw new Exception("floor " + floor + " is not assigned");
                return new Object[] { value };
            }
            throw new Exception("floor out of bounds");
        }
        throw new Exception("no connected lift");
    }

    public TileEntityLiftAccess getRoot()
    {
        if (here == null || here.isEmpty())
        {
            here = Vector3.getNewVector().set(this);
        }

        if (rootNode != null) return rootNode;

        Block b = here.getBlock(worldObj, DOWN);
        if (b == blockID)
        {
            TileEntityLiftAccess te = (TileEntityLiftAccess) here.getTileEntity(worldObj, DOWN);
            if (te != null && te != this) { return rootNode = te.getRoot(); }
        }
        return rootNode = this;
    }

    public int getSidePage(EnumFacing side)
    {
        return sidePages[side.getIndex()];
    }

    /*
     * Returns the Yvalue of the lift.
     */
    @Callback(doc = "returns the current Y value of the lift.")
    @Optional.Method(modid = "OpenComputers")
    public Object[] getYValue(Context context, Arguments args) throws Exception
    {
        if (lift != null) return new Object[] { (int) lift.posY };

        throw new Exception("no connected lift");
    }

    /** Called from Chunk.setBlockIDWithMetadata and Chunk.fillChunk, determines
     * if this tile entity should be re-created when the ID, or Metadata
     * changes. Use with caution as this will leave straggler TileEntities, or
     * create conflicts with other TileEntities if not used properly.
     *
     * @param world
     *            Current world
     * @param pos
     *            Tile's world position
     * @param oldState
     *            The old ID of the block
     * @param newState
     *            The new ID of the block (May be the same)
     * @return true forcing the invalidation of the existing TE, false not to
     *         invalidate the existing TE */
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return oldState.getBlock() != newSate.getBlock();
    }

    /** invalidates a tile entity */
    @Override
    public void invalidate()
    {
        super.invalidate();
        clearConnections();
    }

    public boolean isSideOn(EnumFacing side)
    {
        int state = 1;
        byte byte0 = sides[side.getIndex()];
        return (byte0 & state) != 0;
    }

    @Override
    public void onChunkUnload()
    {
        super.onChunkUnload();
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        NBTTagCompound nbttagcompound = pkt.getNbtCompound();
        this.readFromNBT(nbttagcompound);
    }

    @Override
    public void readFromNBT(NBTTagCompound par1)
    {
        super.readFromNBT(par1);
        blockID = Block.getBlockFromName(par1.getString("block id"));
        floor = par1.getInteger("floor");
        liftID = new UUID(par1.getLong("idMost"), par1.getLong("idLess"));
        root = Vector3.getNewVector();
        root = Vector3.readFromNBT(par1, "root");
        sides = par1.getByteArray("sides");
        if (sides.length != 6) sides = new byte[6];
        sidePages = par1.getByteArray("sidePages");
        if (sidePages.length != 6) sidePages = new byte[6];
        if (par1.hasKey("corners"))
        {
            int[] read = par1.getIntArray("corners");
            int xMin = read[0];
            int zMin = read[1];
            int xMax = read[2];
            int zMax = read[3];
            boundMin.x = xMin;
            boundMin.z = zMin;
            boundMax.x = xMax;
            boundMax.z = zMax;
        }
        if (par1.hasKey("bounds"))
        {
            boundMin = Vector3.readFromNBT(par1.getCompoundTag("bounds"), "min");
            boundMax = Vector3.readFromNBT(par1.getCompoundTag("bounds"), "max");
        }
    }

    public void setEnergy(double energy)
    {
    }

    /*
     * Sets floor associated with this block
     */
    @Callback(doc = "function(floor:number) -- Sets the floor assosiated to the Controller")
    @Optional.Method(modid = "OpenComputers")
    public Object[] setFloor(Context context, Arguments args)
    {
        floor = args.checkInteger(0);
        return new Object[] { floor };
    }

    public void setFloor(int floor)
    {
        if (lift != null && floor <= 64 && floor > 0)
        {
            lift.setFoor(getRoot(), floor);
            getRoot().floor = floor;
            getRoot().markDirty();
        }
    }

    public void setLift(EntityLift lift)
    {
        this.lift = lift;
    }

    public void setRoot(TileEntityLiftAccess root)
    {
        this.rootNode = root;
    }

    public void setSide(EnumFacing side, boolean flag)
    {
        int state = 1;
        byte byte0 = sides[side.getIndex()];

        if (side.getIndex() < 2) return;

        if (flag)
        {
            sides[side.getIndex()] = (byte) (byte0 | state);
        }
        else
        {
            sides[side.getIndex()] = (byte) (byte0 & -state - 1);
        }
        markDirty();
    }

    public void setSidePage(EnumFacing side, int page)
    {
        sidePages[side.getIndex()] = (byte) page;
    }

    @Override
    public void update()
    {
        if (first)
        {
            blockID = worldObj.getBlockState(getPos()).getBlock();
            isLift = worldObj.getBlockState(getPos()).getValue(BlockLift.VARIANT) == BlockLift.EnumType.LIFT;
            here = Vector3.getNewVector().set(this);
            first = false;
        }

        if (isLift) { return; }

        if ((lift == null || lift.isDead))
        {
            calledYValue = -1;
            calledFloor = 0;
            currentFloor = 0;
        }

        if (lift != null && !worldObj.isRemote)
        {
            boolean check = (int) lift.posY + 2 == getPos().getY() && lift.motionY == 0;
            IBlockState state = worldObj.getBlockState(getPos());
            boolean old = state.getValue(BlockLift.CURRENT);
            if (check != old)
            {
                state = state.withProperty(BlockLift.CURRENT, check);
                worldObj.setBlockState(getPos(), state);
            }
            if (lift.motionY == 0 || lift.getDestinationFloor() == floor)
            {
                old = state.getValue(BlockLift.CALLED);
                check = lift.getDestinationFloor() == floor;
                if (check != old)
                {
                    state = state.withProperty(BlockLift.CALLED, check);
                    worldObj.setBlockState(getPos(), state);
                }
            }
        }

        if (lift != null && floor > 0)
        {
            if ((int) lift.posY == getPos().getY() - 2)
            {
                lift.setCurrentFloor(floor);
            }
            else if (lift.getCurrentFloor() == floor)
            {
                lift.setCurrentFloor(-1);
            }
            if (lift.floors[floor - 1] < 0)
            {
                lift.setFoor(this, floor);
            }
            calledFloor = lift.getDestinationFloor();
            currentFloor = lift.getCurrentFloor();
        }
        if (liftID != null && !liftID.equals(empty) && (lift == null || lift.isDead))
        {
            lift = EntityLift.getLiftFromUUID(liftID, worldObj.isRemote);
            if (lift == null) return;
        }
        if (getRoot().floor != floor)
        {
            this.floor = getRoot().floor;
            this.lift = getRoot().lift;
            markDirty();
        }
        if (floor > 0 && (lift == null || lift.isDead))
        {
            lift = null;
            floor = 0;
        }
        if (blockID == ThutBlocks.lift && lift == null)
        {
            for (EnumFacing side : EnumFacing.values())
            {
                TileEntity t = here.getTileEntity(worldObj, side);
                Block b = here.getBlock(worldObj, side);
                if (b == blockID && t instanceof TileEntityLiftAccess)
                {
                    TileEntityLiftAccess te = (TileEntityLiftAccess) t;
                    if (te.lift != null)
                    {
                        lift = te.lift;
                        floor = te.floor;
                        markDirty();
                        break;
                    }
                }
            }
        }
    }

    /** validates a tile entity */
    @Override
    public void validate()
    {
        super.validate();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound par1)
    {
        super.writeToNBT(par1);
        if (blockID == null) blockID = ThutBlocks.liftRail;
        if (blockID == null) blockID = worldObj.getBlockState(getPos()).getBlock();
        par1.setString("block id", blockID.getLocalizedName());
        par1.setInteger("floor", floor);
        par1.setByteArray("sides", sides);
        par1.setByteArray("sidePages", sidePages);
        if (root != null) root.writeToNBT(par1, "root");
        if (lift != null)
        {
            liftID = lift.id;
        }
        if (liftID != null)
        {
            par1.setLong("idLess", liftID.getLeastSignificantBits());
            par1.setLong("idMost", liftID.getMostSignificantBits());
        }
        NBTTagCompound vector = new NBTTagCompound();
        boundMin.writeToNBT(vector, "min");
        boundMax.writeToNBT(vector, "max");
        par1.setTag("bounds", vector);
        return par1;
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        AxisAlignedBB bb = INFINITE_EXTENT_AABB;
        return bb;
    }

    public void sendUpdate(EntityPlayerMP player)
    {
        player.connection.sendPacket(getUpdatePacket());
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        return writeToNBT(new NBTTagCompound());
    }
}
