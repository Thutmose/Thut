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
import thut.api.entity.blockentity.world.IBlockEntityWorld;
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
    private EntityLift            lift;
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
        if (callPanel && this.getLift() != null) this.getLift().call(this.floor);
        else if (button != 0 && button <= this.getLift().floors.length && this.getLift() != null && this
                .getLift().hasFloors[button - 1])
        {
            if (button == this.floor)
            {
            }
            else if (this.getLift().getCurrentFloor() == this.floor) this.getLift().setCurrentFloor(-1);
            this.getLift().call(button);
        }
    }

    public boolean checkSides()
    {
        final List<EntityLift> check = this.world.getEntitiesWithinAABB(EntityLift.class, new AxisAlignedBB(this
                .getPos().getX() + 0.5 - 1, this.getPos().getY(), this.getPos().getZ() + 0.5 - 1, this.getPos().getX()
                        + 0.5 + 1, this.getPos().getY() + 1, this.getPos().getZ() + 0.5 + 1));
        if (check != null && check.size() > 0)
        {
            this.setLift(check.get(0));
            this.liftID = this.getLift().getUniqueID();
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
        if (this.liftID != null && !this.liftID.equals(this.empty) && this.getLift() != EntityLift.getLiftFromUUID(
                this.liftID, this.world)) this.setLift(EntityLift.getLiftFromUUID(this.liftID, this.world));
        final int button = this.getButtonFromClick(side, hitX, hitY, hitZ);
        final boolean valid = this.getLift() != null && this.getLift().hasFloors[button - 1];
        if (this.getLift() != null && this.isSideOn(side)) if (this.editFace[side.ordinal()])
        {
            if (!this.getWorld().isRemote)
            {
                String message = "msg.callPanel";
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
                    message = "msg.floorDisplay";
                    clicker.sendMessage(new TranslationTextComponent(message, this.floorDisplay[side.ordinal()]));
                    break;
                case 16:
                    this.editFace[side.ordinal()] = false;
                    message = "msg.editMode";
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
                this.calledFloor = this.getLift().getDestinationFloor();
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
        x -= this.getPos().getX();
        y -= this.getPos().getY();
        z -= this.getPos().getZ();

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
        if (this.world instanceof IBlockEntityWorld) return;
        TileUpdate.sendUpdate(this);
    }

    public void setFloor(final int floor)
    {
        if (this.getLift() != null && floor <= this.getLift().floors.length && floor > 0)
        {
            this.getLift().setFoor(this, floor);
            this.floor = floor;
            this.markDirty();
        }
    }

    public void setLift(final EntityLift lift)
    {
        this.lift = lift;
        if (lift != null) this.liftID = lift.getUniqueID();
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
            // TODO replace this with something like a built in tag?
            final IBlockEntity blockEntity = ((IBlockEntityWorld) worldIn).getBlockEntity();
            if (blockEntity instanceof EntityLift) this.setLift((EntityLift) blockEntity);
        }
    }

    @Override
    public void tick()
    {
        if (this.here == null) this.here = Vector3.getNewVector();
        this.here.set(this);

        if (this.getLift() != null && this.floor > 0) this.getLift().hasFloors[this.floor - 1] = true;
        if (this.getWorld().isRemote) return;
        if (this.world instanceof IBlockEntityWorld) return;

        if (this.getLift() == null || !this.getLift().isAlive())
        {
            this.calledYValue = -1;
            this.calledFloor = 0;
            this.currentFloor = 0;
        }

        if (this.getLift() != null && !this.world.isRemote)
        {
            // This is whether the lift is currently at this floor, so redstone
            // should be emitted.
            boolean check = this.getLift().getCurrentFloor() == this.floor && (int) (this.getLift().getMotion().y
                    * 16) == 0;

            BlockState state = this.world.getBlockState(this.getPos());
            boolean old = state.get(ControllerBlock.CURRENT);
            boolean callPanel = false;
            if (!old && !this.getLift().getCalled()) for (final Direction face : Direction.Plane.HORIZONTAL)
                callPanel |= this.callFaces[face.ordinal()];
            // Call panels should only respond to redstone signals if they are
            // not supposed to be emitting one themselves.
            if (callPanel && !old && !this.getLift().getCalled() && !check) if (this.world.isBlockPowered(this
                    .getPos())) this.getLift().call(this.floor);

            // If state has changed, change the blockstate as well. only do this
            // if it has changed to prevent too many changes to state.
            if (check != old)
            {
                state = state.with(ControllerBlock.CURRENT, check);
                this.world.setBlockState(this.getPos(), state);
            }

            // Check to see if the called state needs to be changed.
            if (this.getLift().getMotion().y == 0 || this.getLift().getDestinationFloor() == this.floor)
            {
                old = state.get(ControllerBlock.CALLED);
                check = this.getLift().getDestinationFloor() == this.floor;
                if (check != old)
                {
                    state = state.with(ControllerBlock.CALLED, check);
                    this.world.setBlockState(this.getPos(), state);
                }
            }
            MinecraftForge.EVENT_BUS.post(new ControllerUpdate(this));
        }

        if (this.getLift() != null && this.floor > 0)
        {
            // Set lifts current floor to this if it is in the area of the
            // floor.
            if ((int) Math.round(this.getLift().posY) == this.getLift().floors[this.floor - 1]) this.getLift()
                    .setCurrentFloor(this.floor);
            else if (this.getLift().getCurrentFloor() == this.floor) this.getLift().setCurrentFloor(-1);

            // Sets the values used for rendering colours over numbers on the
            // display.
            this.calledFloor = this.getLift().getDestinationFloor();
            this.currentFloor = this.getLift().getCurrentFloor();
        }

        // Cleanup floor if the lift is gone.
        if (this.floor > 0 && (this.getLift() == null || !this.getLift().isAlive()))
        {
            this.setLift(null);
            this.floor = 0;
        }

        // Scan sides for a controller which actually has a lift attached, and
        // attach self to that floor.
        if (this.getLift() == null && this.tick++ % 50 == 0) for (final Direction side : Direction.values())
        {
            final TileEntity t = this.here.getTileEntity(this.world, side);
            this.here.getBlock(this.world, side);
            if (t instanceof ControllerTile)
            {
                final ControllerTile te = (ControllerTile) t;
                if (te.getLift() != null)
                {
                    this.setLift(te.getLift());
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

    /**
     * @return the lift
     */
    public EntityLift getLift()
    {
        if (this.liftID == null) return null;
        else if (this.lift == null)
        {
            this.lift = EntityLift.getLiftFromUUID(this.liftID, this.getWorld());

            if (this.lift == null) this.setLift(null);
            else
            {
                this.setLift(this.lift);
                // Make sure that lift's floor is this one if it doesn't have
                // one defined.
                if (this.floor > 0 && !this.getLift().hasFloors[this.floor - 1]) this.setFloor(this.floor);
            }
        }
        return this.lift;
    }

}
