package thut.tech.common.entity;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import thut.api.entity.blockentity.BlockEntityUpdater;
import thut.api.entity.blockentity.BlockEntityWorld;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.handlers.ConfigHandler;

public class EntityLift extends EntityLivingBase implements IEntityAdditionalSpawnData, IBlockEntity
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

    public static int                   ACCELERATIONTICKS  = 20;

    public static boolean               ENERGYUSE          = false;
    public static int                   ENERGYCOST         = 100;

    public BlockPos                     boundMin           = BlockPos.ORIGIN;
    public BlockPos                     boundMax           = BlockPos.ORIGIN;

    int                                 energy             = 0;
    BlockEntityWorld                    fakeWorld;
    public double                       speedUp            = ConfigHandler.LiftSpeedUp;
    public double                       speedDown          = -ConfigHandler.LiftSpeedDown;
    public double                       speedHoriz         = 0.5;
    public double                       acceleration       = 0.05;
    public boolean                      toMoveY            = false;
    public boolean                      toMoveX            = false;
    public boolean                      toMoveZ            = false;
    public boolean                      axis               = true;
    public boolean                      hasPassenger       = false;
    int                                 n                  = 0;
    int                                 passengertime      = 10;
    boolean                             first              = true;
    Random                              r                  = new Random();
    public UUID                         id                 = null;
    public UUID                         owner;
    public double                       prevFloorY         = 0;
    public double                       prevFloor          = 0;
    TileEntityLiftAccess                current;
    public List<AxisAlignedBB>          blockBoxes         = Lists.newArrayList();
    public int[]                        floors             = new int[64];
    public IBlockState[][][]            blocks             = null;
    public TileEntity[][][]             tiles              = null;
    BlockEntityUpdater                  collider;
    LiftInteractHandler                 interacter;

    public EntityLift(World par1World)
    {
        super(par1World);
        this.ignoreFrustumCheck = true;
        this.hurtResistantTime = 0;
        this.isImmuneToFire = true;
        for (int i = 0; i < 64; i++)
        {
            floors[i] = -1;
        }
    }

    public BlockEntityWorld getFakeWorld()
    {
        if (fakeWorld == null)
        {
<<<<<<< HEAD
            fakeWorld = new BlockEntityWorld(this, world);
=======
            world = new BlockEntityWorld(this, getEntityWorld());
>>>>>>> refs/remotes/origin/1.11.x
        }
        return fakeWorld;
    }

    public EntityLift(World world, double x, double y, double z)
    {
        this(world);
        this.setPosition(x, y, z);
        r.setSeed(100);
    }

    @Override
    /** Called when the entity is attacked. */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        return false;
    }

    @Override
    /** knocks back this entity */
    public void knockBack(Entity entityIn, float strenght, double xRatio, double zRatio)
    {

    }

    private double getSpeed(double pos, double destPos, double speed, double speedPos, double speedNeg)
    {
        double ds = speed;
        if (destPos > pos)
        {
            boolean tooFast = pos + (ds * (ACCELERATIONTICKS + 1)) > destPos;
            if (!tooFast)
            {
                ds = Math.min(speedPos, ds + acceleration * speedPos);
            }
            else while (ds >= 0 && tooFast)
            {
                ds = ds - acceleration * speedPos / 10;
                tooFast = pos + (ds * (ACCELERATIONTICKS + 1)) > destPos;
            }
        }
        else
        {
            speedNeg = Math.abs(speedNeg);
            boolean tooFast = pos + (ds * (ACCELERATIONTICKS + 1)) < destPos;
            if (!tooFast)
            {
                ds = Math.max(-speedNeg, ds - acceleration * speedNeg);
            }
            else while (ds <= 0 && tooFast)
            {
                ds = ds + acceleration * speedNeg / 10;
                tooFast = pos + (ds * (ACCELERATIONTICKS + 1)) < destPos;
            }
        }
        return ds;
    }

    private void accelerate()
    {
        if (isServerWorld() && !consumePower())
        {
            toMoveY = toMoveX = toMoveZ = false;
        }

        if (!toMoveX) motionX *= 0.5;
        if (!toMoveZ) motionZ *= 0.5;
        if (!toMoveY) motionY *= 0.5;

        if (getCalled())
        {
            if (toMoveY)
            {
                float destY = getDestY();
                double dy = getSpeed(posY, destY, motionY, speedUp, speedDown);
                motionY = dy;
            }
            if (toMoveX)
            {
                float destX = getDestX();
                double dx = getSpeed(posX, destX, motionX, speedHoriz, speedHoriz);
                motionX = dx;
            }
            if (toMoveZ)
            {
                float destZ = getDestZ();
                double dz = getSpeed(posZ, destZ, motionZ, speedHoriz, speedHoriz);
                motionZ = dz;
            }
        }
    }

    /** Applies a velocity to each of the entities pushing them away from each
     * other. Args: entity */
    @Override
    public void applyEntityCollision(Entity entity)
    {
        if (collider == null)
        {
            collider = new BlockEntityUpdater(this);
            collider.onSetPosition();
        }
        try
        {
            collider.applyEntityCollision(entity);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void call(int floor)
    {
        if (floor == 0 || floor > 64) { return; }
        if (floors[floor - 1] > 0)
        {
            callYValue(floors[floor - 1]);
            setDestinationFloor(floor);
        }
    }

    public void callYValue(int yValue)
    {
        setDestY(yValue);
    }

    /** Returns true if other Entities should be prevented from moving through
     * this Entity. */
    @Override
    public boolean canBeCollidedWith()
    {
        return !this.isDead;
    }

    /** Returns true if this entity should push and be pushed by other entities
     * when colliding. */
    @Override
    public boolean canBePushed()
    {
        return true;
    }

    @Override
    public boolean canRenderOnFire()
    {
        return false;
    }

    public void checkCollision()
    {
        int xMin = boundMin.getX();
        int zMin = boundMin.getZ();
        int xMax = boundMax.getX();
        int zMax = boundMax.getZ();

<<<<<<< HEAD
        List<?> list = this.world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(posX + (xMin - 1), posY,
                posZ + (zMin - 1), posX + xMax + 1, posY + 64, posZ + zMax + 1));
=======
        List<?> list = this.getEntityWorld().getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(
                posX + (xMin - 1), posY, posZ + (zMin - 1), posX + xMax + 1, posY + 64, posZ + zMax + 1));
>>>>>>> refs/remotes/origin/1.11.x
        if (list != null && !list.isEmpty())
        {
            if (list.size() == 1 && this.getRecursivePassengers() != null
                    && !this.getRecursivePassengers().isEmpty()) { return; }
            for (int i = 0; i < list.size(); ++i)
            {
                Entity entity = (Entity) list.get(i);
                applyEntityCollision(entity);
            }
        }
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

    public void doMotion()
    {
        if (motionX != 0 || motionZ != 0) System.out.println(motionX + " " + motionY + " " + motionZ);
        if (!toMoveX) motionX = 0;
        if (!toMoveY) motionY = 0;
        if (!toMoveZ) motionZ = 0;
        if (getCalled()) CompatWrapper.moveEntitySelf(this, motionX, motionY, motionZ);
    }

    @Override
    public void resetPositionToBB()
    {
        BlockPos min = getMin();
        BlockPos max = getMax();
        float xDiff = (max.getX() - min.getX()) / 2f;
        float zDiff = (max.getZ() - min.getZ()) / 2f;
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        if ((xDiff % 1) != 0) this.posX = (axisalignedbb.minX + xDiff);
        else this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
        this.posY = axisalignedbb.minY;
        if (zDiff % 1 != 0) this.posZ = (axisalignedbb.minZ + zDiff);
        else this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
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

    /** returns the bounding box for this entity */
    public AxisAlignedBB getBoundingBox()
    {
        return null;
    }

    /** Checks if the entity's current position is a valid location to spawn
     * this entity. */
    public boolean getCanSpawnHere()
    {
        return false;
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

    // 1.11
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand)
    {
        return applyPlayerInteraction(player, vec, player.getHeldItem(hand), hand);
    }

    // 1.10
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, @Nullable ItemStack stack,
            EnumHand hand)
    {
        if (interacter == null) interacter = new LiftInteractHandler(this);
        return interacter.applyPlayerInteraction(player, vec, stack, hand);
    }

    // 1.11
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand)
    {
        return processInitialInteract(player, player.getHeldItem(hand), hand);
    }

    // 1.10
    public boolean processInitialInteract(EntityPlayer player, @Nullable ItemStack stack, EnumHand hand)
    {
        return interacter.processInitialInteract(player, stack, hand);
    }

    @Override
    public boolean isPotionApplicable(PotionEffect par1PotionEffect)
    {
        return false;
    }

    @Override
    public void onUpdate()
    {
        if (net.minecraftforge.common.ForgeHooks.onLivingUpdate(this)) return;
        if (collider == null)
        {
            this.collider = new BlockEntityUpdater(this);
            this.collider.onSetPosition();
        }
        this.prevPosY = this.posY;
        this.prevPosX = this.posX;
        this.prevPosZ = this.posZ;
        collider.onUpdate();
        int dy = (int) ((getDestY() - posY) * 16);
        int dx = (int) ((getDestX() - posX) * 16);
        int dz = (int) ((getDestZ() - posZ) * 16);
        toMoveZ = 0 != dz;
        toMoveY = 0 != dy;
        toMoveX = 0 != dx;
        accelerate();
        if (toMoveY || toMoveX || toMoveZ)
        {
            doMotion();
        }
        else// if (!world.isRemote)
        {
            setCalled(false);
            BlockPos pos = getPosition();
            setPosition(pos.getX() + 0.5, Math.round(posY), pos.getZ() + 0.5);
        }
        this.rotationYaw = 0;
        checkCollision();
        passengertime = hasPassenger ? 20 : passengertime - 1;
        n++;
    }

    public void passengerCheck()
    {
<<<<<<< HEAD
        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox());
