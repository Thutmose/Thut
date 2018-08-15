package thut.api.entity.blockentity;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
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
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockEntityBase extends EntityLivingBase implements IEntityAdditionalSpawnData, IBlockEntity
{
    public static int          ACCELERATIONTICKS = 20;
    public BlockPos            boundMin          = BlockPos.ORIGIN;
    public BlockPos            boundMax          = BlockPos.ORIGIN;
    private BlockEntityWorld   fake_world;
    private boolean            shouldRevert      = true;
    public double              speedUp           = 0.5;
    public double              speedDown         = -0.5;
    public double              speedHoriz        = 0.5;
    public double              acceleration      = 0.05;
    public boolean             toMoveY           = false;
    public boolean             toMoveX           = false;
    public boolean             toMoveZ           = false;
    public boolean             hasPassenger      = false;
    int                        n                 = 0;
    boolean                    first             = true;
    Random                     r                 = new Random();
    public UUID                id                = null;
    public UUID                owner;
    public List<AxisAlignedBB> blockBoxes        = Lists.newArrayList();
    public IBlockState[][][]   blocks            = null;
    public TileEntity[][][]    tiles             = null;
    BlockEntityUpdater         collider;
    BlockEntityInteractHandler interacter;

    public BlockEntityBase(World par1World)
    {
        super(par1World);
        this.ignoreFrustumCheck = true;
        this.hurtResistantTime = 0;
        this.isImmuneToFire = true;
    }

    public BlockEntityWorld getFakeWorld()
    {
        if (fake_world == null)
        {
            fake_world = new BlockEntityWorld(this, world);
        }
        return fake_world;
    }

    public BlockEntityBase(World world, double x, double y, double z)
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

    protected double getSpeed(double pos, double destPos, double speed, double speedPos, double speedNeg)
    {
        if (!getEntityWorld().isAreaLoaded(getPosition(), 8)) { return 0; }
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

    abstract protected void accelerate();

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

        List<?> list = this.world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(posX + (xMin - 1), posY,
                posZ + (zMin - 1), posX + xMax + 1, posY + 64, posZ + zMax + 1));
        if (list != null && !list.isEmpty())
        {
            if (list.size() == 1 && this.getRecursivePassengers() != null
                    && !this.getRecursivePassengers().isEmpty()) { return; }
            for (int i = 0; i < list.size(); ++i)
            {
                Entity entity = (Entity) list.get(i);
                applyEntityCollision(entity);
                if (entity instanceof EntityPlayerMP
                        && entity.getEntityBoundingBox().grow(2).intersects(getEntityBoundingBox()))
                {
                    hasPassenger = true;
                }
            }
        }
    }

    abstract protected boolean checkAccelerationConditions();

    abstract protected void doMotion();

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

    abstract protected BlockEntityInteractHandler createInteractHandler();

    @Override
    /** Applies the given player interaction to this Entity. */
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand)
    {
        if (interacter == null) interacter = createInteractHandler();
        try
        {
            return interacter.applyPlayerInteraction(player, vec, player.getHeldItem(hand), hand);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return super.applyPlayerInteraction(player, vec, hand);
        }
    }

    /** First layer of player interaction */
    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand)
    {
        if (interacter == null) interacter = createInteractHandler();
        return interacter.processInitialInteract(player, player.getHeldItem(hand), hand);
    }

    @Override
    public boolean isPotionApplicable(PotionEffect par1PotionEffect)
    {
        return false;
    }

    abstract protected void preColliderTick();

    abstract protected void onGridAlign();

    @Override
    public void onUpdate()
    {
        if (net.minecraftforge.common.ForgeHooks.onLivingUpdate(this)) return;
        if (collider == null)
        {
            this.collider = new BlockEntityUpdater(this);
            this.collider.onSetPosition();
        }
        preColliderTick();
        this.prevPosY = this.posY;
        this.prevPosX = this.posX;
        this.prevPosZ = this.posZ;
        collider.onUpdate();
        accelerate();
        int dy = (int) ((motionY) * 16);
        int dx = (int) ((motionX) * 16);
        int dz = (int) ((motionZ) * 16);
        if (toMoveY || toMoveX || toMoveZ)
        {
            doMotion();
        }
        else if (dx == dy && dy == dz && dz == 0 && !world.isRemote)
        {
            BlockPos pos = getPosition();
            boolean update = posX != pos.getX() + 0.5 || posY != Math.round(posY) || posZ != pos.getZ() + 0.5;
            if (update)
            {
                onGridAlign();
            }
        }
        checkCollision();
    }

    @SuppressWarnings("deprecation")
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
                            state = b.getStateFromMeta(meta);
                        }
                        else
                        {
                            Block b = Block.getBlockById(n);
                            int meta = blockTag.getInteger("M" + i + "," + k + "," + j);
                            state = b.getStateFromMeta(meta);
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
        if (nbt.hasKey("bounds"))
        {
            NBTTagCompound bounds = nbt.getCompoundTag("bounds");
            boundMin = new BlockPos(bounds.getDouble("minx"), bounds.getDouble("miny"), bounds.getDouble("minz"));
            boundMax = new BlockPos(bounds.getDouble("maxx"), bounds.getDouble("maxy"), bounds.getDouble("maxz"));
        }
        if (nbt.hasKey("higher")) id = new UUID(nbt.getLong("higher"), nbt.getLong("lower"));
        if (nbt.hasKey("ownerhigher")) owner = new UUID(nbt.getLong("ownerhigher"), nbt.getLong("ownerlower"));
        readBlocks(nbt);
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

    /** Will get destroyed next tick. */
    @Override
    public void setDead()
    {
        if (!getEntityWorld().isRemote && !this.isDead && this.addedToChunk && shouldRevert)
        {
            IBlockEntity.BlockEntityFormer.RevertEntity(this);
        }
        super.setDead();
    }

    @Override
    public void setDropItemsWhenDead(boolean dropWhenDead)
    {
        shouldRevert = dropWhenDead;
    }

    @Override
    public void setPosition(double x, double y, double z)
    {
        super.setPosition(x, y, z);
        if (collider != null) collider.onSetPosition();
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
        NBTTagCompound vector = new NBTTagCompound();
        vector.setDouble("minx", boundMin.getX());
        vector.setDouble("miny", boundMin.getY());
        vector.setDouble("minz", boundMin.getZ());
        vector.setDouble("maxx", boundMax.getX());
        vector.setDouble("maxy", boundMax.getY());
        vector.setDouble("maxz", boundMax.getZ());
        nbt.setTag("bounds", vector);
        if (owner != null)
        {
            nbt.setLong("ownerlower", owner.getLeastSignificantBits());
            nbt.setLong("ownerhigher", owner.getMostSignificantBits());
        }
        try
        {
            writeBlocks(nbt);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        PacketBuffer buff = new PacketBuffer(data);
        NBTTagCompound tag = new NBTTagCompound();
        writeEntityToNBT(tag);
        buff.writeCompoundTag(tag);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isInRangeToRenderDist(double distance)
    {
        return true;
    }

    /** This is here to prevent teleport packet processing in vanilla
     * updates. */
    @Override
    public boolean canPassengerSteer()
    {
        return true;
    }

    @Override
    public Iterable<ItemStack> getArmorInventoryList()
    {
        return Lists.newArrayList();
    }

    @Override
    public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack)
    {
    }

    @Override
    public ItemStack getHeldItem(EnumHand hand)
    {
        return ItemStack.EMPTY;
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
        this.fake_world = world;
    }
}
