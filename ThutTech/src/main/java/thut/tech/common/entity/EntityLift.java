package thut.tech.common.entity;

import java.util.List;
import java.util.UUID;

import javax.vecmath.Vector3f;

import com.google.common.base.Predicate;

import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import thut.api.entity.blockentity.BlockEntityBase;
import thut.api.entity.blockentity.BlockEntityInteractHandler;
import thut.api.entity.blockentity.BlockEntityWorld;
import thut.api.maths.Vector3;
import thut.api.network.PacketHandler;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.handlers.ConfigHandler;

public class EntityLift extends BlockEntityBase
{
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
    static final DataParameter<Boolean> CALLEDDW           = EntityDataManager.<Boolean> createKey(EntityLift.class,
            DataSerializers.BOOLEAN);

    public static boolean               ENERGYUSE          = false;
    public static int                   ENERGYCOST         = 100;

    int                                 energy             = 0;
    public UUID                         owner;
    public double                       prevFloorY         = 0;
    public double                       prevFloor          = 0;
    TileEntityLiftAccess                current;
    public int[]                        floors             = new int[128];
    public boolean[]                    hasFloors          = new boolean[128];
    private Vector3f                    velocity           = new Vector3f();

    public EntityLift(World par1World)
    {
        super(par1World);
        this.ignoreFrustumCheck = true;
        this.hurtResistantTime = 0;
        this.isImmuneToFire = true;
        speedUp = ConfigHandler.LiftSpeedUp;
        speedDown = -ConfigHandler.LiftSpeedDown;
    }

    public EntityLift(World world, double x, double y, double z)
    {
        this(world);
        this.setPosition(x, y, z);
    }

    @Override
    protected boolean checkAccelerationConditions()
    {
        return consumePower();
    }

    @Override
    protected BlockEntityInteractHandler createInteractHandler()
    {
        return new LiftInteractHandler(this);
    }

    @Override
    public void accelerate()
    {
        // These elevators shouldn't be able to rotate, set this here incase
        // someone else has tried to rotate it.
        this.rotationYaw = 0;
        // Only should run the consume power check on servers.
        if (isServerWorld() && !consumePower())
        {
            toMoveY = toMoveX = toMoveZ = false;
        }
        else
        {
            // Otherwise set it to move if it has a destination.
            toMoveX = getDestX() != posX;
            toMoveY = getDestY() != posY;
            toMoveZ = getDestZ() != posZ;
        }

        // Apply damping to velocities if no destination.
        if (!toMoveX) velocity.x *= 0.5;
        if (!toMoveZ) velocity.z *= 0.5;
        if (!toMoveY) velocity.y *= 0.5;

        if (getCalled())
        {
            if (toMoveY)
            {
                float destY = getDestY();
                // If Sufficiently close (0,01 blocks) just snap the elevator to
                // the destination.
                if (Math.abs(destY - posY) < 0.01)
                {
                    setPosition(posX, destY, posZ);
                    toMoveY = false;
                    velocity.y = 0;
                }
                else
                {
                    // Otherwise accelerate accordingly.
                    double dy = getSpeed(posY, destY, velocity.y, speedUp, speedDown);
                    velocity.y = (float) dy;
                }
            }
            if (toMoveX)
            {
                float destX = getDestX();
                if (Math.abs(destX - posX) < 0.01)
                {
                    setPosition(destX, posY, posZ);
                    toMoveX = false;
                    velocity.x = 0;
                }
                else
                {
                    double dx = getSpeed(posX, destX, velocity.x, speedHoriz, speedHoriz);
                    velocity.x = (float) dx;
                }
            }
            if (toMoveZ)
            {
                float destZ = getDestZ();
                if (Math.abs(destZ - posZ) < 0.01)
                {
                    setPosition(posX, posY, destZ);
                    toMoveZ = false;
                    velocity.z = 0;
                }
                else
                {
                    double dz = getSpeed(posZ, destZ, velocity.z, speedHoriz, speedHoriz);
                    velocity.z = (float) dz;
                }
            }
        }

        motionX = velocity.x;
        motionY = velocity.y;
        motionZ = velocity.z;
    }

    public void call(int floor)
    {
        if (floor == 0 || floor > floors.length) { return; }
        if (hasFloors[floor - 1])
        {
            callYValue(floors[floor - 1]);
            setDestinationFloor(floor);
        }
    }

    public void callYValue(int yValue)
    {
        setDestY(yValue);
    }

    private boolean consumePower()
    {
        if (!ENERGYUSE || !getCalled()) return true;
        boolean power = false;
        Vector3 bounds = Vector3.getNewVector().set(boundMax.subtract(boundMin));
        double volume = bounds.x * bounds.y * bounds.z;
        double energyCost = Math.abs(getDestY() - posY) * ENERGYCOST * volume * 0.01;
        energyCost = Math.max(energyCost, 1);
        power = (energy = (int) (energy - energyCost)) > 0;
        if (energy < 0) energy = 0;
        MinecraftForge.EVENT_BUS.post(new EventLiftConsumePower(this, (long) energyCost));
        if (!power)
        {
            this.setDestinationFloor(-1);
            this.setDestY((float) posY);
            toMoveY = false;
        }
        return power;
    }

    public int getEnergy()
    {
        return energy;
    }

    public void setEnergy(int energy)
    {
        this.energy = energy;
    }

