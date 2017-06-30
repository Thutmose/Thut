package thut.core.common.terrain;

import static thut.api.terrain.TerrainSegment.GRIDSIZE;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainSegment;
import thut.api.terrain.TerrainSegment.ISubBiomeChecker;

public class ConfigTerrainChecker implements ISubBiomeChecker
{
    private final List<Predicate<IBlockState>> list;
    private final BiomeType                    subbiome;

    public ConfigTerrainChecker(List<Predicate<IBlockState>> list, BiomeType subbiome)
    {
        this.list = list;
        this.subbiome = subbiome;
    }

    private boolean apply(IBlockState state)
    {
        if (state.getMaterial() == Material.AIR) return false;
        for (Predicate<IBlockState> predicate : list)
        {
            if (predicate.apply(state)) return true;
        }
        return false;
    }

    @Override
    public int getSubBiome(World world, Vector3 v, TerrainSegment segment, Chunk chunk, boolean caveAdjusted)
    {
        if (caveAdjusted)
        {
            Vector3 temp1 = Vector3.getNewVector();
            int x0 = segment.chunkX * 16, y0 = segment.chunkY * 16, z0 = segment.chunkZ * 16;
            int dx = ((v.intX() - x0) / GRIDSIZE) * GRIDSIZE;
            int dy = ((v.intY() - y0) / GRIDSIZE) * GRIDSIZE;
            int dz = ((v.intZ() - z0) / GRIDSIZE) * GRIDSIZE;
            int x1 = x0 + dx, y1 = y0 + dy, z1 = z0 + dz;
            for (int i = x1; i < x1 + GRIDSIZE; i++)
                for (int j = y1; j < y1 + GRIDSIZE; j++)
                    for (int k = z1; k < z1 + GRIDSIZE; k++)
                    {
                        temp1.set(i, j, k);
                        if (apply(temp1.getBlockState(world))) return subbiome.getType();
                    }
        }
        return -1;
    }

}
