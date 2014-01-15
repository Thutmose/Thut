package thut.world.common.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import thut.api.network.IPacketProcessor;
import thut.world.common.Volcano;

public class PacketSeedMap implements IPacketProcessor
{

	@Override
	public void processPacket(ByteArrayDataInput dat, Player player, World world) 
	{
		int n = dat.readInt();
		System.out.println(n+" entries");
		for(int i = 0; i<n;i++)
		{
			int id = dat.readInt();
			long seed = dat.readLong();
			Volcano.seedMap.put(id, seed);
		}
		Volcano.init(world);
	}
	
	public static void sendPacket(Player player)
	{
		PacketDispatcher.sendPacketToPlayer(getPacket(), player);
	}

	public static Packet250CustomPayload getPacket()
	{
	 	ByteArrayOutputStream bos = new ByteArrayOutputStream(8+(12*Volcano.seedMap.size()));
	 	DataOutputStream dos = new DataOutputStream(bos);
	 	
	 	try 
	 	{
			dos.writeInt(7);
			dos.writeInt(Volcano.seedMap.size());
			for(Integer i:Volcano.seedMap.keySet())
			{
				if(i!=null)
				{
					dos.writeInt(i);
					dos.writeLong(Volcano.seedMap.get(i));
				}
			}
		} 
	 	catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		
        Packet250CustomPayload pkt = new Packet250CustomPayload();
        pkt.channel = "Thut's Concrete";
        pkt.data = bos.toByteArray();
        pkt.length = bos.size();
        pkt.isChunkDataPacket = true;
        return pkt;
	}
}
