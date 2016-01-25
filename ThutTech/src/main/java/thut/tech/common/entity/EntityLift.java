package thut.tech.common.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import thut.api.ThutBlocks;
import thut.api.entity.IMultibox;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;
import thut.tech.common.blocks.lift.BlockLiftRail;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.handlers.ConfigHandler;
import thut.tech.common.items.ItemLinker;

public class EntityLift extends EntityLivingBase implements IEntityAdditionalSpawnData, IMultibox
{
    static final int DESTINATIONFLOORDW = 24;
    static final int DESTINATIONYDW     = 25;
    static final int CURRENTFLOORDW     = 26;

    @Deprecated
    public double  size    = 1;
    //TODO swap over to using this, to allow not-odd-square lifts.
    public int[][] corners = new int[2][2];

    public double                            speedUp           = ConfigHandler.LiftSpeedUp;
    public double                            speedDown         = -ConfigHandler.LiftSpeedDown;
    public static int                        ACCELERATIONTICKS = 20;
    public double                            acceleration      = 0.05;
    public boolean                           up                = true;
    public boolean                           toMoveY           = false;
    public boolean                           moved             = false;
    public boolean                           axis              = true;
    public boolean                           hasPassenger      = false;
    public static boolean                    AUGMENTG          = true;
    int                                      n                 = 0;
    int                                      passengertime     = 10;
    boolean                                  first             = true;
    Random                                   r                 = new Random();
    public UUID                              id                = UUID.randomUUID();
    public UUID                              owner;
    private static HashMap<UUID, EntityLift> lifts             = new HashMap<UUID, EntityLift>();
    private static HashMap<UUID, EntityLift> lifts2            = new HashMap<UUID, EntityLift>();

    public double prevFloorY = 0;
    public double prevFloor  = 0;

    public boolean       called = false;
    TileEntityLiftAccess current;

    Matrix3 mainBox = new Matrix3();
    Matrix3 tempBox = new Matrix3();

    public HashMap<String, Matrix3> boxes   = new HashMap<String, Matrix3>();
    public HashMap<String, Vector3> offsets = new HashMap<String, Vector3>();

    public int[] floors = new int[64];

    Matrix3 base  = new Matrix3();
    Matrix3 top   = new Matrix3();
    Matrix3 wall1 = new Matrix3();

    public ItemStack[][] blocks    = null;
    private ItemStack[]  inventory = new ItemStack[1];

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

    public boolean canRenderOnFire()
    {
        return false;
    }

    /** Checks if the entity's current position is a valid location to spawn
     * this entity. */
    public boolean getCanSpawnHere()
    {
        return false;
    }

    public boolean isPotionApplicable(PotionEffect par1PotionEffect)
    {
        return false;
    }

    public EntityLift(World world, double x, double y, double z, double size)
    {
        this(world);
        this.setPosition(x, y, z);
        r.setSeed(100);
        this.size = Math.max(size, 1);
        this.setSize((float) this.size, 1f);
        lifts.put(id, this);
    }

