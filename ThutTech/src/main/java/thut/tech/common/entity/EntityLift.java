package thut.tech.common.entity;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import thut.api.entity.blockentity.BlockEntityUpdater;
import thut.api.entity.blockentity.BlockEntityWorld;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.maths.Vector3;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.handlers.ConfigHandler;
import thut.tech.common.items.ItemLinker;

public class EntityLift extends EntityLivingBase implements IEntityAdditionalSpawnData, IBlockEntity
{
    static final DataParameter<Integer>             DESTINATIONFLOORDW = EntityDataManager
            .<Integer> createKey(EntityLift.class, DataSerializers.VARINT);
    static final DataParameter<Integer>             DESTINATIONYDW     = EntityDataManager
            .<Integer> createKey(EntityLift.class, DataSerializers.VARINT);
    static final DataParameter<Integer>             CURRENTFLOORDW     = EntityDataManager
            .<Integer> createKey(EntityLift.class, DataSerializers.VARINT);
    static final DataParameter<Optional<ItemStack>> CAMOBLOCKDW        = EntityDataManager
            .<Optional<ItemStack>> createKey(EntityLift.class, DataSerializers.OPTIONAL_ITEM_STACK);

    public static int                               ACCELERATIONTICKS  = 20;

    public static boolean                           ENERGYUSE          = false;
    public static int                               ENERGYCOST         = 100;

    public BlockPos                                 boundMin           = BlockPos.ORIGIN;
    public BlockPos                                 boundMax           = BlockPos.ORIGIN;

