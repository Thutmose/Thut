package thut.api.pathing;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import thut.api.maths.Vector3;

public class Paths {

	public final World world;
	public ChunkCache chunks;
	public Vector3 lastSite = Vector3.getNewVector();
	Vector3 v = Vector3.getNewVector();
	public long lastTime = 0;
	/**
	 * 0 = updating cache, 1 = using cache;
	 */
	public final boolean[] cacheLock = {false, false};
	public Paths(World world) 
	{
		this.world = world;
	}


    public PathEntity getEntityPathToXYZ(Entity entity, int x, int y, int z, float distance)
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
        ThutPathFinder pather = new JPSPather(chunks, (IPathingMob)entity);
        PathEntity pathentity = pather.createEntityPathTo(entity, x, y, z, distance);
        cacheLock[1] = false;
        
        return pathentity;
    }

    public PathEntity getPathEntityToEntity(Entity entityA, Entity entityB, float distance)
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
        PathEntity pathentity = (new JPSPather(chunks, (IPathingMob)entityA)).createEntityPathTo(chunks, entityA, entityB, distance);
        cacheLock[1] = false;
        
        return pathentity;
    }
}
