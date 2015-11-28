package thut.api.terrain;

import static thut.api.terrain.BiomeDatabase.contains;
import static thut.api.terrain.BiomeType.CAVE;
import static thut.api.terrain.BiomeType.CAVE_WATER;
import static thut.api.terrain.BiomeType.INDUSTRIAL;
import static thut.api.terrain.BiomeType.LAKE;
import static thut.api.terrain.BiomeType.SKY;
import static thut.api.terrain.BiomeType.VILLAGE;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.BiomeDictionary.Type;
import thut.api.maths.Vector3;

public class TerrainSegment {

	public final int chunkX;
	public final int chunkY;
	public final int chunkZ;
	public final BlockPos pos;
	private Chunk chunk;
	public boolean toSave = false;
	public boolean isSky = false;
	public boolean init = true;
	Vector3 temp = Vector3.getNewVectorFromPool();
	Vector3 temp1 = Vector3.getNewVectorFromPool();
	Vector3 mid = Vector3.getNewVectorFromPool();
	
	public static final int GRIDSIZE = 4;
	int[] biomes = new int[GRIDSIZE*GRIDSIZE*GRIDSIZE];
	
	HashMap<String, ITerrainEffect> effects = new HashMap();
	
	public TerrainSegment(int x, int y, int z) 
	{
		chunkX = x;
		chunkY = y;
		chunkZ = z;
		pos = new BlockPos(x,y,z);
		mid.set(this.chunkX*16 + 8, this.chunkY*16 + 8, this.chunkZ*16 + 8);
	}
	
	/**
	 * Applies all of the effects onto the mob
	 * @param hungrymob
	 */
	public void doEffects(String effect, EntityLivingBase entity, boolean firstEntry)
	{
		if(effects.containsKey(effect))
		{
			effects.get(effect).doEffect(entity, firstEntry);
		}
	}
	
	public void addEffect(ITerrainEffect effect, String name)
	{
		effect.bindToTerrain(chunkX, chunkY, chunkZ);
		effects.put(name, effect);
	}
	
	public ITerrainEffect geTerrainEffect(String name)
	{
		return effects.get(name);
	}
	
	public void setBiome(Vector3 v, int i)
	{	
		setBiome(v.intX(), v.intY(), v.intZ(), i);
	}
	public void setBiome(int x, int y, int z, int biome)
	{
		int relX = (x&15)/GRIDSIZE, relY = (y&15)/GRIDSIZE, relZ = (z&15)/GRIDSIZE;
		int pre = biomes[relX + GRIDSIZE * relY + GRIDSIZE * GRIDSIZE * relZ];
		biomes[relX + GRIDSIZE * relY + GRIDSIZE * GRIDSIZE * relZ] = biome;
		int post = biomes[relX + GRIDSIZE * relY + GRIDSIZE * GRIDSIZE * relZ];
		if(biome>255) toSave = true;
		
	}
	
	public BlockPos getChunkCoords()
	{
		return pos;
	}
	
	public boolean isInTerrainSegment(double x, double y, double z)
	{
		boolean ret = true;
        int i = MathHelper.floor_double(x / 16.0D);
        int j = MathHelper.floor_double(y / 16.0D);
        int k = MathHelper.floor_double(z / 16.0D);
        
        ret = i==chunkX&&k==chunkZ;//&&j==chunkY;
		return ret;
	}
	
	public static boolean isInTerrainColumn(Vector3 t, Vector3 point)
	{
		boolean ret = true;
        int i = MathHelper.floor_double(point.intX() / 16.0D);
        int k = MathHelper.floor_double(point.intZ() / 16.0D);
        
        ret = i==t.intX()&&k==t.intZ();
		return ret;
	}
	