    @Override
    public void onUpdate()
    {
        this.prevPosY = posY;
        if ((int) size != (int) this.width)
        {
            this.setSize((float) size, 1f);
        }

        if (first)
        {
            checkRails(0);
            first = false;
        }
        clearLiquids();

        if (motionY == 0)
        {
            this.setPosition(posX, Math.round(posY), posZ);
        }

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

    public void accelerate()
    {
        motionX = 0;
        motionZ = 0;
        if (!toMoveY) motionY *= 0.5;
        else
        {
            if (up) motionY = Math.min(speedUp, motionY + acceleration * speedUp);
            else motionY = Math.max(speedDown, motionY + acceleration * speedDown);
        }
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
                    if (current != null)
                    {
                        current.setCalled(false);
                        current = null;
                    }
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
                    if (current != null)
                    {
                        current.setCalled(false);
                        current = null;
                    }
                    motionY = 0;
                    toMoveY = false;
                    moved = false;
                }
            }
        }
        toMoveY = false;
        moved = false;
    }

    public boolean checkBlocks(double dir)
    {
        boolean ret = true;
        Vector3 thisloc = Vector3.getNewVectorFromPool().set(this).addTo(0, dir, 0);

        if (called)
        {
            if (dir > 0 && thisloc.y > getDestY())
            {
                thisloc.freeVectorFromPool();
                return false;
            }
            if (dir < 0 && thisloc.y < getDestY())
            {
                thisloc.freeVectorFromPool();
                return false;
            }
        }

        int rad = (int) (Math.floor(size / 2));
        Vector3 v = Vector3.getNewVectorFromPool();
        for (int i = -rad; i <= rad; i++)
            for (int j = -rad; j <= rad; j++)
            {
                ret = ret && (v.set(thisloc).addTo(i, 0, j)).clearOfBlocks(worldObj);
                ret = ret && (v.set(thisloc).addTo(i, 5, j)).clearOfBlocks(worldObj);
            }
        // TODO decide if I want to re-add rail checks
        return ret;
    }

    public void clearLiquids()
    {
        // int rad = (int) (Math.floor(size / 2));
        //
        // Vector3 thisloc = Vector3.getNewVectorFromPool().set(this);
        // Vector3 v = Vector3.getNewVectorFromPool();
        // for (int i = -rad; i <= rad; i++)
        // for (int j = -rad; j <= rad; j++)
        // {
        // Vector3 check = (v.set(thisloc).addTo(i, 5, j));
        // if (check.isFluid(worldObj))
        // {
        // System.out.println("Setting to air");
        // check.setAir(worldObj);
        // }
        // check = (v.set(thisloc).addTo(i, 0, j));
        // if (check.isFluid(worldObj))
        // {
        // System.out.println("Setting to air");
        // check.setAir(worldObj);
        // }
        // }
        // v.freeVectorFromPool();
        // thisloc.freeVectorFromPool();
    }

    public boolean checkRails(double dir)
    {
        int rad = (int) (1 + Math.floor(size / 2));

        int[][] sides = { { rad, 0 }, { -rad, 0 }, { 0, rad }, { 0, -rad } };

        boolean ret = true;
        boolean rightBlock = false;
        // BlockCoord posA = new BlockCoord();
        // BlockCoord posB = new BlockCoord();
        Vector3 posA = Vector3.getNewVectorFromPool();
        Vector3 posB = Vector3.getNewVectorFromPool();
        // TODO checking positions
        for (int i = 0; i < 2; i++)
        {
            posA.set((int) Math.floor(posX) + sides[axis ? 2 : 0][0], (int) Math.floor(posY + dir + i),
                    (int) Math.floor(posZ) + sides[axis ? 2 : 0][1]);
            BlockLiftRail.isRail(worldObj, posA.getPos());
            ret = ret && rightBlock;

            posB.set((int) Math.floor(posX) + sides[axis ? 3 : 1][0], (int) Math.floor(posY + dir + i),
                    (int) Math.floor(posZ) + sides[axis ? 3 : 1][1]);
            BlockLiftRail.isRail(worldObj, posB.getPos());
            ret = ret && rightBlock;
            if (ret)
            {
                TileEntityLiftAccess teA = (TileEntityLiftAccess) posA.getTileEntity(worldObj);
                TileEntityLiftAccess teB = (TileEntityLiftAccess) posB.getTileEntity(worldObj);
                if (teA != null && teA.lift == null) teA.setLift(this);
                if (teB != null && teB.lift == null) teB.setLift(this);
            }
        }

        posA.freeVectorFromPool();
        posB.freeVectorFromPool();

        if ((!ret && dir == 0))
        {
            axis = !axis;
        }

        return ret;
    }

    @SuppressWarnings("unused") // TODO make use of this
    private boolean consumePower()
    {
        boolean power = false;
        // int sizeFactor = size == 1 ? 4 : size == 3 ? 23 : 55;
        double energyCost = 0;// (destinationY - posY)*ENERGYCOST*sizeFactor;
        if (energyCost <= 0) return true;
        if (!power) toMoveY = false;
        return power;

    }

    public void checkCollision()
    {
        List<?> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(posX - (size + 1),
                posY, posZ - (size + 1), posX + (size + 1), posY + 6, posZ + (size + 1)));

        if (list != null && !list.isEmpty())
        {
            if (list.size() == 1 && this.riddenByEntity != null) { return; }

            for (int i = 0; i < list.size(); ++i)
            {
                Entity entity = (Entity) list.get(i);
                {
                    applyEntityCollision(entity);
                }
            }
        }
    }

    /** Applies a velocity to each of the entities pushing them away from each
     * other. Args: entity */
    public void applyEntityCollision(Entity entity)
    {
        Vector3 v = Vector3.getNewVectorFromPool();
        Vector3 v1 = Vector3.getNewVectorFromPool();

        AxisAlignedBB box = Matrix3.getAABB(posX - size / 2, posY, posZ - size / 2, posX + size / 2, posY + 1,
                posZ + size / 2);

        ArrayList<AxisAlignedBB> aabbs = Lists.newArrayList();
        aabbs.add(box);

        v.setToVelocity(entity).subtractFrom(v1.setToVelocity(this));
        v1.clear();
        Matrix3.doCollision(aabbs, entity.getEntityBoundingBox(), entity, 0, v, v1);
        if (!v1.isEmpty())
        {
            if (v1.y >= 0)
            {
                entity.onGround = true;
                entity.fallDistance = 0;
                entity.fall(entity.fallDistance, 0);
            }
            else if (v1.y < 0)
            {
                boolean below = entity.posY + entity.height - (entity.motionY + motionY) < posY;

                if (below)
                {
                    v1.y = 0;
                }
            }
            if (v1.x != 0) entity.motionX = 0;
            if (v1.y != 0) entity.motionY = motionY;
            if (v1.z != 0) entity.motionZ = 0;

            v1.addTo(v.set(entity));
            v1.moveEntity(entity);
        }
        if (entity instanceof EntityPlayer)
        {// TODO make sure this properly removes things.
            EntityPlayer player = (EntityPlayer) entity;
            if (Math.abs(player.motionY) < 0.1 && !player.capabilities.isFlying)
            {
                entity.onGround = true;
                entity.fallDistance = 0;
            }
            if (!player.capabilities.isCreativeMode)
            {
                if (player.posY < posY + 5) player.capabilities.allowFlying = true;
                else player.capabilities.allowFlying = false;
            }
        }

        v.freeVectorFromPool();
        v1.freeVectorFromPool();

    }

    /** First layer of player interaction */
    public boolean interactFirst(EntityPlayer player)
    {
        ItemStack item = player.getHeldItem();
        if (player.isSneaking() && item != null && item.getItem() instanceof ItemLinker)
        {
            if (item.getTagCompound() == null)
            {
                item.setTagCompound(new NBTTagCompound());
            }
            item.getTagCompound().setString("lift", id.toString());

            String message = StatCollector.translateToLocalFormatted("msg.liftSet.name");

            if (worldObj.isRemote) player.addChatMessage(new ChatComponentText(message));
            return true;
        }
        else if (item != null && item.getItem() instanceof ItemLinker)
        {
            if (!worldObj.isRemote && owner != null)
            {
                Entity ownerentity = worldObj.getPlayerEntityByUUID(owner);
                String message = StatCollector.translateToLocalFormatted("msg.lift.owner", ownerentity.getName());

                player.addChatMessage(new ChatComponentText(message));
            }
        }
        if ((player.isSneaking() && item != null
                && (player.getHeldItem().getItem().getUnlocalizedName().toLowerCase().contains("wrench")
                        || player.getHeldItem().getItem().getUnlocalizedName().toLowerCase().contains("screwdriver")
                        || player.getHeldItem().getItem().getUnlocalizedName()
                                .equals(Items.stick.getUnlocalizedName())))
                && ((owner != null && player.getUniqueID() == owner) || player.capabilities.isCreativeMode))
        {
            if (!worldObj.isRemote)
            {
                String message = StatCollector.translateToLocalFormatted("msg.lift.killed");
                player.addChatMessage(new ChatComponentText(message));
                setDead();
            }
            return true;
        }
        else if (player.isSneaking() && item != null && Block.getBlockFromItem(item.getItem()) != null
                && (owner == null || owner.equals(player.getUniqueID()))) // &&
        // !worldObj.isRemote)
        {
            Block block = Block.getBlockFromItem(item.getItem());
            if (block.isNormalCube())
            {
                ItemStack item2 = item.splitStack(1);
                if (this.getHeldItem() != null && !worldObj.isRemote)
                {
                    this.entityDropItem(getHeldItem(), 1);
                }
                this.setCurrentItemOrArmor(0, item2);
            }
        }
        else if (player.isSneaking() && item == null && (owner == null || owner.equals(player.getUniqueID()))) // &&
        // !worldObj.isRemote)
        {
            if (this.getHeldItem() != null && !worldObj.isRemote)
            {
                this.entityDropItem(getHeldItem(), 1);
            }
            this.setCurrentItemOrArmor(0, null);

        }

        return false;
    }

    /** Will get destroyed next tick. */
    public void setDead()
    {
        if (!worldObj.isRemote && !this.isDead)
        {
            if (blocks != null)
            {
                for (ItemStack[] barr : blocks)
                {
                    for (ItemStack b : barr)
                    {
                        this.entityDropItem(b, 0.5f);
                    }
                }
                if (this.getHeldItem() != null) this.entityDropItem(getHeldItem(), 1);
            }
            else
            {
                int iron = size == 1 ? 0 : size == 3 ? 8 : 24;
                if (iron > 0) this.dropItem(Item.getItemFromBlock(Blocks.iron_block), iron);
                this.dropItem(Item.getItemFromBlock(ThutBlocks.lift), 1);
                if (this.getHeldItem() != null) this.entityDropItem(getHeldItem(), 1);
            }
        }
        super.setDead();
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        data.writeDouble(size);
        data.writeLong(id.getMostSignificantBits());
        data.writeLong(id.getLeastSignificantBits());
        for (int i = 0; i < 64; i++)
        {
            data.writeInt(floors[i]);
        }
        PacketBuffer buff = new PacketBuffer(data);
        NBTTagCompound tag = new NBTTagCompound();
        writeBlocks(tag);
        buff.writeNBTTagCompoundToBuffer(tag);
        lifts.put(id, this);
        data.writeBoolean(owner != null);
        if (owner != null)
        {
            data.writeLong(owner.getMostSignificantBits());
            data.writeLong(owner.getLeastSignificantBits());
        }

    }

    @Override
    public void readSpawnData(ByteBuf data)
    {

        size = data.readDouble();
        id = new UUID(data.readLong(), data.readLong());

        for (int i = 0; i < 64; i++)
        {
            floors[i] = data.readInt();
        }
        lifts2.put(id, this);
        this.setSize((float) this.size, 1f);

        PacketBuffer buff = new PacketBuffer(data);
        NBTTagCompound tag = new NBTTagCompound();
        try
        {
            tag = buff.readNBTTagCompoundFromBuffer();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (data.readBoolean())
        {
            owner = new UUID(data.readLong(), data.readLong());
        }

        readBlocks(tag);
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

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        axis = nbt.getBoolean("axis");
        size = nbt.getDouble("size");
        id = new UUID(nbt.getLong("higher"), nbt.getLong("lower"));
        if (nbt.hasKey("ownerhigher")) owner = new UUID(nbt.getLong("ownerhigher"), nbt.getLong("ownerlower"));

        if (nbt.hasKey("replacement"))
        {
            NBTTagCompound held = nbt.getCompoundTag("replacement");
            inventory[0] = ItemStack.loadItemStackFromNBT(held);
        }
        readList(nbt);
        readBlocks(nbt);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        nbt.setBoolean("axis", axis);
        nbt.setDouble("size", size);
        nbt.setLong("lower", id.getLeastSignificantBits());
        nbt.setLong("higher", id.getMostSignificantBits());

        if (owner != null)
        {
            nbt.setLong("ownerlower", owner.getLeastSignificantBits());
            nbt.setLong("ownerhigher", owner.getMostSignificantBits());
        }
        if (inventory[0] != null)
        {
            NBTTagCompound held = new NBTTagCompound();
            inventory[0].writeToNBT(held);
            nbt.setTag("replacement", held);
        }

        writeList(nbt);
        try
        {
            writeBlocks(nbt);
        }
        catch (Exception e)
        {
        }
    }

    public void writeList(NBTTagCompound nbt)
    {
        for (int i = 0; i < 64; i++)
        {
            nbt.setInteger("floors " + i, floors[i]);
        }
    }

    public void readList(NBTTagCompound nbt)
    {
        for (int i = 0; i < 64; i++)
        {
            floors[i] = nbt.getInteger("floors " + i);
            if (floors[i] == 0) floors[i] = -1;
        }
    }

    public void writeBlocks(NBTTagCompound nbt)
    {
        if (blocks != null)
        {
            nbt.setInteger("BlocksLength", blocks.length);
            int size = blocks.length;
            for (int i = 0; i < size; i++)
                for (int j = 0; j < size; j++)
                {
                    ItemStack b = blocks[i][j];
                    if (b == null || b.getItem() == null) b = new ItemStack(Blocks.iron_block);

                    nbt.setInteger("block" + i + "," + j, Item.getIdFromItem(b.getItem()));
                    nbt.setInteger("meta", b.getItemDamage());
                }
        }
    }

    public void readBlocks(NBTTagCompound nbt)
    {
        if (nbt.hasKey("BlocksLength"))
        {
            int size = nbt.getInteger("BlocksLength");
            blocks = new ItemStack[size][size];
            for (int i = 0; i < size; i++)
                for (int j = 0; j < size; j++)
                {
                    int n = nbt.getInteger("block" + i + "," + j);
                    ItemStack b = new ItemStack(Item.getItemById(n), 1, nbt.getInteger("meta"));
                    blocks[i][j] = b;
                }
        }
        else
        {
            int size = (int) Math.round(this.size);
            blocks = new ItemStack[size][size];
            for (int i = 0; i < size; i++)
                for (int j = 0; j < size; j++)
                {
                    blocks[i][j] = new ItemStack(Blocks.iron_block);
                }
        }
    }

    public static void clear()
    {
        lifts2.clear();
        lifts.clear();
    }

    public static EntityLift getLiftFromUUID(UUID uuid, boolean client)
    {
        if (client) return lifts2.get(uuid);
        return lifts.get(uuid);
    }

    @Override
    public void setBoxes()
    {
        mainBox.boxMin().set(-size / 2d, 0, -size / 2d);
        mainBox.boxMax().set(size / 2d, 1, size / 2d);

        Matrix3 m2;
        if (!boxes.containsKey("base"))
        {
            m2 = new Matrix3();
            boxes.put("base", m2);
        }
        else
        {
            m2 = boxes.get("base");
        }

        m2.boxMin().clear();
        m2.boxMax().set(size, 1, size);
        m2.boxRotation().clear();
        boxes.put("base", m2);
    }

    @Override
    public void setOffsets()
    {

        Vector3 v2;
        if (!offsets.containsKey("base"))
        {
            v2 = Vector3.getNewVectorFromPool();
            offsets.put("base", v2);
        }
        else
        {
            v2 = offsets.get("base");
        }
        v2.set(0 - size / 2, 0, 0 - size / 2);
    }

    @Override
    public HashMap<String, Matrix3> getBoxes()
    {
        return boxes;
    }

    @Override
    public HashMap<String, Vector3> getOffsets()
    {
        return offsets;
    }

    @Override
    public Matrix3 bounds(Vector3 target)
    {

        tempBox.boxMin().set(-size / 2, 0, -size / 2);
        tempBox.boxMax().set(size / 2, 1, size / 2);

        return tempBox;
    }

    /** Called when the entity is attacked. */
    public boolean attackEntityFrom(DamageSource source, int damage)
    {
        if (damage > 15) { return true; }

        return false;
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(24, Integer.valueOf(0));
        this.dataWatcher.addObject(25, Integer.valueOf(0));
        this.dataWatcher.addObject(26, Integer.valueOf(-1));
    }

    @Override
    public ItemStack getHeldItem()
    {
        return getInventory()[0];
    }

    @Override
    public ItemStack getEquipmentInSlot(int var1)
    {
        return getHeldItem();
    }

    @Override
    public void setCurrentItemOrArmor(int var1, ItemStack var2)
    {
        getInventory()[0] = var2;
    }

    /** @return the destinationFloor */
    public int getDestinationFloor()
    {
        return dataWatcher.getWatchableObjectInt(DESTINATIONFLOORDW);
    }

    /** @param destinationFloor
     *            the destinationFloor to set */
    public void setDestinationFloor(int destinationFloor)
    {
        dataWatcher.updateObject(DESTINATIONFLOORDW, Integer.valueOf(destinationFloor));
    }

    /** @return the destinationFloor */
    public int getCurrentFloor()
    {
        return dataWatcher.getWatchableObjectInt(CURRENTFLOORDW);
    }

    /** @param currentFloor
     *            the destinationFloor to set */
    public void setCurrentFloor(int currentFloor)
    {
        dataWatcher.updateObject(CURRENTFLOORDW, Integer.valueOf(currentFloor));
    }

    /** @return the destinationFloor */
    public int getDestY()
    {
        return dataWatcher.getWatchableObjectInt(DESTINATIONYDW);
    }

    /** @param dest
     *            the destinationFloor to set */
    public void setDestY(int dest)
    {
        dataWatcher.updateObject(DESTINATIONYDW, Integer.valueOf(dest));
    }

    /** returns the bounding box for this entity */
    public AxisAlignedBB getBoundingBox()
    {
        return null;
    }

    /** Returns true if other Entities should be prevented from moving through
     * this Entity. */
    public boolean canBeCollidedWith()
    {
        return !this.isDead;
    }

    /** Returns true if this entity should push and be pushed by other entities
     * when colliding. */
    public boolean canBePushed()
    {
        return true;
    }

    @Override
    public ItemStack getCurrentArmor(int slotIn)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ItemStack[] getInventory()
    {
        // TODO Auto-generated method stub
        return inventory;
    }

}
