package pokecube.alternative.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.alternative.PokecubeAlternative;
import pokecube.alternative.container.BeltPlayerData;
import pokecube.alternative.container.IPokemobBelt;

public class PacketSyncBelt implements IMessage, IMessageHandler<PacketSyncBelt, IMessage>
{

    int         playerId;
    byte        selectedSlot;
    ItemStack[] pokemon = new ItemStack[6];

    public PacketSyncBelt()
    {
    }

    public PacketSyncBelt(IPokemobBelt belt, int playerId)
    {
        this.selectedSlot = (byte) belt.getSlot();
        for (int i = 0; i < 6; i++)
        {
            pokemon[i] = belt.getCube(i);
        }
        this.playerId = playerId;
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        buffer.writeInt(playerId);
        buffer.writeByte(selectedSlot);
        for (int i = 0; i < 6; i++)
            ByteBufUtils.writeItemStack(buffer, pokemon[i]);
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        playerId = buffer.readInt();
        selectedSlot = buffer.readByte();
        for (int i = 0; i < 6; i++)
            pokemon[i] = ByteBufUtils.readItemStack(buffer);

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
                cap.setCube(i, message.pokemon[i]);
            }
            cap.setSlot(message.selectedSlot);
        }
        return;
    }

}
