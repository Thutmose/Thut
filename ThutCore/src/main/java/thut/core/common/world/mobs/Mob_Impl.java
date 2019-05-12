package thut.core.common.world.mobs;

import java.util.UUID;

import net.minecraft.entity.Entity;
import thut.api.world.World;
import thut.api.world.mobs.Mob;
import thut.api.world.utils.Info;
import thut.api.world.utils.Vector;
import thut.core.common.world.utils.Info_Impl;
import thut.core.common.world.utils.Vector_D;
import thut.core.common.world.utils.Vector_I;

public class Mob_Impl implements Mob
{
    World           world;
    Entity          entity;
    final Info_Impl info     = new Info_Impl();
    final Vector_I  worldPos = new Vector_I();
    final Vector_D  position = new Vector_D();
    final Vector_D  velocity = new Vector_D();

    public void setEntity(Entity entity)
    {

    }

    @Override
    public World world()
    {
        return world;
    }

    @Override
    public void setWorld(World world)
    {
        this.world = world;
    }

    @Override
    public Vector<Integer> worldPos()
    {
        position.toInts(worldPos);
        return worldPos;
    }

    @Override
    public Vector<Double> position()
    {
        return position;
    }

    @Override
    public Vector<Double> velocity()
    {
        return velocity;
    }

    @Override
    public Info info()
    {
        // TODO decide on what to do with this.
        return info;
    }

    @Override
    public UUID id()
    {
        return entity.getUniqueID();
    }

    @Override
    public void setID(UUID id)
    {
        entity.setUniqueId(id);
    }

}
