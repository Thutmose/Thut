package thut.world.common.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import thut.api.network.IPacketProcessor;
import thut.api.network.PacketStampable;
import thut.world.common.WorldCore;
import thut.world.common.corehandlers.ConfigHandler;
import thut.world.common.multipart.PlayerInteractHandler;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.ITinyPacketHandler;
import cpw.mods.fml.common.network.Player;

public class TCPacket implements IPacketHandler
{
	
	Map<Integer, IPacketProcessor> packetTypes = new HashMap<Integer, IPacketProcessor>();
	
	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {

		if(!(packet.channel.contentEquals("Thut's Concrete")||packet.channel.contentEquals("multiPartPackets"))) return;
		
		if(packet.channel.contentEquals("Thut's Concrete"))
		{
			World world = WorldCore.proxy.getWorld();
			ByteArrayDataInput dat = ByteStreams.newDataInput(packet.data);
			
			handlePacket(dat, player, world);
		}
		else if (player instanceof EntityPlayerMP)
		{
			PlayerInteractHandler.place((EntityPlayerMP)player, ((EntityPlayerMP)player).worldObj);
		}
	}
	
	public TCPacket()
	{
		packetTypes.put(0, new PacketStampable());
//		packetTypes.put(2, new PacketBeam());
		packetTypes.put(3, new PacketInt());
//		packetTypes.put(4, new PacketMountedCommand());
		packetTypes.put(6, new PacketVolcano());
		packetTypes.put(7, new PacketSeedMap());
	}
	
	public void handlePacket(ByteArrayDataInput dat,Player player,World world)
	{
		int id = dat.readInt();
		if(ConfigHandler.debugPrints)
		System.out.println("Packet ID: "+id);
		packetTypes.get(id).processPacket(dat, player, world);
	}
	
}