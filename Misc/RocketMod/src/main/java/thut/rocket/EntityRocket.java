package thut.rocket;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityRocket extends EntityLivingBase implements IEntityAdditionalSpawnData
{
    public static class EntityWorld extends World
    {

        final EntityRocket lift;

        public EntityWorld(EntityRocket lift)
        {
            super(lift.worldObj.getSaveHandler(), lift.worldObj.getWorldInfo(), lift.worldObj.provider,
                    lift.worldObj.theProfiler, lift.worldObj.isRemote);
            this.lift = lift;

        }

        @Override
        public TileEntity getTileEntity(BlockPos pos)
        {
            if (lift.tiles == null) { return null; }
            int i = pos.getX() - MathHelper.floor_double(lift.posX + lift.boundMin.getX());
            int j = (int) (pos.getY() - Math.round(lift.posY + lift.boundMin.getY()));
            int k = pos.getZ() - MathHelper.floor_double(lift.posZ + lift.boundMin.getZ());
            if (i >= lift.tiles.length || j >= lift.tiles[0].length || k >= lift.tiles[0][0].length || i < 0 || j < 0
                    || k < 0) { return null; }
            if (lift.tiles[i][j][k] != null) lift.tiles[i][j][k].setPos(new BlockPos(pos));
            return lift.tiles[i][j][k];
        }

        @Override
        public int getCombinedLight(BlockPos pos, int lightValue)
        {
            return 15 << 20 | 15 << 4;
        }

        @Override
        public IBlockState getBlockState(BlockPos pos)
        {
            int i = pos.getX() - MathHelper.floor_double(lift.posX + lift.boundMin.getX());
            int j = (int) (pos.getY() - Math.round(lift.posY + lift.boundMin.getY()));
            int k = pos.getZ() - MathHelper.floor_double(lift.posZ + lift.boundMin.getZ());
            Block b = Blocks.AIR;

            if (lift.blocks == null) { return Blocks.AIR.getDefaultState(); }
            int meta = 0;
            if (i >= lift.blocks.length || j >= lift.blocks[0].length || k >= lift.blocks[0][0].length || i < 0 || j < 0
                    || k < 0)
            {
                return b.getDefaultState();
            }
            else
            {
                ItemStack stack = lift.blocks[i][j][k];
                if (stack == null || stack.getItem() == null) return Blocks.AIR.getDefaultState();
                b = Block.getBlockFromItem(stack.getItem());
                meta = stack.getItemDamage();
            }
            @SuppressWarnings("deprecation")
            IBlockState iblockstate = b.getStateFromMeta(meta);
            return iblockstate;
        }

        @Override
        public boolean isAirBlock(BlockPos pos)
        {
            IBlockState state = getBlockState(pos);
            return state.getBlock().isAir(state, this, pos);
        }

        @Override
        public Biome getBiomeGenForCoords(BlockPos pos)
        {
            return lift.getEntityWorld().getBiomeGenForCoords(pos);
        }

        @Override
        public int getStrongPower(BlockPos pos, EnumFacing direction)
        {
            return lift.getEntityWorld().getStrongPower(pos, direction);
        }

        @Override
        public WorldType getWorldType()
        {
            return lift.getEntityWorld().getWorldType();
        }

        @Override
        public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default)
        {
            return getBlockState(pos).isSideSolid(this, pos, side);
        }

        @Override
        protected IChunkProvider createChunkProvider()
        {
            return null;
        }

        @Override
        protected boolean isChunkLoaded(int x, int z, boolean allowEmpty)
        {
            return false;
        }

        /** Sets the block state at a given location. Flag 1 will cause a block
         * update. Flag 2 will send the change to clients (you almost always
         * want this). Flag 4 prevents the block from being re-rendered, if this
         * is a client world. Flags can be added together. */
        public boolean setBlockState(BlockPos pos, IBlockState newState, int flags)
        {
            int i = pos.getX() - MathHelper.floor_double(lift.posX + lift.boundMin.getX());
            int j = (int) (pos.getY() - Math.round(lift.posY + lift.boundMin.getY()));
            int k = pos.getZ() - MathHelper.floor_double(lift.posZ + lift.boundMin.getZ());
            if (lift.blocks == null) return false;
            if (i >= lift.blocks.length || j >= lift.blocks[0].length || k >= lift.blocks[0][0].length || i < 0 || j < 0
                    || k < 0) { return false; }
            Block b = newState.getBlock();
            lift.blocks[i][j][k] = new ItemStack(b, 1, b.getMetaFromState(newState));
            return false;
        }
    }

    static final DataParameter<BlockPos> SEAT              = EntityDataManager.<BlockPos> createKey(EntityRocket.class,
            DataSerializers.BLOCK_POS);

    public static int                    ACCELERATIONTICKS = 20;

    public static boolean                ENERGYUSE         = false;
    public static int                    ENERGYCOST        = 100;

    public static TileEntity makeTile(NBTTagCompound tag)
    {
        return TileEntity.create(tag);
    }

    public static EntityRocket makeRocket(World worldObj, BlockPos min, BlockPos max, BlockPos pos)
    {
        EntityRocket rocket = new EntityRocket(worldObj, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        rocket.blocks = checkBlocks(worldObj, min, max, pos);
        rocket.tiles = checkTiles(worldObj, min, max, pos);
        rocket.boundMin = min;
        rocket.boundMax = max;
        removeBlocks(worldObj, min, max, pos);
        worldObj.spawnEntityInWorld(rocket);
        return rocket;
    }

    public static void removeBlocks(World worldObj, BlockPos min, BlockPos max, BlockPos pos)
    {
        int xMin = min.getX();
        int zMin = min.getZ();
        int xMax = max.getX();
        int zMax = max.getZ();
        int yMin = min.getY();
        int yMax = max.getY();
        for (int i = xMin; i <= xMax; i++)
            for (int j = yMin; j <= yMax; j++)
                for (int k = zMin; k <= zMax; k++)
                {
                    BlockPos temp = pos.add(i, j, k);
                    TileEntity tile = worldObj.getTileEntity(temp);
                    if (tile != null) tile.invalidate();
                    worldObj.setBlockState(temp, Blocks.AIR.getDefaultState(), 2);
                }
    }

    public static ItemStack[][][] checkBlocks(World worldObj, BlockPos min, BlockPos max, BlockPos pos)
    {
        int xMin = min.getX();
        int zMin = min.getZ();
        int xMax = max.getX();
        int zMax = max.getZ();
        int yMin = min.getY();
        int yMax = max.getY();
        ItemStack[][][] ret = new ItemStack[(xMax - xMin) + 1][(yMax - yMin) + 1][(zMax - zMin) + 1];
        for (int i = xMin; i <= xMax; i++)
            for (int j = yMin; j <= yMax; j++)
                for (int k = zMin; k <= zMax; k++)
                {
                    IBlockState state = worldObj.getBlockState(pos.add(i, j, k));
                    Block b = state.getBlock();
                    ret[i - xMin][j - yMin][k - zMin] = new ItemStack(b, 1, b.getMetaFromState(state));
                }
        return ret;
    }

    public static TileEntity[][][] checkTiles(World worldObj, BlockPos min, BlockPos max, BlockPos pos)
    {
        int xMin = min.getX();
        int zMin = min.getZ();
        int xMax = max.getX();
        int zMax = max.getZ();
        int yMin = min.getY();
        int yMax = max.getY();
        TileEntity[][][] ret = new TileEntity[(xMax - xMin) + 1][(yMax - yMin) + 1][(zMax - zMin) + 1];
        for (int i = xMin; i <= xMax; i++)
            for (int j = yMin; j <= yMax; j++)
                for (int k = zMin; k <= zMax; k++)
                {
                    BlockPos temp = pos.add(i, j, k);
                    IBlockState state = worldObj.getBlockState(temp);
                    if (((state.getBlock()) instanceof ITileEntityProvider))
                    {
                        TileEntity old = worldObj.getTileEntity(temp);
                        if (old != null)
                        {
                            NBTTagCompound tag = new NBTTagCompound();
                            tag = old.writeToNBT(tag);
                            ret[i - xMin][j - yMin][k - zMin] = makeTile(tag);
                        }
                    }
                }
        return ret;
    }

    public BlockPos         boundMin             = BlockPos.ORIGIN;
    public BlockPos         boundMax             = BlockPos.ORIGIN;

    int                     energy               = 0;
    private EntityWorld     world;
    public double           speedUp              = 0.25;             // ConfigHandler.LiftSpeedUp;
    public double           speedDown            = -0.25;            // ConfigHandler.LiftSpeedDown;
    public double           acceleration         = 0.05;
    public UUID             owner;
    public ItemStack[][][]  blocks               = null;
    public TileEntity[][][] tiles                = null;
    final RocketCollider    collider;
    Map<Entity, BlockPos>   locationsByPassenger = Maps.newHashMap();

    public EntityRocket(World par1World)
    {
        super(par1World);
        this.ignoreFrustumCheck = true;
        this.hurtResistantTime = 0;
        this.isImmuneToFire = true;
        this.collider = new RocketCollider(this);
    }

    public EntityWorld getWorld()
    {
        if (world == null)
        {
            world = new EntityWorld(this);
            int xMin = boundMin.getX();
            int zMin = boundMin.getZ();
            int yMin = boundMin.getY();
            int sizeX = blocks.length;
            int sizeY = blocks[0].length;
            int sizeZ = blocks[0][0].length;
            for (int i = 0; i < sizeX; i++)
                for (int j = 0; j < sizeY; j++)
                    for (int k = 0; k < sizeZ; k++)
                    {
                        if (tiles[i][j][k] != null)
                        {
                            tiles[i][j][k].setWorldObj(world);
                            tiles[i][j][k].setPos(new BlockPos(i + xMin + posX, j + yMin + posY, k + zMin + posZ));
                            tiles[i][j][k].validate();
                        }
                    }
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
        if (!isPassenger(entity)) collider.applyEntityCollision(entity);
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
            BlockPos offset = locationsByPassenger.get(passenger);
            if (passenger.isSneaking())
            {
                passenger.dismountRidingEntity();
            }
            else if (offset == null)
            {

            }
            else passenger.setPosition(this.posX + offset.getX(),
                    this.posY + 0.75 + offset.getY() + passenger.getYOffset(), this.posZ + offset.getZ());
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

    public BlockPos rayTraceInternal(Vec3d start, Vec3d end)
    {
        Vec3d direction = end.subtract(start).normalize();
        double distance = 4;
        for (int i = 0; i < distance; i++)
        {
            Vec3d temp = start.add(direction.scale(i));
            int xMin = MathHelper.floor_double(temp.xCoord + posX);
            int yMin = MathHelper.floor_double(temp.yCoord + posY);
            int zMin = MathHelper.floor_double(temp.zCoord + posZ);
            BlockPos pos = new BlockPos(xMin, yMin, zMin);
            IBlockState state = getWorld().getBlockState(pos);
            // TODO ray trace against the state's boxes instead.
            if (state.getMaterial().isSolid()) return pos;
        }
        int xMin = MathHelper.floor_double(end.xCoord + posX);
        int yMin = MathHelper.floor_double(end.yCoord + posY);
        int zMin = MathHelper.floor_double(end.zCoord + posZ);
        return new BlockPos(xMin, yMin, zMin);
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
            BlockPos pos = rayTraceInternal(
                    player.getPositionVector().addVector(0, player.getEyeHeight(), 0).subtract(getPositionVector()),
                    vec);
            IBlockState state = getWorld().getBlockState(pos);
            System.out.println(state);
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

            boolean activate = state.getBlock().onBlockActivated(getWorld(), pos, state, player, hand, stack,
                    EnumFacing.DOWN, hitX, hitY, hitZ);
            if (activate) return EnumActionResult.SUCCESS;
            // else if (!state.getMaterial().isSolid()) return
            // EnumActionResult.SUCCESS;

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
        this.getWorld().setTotalWorldTime(this.worldObj.getTotalWorldTime());

        int sizeX = blocks.length;
        int sizeY = blocks[0].length;
        int sizeZ = blocks[0][0].length;
        for (int i = 0; i < sizeX; i++)
            for (int j = 0; j < sizeY; j++)
                for (int k = 0; k < sizeZ; k++)
                {
                    if (tiles[i][j][k] instanceof ITickable)
                    {
                        ((ITickable) tiles[i][j][k]).update();
                    }
                }

        this.height = boundMax.getY();
        this.width = 1 + boundMax.getX() - boundMin.getX();
        if (motionY == 0)
        {
            this.setPosition(posX, Math.round(posY), posZ);
        }
        this.motionY -= 0.007;
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
                                tiles[i][k][j] = makeTile(tag);
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
        energy = nbt.getInteger("energy");
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
            int xMin = boundMin.getX();
            int zMin = boundMin.getZ();
            int yMin = boundMin.getY();
            int sizeX = blocks.length;
            int sizeY = blocks[0].length;
            int sizeZ = blocks[0][0].length;
            for (int i = 0; i < sizeX; i++)
                for (int j = 0; j < sizeY; j++)
                    for (int k = 0; k < sizeZ; k++)
                    {
                        BlockPos pos = new BlockPos(i + xMin + posX, j + yMin + posY, k + zMin + posZ);
                        IBlockState state = getWorld().getBlockState(pos);
                        TileEntity tile = getWorld().getTileEntity(pos);
                        if (state != null)
                        {
                            worldObj.setBlockState(pos, state);
                            if (tile != null)
                            {
                                TileEntity newTile = worldObj.getTileEntity(pos);
                                newTile.readFromNBT(tile.writeToNBT(new NBTTagCompound()));
                            }
                        }
                    }
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
        nbt.setInteger("energy", energy);
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

}
