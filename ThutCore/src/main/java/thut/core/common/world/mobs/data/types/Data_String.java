package thut.core.common.world.mobs.data.types;

import io.netty.buffer.ByteBuf;

public class Data_String extends Data_Base<String>
{
    String value = "";

    @Override
    public void set(String value)
    {
        if (this.value.equals(value)) return;
        if (value == null)
        {
            this.value = "";
            return;
        }
        this.value = value;
    }

    @Override
    public String get()
    {
        return this.value;
    }

    @Override
    public void write(ByteBuf buf)
    {
        super.write(buf);
        byte[] arr = value.getBytes();
        buf.writeInt(arr.length);
        buf.writeBytes(arr);
    }

    @Override
    public void read(ByteBuf buf)
    {
        super.read(buf);
        int len = buf.readInt();
        byte[] arr = new byte[len];
        buf.readBytes(arr);
    }

}
