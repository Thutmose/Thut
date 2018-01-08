package pokecube.alternative.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.alternative.PokecubeAlternative;
import pokecube.alternative.container.belt.BeltPlayerData;

public class PacketSyncBelt implements IMessage, IMessageHandler<PacketSyncBelt, IMessage>
{

    int            playerId;
    BeltPlayerData belt = new BeltPlayerData();

    public PacketSyncBelt()
    {
    }

    public PacketSyncBelt(BeltPlayerData belt, int playerId)
    {
        this.belt = belt;
        this.playerId = playerId;
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        buffer.writeInt(playerId);
        NBTTagCompound nbt = new NBTTagCompound();
        belt.writeToNBT(nbt);
        ByteBufUtils.writeTag(buffer, nbt);
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        playerId = buffer.readInt();
        NBTTagCompound nbt = ByteBufUtils.readTag(buffer);
        if (nbt != null) belt.readFromNBT(nbt);
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
            BeltPlayerData cap = BeltPlayerData.getBelt(p);
            BeltPlayerData capData = (BeltPlayerData) p.getCapability(CapabilityHasPokemobs.HASPOKEMOBS_CAP, null);
            capData.wrapper.setData(cap);
            for (int i = 0; i < 6; i++)
            {
                cap.setCube(i, message.belt.getCube(i));
                cap.setSlotID(i, message.belt.getSlotID(i));
            }
            cap.setSlot(message.belt.getSlot());
            cap.setType(message.belt.getType());
            cap.setCanMegaEvolve(message.belt.canMegaEvolve());
            cap.setGender(message.belt.getGender());
        }
        return;
    }

}
