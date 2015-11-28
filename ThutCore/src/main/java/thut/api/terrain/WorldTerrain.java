package thut.api.terrain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import thut.api.maths.Vector3;

public class WorldTerrain {

	public final int dimID;
	
	private TerrainMap terrainMap = new TerrainMap();

	public WorldTerrain(int dimID) 
	{
		this.dimID = dimID;
	}
	
	public TerrainSegment getTerrain(int chunkX, int chunkY, int chunkZ)
	{
		return getTerrain(chunkX, chunkY, chunkZ, false);
	}
	
	public TerrainSegment getTerrain(int chunkX, int chunkY, int chunkZ, boolean saving)
	{
		TerrainSegment ret = null;
		
		ret = terrainMap.getSegment(chunkX, chunkY, chunkZ);
		if(ret==null && !saving)
		{
			ret = new TerrainSegment(chunkX, chunkY, chunkZ);
			terrainMap.setSegment(ret, chunkX, chunkY, chunkZ);
		}
		return ret;
	}
	
	public boolean saveTerrain(NBTTagCompound nbt, int x, int z)
	{
		nbt.setInteger("xCoord", x);
		nbt.setInteger("zCoord", z);
		boolean saved = false;
		int count = 0;
    	for(int i = 0; i<16; i++)
    	{
    		TerrainSegment t = getTerrain(x, i, z, true);
    		if(t==null) continue;
    		t.checkToSave();
    		if(!t.toSave)
			{
    			count++;
				continue;
			}
    		saved = true;
    		NBTTagCompound terrainTag = new NBTTagCompound();
    		t.saveToNBT(terrainTag);
    		nbt.setTag("terrain"+x+","+i+","+z+","+dimID, terrainTag);
    	}
    	return saved;
	}
	
	public void loadTerrain(NBTTagCompound nbt)
	{
    	loadTerrain(nbt, 16);
	}
	
	public void loadTerrain(NBTTagCompound nbt, int max)
	{
		int x = nbt.getInteger("xCoord");
		int z = nbt.getInteger("zCoord");
		max = Math.min(16, max+1);
    	for(int i = 0; i<max; i++)
    	{
			NBTTagCompound terrainTag = null;
			try
			{
				terrainTag = nbt.getCompoundTag("terrain"+x+","+i+","+z+","+dimID);
			}
			catch (Exception e)
			{
				
			}
			TerrainSegment t = null;
			if(terrainTag != null && !terrainTag.hasNoTags())
			{
				t = TerrainSegment.readFromNBT(terrainTag);
				if(t!=null)
				{
					addTerrain(t);
				}
			}
			if(t==null)
			{
				getTerrain(x,i,x);
			}
    	}
	}
	
	public void addTerrain(TerrainSegment terrain)
	{
		if(terrain!=null)
		{
			terrainMap.setSegment(terrain, terrain.chunkX, terrain.chunkY, terrain.chunkZ);
		}
	}
	
	public void removeTerrain(int chunkX, int chunkZ)
	{
		for(int i = 0; i<16; i++)
		{
			terrainMap.setSegment(null, chunkX, i, chunkZ);
		}
	}
	
	public static class TerrainMap
	{
		final HashMap terrain = new HashMap();
		
		public TerrainMap(){}
		
		public void setSegment(TerrainSegment t, int x, int y, int z)
		{
			y = Math.max(0, y);
			y = Math.min(y, 15);
			
			Long key = getKey(x, y, z);
			if(t!=null)
				terrain.put(key, t);
			else
				terrain.remove(key);
		}
		public TerrainSegment getSegment(int x, int y, int z)
		{
			y = Math.max(0, y);
			y = Math.min(y, 15);
			
			Long key = getKey(x, y, z);
			if(!terrain.containsKey(key))
				terrain.put(key, new TerrainSegment(x, y, z));
			return (TerrainSegment) terrain.get(key);
		}
		
		private Long getKey(int x, int y, int z)
		{
			long l = (long)x + (((long)z) << 21) + (((long)y) << 42);
			return new Long(l);
		}
	}
}