	public int count(World world, Block b, Vector3 v, int range)
	{
		temp1.set(temp);
		temp.set(v);
		int ret = 0;
		for(int i = -range; i<= range; i++)
			for(int j = -range; j<=range; j++)
				for(int k = -range; k<=range; k++)
				{
					
					boolean bool = true;
			        int i1 = MathHelper.floor_double(v.intX()+i / 16.0D);
			        int j1 = MathHelper.floor_double(v.intY()+i / 16.0D);
			        int k1 = MathHelper.floor_double(v.intZ()+i / 16.0D);
			        
			        bool = i1==v.intX()/16&&k1==v.intZ()/16;//&&j==chunkY;
					
					
					if(bool)
					{
						temp.set(v).add(i,j,k);
						Block b1 = temp.getBlock(world);
						if(temp.getBlock(world)==b||(b==null&&temp.getBlock(world)==null))
						{
							ret++;
						}
					}
				}
		temp.set(temp1);
		return ret;
	}
	
	public double getAverageSlope(World world, Vector3 point, int range)
	{
		double slope = 0;
		
		double prevY = point.getMaxY(world);
		
		double dy = 0;
		double dz = 0;
		temp1.set(temp);
		temp.set(point);
		int count = 0;
		for(int i = -range; i<=range; i++)
		{
			dz = 0;
			for(int j = -range; j<=range; j++)
			{
				if(isInTerrainColumn(point, temp.addTo(i,0,j)))
					dy += Math.abs((point.getMaxY(world, point.intX()+i, point.intZ()+j) - prevY));
					dz++;
					count++;
				temp.set(point);
			}
			slope += (dy/dz);
		}
		temp.set(temp1);
		
		return slope/count;
	}
	
	private void setBiome(int[] biomes)
	{
		if(biomes.length == this.biomes.length)
			this.biomes = biomes;
		else
		{
			for(int i = 0; i< biomes.length; i++)
			{
				if(i >= this.biomes.length)
					return;
				this.biomes[i] = biomes[i];
			}
		}
	}
	
	private void setEffects(int[] effects)
	{
		if(effects.length==effects.length)
		for(int i = 0; i<effects.length; i++)
		{
			effects[i] = effects[i];
		}
	}
	
	public int getBiome(int x, int y, int z)
	{
		int ret = 0;
		int relX = (x&15)/GRIDSIZE, relY = (y&15)/GRIDSIZE, relZ = (z&15)/GRIDSIZE;
		
		if(relX<4&&relY<4&&relZ<4)
		{
			ret = biomes[relX + GRIDSIZE * relY + GRIDSIZE * GRIDSIZE * relZ];
			
		}
		if(ret>255) toSave = true;

		return ret;
	}
	
	public int getBiome(Vector3 v)
	{
		return getBiome(v.intX(), v.intY(), v.intZ());
	}
	
	public void refresh(World world)
	{
		boolean sky = true;
		chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
		for(int i = 0; i<GRIDSIZE; i++)
			for(int j = 0; j<GRIDSIZE; j++)
				for(int k = 0; k<GRIDSIZE; k++)
				{
					temp.set(chunkX*16+i*16/GRIDSIZE, chunkY*16+j*16/GRIDSIZE, chunkZ*16+k*16/GRIDSIZE);
					long time = System.nanoTime();
					int biome = adjustedCaveBiome(world, temp);
					int biome2 = adjustedNonCaveBiome(world, temp);
					double dt = (System.nanoTime()-time)/1000000000D;
					if(biome>255||biome2>255)
						toSave = true;
					if(biome==-1)
						biome = biome2;
					else
						sky = false;
					
					biomes[i + GRIDSIZE *j + GRIDSIZE * GRIDSIZE * k] = biome;
				}
		if(sky)
		{
			for(int i = 0; i<16; i++)
			{
				sky = getCentre().add(0, -1*i, 0).getBlock(world) == null || getCentre().add(0, -1*i, 0).getBlock(world).getMaterial() == Material.air;
				if(!sky)
					break;
			}
		}
		if(sky)
		{
			isSky = true;
			for(int i = 0; i<GRIDSIZE; i++)
				for(int j = 0; j<GRIDSIZE; j++)
					for(int k = 0; k<GRIDSIZE; k++)
					{
						biomes[i + GRIDSIZE *j + GRIDSIZE * GRIDSIZE * k] = SKY.getType();
					}
		}
		
		
		long time = System.nanoTime();

		double dt = (System.nanoTime()-time)/1000000000D;
		if(dt > 0.001)
			System.out.println("industrial check took "+dt);
	}
	
