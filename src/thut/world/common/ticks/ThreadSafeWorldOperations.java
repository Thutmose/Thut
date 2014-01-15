package thut.world.common.ticks;

import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.liquids.ILiquid;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import thut.api.explosion.Vector3;
import thut.world.common.*;

public class ThreadSafeWorldOperations extends Ticker
{
	// stuff needed
	private Random	r = new Random();
	
	// actually used
	private double x;
	private double y;
	private double z;
	private double vx;
	private double vy;
	private double vz;
	private double dx;
	private double dy;
	private double dz;
	private String type = "null";
	public int ID;
	public int meta;
	public float blastResistance;
	public boolean edits = false;
	
	public static double[] wind = new double[] {0,0};
	private int count = 0;
	
	private float pitch;
	private float volume;
	private String sound;
	private int damage;
	private Entity harmed;
	private Entity looker;
	private TileEntity TE;
	public List<Entity> seen = new ArrayList<Entity>();
	public Block block;
	//Other stuff
	
	public static Map<String, Integer> effect = new HashMap<String, Integer>();
	
	public ThreadSafeWorldOperations(){super();}
	
	public void safeBreak(World worldObj, double x, double y, double z){
		worldObj.destroyBlock((int)x, (int)y, (int)z, false);
	}
	
	public void safeSound(World worldObj,double x, double y, double z, String sound, float volume, float pitch){
		worldObj.playSoundEffect(x, z, y, sound, volume, pitch);
	}
	public void safeHurt(Entity harmed, int damage, DamageSource source){
		if(harmed.worldObj.doChunksNearChunkExist((int)harmed.posX,(int)harmed.posY, (int)harmed.posZ, 1))
		if(!(harmed==null||harmed instanceof EntityXPOrb)){
			harmed.attackEntityFrom(source, damage);
		}
	}
	public void safeBurn(Entity burnt, int time){
		if(burnt.worldObj.doChunksNearChunkExist((int)burnt.posX,(int)burnt.posY, (int)burnt.posZ, 1))
		burnt.setFire(time);
	}
	public void safeThrow(Entity harmed, double vx, double vy, double vz){
		if(harmed instanceof EntityLiving||harmed instanceof EntityCreature){
			harmed.addVelocity(vx, vy, vz);
		}
	}
	public List safeLocateEntity(World worldObj,Entity looker, double x, double y, double z,double dx, double dy, double dz){
		return worldObj.getEntitiesWithinAABBExcludingEntity(looker, AxisAlignedBB.getBoundingBox(x-dx,y-dy,z-dz,x+dx,y+dy,z+dy));
	}
	
	public boolean checkAABB(World worldObj,AxisAlignedBB aabb){
		return worldObj.checkBlockCollision(aabb);
	}
	public boolean checkChunkLoaded(World worldObj, int x, int z){
		return worldObj.getChunkFromBlockCoords(x, z).isChunkLoaded;
	}
	
	public void safeUpdateBlockRange(World worldObj,double x, double y, double z,double r){
		worldObj.markBlockRangeForRenderUpdate((int)(x-r), (int)(y-r), (int)(z-r),(int)(x+r), (int)(y+r), (int)(z+r));
	}
	
