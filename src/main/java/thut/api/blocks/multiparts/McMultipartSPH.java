package thut.api.blocks.multiparts;

import thut.api.ThutCore;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.world.World;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.packet.PacketCustom.IServerPacketHandler;
public class McMultipartSPH implements IServerPacketHandler
{
    public static Object channel = ThutCore.instance;

    @Override
    public void handlePacket(PacketCustom packet, EntityPlayerMP sender, INetHandlerPlayServer netHandler) {
        switch (packet.getType()) {
            case 1:
                EventHandler.place(sender, sender.worldObj);
                break;
        }
    }
}
