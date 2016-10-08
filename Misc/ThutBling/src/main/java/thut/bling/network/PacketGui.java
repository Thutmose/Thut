package thut.bling.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thut.bling.ThutBling;

public class PacketGui implements IMessage, IMessageHandler<PacketGui, IMessage>
{
    public PacketGui()
    {
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
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
        player.openGui(ThutBling.instance, 0, player.worldObj, 0, 0, 0);
        return;
    }
}