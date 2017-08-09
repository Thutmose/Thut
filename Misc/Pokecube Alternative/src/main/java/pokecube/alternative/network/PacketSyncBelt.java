package pokecube.alternative.network;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.alternative.PokecubeAlternative;
import pokecube.alternative.container.belt.BeltPlayerData;
import pokecube.alternative.container.belt.IPokemobBelt;

public class PacketSyncBelt implements IMessage, IMessageHandler<PacketSyncBelt, IMessage>
{

    int          playerId;
    IPokemobBelt belt = new BeltPlayerData();

    public PacketSyncBelt()
    {
    }

    public PacketSyncBelt(IPokemobBelt belt, int playerId)
    {
        this.belt = belt;
        this.playerId = playerId;
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        buffer.writeInt(playerId);
        buffer.writeByte(belt.getSlot());
        for (int i = 0; i < 6; i++)
            ByteBufUtils.writeItemStack(buffer, belt.getCube(i));
        for (int i = 0; i < 6; i++)
        {
            UUID id = belt.getSlotID(i);
            ByteBufUtils.writeUTF8String(buffer, id == null ? "" : id.toString());
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        playerId = buffer.readInt();
        belt.setSlot(buffer.readByte());
        for (int i = 0; i < 6; i++)
            belt.setCube(i, ByteBufUtils.readItemStack(buffer));
        for (int i = 0; i < 6; i++)
        {
            String id = ByteBufUtils.readUTF8String(buffer);
            if (!id.isEmpty())
            {
                belt.setSlotID(i, UUID.fromString(id));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(final PacketSyncBelt message, MessageContext ctx)
    {
        Minecraft.getMinecraft().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                processMessage(message);
            }
        });
        return null;
    }

    @SideOnly(Side.CLIENT)
    void processMessage(PacketSyncBelt message)
    {
        World world = PokecubeAlternative.proxy.getClientWorld();
        if (world == null) return;
        Entity p = world.getEntityByID(message.playerId);
        if (p != null && p instanceof EntityPlayer)
        {
            IPokemobBelt cap = BeltPlayerData.getBelt(p);
            for (int i = 0; i < 6; i++)
            {
                cap.setCube(i, message.belt.getCube(i));
                cap.setSlotID(i, message.belt.getSlotID(i));
            }
            cap.setSlot(message.belt.getSlot());
        }
        return;
    }

}
