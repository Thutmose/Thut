package thut.wearables.network;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

public class PacketSyncWearables implements IMessage, IMessageHandler<PacketSyncWearables, IMessage>
{
    NBTTagCompound data;

    public PacketSyncWearables()
    {
        data = new NBTTagCompound();
    }

    public PacketSyncWearables(EntityPlayer player)
    {
        this();
        data.setInteger("I", player.getEntityId());
        PlayerWearables cap = ThutWearables.getWearables(player);
        cap.writeToNBT(data);
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        new PacketBuffer(buffer).writeNBTTagCompoundToBuffer(data);
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        try
        {
            data = new PacketBuffer(buffer).readNBTTagCompoundFromBuffer();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(final PacketSyncWearables message, MessageContext ctx)
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
    void processMessage(PacketSyncWearables message)
    {
        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) return;
        Entity p = world.getEntityByID(message.data.getInteger("I"));
        if (p != null && p instanceof EntityPlayer)
        {
            PlayerWearables cap = ThutWearables.getWearables((EntityLivingBase) p);
            cap.readFromNBT(message.data);
        }
        return;
    }

}