	@Override
	public boolean equals(Object o)
	{
		boolean ret = false;
		if(o instanceof TerrainSegment)
		{
			ret = ((TerrainSegment)o).chunkX == chunkX&&((TerrainSegment)o).chunkY == chunkY&&((TerrainSegment)o).chunkZ == chunkZ;
		}
		
		return ret;
	}
	
	@Override
	public String toString()
	{
		String ret = "Terrian Segment "+chunkX+","+chunkY+","+chunkZ+" Centre:"+getCentre();
		String eol = System.getProperty("line.separator");
		for(int i = 0; i<4; i++)
			for(int j = 0; j<4; j++)
			{
				String line = "[";
				for(int k = 0; k<4; k++)
				{
					line = line + biomes[i + GRIDSIZE *j + GRIDSIZE * GRIDSIZE * k];
					if(k!=3)
						line = line+", ";
				}
				line = line+"]";
				ret = ret + eol + line;
			}
		
		return ret;
	}
	
	public void initBiomes(World world)
	{
		if(init)
		{
			refresh(world);
			init = false;
		}
	}
	
	public boolean checkIndustrial(World world)
	{
		boolean industrial = false;
		
		for(int i = 0; i<GRIDSIZE; i++)
			for(int j = 0; j<GRIDSIZE; j++)
				for(int k = 0; k<GRIDSIZE; k++)
				{
					temp.set(chunkX*16+i*4, chunkY*16+j*4, chunkZ*16+k*4);
					if(isIndustrial(temp, world))
					{
						industrial = true;
						biomes[i + GRIDSIZE *j + GRIDSIZE * GRIDSIZE * k] = INDUSTRIAL.getType();
					}
				}
		
		
		return industrial;
	}
	
	public int adjustedCaveBiome(World world, Vector3 v)
	{
		int biome = -1;
		if(world.provider.doesWaterVaporize())//This is the isHellWorld thing
			return -1;
		boolean sky = false;
		temp1.clear();
		for(int i = 0; i<GRIDSIZE; i++)
			for(int j = 0; j<GRIDSIZE; j++)
				for(int k = 0; k<GRIDSIZE; k++)
					sky = sky||temp1.set(v).addTo(i, j, k).isOnSurfaceIgnoringWater(chunk, world);
		if(sky) return -1;
		
		if(!sky&&count(world, Blocks.water, v, 1)>2)
			biome = CAVE_WATER.getType();
		else if(!sky)
			biome = CAVE.getType();
		
		return biome;
	}
	
	public int adjustedNonCaveBiome(World world, Vector3 v)
	{
		int biome = 0;

		BiomeGenBase b = v.getBiome(chunk, world.getWorldChunkManager());
		biome = BiomeDatabase.getBiomeType(b);
		
        boolean notLake = BiomeDatabase.contains(b, Type.OCEAN) 
        		|| BiomeDatabase.contains(b, Type.SWAMP) 
        		|| BiomeDatabase.contains(b, Type.RIVER) 
        		|| BiomeDatabase.contains(b, Type.WATER) 
        		|| BiomeDatabase.contains(b, Type.BEACH)
        		;
        
		double slope =  0.1;//!notLake?getAverageSlope(world, v, 5):0;
		int water = v.blockCount2(world, Blocks.water, 3);
        if(water>4)
        {
        	if(!notLake)
        	{
        		biome = LAKE.getType();
        	}
			return biome;
        }
		if(world.villageCollectionObj!=null)
		{
	        Village village = world.villageCollectionObj.getNearestVillage(new BlockPos(
	        		MathHelper.floor_double(v.x), 
	        		MathHelper.floor_double(v.y), 
	        		MathHelper.floor_double(v.z)), 2);
	        if(village!=null)
	        {
	        	biome = VILLAGE.getType();
	        }
		}
		
		return biome;
	}

