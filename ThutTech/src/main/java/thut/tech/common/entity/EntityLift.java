package thut.tech.common.entity;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import thut.api.entity.blockentity.BlockEntityBase;
import thut.api.entity.blockentity.BlockEntityInteractHandler;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Vector3f;
import thut.core.common.ThutCore;
import thut.core.common.network.EntityUpdate;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.lift.ControllerTile;

public class EntityLift extends BlockEntityBase
{
    public static class LiftTracker
    {
        protected static final Map<UUID, EntityLift> liftMap = Maps.newHashMap();
    }

    public static final EntityType<EntityLift> TYPE = new BlockEntityType<>(EntityLift::new);

    static final DataParameter<Integer> DESTINATIONFLOORDW = EntityDataManager.<Integer> createKey(EntityLift.class,
            DataSerializers.VARINT);
    static final DataParameter<Float>   DESTINATIONYDW     = EntityDataManager.<Float> createKey(EntityLift.class,
            DataSerializers.FLOAT);
    static final DataParameter<Float>   DESTINATIONXDW     = EntityDataManager.<Float> createKey(EntityLift.class,
            DataSerializers.FLOAT);
    static final DataParameter<Float>   DESTINATIONZDW     = EntityDataManager.<Float> createKey(EntityLift.class,
            DataSerializers.FLOAT);
    static final DataParameter<Integer> CURRENTFLOORDW     = EntityDataManager.<Integer> createKey(EntityLift.class,
            DataSerializers.VARINT);

    static final DataParameter<Boolean> CALLEDDW  = EntityDataManager.<Boolean> createKey(EntityLift.class,
            DataSerializers.BOOLEAN);
    public static boolean               ENERGYUSE = false;

    public static int ENERGYCOST = 100;

    public static EntityLift getLiftFromUUID(final UUID liftID, final World world)
    {
        if (world instanceof ServerWorld)
        {
            final Entity e = ((ServerWorld) world).getEntityByUuid(liftID);
            if (e instanceof EntityLift) return (EntityLift) e;
        }
        return LiftTracker.liftMap.get(liftID);
    }

    public IEnergyStorage energy     = null;
    public UUID           owner;
    public double         prevFloorY = 0;
    public double         prevFloor  = 0;
    ControllerTile        current;
    public int[]          floors     = new int[128];

    public boolean[] hasFloors = new boolean[128];

    private final Vector3f velocity = new Vector3f();

    EntitySize size;

    public EntityLift(final EntityType<EntityLift> type, final World par1World)
    {
        super(type, par1World);
        this.ignoreFrustumCheck = true;
        this.hurtResistantTime = 0;
        this.speedUp = TechCore.config.LiftSpeedUp;
        this.speedDown = -TechCore.config.LiftSpeedDown;
    }

    @Override
    public void accelerate()
    {
        // These elevators shouldn't be able to rotate, set this here incase
        // someone else has tried to rotate it.
        this.rotationYaw = 0;
        // Only should run the consume power check on servers.
        if (this.isServerWorld() && !this.consumePower()) this.toMoveY = this.toMoveX = this.toMoveZ = false;
        else
        {
            // Otherwise set it to move if it has a destination.
            this.toMoveX = this.getDestX() != this.posX;
            this.toMoveY = this.getDestY() != this.posY;
            this.toMoveZ = this.getDestZ() != this.posZ;
        }
        if (!(this.toMoveX || this.toMoveY || this.toMoveZ)) this.setCalled(false);

        // Apply damping to velocities if no destination.
        if (!this.toMoveX) this.velocity.x *= 0.5;
        if (!this.toMoveZ) this.velocity.z *= 0.5;
        if (!this.toMoveY) this.velocity.y *= 0.5;

        if (this.getCalled())
        {
            if (this.toMoveY)
            {
                final float destY = this.getDestY();
                // If Sufficiently close (0,01 blocks) just snap the elevator to
                // the destination.
                if (Math.abs(destY - this.posY) < 0.01)
                {
                    this.setPosition(this.posX, destY, this.posZ);
                    this.toMoveY = false;
                    this.velocity.y = 0;
                }
                else
                {
                    // Otherwise accelerate accordingly.
                    final double dy = this.getSpeed(this.posY, destY, this.velocity.y, this.speedUp, this.speedDown);
                    this.velocity.y = (float) dy;
                }
            }
            if (this.toMoveX)
            {
                final float destX = this.getDestX();
                if (Math.abs(destX - this.posX) < 0.01)
                {
                    this.setPosition(destX, this.posY, this.posZ);
                    this.toMoveX = false;
                    this.velocity.x = 0;
                }
                else
                {
                    final double dx = this.getSpeed(this.posX, destX, this.velocity.x, this.speedHoriz,
                            this.speedHoriz);
                    this.velocity.x = (float) dx;
                }
            }
            if (this.toMoveZ)
            {
                final float destZ = this.getDestZ();
                if (Math.abs(destZ - this.posZ) < 0.01)
                {
                    this.setPosition(this.posX, this.posY, destZ);
                    this.toMoveZ = false;
                    this.velocity.z = 0;
                }
                else
                {
                    final double dz = this.getSpeed(this.posZ, destZ, this.velocity.z, this.speedHoriz,
                            this.speedHoriz);
                    this.velocity.z = (float) dz;
                }
            }
        }
        this.setMotion(this.velocity.x, this.velocity.y, this.velocity.z);
    }

