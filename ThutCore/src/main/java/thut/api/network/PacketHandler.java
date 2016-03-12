package thut.api.network;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import thut.api.maths.Cruncher;
import thut.api.maths.Vector3;
import thut.api.network.PacketHandler.MessageClient.MessageHandlerClient;
import thut.api.network.PacketHandler.MessageServer.MessageHandlerServer;

public class PacketHandler
{
    public static class MessageClient implements IMessage
    {
        public static class MessageHandlerClient implements IMessageHandler<MessageClient, MessageServer>
        {
            public void handleClientSide(EntityPlayer player, PacketBuffer buffer)
            {
                byte channel = buffer.readByte();

                if (player == null)
                {
                    new NullPointerException("Packet recieved by null player").printStackTrace();
                    return;
                }
                if (channel == BLASTAFFECTED)
                {
                    try
                    {
                        NBTTagCompound nbt = buffer.readNBTTagCompoundFromBuffer();
                        int[] mid = nbt.getIntArray("mid");
                        int[] affected = nbt.getIntArray("affected");
                        List<Integer> locs = Lists.newArrayList();
                        for (int i : affected)
                        {
                            locs.add(i);
                        }
                        new ParticleTicker(player.dimension, locs, mid);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                if (channel == TELEPORTID)
                {
                    int id = buffer.readInt();
                    if (player.worldObj.getEntityByID(id) != null) player.worldObj.getEntityByID(id).setDead();
                }
            }

            @Override
            public MessageServer onMessage(MessageClient message, MessageContext ctx)
            {
                handleClientSide(provider.getPlayer(), message.buffer);
                return null;
            }
        }
        public static final byte BLASTAFFECTED = 1;
        public static final byte TELEPORTID    = 2;

        PacketBuffer             buffer;

        public MessageClient()
        {
        }

        public MessageClient(byte channel, NBTTagCompound nbt)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeByte(channel);
            buffer.writeNBTTagCompoundToBuffer(nbt);
        }

        public MessageClient(byte[] data)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeBytes(data);
        }

        public MessageClient(PacketBuffer buffer)
        {
            this.buffer = buffer;
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            if (buffer == null)
            {
                buffer = new PacketBuffer(Unpooled.buffer());
            }
            buffer.writeBytes(buf);
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            if (buffer == null)
            {
                buffer = new PacketBuffer(Unpooled.buffer());
            }
            buf.writeBytes(buffer);
        }
    }
    public static class MessageServer implements IMessage
    {
        public static class MessageHandlerServer implements IMessageHandler<MessageServer, IMessage>
        {
            public void handleServerSide(EntityPlayer player, PacketBuffer buffer)
            {

            }

            @Override
            public IMessage onMessage(MessageServer message, MessageContext ctx)
            {
                EntityPlayer player = ctx.getServerHandler().playerEntity;
                handleServerSide(player, message.buffer);
                return null;
            }

        }

        PacketBuffer buffer;;

        public MessageServer()
        {
        }

        public MessageServer(byte channel, NBTTagCompound nbt)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeByte(channel);
            buffer.writeNBTTagCompoundToBuffer(nbt);
        }

        public MessageServer(byte[] data)
        {
            this.buffer = new PacketBuffer(Unpooled.buffer());
            buffer.writeBytes(data);
        }

        public MessageServer(PacketBuffer buffer)
        {
            this.buffer = buffer;
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            if (buffer == null)
            {
                buffer = new PacketBuffer(Unpooled.buffer());
            }
            buffer.writeBytes(buf);
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            if (buffer == null)
            {
                buffer = new PacketBuffer(Unpooled.buffer());
            }
            buf.writeBytes(buffer);
        }

    }

    public static class ParticleTicker
    {
        public final int           dimension;
        public final List<Integer> locs;
        public final int[]         mid;

        public ParticleTicker(int dimension, List<Integer> locs, int[] mid)
        {
            this.dimension = dimension;
            this.locs = locs;
            this.mid = mid;
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void tick(ClientTickEvent evt)
        {
            MinecraftForge.EVENT_BUS.unregister(this);
            Vector3 vTemp = Vector3.getNewVector();
            Collections.shuffle(locs);
            int max = 50;
            Vector3 vMid = Vector3.getNewVector().set(mid);
            vMid.addTo(0.5, 0.5, 0.5);
            int[] toFill = new int[3];
            int n = 0;
            for (Integer i : locs)
            {
                n++;
                Cruncher.fillFromInt(toFill, i);
                vTemp.set(toFill);
                vTemp.addTo(vMid);
                provider.getPlayer().worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, vTemp.x, vTemp.y,
                        vTemp.z, 0, 0, 0);
                if (n > max) break;
            }
        }
    }

    public static SimpleNetworkWrapper packetPipeline = NetworkRegistry.INSTANCE.newSimpleChannel("thut.api");

    public static IPlayerProvider      provider;

    static
    {
        packetPipeline.registerMessage(MessageHandlerClient.class, MessageClient.class, 1, Side.CLIENT);
        packetPipeline.registerMessage(MessageHandlerServer.class, MessageServer.class, 2, Side.SERVER);
    }

    public static MessageClient makeClientPacket(byte channel, byte[] data)
    {
        byte[] packetData = new byte[data.length + 1];
        packetData[0] = channel;

        for (int i = 1; i < packetData.length; i++)
        {
            packetData[i] = data[i - 1];
        }
        return new MessageClient(packetData);
    }

    public static MessageServer makeServerPacket(byte channel, byte[] data)
    {
        byte[] packetData = new byte[data.length + 1];
        packetData[0] = channel;

        for (int i = 1; i < packetData.length; i++)
        {
            packetData[i] = data[i - 1];
        }
        return new MessageServer(packetData);
    }

    public static void sendToAllNear(IMessage toSend, Vector3 point, int dimID, double distance)
    {
        packetPipeline.sendToAllAround(toSend, new TargetPoint(dimID, point.x, point.y, point.z, distance));
    }
}
