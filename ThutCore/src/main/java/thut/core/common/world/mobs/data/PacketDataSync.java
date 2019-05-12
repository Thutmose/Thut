package thut.core.common.world.mobs.data;

import java.util.List;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thut.api.network.PacketHandler;
import thut.api.world.mobs.data.Data;
import thut.api.world.mobs.data.DataSync;

public class PacketDataSync implements IMessage, IMessageHandler<PacketDataSync, IMessage>
{
    public int           id;
    public List<Data<?>> data = Lists.newArrayList();

    public static void sync(EntityPlayerMP syncTo, DataSync data, int entity_id, boolean all)
    {
        List<Data<?>> list = all ? data.getDirty() : data.getAll();
        // Nothing to sync.
        if (list == null) return;
        PacketDataSync packet = new PacketDataSync();
        packet.data = list;
        packet.id = entity_id;
        PacketHandler.packetPipeline.sendTo(packet, syncTo);
    }

    public PacketDataSync()
    {
    }

    @Override
    public IMessage onMessage(final PacketDataSync message, final MessageContext ctx)
    {
        EntityPlayer player;
        player = Minecraft.getMinecraft().player;
        int id = message.id;
        World world = player.getEntityWorld();
        Entity mob = world.getEntityByID(id);
        if (mob == null) return null;
        DataSync sync = SyncHandler.getData(mob);
        if (sync == null) return null;
        sync.update(message.data);
        return null;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        id = buf.readInt();
        byte num = buf.readByte();
        if (num > 0)
        {
            for (int i = 0; i < num; i++)
            {
                int uid = buf.readInt();
                try
                {
                    Data<?> val = DataSync_Impl.makeData(uid);
                    val.read(buf);
                    data.add(val);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(id);
        byte num = (byte) (data.size());
        buf.writeByte(num);
        for (int i = 0; i < num; i++)
        {
            Data<?> val = data.get(i);
            buf.writeInt(val.getUID());
            val.write(buf);
        }
    }
}
