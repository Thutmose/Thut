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
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.api.entity.blockentity.BlockEntityWorld;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.maths.Vector3;
import thut.api.network.PacketHandler;
import thut.lib.CompatWrapper;
import thut.tech.common.entity.EntityLift;

@net.minecraftforge.fml.common.Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")
public class TileEntityLiftAccess extends TileEntity implements ITickable, SimpleComponent
{
    public int                          power        = 0;
    public int                          prevPower    = 1;
    public EntityLift                   lift;
    public IBlockState                  copiedState  = null;
    boolean                             listNull     = false;
    List<Entity>                        list         = new ArrayList<Entity>();
    Vector3                             here;
    public Vector3                      root         = Vector3.getNewVector();
    public TileEntityLiftAccess         rootNode;
    public Vector<TileEntityLiftAccess> connected    = new Vector<TileEntityLiftAccess>();
    EnumFacing                          sourceSide;
    public double                       energy;
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
    public boolean                      callPanel    = false;

    public TileEntityLiftAccess()
    {
    }

    public void buttonPress(int button)
    {
        if (callPanel && lift != null)
        {
            lift.call(floor);
        }
        else
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
            lift.setDestY(args.checkInteger(0));
            return new Object[] {};
        }
        throw new Exception("no connected lift");
    }

    /*
     * Calls lift to specified Y value
     */
    @Callback(doc = "function(xValue:number) -- Calls the Lift to the specified X location")
    @Optional.Method(modid = "OpenComputers")
    public Object[] callXValue(Context context, Arguments args) throws Exception
    {
        if (lift != null)
        {
            lift.setDestX(args.checkInteger(0));
            return new Object[] {};
        }
        throw new Exception("no connected lift");
    }

    /*
     * Calls lift to specified Y value
     */
    @Callback(doc = "function(zValue:number) -- Calls the Lift to the specified Z location")
    @Optional.Method(modid = "OpenComputers")
    public Object[] callZValue(Context context, Arguments args) throws Exception
    {
        if (lift != null)
        {
            lift.setDestZ(args.checkInteger(0));
            return new Object[] {};
        }
        throw new Exception("no connected lift");
    }

    /** Sets the world for this tileEntity. */
    public void setWorldObj(World worldIn)
    {
        this.world = worldIn;
        if (worldIn instanceof BlockEntityWorld)
        {
            IBlockEntity blockEntity = ((BlockEntityWorld) worldIn).getEntity();
            if (blockEntity instanceof EntityLift)
            {
                this.setLift((EntityLift) blockEntity);
            }
        }
    }

    public boolean checkSides()
    {
        List<EntityLift> check = world.getEntitiesWithinAABB(EntityLift.class,
                new AxisAlignedBB(getPos().getX() + 0.5 - 1, getPos().getY(), getPos().getZ() + 0.5 - 1,
                        getPos().getX() + 0.5 + 1, getPos().getY() + 1, getPos().getZ() + 0.5 + 1));
        if (check != null && check.size() > 0)
        {
            lift = check.get(0);
            liftID = lift.getPersistentID();
        }
        return !(check == null || check.isEmpty());
    }

    public String connectionInfo()
    {
        String ret = "";
        return ret;
    }

    public void doButtonClick(EntityLivingBase clicker, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (liftID != null && !liftID.equals(empty) && lift != EntityLift.getLiftFromUUID(liftID, world))
        {
            lift = EntityLift.getLiftFromUUID(liftID, world);
        }
        int button = getButtonFromClick(side, hitX, hitY, hitZ);
        if (!world.isRemote && lift != null)
        {
            if (isSideOn(side))
            {
                buttonPress(button);
                calledFloor = lift.getDestinationFloor();
            }
        }
        if (clicker instanceof EntityPlayerMP) sendUpdate((EntityPlayerMP) clicker);
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

    @Override
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

        Block b = here.getBlock(world, DOWN);
        if (b == getBlockType())
        {
            TileEntityLiftAccess te = (TileEntityLiftAccess) here.getTileEntity(world, DOWN);
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
        if (lift != null) return new Object[] { (float) lift.posY };

        throw new Exception("no connected lift");
    }

    /*
     * Returns the Yvalue of the lift.
     */
    @Callback(doc = "returns the current X value of the lift.")
    @Optional.Method(modid = "OpenComputers")
    public Object[] getXValue(Context context, Arguments args) throws Exception
    {
        if (lift != null) return new Object[] { (float) lift.posX };

        throw new Exception("no connected lift");
    }

    /*
     * Returns the Yvalue of the lift.
     */
    @Callback(doc = "returns the current Z value of the lift.")
    @Optional.Method(modid = "OpenComputers")
    public Object[] getZValue(Context context, Arguments args) throws Exception
    {
        if (lift != null) return new Object[] { (float) lift.posZ };

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
        floor = par1.getInteger("floor");
        liftID = new UUID(par1.getLong("idMost"), par1.getLong("idLess"));
        root = Vector3.getNewVector();
        root = Vector3.readFromNBT(par1, "root");
        sides = par1.getByteArray("sides");
        callPanel = par1.getBoolean("callPanel");
        if (sides.length != 6) sides = new byte[6];
        sidePages = par1.getByteArray("sidePages");
        if (sidePages.length != 6) sidePages = new byte[6];
        if (par1.hasKey("state"))
        {
            NBTTagCompound state = par1.getCompoundTag("state");
            String key = state.getString("K");
            int meta = state.getInteger("M");
            Block block = Block.REGISTRY.getObject(new ResourceLocation(key));
            if (block != null) copiedState = CompatWrapper.getBlockStateFromMeta(block, meta);
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
        this.liftID = lift.getUniqueID();
        if (!world.isRemote) PacketHandler.sendTileUpdate(this);
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
        if (here == null) here = Vector3.getNewVector();
        here.set(this);
        if (this.world instanceof BlockEntityWorld) { return; }

        if ((lift == null || lift.isDead))
        {
            calledYValue = -1;
            calledFloor = 0;
            currentFloor = 0;
        }

        if (lift != null && !world.isRemote)
        {
            boolean check = lift.getCurrentFloor() == this.floor && (int) (lift.motionY * 16) == 0;
            IBlockState state = world.getBlockState(getPos());
            boolean old = state.getValue(BlockLift.CURRENT);

            if (callPanel && !old && !lift.getCalled())
            {
                if (world.isBlockPowered(getPos())) lift.call(floor);
            }

            if (check != old)
            {
                state = state.withProperty(BlockLift.CURRENT, check);
                world.setBlockState(getPos(), state);
            }
            if (lift.motionY == 0 || lift.getDestinationFloor() == floor)
            {
                old = state.getValue(BlockLift.CALLED);
                check = lift.getDestinationFloor() == floor;
                if (check != old)
                {
                    state = state.withProperty(BlockLift.CALLED, check);
                    world.setBlockState(getPos(), state);
                }
            }
            MinecraftForge.EVENT_BUS.post(new EventLiftUpdate(this));
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
        EntityLift tempLift = EntityLift.getLiftFromUUID(liftID, world);
        if (liftID != null && !liftID.equals(empty) && (lift == null || lift.isDead || tempLift != lift))
        {
            lift = tempLift;
            if (lift == null || lift.isDead) return;
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
        if (lift == null)
        {
            for (EnumFacing side : EnumFacing.values())
            {
                TileEntity t = here.getTileEntity(world, side);
                Block b = here.getBlock(world, side);
                if (b == getBlockType() && t instanceof TileEntityLiftAccess)
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
        par1.setInteger("floor", floor);
        par1.setByteArray("sides", sides);
        par1.setByteArray("sidePages", sidePages);
        par1.setBoolean("callPanel", callPanel);
        if (root != null) root.writeToNBT(par1, "root");
        if (lift != null)
        {
            liftID = lift.getPersistentID();
        }
        if (liftID != null)
        {
            par1.setLong("idLess", liftID.getLeastSignificantBits());
            par1.setLong("idMost", liftID.getMostSignificantBits());
        }
        if (copiedState != null)
        {
            NBTTagCompound state = new NBTTagCompound();
            state.setString("K", copiedState.getBlock().getRegistryName().toString());
            state.setInteger("M", copiedState.getBlock().getMetaFromState(copiedState));
            par1.setTag("state", state);
        }
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
        if (world instanceof BlockEntityWorld) return;
        player.connection.sendPacket(getUpdatePacket());
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        return writeToNBT(new NBTTagCompound());
    }
}
