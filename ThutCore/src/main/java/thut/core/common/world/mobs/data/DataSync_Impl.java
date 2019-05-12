package thut.core.common.world.mobs.data;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import thut.api.world.mobs.data.Data;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.world.mobs.data.types.Data_Byte;
import thut.core.common.world.mobs.data.types.Data_Float;
import thut.core.common.world.mobs.data.types.Data_Int;
import thut.core.common.world.mobs.data.types.Data_String;
import thut.core.common.world.mobs.data.types.Data_UUID;

public class DataSync_Impl implements DataSync, ICapabilityProvider
{
    public static Int2ObjectArrayMap<Class<? extends Data<?>>> REGISTRY = new Int2ObjectArrayMap<>();

    static
    {
        addMapping(Data_Byte.class);
        addMapping(Data_Int.class);
        addMapping(Data_Float.class);
        addMapping(Data_String.class);
        addMapping(Data_UUID.class);
    }

    public static void addMapping(Class<? extends Data<?>> dataType)
    {
        REGISTRY.put(REGISTRY.size(), dataType);
    }

    public static int getID(Data<?> data)
    {
        if (data.getUID() != -1) return data.getUID();
        for (Entry<Integer, Class<? extends Data<?>>> entry : REGISTRY.entrySet())
        {
            if (entry.getValue() == data.getClass())
            {
                data.setUID(entry.getKey());
                return data.getUID();
            }
        }
        throw new NullPointerException("Datatype not found for " + data);
    }

    @SuppressWarnings("unchecked")
    public static <T> T makeData(int id) throws InstantiationException, IllegalAccessException
    {
        Class<? extends Data<?>> dataType = REGISTRY.get(id);
        if (dataType == null) throw new NullPointerException("No type registered for ID: " + id);
        return (T) dataType.newInstance();
    }

    public Int2ObjectArrayMap<Data<?>> data = new Int2ObjectArrayMap<>();
    private final ReadWriteLock        lock = new ReentrantReadWriteLock();

    @Override
    public <T> int register(Data<T> data, T value)
    {
        data.set(value);
        int id = this.data.size();
        data.setID(id);
        // Initialize the UID for this data.
        getID(data);
        this.data.put(id, data);
        return id;
    }

    @Override
    public <T> void set(int key, T value)
    {
        lock.writeLock().lock();
        @SuppressWarnings("unchecked")
        Data<T> type = (Data<T>) data.get(key);
        type.set(value);
        lock.writeLock().unlock();
    }

    @Override
    public <T> T get(int key)
    {
        lock.readLock().lock();
        @SuppressWarnings("unchecked")
        Data<T> value = (Data<T>) data.get(key);
        lock.readLock().unlock();
        return value.get();
    }

    @Override
    public List<Data<?>> getDirty()
    {
        List<Data<?>> list = null;
        lock.readLock().lock();
        for (Data<?> value : data.values())
        {
            if (value.dirty())
            {
                if (list == null) list = Lists.newArrayList();
                list.add(value);
            }
        }
        lock.readLock().unlock();
        return list;
    }

    @Override
    public List<Data<?>> getAll()
    {
        List<Data<?>> list = null;
        lock.readLock().lock();
        for (Data<?> value : data.values())
        {
            if (list == null) list = Lists.newArrayList();
            list.add(value);
        }
        lock.readLock().unlock();
        return list;
    }

    @Override
    public void update(List<Data<?>> values)
    {
        lock.writeLock().lock();
        for (Data<?> value : values)
        {
            this.data.put(value.getID(), value);
        }
        lock.writeLock().unlock();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        return capability == SyncHandler.CAP;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        return hasCapability(capability, facing) ? SyncHandler.CAP.cast(this) : null;
    }

}
