package thut.api.pathing;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IPathFinder
{

    Path createEntityPathTo(IBlockAccess blockaccess, Entity entityIn, BlockPos targetPos, float dist);
    
    Path createEntityPathTo(IBlockAccess blockaccess, Entity entityFrom, Entity entityTo, float dist);
}
