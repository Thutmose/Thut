package thut.tech.common.blocks.lift;

import static net.minecraft.util.EnumFacing.DOWN;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.common.Optional;
import thut.api.ThutBlocks;
import thut.api.maths.Vector3;
import thut.tech.common.entity.EntityLift;

@net.minecraftforge.fml.common.Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")
public class TileEntityLiftAccess extends TileEntity implements ITickable, SimpleComponent
{
    public int        power     = 0;
    public int        prevPower = 1;
    public EntityLift lift;

    boolean      listNull = false;
    List<Entity> list     = new ArrayList<Entity>();
    Vector3      here;

    public Vector3                      root      = Vector3.getNewVector();
    public TileEntityLiftAccess         rootNode;
    public Vector<TileEntityLiftAccess> connected = new Vector<TileEntityLiftAccess>();
    EnumFacing                          sourceSide;
    public double                       energy;

    public long  time     = 0;
    public int   metaData = 0;
    public Block blockID  = Blocks.air;

    public int[][] corners = new int[2][2];

    boolean loaded = false;

    public int     floor        = 0;
    public int     calledYValue = -1;
    public int     calledFloor  = 0;
    public int     currentFloor = 0;
    UUID           liftID       = null;
    UUID           empty        = new UUID(0, 0);
    private byte[] sides        = new byte[6];
    private byte[] sidePages    = new byte[6];

    int tries = 0;

    public boolean toClear = false;

    public boolean first    = true;
    public boolean read     = false;
    public boolean redstone = true;
    public boolean powered  = false;
    boolean        called   = false;

    public TileEntityLiftAccess()
    {
        // The 'node' of a tile entity is used to connect it to other components
        // including computers. They are connected to nodes of neighboring
        // blocks, forming a network that way. That network is also used for
        // distributing energy among components for the mod.
    }

    public void update()
    {
        if (first)
        {
            blockID = worldObj.getBlockState(getPos()).getBlock();
            metaData = blockID.getMetaFromState(worldObj.getBlockState(getPos()));
            here = Vector3.getNewVector().set(this);
            first = false;
        }

        if (metaData != 1 && blockID == ThutBlocks.lift) { return; }

        if ((lift == null || lift.isDead)) // &&floor!=0)
        {
            calledYValue = -1;
            calledFloor = 0;
            currentFloor = 0;
        }

        if (lift != null)
        {
            boolean check = (int) lift.posY + 2 == getPos().getY() && lift.motionY == 0;
            setCalled(check);
            time++;
        }

        if (lift != null && floor > 0)
        {
            int calledFloorOld = calledFloor;

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

            // if (calledFloor == floor)
            // {
            // setCalled(true);
            // }
            // else
            // {
            // setCalled(false);
            // }

            if (calledFloor != calledFloorOld)
            {
                // worldObj.scheduledUpdatesAreImmediate = true;
                // getBlockType().updateTick(worldObj, xCoord, getPos().getY(),
                // zCoord, worldObj.rand);
                // worldObj.scheduledUpdatesAreImmediate = false;
            }
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
        time++;
    }

    public void checkPower()
    {

    }

    public void clearConnections()
    {

    }

    public double getEnergy()
    {
        return 0;
    }

    public void setEnergy(double energy)
    {
    }

    public String connectionInfo()
    {
        String ret = "";
        return ret;
    }

    /** invalidates a tile entity */
    public void invalidate()
    {
        super.invalidate();
        clearConnections();
    }

    @Override
    public void onChunkUnload()
    {
        super.onChunkUnload();
    }

    /** validates a tile entity */
    public void validate()
    {
        super.validate();
    }

    public boolean checkSides()
    {
        List<EntityLift> check = worldObj.getEntitiesWithinAABB(EntityLift.class,
                new AxisAlignedBB(getPos().getX() + 0.5 - 1, getPos().getY(), getPos().getZ() + 0.5 - 1,
                        getPos().getX() + 0.5 + 1, getPos().getY() + 1, getPos().getZ() + 0.5 + 1));
        if (check != null && check.size() > 0)
        {
            lift = (EntityLift) check.get(0);
            liftID = lift.id;
        }
        return !(check == null || check.isEmpty());
    }

    public void setFloor(int floor)
    {
        if (lift != null && floor <= 64 && floor > 0)
        {
            lift.setFoor(getRoot(), floor);
            getRoot().floor = floor;
            getRoot().markDirty();
            // getRoot().updateBlock();
        }
    }

    public void setLift(EntityLift lift)
    {
        this.lift = lift;
    }

    public void writeToNBT(NBTTagCompound par1)
    {
        super.writeToNBT(par1);
        par1.setInteger("meta", metaData);
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
        int xMin = corners[0][0];
        int zMin = corners[0][1];
        int xMax = corners[1][0];
        int zMax = corners[1][1];

        int[] toStore = { xMin, zMin, xMax, zMax };
        par1.setIntArray("corners", toStore);
    }

    public void readFromNBT(NBTTagCompound par1)
    {
        super.readFromNBT(par1);
        metaData = par1.getInteger("meta");
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
            corners[0][0] = xMin;
            corners[0][1] = zMin;
            corners[1][0] = xMax;
            corners[1][1] = zMax;
        }
    }

