package thut.api.entity.blockentity;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import thut.api.TickHandler;

public class BlockEntityUpdater
{
    public static boolean autoBlacklist = false;

    public static boolean isWhitelisted(final TileEntity tile)
    {
        final ResourceLocation id = TileEntityType.getId(tile.getType());
        return id == null ? true : !IBlockEntity.TEBLACKLIST.contains(id.toString());
    }

    final IBlockEntity  blockEntity;
    final Entity        theEntity;
    List<AxisAlignedBB> blockBoxes = Lists.newArrayList();
    Set<TileEntity>     erroredSet = Sets.newHashSet();
    VoxelShape          totalShape = VoxelShapes.empty();

    public BlockEntityUpdater(final IBlockEntity rocket)
    {
        this.blockEntity = rocket;
        this.theEntity = (Entity) rocket;
    }

    public VoxelShape buildShape()
    {
        final int sizeX = this.blockEntity.getBlocks().length;
        final int sizeY = this.blockEntity.getBlocks()[0].length;
        final int sizeZ = this.blockEntity.getBlocks()[0][0].length;
        final Entity mob = (Entity) this.blockEntity;
        this.totalShape = VoxelShapes.empty();
        final BlockPos.Mutable pos = new BlockPos.Mutable();
        final BlockPos min = this.blockEntity.getMin();
        final BlockPos origin = mob.getPosition();
        final IBlockReader world = this.blockEntity.getFakeWorld();

        final int xMin = MathHelper.floor(this.blockEntity.getMin().getX());
        final int xMax = MathHelper.floor(this.blockEntity.getMax().getX());
        final int zMin = MathHelper.floor(this.blockEntity.getMin().getZ());
        final int zMax = MathHelper.floor(this.blockEntity.getMax().getZ());

        final double dx = (xMax - xMin) / 2 + 0.5;
        final double dz = (zMax - zMin) / 2 + 0.5;

        for (int i = 0; i < sizeX; i++)
            for (int j = 0; j < sizeY; j++)
                for (int k = 0; k < sizeZ; k++)
                {
                    pos.setPos(i, j, k);
                    final BlockState state = this.blockEntity.getBlocks()[i][j][k];
                    pos.setPos(i + min.getX() + origin.getX(), j + min.getY() + origin.getY(), k + min.getZ() + origin
                            .getZ());
                    VoxelShape shape;
                    if (state == null || (shape = state.getShape(world, pos)) == null) continue;
                    if (shape.isEmpty()) continue;
                    shape = shape.withOffset(mob.getPosX() + i - dx, mob.getPosY() + j + min.getY(), mob.getPosZ() + k
                            - dz);
                    this.totalShape = VoxelShapes.combineAndSimplify(this.totalShape, shape, IBooleanFunction.OR);
                }
        return this.totalShape;
    }

    public void applyEntityCollision(final Entity entity)
    {
        // TODO instead of this, apply appropriate transformation to the
        // entity's box, and then collide off that, then apply appropriate
        // inverse transformation before actually applying collision to entity.
        if ((this.theEntity.rotationYaw + 360) % 90 > 5 || this.theEntity.isPassenger(entity)) return;

        boolean serverSide = entity.getEntityWorld().isRemote;
        final boolean isPlayer = entity instanceof PlayerEntity;
        if (isPlayer) serverSide = entity instanceof ServerPlayerEntity;

        // Players are funny, and need to be specifically run on the client,
        // everything else should be server side!
        if (!isPlayer && !serverSide) return;

        final double minX = entity.getBoundingBox().minX;
        final double minY = entity.getBoundingBox().minY;
        final double minZ = entity.getBoundingBox().minZ;
        final double maxX = entity.getBoundingBox().maxX;
        final double maxY = entity.getBoundingBox().maxY;
        final double maxZ = entity.getBoundingBox().maxZ;
        double dx = 0, dz = 0, dy = 0;
        final Vec3d motion_a = this.theEntity.getMotion();
        Vec3d motion_b = entity.getMotion();
        final AxisAlignedBB boundingBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);

        if (isPlayer && serverSide)
        {
            final ServerPlayerEntity player = (ServerPlayerEntity) entity;
            dx = player.chasingPosX - player.prevChasingPosX;
            dy = player.chasingPosY - player.prevChasingPosY;
            dz = player.chasingPosZ - player.prevChasingPosZ;
            motion_b = new Vec3d(dx, dy, dz).scale(0.5);
        }
        final Vec3d totalV = motion_a.add(motion_b);
        final Vec3d diffV = motion_b.subtract(motion_a);

        /** Expanded box by velocities to test for collision with. */

        final AxisAlignedBB testBox = boundingBox.expand(totalV.x, totalV.y + dy, totalV.z);