	public List safeLocateEntity(World world, double x, double y, double z,double dx, double dy, double dz){
		return safeLocateEntity(world,(Entity)null, x,y,z,dx,dy,dz);
	}
	public synchronized boolean safeLookUp(World worldObj,double x, double y, double z){
		if(worldObj!=null)
		{
		//	if(worldObj.doChunksNearChunkExist((int)x,(int)y, (int)z, 1))
			{
				this.ID = worldObj.getBlockId((int)x, (int)y, (int)z);
				this.meta = worldObj.getBlockMetadata((int)x,(int)y, (int)z);
				this.block = Block.blocksList[ID];
				if(ID!=0&&block!=null)
				{
					this.blastResistance = 5.0F*block.getExplosionResistance((Entity)null, worldObj, (int)x,(int)y, (int)z, 0d, 0d, 0d);
				}
				//System.out.println("test");
				return true;
			}
		}
		return false;
	}
	public void safeSet(World worldObj,double x, double y, double z, int ID, int Meta, int flag){
		if(!worldObj.isRemote)
		{
			Vector3 vec = new Vector3(x,y,z);
			worldObj.setBlock(vec.intX(), vec.intY(), vec.intZ(), ID, Meta, flag);
		}
	}
	public void notAsSafeSet(World worldObj,double x, double y, double z, int ID, int Meta){
		if(!worldObj.isRemote)
		{
			worldObj.setBlock((int)x, (int)y, (int)z, ID, Meta, 2);
			worldObj.scheduleBlockUpdate((int)x, (int)y, (int)z, ID, 5);
		}
	}
	public void safeSetMeta(World worldObj,double x, double y, double z, int Meta){
		if(!worldObj.isRemote)
		worldObj.setBlockMetadataWithNotify((int)x, (int)y, (int)z, Meta, 2);
		
	}
	public void safeScheduleUpdate(World worldObj,double x, double y, double z, int rate){
		if(!worldObj.isRemote)
		worldObj.scheduleBlockUpdate((int)x, (int)y, (int)z, worldObj.getBlockId((int)x, (int)y, (int)z), rate);
		
	}
	public int getBlockMetadata(World worldObj,double x, double y, double z){
		Vector3 vec = new Vector3(x,y,z);
		return worldObj.getBlockMetadata(vec.intX(), vec.intY(), vec.intZ());
	}
	public int getBlockID(World worldObj,double x, double y, double z){
		Vector3 vec = new Vector3(x,y,z);
		return worldObj.getBlockId(vec.intX(), vec.intY(), vec.intZ());
	}
	public Block safeGetBlock(World worldObj,double x, double y, double z){
		return Block.blocksList[getBlockID(worldObj, x, y, z)];
	}
	public void safeSpawn(World worldObj, Entity entity){
		worldObj.spawnEntityInWorld(entity);
	}
	public TileEntity safeGetTE(World worldObj,double x, double y, double z){

		TE = worldObj.getBlockTileEntity((int)x, (int)y, (int)z);
		
		return TE;
	}
	
	//*
	public synchronized  void set(World worldObj,Vector3 vec, int ID, int Meta){
		if(!worldObj.isRemote)
		{
			vec.setBlock(worldObj, ID , Meta, 2);
		}
	}
	//*/
	public synchronized int getMeta(World worldObj,Vector3 vec){
		return vec.getBlockMetadata(worldObj);
	}
	public synchronized int getID(World worldObj,Vector3 vec){
		return vec.getBlockId(worldObj);
	}
	public synchronized Block getBlock(World worldObj,Vector3 vec){
		return vec.getBlock(worldObj);
	}
	public synchronized TileEntity getTE(World worldObj,Vector3 vec){
		return vec.getTileEntity(worldObj);
	}
	public synchronized Material getMaterial(World worldObj, Vector3 vec)
	{
		return vec.getBlockMaterial(worldObj);
	}
	public synchronized float getResistance(World worldObj, Vector3 vec)
	{
		return vec.getExplosionResistance(worldObj);
	}
	public synchronized void doExplosion(World worldObj, Vector3 vec, float strength, boolean destroyBlocks)
	{
		vec.doExplosion(worldObj, strength, destroyBlocks);
	}
	
	
	
	
	
	
	
	
	
	
	
