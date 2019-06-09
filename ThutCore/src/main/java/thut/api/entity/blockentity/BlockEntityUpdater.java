package thut.api.entity.blockentity;

import java.util.List;
import java.util.Set;

import javax.vecmath.Vector3f;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import thut.api.TickHandler;
import thut.api.maths.Matrix3;

public class BlockEntityUpdater
{
    public static boolean autoBlacklist = false;

    public static boolean isWhitelisted(TileEntity tile)
    {
        ResourceLocation id = TileEntityType.getId(tile.getType());
        return id == null ? true : !IBlockEntity.TEBLACKLIST.contains(id.toString());
    }

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
        theEntity.setBoundingBox(new AxisAlignedBB(xMin, yMin, zMin, xMax, yMax, zMax));
    }

    public void onUpdate()
    {
        if (blockEntity.getBlocks() == null) return;
        // TODO see if this is needed.
        // if (World.MAX_ENTITY_RADIUS < blockEntity.getBlocks().length)
        // World.MAX_ENTITY_RADIUS = blockEntity.getBlocks().length;
        // if (World.MAX_ENTITY_RADIUS < blockEntity.getBlocks()[0].length)
        // World.MAX_ENTITY_RADIUS = blockEntity.getBlocks()[0].length;
        // if (World.MAX_ENTITY_RADIUS < blockEntity.getBlocks()[0][0].length)
        // World.MAX_ENTITY_RADIUS = blockEntity.getBlocks()[0][0].length;
        EntitySize size = theEntity.getSize(theEntity.getPose());
        size = EntitySize.fixed(1 + blockEntity.getMax().getX() - blockEntity.getMin().getX(),
                blockEntity.getMax().getY());
        blockEntity.setSize(size);
        if (theEntity.getMotion().y == 0)
        {
            theEntity.setPosition(theEntity.posX, Math.round(theEntity.posY), theEntity.posZ);
        }
        blockEntity.getFakeWorld().getWorldInfo().setGameTime(theEntity.getEntityWorld().getGameTime());
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
                    TileEntity tile = blockEntity.getTiles()[i][j][k];
                    if (tile != null)
                    {
                        tile.setPos(pos.toImmutable());
                        tile.setWorld(blockEntity.getFakeWorld());
                    }
                    if (tile instanceof ITickable)
                    {
                        if (erroredSet.contains(tile) || !isWhitelisted(tile)) continue;
                        try
                        {
                            ((ITickable) tile).tick();
                        }
                        catch (Throwable e)
                        {
                            e.printStackTrace();
                            System.err.println("Error with Tile Entity " + tile);
                            erroredSet.add(tile);
                            if (autoBlacklist && TileEntityType.getId(tile.getType()) != null)
                            {
                                IBlockEntity.TEBLACKLIST.add(TileEntityType.getId(tile.getType()).toString());
                            }
                        }
                    }
                }
    }

    public void applyEntityCollision(Entity entity)
    {
        // TODO instead of this, apply appropriate transformation to the
        // entity's box, and then collide off that, then apply appropriate
        // inverse transformation before actually applying collision to entity.
        if ((theEntity.rotationYaw + 360) % 90 > 5 || theEntity.isPassenger(entity)) return;

        blockBoxes.clear();
        int sizeX = blockEntity.getBlocks().length;
        int sizeY = blockEntity.getBlocks()[0].length;
        int sizeZ = blockEntity.getBlocks()[0][0].length;
        Set<Double> topY = Sets.newHashSet();
        MutableBlockPos pos = new MutableBlockPos();
        int xMin = blockEntity.getMin().getX();
        int yMin = blockEntity.getMin().getY();
        int zMin = blockEntity.getMin().getZ();
        BlockPos origin = theEntity.getPosition();

        double minX = entity.getBoundingBox().minX;
        double minY = entity.getBoundingBox().minY;
        double minZ = entity.getBoundingBox().minZ;
        double maxX = entity.getBoundingBox().maxX;
        double maxY = entity.getBoundingBox().maxY;
        double maxZ = entity.getBoundingBox().maxZ;
        double dx, dz, dy, r;
        Vec3d motion_a = theEntity.getMotion();
        Vec3d motion_b = entity.getMotion();
        Vector3f diffs = new Vector3f((float) (motion_a.x - motion_b.x), (float) (motion_a.y - motion_b.y),
                (float) (motion_a.z - motion_b.z));
        World fakeworld = blockEntity.getFakeWorld();
        AxisAlignedBB boundingBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
        /** Expanded box by velocities to test for collision with. */
        AxisAlignedBB testBox = boundingBox.expand(-diffs.x, -diffs.y, -diffs.z);
        for (int i = 0; i < sizeX; i++)
            for (int j = 0; j < sizeY; j++)
                for (int k = 0; k < sizeZ; k++)
                {
                    pos.setPos(i + xMin + origin.getX(), j + yMin + origin.getY(), k + zMin + origin.getZ());
                    BlockState state = fakeworld.getBlockState(pos);
                    state = state.getExtendedState(fakeworld, pos);
                    VoxelShape shape = state.getCollisionShape(fakeworld, pos);
                    List<AxisAlignedBB> toAdd = shape.toBoundingBoxList();
                    for (AxisAlignedBB blockBox : toAdd)
                    {
                        if (blockBox != null)
                        {
                            float dx2 = (float) (theEntity.posX - origin.getX()) - 0.5f;
                            float dy2 = (float) (theEntity.posY - origin.getY());
                            float dz2 = (float) (theEntity.posZ - origin.getZ()) - 0.5f;
                            AxisAlignedBB box = blockBox.offset(dx2, dy2, dz2);
                            if (box.intersects(testBox))
                            {
                                blockBoxes.add(box);
                                topY.add(box.maxY);
                            }
                        }
                    }
                }
        // No boxes, no need to process further.
        if (blockBoxes.isEmpty()) return;

        pos.setPos(theEntity.getPosition());
        Vector3f temp1 = new Vector3f();

        boolean merge = true;

        // Here we merge the boxes into less boxes, by taking any boxes with
        // shared faces and merging them.
        if (merge)
        {
            Matrix3.mergeAABBs(blockBoxes, 0, 0, 0);
        }
        // Finished merging the boxes.

        /** Positions adjusted for velocity. */
        double lastTickMinY = minY + diffs.y;
        double nextTickMinY = minY - diffs.y;

        double lastTickMaxY = maxY + diffs.y;
        double nextTickMaxY = maxY - diffs.y;

        double lastTickMinX = minX + diffs.x;
        double nextTickMinX = minX - diffs.x;

        double lastTickMaxX = maxX + diffs.x;
        double nextTickMaxX = maxX - diffs.x;

        double lastTickMinZ = minZ + diffs.z;
        double nextTickMinZ = minZ - diffs.z;

        double lastTickMaxZ = maxZ + diffs.z;
        double nextTickMaxZ = maxZ - diffs.z;

        // for each box, compute collision.
        for (AxisAlignedBB aabb : blockBoxes)
        {
            dx = 10e3;
            dz = 10e3;
            dy = 10e3;

            boolean fromAbove = lastTickMinY >= aabb.maxY && nextTickMinY <= aabb.maxY;
            boolean fromBelow = nextTickMaxY >= aabb.minY && lastTickMaxY <= aabb.minY;
            boolean yPos = minY <= aabb.maxY && minY >= aabb.minY;
            boolean yNeg = maxY <= aabb.maxY && maxY >= aabb.minY;

            boolean fromXPos = lastTickMinX >= aabb.maxX && nextTickMinX <= aabb.maxX;
            boolean fromXNeg = nextTickMaxX >= aabb.minX && lastTickMaxX <= aabb.minX;
            boolean xPos = minX <= aabb.maxX && minX >= aabb.minX;
            boolean xNeg = maxX <= aabb.maxX && maxX >= aabb.minX;

            boolean fromZPos = lastTickMinZ >= aabb.maxZ && nextTickMinZ <= aabb.maxZ;
            boolean fromZNeg = nextTickMaxZ >= aabb.minZ && lastTickMaxZ <= aabb.minZ;
            boolean zPos = minZ <= aabb.maxZ && minZ >= aabb.minZ;
            boolean zNeg = maxZ <= aabb.maxZ && maxZ >= aabb.minZ;

            boolean collidesXPos = xPos && zPos && zNeg;
            boolean collidesXNeg = xNeg && zPos && zNeg;

            boolean collidesZPos = zPos && xPos && xNeg;
            boolean collidesZNeg = zNeg && xPos && xNeg;

            boolean collidesYNeg = (yNeg && (xPos || xNeg || zPos || zNeg));
            boolean collidesYPos = (yPos && (xPos || xNeg || zPos || zNeg));

            boolean collided = false;
            /** Collides with top of box, is standing on it. */
            if (!collided && fromAbove)
            {
                temp1.y = (float) Math.max(aabb.maxY - diffs.y - nextTickMinY, temp1.y);
                collided = true;
            }
            /** Collides with bottom of box, is under it. */
            if (!collided && fromBelow)
            {
                temp1.y = (float) Math.min(aabb.minY - diffs.y - nextTickMaxY, temp1.y);
                collided = true;
            }

            /** Collides with middle of +x face. */
            if (!collided && (fromXPos || collidesXPos))
            {
                r = Math.max(aabb.maxX - boundingBox.minX, temp1.x);
                dx = Math.min(dx, r);
                collided = true;
            }
            /** Collides with middle of -x face. */
            if (!collided && (fromXNeg || collidesXNeg))
            {
                r = Math.min(aabb.minX - boundingBox.maxX, temp1.x);
                dx = Math.min(dx, r);
                collided = true;
            }
            /** Collides with middle of +z face. */
            if (!collided && (fromZPos || collidesZPos))
            {
                r = Math.max(aabb.maxZ - boundingBox.minZ, temp1.z);
                dz = Math.min(dz, r);
                collided = true;
            }
            /** Collides with middle of -z face. */
            if (!collided && (fromZNeg || collidesZNeg))
            {
                r = Math.min(aabb.minZ - boundingBox.maxZ, temp1.z);
                dz = Math.min(dz, r);
                collided = true;
            }
            /** Collides with +x, +z corner. */
            if (!collided && xPos && zPos)
            {
                r = Math.max(aabb.maxZ - boundingBox.minZ, temp1.z);
                dz = Math.min(dz, r);
                r = Math.max(aabb.maxX - boundingBox.minX, temp1.x);
                dx = Math.min(dx, r);
                collided = true;
            }
            /** Collides with +x, -z corner. */
            if (!collided && xPos && zNeg)
            {
                r = Math.min(aabb.minZ - boundingBox.maxZ, temp1.z);
                dz = Math.min(dz, r);
                r = Math.max(aabb.maxX - boundingBox.minX, temp1.x);
                dx = Math.min(dx, r);
                collided = true;
            }
            /** Collides with -x, -z corner. */
            if (!collided && xNeg && zNeg)
            {
                r = Math.min(aabb.minZ - boundingBox.maxZ, temp1.z);
                dz = Math.min(dz, r);
                r = Math.min(aabb.minX - boundingBox.maxX, temp1.x);
                dx = Math.min(dx, r);
                collided = true;
            }
            /** Collides with -x, +z corner. */
            if (!collided && xNeg && zPos)
            {
                r = Math.max(aabb.maxZ - boundingBox.minZ, temp1.z);
                dz = Math.min(dz, r);
                r = Math.min(aabb.minX - boundingBox.maxX, temp1.x);
                dx = Math.min(dx, r);
                collided = true;
            }

            if (collidesYNeg)
            {
                r = (float) Math.min(aabb.minY - diffs.y - nextTickMaxY, temp1.y);
                dy = Math.min(r, dy);
            }
            else if (collidesYPos)
            {
                r = (float) Math.max(aabb.maxY - diffs.y - nextTickMinY, temp1.y);
                dy = Math.min(r, dy);
            }

            double dy1 = Math.abs(dy);
            double dz1 = Math.abs(dz);
            double dx1 = Math.abs(dx);

            /** y minimum penetration. */
            if (dy1 < dx1 && dy1 < dz1)
            {
                temp1.y = (float) dy;
            }
            /** x minimum penetration. */
            else if (dx1 < dy1 && dx1 < dz1)
            {
                temp1.x = (float) dx;
            }
            else if (dz < 10e2)
            {
                temp1.z = (float) dz;
            }

        }
        // Extra stuff to do with players.
        if (entity instanceof PlayerEntity)
        {
            PlayerEntity player = (PlayerEntity) entity;

            if (player.getEntityWorld().isRemote)
            {
                // This fixes jitter, need a better way to handle this.
                if (Minecraft.getInstance().gameSettings.viewBobbing
                        || TickHandler.playerTickTracker.containsKey(player.getUniqueID()))
                {
                    TickHandler.playerTickTracker.put(player.getUniqueID(), (int) (System.currentTimeMillis() % 2000));
                    Minecraft.getInstance().gameSettings.viewBobbing = false;
                }
            }
            /** This is for clearing jump values on client. */
            if (player.getEntityWorld().isRemote) player.getEntityData().putInt("lastStandTick", player.ticksExisted);
            if (!player.playerAbilities.isFlying)
            {
                entity.onGround = true;
                entity.fall(entity.fallDistance, 0);
                entity.fallDistance = 0;
            }
            // Meed to set floatingTickCount to prevent being kicked for flying.
            if (!player.playerAbilities.isCreativeMode && !player.getEntityWorld().isRemote)
            {
                ServerPlayerEntity PlayerEntity = (ServerPlayerEntity) player;
                // TODO fix access transformer
                // PlayerEntity.connection.floatingTickCount = 0;
            }
        }

        // If entity has collided, adjust motion accordingly.
        if (temp1.lengthSquared() > 0)
        {
            if (temp1.y >= 0)
            {
                entity.onGround = true;
                entity.fall(entity.fallDistance, 0);
                entity.fallDistance = 0;
            }
            Vec3d motion = new Vec3d(temp1.x, temp1.y, temp1.z);
            if (!(entity instanceof ServerPlayerEntity)) entity.move(MoverType.SELF, motion);

            dx = motion_b.x;
            dy = motion_b.y;
            dz = motion_b.z;

            if (temp1.x != 0) dx = motion_a.x;
            if (temp1.y != 0) dx = motion_a.y;
            if (temp1.z != 0) dx = motion_a.z;

            entity.setMotion(dx, dy, dz);

            // Attempt to also set previous positions to prevent desync like
            // issues on servers.
            entity.prevPosX = entity.posX;
            entity.prevPosY = entity.posY;
            entity.prevPosZ = entity.posZ;
            entity.prevRotationPitch = entity.rotationPitch;
            entity.prevRotationYaw = entity.rotationYaw;
        }
    }
}
