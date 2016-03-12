package thut.api.pathing;

import static net.minecraft.util.EnumFacing.DOWN;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.NORTH;
import static net.minecraft.util.EnumFacing.SOUTH;
import static net.minecraft.util.EnumFacing.UP;
import static net.minecraft.util.EnumFacing.WEST;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import thut.api.maths.Matrix3;
import thut.api.maths.Vector3;

/** between the sub and main path creation.
 * 
 * @author Patrick */
public class ThutPathFinder extends PathFinder implements IPathFinder
{
    static double max   = 0;
    static int    count = 0;
    static double mean  = 0;
    public static Vector3 getOpposite(EnumFacing side, Vector3 ret)
    {
        switch (side)
        {
        case UP:
            return ret.set(DOWN);
        case DOWN:
            return ret.set(UP);
        case EAST:
            return ret.set(WEST);
        case WEST:
            return ret.set(EAST);
        case NORTH:
            return ret.set(SOUTH);
        case SOUTH:
            return ret.set(NORTH);
        default:
            return ret.set(side);
        }
    }
    /** Used to find obstacles */
    protected final IBlockAccess          worldMap;
    /** The path being generated */
    /** The path being generated forward */
    protected final ThutPath              pathf        = new ThutPath();
    /** The path being generated backwards */
    protected final ThutPath              pathb        = new ThutPath();
    /** The points in the path */
    protected final IntHashMap<PathPoint> pointMap     = new IntHashMap<PathPoint>();
    /** Selection of path points to add to the path */
    protected final PathPoint[]           pathOptionsf = new PathPoint[64];

    /** Selection of path points to add to the path */
    protected final PathPoint[]           pathOptionsb = new PathPoint[64];
    protected final IPathingMob           mob;

    protected int                         PATHTIME     = 5000000;

    protected final Matrix3               box          = new Matrix3();

    Vector3 v0 = Vector3.getNewVector();

    Vector3 v1 = Vector3.getNewVector();

    public ThutPathFinder(IBlockAccess world, IPathingMob entity)
    {
        super(null);
        this.worldMap = world;
        mob = entity;
        // pokemob = poke;
        // entry = poke.getPokedexEntry();
    }

    /** Adds a path from start to end and returns the whole path (args: unused,
     * start, end, unused, maxDistance) */
    protected PathEntity addToPath(Entity entity, PathPoint start, PathPoint end, float distance)
    {

        if (end.equals(start)) { return null; }
        start.totalPathDistance = 0.0F;
        start.distanceToNext = start.distanceToSquared(end);
        start.distanceToTarget = start.distanceToNext;
        pathf.clearPath();
        pathb.clearPath();
        pathf.addPoint(start);
        pathb.addPoint(end);

        long starttime = System.nanoTime();
        // IPokemob pokemob = (IPokemob) entity;
        PATHTIME = mob.getPathTime();
        // if(true) return null;
        Vector3 size = Vector3.getNewVector();
        size.set(mob.getMobSizes());
        PathPoint pathpoint3 = getSubPath(size, start, end, pathf, distance);
        // if(true) return null;

        double dt = ((System.nanoTime() - starttime) / 1000000d);

        max = Math.max(dt, max);
        count++;
        mean = (mean * (count - 1) + dt) / count;

        if (pathpoint3 == start || pathpoint3 == null) { return null; }

        if (pathpoint3 == start)
        {
            return null;
        }
        else
        {
            return this.createEntityPath(pathpoint3);
        }
    }

