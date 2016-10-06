package thut.api.pathing;

import javax.annotation.Nullable;

import net.minecraft.world.IBlockAccess;
import thut.api.maths.Vector3;

public interface IPathingMob
{
    boolean floats();

    boolean flys();

    /** This should return values in the following ranges: -1 for blocks that
     * cannot be pathed over no matter what, 1-20 for blocks that are preferred
     * paths, 21-50 for blocks that are valid paths, but not preferred, and over
     * 50 for unfavourable, but still valid paths
     * 
     * @param world
     * @param location
     * @return */
    float getBlockPathWeight(IBlockAccess world, Vector3 location);

    double getFloatHeight();

    /** width, height, length
     * 
     * @return */
    Vector3 getMobSizes();

    boolean fits(IBlockAccess world, Vector3 location, @Nullable Vector3 directionFrom);

    int getPathTime();

    boolean swims();
}
