package thut.api.blocks.multiparts;

import thut.api.ThutCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.packet.PacketCustom.IClientPacketHandler;
import codechicken.lib.vec.BlockCoord;
import net.minecraft.network.play.INetHandlerPlayClient;

public class McMultipartCPH implements IClientPacketHandler
{
    public static Object channel = ThutCore.instance;

    @Override
    public void handlePacket(PacketCustom packet, Minecraft mc, INetHandlerPlayClient netHandler) {
    	System.out.println("packet "+packet);
    }
}
