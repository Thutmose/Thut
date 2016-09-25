package thut.api.entity.blockentity;

import static java.lang.Math.max;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.vecmath.Vector3f;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import thut.api.maths.Matrix3;

public class BlockEntityUpdater
{
    final IBlockEntity  blockEntity;
    final Entity        theEntity;
    List<AxisAlignedBB> blockBoxes = Lists.newArrayList();
    Set<TileEntity>     erroredSet = Sets.newHashSet();

    public BlockEntityUpdater(IBlockEntity rocket)
    {
        this.blockEntity = rocket;
        this.theEntity = (Entity) rocket;
    }

    public void onSetPosition()
    {
        double xMin, yMin, zMin, xMax, yMax, zMax;
        xMin = theEntity.posX + blockEntity.getMin().getX() - 0.5;
        yMin = theEntity.posY + blockEntity.getMin().getY();
        zMin = theEntity.posZ + blockEntity.getMin().getZ() - 0.5;
        xMax = theEntity.posX + blockEntity.getMax().getX() + 0.5;
        yMax = theEntity.posY + blockEntity.getMax().getY() + 1;
        zMax = theEntity.posZ + blockEntity.getMax().getZ() + 0.5;
        theEntity.setEntityBoundingBox(new AxisAlignedBB(xMin, yMin, zMin, xMax, yMax, zMax));
    }

    public void onUpdate()
    {
        theEntity.height = blockEntity.getMax().getY();
        theEntity.width = 1 + blockEntity.getMax().getX() - blockEntity.getMin().getX();
        if (theEntity.motionY == 0)
        {
            theEntity.setPosition(theEntity.posX, Math.round(theEntity.posY), theEntity.posZ);
        }
        blockEntity.getFakeWorld().getWorldInfo().setWorldTotalTime(theEntity.worldObj.getTotalWorldTime());
        MutableBlockPos pos = new MutableBlockPos();
        int xMin = blockEntity.getMin().getX();
        int zMin = blockEntity.getMin().getZ();
        int yMin = blockEntity.getMin().getY();
        int sizeX = blockEntity.getTiles().length;
        int sizeY = blockEntity.getTiles()[0].length;
        int sizeZ = blockEntity.getTiles()[0][0].length;
        for (int i = 0; i < sizeX; i++)
            for (int j = 0; j < sizeY; j++)
                for (int k = 0; k < sizeZ; k++)
                {
                    pos.setPos(i + xMin + theEntity.posX, j + yMin + theEntity.posY, k + zMin + theEntity.posZ);

                    // TODO rotate here by entity rotation.

                    if (blockEntity.getTiles()[i][j][k] != null)
                    {
                        blockEntity.getTiles()[i][j][k].setPos(pos.toImmutable());
                        blockEntity.getTiles()[i][j][k].setWorldObj(blockEntity.getFakeWorld());
                    }
                    if (blockEntity.getTiles()[i][j][k] instanceof ITickable)
                    {
                        if (erroredSet.contains(blockEntity.getTiles()[i][j][k])) continue;
                        try
                        {
                            ((ITickable) blockEntity.getTiles()[i][j][k]).update();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            System.err.println("Error with Tile Entity " + blockEntity.getTiles()[i][j][k]);
                            erroredSet.add(blockEntity.getTiles()[i][j][k]);
                        }
                    }
                }
    }

