package thut.api.terrain;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ITerrainProvider
{
    default TerrainSegment getTerrain(World world, BlockPos p)
    {
        return world.getChunkFromBlockCoords(p).getCapability(CapabilityTerrain.TERRAIN_CAP, null)
                .getTerrainSegement(p);
    }
}
