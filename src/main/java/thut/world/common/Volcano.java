package thut.world.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import thut.api.maths.Vector3;
import thut.world.common.corehandlers.ConfigHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class Volcano {
	
	
	public static Map<Integer, Map<Integer, Volcano>> volcanoMap = new ConcurrentHashMap<Integer, Map<Integer, Volcano>>();
	public static double TIMERATIO = 10E-3;
	
	public static Map<Integer, Long> seedMap = new ConcurrentHashMap<Integer, Long>();
	
	public static Volcano getVolcano(int x, int z, World worldObj)
	{
		if(!(volcanoMap.containsKey(x)))
		{
			addVolcano(x,z, worldObj);
		}
		else if(!(volcanoMap.get(x).containsKey(z)))
		{
			addVolcano(x,z, worldObj);
		}
		return volcanoMap.get(x).get(z);
	}
	
	public static void setSeed(World worldObj)
	{
		seedMap.put(worldObj.provider.dimensionId, worldObj.getSeed());
	}
	
	public static void setSeed(int id, long seed)
	{
		seedMap.put(id, seed);
	}
	
	public static void removeVolcano(int x, int z)
	{
		if(volcanoMap.containsKey(x)&&volcanoMap.get(x).containsKey(z))
			volcanoMap.get(x).remove(z);
	}
	

	public boolean isOverThisVolcano(int x, int z)
	{
		if(z<=this.z+1&&z>=this.z-1&&x<=this.x+1&&x>=this.x-1)
			return true;
		return false;
	}
	
	public static void addVolcano(int x, int z, World worldObj)
	{
		Random rX = new Random(x);
		Random rZ = new Random(z);
		Byte height = (byte) (rX.nextInt(50)+rZ.nextInt(50));
		int type = height>70?2:height>30?1:0;
		
		Random r = new Random(seedFromBlock(x,z,worldObj));
		
		Map tempMap = new ConcurrentHashMap<Integer, Volcano>();
		tempMap.put(z, new Volcano(x, z, type, height, r.nextDouble()));
		volcanoMap.put(x, tempMap);
	}
	
	public static long seedFromBlock(int x, int z, World worldObj)
	{
		if(!worldObj.isRemote)
		{
			return worldObj.getSeed()+worldObj.getChunkFromBlockCoords(x, z).xPosition+(((long)worldObj.getChunkFromBlockCoords(x, z).zPosition)<<32);
		}
		else
		{
			if(seedMap.get(worldObj.provider.dimensionId)!=null)
			return seedMap.get(worldObj.provider.dimensionId)+worldObj.getChunkFromBlockCoords(x, z).xPosition+(((long)worldObj.getChunkFromBlockCoords(x, z).zPosition)<<32);
		}
		return 0;
	}
	
	public static long seedFromChunk(int x, int z, World worldObj)
	{
		if(!worldObj.isRemote)
		{
			return worldObj.getSeed()+x+(((long)z)<<32);
		}
		else
		{
		//	System.out.println("chunk: "+x+" "+z);
			return seedMap.get(worldObj.provider.dimensionId)+x+(((long)z)<<32);
		}
	}
	
	public static int[] volcanoGen(int chunkX, int chunkZ, World worldObj)
	{
		Random r = new Random(worldObj.getSeed()+chunkX+(((long)chunkZ)<<32));
		int x = chunkX*16 + r.nextInt(16);
		int y = chunkZ*16 + r.nextInt(16);
		
		int[] ret = {x,y};
		int rand = r.nextInt(ConfigHandler.VolcRate);
		if(!(rand==1))
		{
			ret = null;
		}
		
		return ret;
	}

	public int x=0,y=5,z=0,type=10,h=0;
	public double frequency = 0;
	public double shift = 0;
	public double majorFactor = 1;
	public double growthFactor = 1;
	public double minorFactor = 1;
	public double activeFactor = 1;
	public boolean active = true;
	
	public static void init(World worldObj)
	{
		if(!worldObj.isRemote)
		{
			setSeed(worldObj);
		}
		for(int i = -500; i<500; i++)
			for(int j = -500; j<500; j++)
			{
				int[] volc = volcanoGen(i, j, worldObj);
				if(volc!=null)
				{
					addVolcano(volc[0], volc[1], worldObj);
				}
			}
	}
	
	
	
	public Volcano(){}
	
	public Volcano(int x, int z, int type, int h, double frequency)
	{
		this.x = x;
		this.z=z;
		this.type=type;
		this.h=h;
		this.frequency = frequency;
	}
	
	public double strength(double d)
	{
		//return activeFactor*majorFactor*minorFactor*growthFactor*(type+1)*Math.sin((frequency+shift)*d/TIMERATIO);
		return activeFactor*majorFactor*minorFactor*growthFactor*(type+1)*MathHelper.sin((float) ((frequency+shift)*d/TIMERATIO));
	}
	
	public double distanceSq(TileEntity te)
	{
		return (new Vector3(x,y,z)).distToSq(new Vector3(te));
	}
	
	
	public void writeToNBT(NBTTagCompound cmpnd, String tag)
	{
		cmpnd.setInteger(tag+"x",this.x);
		cmpnd.setInteger(tag+"y",this.y);
		cmpnd.setInteger(tag+"z",this.z);
		cmpnd.setInteger(tag+"type",this.type);
		cmpnd.setInteger(tag+"h",this.h);
	}

	public static Volcano readFromNBT(NBTTagCompound cmpnd, String tag)
	{
		Volcano ret = new Volcano();
		ret.x = cmpnd.getInteger(tag+"x");
		ret.y = cmpnd.getInteger(tag+"y");
		ret.z = cmpnd.getInteger(tag+"z");
		ret.type = cmpnd.getInteger(tag+"type");
		ret.h = cmpnd.getInteger(tag+"h");
		return ret;
	}
	
}