    @SuppressWarnings("deprecation")
    public void applyEntityCollision(Entity entity)
    {
        if ((theEntity.rotationYaw + 360) % 90 > 5 || theEntity.isPassenger(entity)) return;
        blockBoxes.clear();
        int sizeX = blockEntity.getBlocks().length;
        int sizeY = blockEntity.getBlocks()[0].length;
        int sizeZ = blockEntity.getBlocks()[0][0].length;
        Set<Double> topY = Sets.newHashSet();
        MutableBlockPos pos = new MutableBlockPos();
        int xMin = blockEntity.getMin().getX();
        int zMin = blockEntity.getMin().getZ();
        int yMin = blockEntity.getMin().getY();
        // Expand by velociteis as well.
        // Adds AABBS for contained blocks
        for (int i = 0; i < sizeX; i++)
            for (int k = 0; k < sizeY; k++)
                for (int j = 0; j < sizeZ; j++)
                {
                    ItemStack stack = blockEntity.getBlocks()[i][k][j];
                    if (stack == null || stack.getItem() == null) continue;
                    List<AxisAlignedBB> toAdd = Lists.newArrayList();
                    pos.setPos(i + xMin, j + yMin, k + zMin);
                    Block block = Block.getBlockFromItem(stack.getItem());
                    IBlockState state = block.getStateFromMeta(stack.getItemDamage())
                            .getActualState(blockEntity.getFakeWorld(), pos);
                    try
                    {
                        toAdd.add(state.getCollisionBoundingBox(blockEntity.getFakeWorld(), pos));
                        // TODO find collsion box list instead.
                    }
                    catch (Exception e)
                    {
                        // blockBox = block.getBoundingBox(state, worldObj,
                        // pos);
                    }
                    for (AxisAlignedBB blockBox : toAdd)
                    {
                        if (blockBox != null)
                        {
                            float dx = 0.5f;
                            float dz = 0.5f;
                            AxisAlignedBB box = Matrix3.getAABB(
                                    theEntity.posX + blockBox.minX - dx + blockEntity.getMin().getX() + i,
                                    theEntity.posY + blockBox.minY + k,
                                    theEntity.posZ + blockBox.minZ - dz + blockEntity.getMin().getZ() + j,
                                    theEntity.posX + blockBox.maxX - dx + blockEntity.getMin().getX() + i,
                                    theEntity.posY + blockBox.maxY + k,
                                    theEntity.posZ + blockBox.maxZ - dz + blockEntity.getMin().getZ() + j);
                            blockBoxes.add(box);
                            topY.add(box.maxY);
                        }
                    }
                }
        // Add AABBS for blocks around under the base, to stop sending into
        // floor.
        pos.setPos(theEntity.getPosition());
        int mx = sizeX;
        int mz = sizeZ;
        if (sizeY > 1) for (int i = -1 - mx; i <= 1 + mx; i++)
            for (int j = -1 - mz; j <= 1 + mz; j++)
            {
                pos.setPos(theEntity.getPosition().down());
                pos.setPos(pos.getX() + i, pos.getY(), pos.getZ() + j);
                IBlockState state = theEntity.worldObj.getBlockState(pos);
                AxisAlignedBB blockBox = state.getActualState(theEntity.worldObj, pos)
                        .getCollisionBoundingBox(theEntity.worldObj, pos);
                if (blockBox != null)
                {
                    AxisAlignedBB box = blockBox.offset(pos);
                    blockBoxes.add(box);
                }
            }
        Vector3f temp1 = new Vector3f();
        Vector3f diffs = new Vector3f((float) (theEntity.motionX - entity.motionX),
                (float) (theEntity.motionY - entity.motionY), (float) (theEntity.motionY - entity.motionY));

        double minX = entity.getEntityBoundingBox().minX;
        double minY = entity.getEntityBoundingBox().minY;
        double minZ = entity.getEntityBoundingBox().minZ;
        double maxX = entity.getEntityBoundingBox().maxX;
        double maxY = entity.getEntityBoundingBox().maxY;
        double maxZ = entity.getEntityBoundingBox().maxZ;
        double factor = 0.75d;
        double dx = max(maxX - minX, 0.5) / factor + (entity.motionX - theEntity.motionX),
                dz = max(maxZ - minZ, 0.5) / factor + (entity.motionZ - theEntity.motionZ), r;

        AxisAlignedBB boundingBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);

        boolean merge = false;

