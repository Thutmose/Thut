package thut.api.pathing;

import javax.annotation.Nullable;

import net.minecraft.world.IBlockAccess;
import thut.api.maths.Vector3;

public interface IPathingMob
{
    boolean floats();

    boolean flys();

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
