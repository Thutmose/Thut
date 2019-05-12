package thut.api.world.mobs.data;

import java.util.List;

public interface DataSync
{
    /** This registers the given data type, the integer returned is the key for
     * this data.
     * 
     * @param data
     * @return */
    <T> int register(Data<T> data, T value);

    /** Sets the given entry to the value.
     * 
     * @param key
     * @param value */
    <T> void set(int key, T value);

    /** Gets the value for the entry.
     * 
     * @param key
     * @return */
    <T> T get(int key);

    /** Gets all entries which need to by synced.
     * 
     * @return */
    List<Data<?>> getDirty();

    /** Gets all entries.
     * 
     * @return */
    List<Data<?>> getAll();

    /** Updates the given values.
     * 
     * @param values */
    void update(List<Data<?>> values);
}
