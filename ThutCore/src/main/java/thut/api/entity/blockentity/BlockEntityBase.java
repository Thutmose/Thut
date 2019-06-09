package thut.api.entity.blockentity;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import thut.core.common.ThutCore;

public abstract class BlockEntityBase extends LivingEntity implements IEntityAdditionalSpawnData, IBlockEntity
{
    public static int          ACCELERATIONTICKS = 20;
    public BlockPos            boundMin          = BlockPos.ZERO;
    public BlockPos            boundMax          = BlockPos.ZERO;
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
    public UUID                owner;
    public List<AxisAlignedBB> blockBoxes        = Lists.newArrayList();
    public BlockState[][][]    blocks            = null;
    public TileEntity[][][]    tiles             = null;
    BlockEntityUpdater         collider;
    BlockEntityInteractHandler interacter;

    public BlockEntityBase(EntityType<? extends LivingEntity> type, World par1World)
    {
        super(type, par1World);
        this.ignoreFrustumCheck = true;
        this.hurtResistantTime = 0;
    }

    @Override
    public BlockEntityWorld getFakeWorld()
    {
        if (fake_world == null)
        {
            fake_world = new BlockEntityWorld(this, world);
        }
        return fake_world;
    }

    public BlockEntityBase(EntityType<? extends LivingEntity> type, World world, double x, double y, double z)
    {
        this(type, world);
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
        double dp = destPos - pos;
        if (dp > 0)
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
            return ds;
        }
        else if (dp < 0)
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
            return ds;
        }
        else return 0;
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
        return this.isAlive();
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
                if (entity instanceof ServerPlayerEntity
                        && entity.getBoundingBox().grow(2).intersects(getBoundingBox()))
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
        AxisAlignedBB axisalignedbb = this.getBoundingBox();
        if ((xDiff % 1) != 0) this.posX = (axisalignedbb.minX + xDiff);
        else this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
        this.posY = axisalignedbb.minY;
        if (zDiff % 1 != 0) this.posZ = (axisalignedbb.minZ + zDiff);
        else this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
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
    public ActionResultType applyPlayerInteraction(PlayerEntity player, Vec3d vec, Hand hand)
    {
        if (interacter == null) interacter = createInteractHandler();
        try
        {
            return interacter.applyPlayerInteraction(player, vec, player.getHeldItem(hand), hand);
        }
        catch (Exception e)
        {
            ThutCore.logger.log(Level.SEVERE, "Error handling interactions for " + this, e);
            return super.applyPlayerInteraction(player, vec, hand);
        }
    }

    /** First layer of player interaction */
    @Override
    public boolean processInitialInteract(PlayerEntity player, Hand hand)
    {
        if (interacter == null) interacter = createInteractHandler();
        return interacter.processInitialInteract(player, player.getHeldItem(hand), hand);
    }

    @Override
    public boolean isPotionApplicable(EffectInstance par1EffectInstance)
    {
        return false;
    }

    abstract protected void preColliderTick();

    abstract protected void onGridAlign();

    @Override
    public void tick()
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
        int dy = (int) ((getMotion().x) * 16);
        int dx = (int) ((getMotion().y) * 16);
        int dz = (int) ((getMotion().z) * 16);
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

    public void readBlocks(CompoundNBT nbt)
    {
        if (nbt.contains("Blocks"))
        {
            CompoundNBT blockTag = nbt.getCompound("Blocks");
            int sizeX = blockTag.getInt("BlocksLengthX");
            int sizeZ = blockTag.getInt("BlocksLengthZ");
            int sizeY = blockTag.getInt("BlocksLengthY");
            if (sizeX == 0 || sizeZ == 0)
            {
                sizeX = sizeZ = nbt.getInt("BlocksLength");
            }
            if (sizeY == 0) sizeY = 1;
            blocks = new BlockState[sizeX][sizeY][sizeZ];
            tiles = new TileEntity[sizeX][sizeY][sizeZ];
            for (int i = 0; i < sizeX; i++)
                for (int k = 0; k < sizeY; k++)
                    for (int j = 0; j < sizeZ; j++)
                    {
                        String name = "B" + i + "," + k + "," + j;
                        if (!blockTag.contains(name)) continue;
                        BlockState state = NBTUtil.readBlockState(blockTag.getCompound(name));
                        blocks[i][k][j] = state;
                        if (blockTag.contains("T" + i + "," + k + "," + j))
                        {
                            try
                            {
                                CompoundNBT tag = blockTag.getCompound("T" + i + "," + k + "," + j);
                                tiles[i][k][j] = TileEntity.create(tag);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
            // Call these in this order so any appropriate changes can be made.
            this.setBlocks(blocks);
            this.setTiles(tiles);
        }
    }

    @Override
    public void readAdditional(CompoundNBT nbt)
    {
        super.readAdditional(nbt);
        if (nbt.contains("bounds"))
        {
            CompoundNBT bounds = nbt.getCompound("bounds");
            boundMin = new BlockPos(bounds.getDouble("minx"), bounds.getDouble("miny"), bounds.getDouble("minz"));
            boundMax = new BlockPos(bounds.getDouble("maxx"), bounds.getDouble("maxy"), bounds.getDouble("maxz"));
        }
        readBlocks(nbt);
    }

    @Override
    public void readSpawnData(PacketBuffer data)
    {
        PacketBuffer buff = new PacketBuffer(data);
        CompoundNBT tag = new CompoundNBT();
        try
        {
            tag = buff.readCompoundTag();
            readAdditional(tag);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** Will get destroyed next tick. */
    @Override
    public void remove()
    {
        if (!getEntityWorld().isRemote && this.isAlive() && shouldRevert)
        {
            IBlockEntity.BlockEntityFormer.RevertEntity(this);
        }
        super.remove();
    }

    @Override
    public void remove(boolean keepData)
    {
        shouldRevert = !keepData;
        super.remove(keepData);
    }

    @Override
    public void setPosition(double x, double y, double z)
    {
        super.setPosition(x, y, z);
        if (collider != null) collider.onSetPosition();
    }

    public void writeBlocks(CompoundNBT nbt)
    {
        if (blocks != null)
        {
            CompoundNBT blocksTag = new CompoundNBT();
            blocksTag.putInt("BlocksLengthX", blocks.length);
            blocksTag.putInt("BlocksLengthY", blocks[0].length);
            blocksTag.putInt("BlocksLengthZ", blocks[0][0].length);
            int sizeX = blocks.length;
            int sizeY = blocks[0].length;
            int sizeZ = blocks[0][0].length;
            for (int i = 0; i < sizeX; i++)
            {
                for (int k = 0; k < sizeY; k++)
                {
                    for (int j = 0; j < sizeZ; j++)
                    {
                        BlockState b = blocks[i][k][j];
                        if (b == null) continue;
                        blocksTag.put("B" + i + "," + k + "," + j, NBTUtil.writeBlockState(b));
                        try
                        {
                            if (tiles[i][k][j] != null)
                            {
                                CompoundNBT tag = new CompoundNBT();
                                tag = tiles[i][k][j].write(tag);
                                blocksTag.put("T" + i + "," + k + "," + j, tag);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
            nbt.put("Blocks", blocksTag);
        }
    }

    @Override
    public void writeAdditional(CompoundNBT nbt)
    {
        super.writeAdditional(nbt);
        CompoundNBT vector = new CompoundNBT();
        vector.putDouble("minx", boundMin.getX());
        vector.putDouble("miny", boundMin.getY());
        vector.putDouble("minz", boundMin.getZ());
        vector.putDouble("maxx", boundMax.getX());
        vector.putDouble("maxy", boundMax.getY());
        vector.putDouble("maxz", boundMax.getZ());
        nbt.put("bounds", vector);
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
    public void writeSpawnData(PacketBuffer data)
    {
        PacketBuffer buff = new PacketBuffer(data);
        CompoundNBT tag = new CompoundNBT();
        writeAdditional(tag);
        buff.writeCompoundTag(tag);
    }

    @OnlyIn(Dist.CLIENT)
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
    public ItemStack getHeldItem(Hand hand)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public void setHeldItem(Hand hand, @Nullable ItemStack stack)
    {

    }

    @Override
    public HandSide getPrimaryHand()
    {
        return HandSide.LEFT;
    }

    @Override
    public void setBlocks(BlockState[][][] blocks)
    {
        this.blocks = blocks;
    }

    @Override
    public BlockState[][][] getBlocks()
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
