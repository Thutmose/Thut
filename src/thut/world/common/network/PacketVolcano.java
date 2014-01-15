package thut.world.common.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

import cpw.mods.fml.common.network.Player;
import thut.api.network.IPacketProcessor;
import thut.world.common.Volcano;
import thut.world.common.blocks.tileentity.TileEntityVolcano;
import thut.world.common.blocks.tileentity.TileEntityVolcano.Vect;

public class PacketVolcano implements IPacketProcessor
{

	@Override
	public void processPacket(ByteArrayDataInput dat, Player player, World world) {
		int x = dat.readInt();
        int y = dat.readInt();
        int z = dat.readInt();
        long seed = dat.readLong();
        
        Volcano.setSeed(world.provider.dimensionId, seed);
        if(world.getBlockTileEntity(x, y, z) instanceof TileEntityVolcano)
        {
        	TileEntityVolcano v = (TileEntityVolcano)world.getBlockTileEntity(x, y, z);
        	if(v.v==null)
        	{
        		v.v = Volcano.getVolcano(x, z, world);
        	}
        	if(v.v!=null)
        	{
	        	v.v.growthFactor = dat.readDouble();
	        	v.v.majorFactor = dat.readDouble();
	        	v.v.minorFactor = dat.readDouble();
	        	v.v.activeFactor = dat.readDouble();
        	}
        	
        }
	}

	
	public static Packet250CustomPayload getPacket(TileEntity te)
	{
		TileEntityVolcano v = (TileEntityVolcano)te;
		
		int x = te.xCoord;
		int y = te.yCoord;
		int z = v.z;
		
		long seed = v.worldObj.getSeed();
		
		double growthFactor = v.v.growthFactor;
		double majorFactor = v.v.majorFactor;
		double minorFactor = v.v.minorFactor;
		double activeFactor = v.v.activeFactor;
		
		
	 	ByteArrayOutputStream bos = new ByteArrayOutputStream(56);
	 	DataOutputStream dos = new DataOutputStream(bos);
	 	
		try
        {
        	dos.writeInt(6);
            dos.writeInt(x);
            dos.writeInt(y);
            dos.writeInt(z);
            
            dos.writeLong(seed);
            
            dos.writeDouble(growthFactor);
            dos.writeDouble(majorFactor);
            dos.writeDouble(minorFactor);
            dos.writeDouble(activeFactor);
            
             
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
