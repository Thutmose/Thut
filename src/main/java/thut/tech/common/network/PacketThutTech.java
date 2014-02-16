package thut.tech.common.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cpw.mods.fml.repackage.com.nothome.delta.ByteBufferSeekableSource;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import thut.api.network.Packet;
import thut.tech.common.blocks.tileentity.TileEntityLiftAccess;
import thut.tech.common.entity.EntityLift;

public class PacketThutTech extends Packet {

	byte[] data;
	
	public PacketThutTech(){};
	
	public PacketThutTech(byte[] message)
	{
		data = message;
	}
	
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) 
	{
		for(int i = 0; i<data.length; i++)
		{
			buffer.writeByte(data[i]);
		}
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) 
	{
		int count = buffer.capacity();
		data = new byte[count];
		for(int i = 0; i<count; i++)
		{
			data[i] = buffer.readByte();
		}
	}

	@Override
	public void handleClientSide(EntityPlayer player) 
	{
		byte mess = data[0];
		if(mess==0)
			processLiftPacket(player, player.worldObj);
	}

	@Override
	public void handleServerSide(EntityPlayer player) 
	{
		
	}
	
	private void processLiftPacket(EntityPlayer player, World world)
	{
		DataInputStream dat = new DataInputStream(new ByteArrayInputStream(data));
		try {
			byte mess = dat.readByte();
			
			int id = dat.readInt();
			int command = dat.readInt();
			int command2 = -1;
			if(command==1||command==0)
				 command2 = dat.readInt();
			
			Entity e = world.getEntityByID(id);
				
			if(e instanceof EntityLift)
			{
				if(command == 1 || command == 0)
					((EntityLift)e).toMoveY = command!=0;
				if((command == 1 || command == 0)&&(command2 == 1 || command2 == 0))
					((EntityLift)e).up = command2!=0;
				if(command == 3)
				{
					((EntityLift)e).callClient(dat.readDouble());
					((EntityLift)e).destinationFloor = dat.readInt();
				}
				if(command == 4)
				{
					((EntityLift)e).size = dat.readDouble();
				}
				
			}
			
			if(command == 2)
			{
				int x = dat.readInt();
				int y = dat.readInt();
				int z = dat.readInt();
				TileEntity te = player.worldObj.getTileEntity(x, y, z);
				if(te instanceof TileEntityLiftAccess)
				{
					((TileEntityLiftAccess) te).lift = EntityLift.lifts.get(id);
					System.out.println(((TileEntityLiftAccess) te)+" "+((TileEntityLiftAccess) te).lift);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static PacketThutTech getLiftPacket(Entity e, int command, double value, int value2)
	{
		int id = e.getEntityId();
		
	 	ByteArrayOutputStream bos = new ByteArrayOutputStream(16);
	 	DataOutputStream dos = new DataOutputStream(bos);
		
		try
       {
			dos.writeByte(0);
			dos.writeInt(id);
			dos.writeInt(command);
			dos.writeDouble(value);
			dos.writeInt(value2);
       }
       catch (IOException ex)
       {
       	ex.printStackTrace();
       }
		
		return new PacketThutTech(bos.toByteArray());
	}

}