    public void call(final int floor)
    {
        if (floor == 0 || floor > this.floors.length || !this.isServerWorld()) return;
        if (this.hasFloors[floor - 1])
        {
            this.callYValue(this.floors[floor - 1]);
            this.setDestinationFloor(floor);
            ThutCore.LOGGER.debug("Lift Called to floor: " + floor);
        }
    }

    public void callYValue(final int yValue)
    {
        this.setDestY(yValue);
    }

    @Override
    protected boolean checkAccelerationConditions()
    {
        return this.consumePower();
    }

    private boolean consumePower()
    {
        if (!EntityLift.ENERGYUSE || !this.getCalled()) return true;
        if (this.energy == null) this.energy = this.getCapability(CapabilityEnergy.ENERGY, null).orElse(null);
        if (this.energy == null) return true;

        boolean power = false;
        final Vector3 bounds = Vector3.getNewVector().set(this.boundMax.subtract(this.boundMin));
        final double volume = bounds.x * bounds.y * bounds.z;
        int energyCost = (int) (Math.abs(this.getDestY() - this.posY) * EntityLift.ENERGYCOST * volume * 0.01);
        energyCost = Math.max(energyCost, 1);
        final int canExtract = this.energy.extractEnergy(energyCost, true);
        if (canExtract == energyCost)
        {
            power = true;
            this.energy.extractEnergy(energyCost, false);
        }
        MinecraftForge.EVENT_BUS.post(new EventLiftConsumePower(this, energyCost));
        if (!power)
        {
            this.setDestinationFloor(-1);
            this.setDestY((float) this.posY);
            this.setCalled(false);
            this.toMoveY = false;
        }
        return power;
    }

    @Override
    protected BlockEntityInteractHandler createInteractHandler()
    {
        return new LiftInteractHandler(this);
    }

    @Override
    public void doMotion()
    {
        if (!this.toMoveX) this.velocity.x = 0;
        if (!this.toMoveY) this.velocity.y = 0;
        if (!this.toMoveZ) this.velocity.z = 0;
        this.setMotion(this.velocity.x, this.velocity.y, this.velocity.z);
        if (this.getCalled()) this.move(MoverType.SELF, this.getMotion());
        else
        {
            final BlockPos pos = this.getPosition();
            this.setPosition(pos.getX() + 0.5, Math.round(this.posY), pos.getZ() + 0.5);
        }
    }

    public boolean getCalled()
    {
        return this.dataManager.get(EntityLift.CALLEDDW);
    }

    /** @return the destinationFloor */
    public int getCurrentFloor()
    {
        return this.dataManager.get(EntityLift.CURRENTFLOORDW);
    }

    /** @return the destinationFloor */
    public int getDestinationFloor()
    {
        return this.dataManager.get(EntityLift.DESTINATIONFLOORDW);
    }

    /** @return the destinationFloor */
    public float getDestX()
    {
        return this.dataManager.get(EntityLift.DESTINATIONXDW);
    }

    /** @return the destinationFloor */
    public float getDestY()
    {
        return this.dataManager.get(EntityLift.DESTINATIONYDW);
    }

    /** @return the destinationFloor */
    public float getDestZ()
    {
        return this.dataManager.get(EntityLift.DESTINATIONZDW);
    }

    @Override
    public EntitySize getSize(final Pose pose)
    {
        if (this.size == null) this.size = EntitySize.fixed(1 + this.getMax().getX() - this.getMin().getX(), this
                .getMax().getY());
        return this.size;
    }

    @Override
    public void onAddedToWorld()
    {
        super.onAddedToWorld();
        LiftTracker.liftMap.put(this.getUniqueID(), this);
    }

    @Override
    protected void onGridAlign()
    {
        this.setCalled(false);
        final BlockPos pos = this.getPosition();
        this.setPosition(pos.getX() + 0.5, Math.round(this.posY), pos.getZ() + 0.5);
        EntityUpdate.sendEntityUpdate(this);
    }

