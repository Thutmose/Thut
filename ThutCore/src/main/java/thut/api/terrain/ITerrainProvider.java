package thut.api.terrain;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ITerrainProvider
{
    default TerrainSegment getTerrain(World world, BlockPos p)
    {
        CapabilityTerrain.ITerrainProvider provider = world.getChunk(p).getCapability(CapabilityTerrain.TERRAIN_CAP)
                .orElse(null);
        return provider.getTerrainSegement(p);
    }
}