        this.blockBoxes.clear();
        this.buildShape().forEachBox((x0, y0, z0, x1, y1, z1) ->
        {
            final AxisAlignedBB box = new AxisAlignedBB(x0, y0, z0, x1, y1, z1);
            if (box.intersects(testBox)) this.blockBoxes.add(box);
        });

        boolean colX = false;
        boolean colY = false;
        boolean colZ = false;

        dx = 0;
        dy = 0;
        dz = 0;

        boolean min = false;
        boolean max = false;
        boolean step = false;
        for (final AxisAlignedBB aabb : this.blockBoxes)
        {
            double dx1 = 0, dy1 = 0, dz1 = 0;
            // We compare to testBox as it accounts for velocity
            final AxisAlignedBB inter = testBox.intersect(aabb);

            boolean hitXn, hitYn, hitZn;
            boolean hitXp, hitYp, hitZp;
            hitYp = inter.maxY == aabb.maxY;
            hitYn = inter.minY == aabb.minY;

            hitXp = inter.maxX == aabb.maxX;
            hitXn = inter.minX == aabb.minX;

            hitZp = inter.maxZ == aabb.maxZ;
            hitZn = inter.minZ == aabb.minZ;

            boolean hx = hitXp || hitXn;
            boolean hy = hitYp || hitYn;
            boolean hz = hitZp || hitZn;

            // Check if should step, if so, call false on this.
            if (hitYp && (hx || hz))
            {
                dy1 = aabb.maxY - boundingBox.minY;
                if (dy1 < entity.stepHeight)
                {
                    hitXp = false;
                    hitXn = false;

                    hitZp = false;
                    hitZn = false;
                    step = true;
                    dy = dy1;
                }
                else dy1 = 0;
            }
            hx = hitXp || hitXn;
            hy = hitYp || hitYn || step;
            hz = hitZp || hitZn;

            min = hitYn;
            max = hitYp;

            check:
            if (min || max && !(min && max))
            {
                // we already checked this for step!
                if (step) break check;
                if (min && diffV.y < 0) break check;
                if (max && diffV.y > 0) break check;
                dy1 = max ? aabb.maxY - boundingBox.minY : aabb.minY - boundingBox.maxY;
            }
            min = hitXn;
            max = hitXp;
            check:
            if (min || max && !(min && max))
            {
                if (min && diffV.x < 0) break check;
                if (max && diffV.x > 0) break check;
                dx1 = max ? aabb.maxX - boundingBox.minX : aabb.minX - boundingBox.maxX;
            }
            min = hitZn;
            max = hitZp;
            check:
            if (min || max && !(min && max))
            {
                if (min && diffV.z < 0) break check;
                if (max && diffV.z > 0) break check;
                dz1 = max ? aabb.maxZ - boundingBox.minZ : aabb.minZ - boundingBox.maxZ;
            }
            final double x = Math.abs(dx1);
            final double y = Math.abs(dy1);
            final double z = Math.abs(dz1);

            final boolean toX = hx && !(x < y && hy || x < z && hz);
            final boolean toY = hy && !(y < x && hx || y < z && hz);
            final boolean toZ = hz && !(z < y && hy || z < x && hx);

            if (toY)
            {
                colY = true;
                dy = Math.abs(dy) > Math.abs(dy1) ? dy : dy1;
            }
            else if (toX)
            {
                colX = true;
                dx = Math.abs(dx) > Math.abs(dx1) ? dx : dx1;
            }
            else if (toZ)
            {
                colZ = true;
                dz = Math.abs(dz) > Math.abs(dz1) ? dz : dz1;
            }
        }

