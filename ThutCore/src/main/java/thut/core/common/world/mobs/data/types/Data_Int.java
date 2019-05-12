package thut.core.common.world.mobs.data.types;

import io.netty.buffer.ByteBuf;

public class Data_Int extends Data_Base<Integer>
{
    Integer value = 0;

    @Override
    public void set(Integer value)
    {
        if (this.value.equals(value)) return;
        if (value == null)
        {
            this.value = 0;
            return;
        }
        this.value = value;
    }

    @Override
    public Integer get()
    {
        return this.value;
    }

    @Override
    public void write(ByteBuf buf)
    {
        super.write(buf);
        buf.writeInt(value);
    }

    @Override
    public void read(ByteBuf buf)
    {
        super.read(buf);
        value = buf.readInt();
    }

}
