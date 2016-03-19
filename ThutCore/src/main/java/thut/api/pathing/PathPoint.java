package thut.api.pathing;

import net.minecraft.util.MathHelper;

public class PathPoint extends net.minecraft.pathfinding.PathPoint{
    public static int makeHash(int x, int y, int z)
    {
    	int i = ((x + 511) + (y + 511) * 1024 + (z + 511) * 1024 * 1024);
    	return i;
      //  return y & 255 | (x & 32767) << 8 | (z & 32767) << 24 | (x < 0 ? Integer.MIN_VALUE : 0) | (z < 0 ? 32768 : 0);
    }
    public static PathPoint merge(PathPoint forward, PathPoint backward)
    {
    	PathPoint ret = null;
    	
        int i = 1;
        PathPoint pathpoint2 = backward;
        
        for(pathpoint2 = backward; pathpoint2.previous != null; pathpoint2 = pathpoint2.previous)
        {
        	++i;
        }

        PathPoint[] arr = new PathPoint[i];
        pathpoint2 = backward;
        int j = i-1;
        --i;
        for (arr[j-i] = backward; pathpoint2.previous != null; arr[j-i] = pathpoint2)
        {
            pathpoint2 = pathpoint2.previous;
            --i;
        }
        arr[0].previous = forward;
        for(i = 1; i<j+1; i++)
        {
        	arr[i].previous = arr[i-1];
        }
    	ret = arr[j];
    	return ret;
    }
    /** The x coordinate of this point */
    public final int xCoord;
    /** The y coordinate of this point */
    public final int yCoord;
    /** The z coordinate of this point */
    public final int zCoord;
    /** A hash of the coordinates used to identify this point */
    private final int hash;
    /** The index of this point in its assigned path */
    int index = -1;
    /** The distance along the path to this point */
    float totalPathDistance;
    /** The linear distance to the next point */
    float distanceToNext;
    /** The distance to the target */
    float distanceToTarget;
    /** The point preceding this in its assigned path */
    PathPoint previous;
    
    /** Indicates this is the origin */
    public boolean isFirst;

    /** Indicates direction of path 1 is forwards, -1 is backwards*/
    public byte direction = 0;

    public float blockWeight=10;

    public PathPoint(int x, int y, int z)
    {
    	super(x, y, z);
        this.xCoord = x;
        this.yCoord = y;
        this.zCoord = z;
        this.hash = makeHash(x, y, z);
    }

    /**y & 1023 | (x & 524287) << 10 | (z & 524287) << 28 | (x < 0 ? Integer.MIN_VALUE : 0) | (z < 0 ? 524288 : 0);
     * Returns the linear distance to another path point
     */
    public float distanceTo(PathPoint point)
    {
        float f = point.xCoord - this.xCoord;
        float f1 = point.yCoord - this.yCoord;
        float f2 = point.zCoord - this.zCoord;
        return MathHelper.sqrt_float(f * f + f1 * f1 + f2 * f2);
    }

    /**
     * Returns the squared distance to another path point
     */
    public float distanceToSquared(PathPoint point)
    {
        float f = point.xCoord - this.xCoord;
        float f1 = point.yCoord - this.yCoord;
        float f2 = point.zCoord - this.zCoord;
        return f * f + f1 * f1 + f2 * f2;
    }

    @Override
	public boolean equals(Object o)
    {
        if (!(o instanceof PathPoint))
        {
            return false;
        }
        else
        {
            PathPoint pathpoint = (PathPoint)o;
            return this.hash == pathpoint.hash && this.xCoord == pathpoint.xCoord && this.yCoord == pathpoint.yCoord && this.zCoord == pathpoint.zCoord;
        }
    }

    @Override
	public int hashCode()
    {
        return this.hash;
    }

    /**
     * Returns true if this point has already been assigned to a path
     */
    @Override
	public boolean isAssigned()
    {
        return this.index >= 0;
    }
    
    @Override
	public String toString()
    {
        return this.xCoord + ", " + this.yCoord + ", " + this.zCoord;
    }
}
