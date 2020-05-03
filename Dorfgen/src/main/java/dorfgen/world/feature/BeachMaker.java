package dorfgen.world.feature;

import dorfgen.Dorfgen;
import dorfgen.conversion.DorfMap;
import dorfgen.world.gen.DorfBiomeProvider;
import dorfgen.world.gen.DorfSettings;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.chunk.IChunk;

public class BeachMaker
{
    private final DorfMap           dorfs;
    private final DorfBiomeProvider biomes;

    public BeachMaker(final IWorld world, final DorfBiomeProvider biomes)
    {
        this.dorfs = Dorfgen.instance.getDorfs(world);
        this.biomes = biomes;
    }

    /**
     * Takes Blocks Coordinates
     *
     * @param scale
     *            - number of blocks per pixel
     * @param x
     *            - x coordinate of the pixel being used
     * @param z
     *            - y coordinate of the pixel being used
     * @param blocks
     */
    public void makeBeaches(final int seaLevel, final DorfSettings settings, final IChunk blocks, final Mutable pos)
    {
        if (blocks.getBiomes() == null) return;
        final int scale = this.dorfs.scale;
        final int chunkX = blocks.getPos().x;
        final int chunkZ = blocks.getPos().z;
        final int x0 = chunkX << 4;
        final int z0 = chunkZ << 4;
        final int x = x0 - Dorfgen.shift.getX();
        final int z = z0 - Dorfgen.shift.getZ();
        int x1, z1, h1;
        for (int dx = 0; dx < 16; dx++)
            for (int dz = 0; dz < 16; dz++)
            {
                x1 = (x + dx) / scale;
                z1 = (z + dz) / scale;
                if (x1 >= this.dorfs.elevationMap.length || z1 >= this.dorfs.elevationMap[0].length || x1 < 0 || z1 < 0)
                    h1 = 10;
                else h1 = this.dorfs.elevationMap[x1][z1];

                final Biome b1 = this.biomes.getNoiseBiome(x0 + dx, h1, z0 + dz);

                boolean beach = false;

                if (BeachMaker.isBeachOrOcean(b1)) for (int y = 100; y > 10; y--)
                {
                    pos.setPos(dx, y, dz);
                    if (blocks.getBlockState(pos) == settings.getDefaultFluid())
                    {
                        h1 = y;
                        beach = true;
                        break;
                    }
                }
                if (beach) for (int j = h1 + 1; j < seaLevel; j++)
                {
                    pos.setPos(dx, j, dz);
                    blocks.setBlockState(pos, settings.getDefaultFluid(), false);
                }
            }
    }

    public static boolean isBeachOrOcean(final Biome b1)
    {
        final Category cat = b1.getCategory();
        return cat == Category.BEACH || cat == Category.OCEAN;
    }

    public static boolean isOcean(final Biome b1)
    {
        final Category cat = b1.getCategory();
        return cat == Category.OCEAN;
    }
}
