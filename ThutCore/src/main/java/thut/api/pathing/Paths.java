package thut.api.pathing;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import thut.api.maths.Vector3;

public class Paths {

	public final World world;
	public ChunkCache chunks;
	public Vector3 lastSite = Vector3.getNewVectorFromPool();
	Vector3 v = Vector3.getNewVectorFromPool();
	public long lastTime = 0;
	/**
	 * 0 = updating cache, 1 = using cache;
	 */
	public final boolean[] cacheLock = {false, false};
	public Paths(World world) 
	{
		this.world = world;
	}


    public PathEntity getPathEntityToEntity(Entity entityA, Entity entityB, float distance, boolean doors, boolean block, boolean water, boolean air, boolean drown)
    {
//    	System.out.println(Arrays.toString(cacheLock));
    	while(cacheLock[0])
    	{
    		try
			{
				Thread.sleep(1);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
    	}
    	if(chunks == null)
    		return null;
    	
    	cacheLock[1] = true;
        PathEntity pathentity = (new ThutPathFinder(chunks, (IPathingMob)entityA, doors, block, water, air, drown)).createEntityPathTo(entityA, entityB, distance);
        cacheLock[1] = false;
        return pathentity;
    }

    public PathEntity getEntityPathToXYZ(Entity entity, int x, int y, int z, float distance, boolean doors, boolean block, boolean water, boolean air, boolean drown)
    {
    	if(x==0&&z==0)
    	{
    		new Exception().printStackTrace();
    		System.out.println(x+" "+y+" "+z);
    	}
    	while(cacheLock[0])
    	{
    		try
			{
				Thread.sleep(1);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
    	}
    	if(chunks == null)
    		return null;
    	
    	cacheLock[1] = true;
        ThutPathFinder pather = new ThutPathFinder(chunks, (IPathingMob)entity, doors, block, water, air, drown);
        PathEntity pathentity = pather.createEntityPathTo(entity, x, y, z, distance);
        cacheLock[1] = false;
//    	if(true) return null;
        return pathentity;
    }
}