        // Here we merge the boxes into less boxes, by taking any boxes with
        // shared faces and merging them.
        if (merge)
        {
            double dx1 = maxX - minX;
            double dy1 = maxY - minY;
            double dz1 = maxZ - minZ;
            Comparator<AxisAlignedBB> comparator = new Comparator<AxisAlignedBB>()
            {
                @Override
                public int compare(AxisAlignedBB arg0, AxisAlignedBB arg1)
                {
                    int minX0 = (int) (arg0.minX * 32);
                    int minY0 = (int) (arg0.minY * 32);
                    int minZ0 = (int) (arg0.minZ * 32);
                    int minX1 = (int) (arg1.minX * 32);
                    int minY1 = (int) (arg1.minY * 32);
                    int minZ1 = (int) (arg1.minZ * 32);
                    if (minX0 == minX1)
                    {
                        if (minZ0 == minZ1) return minY0 - minY1;
                        return minZ0 - minZ1;
                    }
                    return minX0 - minX1;
                }
            };
            AxisAlignedBB[] boxes = blockBoxes.toArray(new AxisAlignedBB[blockBoxes.size()]);
            blockBoxes.clear();
            Arrays.sort(boxes, comparator);
            AxisAlignedBB b1;
            AxisAlignedBB b2;
            for (int i = 0; i < boxes.length; i++)
            {
                b1 = boxes[i];
                if (b1 == null) continue;
                for (int j = 0; j < boxes.length; j++)
                {
                    b2 = boxes[j];
                    if (i == j || b2 == null) continue;
                    if (Math.abs(b2.maxY - b1.maxY) < dy1 && Math.abs(b2.minY - b1.minY) < dy1
                            && Math.abs(b2.maxX - b1.maxX) < dx1 && Math.abs(b2.minX - b1.minX) < dx1
                            && Math.abs(b2.minZ - b1.maxZ) < dz1)
                    {
                        b1 = b1.union(b2);
                        boxes[i] = b1;
                        boxes[j] = null;
                    }
                }
            }
            for (int i = 0; i < boxes.length; i++)
            {
                b1 = boxes[i];
                if (b1 == null) continue;
                for (int j = 0; j < boxes.length; j++)
                {
                    b2 = boxes[j];
                    if (i == j || b2 == null) continue;
                    if (Math.abs(b2.maxY - b1.maxY) < dy1 && Math.abs(b2.minY - b1.minY) < dy1
                            && Math.abs(b2.maxZ - b1.maxZ) < dz1 && Math.abs(b2.minZ - b1.minZ) < dz1
                            && Math.abs(b2.minX - b1.maxX) < dx1)
                    {
                        b1 = b1.union(b2);
                        boxes[i] = b1;
                        boxes[j] = null;
                    }
                }
            }
            for (int i = 0; i < boxes.length; i++)
            {
                b1 = boxes[i];
                if (b1 == null) continue;
                for (int j = 0; j < boxes.length; j++)
                {
                    b2 = boxes[j];
                    if (i == j || b2 == null) continue;
                    if (Math.abs(b2.maxX - b1.maxX) < dx1 && Math.abs(b2.minX - b1.minX) < dx1
                            && Math.abs(b2.maxZ - b1.maxZ) < dz1 && Math.abs(b2.minZ - b1.minZ) < dz1
                            && Math.abs(b2.minY - b1.maxY) < dy1)
                    {
                        b1 = b1.union(b2);
                        boxes[i] = b1;
                        boxes[j] = null;
                    }
                }
            }

            for (int i = 0; i < boxes.length; i++)
            {
                b1 = boxes[i];
                if (b1 == null) continue;
                for (int j = 0; j < boxes.length; j++)
                {
                    b2 = boxes[j];
                    if (i == j || b2 == null) continue;
                    // Check if subbox after previous passes, if so, combine.
                    if (b2.maxX <= b1.maxX && b2.maxY <= b1.maxY && b2.maxZ <= b1.maxZ && b2.minX >= b1.minX
                            && b2.minY >= b1.minY && b2.minZ >= b1.minZ)
                    {
                        boxes[i] = b1;
                        boxes[j] = null;
                    }
                }
            }
            for (AxisAlignedBB b : boxes)
            {
                if (b != null)
                {
                    blockBoxes.add(b);
                }
            }
        }
        // Finished merging the boxes.

        double yTop = Math.min(entity.stepHeight + entity.posY + theEntity.motionY, maxY);

        boolean floor = false;
        boolean ceiling = false;
        double yMaxFloor = minY;

