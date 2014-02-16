package thut.tech.common.entity;

import static thut.api.ThutBlocks.*;
import io.netty.buffer.ByteBuf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import thut.api.ThutBlocks;
import thut.api.entity.IMultiBox;
import thut.api.explosion.Vector3;
import thut.api.explosion.Vector3.Matrix3;
import thut.api.network.PacketPipeline;
import thut.tech.common.blocks.tileentity.TileEntityLiftAccess;
import thut.tech.common.handlers.ConfigHandler;
import thut.tech.common.items.ItemLinker;
import thut.tech.common.network.PacketThutTech;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityLift extends EntityLivingBase implements IEntityAdditionalSpawnData, IMultiBox
{

	//TODO remove this
	TileEntity test = new TileEntityHopper();
	
	public double size = 1;
	public double speedUp = ConfigHandler.LiftSpeedUp;
	public double speedDown = -ConfigHandler.LiftSpeedDown;
	public double NOPASSENGERSPEEDDOWN = -ConfigHandler.LiftSpeedDown;
	public double PASSENDERSPEEDDOWN = -ConfigHandler.LiftSpeedDownOccupied;
	public static int ACCELERATIONTICKS = 20;
	public double acceleration = 0.05;
	public boolean up = true;
	public boolean toMoveY = false;
	public boolean moved = false;
	public boolean axis = true;
	public boolean hasPassenger = false;
	public static boolean AUGMENTG = true;
	int n = 0;
	int passengertime = 10;
	boolean first = true;
	Random r = new Random();
//	public IElectricityStorage source;
	
	public double storedEnergy = 0;
	
	public static double ENERGYCOST = 0;
	
	public boolean xAxis = false;
	
	public double destinationY = 0;
	public int destinationFloor = 0;
	
	public double prevFloorY = 0;
	public double prevFloor = 0;
	
	public boolean called = false;
	TileEntityLiftAccess current;
	public int id;
	
	public static ConcurrentHashMap<Integer, EntityLift> lifts = new ConcurrentHashMap<Integer, EntityLift>();
	public static int MAXID = 0;
	
	Matrix3 mainBox = new Matrix3();
	
	public ConcurrentHashMap<String, Matrix3> boxes = new ConcurrentHashMap<String, Matrix3>();
	public ConcurrentHashMap<String, Vector3> offsets = new ConcurrentHashMap<String, Vector3>();
	
	public TileEntityLiftAccess[][] floors = new TileEntityLiftAccess[64][4];
	
	public int[][][]floorArray = new int[64][4][3];

	Matrix3 base = new Matrix3();
	Matrix3 top = new Matrix3();
	Matrix3 wall1 = new Matrix3();
	
	public EntityLift(World par1World) 
	{
		super(par1World);
		this.ignoreFrustumCheck = true;
		this.hurtResistantTime =0;
		this.isImmuneToFire = true;
	}
	
	public boolean canRenderOnFire()
	{
		return false;
	}
	
    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    public boolean getCanSpawnHere()
    {
    	return false;
    }
	
    public boolean isPotionApplicable(PotionEffect par1PotionEffect)
    {
    	return false;
    }
    
	public EntityLift(World world, double x, double y, double z, double size)
	{
		this(world);
		this.setPosition(x, y, z);
		r.setSeed(100);
		this.id = MAXID++;//r.nextInt((int)Math.abs(x)+1)+r.nextInt((int)Math.abs(y)+1)+r.nextInt((int)Math.abs(z)+1);
		lifts.put(id, this);
		this.size = Math.max(size, 1);
		this.setSize((float)this.size, 1f);
	}
	
	@Override
	public void onUpdate()
	{
		this.prevPosY = posY;
		if((int)size!=(int)this.width)
		{
			this.setSize((float)size, 1f);
		}
		
//		if(test!=null)
//		{
//			test.setWorldObj(worldObj);
//			test.xCoord = (int)posX;
//			test.yCoord = (int)posY;
//			test.zCoord = (int)posZ;
//			test.updateEntity();	
//		}

//		if(this.health <=0)
//		{
//			this.setDead();
//		}
//		if(this.health < this.getMaxHealth()&&Math.random()>0.9)
//		{
//			this.health++;
//		}
		
		if(first)
		{
			checkRails(0);
			first = false;
		}
		clearLiquids();
		
		if(!checkBlocks(0))
			toMoveY=false;
		
		accelerate();
		if(toMoveY)
		{
			doMotion();
		}
		else
		{
			setPosition(posX, called&&Math.abs(posY-destinationY)<0.5?destinationY:Math.floor(posY), posZ);
			called = false;
			prevFloor = destinationFloor;
			prevFloorY = destinationY;
			destinationY = -1;
			destinationFloor = 0;
			if(current!=null)
			{
				current.setCalled(false);
				worldObj.scheduleBlockUpdate(current.xCoord, current.yCoord, current.zCoord, current.getBlockType(), 5);
				current = null;
			}
		}
		
		checkCollision();
		passengertime = hasPassenger?20:passengertime-1;
		n++;
	}
	
	public void passengerCheck()
	{
		List<Entity> list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox);
		if(list.size()>0)
		{
			hasPassenger = true;
			System.out.println("passenger");
		}
		else
		{
			hasPassenger = false;
		}
	}

	public void call(int floor)
	{
		if(floor == 0||floor>64)
		{
			return;
		}
		
		if(!worldObj.isRemote&&floorArray[floor-1]!=null)
		{
			int i = -1;
			for(int j = 0; j<4;j++)
			{
				if(floorArray[floor-1][j]!=null&&floorArray[floor-1][j].length==3)
				{
					int x = floorArray[floor-1][j][0];
					int y = floorArray[floor-1][j][1];
					int z = floorArray[floor-1][j][2];
					if(worldObj.getTileEntity(x, y, z)!=null && worldObj.getTileEntity(x, y, z) instanceof TileEntityLiftAccess)
					{
						prevFloorY = posY;
						destinationY = worldObj.getTileEntity(x, y, z).yCoord - 2;
						current = (TileEntityLiftAccess)worldObj.getTileEntity(x, y, z);
						current.called = true;
						worldObj.scheduleBlockUpdate(x, y, z, worldObj.getBlock(x, y, z), 5);
						destinationFloor = floor;
						
						callYValue((int) destinationY);
						
						return;
					}
				}
			}
		}
	}
	
	public void callClient(double destinationY)
	{
		prevFloorY = posY;
		this.destinationY = destinationY;
		up = destinationY > posY;
		toMoveY = true;
		called = true;
		System.out.println("called client "+destinationY+" "+up);
	}
	
	public void callYValue(int yValue)
	{
		if(!worldObj.isRemote&&consumePower())
		{
			destinationY = yValue;
			up = destinationY > posY;
			toMoveY = true;
			called = true;
			PacketPipeline.packetPipeline.sendToAllAround(PacketThutTech.getLiftPacket(this, 3, destinationY, 0), new TargetPoint(this.dimension, posX, posY, posZ, 100));
		}
	}
	
	public void accelerate()
	{
		motionX = 0;
		motionZ = 0;
		if(!toMoveY)
			motionY *= 0.5;
		else
		{
			if(up)
				motionY = Math.min(speedUp, motionY + acceleration*speedUp);
			else
				motionY = Math.max(speedDown, motionY + acceleration*speedDown);
		}
	}
	
	public void doMotion()
	{
		if(up)
		{
			if(checkBlocks(motionY*(ACCELERATIONTICKS+1)))
			{
				setPosition(posX, posY+motionY, posZ);
				moved = true;
				return;
			}
			else
			{
				while(motionY>=0&&!checkBlocks((motionY - acceleration*speedUp/10)*(ACCELERATIONTICKS+1)))
				{
					motionY = motionY - acceleration*speedUp/10;
				}
				
				if(checkBlocks(motionY))
				{
					setPosition(posX, posY+motionY, posZ);
					moved = true;
					return;
				}
				else
				{
			//		System.out.println("blocked up");
					setPosition(posX, called&&Math.abs(posY-destinationY)<0.5?destinationY:Math.floor(posY), posZ);
					called = false;
					prevFloor = destinationFloor;
					prevFloorY = destinationY;
					destinationY = -1;
					destinationFloor = 0;
					if(current!=null)
					{
						current.setCalled(false);
						worldObj.scheduleBlockUpdate(current.xCoord, current.yCoord, current.zCoord, current.getBlockType(), 5);
						current = null;
					}
					motionY = 0;
					toMoveY = false;
					moved = false;
				}
			}
		}
		else
		{
			if(checkBlocks(motionY*(ACCELERATIONTICKS+1)))
			{
				setPosition(posX, posY+motionY, posZ);
				moved = true;
				return;
			}
			else
			{
				while(motionY<=0&&!checkBlocks((motionY - acceleration*speedDown/10)*(ACCELERATIONTICKS+1)))
				{
					motionY = motionY - acceleration*speedDown/10;
				}
				
				if(checkBlocks(motionY))
				{
					setPosition(posX, posY+motionY, posZ);
					moved = true;
					return;
				}
				else
				{
			//		System.out.println("blocked down");
					setPosition(posX, called&&Math.abs(posY-destinationY)<0.5?destinationY:Math.floor(posY), posZ);
					called = false;
					prevFloor = destinationFloor;
					prevFloorY = destinationY;
					destinationY = -1;
					destinationFloor = 0;
					if(current!=null)
					{
						current.setCalled(false);
						worldObj.scheduleBlockUpdate(current.xCoord, current.yCoord, current.zCoord, current.getBlockType(), 5);
						current = null;
					}
					motionY = 0;
					toMoveY = false;
					moved = false;
				}
			}
		}
		toMoveY = false;
		moved = false;
	}
	
	public boolean checkBlocks(double dir)
	{
		boolean ret = true;
		Vector3 thisloc = new Vector3(this);
		thisloc = thisloc.add(new Vector3(0,dir,0));
		
		if(called)
		{
			if(dir > 0 && thisloc.y > destinationY)
			{
				return false;
			}
			if(dir < 0 && thisloc.y < destinationY)
			{
				return false;
			}
		}

		int rad = (int)(Math.floor(size/2));
		
		for(int i = -rad; i<=rad;i++)
			for(int j = -rad;j<=rad;j++)
			{
				Vector3 checkTop = (thisloc.add(new Vector3(i,4,j)));
				Vector3 checkBottom = (thisloc.add(new Vector3(i,1,j)));
				ret = ret && (thisloc.add(new Vector3(i,0,j))).clearOfBlocks(worldObj);
				ret = ret && (thisloc.add(new Vector3(i,5,j))).clearOfBlocks(worldObj);
				if(checkTop.isFluid(worldObj))
				{
					checkTop.setAir(worldObj);
				}
				if(checkBottom.isFluid(worldObj))
				{
					checkBottom.setAir(worldObj);
				}
			}

		ret = ret && checkRails(dir);
		return ret;
	}
	
	public void clearLiquids()
	{
		int rad = (int)(Math.floor(size/2));

		Vector3 thisloc = new Vector3(this);
		for(int i = -rad; i<=rad;i++)
			for(int j = -rad;j<=rad;j++)
			{
				Vector3 check = (thisloc.add(new Vector3(i,5,j)));
				if(check.isFluid(worldObj))
				{
					check.setBlock(worldObj, air,0);
				}
				check = (thisloc.add(new Vector3(i,0,j)));
				if(check.isFluid(worldObj))
				{
					check.setBlock(worldObj, air,0);
				}
			}
	}
	
	public boolean checkRails(double dir)
	{
		int rad = (int)(1+Math.floor(size/2));
		
		int[][] sides = {{rad,0},{-rad,0},{0,rad},{0,-rad}};
		
		boolean ret = true;
		
		for(int i = 0; i<5; i++)
		{
			ret = ret&&worldObj.getBlock((int)Math.floor(posX)+sides[axis?2:0][0],(int)Math.floor(posY+dir+i),(int)Math.floor(posZ)+sides[axis?2:0][1])==liftRail;
			ret = ret&&worldObj.getBlock((int)Math.floor(posX)+sides[axis?3:1][0],(int)Math.floor(posY+dir+i),(int)Math.floor(posZ)+sides[axis?3:1][1])==liftRail;
		}
		
		if((!ret&&dir==0))
		{
			axis = !axis;
			for(int i = 0; i<5; i++)
			{
				ret = ret&&worldObj.getBlock((int)Math.floor(posX)+sides[axis?2:0][0],(int)Math.floor(posY+dir+i),(int)Math.floor(posZ)+sides[axis?2:0][1])==liftRail;
				ret = ret&&worldObj.getBlock((int)Math.floor(posX)+sides[axis?3:1][0],(int)Math.floor(posY+dir+i),(int)Math.floor(posZ)+sides[axis?3:1][1])==liftRail;
			}
		}
		
		return ret;
	}
	
	private boolean consumePower()
	{
		boolean power = false;
		int sizeFactor = size == 1?4:size==3?23:55;
		double energyCost = (destinationY - posY)*ENERGYCOST*sizeFactor;
		if(energyCost<=0)
			return true;
		
//		if(energyCost>0&&source==null)
//		{
//			toMoveY = false;
//		}
//		if(source!=null)
//		{
//			double available = source.getJoules();
//			if(available > energyCost)
//			{
//				power = true;
//				source.setJoules(available-energyCost);
//			}
//		}
		if(!power)
			toMoveY = false;
		return power;
		
	}
    public void checkCollision()
    {
        List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, AxisAlignedBB.getBoundingBox(posX - (size+1), posY, posZ - (size+1), posX+(size+1), posY + 6, posZ + (size+1)));
        
        setOffsets();
        setBoxes();
        
        if (list != null && !list.isEmpty())
        {
        	if(list.size() == 1 && this.riddenByEntity!=null)
        	{
        		return;
        	}
        	
            for (int i = 0; i < list.size(); ++i)
            {
                Entity entity = (Entity)list.get(i);
//                if(entity!=this.riddenByEntity&&!(entity instanceof CopyOfEntityLift))
                {
                	applyEntityCollision(entity);
                }
            }
        }
    }
	
    /**
     * Applies a velocity to each of the entities pushing them away from each other. Args: entity
     */
    public void applyEntityCollision(Entity entity)
    {
    	boolean collided = false;
    	for(String key: boxes.keySet())
    	{
    		Matrix3 box = boxes.get(key);
    		Vector3 offset = new Vector3();
    		if(offsets.containsKey(key))
    		{
    			offset = offsets.get(key);
    		}
    		if(box!=null)
    		{
    			boolean push = box.pushOutOfBox((Entity)this, entity, offset);
    			collided = push || collided;
    			if(key.contains("top")||key.contains("base"))
    			{
                    if(AUGMENTG&&push&&toMoveY&&!up)
                    {
                    	entity.motionY+=motionY;
                    }
    			}
    		}
    	}
    	
    	if(!collided)
    	{
	    	Vector3 rotation = mainBox.boxRotation();	
	    	Vector3 r = ((new Vector3(entity)).subtract(new Vector3(this)));
	    	if(!(rotation.y==0&&rotation.z==0))
	    	{
	    		r = r.rotateAboutAngles(rotation.y, rotation.z);
	    	}
	    	if(r.inMatBox(mainBox))
	    	{
	    		entity.setPosition(entity.posX + motionX, entity.posY, entity.posZ+motionZ);
	    	}
    	}
    	
    }
	
    /**
     * First layer of player interaction
     */
    public boolean interactFirst(EntityPlayer player)
    {
    	ItemStack item = player.getHeldItem();
    	System.out.println("interact");
    	if(player.isSneaking()&&item!=null&&item.getItem() instanceof ItemLinker)
    	{
           	if(item.stackTagCompound == null)
        	{
        		item.setTagCompound(new NBTTagCompound() );
        	}
           	item.stackTagCompound.setInteger("lift", id);
           	if(worldObj.isRemote)
           	player.addChatMessage(new ChatComponentText("lift set"));
           	return true;
    	}
    	if(player.isSneaking()&&item!=null&&(
    			player.getHeldItem().getItem().getUnlocalizedName().toLowerCase().contains("wrench")
				 ||player.getHeldItem().getItem().getUnlocalizedName().toLowerCase().contains("screwdriver")
				 ||player.getHeldItem().getItem().getUnlocalizedName().equals(Items.stick.getUnlocalizedName())
    			))
		 {
    		if(worldObj.isRemote)
    		{
    			player.addChatMessage(new ChatComponentText("killed lift"));
    		}
    		setDead();
    		return true;
    	}
    	if(item!=null&&(
				 player.getHeldItem().getItem().getUnlocalizedName().toLowerCase().contains("wrench")
				 ||player.getHeldItem().getItem().getUnlocalizedName().toLowerCase().contains("screwdriver")))
		 {

    		axis = !axis;
    		return true;
		 }
    	
    	return false;
    }
	
    /**
     * Will get destroyed next tick.
     */
    public void setDead()
    {
    	if(!worldObj.isRemote&&!this.isDead)
    	{
    		int iron = size == 1?4:size==3?23:55;
	    	this.dropItem(Item.getItemFromBlock(Blocks.iron_block), iron);
	    	this.dropItem(Item.getItemFromBlock(ThutBlocks.lift), 1);
    	}
        super.setDead();
    }
	

	@Override
	public void writeSpawnData(ByteBuf data) {
		data.writeDouble(size);
		data.writeInt(id);
	}

	@Override
	public void readSpawnData(ByteBuf data) {
		size = data.readDouble();
		this.setSize((float)this.size, 1f);
		id = data.readInt();
	}

	public void setFoor(TileEntityLiftAccess te, int floor)
	{
		if(te.floor == 0)
		{
			int j = 0;
			for(int i = 0; i<4; i++)
			{
				if(floors[floor-1][i]==null)
				{
					j = i;
					break;
				}
			}
			floors[floor-1][j]=te;
			floorArray[floor-1][j] = new int[]{te.xCoord,te.yCoord,te.zCoord};
		}
		else if(te.floor!=0)
		{
			for(int i = 0; i<4; i++)
			{
				if(floors[te.floor-1][i] == te)
				{
					floors[te.floor-1][i] = null;
				}
			}
			int j = 0;
			for(int i = 0; i<4; i++)
			{
				if(floors[floor-1][i]==null)
				{
					j = i;
					break;
				}
			}
			floors[floor-1][j]=te;
			floorArray[floor-1][j] = new int[]{te.xCoord,te.yCoord,te.zCoord};
		}
	}

