package thut.world.common.corehandlers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.*;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thut.world.client.ClientProxy;
import thut.world.common.Volcano;
import thut.world.common.WorldCore;
import thut.world.common.network.PacketSeedMap;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet13PlayerLookMove;
import net.minecraft.network.packet.Packet30Entity;
import net.minecraft.network.packet.Packet39AttachEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkEvent.Unload;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.ChunkWatchEvent.Watch;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.ChunkEvent.Load;

public class WorldEventHandler {

	public static int chunkSize = 20;
	public static int blockSize = 320;
	public static final Map<Integer, Long> players = new HashMap<Integer, Long>();
	int n = 0;
	
	public WorldEventHandler(int size)
	{
		chunkSize = size;
		blockSize = chunkSize*16;
	}
	
	@ForgeSubscribe
	public void onJoin(EntityJoinWorldEvent evt)
	{
//		if(evt.entity instanceof Player)
//		{
//			if(!evt.world.isRemote)
//			{
//				Volcano.init(evt.world);
//				PacketSeedMap.sendPacket((Player)evt.entity);
//			}
//			else
//			{
//				initPlayer();
//			}
//		}
	}

	@ForgeSubscribe
	public void onLivingUpdate(LivingEvent evt)
	{
		
		if(WorldCore.instance.config.ChunkSize<20||!(evt.entityLiving instanceof EntityPlayerMP)) return;
		
		//EntityPlayerMP player = (EntityPlayerMP) evt.entityLiving;
		EntityLivingBase player = evt.entityLiving;
		World world = player.worldObj;
		if(!ConfigHandler.defaultTypeFinite&&!(world.provider.terrainType.getWorldTypeName().contentEquals("FINITE"))) return;

		double x = player.posX;
		double y = player.posY;
		double z = player.posZ;
		
		if(!world.isRemote&&(abs(floor(x))>blockSize||abs(floor(z))>blockSize))
		{
			System.out.println("wrapping "+x+" "+z+" "+blockSize+" "+player);
			
			Entity mount = null;
			
			if(player.isRiding())
			{
				mount = player.ridingEntity;
				x = mount.posX;
				y = mount.posY;
				z = mount.posZ;
			}
			
			if(players.containsKey(player.entityId)&&players.get(player.entityId)<=world.getTotalWorldTime())
			{
				if(world.getTotalWorldTime()-players.get(player.entityId)>5)
				{
					players.remove(player.entityId);
				}
				
				return;
			}
			
			if(!players.containsKey(player.entityId))
			{
				players.put(player.entityId, world.getTotalWorldTime());
			}
			else if(players.get(player.entityId)>world.getTotalWorldTime()-5)
			{
				players.put(player.entityId, world.getTotalWorldTime());
			}
			
			if(abs(floor(x))>blockSize)
			{
				x = x<0?Math.max(-blockSize, x):Math.min(blockSize, x);
				
				if(player instanceof EntityPlayer&&player.isRiding())
				{
					if(mount!=null)
					{
						System.out.println("CASE 1");
						tpPlayerWithMount((EntityPlayer) player, x, y, z);
					}
				}
				else
				{
					player.setPositionAndUpdate(-x,y,z);
				}
				
			}
			if(abs(floor(z))>blockSize)
			{
				
				
				z = z<0?Math.max(-blockSize, z):Math.min(blockSize, z);
				
				if(player.isRiding())
				{
					System.out.println("CASE 2");
					player.ridingEntity.setPosition(x,y,z);
					player.setPositionAndUpdate(x,y,z);
				}
				else
				{
					player.setPositionAndUpdate(x,y,-z);
					if(player instanceof EntityPlayer)
					if(((EntityPlayer)player).isEntityInsideOpaqueBlock())
						TPhere((EntityPlayer)player);
				}
				
			}
			System.out.println("wrapped "+x+" "+z+" "+blockSize+" "+player);
		}
	}
	
	public void TPhere(EntityPlayer player)
	{
		if(!player.isEntityInsideOpaqueBlock()&&blockBelowSolid(player)||player.isInsideOfMaterial(Material.water))
		{
			return;
		}
		else if(!player.isEntityInsideOpaqueBlock()&&!blockBelowSolid(player))
		{
			double y = player.posY;
			while(y>0)
			{
				y--;
				player.setPositionAndUpdate(player.posX,y++,player.posZ);
				if(!player.isEntityInsideOpaqueBlock()&&blockBelowSolid(player))
				{
					return;
				}
			}
		}
		else
		{
			double y = player.posY;
			while(player.isEntityInsideOpaqueBlock())
			{
				player.setPositionAndUpdate(player.posX,y++,player.posZ);
			}
			return;
		}
		return;
	}
	
	public void tpPlayerWithMount(EntityPlayer player, double x, double y, double z)
	{
		Entity mount = player.ridingEntity;

		if(mount instanceof EntityLivingBase)
			((EntityLivingBase)mount).setPositionAndUpdate(x, y, z);
		else
			mount.setPosition(x, y, z);
		System.out.println(player.ridingEntity);
		player.onChunkLoad();
		mount.worldObj.updateEntities();
		
		
	}
	
	public boolean blockBelowSolid(EntityPlayer player)
	{
		return player.worldObj.isBlockSolidOnSide((int)player.posX, (int)player.posY-1, (int)player.posZ, ForgeDirection.UP, false);
	}

}
