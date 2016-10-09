package thut.api.pathing;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.Path;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thut.api.TickHandler;

public class Paths
{
    public final World  world;
    public IBlockAccess chunks;

    public Paths(World world)
    {
        this.world = world;
    }

    public Path getEntityPathToXYZ(Entity entity, int x, int y, int z, float distance)
    {
        chunks = TickHandler.getInstance().getWorldCache(entity.dimension);
        if (chunks == null) return null;
        ThutPathFinder pather = new JPSPather(chunks, (IPathingMob) entity);
        Path path = pather.createEntityPathTo(entity, x, y, z, distance);
        return path;
    }

    public Path getPathHeapToEntity(Entity entityA, Entity entityB, float distance)
    {
        chunks = TickHandler.getInstance().getWorldCache(entityA.dimension);
        if (chunks == null) return null;
        Path path = (new JPSPather(chunks, (IPathingMob) entityA)).createEntityPathTo(chunks, entityA, entityB,
                distance);
        return path;
    }
}
