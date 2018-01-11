package pokecube.alternative.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.alternative.Config;
import pokecube.core.interfaces.PokecubeMod;

public class PacketSyncEnabled implements IMessage, IMessageHandler<PacketSyncEnabled, IMessage>
{
    boolean var;

    public PacketSyncEnabled()
    {
    }

    public PacketSyncEnabled(boolean var)
    {
        this.var = var;
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        buffer.writeBoolean(var);
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        var = buffer.readBoolean();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(final PacketSyncEnabled message, MessageContext ctx)
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
    void processMessage(PacketSyncEnabled message)
    {
        if (PokecubeMod.debug) PokecubeMod.log("Setting Enabled State to: " + message.var);
        Config.instance.isEnabled = message.var;
    }

}