=======
        List<Entity> list = getEntityWorld().getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox());
>>>>>>> refs/remotes/origin/1.11.x
        if (list.size() > 0)
        {
            hasPassenger = true;
        }
        else
        {
            hasPassenger = false;
        }
    }

    public void readBlocks(NBTTagCompound nbt)
    {
        if (nbt.hasKey("Blocks"))
        {
            NBTTagCompound blockTag = nbt.getCompoundTag("Blocks");
            int sizeX = blockTag.getInteger("BlocksLengthX");
            int sizeZ = blockTag.getInteger("BlocksLengthZ");
            int sizeY = blockTag.getInteger("BlocksLengthY");
            if (sizeX == 0 || sizeZ == 0)
            {
                sizeX = sizeZ = nbt.getInteger("BlocksLength");
            }
            if (sizeY == 0) sizeY = 1;
            int version = blockTag.getInteger("v");
            blocks = new IBlockState[sizeX][sizeY][sizeZ];
            tiles = new TileEntity[sizeX][sizeY][sizeZ];
            for (int i = 0; i < sizeX; i++)
                for (int k = 0; k < sizeY; k++)
                    for (int j = 0; j < sizeZ; j++)
                    {
                        int n = -1;
                        if (blockTag.hasKey("I" + i + "," + j))
                        {
                            n = blockTag.getInteger("I" + i + "," + j);
                        }
                        else if (blockTag.hasKey("I" + i + "," + k + "," + j))
                        {
                            n = blockTag.getInteger("I" + i + "," + k + "," + j);
                        }
                        if (n == -1) continue;
                        IBlockState state;
                        if (version == 0)
                        {
                            Block b = Block.getBlockFromItem(Item.getItemById(n));
                            int meta = blockTag.getInteger("M" + i + "," + k + "," + j);
                            state = CompatWrapper.getBlockStateFromMeta(b, meta);
                        }
                        else
                        {
                            Block b = Block.getBlockById(n);
                            int meta = blockTag.getInteger("M" + i + "," + k + "," + j);
                            state = CompatWrapper.getBlockStateFromMeta(b, meta);
                        }
                        blocks[i][k][j] = state;
                        if (blockTag.hasKey("T" + i + "," + k + "," + j))
                        {
                            try
                            {
                                NBTTagCompound tag = blockTag.getCompoundTag("T" + i + "," + k + "," + j);
                                tiles[i][k][j] = IBlockEntity.BlockEntityFormer.makeTile(tag);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        axis = nbt.getBoolean("axis");
        energy = nbt.getInteger("energy");
        if (nbt.hasKey("bounds"))
        {
            NBTTagCompound bounds = nbt.getCompoundTag("bounds");
            boundMin = new BlockPos(bounds.getDouble("minx"), bounds.getDouble("miny"), bounds.getDouble("minz"));
            boundMax = new BlockPos(bounds.getDouble("maxx"), bounds.getDouble("maxy"), bounds.getDouble("maxz"));
        }
        if (nbt.hasKey("higher")) id = new UUID(nbt.getLong("higher"), nbt.getLong("lower"));
        if (nbt.hasKey("ownerhigher")) owner = new UUID(nbt.getLong("ownerhigher"), nbt.getLong("ownerlower"));
        readList(nbt);
        readBlocks(nbt);
    }

    public void readList(NBTTagCompound nbt)
    {
        if (nbt.hasKey("floors 0")) for (int i = 0; i < 64; i++)
        {
            floors[i] = nbt.getInteger("floors " + i);
            if (floors[i] == 0) floors[i] = -1;
        }
        else
        {
            NBTTagCompound floorTag = nbt.getCompoundTag("floors");
            for (int i = 0; i < 64; i++)
            {
                floors[i] = floorTag.getInteger("" + i);
                if (floors[i] == 0) floors[i] = -1;
            }
        }
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        PacketBuffer buff = new PacketBuffer(data);
        NBTTagCompound tag = new NBTTagCompound();
        try
        {
            tag = buff.readCompoundTag();
            readEntityFromNBT(tag);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** @param currentFloor
     *            the destinationFloor to set */
    public void setCurrentFloor(int currentFloor)
    {
        dataManager.set(CURRENTFLOORDW, Integer.valueOf(currentFloor));
    }

    /** Will get destroyed next tick. */
    @Override
    public void setDead()
    {
<<<<<<< HEAD
        if (!world.isRemote && !this.isDead)
=======
        if (!getEntityWorld().isRemote && !this.isDead && this.addedToChunk)
>>>>>>> refs/remotes/origin/1.11.x
        {
            IBlockEntity.BlockEntityFormer.RevertEntity(this);
        }
        super.setDead();
    }

    @Override
    public void setPosition(double x, double y, double z)
    {
        super.setPosition(x, y, z);
        if (collider != null) collider.onSetPosition();
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
        }
        else if (te.floor != 0)
        {
            floors[te.floor - 1] = -1;
            floors[floor - 1] = te.getPos().getY() - 2;
        }
    }

    public void writeBlocks(NBTTagCompound nbt)
    {
        if (blocks != null)
        {
            NBTTagCompound blocksTag = new NBTTagCompound();
            blocksTag.setInteger("BlocksLengthX", blocks.length);
            blocksTag.setInteger("BlocksLengthY", blocks[0].length);
            blocksTag.setInteger("BlocksLengthZ", blocks[0][0].length);
            blocksTag.setInteger("v", 1);
            int sizeX = blocks.length;
            int sizeY = blocks[0].length;
            int sizeZ = blocks[0][0].length;
            for (int i = 0; i < sizeX; i++)
            {
                for (int k = 0; k < sizeY; k++)
                {
                    for (int j = 0; j < sizeZ; j++)
                    {
                        IBlockState b = blocks[i][k][j];
                        if (b == null) continue;
                        blocksTag.setInteger("I" + i + "," + k + "," + j, Block.getIdFromBlock(b.getBlock()));
                        blocksTag.setInteger("M" + i + "," + k + "," + j, b.getBlock().getMetaFromState(b));
                        try
                        {
                            if (tiles[i][k][j] != null)
                            {

                                NBTTagCompound tag = new NBTTagCompound();
                                tag = tiles[i][k][j].writeToNBT(tag);
                                blocksTag.setTag("T" + i + "," + k + "," + j, tag);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
            nbt.setTag("Blocks", blocksTag);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setBoolean("axis", axis);

        NBTTagCompound vector = new NBTTagCompound();
        vector.setDouble("minx", boundMin.getX());
        vector.setDouble("miny", boundMin.getY());
        vector.setDouble("minz", boundMin.getZ());
        vector.setDouble("maxx", boundMax.getX());
        vector.setDouble("maxy", boundMax.getY());
        vector.setDouble("maxz", boundMax.getZ());
        nbt.setTag("bounds", vector);
        nbt.setInteger("energy", energy);
        if (owner != null)
        {
            nbt.setLong("ownerlower", owner.getLeastSignificantBits());
            nbt.setLong("ownerhigher", owner.getMostSignificantBits());
        }
        writeList(nbt);
        try
        {
            writeBlocks(nbt);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void writeList(NBTTagCompound nbt)
    {
        NBTTagCompound floorTag = new NBTTagCompound();
        for (int i = 0; i < 64; i++)
        {
            floorTag.setInteger("" + i, floors[i]);
        }
        nbt.setTag("floors", floorTag);
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        PacketBuffer buff = new PacketBuffer(data);
        NBTTagCompound tag = new NBTTagCompound();
        writeEntityToNBT(tag);
        buff.writeCompoundTag(tag);
    }

    @Override
    public Iterable<ItemStack> getArmorInventoryList()
    {
        return Lists.newArrayList();
    }

    @Override
    public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn)
    {
        return CompatWrapper.nullStack;
    }

    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack)
    {
    }

    @Override
    public ItemStack getHeldItem(EnumHand hand)
    {
        return CompatWrapper.nullStack;
    }

    @Override
    public void setHeldItem(EnumHand hand, @Nullable ItemStack stack)
    {

    }

    @Override
    public EnumHandSide getPrimaryHand()
    {
        return EnumHandSide.LEFT;
    }

    @Override
    public void setBlocks(IBlockState[][][] blocks)
    {
        this.blocks = blocks;
    }

    @Override
    public IBlockState[][][] getBlocks()
    {
        return blocks;
    }

    @Override
    public void setTiles(TileEntity[][][] tiles)
    {
        this.tiles = tiles;
    }

    @Override
    public TileEntity[][][] getTiles()
    {
        return tiles;
    }

    @Override
    public BlockPos getMin()
    {
        return boundMin;
    }

    @Override
    public BlockPos getMax()
    {
        return boundMax;
    }

    @Override
    public void setMin(BlockPos pos)
    {
        this.boundMin = pos;
    }

    @Override
    public void setMax(BlockPos pos)
    {
        this.boundMax = pos;
    }

    @Override
    public void setFakeWorld(BlockEntityWorld world)
    {
        this.fakeWorld = world;
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

}
