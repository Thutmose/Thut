package thut.core.common.world.mobs.data.types;

import io.netty.buffer.ByteBuf;
import thut.api.world.mobs.data.Data;

public abstract class Data_Base<T> implements Data<T>
{
    private int     ID       = -1;
    private int     UID      = -1;
    private boolean dirty    = false;
    private T       lastSent = null;

    protected void initLast(T last)
    {
        this.lastSent = last;
    }

    protected boolean isDifferent(T last, T value)
    {
        return last != null ? !last.equals(value) : value != null;
    }

    @Override
    public boolean dirty()
    {
        if (dirty) return true;
        T value = get();
        return isDifferent(lastSent, value);
    }

    @Override
    public void setDirty(boolean dirty)
    {
        if (!dirty) lastSent = this.get();
        else this.dirty = dirty;
    }

    @Override
    public void write(ByteBuf buf)
    {
        this.dirty = false;
        lastSent = this.get();
        buf.writeInt(ID);
    }

    @Override
    public void read(ByteBuf buf)
    {
        this.ID = buf.readInt();
    }

    @Override
    public int getID()
    {
        return ID;
    }

    @Override
    public void setID(int id)
    {
        this.ID = id;
    }

    @Override
    public int getUID()
    {
        return UID;
    }

    @Override
    public void setUID(int id)
    {
        this.UID = id;
    }

}