        // for each box, compute collision.
        for (AxisAlignedBB aabb : blockBoxes)
        {
            dx = 10e3;
            dz = 10e3;
            boolean thisFloor = false;
            boolean thisCieling = false;
            boolean collidesX = ((maxZ <= aabb.maxZ) && (maxZ >= aabb.minZ))
                    || ((minZ <= aabb.maxZ) && (minZ >= aabb.minZ)) || ((minZ <= aabb.minZ) && (maxZ >= aabb.maxZ));

            boolean collidesY = ((minY >= aabb.minY) && (minY <= aabb.maxY))
                    || ((maxY <= aabb.maxY) && (maxY >= aabb.minY)) || ((minY <= aabb.minY) && (maxY >= aabb.maxY));

            boolean collidesZ = ((maxX <= aabb.maxX) && (maxX >= aabb.minX))
                    || ((minX <= aabb.maxX) && (minX >= aabb.minX)) || ((minX <= aabb.minX) && (maxX >= aabb.maxX));

            collidesZ = collidesZ && (collidesX || collidesY);
            collidesX = collidesX && (collidesZ || collidesY);

            if (collidesX && collidesZ && yTop >= aabb.maxY
                    && boundingBox.minY - entity.stepHeight - theEntity.motionY <= aabb.maxY - diffs.y)
            {
                if (!floor)
                {
                    temp1.y = (float) Math.max(aabb.maxY - boundingBox.minY, temp1.y);
                }
                floor = true;
                thisFloor = aabb.maxY >= yMaxFloor;
                if (thisFloor) yMaxFloor = aabb.maxY;
            }
            if (collidesX && collidesZ && boundingBox.maxY >= aabb.minY && boundingBox.minY < aabb.minY)
            {
                if (!(floor || ceiling))
                {
                    double dy = aabb.minY - boundingBox.maxY - diffs.y;
                    temp1.y = (float) Math.min(dy, temp1.y);
                }
                thisCieling = !(thisFloor || floor);
                ceiling = true;
            }

            boolean vert = thisFloor || thisCieling;

            if (collidesX && !vert && collidesY && boundingBox.maxX >= aabb.maxX && boundingBox.minX <= aabb.maxX)
            {
                r = Math.max(aabb.maxX - boundingBox.minX, temp1.x);
                dx = Math.min(dx, r);
            }
            if (collidesX && !vert && collidesY && boundingBox.maxX >= aabb.minX && boundingBox.minX < aabb.minX)
            {
                r = Math.min(aabb.minX - boundingBox.maxX, temp1.x);
                dx = Math.min(dx, r);
            }
            if (collidesZ && !vert && collidesY && boundingBox.maxZ >= aabb.maxZ && boundingBox.minZ <= aabb.maxZ)
            {
                r = Math.max(aabb.maxZ - boundingBox.minZ, temp1.z);
                dz = Math.min(dz, r);
            }
            if (collidesZ && !vert && collidesY && boundingBox.maxZ >= aabb.minZ && boundingBox.minZ < aabb.minZ)
            {
                r = Math.min(aabb.minZ - boundingBox.maxZ, temp1.z);
                dz = Math.min(dz, r);
            }
            if (Math.abs(dx) > Math.abs(dz) && dx < 10e2 || dx == 10e3 && dz < 10e2)
            {
                temp1.z = (float) dz;
            }
            else if (dx < 10e2)
            {
                temp1.x = (float) dx;
            }
        }

        // Figure out top most collsion postion, if it collides, then set
        // vertical motion to 0.
        for (Double d : topY)
            if (entity.posY >= d && entity.posY + entity.motionY <= d && theEntity.motionY <= 0)
            {
                double diff = (entity.posY + entity.motionY) - (d + theEntity.motionY);
                double check = Math.max(0.5, Math.abs(entity.motionY + theEntity.motionY));
                if (diff > 0 || diff < -0.5 || Math.abs(diff) > check)
                {
                    entity.motionY = 0;
                }
            }

        // If entity has collided, adjust motion accordingly.
        boolean collidedY = false;
        if (temp1.lengthSquared() > 0)
        {
            if (temp1.y >= 0)
            {
                entity.onGround = true;
                entity.fallDistance = 0;
                entity.fall(entity.fallDistance, 0);
            }
            else if (temp1.y < 0)
            {
                boolean below = entity.posY + entity.height - (entity.motionY + theEntity.motionY) < theEntity.posY;
                if (below)
                {
                    temp1.y = 0;
                }
            }
            if (temp1.x != 0) entity.motionX = theEntity.motionX;
            if (temp1.y != 0) entity.motionY = theEntity.motionY;
            if (temp1.z != 0) entity.motionZ = theEntity.motionZ;
            if (temp1.y != 0) collidedY = true;
            temp1.add(new Vector3f((float) entity.posX, (float) entity.posY, (float) entity.posZ));
            entity.setPosition(temp1.x, temp1.y, temp1.z);
        }
        // Extra stuff to do with players.
        if (entity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) entity;

            if (player.worldObj.isRemote)
            {
                // This fixes jitter, need a better way to handle this.
                Minecraft.getMinecraft().gameSettings.viewBobbing = false;
            }

            if (Math.abs(player.motionY) < 0.1 && !player.capabilities.isFlying)
            {
                entity.onGround = true;
                entity.fallDistance = 0;
            }
            // Meed to set floatingTickCount to prevent being kicked for flying.
            if (!player.capabilities.isCreativeMode && !player.worldObj.isRemote)
            {
                EntityPlayerMP entityplayer = (EntityPlayerMP) player;
                if (collidedY) entityplayer.connection.floatingTickCount = 0;
            }
        }
    }
}