    @Override
    protected void preColliderTick()
    {
    }

    @Override
    public void readAdditional(final CompoundNBT arg0)
    {
        super.readAdditional(arg0);
        if (arg0.hasUniqueId("owner")) this.owner = arg0.getUniqueId("owner");
    }

    @Override
    protected void registerData()
    {
        super.registerData();
        this.dataManager.register(EntityLift.DESTINATIONFLOORDW, Integer.valueOf(0));
        this.dataManager.register(EntityLift.DESTINATIONYDW, Float.valueOf(0));
        this.dataManager.register(EntityLift.DESTINATIONXDW, Float.valueOf(0));
        this.dataManager.register(EntityLift.DESTINATIONZDW, Float.valueOf(0));
        this.dataManager.register(EntityLift.CURRENTFLOORDW, Integer.valueOf(-1));
        this.dataManager.register(EntityLift.CALLEDDW, Boolean.FALSE);
    }

    @Override
    public void remove()
    {
        super.remove();
        LiftTracker.liftMap.remove(this.getUniqueID());
    }

    private void setCalled(final boolean called)
    {
        this.dataManager.set(EntityLift.CALLEDDW, called);
    }

    /**
     * @param currentFloor
     *            the destinationFloor to set
     */
    public void setCurrentFloor(final int currentFloor)
    {
        this.dataManager.set(EntityLift.CURRENTFLOORDW, Integer.valueOf(currentFloor));
    }

    /**
     * @param destinationFloor
     *            the destinationFloor to set
     */
    public void setDestinationFloor(final int destinationFloor)
    {
        this.dataManager.set(EntityLift.DESTINATIONFLOORDW, Integer.valueOf(destinationFloor));
    }

    /**
     * @param dest
     *            the destinationFloor to set
     */
    public void setDestX(final float dest)
    {
        this.dataManager.set(EntityLift.DESTINATIONXDW, Float.valueOf(dest));
        this.dataManager.set(EntityLift.DESTINATIONYDW, Float.valueOf((float) this.posY));
        this.dataManager.set(EntityLift.DESTINATIONZDW, Float.valueOf((float) this.posZ));
        this.setCalled(true);
    }

    /**
     * @param dest
     *            the destinationFloor to set
     */
    public void setDestY(final float dest)
    {
        this.dataManager.set(EntityLift.DESTINATIONYDW, Float.valueOf(dest));
        this.dataManager.set(EntityLift.DESTINATIONXDW, Float.valueOf((float) this.posX));
        this.dataManager.set(EntityLift.DESTINATIONZDW, Float.valueOf((float) this.posZ));
        this.setCalled(true);
    }

    /**
     * @param dest
     *            the destinationFloor to set
     */
    public void setDestZ(final float dest)
    {
        this.dataManager.set(EntityLift.DESTINATIONZDW, Float.valueOf(dest));
        this.dataManager.set(EntityLift.DESTINATIONYDW, Float.valueOf((float) this.posY));
        this.dataManager.set(EntityLift.DESTINATIONXDW, Float.valueOf((float) this.posX));
        this.setCalled(true);
    }

    public void setFoor(final ControllerTile te, final int floor)
    {
        if (te != null)
        {
            boolean changed = false;
            final int prev = te.floor;
            if (floor > 0)
            {
                this.floors[floor - 1] = te.getPos().getY() - 2;
                this.hasFloors[floor - 1] = true;
                changed = true;
            }
            if (changed)
            {
                if (prev != 0 && prev != floor) this.hasFloors[prev - 1] = false;
                if (this.isServerWorld()) EntityUpdate.sendEntityUpdate(this);
            }
        }
        else
        {
            this.floors[floor - 1] = 0;
            this.hasFloors[floor - 1] = false;
            if (this.isServerWorld()) EntityUpdate.sendEntityUpdate(this);
        }

    }

    @Override
    public void setItemStackToSlot(final EquipmentSlotType slotIn, final ItemStack stack)
    {
    }

    @Override
    public void setSize(final EntitySize size)
    {
        this.size = size;
    }

    @Override
    public void setTiles(final TileEntity[][][] tiles)
    {
        super.setTiles(tiles);
        for (final TileEntity[][] tileArrArr : tiles)
            for (final TileEntity[] tileArr : tileArrArr)
                for (final TileEntity tile : tileArr)
                    if (tile instanceof ControllerTile)
                    {
                        ((ControllerTile) tile).setLift(this);
                        ((ControllerTile) tile).setWorldObj((World) this.getFakeWorld());
                    }
    }

    @Override
    public void writeAdditional(final CompoundNBT arg0)
    {
        super.writeAdditional(arg0);
        if (this.owner != null) arg0.putUniqueId("owner", this.owner);
    }
}
