package dorfgen.worldgen;

import static dorfgen.WorldGenerator.scale;

import java.util.List;

import dorfgen.WorldGenerator;
import dorfgen.conversion.DorfMap;
import dorfgen.conversion.Interpolator.BicubicInterpolator;
import dorfgen.conversion.Interpolator.CachedBicubicInterpolator;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.structure.MapGenVillage;

public class WorldChunkManagerFinite extends WorldChunkManager
{
    /** The biomes that are used to generate the chunk */
    private BiomeGenBase[]           biomesForGeneration = new BiomeGenBase[256];
    public BicubicInterpolator       biomeInterpolator   = new BicubicInterpolator();
    public CachedBicubicInterpolator heightInterpolator  = new CachedBicubicInterpolator();
    public CachedBicubicInterpolator miscInterpolator    = new CachedBicubicInterpolator();

    public WorldChunkManagerFinite()
    {
        super();
    }

    public WorldChunkManagerFinite(World world)
    {
        super(world);
    }

    @Override
    /** Returns the BiomeGenBase related to the x, z position on the world. */
    public BiomeGenBase getBiomeGenerator(BlockPos pos)
    {
        try
        {
            return super.getBiomeGenerator(pos);
        }
        catch (Exception e)
        {
            System.out.println(pos);
            e.printStackTrace();
        }
        return BiomeGenBase.ocean;
    }

    @Override
    /** Returns an array of biomes for the location input. */
    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] biomes, int x, int z, int width, int length)
    {
        biomes = super.getBiomesForGeneration(biomes, x, z, width, length);
        if (x >= 0 && z >= 0 && (x + 16) / scale <= WorldGenerator.instance.dorfs.biomeMap.length
                && (z + 16) / scale <= WorldGenerator.instance.dorfs.biomeMap[0].length)
        {
            makeBiomes(biomes, scale, x, z);
        }

        return biomes;
    }

    @Override
    /** Returns biomes to use for the blocks and loads the other data like
     * temperature and humidity onto the WorldChunkManager Args: oldBiomeList,
     * x, z, width, depth */
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] biomes, int x, int z, int width, int depth)
    {
        biomes = super.getBiomesForGeneration(biomes, x, z, width, depth);
        x -= WorldGenerator.shift.getX();
        z -= WorldGenerator.shift.getZ();

        if (x >= 0 && z >= 0 && (x + 16) / scale <= WorldGenerator.instance.dorfs.biomeMap.length
                && (z + 16) / scale <= WorldGenerator.instance.dorfs.biomeMap[0].length)
        {

            x += WorldGenerator.shift.getX();
            z += WorldGenerator.shift.getZ();

            return biomes = makeBiomes(biomes, scale, x, z);
        }
        return biomes;
    }

    /** Takes Blocks Coordinates
     * 
     * @param scale
     *            - number of blocks per pixel
     * @param x
     *            - x coordinate of the block being used
     * @param z
     *            - y coordinate of the block being used
     * @param blocks */
    private BiomeGenBase[] makeBiomes(BiomeGenBase[] biomes, int scale, int x, int z)
    {
        int index;
        for (int i1 = 0; i1 < 16; i1++)
        {
            for (int k1 = 0; k1 < 16; k1++)
            {
                index = (i1) + (k1) * 16;
                biomes[index] = BiomeGenBase.getBiome(
                        getBiomeFromMaps(x + i1 - WorldGenerator.shift.getX(), z + k1 - WorldGenerator.shift.getZ()));
            }
        }
        return biomes;
    }

    private int getBiomeFromMaps(int x, int z)
    {
        DorfMap dorfs = WorldGenerator.instance.dorfs;

        int b1 = biomeInterpolator.interpolateBiome(dorfs.biomeMap, x, z, scale);
        if (dorfs.riverMap.length > 0)
        {
            int r1 = miscInterpolator.interpolateHeight(scale, x, z, dorfs.riverMap);
            if (r1 > 0)
            {
                b1 = BiomeGenBase.river.biomeID;
            }
        }

        boolean hasHeightmap = dorfs.elevationMap.length > 0;
        boolean hasThermalMap = dorfs.temperatureMap.length > 0;

        int h1 = hasHeightmap ? heightInterpolator.interpolateHeight(scale, x, z, dorfs.elevationMap) : 64;
        int t1 = hasThermalMap ? miscInterpolator.interpolateHeight(scale, x, z, dorfs.temperatureMap) : 128;

        if (h1 > 60 && (b1 == BiomeGenBase.deepOcean.biomeID || b1 == BiomeGenBase.ocean.biomeID))
        {
            b1 = BiomeGenBase.beach.biomeID;
            if (t1 < 80)
            {
                b1 = BiomeGenBase.coldBeach.biomeID;
            }
        }
        else if (h1 > 45 && (b1 == BiomeGenBase.deepOcean.biomeID || b1 == BiomeGenBase.ocean.biomeID))
        {
            b1 = BiomeGenBase.ocean.biomeID;
            if (t1 < 65)
            {
                b1 = BiomeGenBase.frozenOcean.biomeID;
            }
        }
        else if (h1 <= 45 && (b1 == BiomeGenBase.ocean.biomeID))
        {
            b1 = BiomeGenBase.deepOcean.biomeID;
        }

        return b1;
    }

    @Override
    /** checks given Chunk's Biomes against List of allowed ones */
    public boolean areBiomesViable(int p1_, int p2_, int p3_, List<BiomeGenBase> biomes)
    {
        if (biomes == MapGenVillage.villageSpawnBiomes) return true;

        biomesForGeneration = getBiomesForGeneration(biomesForGeneration, p1_, p2_, 0, 0);
        for (BiomeGenBase b : biomesForGeneration)
        {
            if (b != null)
            {
                if (biomes.contains(b)) return true;
            }
        }
        return false;
    }
}
