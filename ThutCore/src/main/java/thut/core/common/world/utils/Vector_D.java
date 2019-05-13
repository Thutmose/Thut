package thut.core.common.world.utils;

import thut.api.world.utils.Vector;

public class Vector_D extends Vector_Base<Double>
{
    /** Returns the greatest integer less than or equal to the double
     * argument */
    public static int floor(double value)
    {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    public void toInts(Vector_I toSet)
    {
        Integer[] v2 = toSet.getVector();
        Double[] v1 = array;
        v2[0] = floor(v1[0]);
        v2[1] = floor(v1[1]);
        v2[2] = floor(v1[2]);
    }

    @Override
    public void subtract(Vector<Double> other)
    {
        Double[] v2 = other.getVector();
        Double[] v1 = array;
        v1[0] -= v2[0];
        v1[1] -= v2[1];
        v1[2] -= v2[2];
    }

    @Override
    public void add(Vector<Double> other)
    {
        Double[] v2 = other.getVector();
        Double[] v1 = array;
        v1[0] += v2[0];
        v1[1] += v2[1];
        v1[2] += v2[2];
    }

    @Override
    public int compareTo(Vector<Double> o)
    {
        // Lets see if this causes any issues?
        return (int) dot(o);
    }

    @Override
    void init()
    {
        array = new Double[3];
    }

}
