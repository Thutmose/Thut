package thut.tech.common.blocks.lift;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import io.netty.buffer.Unpooled;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeTileEntity;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.entity.blockentity.world.client.IBlockEntityWorld;
import thut.api.maths.Vector3;
import thut.core.common.network.TileUpdate;
import thut.tech.common.TechCore;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.network.PacketLift;

public class ControllerTile extends TileEntity implements ITickableTileEntity// ,
// SimpleComponent
{
    public static TileEntityType<? extends TileEntity> TYPE;

    public int                    power        = 0;
    public int                    prevPower    = 1;
    public EntityLift             lift;
    public BlockState             copiedState  = null;
    boolean                       listNull     = false;
    List<Entity>                  list         = new ArrayList<>();
    Vector3                       here;
    public ControllerTile         rootNode;
    public Vector<ControllerTile> connected    = new Vector<>();
    Direction                     sourceSide;
    boolean                       loaded       = false;
    public int                    floor        = 0;
    public int                    calledYValue = -1;
    public int                    calledFloor  = 0;
    public int                    currentFloor = 0;
    public UUID                   liftID       = null;
    UUID                          empty        = new UUID(0, 0);
    private byte[]                sides        = new byte[6];
    private byte[]                sidePages    = new byte[6];
    int                           tries        = 0;
    public boolean                toClear      = false;
    public boolean                first        = true;
    public boolean                read         = false;
    public boolean                redstone     = true;
    public boolean                powered      = false;
    public boolean[]              callFaces    = new boolean[6];
    public boolean[]              editFace     = new boolean[6];
    public boolean[]              floorDisplay = new boolean[6];
    public boolean                callPanel    = false;

    // Used for limiting how often checks for connected controllers are done.
    private int tick = 0;

    public ControllerTile()
    {
        super(ControllerTile.TYPE);
    }

    public ControllerTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    public void buttonPress(final int button, final boolean callPanel)
    {
        if (callPanel && this.lift != null) this.lift.call(this.floor);
        else if (button != 0 && button <= this.lift.floors.length && this.lift != null && this.lift.hasFloors[button
                - 1])
        {
            if (button == this.floor)
            {
            }
            else if (this.lift.getCurrentFloor() == this.floor) this.lift.setCurrentFloor(-1);
            this.lift.call(button);
        }
    }

    public boolean checkSides()
    {
        final List<EntityLift> check = this.world.getEntitiesWithinAABB(EntityLift.class, new AxisAlignedBB(this
                .getPos().getX() + 0.5 - 1, this.getPos().getY(), this.getPos().getZ() + 0.5 - 1, this.getPos().getX()
                        + 0.5 + 1, this.getPos().getY() + 1, this.getPos().getZ() + 0.5 + 1));
        if (check != null && check.size() > 0)
        {
            this.lift = check.get(0);
            this.liftID = this.lift.getUniqueID();
        }
        return !(check == null || check.isEmpty());
    }

    public String connectionInfo()
    {
        final String ret = "";
        return ret;
    }

    public boolean doButtonClick(final LivingEntity clicker, final Direction side, final float hitX, final float hitY,
            final float hitZ)
    {
        if (this.liftID != null && !this.liftID.equals(this.empty) && this.lift != EntityLift.getLiftFromUUID(
                this.liftID, this.world)) this.lift = EntityLift.getLiftFromUUID(this.liftID, this.world);
        final int button = this.getButtonFromClick(side, hitX, hitY, hitZ);

        final boolean valid = this.lift != null && this.lift.hasFloors[button - 1];
        if (this.lift != null && this.isSideOn(side)) if (this.editFace[side.ordinal()])
        {
            if (!this.getWorld().isRemote)
            {
                String message = "msg.callPanel.name";
                switch (button)
                {
                case 1:
                    this.callFaces[side.ordinal()] = !this.callFaces[side.ordinal()];
                    this.floorDisplay[side.ordinal()] = false;
                    clicker.sendMessage(new TranslationTextComponent(message, this.callFaces[side.ordinal()]));
                    break;
                case 2:
                    this.floorDisplay[side.ordinal()] = !this.floorDisplay[side.ordinal()];
                    this.callFaces[side.ordinal()] = false;
                    message = "msg.floorDisplay.name";
                    clicker.sendMessage(new TranslationTextComponent(message, this.floorDisplay[side.ordinal()]));
                    break;
                case 16:
                    this.editFace[side.ordinal()] = false;
                    message = "msg.editMode.name";
                    clicker.sendMessage(new TranslationTextComponent(message, false));
                    break;
                }
                if (clicker instanceof ServerPlayerEntity) this.sendUpdate((ServerPlayerEntity) clicker);
            }
            return true;
        }
        else
        {
            if (this.floorDisplay[side.ordinal()]) return false;
            if (this.getWorld() instanceof IBlockEntityWorld)
            {
                this.buttonPress(button, this.callFaces[side.ordinal()]);
                this.calledFloor = this.lift.getDestinationFloor();
            }
            else if (this.getWorld().isRemote)
            {
                final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(32));
                buffer.writeBlockPos(this.getPos());
                buffer.writeInt(button);
                buffer.writeBoolean(this.callFaces[side.ordinal()]);
                final PacketLift packet = new PacketLift(buffer);
                TechCore.packets.sendToServer(packet);
            }
        }
        if (clicker instanceof ServerPlayerEntity) this.sendUpdate((ServerPlayerEntity) clicker);
        return valid;
    }

    public int getButtonFromClick(final Direction side, double x, double y, double z)
    {
        int ret = 0;
        x = x % 1f;
        y = y % 1f;
        z = z % 1f;
        x = Math.abs(x);
        y = Math.abs(y);
        z = Math.abs(z);
        final int page = this.getSidePage(side);
        switch (side.getIndex())
        {
        case 0:
        {
            return 0 + 16 * page;
        }
        case 1:
        {
            ret = 1 + (int) ((1 - x) * 4 % 4) + 4 * (int) ((1 - z) * 4 % 4);
            return ret + 16 * page;
        }
        case 2:
        {
            ret = 1 + (int) ((1 - x) * 4 % 4) + 4 * (int) ((1 - y) * 4 % 4);
            return ret + 16 * page;
        }
        case 3:
        {
            ret = 1 + (int) (x * 4 % 4) + 4 * (int) ((1 - y) * 4 % 4);
            return ret + 16 * page;
        }
        case 4:
        {
            ret = 1 + 4 * (int) ((1 - y) * 4 % 4) + (int) (z * 4 % 4);
            return ret + 16 * page;
        }
        case 5:
        {
            ret = 1 + 4 * (int) ((1 - y) * 4 % 4) + (int) ((1 - z) * 4 % 4);
            return ret + 16 * page;
        }
        default:
        {
            return 0 + 16 * page;
        }

        }

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        final AxisAlignedBB bb = IForgeTileEntity.INFINITE_EXTENT_AABB;
        return bb;
    }

    public int getSidePage(final Direction side)
    {
        return this.sidePages[side.getIndex()];
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        final CompoundNBT tag = new CompoundNBT();
        return this.write(tag);
    }

    @Override
    public void handleUpdateTag(final CompoundNBT tag)
    {
        this.read(tag);
    }

    public boolean isSideOn(final Direction side)
    {
        final int state = 1;
        final byte byte0 = this.sides[side.getIndex()];
        return (byte0 & state) != 0;
    }

    @Override
    public void read(final CompoundNBT par1)
    {
        super.read(par1);
        this.floor = par1.getInt("floor");
        this.liftID = new UUID(par1.getLong("idMost"), par1.getLong("idLess"));
        this.sides = par1.getByteArray("sides");
        for (final Direction face : Direction.Plane.HORIZONTAL)
            this.callFaces[face.ordinal()] = par1.getBoolean(face + "Call");
        for (final Direction face : Direction.Plane.HORIZONTAL)
            this.editFace[face.ordinal()] = par1.getBoolean(face + "Edit");
        for (final Direction face : Direction.Plane.HORIZONTAL)
            this.floorDisplay[face.ordinal()] = par1.getBoolean(face + "Display");
        if (this.sides.length != 6) this.sides = new byte[6];
        this.sidePages = par1.getByteArray("sidePages");
        if (this.sidePages.length != 6) this.sidePages = new byte[6];
        if (par1.contains("state"))
        {
            final CompoundNBT state = par1.getCompound("state");
            this.copiedState = NBTUtil.readBlockState(state);
        }
    }

    public void sendUpdate(final ServerPlayerEntity player)
    {
        if (this.world instanceof IBlockEntityWorld<?>) return;
        TileUpdate.sendUpdate(this);
    }

    public void setFloor(final int floor)
    {
        if (this.lift != null && floor <= this.lift.floors.length && floor > 0)
        {
            this.lift.setFoor(this, floor);
            this.floor = floor;
            this.markDirty();
        }
    }

    public void setLift(final EntityLift lift)
    {
        this.lift = lift;
        this.liftID = lift.getUniqueID();
        if (this.world != null && !this.world.isRemote) TileUpdate.sendUpdate(this);
    }

    public void setSide(final Direction side, final boolean flag)
    {
        final int state = 1;
        final byte byte0 = this.sides[side.getIndex()];

        if (side.getIndex() < 2) return;

        if (flag) this.sides[side.getIndex()] = (byte) (byte0 | state);
        else this.sides[side.getIndex()] = (byte) (byte0 & -state - 1);
        this.markDirty();
    }

    public void setSidePage(final Direction side, final int page)
    {
        this.sidePages[side.getIndex()] = (byte) page;
    }

    /** Sets the worldObj for this tileEntity. */
    public void setWorldObj(final World worldIn)
    {
        this.world = worldIn;
        if (worldIn instanceof IBlockEntityWorld)
        {
            final IBlockEntity blockEntity = ((IBlockEntityWorld<?>) worldIn).getBlockEntity();
            if (blockEntity instanceof EntityLift) this.setLift((EntityLift) blockEntity);
        }
    }

    @Override
    public void tick()
    {
        if (this.here == null) this.here = Vector3.getNewVector();
        this.here.set(this);

        if (this.lift != null && this.floor > 0) this.lift.hasFloors[this.floor - 1] = true;
        if (this.getWorld().isRemote) return;
        if (this.world instanceof IBlockEntityWorld) return;

        if (this.lift == null || !this.lift.isAlive())
        {
            this.calledYValue = -1;
            this.calledFloor = 0;
            this.currentFloor = 0;
        }

        if (this.lift != null && !this.world.isRemote)
        {
            // This is whether the lift is currently at this floor, so redstone
            // should be emitted.
            boolean check = this.lift.getCurrentFloor() == this.floor && (int) (this.lift.getMotion().y * 16) == 0;

            BlockState state = this.world.getBlockState(this.getPos());
            boolean old = state.get(ControllerBlock.CURRENT);
            boolean callPanel = false;
            if (!old && !this.lift.getCalled()) for (final Direction face : Direction.Plane.HORIZONTAL)
                callPanel |= this.callFaces[face.ordinal()];
            // Call panels should only respond to redstone signals if they are
            // not supposed to be emitting one themselves.
            if (callPanel && !old && !this.lift.getCalled() && !check) if (this.world.isBlockPowered(this.getPos()))
                this.lift.call(this.floor);

            // If state has changed, change the blockstate as well. only do this
            // if it has changed to prevent too many changes to state.
            if (check != old)
            {
                state = state.with(ControllerBlock.CURRENT, check);
                this.world.setBlockState(this.getPos(), state);
            }

            // Check to see if the called state needs to be changed.
            if (this.lift.getMotion().y == 0 || this.lift.getDestinationFloor() == this.floor)
            {
                old = state.get(ControllerBlock.CALLED);
                check = this.lift.getDestinationFloor() == this.floor;
                if (check != old)
                {
                    state = state.with(ControllerBlock.CALLED, check);
                    this.world.setBlockState(this.getPos(), state);
                }
            }
            MinecraftForge.EVENT_BUS.post(new ControllerUpdate(this));
        }

        if (this.lift != null && this.floor > 0)
        {
            // Set lifts current floor to this if it is in the area of the
            // floor.
            if ((int) Math.round(this.lift.posY) == this.lift.floors[this.floor - 1]) this.lift.setCurrentFloor(
                    this.floor);
            else if (this.lift.getCurrentFloor() == this.floor) this.lift.setCurrentFloor(-1);

            // Sets the values used for rendering colours over numbers on the
            // display.
            this.calledFloor = this.lift.getDestinationFloor();
            this.currentFloor = this.lift.getCurrentFloor();
        }

        if (this.lift == null && this.liftID != null)
        {
            // Find lift if existing lift isn't found.
            final EntityLift tempLift = EntityLift.getLiftFromUUID(this.liftID, this.world);
            if (this.liftID != null && !this.liftID.equals(this.empty) && (this.lift == null || !this.lift.isAlive()
                    || tempLift != this.lift))
            {
                this.lift = tempLift;
                if (this.lift == null || !this.lift.isAlive()) return;

                // Make sure that lift's floor is this one if it doesn't have
                // one defined.
                if (this.floor > 0 && !this.lift.hasFloors[this.floor - 1]) this.setFloor(this.floor);
            }
        }
        // Cleanup floor if the lift is gone.
        if (this.floor > 0 && (this.lift == null || !this.lift.isAlive()))
        {
            this.lift = null;
            this.floor = 0;
        }

        // Scan sides for a controller which actually has a lift attached, and
        // attach self to that floor.
        if (this.lift == null && this.tick++ % 50 == 0) for (final Direction side : Direction.values())
        {
            final TileEntity t = this.here.getTileEntity(this.world, side);
            this.here.getBlock(this.world, side);
            if (t instanceof ControllerTile)
            {
                final ControllerTile te = (ControllerTile) t;
                if (te.lift != null)
                {
                    this.lift = te.lift;
                    this.floor = te.floor;
                    this.markDirty();
                    break;
                }
            }
        }
    }

    @Override
    public CompoundNBT write(final CompoundNBT par1)
    {
        super.write(par1);
        par1.putInt("floor", this.floor);
        par1.putByteArray("sides", this.sides);
        par1.putByteArray("sidePages", this.sidePages);
        for (final Direction face : Direction.Plane.HORIZONTAL)
            par1.putBoolean(face + "Call", this.callFaces[face.ordinal()]);
        for (final Direction face : Direction.Plane.HORIZONTAL)
            par1.putBoolean(face + "Edit", this.editFace[face.ordinal()]);
        for (final Direction face : Direction.Plane.HORIZONTAL)
            par1.putBoolean(face + "Display", this.floorDisplay[face.ordinal()]);
        if (this.lift != null) this.liftID = this.lift.getUniqueID();
        if (this.liftID != null)
        {
            par1.putLong("idLess", this.liftID.getLeastSignificantBits());
            par1.putLong("idMost", this.liftID.getMostSignificantBits());
        }
        if (this.copiedState != null)
        {
            final CompoundNBT state = NBTUtil.writeBlockState(this.copiedState);
            par1.put("state", state);
        }
        return par1;
    }

    // Open Computers stuff here, possibly will move this to a compat class or
    // something? TODO OC Stuff
    //
    //
    // @Override
    // public String getComponentName()
    // {
    // return "lift";
    // }
    //
    //
    // /*
    // * Calls lift to specified Floor
    // */
    // @Callback(doc = "function(floor:number) -- Calls the Lift to the
    // specified Floor")
    // @Optional.Method(modid = "opencomputers")
    // public Object[] callFloor(Context context, Arguments args) throws
    // Exception
    // {
    // if (lift != null)
    // {
    // lift.call(args.checkInteger(0));
    // return new Object[] {};
    // }
    // throw new Exception("no connected lift");
    // }
    //
    // /*
    // * Calls lift to specified Y value
    // */
    // @Callback(doc = "function(yValue:number) -- Calls the Lift to the
    // specified Y level")
    //
    // @Optional.Method(modid = "opencomputers")
    // public Object[] callYValue(Context context, Arguments args) throws
    // Exception
    // {
    // if (lift != null)
    // {
    // lift.setDestY(args.checkInteger(0));
    // return new Object[] {};
    // }
    // throw new Exception("no connected lift");
    // }
    //
    // /*
    // * Calls lift to specified Y value
    // */
    // @Callback(doc = "function(xValue:number) -- Calls the Lift to
    // thespecified X location")
    //
    // @Optional.Method(modid = "opencomputers")
    // public Object[] callXValue(Context context, Arguments args) throws
    // Exception
    // {
    // if (lift != null)
    // {
    // // +0.5f as the elevator is in centre of blocks.
    // lift.setDestX(args.checkInteger(0) + 0.5f);
    // return new Object[] {};
    // }
    // throw new Exception("no connected lift");
    // }
    //
    // /*
    // * Calls lift to specified Y value
    // */
    // @Callback(doc = "function(zValue:number) -- Calls the Lift to
    // thespecified Z location")
    //
    // @Optional.Method(modid = "opencomputers")
    // public Object[] callZValue(Context context, Arguments args) throws
    // Exception
    // {
    // if (lift != null)
    // {
    // // +0.5f as the elevator is in centre of blocks.
    // lift.setDestZ(args.checkInteger(0) + 0.5f);
    // return new Object[] {};
    // }
    // throw new Exception("no connected lift");
    // }
    //
    // /*
    // * Sets floor associated with this block
    // */
    // @Callback(doc = "function(floor:number) -- Sets the floor assosiated
    // tothe Controller")
    // @Optional.Method(modid = "opencomputers")
    // public Object[] setFloor(Context context, Arguments args)
    // {
    // floor = args.checkInteger(0);
    // return new Object[] { floor };
    // }
    //
    // /*
    // * Returns the Yvalue of the lift.
    // */
    // @Callback(doc = "returns the current Y value of the lift.")
    // @Optional.Method(modid = "opencomputers")
    // public Object[] getYValue(Context context, Arguments args) throws
    // Exception
    // {
    // if (lift != null) return new Object[] { (float) lift.posY };
    //
    // throw new Exception("no connected lift");
    // }
    //
    // /*
    // * Returns the Yvalue of the lift.
    // */
    // @Callback(doc = "returns the current X value of the lift.")
    // @Optional.Method(modid = "opencomputers")
    // public Object[] getXValue(Context context, Arguments args) throws
    // Exception
    // {
    // if (lift != null) return new Object[] { (float) lift.posX };
    //
    // throw new Exception("no connected lift");
    // }
    //
    // /*
    // * Returns the Yvalue of the lift.
    // */
    // @Callback(doc = "returns the current Z value of the lift.")
    // @Optional.Method(modid = "opencomputers")
    // public Object[] getZValue(Context context, Arguments args) throws
    // Exception
    // {
    // if (lift != null) return new Object[] { (float) lift.posZ };
    //
    // throw new Exception("no connected lift");
    // }
    //
    // /*
    // * Returns floor associated with this block
    // */
    // @Callback(doc = "returns the Floor assigned to the Controller")
    // @Optional.Method(modid = "opencomputers")
    // public Object[] getFloor(Context context, Arguments args)
    // {
    // return new Object[] { floor };
    // }
    //
    // /*
    // * Returns the Y value of the controller for the specified floor
    // */
    // @Callback(doc = "function(floor:number) -- returns the y value of the
    // specified floor")
    // @Optional.Method(modid = "opencomputers")
    // public Object[] getFloorYValue(Context context, Arguments args) throws
    // Exception
    // {
    // if (lift != null)
    // {
    // int floor = args.checkInteger(0);
    //
    // if (floor > 0 && floor <= lift.floors.length)
    // {
    // int value = lift.floors[floor - 1];
    // if (!lift.hasFloors[floor - 1]) throw new Exception("floor " + floor + "
    // is not assigned");
    // return new Object[] { value };
    // }
    // throw new Exception("floor out of bounds");
    // }
    // throw new Exception("no connected lift");
    // }
    //
    // /*
    // * Returns floor associated with this block
    // */
    // @Callback(doc = "returns if the elevator is not currently called to
    // afloor")
    // @Optional.Method(modid = "opencomputers")
    // public Object[] isReady(Context context, Arguments args) throws Exception
    // {
    // if (lift != null) { return new Object[] { !lift.getCalled() }; }
    // throw new Exception("no connected lift");
    // }

}
