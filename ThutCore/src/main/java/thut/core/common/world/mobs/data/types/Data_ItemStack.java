package thut.core.common.world.mobs.data.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class Data_ItemStack extends Data_Base<ItemStack>
{
    ItemStack value = ItemStack.EMPTY;

    public Data_ItemStack()
    {
        initLast(value);
    }

    @Override
    protected boolean isDifferent(ItemStack last, ItemStack value)
    {
        return !ItemStack.areItemStacksEqual(last, value);
    }

    @Override
    public void set(ItemStack value)
    {
        if (value.isEmpty())
        {
            if (this.value.isEmpty()) return;
            this.value = ItemStack.EMPTY;
            return;
        }
        if (value.equals(this.value)) return;
        this.value = value;
    }

    @Override
    public ItemStack get()
    {
        return this.value;
    }

    @Override
    public void write(ByteBuf buf)
    {
        super.write(buf);
        PacketBuffer wrapped = new PacketBuffer(Unpooled.buffer(0));
        wrapped.writeItemStack(value);
        int num = wrapped.readableBytes();
        buf.writeInt(num);
        buf.writeBytes(wrapped);

    }

    @Override
    public void read(ByteBuf buf)
    {
        super.read(buf);
        int num = buf.readInt();
        PacketBuffer wrapped = new PacketBuffer(Unpooled.buffer(0));
        byte[] dst = new byte[num];
        buf.readBytes(dst);
        try
        {
            wrapped.writeBytes(dst);
            value = wrapped.readItemStack();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
