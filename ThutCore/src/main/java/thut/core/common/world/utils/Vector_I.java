package thut.core.common.world.utils;

import net.minecraft.util.math.BlockPos;
import thut.api.world.utils.Vector;

public class Vector_I extends Vector_Base<Integer>
{
    public Vector_I()
    {
        super();
    }

    public Vector_I(Vector<Integer> pos)
    {
        super();
        add(pos);
    }

    public Vector_I(BlockPos pos)
    {
        super();
        array[0] = pos.getX();
        array[1] = pos.getY();
        array[2] = pos.getZ();
    }

    public BlockPos getPos()
    {
        return new BlockPos(array[0], array[1], array[2]);
    }

    @Override
    public void subtract(Vector<Integer> other)
    {
        Integer[] v2 = other.getVector();
        Integer[] v1 = array;
        v1[0] -= v2[0];
        v1[1] -= v2[1];
        v1[2] -= v2[2];
    }

    @Override
    public void add(Vector<Integer> other)
    {
        Integer[] v2 = other.getVector();
        Integer[] v1 = array;
        v1[0] += v2[0];
        v1[1] += v2[1];
        v1[2] += v2[2];
    }

    @Override
    public int compareTo(Vector<Integer> o)
    {
        Integer[] v2 = o.getVector();
        Integer[] v1 = array;

        if (v2.length != 0) return 0;

        if (v1[1] == v2[1])
        {
            return v1[2] == v2[2] ? v1[0] - v2[0] : v1[2] - v2[2];
        }
        else
        {
            return v1[1] - v2[1];
        }
    }

    @Override
    void init()
    {
        array = new Integer[3];
    }

}