    @Override
    public void doMotion()
    {
        if (!toMoveX) motionX = velocity.x = 0;
        if (!toMoveY) motionY = velocity.y = 0;
        if (!toMoveZ) motionZ = velocity.z = 0;
        if (getCalled())
        {
            this.move(MoverType.SELF, velocity.x, velocity.y, velocity.z);
        }
        else
        {
            BlockPos pos = getPosition();
            setPosition(pos.getX() + 0.5, Math.round(posY), pos.getZ() + 0.5);
        }
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataManager.register(DESTINATIONFLOORDW, Integer.valueOf(0));
        this.dataManager.register(DESTINATIONYDW, Float.valueOf(0));
        this.dataManager.register(DESTINATIONXDW, Float.valueOf(0));
        this.dataManager.register(DESTINATIONZDW, Float.valueOf(0));
        this.dataManager.register(CURRENTFLOORDW, Integer.valueOf(-1));
        this.dataManager.register(CALLEDDW, Boolean.FALSE);
    }

    public boolean getCalled()
    {
        return dataManager.get(CALLEDDW);
    }

    private void setCalled(boolean called)
    {
        dataManager.set(CALLEDDW, called);
    }

    /** @return the destinationFloor */
    public int getCurrentFloor()
    {
        return dataManager.get(CURRENTFLOORDW);
    }

    /** @return the destinationFloor */
    public int getDestinationFloor()
    {
        return dataManager.get(DESTINATIONFLOORDW);
    }

    /** @return the destinationFloor */
    public float getDestX()
    {
        return dataManager.get(DESTINATIONXDW);
    }

    /** @return the destinationFloor */
    public float getDestY()
    {
        return dataManager.get(DESTINATIONYDW);
    }

    /** @return the destinationFloor */
    public float getDestZ()
    {
        return dataManager.get(DESTINATIONZDW);
    }

    @Override
    public boolean isPotionApplicable(PotionEffect par1PotionEffect)
    {
        return false;
    }

    @Override
    protected void onGridAlign()
    {
        BlockPos pos = getPosition();
        setCalled(false);
        setPosition(pos.getX() + 0.5, Math.round(posY), pos.getZ() + 0.5);
        PacketHandler.sendEntityUpdate(this);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        energy = nbt.getInteger("energy");
    }

    /** @param currentFloor
     *            the destinationFloor to set */
    public void setCurrentFloor(int currentFloor)
    {
        dataManager.set(CURRENTFLOORDW, Integer.valueOf(currentFloor));
    }

    /** @param destinationFloor
     *            the destinationFloor to set */
    public void setDestinationFloor(int destinationFloor)
    {
        dataManager.set(DESTINATIONFLOORDW, Integer.valueOf(destinationFloor));
    }

    /** @param dest
     *            the destinationFloor to set */
    public void setDestX(float dest)
    {
        dataManager.set(DESTINATIONXDW, Float.valueOf(dest));
        dataManager.set(DESTINATIONYDW, Float.valueOf((float) posY));
        dataManager.set(DESTINATIONZDW, Float.valueOf((float) posZ));
        setCalled(true);
    }

    /** @param dest
     *            the destinationFloor to set */
    public void setDestY(float dest)
    {
        dataManager.set(DESTINATIONYDW, Float.valueOf(dest));
        dataManager.set(DESTINATIONXDW, Float.valueOf((float) posX));
        dataManager.set(DESTINATIONZDW, Float.valueOf((float) posZ));
        setCalled(true);
    }

    /** @param dest
     *            the destinationFloor to set */
    public void setDestZ(float dest)
    {
        dataManager.set(DESTINATIONZDW, Float.valueOf(dest));
        dataManager.set(DESTINATIONYDW, Float.valueOf((float) posY));
        dataManager.set(DESTINATIONXDW, Float.valueOf((float) posX));
        setCalled(true);
    }

    public void setFoor(TileEntityLiftAccess te, int floor)
    {
        if (te.floor == 0)
        {
            floors[floor - 1] = te.getPos().getY() - 2;
            hasFloors[floor - 1] = true;
        }
        else if (te.floor != 0)
        {
            hasFloors[te.floor - 1] = false;
            floors[floor - 1] = te.getPos().getY() - 2;
            hasFloors[floor - 1] = true;
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("energy", energy);
    }

    @Override
    public void setTiles(TileEntity[][][] tiles)
    {
        super.setTiles(tiles);
        for (TileEntity[][] tileArrArr : tiles)
        {
            for (TileEntity[] tileArr : tileArrArr)
            {
                for (TileEntity tile : tileArr)
                {
                    if (tile instanceof TileEntityLiftAccess)
                    {
                        ((TileEntityLiftAccess) tile).setLift(this);
                    }
                }
            }
        }
    }

    public static EntityLift getLiftFromUUID(final UUID liftID, World world)
    {
        EntityLift ret = null;
        if (world instanceof BlockEntityWorld)
        {
            world = ((BlockEntityWorld) world).getWorld();
        }
        if (world instanceof WorldServer)
        {
            WorldServer worlds = (WorldServer) world;
            return (EntityLift) worlds.getEntityFromUuid(liftID);
        }
        else
        {
            List<EntityLift> entities = world.getEntities(EntityLift.class, new Predicate<EntityLift>()
            {
                @Override
                public boolean apply(EntityLift input)
                {
                    return input.getUniqueID().equals(liftID);
                }
            });
            if (!entities.isEmpty()) return entities.get(0);
        }
        return ret;
    }

    @Override
    protected void preColliderTick()
    {
    }
}