	public static Vector3 getWind(World worldObj, double x, double z){
		double[] tempWind = new double[] {0,0};
		double windFactor = 1;
		double frequencyFactor = 0.00015;
		tempWind[0] = windFactor*(sin(frequencyFactor*((x/16)+worldObj.getTotalWorldTime()))+
				sin(frequencyFactor*((x/16)+worldObj.getTotalWorldTime())*frequencyFactor*((x/16)+worldObj.getTotalWorldTime()))+
				sin(frequencyFactor*worldObj.getTotalWorldTime()*(x/16))*cos(frequencyFactor*worldObj.getTotalWorldTime()*(x/16))+
				sin(frequencyFactor*worldObj.getTotalWorldTime()*(x/16))*cos(frequencyFactor*worldObj.getTotalWorldTime()*(x/16))*
				sin(frequencyFactor*worldObj.getTotalWorldTime()*(x/16))*cos(frequencyFactor*worldObj.getTotalWorldTime()*(x/16)));
	
		tempWind[1] = windFactor*(cos(frequencyFactor*((z/16)+worldObj.getTotalWorldTime()))+
				cos(frequencyFactor*((z/16)+worldObj.getTotalWorldTime())*frequencyFactor*((z/16)+worldObj.getTotalWorldTime()))+
				sin(frequencyFactor*((z/16)+worldObj.getTotalWorldTime()))*cos(frequencyFactor*((z/16)+worldObj.getTotalWorldTime()))+
				sin(frequencyFactor*((z/16)+worldObj.getTotalWorldTime()))*cos(frequencyFactor*((z/16)+worldObj.getTotalWorldTime()))*
				sin(frequencyFactor*((z/16)+worldObj.getTotalWorldTime()))*cos(frequencyFactor*((z/16)+worldObj.getTotalWorldTime())));
		Vector3 wind = new Vector3(tempWind[0], tempWind[1], 0);
		return wind;
	}
	
	
    private static double cos(double d) {
		return MathHelper.cos((float)d);
	}

	private static double sin(double d) {
		return MathHelper.sin((float)d);
	}

	/**
     * Whether or not a certain block is considered a liquid.
     * @param world - world the block is in
     * @param (int)d - x coordinate
     * @param e - y coordinate
     * @param f - z coordinate
     * @return if the block is a liquid
     */
    public static boolean isLiquid(World world, double d, double e, double f)
    {
    	return getLiquid(world, (int)d,(int) e,(int) f) != null;
    }
    
    /**
     * Gets a liquid from a certain location.
     * @param world - world the block is in
     * @param x - x coordinate
     * @param y - y coordinate
     * @param z - z coordinate
     * @return the liquid at the certain location, null if it doesn't exist
     */
    public static synchronized LiquidStack getLiquid(World world, int x, int y, int z)
    {
    	int id = world.getBlockId(x, y, z);
    	int meta = world.getBlockMetadata(x, y, z);
    	
    	if(id == 0)
    	{
    		return null;
    	}
    	
    	if((id == Block.waterStill.blockID || id == Block.waterMoving.blockID) && meta == 0)
    	{
    		return new LiquidStack(Block.waterStill.blockID, LiquidContainerRegistry.BUCKET_VOLUME, 0);
    	}
    	else if((id == Block.lavaStill.blockID || id == Block.lavaMoving.blockID) && meta == 0)
    	{
    		return new LiquidStack(Block.lavaStill.blockID, LiquidContainerRegistry.BUCKET_VOLUME, 0);
    	}
    	else if(Block.blocksList[id] instanceof ILiquid)
    	{
    		ILiquid liquid = (ILiquid)Block.blocksList[id];
    	
    		if(liquid.isMetaSensitive())
    		{
    			return new LiquidStack(liquid.stillLiquidId(), LiquidContainerRegistry.BUCKET_VOLUME, liquid.stillLiquidMeta());
    		}
    		else if(meta == 0)
    		{
    			return new LiquidStack(liquid.stillLiquidId(), LiquidContainerRegistry.BUCKET_VOLUME, 0);
    		}
    	}
    	
    	return null;
    }
    
	@Override
	public void onUpdate() 
	{
	}

	
	
}