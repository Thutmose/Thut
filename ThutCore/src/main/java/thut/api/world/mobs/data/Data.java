package thut.api.world.mobs.data;

import io.netty.buffer.ByteBuf;

public interface Data<T>
{
    int getUID();

    void setUID(int id);

    int getID();

    void setID(int id);

    void set(T value);

    T get();

    boolean dirty();

    void setDirty(boolean dirty);

    void write(ByteBuf buf);

    void read(ByteBuf buf);
}
