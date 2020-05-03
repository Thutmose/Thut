package dorfgen.world.gen;

import java.util.Collections;

import dorfgen.Dorfgen;
import dorfgen.conversion.BiomeConversion;
import dorfgen.conversion.DorfMap;
import dorfgen.conversion.Interpolator.BicubicInterpolator;
import dorfgen.conversion.Interpolator.CachedBicubicInterpolator;
import dorfgen.world.feature.BeachMaker;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.IChunk;

public class DorfBiomeProvider extends BiomeProvider
{
    public static int getBiomeContainerIndex(final int worldX, final int worldY, final int worldZ)
    {
        final int i = worldX & BiomeContainer.HORIZONTAL_MASK;
        final int j = MathHelper.clamp(worldY, 0, BiomeContainer.VERTICAL_MASK);
        final int k = worldZ & BiomeContainer.HORIZONTAL_MASK;
        return j << BiomeContainer.WIDTH_BITS + BiomeContainer.WIDTH_BITS | k << BiomeContainer.WIDTH_BITS | i;
    }

    public static void setBiome(final IChunk c, final int worldX, final int worldY, final int worldZ, final Biome b)
    {

    }

    public BicubicInterpolator       biomeInterpolator  = new BicubicInterpolator();
    public CachedBicubicInterpolator heightInterpolator = new CachedBicubicInterpolator();
    public CachedBicubicInterpolator miscInterpolator   = new CachedBicubicInterpolator();

    int            scale;
    public boolean forGen = false;
    public DorfMap dorfs;

    protected DorfBiomeProvider(final DorfSettings settings)
    {
        super(Collections.emptySet());
    }

    private Biome getBiomeFromMaps(final int x, final int z)
    {
        final int b1 = this.biomeInterpolator.interpolateBiome(this.dorfs.biomeMap, x, z, this.scale);
        Biome biome = null;
        if (this.dorfs.riverMap.length > 0)
        {
            final int r1 = this.miscInterpolator.interpolateHeight(this.scale, x, z, this.dorfs.riverMap);
            if (r1 > 0) biome = Biomes.RIVER;
        }
        if (biome == null) biome = BiomeConversion.getBiome(b1);

        final boolean hasHeightmap = this.dorfs.elevationMap.length > 0;
        final boolean hasThermalMap = this.dorfs.temperatureMap.length > 0;

        final int h1 = hasHeightmap ? this.heightInterpolator.interpolateHeight(this.scale, x, z,
                this.dorfs.elevationMap) : 64;
        final int t1 = hasThermalMap ? this.miscInterpolator.interpolateHeight(this.scale, x, z,
                this.dorfs.temperatureMap) : 128;

        final boolean beachOrOcean = BeachMaker.isBeachOrOcean(biome);
        if (h1 > 60 && beachOrOcean)
        {
            biome = Biomes.BEACH;
            if (t1 < 100) biome = Biomes.STONE_SHORE;
            if (t1 < 80) biome = Biomes.SNOWY_BEACH;
        }
        else if (h1 > 45 && beachOrOcean)
        {
            biome = Biomes.WARM_OCEAN;
            if (t1 < 160) biome = Biomes.LUKEWARM_OCEAN;
            if (t1 < 100) biome = Biomes.OCEAN;
            if (t1 < 80) biome = Biomes.COLD_OCEAN;
            if (t1 < 50) biome = Biomes.FROZEN_OCEAN;
        }
        else if (h1 <= 45 && beachOrOcean)
        {
            biome = Biomes.DEEP_WARM_OCEAN;
            if (t1 < 160) biome = Biomes.DEEP_LUKEWARM_OCEAN;
            if (t1 < 100) biome = Biomes.DEEP_OCEAN;
            if (t1 < 80) biome = Biomes.DEEP_COLD_OCEAN;
            if (t1 < 50) biome = Biomes.DEEP_FROZEN_OCEAN;
        }
        return biome;
    }

    @Override
    public Biome getNoiseBiome(int x, final int y, int z)
    {
        final int[][] map = this.dorfs.biomeMap;

        // This accounts for the effects of the biome magnifier during worldgen
        if (this.forGen)
        {
            x = (x << 2) + (x & 2);
            z = (z << 2) + (z & 2);
        }
        final int imgX = x - Dorfgen.shift.getX();
        final int imgZ = z - Dorfgen.shift.getZ();
        this.scale = this.dorfs.scale;
        // Just do void for now if out of range, maybe do ocean later or
        // something?
        if (imgX < 0 || (imgX + 16) / this.scale >= map.length) return Biomes.DEFAULT;
        if (imgZ < 0 || (imgZ + 16) / this.scale >= map[0].length) return Biomes.DEFAULT;
        return this.getBiomeFromMaps(imgX, imgZ);
    }

}
