package thut.api.pathing;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import thut.api.maths.Vector3;

/** This is a modified version of the A* Pathfinding algorithm, it resembles the
 * Jump Point Search version, where it skips large numbers of points in sparse
 * systems
 * 
 * @author Thutmose */
public class JPSPather extends ThutPathFinder
{
    public static int Int(double x)
    {
        return MathHelper.floor(x);
    }

    public JPSPather(IBlockReader world, IPathingMob entity)
    {
        super(world, entity);
    }

    /** Finds the closest location to the target where the pokemob can path the
     * entire way.
     * 
     * @param world
     * @param e
     * @param source
     * @param direction
     * @param range
     * @return */
    public Vector3 findNextLocation(IBlockReader world, Vector3 e, Vector3 source, Vector3 direction, double range)
    {
        direction.norm();
        double xprev = source.x, yprev = source.y, zprev = source.z;
        double dx, dy, dz;
        int x, y, z;
        for (double i = 0; i < range; i += 0.5)
        {
            dx = i * direction.x;
            dy = i * direction.y;
            dz = i * direction.z;

            double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

            if (ytest > 255) return null;
            y = Int(ytest);
            x = Int(xtest);
            z = Int(ztest);
            if (!(x == Int(xprev) && y == Int(yprev) && z == Int(zprev)))
            {
                PathPoint point = openPoint(x, y, z);
                boolean clear = isSafe(e, point, direction);
                if (!clear) { return Vector3.getNewVector().set(Int(xtest), Int(ytest), Int(ztest)); }
            }
            yprev = ytest;
            xprev = xtest;
            zprev = ztest;
        }
        return null;
    }

    @Override
    /** populates pathOptions with available points and returns the number of
     * options found (args: entity, currentPoint, targetPoint, maxDistance) */
    protected int findOptions(Vector3 pokemob, PathPoint current, PathPoint end, PathPoint[] pathOptions)
    {
        int ret = 0;

        Vector3 here = Vector3.getNewVector().set(current);
        Vector3 dest = Vector3.getNewVector().set(end);
        Vector3 closestToDest = Vector3.getNewVector();

        getBlockedPoint(pokemob, here, dest, closestToDest);

        if (closestToDest.sameBlock(dest))
        {
            if (!end.isFirst)
            {
                end.blockWeight = 1;
                pathOptions[ret++] = end;
            }
        }
        else
        {
            ret = super.findOptions(pokemob, current, end, pathOptions);
            // Consider any area with a skip less than about 10 blocks as too
            // dense for JPS.
            double size = pokemob.x * pokemob.z;
            if (size < 1) size = 0;
            if (ret >= 3 && closestToDest.distToSq(here) > 100)
            {
                current = openPoint(closestToDest.intX(), closestToDest.intY(), closestToDest.intZ());
                ret = super.findOptions(pokemob, current, end, pathOptions);
            }
        }
        return ret;
    }

    /** Sets temp to the closest point to dest from here.
     * 
     * @param here
     * @param dest
     * @param temp */
    private void getBlockedPoint(Vector3 e, Vector3 here, Vector3 dest, Vector3 temp)
    {
        Vector3 direction = dest.subtract(here).norm();
        Vector3 t2 = findNextLocation(worldMap, e, here, direction, here.distanceTo(dest));
        if (t2 != null)
        {
            temp.set(t2);
            temp.addTo(0.5, 0, 0.5).addTo(direction.reverse().scalarMultBy(1.5));
        }
        else
        {
            temp.set(dest);
        }
    }

    @Override
    /** Adds a path from start to end and returns the whole path (args: unused,
     * start, end, unused, maxDistance) */
    protected PathPoint getSubPath(Vector3 pokemob, PathPoint start, PathPoint end, ThutPath path, float distance)
    {
        PathPoint pointf[] = { start, null, null };
        int tries = 0;
        PathPoint point = null;
        long starttime = System.nanoTime();
        while (true)
        {
            if (!pathf.isPathEmpty())
            {
                point = getPoint(pokemob, pointf, start, end, pathf, pathOptionsf, true);
            }
            if (point == end) return point;

            if (pointf[0].equals(end)) return pointf[0];
            if (pointf[2] != null && pointf[2].equals(end)) return pointf[2];

            if (pathf.isPathEmpty())
            {
                // TODO debug when this occurs
                // System.out.println("no path " + point + " " + pointf[2] + " "
                // + start + " " + end);
            }
            if (pathf.isPathEmpty()) { return pointf[2]; }

            tries++;
            long time = System.nanoTime() - starttime;

            if (time > PATHTIME || tries > 1000)
            {
             //   System.out.println("Too long " + tries + " " + end + " " + time+" "+start+" "+end);
                break;
            }
        }
        return pointf[2];
    }
}
