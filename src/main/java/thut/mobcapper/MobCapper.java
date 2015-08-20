package thut.mobcapper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Vector;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.INpc;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "MobCapper", name = "MobCapper", version = "1.7.10", acceptableRemoteVersions = "*")
public class MobCapper {

	@Mod.Instance("MobCapper")
	public static MobCapper instance;

	Config conf;
	
	public MobCapper() {
		instance = this;
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(this);
		conf = new Config(e);
    	
	}

	@EventHandler
	public void load(FMLInitializationEvent evt) {
		
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {
		
	}

	AxisAlignedBB box = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
	@SubscribeEvent
	public void specialEvent(LivingSpawnEvent evt) {
		if(evt.world.isRemote)
			return;
		if(evt.entity instanceof EntityLivingBase && !(evt.entity instanceof EntityPlayer))
		{
			boolean shouldCull = (evt.entity instanceof IMob);
			shouldCull = shouldCull || (evt.entity instanceof IAnimals);
			shouldCull = shouldCull && !(evt.entity instanceof IMerchant);
			shouldCull = shouldCull && !(evt.entity instanceof IBossDisplayData);
			boolean tameable = evt.entity instanceof EntityTameable;
			
			if(tameable || !shouldCull)
			{
				if(!tameable)
					return;
				EntityTameable tame = (EntityTameable) evt.entity;
				if(tame.isTamed())
				{
					return;
				}
			}
			World world = evt.world;
			box.setBounds(evt.x, evt.y, evt.z, evt.x, evt.y, evt.z);
			List l;
			List l2 = world.getEntitiesWithinAABB(EntityPlayer.class, box.expand(conf.number2, conf.number2, conf.number2));
			int n = 0;
			boolean player = !l2.isEmpty();
			if(player)
			{
				l = world.getEntitiesWithinAABB(evt.entity.getClass(), box.expand(conf.number2, conf.number2, conf.number2));
			}
			else
			{
				if(evt.isCancelable())
					evt.setCanceled(true);
				if(evt.hasResult())
					evt.setResult(Result.DENY);
				return;
			}
			
			if(!player || l.size()>conf.number)
			{
				if(evt.isCancelable())
					evt.setCanceled(true);
				if(evt.hasResult())
					evt.setResult(Result.DENY);
			}
		}
	}
	@SubscribeEvent
	public void joinEvent(EntityJoinWorldEvent evt) {
		if(evt.world.isRemote)
			return;
		if(evt.entity instanceof EntityLivingBase && !(evt.entity instanceof EntityPlayer))
		{
			boolean shouldCull = (evt.entity instanceof IMob);
			shouldCull = shouldCull || (evt.entity instanceof IAnimals);
			shouldCull = shouldCull && !(evt.entity instanceof IMerchant);
			shouldCull = shouldCull && !(evt.entity instanceof IBossDisplayData);
			boolean tameable = evt.entity instanceof EntityTameable;
			
			if(tameable || !shouldCull)
			{
				if(!tameable)
					return;
				EntityTameable tame = (EntityTameable) evt.entity;
				if(tame.isTamed())
				{
					return;
				}
			}
			World world = evt.world;
			box.setBounds(evt.entity.posX, evt.entity.posY, evt.entity.posZ, evt.entity.posX, evt.entity.posY, evt.entity.posZ);
			List l;
			List l2 = world.getEntitiesWithinAABB(EntityPlayer.class, box.expand(conf.number2, conf.number2, conf.number2));
			int n = 0;
			boolean player = !l2.isEmpty();
			if(player)
			{
				l = world.getEntitiesWithinAABB(evt.entity.getClass(), box.expand(conf.number2, conf.number2, conf.number2));
			}
			else
			{
				if(evt.isCancelable())
					evt.setCanceled(true);
				if(evt.hasResult())
					evt.setResult(Result.DENY);
				return;
			}
			
			if(!player || l.size()>conf.number)
			{
				if(evt.isCancelable())
					evt.setCanceled(true);
				if(evt.hasResult())
					evt.setResult(Result.DENY);
			}
		}
	}
}
