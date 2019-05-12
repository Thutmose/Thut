package thut.api.world.utils;

/** @author Thutmose
 * @param <T>
 *            The type for this vector, this should be a number type, like
 *            Double, Float, Int, etc */
public interface Vector<T extends Number> extends Comparable<Vector<T>>
{
    /** This gets the entire vector as an array;
     * 
     * @return */
    T[] getVector();

    /** This should return the size of the array representing this vector.
     * 
     * @return */
    int getDim();

    /** Subtracts the given vector from us.
     * 
     * @param other */
    void subtract(Vector<T> other);

    /** adds the given vector from us.
     * 
     * @param other */
    void add(Vector<T> other);

    default double dot(Vector<T> other)
    {
        if (other.getDim() != getDim()) throw new IllegalArgumentException("must be same dimensionality to dot.");
        double l = 0;
        T[] others = other.getVector();
        T[] ours = getVector();
        for (int i = 0; i < ours.length; i++)
        {
            double val1 = others[i].doubleValue();
            double val2 = ours[i].doubleValue();
            l += val1 * val2;
        }
        return l;
    }

    default double normSq()
    {
        return dot(this);
    }

    default double norm()
    {
        return Math.sqrt(normSq());
    }

    /** This sets the value at the given index.
     * 
     * @param index
     * @param value
     * @return this vector */
    default Vector<T> setValue(int index, T value)
    {
        getVector()[index] = value;
        return this;
    }

    /** @param index
     * @return the value at the given index. */
    default T getValue(int index)
    {
        return getVector()[index];
    }

}