    int                                             energy             = 0;
    private BlockEntityWorld                        world;
    public double                                   speedUp            = ConfigHandler.LiftSpeedUp;
    public double                                   speedDown          = -ConfigHandler.LiftSpeedDown;
    public double                                   acceleration       = 0.05;
    public boolean                                  up                 = true;
    public boolean                                  toMoveY            = false;
    public boolean                                  moved              = false;
    public boolean                                  axis               = true;
    public boolean                                  hasPassenger       = false;
    int                                             n                  = 0;
    int                                             passengertime      = 10;
    boolean                                         first              = true;
    Random                                          r                  = new Random();
    public UUID                                     id                 = null;
    public UUID                                     owner;
    public double                                   prevFloorY         = 0;
    public double                                   prevFloor          = 0;
    public boolean                                  called             = false;
    TileEntityLiftAccess                            current;
    public List<AxisAlignedBB>                      blockBoxes         = Lists.newArrayList();
    public int[]                                    floors             = new int[64];
    public ItemStack[][][]                          blocks             = null;
    public TileEntity[][][]                         tiles              = null;
    BlockEntityUpdater                              collider;

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
        this.collider = new BlockEntityUpdater(this);
    }

    public BlockEntityWorld getFakeWorld()
    {
        if (world == null)
        {
            world = new BlockEntityWorld(this, worldObj);
        }
        return world;
    }

    public EntityLift(World world, double x, double y, double z)
    {
        this(world);
        this.setPosition(x, y, z);
        r.setSeed(100);
    }

    public void accelerate()
    {
        if (isServerWorld() && !consumePower())
        {
            toMoveY = false;
        }

        motionX = 0;
        motionZ = 0;
        if (!toMoveY) motionY *= 0.5;
        else
        {
            if (up) motionY = Math.min(speedUp, motionY + acceleration * speedUp);
            else motionY = Math.max(speedDown, motionY + acceleration * speedDown);
        }
    }

    /** Applies a velocity to each of the entities pushing them away from each
     * other. Args: entity */
    @Override
    public void applyEntityCollision(Entity entity)
    {
        collider.applyEntityCollision(entity);
    }

    /** Called when the entity is attacked. */
    public boolean attackEntityFrom(DamageSource source, int damage)
    {
        return false;
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

        List<?> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(posX + (xMin - 1),
                posY, posZ + (zMin - 1), posX + xMax + 1, posY + 64, posZ + zMax + 1));

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

    public boolean checkBlocks(double dir)
    {
        boolean ret = true;
        Vector3 thisloc = Vector3.getNewVector().set(this).addTo(0, dir, 0);

        if (called)
        {
            if (dir > 0 && thisloc.y > getDestY()) { return false; }
            if (dir < 0 && thisloc.y < getDestY()) { return false; }
        }

        int xMin = boundMin.getX();
        int zMin = boundMin.getZ();
        int xMax = boundMax.getX();
        int zMax = boundMax.getZ();

        Vector3 v = Vector3.getNewVector();
        for (int i = xMin; i <= xMax; i++)
            for (int j = zMin; j <= zMax; j++)
            {
                ret = ret && (v.set(thisloc).addTo(i, 0, j)).clearOfBlocks(worldObj);
            }
        return ret;
    }

    private boolean consumePower()
    {
        if (!ENERGYUSE || !toMoveY) return true;
        boolean power = false;
        Vector3 bounds = Vector3.getNewVector().set(boundMax.subtract(boundMin));
        double volume = bounds.x * bounds.y * bounds.z;
        double energyCost = Math.abs(getDestY() - posY) * ENERGYCOST * volume * 0.01;
        energyCost = Math.max(energyCost, 1);
        if (energyCost <= 0) return true;
        power = (energy = (int) (energy - energyCost)) > 0;
        if (energy < 0) energy = 0;
        MinecraftForge.EVENT_BUS.post(new EventLiftConsumePower(this, (long) energyCost));
        if (!power)
        {
            this.setDestinationFloor(-1);
            this.setDestY(0);
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
        if (up)
        {
            if (checkBlocks(motionY * (ACCELERATIONTICKS + 1)))
            {
                setPosition(posX, posY + motionY, posZ);
                moved = true;
                return;
            }
            else
            {
                while (motionY >= 0 && !checkBlocks((motionY - acceleration * speedUp / 10) * (ACCELERATIONTICKS + 1)))
                {
                    motionY = motionY - acceleration * speedUp / 10;
                }

                if (checkBlocks(motionY))
                {
                    setPosition(posX, posY + motionY, posZ);
                    moved = true;
                    return;
                }
                else
                {
                    setPosition(posX, Math.abs(posY - getDestY()) < 0.5 ? getDestY() : Math.floor(posY), posZ);
                    called = false;
                    prevFloor = getDestinationFloor();
                    prevFloorY = getDestY();
                    setDestY(-1);
                    setDestinationFloor(0);
                    current = null;
                    motionY = 0;
                    toMoveY = false;
                    moved = false;
                }
            }
        }
        else
        {
            if (checkBlocks(motionY * (ACCELERATIONTICKS + 1)))
            {
                setPosition(posX, posY + motionY, posZ);
                moved = true;
                return;
            }
            else
            {
                while (motionY <= 0
                        && !checkBlocks((motionY - acceleration * speedDown / 10) * (ACCELERATIONTICKS + 1)))
                {
                    motionY = motionY - acceleration * speedDown / 10;
                }

                if (checkBlocks(motionY))
                {
                    setPosition(posX, posY + motionY, posZ);
                    moved = true;
                    return;
                }
                else
                {
                    setPosition(posX, Math.abs(posY - getDestY()) < 0.5 ? getDestY() : Math.floor(posY), posZ);
                    called = false;
                    prevFloor = getDestinationFloor();
                    prevFloorY = getDestY();
                    setDestY(-1);
                    setDestinationFloor(0);
                    current = null;
                    motionY = 0;
                    toMoveY = false;
                    moved = false;
                }
            }
        }
        toMoveY = false;
        moved = false;
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataManager.register(DESTINATIONFLOORDW, Integer.valueOf(0));
        this.dataManager.register(DESTINATIONYDW, Integer.valueOf(0));
        this.dataManager.register(CURRENTFLOORDW, Integer.valueOf(-1));
        this.dataManager.register(CAMOBLOCKDW, Optional.<ItemStack> absent());
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
    public int getDestY()
    {
        return dataManager.get(DESTINATIONYDW);
    }

    @Override
    /** Applies the given player interaction to this Entity. */
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, @Nullable ItemStack stack,
            EnumHand hand)
    {
        if (player.isSneaking()) return EnumActionResult.PASS;

        if (hand == EnumHand.MAIN_HAND)
        {
            vec = vec.addVector(vec.xCoord > 0 ? -0.01 : 0.01, vec.yCoord > 0 ? -0.01 : 0.01,
                    vec.zCoord > 0 ? -0.01 : 0.01);
            Vec3d playerPos = player.getPositionVector().addVector(0, player.getEyeHeight(), 0);
            BlockPos pos = IBlockEntity.BlockEntityFormer.rayTraceInternal(playerPos.subtract(getPositionVector()), vec,
                    this);
            IBlockState state = getFakeWorld().getBlockState(pos);
            // Ray trace to a litte more than vec, and look for solid blocks.
            // TODO hit x, y, and z.
            float hitX, hitY, hitZ;
            hitX = hitY = hitZ = 0;
            boolean activate = state.getBlock().onBlockActivated(getFakeWorld(), pos, state, player, hand, stack,
                    EnumFacing.DOWN, hitX, hitY, hitZ);
            if (activate) return EnumActionResult.SUCCESS;
            else if (!state.getMaterial().isSolid())
            {
                Vec3d playerLook = playerPos.add(player.getLookVec().scale(4));
                RayTraceResult result = worldObj.rayTraceBlocks(playerPos, playerLook, false, true, false);
                if (result != null && result.typeOfHit == Type.BLOCK)
                {
                    pos = result.getBlockPos();
                    state = worldObj.getBlockState(pos);
                    hitX = (float) (result.hitVec.xCoord - pos.getX());
                    hitY = (float) (result.hitVec.yCoord - pos.getY());
                    hitZ = (float) (result.hitVec.zCoord - pos.getZ());
                    activate = state.getBlock().onBlockActivated(getEntityWorld(), pos, state, player, hand, stack,
                            result.sideHit, hitX, hitY, hitZ);
                }
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    /** First layer of player interaction */
    @Override
    public boolean processInitialInteract(EntityPlayer player, @Nullable ItemStack stack, EnumHand hand)
    {
        ItemStack item = player.getHeldItem(hand);
        System.out.println("Test " + hand);
        if (hand != EnumHand.MAIN_HAND) return false;

        if (player.isSneaking() && item != null && item.getItem() instanceof ItemLinker
                && ((owner != null && player.getUniqueID().equals(owner)) || player.capabilities.isCreativeMode))
        {
            if (item.getTagCompound() == null)
            {
                item.setTagCompound(new NBTTagCompound());
            }
            item.getTagCompound().setString("lift", getCachedUniqueIdString());

            String message = "msg.liftSet.name";

            if (worldObj.isRemote) player.addChatMessage(new TextComponentTranslation(message));
            return true;
        }
        else if (item != null && item.getItem() instanceof ItemLinker
                && ((owner != null && player.getUniqueID().equals(owner)) || player.capabilities.isCreativeMode))
        {
            if (!worldObj.isRemote && owner != null)
            {
                Entity ownerentity = worldObj.getPlayerEntityByUUID(owner);
                String message = "msg.lift.owner";

                player.addChatMessage(new TextComponentTranslation(message, ownerentity.getName()));
            }
            return true;
        }
        if ((player.isSneaking() && item != null
                && (player.getHeldItem(hand).getItem().getUnlocalizedName().toLowerCase().contains("wrench")
                        || player.getHeldItem(hand).getItem().getUnlocalizedName().toLowerCase().contains("screwdriver")
                        || player.getHeldItem(hand).getItem().getUnlocalizedName()
                                .equals(Items.STICK.getUnlocalizedName())))
                && ((owner != null && player.getUniqueID().equals(owner)) || player.capabilities.isCreativeMode))
        {
            if (!worldObj.isRemote)
            {
                String message = "msg.lift.killed";
                player.addChatMessage(new TextComponentTranslation(message));
                this.setHealth(0);
                setDead();
            }
            return true;
        }
        else if (player.isSneaking() && item != null && Block.getBlockFromItem(item.getItem()) != null
                && (owner == null || owner.equals(player.getUniqueID())))
        {
            System.out.println("Test");
            Block block = Block.getBlockFromItem(item.getItem());
            if (!(block instanceof ITileEntityProvider))
            {
                ItemStack item2 = item.splitStack(1);
                if (getHeldItem(null) != null && !worldObj.isRemote)
                {
                    this.entityDropItem(getHeldItem(null), 1);
                }
                if (!worldObj.isRemote)
                {
                    String message = "msg.lift.camo";
                    player.addChatMessage(new TextComponentTranslation(message, item.getDisplayName()));
                    setHeldItem(null, item2);
                }
            }
            return true;
        }
        else if (player.isSneaking() && item == null && (owner == null || owner.equals(player.getUniqueID())))
        {
            if (getHeldItem(null) != null && !worldObj.isRemote)
            {
                this.entityDropItem(getHeldItem(null), 1);
            }
            if (!worldObj.isRemote) setHeldItem(null, null);
            return true;
        }
        return false;
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
        this.prevPosY = posY;
        collider.onUpdate();
        if (!checkBlocks(0)) toMoveY = false;
        toMoveY = called = getDestY() > 0;
        up = getDestY() > posY;
        accelerate();
        if (toMoveY)
        {
            doMotion();
        }
        else if (!worldObj.isRemote)
        {
            setPosition(posX, Math.round(posY), posZ);
        }
        checkCollision();
        passengertime = hasPassenger ? 20 : passengertime - 1;
        n++;
    }

    public void passengerCheck()
    {
        List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox());
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

            blocks = new ItemStack[sizeX][sizeY][sizeZ];
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
                        ItemStack b = new ItemStack(Item.getItemById(n), 1,
                                blockTag.getInteger("M" + i + "," + k + "," + j));
                        blocks[i][k][j] = b;
                        if (blockTag.hasKey("T" + i + "," + k + "," + j))
                        {
                            try
                            {
                                NBTTagCompound tag = blockTag.getCompoundTag("T" + i + "," + k + "," + j);
                                tiles[i][k][j] = TileEntity.create(tag);
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

        if (nbt.hasKey("replacement"))
        {
            NBTTagCompound held = nbt.getCompoundTag("replacement");
            setHeldItem(null, ItemStack.loadItemStackFromNBT(held));
        }
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
            tag = buff.readNBTTagCompoundFromBuffer();
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
        if (!worldObj.isRemote && !this.isDead)
        {
            IBlockEntity.BlockEntityFormer.RevertEntity(this);
        }
        super.setDead();
    }

    /** @param destinationFloor
     *            the destinationFloor to set */
    public void setDestinationFloor(int destinationFloor)
    {
        dataManager.set(DESTINATIONFLOORDW, Integer.valueOf(destinationFloor));
    }

    /** @param dest
     *            the destinationFloor to set */
    public void setDestY(int dest)
    {
        dataManager.set(DESTINATIONYDW, Integer.valueOf(dest));
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
            int sizeX = blocks.length;
            int sizeY = blocks[0].length;
            int sizeZ = blocks[0][0].length;
            for (int i = 0; i < sizeX; i++)
            {
                for (int k = 0; k < sizeY; k++)
                {
                    for (int j = 0; j < sizeZ; j++)
                    {
                        ItemStack b = blocks[i][k][j];
                        if (b == null || b.getItem() == null) continue;
                        blocksTag.setInteger("I" + i + "," + k + "," + j, Item.getIdFromItem(b.getItem()));
                        blocksTag.setInteger("M" + i + "," + k + "," + j, b.getItemDamage());
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
        if (getHeldItem(null) != null)
        {
            NBTTagCompound held = new NBTTagCompound();
            getHeldItem(null).writeToNBT(held);
            nbt.setTag("replacement", held);
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
        buff.writeNBTTagCompoundToBuffer(tag);
    }

    @Override
    public Iterable<ItemStack> getArmorInventoryList()
    {
        return Lists.newArrayList();
    }

    @Override
    public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn)
    {
        return null;
    }

    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack)
    {
    }

    @Override
    public ItemStack getHeldItem(EnumHand hand)
    {
        return dataManager.get(CAMOBLOCKDW).orNull();
    }

    @Override
    public void setHeldItem(EnumHand hand, @Nullable ItemStack stack)
    {
        if (stack != null)
        {
            dataManager.set(CAMOBLOCKDW, Optional.of(stack));
        }
        else
        {
            dataManager.set(CAMOBLOCKDW, Optional.<ItemStack> absent());
        }
    }

    @Override
    public EnumHandSide getPrimaryHand()
    {
        return EnumHandSide.LEFT;
    }

    @Override
    public void setBlocks(ItemStack[][][] blocks)
    {
        this.blocks = blocks;
    }

    @Override
    public ItemStack[][][] getBlocks()
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
        this.world = world;
    }

    public static EntityLift getLiftFromUUID(final UUID liftID, World world)
    {
        EntityLift ret = null;
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
