package thut.core.common.world;

import java.util.UUID;

import thut.api.world.World;
import thut.api.world.blocks.Block;
import thut.api.world.mobs.Mob;
import thut.api.world.utils.Vector;
import thut.core.common.world.blocks.Block_Impl;
import thut.core.common.world.utils.Vector_I;

public class World_Impl implements World
{
    net.minecraft.world.World wrapped;

    public World_Impl(net.minecraft.world.World wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public Block getBlock(Vector<Integer> position)
    {
        Vector_I pos = new Vector_I(position);
        // TODO consider caching this and cleanup stuff?
        // Maybe store these in a chunk capability instead.
        Block block = new Block_Impl(pos);
        
        return block;
    }

    @Override
    public boolean addMob(Mob mob)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeMob(Mob mob)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Mob getMob(UUID id)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
