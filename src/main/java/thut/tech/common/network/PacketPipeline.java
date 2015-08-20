/**
 *
 */
package thut.tech.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import thut.api.ThutCore;
import thut.api.maths.Vector3;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.entity.EntityLift;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketPipeline
{

	public static SimpleNetworkWrapper packetPipeline;
	
	public static void sendToServer(IMessage toSend)
	{
		packetPipeline.sendToServer(toSend);
	}
	
	public static void sendToClient(IMessage toSend, EntityPlayer player)
	{
		if(player==null)
		{
			System.out.println("null player");
			return;
		}
		packetPipeline.sendTo(toSend, (EntityPlayerMP) player);
	}
	
	public static void sendToAll(IMessage toSend)
	{
		packetPipeline.sendToAll(toSend);
	}
	
	public static void sendToAllNear(IMessage toSend, Vector3 point, int dimID, double distance)
	{
		packetPipeline.sendToAllAround(toSend, new TargetPoint(dimID, point.x, point.y, point.z, distance));
	}
	
	public static ClientPacket makeClientPacket(byte channel, byte[] data)
	{
		byte[] packetData = new byte[data.length+1];
		packetData[0] = channel;
		
		for(int i = 1; i<packetData.length; i++)
		{
			packetData[i] = data[i-1];
		}
		return new ClientPacket(packetData);
	}
	
	public static ClientPacket makeClientPacket(byte channel, NBTTagCompound nbt)
	{
		PacketBuffer packetData = new PacketBuffer(Unpooled.buffer());
		packetData.writeByte(channel);
		try {
			packetData.writeNBTTagCompoundToBuffer(nbt);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new ClientPacket(packetData);
	}
	
	public static ServerPacket makeServerPacket(byte channel, byte[] data)
	{
		byte[] packetData = new byte[data.length+1];
		packetData[0] = channel;
		
		for(int i = 1; i<packetData.length; i++)
		{
			packetData[i] = data[i-1];
		}
		return new ServerPacket(packetData);
	}
	
	public static ServerPacket makePacket(byte channel, NBTTagCompound nbt)
	{
		PacketBuffer packetData = new PacketBuffer(Unpooled.buffer());
		packetData.writeByte(channel);
		try {
			packetData.writeNBTTagCompoundToBuffer(nbt);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new ServerPacket(packetData);
	}
	
    public static int ByteArrayAsInt(byte[] stats)
    {
        if (stats.length != 4)
            return 0;
        int value = 0;
        for (int i = 3; i >= 0; i--)
        {
           value = (value << 8) + (stats[i] & 0xff);
        }
        return value;
    }

    public static byte[] intAsByteArray(int ints)
    {
        byte[] stats = new byte[]
        {
            (byte)((ints      & 0xFF)),
            (byte)((ints >> 8  & 0xFF)),
            (byte)((ints >> 16 & 0xFF)),
            (byte)((ints >> 24 & 0xFF)),
        };
        return stats;
    }
    
    public static class ClientPacket implements IMessage
    {
    	PacketBuffer buffer;
    	
    	public ClientPacket(){};
    	
	    public ClientPacket(byte[] data) {
	    	this.buffer = new PacketBuffer(Unpooled.buffer());
	    	buffer.writeBytes(data);
	    }
	    
	    public ClientPacket(ByteBuf buffer)
	    {
	    	this.buffer = new PacketBuffer(buffer);
	    }
	    
	    public ClientPacket(byte channel, NBTTagCompound nbt)
	    {
	    	this.buffer = new PacketBuffer(Unpooled.buffer());
	    	buffer.writeByte(channel);
	    	try {
				buffer.writeNBTTagCompoundToBuffer(nbt);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    
		@Override
		public void fromBytes(ByteBuf buf) {
			if(buffer == null)
			{
				buffer = new PacketBuffer(Unpooled.buffer());
			}
			buffer.writeBytes(buf);
		}

		@Override
		public void toBytes(ByteBuf buf) {
			if(buffer == null)
			{
				buffer = new PacketBuffer(Unpooled.buffer());
			}
			buf.writeBytes(buffer);
		}
    	
		public static class MessageHandlerClient implements IMessageHandler<ClientPacket, ServerPacket>
		{
			@Override
			public ServerPacket onMessage(ClientPacket message,
					MessageContext ctx) {
				ByteBuf buffer = message.buffer;
				byte mess = buffer.readByte();
				if(mess == 0)
				{
					double y = buffer.readDouble();
					double dy = buffer.readDouble();
					EntityPlayer player = ThutCore.proxy.getPlayer();
					y += player.yOffset;
					player.motionY = Math.max(dy, player.motionY);
					ThutCore.proxy.getPlayer().setPosition(player.posX, y, player.posZ);
				}
				return null;
			}
		}
    }
    public static class ServerPacket implements IMessage
    {
    	PacketBuffer buffer;
    	
    	public ServerPacket(){};
    	
	    public ServerPacket(byte[] data) {
	    	this.buffer = new PacketBuffer(Unpooled.buffer());
	    	buffer.writeBytes(data);
	    }
	    
	    public ServerPacket(ByteBuf buffer)
	    {
	    	this.buffer = (PacketBuffer) buffer;
	    }
	    
	    public ServerPacket(byte channel, NBTTagCompound nbt)
	    {
	    	this.buffer = new PacketBuffer(Unpooled.buffer());
	    	buffer.writeByte(channel);
	    	try {
				buffer.writeNBTTagCompoundToBuffer(nbt);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    
		@Override
		public void fromBytes(ByteBuf buf) {
			if(buffer == null)
			{
				buffer = new PacketBuffer(Unpooled.buffer());
			}
			buffer.writeBytes(buf);
		}

		@Override
		public void toBytes(ByteBuf buf) {
			if(buffer == null)
			{
				buffer = new PacketBuffer(Unpooled.buffer());
			}
			buf.writeBytes(buffer);
		}
		public static class MessageHandlerServer implements IMessageHandler<ServerPacket, IMessage>
		{
			
			public void handleServerSide(EntityPlayer player, PacketBuffer buffer) {
		        
		        
			}
			
			@Override
			public ServerPacket onMessage(ServerPacket message,
					MessageContext ctx) {
				EntityPlayer player = ctx.getServerHandler().playerEntity;
				handleServerSide(player, message.buffer);
			
				return null;
			}
		}
    }
    
}
