package thut.wearables.network;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thut.api.network.PacketHandler;
import thut.core.common.handlers.PlayerDataHandler;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

public class PacketGui implements IMessage, IMessageHandler<PacketGui, IMessage>
{
    public NBTTagCompound data;

    public PacketGui()
    {
        data = new NBTTagCompound();
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        PacketBuffer buf = new PacketBuffer(buffer);
        buf.writeNBTTagCompoundToBuffer(data);
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        PacketBuffer buf = new PacketBuffer(buffer);
        try
        {
            data = buf.readNBTTagCompoundFromBuffer();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public IMessage onMessage(final PacketGui message, final MessageContext ctx)
    {
        FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(new Runnable()
        {
            @Override
            public void run()
            {
                processMessage(ctx.getServerHandler().playerEntity, message);
            }
        });
        return null;
    }

    void processMessage(EntityPlayerMP player, PacketGui message)
    {
        PlayerWearables cap = PlayerDataHandler.getInstance().getPlayerData(player).getData(PlayerWearables.class);
        if (message.data.hasNoTags())
        {
            player.openGui(ThutWearables.instance, 0, player.worldObj, 0, 0, 0);
            return;
        }
        PacketSyncWearables packet = new PacketSyncWearables(player);
        PacketHandler.packetPipeline.sendToAll(packet);
        PlayerDataHandler.getInstance().save(player.getCachedUniqueIdString(), cap.getIdentifier());
        return;
    }
}