	static BiomeGenBase getBiome(Type not,Type... types)
	{
		BiomeGenBase ret = null;
		biomes:
		for(BiomeGenBase b: BiomeGenBase.getBiomeGenArray())
		{
			if(b==null)
				continue;
			if(not !=null && contains(b, not))
				continue;
			for(Type t: types)
			{
				if(!BiomeDatabase.contains(b, t))
					continue biomes;
			}
			ret = b;
		}
		return ret;
	}
	
	static boolean isIndustrial(Vector3 v, World world)
	{
		boolean ret = false;
		
		int count = v.blockCount2(world, Blocks.redstone_block, 16);
		
		ret = count >= 2;
		
		return ret;
	}
	
	void checkToSave()
	{
		int subCount = biomes.length;
		for(int i = 0; i<subCount; i++)
		{
			int temp1 = biomes[i];
			if(temp1>255 && temp1 != BiomeType.SKY.getType())
			{
				toSave = true;
				return;
			}
		}
		toSave = false;
	}
	
	public Vector3 getCentre()
	{
		return mid;
	}
	
	public void saveToNBT(NBTTagCompound nbt)
	{
		if(!(toSave)) return;
		nbt.setIntArray("biomes", biomes);
		nbt.setInteger("x", chunkX);
		nbt.setInteger("y", chunkY);
		nbt.setInteger("z", chunkZ);
		
		//TODO save terraineffects including class for constructing.
	}
	
	public static TerrainSegment readFromNBT(NBTTagCompound nbt)
	{
		TerrainSegment t = null;
		int[] biomes = nbt.getIntArray("biomes");
		int x = nbt.getInteger("x");
		int y = nbt.getInteger("y");
		int z = nbt.getInteger("z");
		t = new TerrainSegment(x, y, z);
		t.init = false;
		t.setBiome(biomes);
		return t;
	}
    
    public static Vector3 getWind(World worldObj, double x, double y, double z)
    {

        double[] tempWind = new double[] { 0, 0 };
        double windFactor = 1;
        double frequencyFactor = 0.00015;
        tempWind[0] = windFactor * (sin(frequencyFactor * ((x / 16) + worldObj.getTotalWorldTime())) +
            sin(frequencyFactor * ((x / 16) + worldObj.getTotalWorldTime()) * frequencyFactor * ((x / 16) + worldObj.getTotalWorldTime())) +
            sin(frequencyFactor * worldObj.getTotalWorldTime() * (x / 16)) * cos(frequencyFactor * worldObj.getTotalWorldTime() * (x / 16)) +
            sin(frequencyFactor * worldObj.getTotalWorldTime() * (x / 16)) * cos(frequencyFactor * worldObj.getTotalWorldTime() * (x / 16)) *
                sin(frequencyFactor * worldObj.getTotalWorldTime() * (x / 16)) * cos(frequencyFactor * worldObj.getTotalWorldTime() * (x / 16)));

        tempWind[1] = windFactor * (cos(frequencyFactor * ((z / 16) + worldObj.getTotalWorldTime())) +
            cos(frequencyFactor * ((z / 16) + worldObj.getTotalWorldTime()) * frequencyFactor * ((z / 16) + worldObj.getTotalWorldTime())) +
            sin(frequencyFactor * ((z / 16) + worldObj.getTotalWorldTime())) * cos(frequencyFactor * ((z / 16) + worldObj.getTotalWorldTime())) +
            sin(frequencyFactor * ((z / 16) + worldObj.getTotalWorldTime())) * cos(frequencyFactor * ((z / 16) + worldObj.getTotalWorldTime())) *
                sin(frequencyFactor * ((z / 16) + worldObj.getTotalWorldTime())) * cos(frequencyFactor * ((z / 16) + worldObj.getTotalWorldTime())));
        Vector3 wind = Vector3.getNewVectorFromPool().set(tempWind[0], 0, tempWind[1]);
        return wind;
    }
    
    
    private static double cos(double d) {
        return MathHelper.cos((float) d);
      }

      private static double sin(double d) {
        return MathHelper.sin((float) d);
      }

	public void setBiome(BlockPos p, int type) {
		this.setBiome(p.getX(), p.getY(), p.getZ(), type);
	}
}
