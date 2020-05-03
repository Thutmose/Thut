package dorfgen.conversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import dorfgen.conversion.DorfMap.Region;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class BiomeList
{

    public static HashMap<Integer, BiomeConversion> biomes    = new HashMap<>();
    public static int                               FREEZING  = 67;
    public static int                               COLD      = 80;
    public static int                               TEMPERATE = 128;
    public static int                               WARM      = 155;
    public static int                               HOT       = 180;
    public static int                               SCORCHING = 255;

    public static int DRY = 100;
    public static int WET = 200;

    private static ArrayList<Biome> biomeArray;

    public static int GetBiomeIndex(final int rgb)
    {
        if (BiomeList.biomes.containsKey(rgb)) return BiomeList.biomes.get(rgb).mineCraftBiome;
        return 0;
    }

    public static int getBiomeFromValues(final int biome, final int temperature, final int drainage, final int rainfall,
            final int evil, final Region region)
    {
        Biome b = BiomeConversion.getBiome(biome);

        if (temperature < BiomeList.TEMPERATE && !BiomeDictionary.hasType(b, Type.COLD))
        {
            final boolean freezing = temperature < BiomeList.FREEZING;
            boolean matched = false;
            if (freezing && (BiomeDictionary.hasType(b, Type.OCEAN) || BiomeDictionary.hasType(b, Type.RIVER)
                    || BiomeDictionary.hasType(b, Type.BEACH)))
            {
                final Biome temp = BiomeList.getMatch(b, Type.SNOWY);
                if (temp != b)
                {
                    b = temp;
                    matched = true;
                }
                if (!matched) b = BiomeList.getMatch(b, Type.COLD);
            }
            else if (b != Biomes.RIVER)
            {
                // b = getMatch(b, Type.COLD);
            }
        }

        // if(true)
        return BiomeConversion.getBiomeID(b);
        // //TODO finish this
        //
        // if(temperature > WARM && !BiomeDictionary.hasType(b, Type.HOT))
        // {
        // b = getMatch(b, Type.HOT);
        // }
        // if(rainfall > WET && !BiomeDictionary.hasType(b, Type.WET))
        // {
        // b = getMatch(b, Type.WET);
        // }
        // if(rainfall < DRY && !BiomeDictionary.hasType(b, Type.DRY))
        // {
        // b = getMatch(b, Type.DRY);
        // }
        // int newBiome = b.biomeID;
        // if(region!=null)
        // {
        // if(region.type==RegionType.GLACIER &&
        // BiomeDictionary.hasType(b, Type.PLAINS))
        // {
        // boolean cold = BiomeDictionary.hasType(b, Type.COLD) ||
        // BiomeDictionary.hasType(b, Type.SNOWY);
        // if(!cold)
        // {
        // b = getMatch(Biome.icePlains, Type.SNOWY);
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

    private static Biome getMatch(final Biome toMatch, final Type type)
    {
        if (BiomeList.biomeArray == null)
        {
            BiomeList.biomeArray = new ArrayList<>();
            for (final Biome b : Biome.BIOMES)
                if (b != null) BiomeList.biomeArray.add(b);
        }
        final Set<Type> existing = BiomeDictionary.getTypes(toMatch);
        final int i = BiomeList.rand.nextInt(123456);
        biomes:
        for (int j = 0; j < BiomeList.biomeArray.size(); j++)
        {
            final Biome b = BiomeList.biomeArray.get((j + i) % BiomeList.biomeArray.size());
            if (b != toMatch && b != null)
            {
                if (!BiomeDictionary.hasType(b, type)) continue;
                for (final Type t : existing)
                    if (!BiomeDictionary.hasType(b, t)) continue biomes;
                return b;
            }
        }
        return toMatch;
    }

}