    private boolean canFit(Vector3 e, Block b)
    {
        double dz = b.getBlockBoundsMaxZ() - b.getBlockBoundsMinZ();
        double dx = b.getBlockBoundsMaxX() - b.getBlockBoundsMinX();
        double dy = b.getBlockBoundsMaxY() - b.getBlockBoundsMinY();
        if (dz < e.z || dx < e.x || dy < e.y) return false;
        return true;
    }
    /** Returns a new PathEntity for a given start and end point */
    private PathEntity createEntityPath(PathPoint end)
    {
        int i = 1;
        PathPoint pathpoint2 = end;

        for (pathpoint2 = end; pathpoint2.previous != null; pathpoint2 = pathpoint2.previous)
        {
            ++i;
        }

        PathPoint[] apathpoint = new PathPoint[i];
        pathpoint2 = end;
        --i;
        for (apathpoint[i] = end; pathpoint2.previous != null; apathpoint[i] = pathpoint2)
        {
            pathpoint2 = pathpoint2.previous;
            --i;
        }
        return new PathEntity(apathpoint);
    }
    /** Internal implementation of creating a path from an entity to a point */
    private PathEntity createEntityPathTo(double x, double y, double z, float distance)
    {
        Entity entity = (Entity) mob;
        this.pathb.clearPath();
        this.pathf.clearPath();
        this.pointMap.clearMap();
        int i = MathHelper.floor_double(entity.getEntityBoundingBox().minY + 0.5D);

        if (!mob.swims() && entity.isInWater())
        {
            i = (int) entity.getEntityBoundingBox().minY;
            // If it isn't supposed to be under water, attempt to adjust path to
            // top of water.
            for (Block block = this.worldMap
                    .getBlockState(
                            new BlockPos(MathHelper.floor_double(entity.posX), i, MathHelper.floor_double(entity.posZ)))
                    .getBlock(); block == Blocks.flowing_water
                            || block == Blocks.water; block = this.worldMap
                                    .getBlockState(new BlockPos(MathHelper.floor_double(entity.posX), i,
                                            MathHelper.floor_double(entity.posZ)))
                                    .getBlock())
            {
                ++i;
            }
        }
        // Attempt to path in air, TODO make this walk for short distances
        // instead.
        if (mob.flys())
        {
            y += 1;
        }
        else if (mob.floats())
        {
            y += mob.getFloatHeight();
        }
        else
        {

        }
        PathPoint start = this.openPoint(MathHelper.floor_double(entity.getEntityBoundingBox().minX), i,
                MathHelper.floor_double(entity.getEntityBoundingBox().minZ));
        PathPoint end = this.openPoint(MathHelper.floor_double(x - entity.width / 2.0F), MathHelper.floor_double(y),
                MathHelper.floor_double(z - entity.width / 2.0F));
        PathEntity pathentity = this.addToPath(entity, start, end, distance);

        return pathentity;
    }

    /** Creates a path from an entity to a specified location within a minimum
     * distance */
    public PathEntity createEntityPathTo(Entity entity, int x, int y, int z, float distance)
    {
        return this.createEntityPathTo(x + 0.5F, y + 0.5F, z + 0.5F, distance);
    }