//	@Override
//	protected void entityInit()
//	{
//		super.entityInit();
//	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		axis = nbt.getBoolean("axis");
		id = nbt.getInteger("cuid");
		MAXID = nbt.getInteger("MAXID");
		size = nbt.getDouble("size");
		lifts.put(id, this);
		readList(nbt);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
//		super.writeEntityToNBT(nbt);
		nbt.setBoolean("axis", axis);
		nbt.setInteger("cuid", id);
		nbt.setInteger("MAXID", MAXID);
		nbt.setDouble("size", size);
		writeList(nbt);
	}

	public void writeList(NBTTagCompound nbt)
	{
		for(int i = 0; i<floorArray.length; i++)
		{
			for(int j = 0; j<4; j++)
			{
				nbt.setIntArray("list"+i+" "+j, floorArray[i][j]);
			}
		}
	}
	
	public void readList(NBTTagCompound nbt)
	{
		for(int i = 0; i<floorArray.length; i++)
		{
			for(int j = 0; j<4; j++)
			{
				int[] loc = nbt.getIntArray("list"+i+" "+j);
			//System.out.println(Arrays.toString(loc));
				floorArray[i][j] = loc;
			}
		}
	}
	

	@Override
	public void setBoxes()
	{
		mainBox = new Matrix3(new Vector3(-size/2,0,-size/2), new Vector3(size/2,5,size/2));
        boxes.put("base", new Matrix3(new Vector3(-size/2,0,-size/2), new Vector3(size/2,1,size/2)));
        boxes.put("top", new Matrix3(new Vector3(-size/2,0,-size/2), new Vector3(size/2,0.5,size/2)));
        boxes.put("wall1", new Matrix3(new Vector3(-0.5,0,-0.5), new Vector3(0.5,5,0.5)));
        boxes.put("wall2", new Matrix3(new Vector3(-0.5,0,-0.5), new Vector3(0.5,5,0.5)));
	}

	@Override
	public void setOffsets() 
	{
		offsets.put("top", new Vector3(0,5*0.9,0));
    	double wallOffset = size/2 + 0.5;
    	if(!axis)
    	{
    		offsets.put("wall1",new Vector3(wallOffset,0,0));
	    	offsets.put("wall2",new Vector3(-wallOffset,0,0));
    	}
    	else
    	{
    		offsets.put("wall1",new Vector3(0,0,wallOffset));
	    	offsets.put("wall2",new Vector3(0,0,-wallOffset));
    	}
	}

	@Override
	public ConcurrentHashMap<String, Matrix3> getBoxes() 
	{
		return boxes;
	}

	@Override
	public void addBox(String name, Matrix3 box) 
	{
		boxes.put(name, box);
	}

	@Override
	public ConcurrentHashMap<String, Vector3> getOffsets()
	{
		return offsets;
	}

	@Override
	public void addOffset(String name, Vector3 offset) 
	{
		offsets.put(name, offset);
	}

	@Override
	public Matrix3 bounds(Vector3 target) {
		return new Matrix3(new Vector3(-size/2,0, -size/2), new Vector3(size/2, 5, size/2));
	}
	
	 /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, int damage)
    {
    	if(damage>15)
    	{
//    		this.health -= damage;
    		return true;
    	}
    	
    	return false;
    }

	@Override
	protected void entityInit() {
		super.entityInit();
	}

	@Override
	public ItemStack getHeldItem() {
		return null;
	}

	@Override
	public ItemStack getEquipmentInSlot(int var1) {
		return null;
	}

	@Override
	public void setCurrentItemOrArmor(int var1, ItemStack var2) {
	}

	@Override
	public ItemStack[] getLastActiveItems() {
		return null;
	}


}
