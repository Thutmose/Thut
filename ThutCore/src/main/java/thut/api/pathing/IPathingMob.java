package thut.api.pathing;

import net.minecraft.world.IBlockAccess;
import thut.api.maths.Vector3;

public interface IPathingMob
{
	float getBlockPathWeight(IBlockAccess world, Vector3 location);
	boolean flys();
	boolean floats();
	boolean swims();
	double getFloatHeight();
	int getPathTime();
	/**
	 * width, height, length
	 * @return
	 */
	Vector3 getMobSizes();
}
