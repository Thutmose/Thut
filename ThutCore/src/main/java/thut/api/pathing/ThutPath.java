package thut.api.pathing;

import net.minecraft.pathfinding.Path;

public class ThutPath extends Path {
    /** Contains the points in this path */
    private PathPoint[] pathPoints = new PathPoint[1024];
    /** The number of points in this path */
    private int count;

    /**
     * Adds a point to the path
     */
    public PathPoint addPoint(PathPoint toAdd)
    {
        if (toAdd.index >= 0)
        {
            throw new IllegalStateException("OW KNOWS!");
        }
        else
        {
            if (this.count == this.pathPoints.length)
            {
                PathPoint[] apathpoint = new PathPoint[this.count << 1];
                System.arraycopy(this.pathPoints, 0, apathpoint, 0, this.count);
                this.pathPoints = apathpoint;
            }

            this.pathPoints[this.count] = toAdd;
            toAdd.index = this.count;
            this.sortBack(this.count++);
            return toAdd;
        }
    }

    /**
     * Changes the provided point's distance to target
     */
    public void changeDistance(PathPoint point, float newDistance)
    {
        float f1 = point.distanceToTarget;
        point.distanceToTarget = newDistance;

        if (newDistance < f1)
        {
            this.sortBack(point.index);
        }
        else
        {
            this.sortForward(point.index);
        }
    }

    /**
     * Clears the path
     */
    @Override
	public void clearPath()
    {
        this.count = 0;
    }

    /**
     * Returns and removes the first point in the path
     */
    @Override
	public PathPoint dequeue()
    {
        PathPoint pathpoint = this.pathPoints[0];
        this.pathPoints[0] = this.pathPoints[--this.count];
        this.pathPoints[this.count] = null;

        if (this.count > 0)
        {
            this.sortForward(0);
        }

        pathpoint.index = -1;
        return pathpoint;
    }

    /**
     * Returns true if this path contains no points
     */
    @Override
	public boolean isPathEmpty()
    {
        return this.count == 0;
    }

    /**
     * Sorts a point to the left
     */
    private void sortBack(int index)
    {
        PathPoint pathpoint = this.pathPoints[index];
        int j;

        for (float f = pathpoint.distanceToTarget; index > 0; index = j)
        {
            j = index - 1 >> 1;
            PathPoint pathpoint1 = this.pathPoints[j];

            if (f >= pathpoint1.distanceToTarget)
            {
                break;
            }

            this.pathPoints[index] = pathpoint1;
            pathpoint1.index = index;
        }

        this.pathPoints[index] = pathpoint;
        pathpoint.index = index;
    }

    /**
     * Sorts a point to the right
     */
    private void sortForward(int index)
    {
        PathPoint pathpoint = this.pathPoints[index];
        float f = pathpoint.distanceToTarget;

        while (true)
        {
            int j = 1 + (index << 1);
            int k = j + 1;

            if (j >= this.count)
            {
                break;
            }

            PathPoint pathpoint1 = this.pathPoints[j];
            float f1 = pathpoint1.distanceToTarget;
            PathPoint pathpoint2;
            float f2;

            if (k >= this.count)
            {
                pathpoint2 = null;
                f2 = Float.POSITIVE_INFINITY;
            }
            else
            {
                pathpoint2 = this.pathPoints[k];
                f2 = pathpoint2.distanceToTarget;
            }

            if (f1 < f2)
            {
                if (f1 >= f)
                {
                    break;
                }

                this.pathPoints[index] = pathpoint1;
                pathpoint1.index = index;
                index = j;
            }
            else
            {
                if (f2 >= f)
                {
                    break;
                }

                this.pathPoints[index] = pathpoint2;
                pathpoint2.index = index;
                index = k;
            }
        }

        this.pathPoints[index] = pathpoint;
        pathpoint.index = index;
    }
}