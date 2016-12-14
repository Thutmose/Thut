package pokecube.alternative.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import pokecube.alternative.PokecubeAlternative;
import pokecube.alternative.container.belt.BeltPlayerData;
import pokecube.alternative.container.belt.IPokemobBelt;
import pokecube.core.events.handlers.PCEventsHandler;
import pokecube.core.interfaces.IPokemob;
import thut.lib.CompatWrapper;

public class PacketKeyUse implements IMessage, IMessageHandler<PacketKeyUse, IMessage>
{
    public static final byte SLOTUP   = 0;
    public static final byte SLOTDOWN = 1;
    public static final byte SENDOUT  = 2;
    public static final byte RECALL   = 3;
    public static final byte OPENCARD = 4;
    public static final byte USEITEM  = 5;

    byte                     messageId;
    int                      ticks    = 0;

    public PacketKeyUse()
    {
    }

    public PacketKeyUse(byte message)
    {
        this.messageId = message;
    }

    public PacketKeyUse(byte message, int ticks)
    {
        this.messageId = message;
        this.ticks = ticks;
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        buffer.writeByte(messageId);
        buffer.writeInt(ticks);
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        messageId = buffer.readByte();
        ticks = buffer.readInt();
    }

    @Override
    public IMessage onMessage(final PacketKeyUse message, final MessageContext ctx)
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

    void processMessage(EntityPlayer player, PacketKeyUse message)
    {
        IPokemobBelt cap = BeltPlayerData.getBelt(player);
        if (message.messageId == OPENCARD)
        {
            player.openGui(PokecubeAlternative.instance, 0, player.getEntityWorld(), 0, 0, 0);
            return;
        }
        if (message.messageId == USEITEM)
        {
            int id = message.ticks;
            Entity entity = player.getEntityWorld().getEntityByID(id);
            if (entity != null)
                CompatWrapper.processInitialInteract(entity, player, EnumHand.MAIN_HAND, player.getHeldItemMainhand());
            return;
        }
        if (message.messageId == SENDOUT)
        {
            ItemStack cube = cap.getCube(cap.getSlot());
            if (CompatWrapper.isValid(cube))
            {
                cube.getItem().onPlayerStoppedUsing(cube, player.worldObj, player, message.ticks);
                CompatWrapper.setStackSize(cube, 1);
                cap.setCube(cap.getSlot(), CompatWrapper.nullStack);
            }
        }
        else if (message.messageId == RECALL)
        {
            int id = message.ticks;
            if (id == -1)
            {
                PCEventsHandler.recallAllPokemobs(player);
            }
            else
            {
                Entity mob = player.worldObj.getEntityByID(id);
                if (mob instanceof IPokemob)
                {
                    ((IPokemob) mob).returnToPokecube();
                }
                else
                {
                    PCEventsHandler.recallAllPokemobs(player);
                }
            }
        }
        else if (message.messageId == SLOTUP)
        {
            int currentSlot = cap.getSlot();
            if (currentSlot <= 0)
            {
                cap.setSlot(5);
            }
            else
            {
                cap.setSlot(currentSlot - 1);
            }
        }
        else if (message.messageId == SLOTDOWN)
        {
            int currentSlot = cap.getSlot();
            if (currentSlot >= 5)
            {
                cap.setSlot(0);
            }
            else
            {
                cap.setSlot(currentSlot + 1);
            }
        }
        PacketSyncBelt packet = new PacketSyncBelt(cap, player.getEntityId());
        PacketHandler.INSTANCE.sendToAll(packet);
        return;
    }

}
