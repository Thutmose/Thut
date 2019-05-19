package thut.core.common.world.blocks;

import java.util.UUID;

import thut.api.world.World;
import thut.api.world.blocks.Block;
import thut.api.world.utils.Info;
import thut.api.world.utils.Vector;
import thut.core.common.world.utils.Vector_I;

public class Block_Impl implements Block
{
    World    world;
    Vector_I position;
    UUID     id   = null;
    Info     info = null;

    public Block_Impl(Vector_I pos)
    {
        this.position = pos;
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
    public Vector<Integer> position()
    {
        return position;
    }

    @Override
    public Info info()
    {
        return info;
    }

    @Override
    public UUID id()
    {
        return id;
    }

    @Override
    public String key()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
