package thut.core.common.world.mobs.data.types;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class Data_ItemStack extends Data_Base<ItemStack>
{
    ItemStack value = ItemStack.EMPTY;

    @Override
    public void set(ItemStack value)
    {
        if (value == null)
        {
            if (this.value == ItemStack.EMPTY) return;
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
        buf.writeBytes(wrapped);
    }

    @Override
    public void read(ByteBuf buf)
    {
        super.read(buf);
        PacketBuffer wrapped = new PacketBuffer(Unpooled.buffer(0));
        wrapped.writeBytes(buf);
        try
        {
            value = wrapped.readItemStack();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