    public void doButtonClick(EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (liftID != null && !liftID.equals(empty) && lift != EntityLift.getLiftFromUUID(liftID, worldObj.isRemote))
        {
            lift = EntityLift.getLiftFromUUID(liftID, worldObj.isRemote);
        }
        int button = getButtonFromClick(side, hitX, hitY, hitZ);
        if (metaData != 1 && blockID == ThutBlocks.lift)
        {
            if (button == 2)
            {
                corners[0][0] = Math.max(-2, corners[0][0] - 1);
            }
            if (button == 3)
            {
                corners[0][0] = Math.min(0, corners[0][0] + 1);
            }
            if (button == 6)
            {
                corners[0][1] = Math.max(-2, corners[0][1] - 1);
            }
            if (button == 7)
            {
                corners[0][1] = Math.min(0, corners[0][1] + 1);
            }

            if (button == 10)
            {
                corners[1][0] = Math.max(0, corners[1][0] - 1);
            }
            if (button == 11)
            {
                corners[1][0] = Math.min(2, corners[1][0] + 1);
            }
            if (button == 14)
            {
                corners[1][1] = Math.max(0, corners[1][1] - 1);
            }
            if (button == 15)
            {
                corners[1][1] = Math.min(2, corners[1][1] + 1);
            }
        }
        worldObj.markBlockForUpdate(getPos());
        if (!worldObj.isRemote && lift != null)
        {
            if (isSideOn(side))
            {
                buttonPress(button);
                calledFloor = lift.getDestinationFloor();
            }
        }
    }

    public void callYValue(int yValue)
    {
        if (lift != null)
        {
            lift.callYValue(yValue);
        }
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

    public void setCalled(boolean called)
    {
        // IBlockState state = worldObj.getBlockState(getPos());
        boolean isCalled = this.called;// ((Boolean)state.getValue(BlockLift.CALLED))
        if (called != isCalled)
        {
            this.called = called;
            worldObj.notifyNeighborsOfStateChange(getPos(), getBlockType());
        }
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
        worldObj.markBlockForUpdate(pos);
    }

    public boolean isSideOn(EnumFacing side)
    {
        int state = 1;
        byte byte0 = sides[side.getIndex()];
        return (byte0 & state) != 0;
    }

    public int getSidePage(EnumFacing side)
    {
        return sidePages[side.getIndex()];
    }

    public void setSidePage(EnumFacing side, int page)
    {
        sidePages[side.getIndex()] = (byte) page;
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

    /** Overriden in a sign to provide the text. */
    @SuppressWarnings("rawtypes")
    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
        NBTTagCompound nbttagcompound = pkt.getNbtCompound();
        this.readFromNBT(nbttagcompound);
    }

    public void setRoot(TileEntityLiftAccess root)
    {
        this.rootNode = root;
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

    @Override
    public String getComponentName()
    {
        return "lift";
    }
}
