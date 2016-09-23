package thut.rocket;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.BlockStairs;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import thut.api.entity.IMultiplePassengerEntity;
import thut.api.entity.blockentity.BlockEntityUpdater;
import thut.api.entity.blockentity.BlockEntityWorld;
import thut.api.entity.blockentity.IBlockEntity;

public class EntityRocket extends EntityLivingBase
        implements IEntityAdditionalSpawnData, IBlockEntity, IMultiplePassengerEntity
{
    static final DataParameter<BlockPos> SEAT                 = EntityDataManager
            .<BlockPos> createKey(EntityRocket.class, DataSerializers.BLOCK_POS);

    private BlockPos                     boundMin             = BlockPos.ORIGIN;
    private BlockPos                     boundMax             = BlockPos.ORIGIN;
    private BlockEntityWorld             world;
    public UUID                          owner;
    private ItemStack[][][]              blocks               = null;
    private TileEntity[][][]             tiles                = null;
    final BlockEntityUpdater             collider;
    Map<Entity, BlockPos>                locationsByPassenger = Maps.newHashMap();

    public EntityRocket(World par1World)
    {
        super(par1World);
        this.ignoreFrustumCheck = true;
        this.hurtResistantTime = 0;
        this.isImmuneToFire = true;
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

    public EntityRocket(World world, double x, double y, double z)
    {
        this(world);
        this.setPosition(x, y, z);
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        dataManager.register(SEAT, BlockPos.ORIGIN);
    }

    /** Applies a velocity to each of the entities pushing them away from each
     * other. Args: entity */
    @Override
    public void applyEntityCollision(Entity entity)
    {
        try
        {
            collider.applyEntityCollision(entity);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** Called when the entity is attacked. */
    public boolean attackEntityFrom(DamageSource source, int damage)
    {
        return false;
    }

    private BlockPos getSeat()
    {
        return dataManager.get(SEAT);
    }

    @Override
    public void updatePassenger(Entity passenger)
    {
        if (this.isPassenger(passenger))
        {
            if (passenger.isSneaking())
            {
                passenger.dismountRidingEntity();
            }
            IMultiplePassengerEntity.MultiplePassengerManager.managePassenger(passenger, this);
        }
    }

    @Override
    protected void addPassenger(Entity passenger)
    {
        super.addPassenger(passenger);
        // Added location elsewhere (probably when mounting)
        if (locationsByPassenger.containsKey(passenger)) return;

        // TODO here we look for a seat to put them in
        locationsByPassenger.put(passenger, getSeat());
    }

    @Override
    protected void removePassenger(Entity passenger)
    {
        locationsByPassenger.remove(passenger);
        super.removePassenger(passenger);
    }

    @Override
    protected boolean canFitPassenger(Entity passenger)
    {
        // TODO check for seats here.
        return this.getPassengers().size() < 10;
    }

    /** If a rider of this entity can interact with this entity. Should return
     * true on the ridden entity if so.
     *
     * @return if the entity can be interacted with from a rider */
    @Override
    public boolean canRiderInteract()
    {
        return true;
    }

    /** Returns true if other Entities should be prevented from moving through
     * this Entity. */
    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    /** Returns true if this entity should push and be pushed by other entities
     * when colliding. */
    @Override
    public boolean canBePushed()
    {
        return false;
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

    public void doMotion()
    {
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;
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
            BlockPos pos = IBlockEntity.BlockEntityFormer.rayTraceInternal(
                    player.getPositionVector().addVector(0, player.getEyeHeight(), 0).subtract(getPositionVector()),
                    vec, this);
            IBlockState state = getFakeWorld().getBlockState(pos);
            // Ray trace to a litte more than vec, and look for solid blocks.
            // TODO hit x, y, and z.
            float hitX, hitY, hitZ;
            hitX = hitY = hitZ = 0;

            if (state.getBlock() instanceof BlockStairs && !isPassenger(player))
            {
                pos = pos.subtract(this.getPosition());
                System.out.println(player + " " + pos);
                System.out.println(locationsByPassenger);
                locationsByPassenger.put(player, pos);
                dataManager.set(SEAT, pos);
                if (!player.startRiding(this)) locationsByPassenger.remove(player);
                return EnumActionResult.SUCCESS;
            }

            boolean activate = state.getBlock().onBlockActivated(getFakeWorld(), pos, state, player, hand, stack,
                    EnumFacing.DOWN, hitX, hitY, hitZ);
            if (activate) return EnumActionResult.SUCCESS;

        }
        return EnumActionResult.FAIL;
    }

    /** First layer of player interaction */
    @Override
    public boolean processInitialInteract(EntityPlayer player, @Nullable ItemStack stack, EnumHand hand)
    {
        if (hand != EnumHand.MAIN_HAND) return false;
        if (stack != null && !worldObj.isRemote)
        {
            System.out.println("interact " + stack.getDisplayName());

            if (stack.getDisplayName().equals("x"))
            {
                this.motionX += 0.5;
            }
            else if (stack.getDisplayName().equals("-x"))
            {
                this.motionX += -0.5;
            }
            else if (stack.getDisplayName().equals("z"))
            {
                this.motionZ += 0.5;
            }
            else if (stack.getDisplayName().equals("-z"))
            {
                this.motionZ += -0.5;
            }
            else if (stack.getDisplayName().equals("y"))
            {
                this.motionY += 0.5;
            }
            else if (stack.getDisplayName().equals("-y"))
            {
                this.motionY += -0.5;
            }
            else if (stack.getDisplayName().equals("r"))
            {
                this.rotationYaw += 10;
            }
            else if (stack.getDisplayName().equals("-r"))
            {
                this.rotationYaw -= 10;
            }
            else if (stack.getItem() == Items.STICK && stack.getTagCompound() == null)
            {
                this.setDead();
            }

            System.out.println(motionX + " " + motionY + " " + motionZ);
            // PacketHandler.sendEntityUpdate(this);
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
        collider.onUpdate();
        // this.motionY -= 0.007;
        this.setPosition(this.posX, 10, this.posZ);
        // super.onUpdate();
        this.prevRotationYaw = rotationYaw;
        this.rotationYaw += 1;

        float pitchTime = this.ticksExisted * 0.01f;
        this.prevRotationPitch = this.rotationPitch;
        this.rotationPitch = -90 + (float) Math.toDegrees(Math.acos(Math.cos(pitchTime)));

      this.rotationYaw = 0;
          this.rotationPitch = 0;
        doMotion();
        checkCollision();
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
            boundMin = new BlockPos(bounds.getInteger("minx"), bounds.getInteger("miny"), bounds.getInteger("minz"));
            boundMax = new BlockPos(bounds.getInteger("maxx"), bounds.getInteger("maxy"), bounds.getInteger("maxz"));
        }
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
            tag = buff.readNBTTagCompoundFromBuffer();
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
        if (!worldObj.isRemote && !this.isDead)
        {
            IBlockEntity.BlockEntityFormer.RevertEntity(this);
        }
        super.setDead();
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
        NBTTagCompound vector = new NBTTagCompound();
        vector.setInteger("minx", boundMin.getX());
        vector.setInteger("miny", boundMin.getY());
        vector.setInteger("minz", boundMin.getZ());
        vector.setInteger("maxx", boundMax.getX());
        vector.setInteger("maxy", boundMax.getY());
        vector.setInteger("maxz", boundMax.getZ());
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

    @Override
    public Vector3f getSeat(Entity passenger)
    {
        Vector3f ret = new Vector3f();
        BlockPos pos = getSeat();
        if (pos != null) ret.set(pos.getX(), pos.getY() + 0.5f, pos.getZ());
        return ret;
    }

    @Override
    public Entity getPassenger(Vector3f seat)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Vector3f> getSeats()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public float getYaw()
    {
        return this.rotationYaw;
    }

    @Override
    public float getPitch()
    {
        // TODO datawatcher value of pitch.
        return this.rotationPitch;
    }

    @Override
    public float getPrevYaw()
    {
        return prevRotationYaw;
    }

    @Override
    public float getPrevPitch()
    {
        return prevRotationPitch;
    }

}
