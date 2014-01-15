package thut.world.common.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import thut.api.network.IPacketProcessor;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.Player;

public class PacketInt implements IPacketProcessor
{

	@Override
	public void processPacket(ByteArrayDataInput dat, Player player, World world) {
		int x = dat.readInt();
        int y = dat.readInt();
        int z = dat.readInt();
        int f = dat.readInt();
        TileEntity te = world.getBlockTileEntity(x, y, z);
	}
	
	public static Packet250CustomPayload getPacket(TileEntity te)
	{
			int x = te.xCoord;
			int y = te.yCoord;
			int z = te.zCoord;
			int f = 0;
			int v = 0;
			double d = 0;
			int k = 0;
			boolean hasDouble = false;
			boolean hasThirdInt = false;
			

		 	ByteArrayOutputStream bos = new ByteArrayOutputStream(24);
		 	DataOutputStream dos = new DataOutputStream(bos);
			
			try
	        {
	        	dos.writeInt(3);
	            dos.writeInt(x);
	            dos.writeInt(y);
	            dos.writeInt(z);
	            dos.writeInt(f);
	            dos.writeInt(v);
	            if(hasDouble)
	            	dos.writeDouble(d);
	            if(hasThirdInt)
	            	dos.writeInt(k);
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