        // If entity has collided, adjust motion accordingly.
        if (colX || colY || colZ)
        {
            motion_b = entity.getMotion();
            if (colY)
            {
                final Vec3d motion = new Vec3d(0, dy, 0);
                entity.move(MoverType.SELF, motion);
                dy = motion_a.y;
            }
            else dy = motion_b.y;
            if (colX)
            {
                final Vec3d motion = new Vec3d(dx, 0, 0);
                entity.move(MoverType.SELF, motion);
                dx = motion_a.x;
            }
            else dx = 0.9 * motion_b.x;
            if (colZ)
            {
                final Vec3d motion = new Vec3d(0, 0, dz);
                entity.move(MoverType.SELF, motion);
                dz = motion_a.z;
            }
            else dz = 0.9 * motion_b.z;

            entity.setMotion(dx, dy, dz);

            if (colY)
            {
                entity.onGround = true;
                entity.onLivingFall(entity.fallDistance, 0);
                entity.fallDistance = 0;
            }

            // Extra stuff to do with players.
            if (isPlayer)
            {
                final PlayerEntity player = (PlayerEntity) entity;

                if (!serverSide && (Minecraft.getInstance().gameSettings.viewBobbing || TickHandler.playerTickTracker
                        .containsKey(player.getUniqueID())))
                { // This fixes jitter, need a better way to handle this.
                    TickHandler.playerTickTracker.put(player.getUniqueID(), (int) (System.currentTimeMillis() % 2000));
                    Minecraft.getInstance().gameSettings.viewBobbing = false;
                }
                /** This is for clearing jump values on client. */
                if (!serverSide) player.getPersistentData().putInt("lastStandTick", player.ticksExisted);
                // Meed to set floatingTickCount to prevent being kicked for
                // flying.
                if (!player.abilities.isCreativeMode && serverSide)
                {
                    final ServerPlayerEntity serverplayer = (ServerPlayerEntity) player;
                    serverplayer.connection.floatingTickCount = 0;
                }
            }

        }
    }

    public void onSetPosition()
    {
        double xMin, yMin, zMin, xMax, yMax, zMax;
        xMin = this.theEntity.posX + this.blockEntity.getMin().getX() - 0.5;
        yMin = this.theEntity.posY + this.blockEntity.getMin().getY();
        zMin = this.theEntity.posZ + this.blockEntity.getMin().getZ() - 0.5;
        xMax = this.theEntity.posX + this.blockEntity.getMax().getX() + 0.5;
        yMax = this.theEntity.posY + this.blockEntity.getMax().getY() + 1;
        zMax = this.theEntity.posZ + this.blockEntity.getMax().getZ() + 0.5;
        this.theEntity.setBoundingBox(new AxisAlignedBB(xMin, yMin, zMin, xMax, yMax, zMax));
    }

    public void onUpdate()
    {
        if (this.blockEntity.getBlocks() == null) return;
        final double wMax = this.theEntity.getEntityWorld().getMaxEntityRadius();
        double uMax = -1;
        if (wMax < this.blockEntity.getBlocks().length) uMax = this.blockEntity.getBlocks().length;
        if (wMax < this.blockEntity.getBlocks()[0].length) uMax = this.blockEntity.getBlocks()[0].length;
        if (wMax < this.blockEntity.getBlocks()[0][0].length) uMax = this.blockEntity.getBlocks()[0][0].length;
        this.theEntity.getEntityWorld().increaseMaxEntityRadius(uMax);
        EntitySize size = this.theEntity.getSize(this.theEntity.getPose());
        size = EntitySize.fixed(1 + this.blockEntity.getMax().getX() - this.blockEntity.getMin().getX(),
                this.blockEntity.getMax().getY());
        this.blockEntity.setSize(size);
        double y;
        if (this.theEntity.getMotion().y == 0 && this.theEntity.getPosY() != (y = Math.round(this.theEntity.getPosY())))
            this.theEntity.setPosition(this.theEntity.getPosX(), y, this.theEntity.getPosZ());
        final BlockPos.Mutable pos = new BlockPos.Mutable();
        final int xMin = this.blockEntity.getMin().getX();
        final int zMin = this.blockEntity.getMin().getZ();
        final int yMin = this.blockEntity.getMin().getY();
        final int sizeX = this.blockEntity.getTiles().length;
        final int sizeY = this.blockEntity.getTiles()[0].length;
        final int sizeZ = this.blockEntity.getTiles()[0][0].length;

        final World world = this.blockEntity.getFakeWorld() instanceof World ? (World) this.blockEntity.getFakeWorld()
                : this.theEntity.getEntityWorld();

        for (int i = 0; i < sizeX; i++)
            for (int j = 0; j < sizeY; j++)
                for (int k = 0; k < sizeZ; k++)
                {
                    pos.setPos(i + xMin + this.theEntity.getPosX(), j + yMin + this.theEntity.getPosY(), k + zMin
                            + this.theEntity.getPosZ());

                    // TODO rotate here by entity rotation.
                    final TileEntity tile = this.blockEntity.getTiles()[i][j][k];
                    if (tile != null) tile.setWorldAndPos(world, pos.toImmutable());
                    if (tile instanceof ITickableTileEntity)
                    {
                        if (this.erroredSet.contains(tile) || !BlockEntityUpdater.isWhitelisted(tile)) continue;
                        try
                        {
                            ((ITickableTileEntity) tile).tick();
                        }
                        catch (final Throwable e)
                        {
                            e.printStackTrace();
                            System.err.println("Error with Tile Entity " + tile);
                            this.erroredSet.add(tile);
                            if (BlockEntityUpdater.autoBlacklist && TileEntityType.getId(tile.getType()) != null)
                                IBlockEntity.TEBLACKLIST.add(TileEntityType.getId(tile.getType()).toString());
                        }
                    }
                }
    }
}
