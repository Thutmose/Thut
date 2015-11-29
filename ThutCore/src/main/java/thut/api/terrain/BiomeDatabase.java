package thut.api.terrain;

import java.util.HashMap;

import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import thut.api.maths.Vector3;

public class BiomeDatabase 
{
	public static HashMap<BiomeGenBase, Type[]> biomeTypes = new HashMap<BiomeGenBase, Type[]>();
	
	public static BiomeType getBiome(BiomeGenBase b)
	{
		if(b!=null)
		{
			if(b.biomeName.toLowerCase().contains("flower"))
				return BiomeType.FLOWER;
		}
		
		return BiomeType.NONE;//("none");
	}
	
	public static boolean contains(BiomeGenBase b, Type type)
	{
		boolean ret = false;
		if(b==null) return ret;
		
		Type[] arr = biomeTypes.get(b);
		if(arr==null)
		{
			arr = BiomeDictionary.getTypesForBiome(b);
			biomeTypes.put(b, arr);
		}
		
		for(Type t: arr )
		{
			ret = ret || t.equals(type);
			if(ret) break;
		}
		return ret;
	}
	
	public static String getBiome(World world, Vector3 v)
	{
		int type = v.getBiomeID(world);
		return getNameFromType(type);
	}

	public static String getBiome(World world, Vector3 v, boolean checkIndandVillage)
	{
		String ret = "";
		
		if(checkIndandVillage)
		{
	        Village village = world.villageCollectionObj.getNearestVillage(new BlockPos(
	        		MathHelper.floor_double(v.intX()), 
	        		MathHelper.floor_double(v.intY()), 
	        		MathHelper.floor_double(v.intZ())), 2);
	        if(village!=null)
	        	return "village";
		}
		BiomeGenBase biome = v.getBiome(world);
		ret = getBiome(biome).name;
		
		return ret;
	}
	
	public static int getBiomeType(BiomeGenBase biome)
	{
		return biome.biomeID;
	}
	
	public static int getBiomeType(String name)
	{
		for(BiomeType b: BiomeType.values())
		{
			if(b.name.equalsIgnoreCase(name))
				return (byte) b.ordinal();
		}
		for(BiomeGenBase b: BiomeGenBase.getBiomeGenArray())
		{
			if(b!=null)
			if(b.biomeName.equalsIgnoreCase(name))
				return b.biomeID;
		}
		
		return BiomeType.NONE.getType();
	}
	
	public static String getNameFromType(int type)
	{
		if(type>255)
			return BiomeType.values()[type-256].name;
		else if(BiomeGenBase.getBiome(type)!=null)
			return BiomeGenBase.getBiome(type).biomeName;
		else
			return "none";
	}
	
	public static String getReadableNameFromType(int type)
	{
		if(type>255)
			return BiomeType.values()[type-256].readableName;
		else if(BiomeGenBase.getBiome(type)!=null)
			return BiomeGenBase.getBiome(type).biomeName;
		else
			return "None "+type;
	}
	
}