package dorfgen.conversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import dorfgen.conversion.DorfMap.Region;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class BiomeList
{

    public static HashMap<Integer, BiomeConversion> biomes    = new HashMap<Integer, BiomeConversion>();
    public static int                               FREEZING  = 67;
    public static int                               COLD      = 80;
    public static int                               TEMPERATE = 128;
    public static int                               WARM      = 155;
    public static int                               HOT       = 180;
    public static int                               SCORCHING = 255;

    public static int DRY = 100;
    public static int WET = 200;

    private static ArrayList<BiomeGenBase> biomeArray;

    public static int GetBiomeIndex(int rgb)
    {
        if (biomes.containsKey(rgb)) return biomes.get(rgb).mineCraftBiome;
        return 0;
    }

    public static int getBiomeFromValues(int biome, int temperature, int drainage, int rainfall, int evil,
            Region region)
    {
        BiomeGenBase b = BiomeGenBase.getBiome(biome);

        if (temperature < TEMPERATE && !BiomeDictionary.isBiomeOfType(b, Type.COLD))
        {
            boolean freezing = temperature < FREEZING;
            boolean matched = false;
            if (freezing && (BiomeDictionary.isBiomeOfType(b, Type.OCEAN)
                    || BiomeDictionary.isBiomeOfType(b, Type.RIVER) || BiomeDictionary.isBiomeOfType(b, Type.BEACH)))
            {
                BiomeGenBase temp = getMatch(b, Type.SNOWY);
                if (temp != b)
                {
                    b = temp;
                    matched = true;
                }
                if (!matched)
                {
                    b = getMatch(b, Type.COLD);
                }
            }
            else if (b != BiomeGenBase.river)
            {
                // b = getMatch(b, Type.COLD);
            }
        }

        // if(true)
        return b.biomeID;
        // //TODO finish this
        //
        // if(temperature > WARM && !BiomeDictionary.isBiomeOfType(b, Type.HOT))
        // {
        // b = getMatch(b, Type.HOT);
        // }
        // if(rainfall > WET && !BiomeDictionary.isBiomeOfType(b, Type.WET))
        // {
        // b = getMatch(b, Type.WET);
        // }
        // if(rainfall < DRY && !BiomeDictionary.isBiomeOfType(b, Type.DRY))
        // {
        // b = getMatch(b, Type.DRY);
        // }
        // int newBiome = b.biomeID;
        // if(region!=null)
        // {
        // if(region.type==RegionType.GLACIER &&
        // BiomeDictionary.isBiomeOfType(b, Type.PLAINS))
        // {
        // boolean cold = BiomeDictionary.isBiomeOfType(b, Type.COLD) ||
        // BiomeDictionary.isBiomeOfType(b, Type.SNOWY);
        // if(!cold)
        // {
        // b = getMatch(BiomeGenBase.icePlains, Type.SNOWY);
        // }
        // }
        // if(region.biomeMap.containsKey(biome))
        // {
        // newBiome = region.biomeMap.get(biome);
        // }
        // else
        // {
        // region.biomeMap.put(biome, newBiome);
        // }
        // }
        //
        // return newBiome;
    }

    private static Random rand = new Random(1234);

    private static BiomeGenBase getMatch(BiomeGenBase toMatch, Type type)
    {
        if (biomeArray == null)
        {
            biomeArray = new ArrayList<BiomeGenBase>();
            for (BiomeGenBase b : BiomeGenBase.getBiomeGenArray())
            {
                if (b != null) biomeArray.add(b);
            }
        }
        Type[] existing = BiomeDictionary.getTypesForBiome(toMatch);
        int i = rand.nextInt(123456);
        biomes:
        for (int j = 0; j < biomeArray.size(); j++)
        {
            BiomeGenBase b = biomeArray.get((j + i) % biomeArray.size());
            if (b != toMatch && b != null)
            {
                if (!BiomeDictionary.isBiomeOfType(b, type)) continue;
                for (Type t : existing)
                {
                    if (!BiomeDictionary.isBiomeOfType(b, t)) continue biomes;
                }
                return b;
            }
        }
        return toMatch;
    }

}