    @Override
    /** Creates a path from an entity to a specified location within a minimum
     * distance */
    public PathEntity createEntityPathTo(IBlockAccess blockaccess, Entity entityIn, BlockPos targetPos, float dist)
    {
        return createEntityPathTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), dist);
    }

    @Override
    /** Creates a path from one entity to another within a minimum distance */
    public PathEntity createEntityPathTo(IBlockAccess world, Entity entity, Entity target, float distance)
    {
        return this.createEntityPathTo(target.posX, target.getEntityBoundingBox().minY + 0.5f, target.posZ, distance);
    }

    /** populates pathOptions with available points and returns the number of
     * options found (args: entity, currentPoint, targetPoint, maxDistance) */
    protected int findOptions(Vector3 pokemob, PathPoint current, PathPoint end, PathPoint[] pathOptions)
    {
        int ret = 0;
        int scale = 1;// (int) Math.max(Math.ceil(pokemob.x), 1);

        Vector3 check = Vector3.getNewVector();
        for (int l = 0; l < scale; l++)
        {
            if (ret > pathOptions.length - 10) return ret;

            int dx = end.xCoord - current.xCoord;
            int dy = end.yCoord - current.yCoord;
            int dz = end.zCoord - current.zCoord;

            EnumFacing s = null;

            if (dx != 0 && dx > dy && dx > dz)
            {
                s = dx < 0 ? EnumFacing.EAST : EnumFacing.WEST;
            }
            else if (dy != 0 && dy > dz && dy > dx)
            {
                s = dy < 0 ? EnumFacing.UP : EnumFacing.DOWN;
            }
            else if (dz != 0 && dz > dx && dz > dy)
            {
                s = dz < 0 ? EnumFacing.SOUTH : EnumFacing.NORTH;
            }

            int num = s != null ? s.ordinal() : 0;

            for (int i = 0; i < 6; i++)
            {
                EnumFacing side = EnumFacing.values()[(i + num) % 6];
                if (side == getDirFromPoint(current)) continue;

                int x = current.xCoord + side.getFrontOffsetX() * (l + 1);
                int y = current.yCoord + side.getFrontOffsetY() * (l + 1);
                int z = current.zCoord + side.getFrontOffsetZ() * (l + 1);

                boolean safe = false;
                PathPoint point = openPoint(x, y, z);
                if (isSafe(pokemob, x, y, z, getOpposite(side, check)))
                {
                    PathPoint point1 = openPoint(x, y - 1, z);
                    if (!point.isFirst)
                    {
                        Block down = v0.set(point1).getBlock(worldMap);
                        v1.set(point1);
                        float f1 = this.mob.getBlockPathWeight(worldMap, v1.offsetBy(DOWN));

                        if (down.getMaterial().isLiquid())
                        {
                            if (!point1.isFirst)
                            {
                                point1.blockWeight = f1;
                                pathOptions[ret++] = point1;
                                safe = true;
                            }
                        }
                        else
                        {
                            if (!point.isFirst)
                            {
                                point.blockWeight = f1;
                                pathOptions[ret++] = point;
                                safe = true;
                            }
                        }
                    }
                }
                if (!safe && !(side == EnumFacing.DOWN || side == EnumFacing.UP))
                {
                    if (isSafe(pokemob, x, y + 1, z, getOpposite(side, check)))
                    {
                        PathPoint point1 = openPoint(x, y + 1, z);
                        if (!point.isFirst)
                        {
                            Block down = v0.set(point1).getBlock(worldMap);
                            v1.set(point1);
                            float f1 = this.mob.getBlockPathWeight(worldMap, v1.offsetBy(DOWN));
                            if (down.getMaterial().isLiquid())
                            {
                                if (!point.isFirst)
                                {
                                    point.blockWeight = f1 + 5;
                                    pathOptions[ret++] = point;
                                    safe = true;
                                }
                            }
                            else
                            {
                                if (!point1.isFirst)
                                {
                                    point1.blockWeight = f1 + 5;
                                    pathOptions[ret++] = point1;
                                    safe = true;
                                }
                            }
                        }
                    }
                    if (isSafe(pokemob, x, y - 1, z, getOpposite(side, check)))
                    {
                        point = openPoint(x, y - 1, z);
                        PathPoint point1 = openPoint(x, y - 2, z);
                        if (!point.isFirst)
                        {
                            Block down = v0.set(point1).getBlock(worldMap);
                            v1.set(point1);
                            float f1 = this.mob.getBlockPathWeight(worldMap, v1.offsetBy(DOWN));
                            // check if this causes cmod exp
                            if (down.getMaterial().isLiquid())
                            {
                                if (!point1.isFirst)
                                {
                                    point1.blockWeight = f1 + 5;
                                    pathOptions[ret++] = point1;
                                    safe = true;
                                }
                            }
                            else
                            {
                                if (!point.isFirst)
                                {
                                    point.blockWeight = f1 + 5;
                                    pathOptions[ret++] = point;
                                    safe = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    EnumFacing getDirFromPoint(PathPoint current)
    {
        EnumFacing side = null;
        if (current.previous == null) return side;

        int dx = current.xCoord - current.previous.xCoord;
        int dy = current.yCoord - current.previous.yCoord;
        int dz = current.zCoord - current.previous.zCoord;

        if (dx != 0 && dx > dy && dx >= dz)
        {
            return dx < 0 ? EnumFacing.EAST : EnumFacing.WEST;
        }
        else if (dy != 0 && dy > dz && dy >= dx)
        {
            return dy < 0 ? EnumFacing.UP : EnumFacing.DOWN;
        }
        else if (dz != 0 && dz > dx && dz >= dy) { return dz < 0 ? EnumFacing.SOUTH : EnumFacing.NORTH; }

        return side;
    }

    PathPoint getPoint(Vector3 pokemob, PathPoint pathpoint[], PathPoint start, PathPoint end, ThutPath path,
            PathPoint[] pathOptions, boolean forward)
    {
        PathPoint pathpoint4 = path.dequeue();
        PathPoint pathpoint3 = pathpoint[2];
        byte dir = (byte) (forward ? 1 : -1);
        if (pathpoint4.equals(end)) { return pathpoint4; }
        pathpoint4.isFirst = true;
        int i = findOptions(pokemob, pathpoint4, end, pathOptions);
        for (int j = 0; j < i; ++j)
        {
            PathPoint pathpoint5 = pathOptions[j];

            if (pathpoint5.equals(end))
            {
                pathpoint5.previous = pathpoint4;
                return pathpoint5;
            }
            if (pathpoint5.direction != 0)
            {
                if (pathpoint5.direction != dir)
                {
                    pathpoint[1] = pathpoint5;
                    return pathpoint4;
                }
            }
            pathpoint5.direction = dir;
            float newPathDistance = pathpoint4.totalPathDistance + pathpoint4.distanceToSquared(pathpoint5)
                    + pathpoint5.blockWeight;

            if (!pathpoint5.isAssigned() || (newPathDistance < pathpoint5.totalPathDistance))
            {
                pathpoint5.previous = pathpoint4;
                pathpoint5.totalPathDistance = newPathDistance;
                pathpoint5.distanceToNext = pathpoint5.distanceToSquared(end);

                if (pathpoint5.isAssigned())
                {
                    path.changeDistance(pathpoint5,
                            pathpoint5.totalPathDistance + pathpoint5.distanceToNext + pathpoint5.blockWeight);
                }
                else
                {
                    pathpoint5.distanceToTarget = pathpoint5.totalPathDistance + pathpoint5.distanceToNext
                            + pathpoint5.blockWeight;
                    path.addPoint(pathpoint5);
                }
            }
            if ((pathpoint3 == null || pathpoint3.distanceToSquared(forward ? end : start) > pathpoint5
                    .distanceToSquared(forward ? end : start)))
            {
                pathpoint3 = pathpoint[2] = pathpoint5;
            }

        }
        return null;
    }

    /** Adds a path from start to end and returns the whole path (args: unused,
     * start, end, unused, maxDistance) */
    protected PathPoint getSubPath(Vector3 pokemob, PathPoint start, PathPoint end, ThutPath path, float distance)
    {
        PathPoint pointf[] = { start, null, null };
        PathPoint pointb[] = { end, null, null };
        PathPoint p1 = null;
        PathPoint p2 = null;
        PathPoint p3 = null;
        int p3t = 0;
        long starttime = System.nanoTime();
        int tries = 0;
        while (true)
        {
            if (pointf[2] != null)
            {
                if (p3 == null)
                {
                    p3 = pointf[2];
                    p3t = tries;
                }
                if (p3 != pointf[2])
                {
                    p3 = pointf[2];
                    p3t = tries;
                }
            }
            if (!pathf.isPathEmpty() && p2 == null)
            {
                p1 = getPoint(pokemob, pointf, start, end, pathf, pathOptionsf, true);
            }
            if (pointf[0].equals(end)) { return pointf[0]; }
            if (!pathb.isPathEmpty() && p1 == null)
            {
                p2 = getPoint(pokemob, pointb, end, start, pathb, pathOptionsb, false);
            }
            if (pointb[0].equals(start)) { return PathPoint.merge(null, pointb[0]); }

            if (pathf.isPathEmpty() && pathb.isPathEmpty()) { return null; }

            if (pointf[1] != null && p1 != null && p2 == null)
            {
                p2 = pointf[1];
            }
            if (pointb[1] != null && p2 != null && p1 == null)
            {
                p1 = pointb[1];
            }
            tries++;
            if (p1 != null && p2 != null) { return PathPoint.merge(p1, p2); }
            long time = System.nanoTime() - starttime;
            if (time > PATHTIME || (tries > 2.5 * p3t && p3t > 200) || tries > 1000)
            {
                break;
            }
        }
        return pointf[2];
    }

    private boolean isEmpty(Vector3 e, int x, int y, int z, Vector3 from)
    {
        Vector3 v = v0.set(x + 0.5, y, z + 0.5);

        boolean clear = false;

        Block b = v.getBlock(worldMap);
        if (mob.getBlockPathWeight(worldMap, e) < 0) { return false; }
        if (b instanceof BlockDoor)
        {
            if (b.getMaterial() == Material.wood) { return canFit(e, b); }
        }
        if (b.getMaterial() == Material.lava) return false;
        if (b.isNormalCube() || b.getMaterial().blocksMovement()) return false;
        if (b.isLadder(worldMap, v.getPos(), (EntityLivingBase) mob)) return true;

        if (e.x > 1 || e.z > 1)
        {
            v1.set(e);
            box.clear();
            box.boxMax().set(e);
            box.boxMin().clear();
            v1.y = 0;
            v1.reverse().scalarMultBy(0.5);
            box.addOffsetTo(v1);
            v1.clear();
            clear = !box.doTileCollision(worldMap, v, (Entity) mob, v1);
            return clear;
        }
        else if (!(clear = v.clearOfBlocks(worldMap)
                || v.add(0, ((EntityLiving) mob).stepHeight, 0).isClearOfBlocks(worldMap))) { return false; }

        return clear;
    }

    protected boolean isSafe(Vector3 e, int x, int y, int z, Vector3 from)
    {
        Block down = worldMap.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
        Material mDown = down.getMaterial();

        boolean water = mob.swims();
        boolean air = mob.flys() || mob.floats();
        BlockPos pos;
        boolean ladder = worldMap.getBlockState(pos = new BlockPos(x, y, z)).getBlock().isLadder(worldMap, pos,
                (EntityLivingBase) mob);
        // System.out.println("test");
        if (air || ladder) { return isEmpty(e, x, y, z, from); }
        if (water)
        {
            if (!mDown.isSolid())
            {
                if (mDown != Material.water) return false;
            }
            boolean empty = isEmpty(e, x, y, z, from);
            return empty;
        }
        if (mDown.isLiquid())
        {
            if (mDown != Material.water) return false;
        }
        return isEmpty(e, x, y, z, from) && (mDown == Material.water || !isEmpty(e, x, y - 1, z, from));

    }

    /** Returns a mapped point or creates and adds one */
    protected final PathPoint openPoint(int x, int y, int z)
    {
        int l = PathPoint.makeHash(x, y, z);
        PathPoint pathpoint = this.pointMap.lookup(l);
        if (pathpoint == null)
        {
            pathpoint = new PathPoint(x, y, z);
            this.pointMap.addKey(l, pathpoint);
        }

        return pathpoint;
    }

}
