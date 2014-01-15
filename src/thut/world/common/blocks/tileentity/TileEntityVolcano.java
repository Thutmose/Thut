package thut.world.common.blocks.tileentity;

import static net.minecraftforge.common.ForgeDirection.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.BlockMultipart;
import codechicken.multipart.TileMultipart;

import com.google.common.io.ByteArrayDataInput;

import thut.api.Blocks;
import thut.api.explosion.ExplosionCustom;
import thut.api.explosion.ExplosionCustom.Cruncher;
import thut.api.explosion.Vector3;
import thut.world.client.ClientProxy;
import thut.world.common.Volcano;
import thut.world.common.WorldCore;
import thut.world.common.blocks.BlockMisc;
import thut.world.common.blocks.crystals.CrystalPart;
import thut.world.common.blocks.fluids.BlockFluid;
import thut.world.common.blocks.fluids.dusts.BlockDust;
import thut.world.common.blocks.fluids.liquids.BlockLava;
import thut.world.common.blocks.fluids.solids.BlockSolidLava;
import thut.world.common.blocks.world.BlockVolcano;
import thut.world.common.corehandlers.ConfigHandler;
import thut.world.common.network.PacketInt;
import thut.world.common.network.PacketVolcano;
import thut.world.common.ticks.ThreadSafeWorldOperations;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;

public class TileEntityVolcano extends TileEntity
{
	
	public int typeid = 10;
	public int height = 0;
	public int ventCount = 0;
	public int sulfurVentCount = 0;
    int n=0;
    
    public boolean grown = false;
   
    public Volcano v;
    
    public static List<Integer> replaceable =new ArrayList<Integer>();
    public static List<Integer> lava = new ArrayList<Integer>();
    public static List<Integer> solidlava = new ArrayList<Integer>();
    public boolean firstTime = true;
    public boolean erupted = false;
    public boolean active = false;
    public static int ashAmount = ConfigHandler.ashAmount;
    public static int RAPIDRATE = 5;
    final ThreadSafeWorldOperations safe = new ThreadSafeWorldOperations();
    final boolean[] plumes = {false};

    public static double eruptionStartRate;
    public static double eruptionStopRate;
    
    public int z;
    
    public static int minorExplosionRate;
    public static int majorExplosionRate;
    
    public static double dormancyRate;
    public static double activityRate;
    
    public long age = 0;
    
    public String[] types = 
    	{
    		"Mafic",
    		"Intermediate",
    		"Felsic",
    	};
    
 //   public int[][] sides = {{0,0}};//{{0,1},{0,-1},{1,0},{-1,0},{1,1},{1,-1},{-1,1},{-1,-1},{0,0}}; TODO
 //   public int[][] extendedSides = {{0,1},{0,-1},{1,0},{-1,0}}; //{{0,2},{0,-2},{2,0},{-2,0},{2,1},{2,-1},{-2,1},{-2,-1}};
    
    public int[][][] sidesA = {
    		{{0,0}},
    		{{0,1},{0,-1},{1,0},{-1,0},{0,0}},
    		{{0,1},{0,-1},{1,0},{-1,0},{1,1},{1,-1},{-1,1},{-1,-1},{0,0}},
    		{
    			{0,1},{0,-1},{1,0},{-1,0},{1,1},{1,-1},{-1,1},{-1,-1},{0,0},
    			{0,2},{0,-2},{2,0},{-2,0},{2,1},{2,-1},{-2,1},{-2,-1}
    		}
    		
    		
    };
    public int[][][] extendedSidesA = {
    		{{0,1},{0,-1},{1,0},{-1,0}},
    		{{1,1},{1,-1},{-1,1},{0,2},{0,-2},{2,0},{-2,0}},
    		{{0,2},{0,-2},{2,0},{-2,0},{2,1},{2,-1},{-2,1},{-2,-1}},
    		{
    			{0,-3},{1,-3},{-1,-3},
    			{0,3},{1,3},{-1,3},
    			{3,0},{3,-1},{3,1},
    			{-3,0},{-3,1},{-3,-1},
    			{2,2},{2,-2},{-2,2},{-2,-2},
    			
    		}
    };
    
    public List<Vect> sideVents = new ArrayList<Vect>();
    public List<Vect> sulfurVents = new ArrayList<Vect>();
    public Vect mainVent = new Vect(0,1,0);
    
    public Vector3 here;
    
	public List<PlumeParticle> particles = new ArrayList<PlumeParticle>();
	
	public boolean doop = false;
	
	private double x0,y0,z0;

	public double r0 = 0;
	
	private int num = 15;
	private int dustId = 0;
	private int lavaID;

	public boolean first = true;
	public boolean dormant = false;
	public int time = 0;
	Random rand = new Random();
	int index = 0;
	int otherindex = 0;
	public int growthTimes = 0;
	public static int tickRate;
	public int activeCount = 0;
	
	@Override
	public void updateEntity()
	{
		if(firstTime)
		{
			init();
		}
		if(worldObj.isRemote)
			return;
//		if(!firstTime)
//		{
//			return;
//		}
		if(tickRate<=0)
		{
			tickRate = 1;
		}
		if(age%(tickRate)==0&&ConfigHandler.volcanosActive&&worldObj.doChunksNearChunkExist(xCoord, yCoord, z, 48))
		{
			this.active = true;
			ConfigHandler.debugPrints = true;
			double growthFactor = v.growthFactor;
			double majorFactor = v.majorFactor;
			double minorFactor = v.minorFactor;
			double activeFactor = v.activeFactor;
		//	System.out.println(age);
			if(!dormant)
			{
				volcanoTick();
				plumeTick();
				checkSize();
				
				maintainMagma();
				if(rand.nextGaussian()>dormancyRate)
				{
					dormant = true;
					v.activeFactor = 0.01;
					if(ConfigHandler.debugPrints)
					System.out.println(types[typeid]+" Volcano at location "+xCoord+" "+z+" changed to activity state: "+getState()+" "+worldObj);
				}
			}
			else if(rand.nextGaussian()>activityRate)
			{
				dormant = false;
				v.activeFactor = 1;
				if(ConfigHandler.debugPrints)
				System.out.println(types[typeid]+" Volcano at location "+xCoord+" "+z+" changed to activity state: "+getState()+" "+worldObj);
			}
			if(v.growthFactor!=growthFactor||v.minorFactor!=minorFactor||v.majorFactor!=majorFactor||v.activeFactor!=activeFactor)
				worldObj.markBlockForUpdate(xCoord, yCoord, z);
		}
		age++;
	}
	
	public void checkSize()
	{
		if(!grown)
		{
			int maxLength = height + 64 + 1;
			boolean test = false;
			
			int index = 2-typeid;
			
			for(int[] a: extendedSidesA[index])//TODO revert if needed
			{
				Vector3 vec = new Vector3(xCoord+a[0], (int) (maxLength), this.z+a[1]);
				int id = vec.getBlockId(worldObj);
				if(solidlava.contains(id))
				{
					test = true;
					break;
				}
			}
			grown = test;
			if(grown)
			{
				if(ConfigHandler.debugPrints)
					System.out.println("max size "+(maxLength));
			}
		}
	}
	
	public void resetGrowthFactor()
	{
		growthTimes++;
		activeCount = 0;
		v.growthFactor = 1;
	}

	public void resetMajorFactor()
	{
		v.majorFactor = 1;
	}
	public void resetMinorFactor()
	{
		v.minorFactor = 1;
	}
	
	public void setDormancy(boolean bool)
	{
		dormant = bool;
		if(ConfigHandler.debugPrints)
		System.out.println(types[typeid]+" Volcano at location "+xCoord+" "+z+" changed to activity state: "+getState()+" "+worldObj);
	}
	
    /**
     * invalidates a tile entity
     */
    public void invalidate()
    {
    	typeid = 10;
    	Volcano.removeVolcano(xCoord, z);
        this.tileEntityInvalid = true;
    }

	
	private void init()
	{
		dustId = Blocks.dust.blockID;
		ashAmount = ConfigHandler.ashAmount;
		firstTime = false;
		here = new Vector3(this);
		MinecraftForge.EVENT_BUS.register(this);
		v = Volcano.getVolcano(xCoord, z, worldObj);
		if(typeid>2)
		{
			height = v.h;
			typeid = v.type;
			r0 = height/2;
			n = ashAmount*(typeid*typeid+1);
			ventCount = (int) (10*Math.random());
			sulfurVentCount = (int)(10*Math.random());
			mainVent.i = height+64-yCoord;
			mainVent.r = 2*rand.nextInt(majorExplosionRate);
			mainVent.k = 10*(1);
			mainVent.j = 0;
			mainVent.bool = false;
			mainVent.var1 = 0;
			for(int i = 0; i<ventCount; i++)
			{
				sideVents.add(new Vect(new double[] {2*(Math.random()-0.5), 0.25+Math.random(), 2*(Math.random()-0.5)},
				height, mainVent.i*Math.random(), Math.random()*1*(1), 0.85*mainVent.r , false));
			}
			for(int i = 0; i<sulfurVentCount; i++)
			{
				sulfurVents.add(new Vect(new double[] {2*(Math.random()-0.5), 0.1+Math.random()*0.75, 2*(Math.random()-0.5)},
				height, mainVent.i*Math.random(), Math.random()*2*(1), 0.85*mainVent.r , false));
			}
		}
		if(!worldObj.isRemote&&ConfigHandler.debugPrints)
		{
			System.out.println("Initiated "+types[typeid]+" Volcano at location "+xCoord+" "+z+" of activity state: "+getState()+
					   ". Maximum Height of:  "+(height+64)+". Number of Vents: "+(sideVents.size()+1)+". Number of sulfur vents: "+(sulfurVents.size()));
		}
		lavaID = BlockLava.getInstance(typeid).blockID;
		if(replaceable.size()==0){
			replaceable.add(0);
			replaceable.add(Block.stone.blockID);
			replaceable.add(Block.gravel.blockID);
			replaceable.add(Block.grass.blockID);
			replaceable.add(Block.waterMoving.blockID);
			replaceable.add(Block.waterStill.blockID);
			replaceable.add(Block.lavaMoving.blockID);
			replaceable.add(Block.lavaStill.blockID);
			replaceable.add(Blocks.dust.blockID);
			
			if(!lava.contains(BlockLava.getInstance(0).blockID))
			{
	
				for(Block block:Block.blocksList){
					if(block!=null){
					String name = block.getUnlocalizedName().toLowerCase();
					if(name.contains("ore")
							||name.contains("dirt")	
							||name.contains("sand")	
							||name.contains("stone")		
							||name.contains("chalk")		
							||name.contains("rock")	
							||block.getExplosionResistance(null)<100
							)
					{
						replaceable.add(block.blockID);
					}
					}
				}
				for(int i=0;i<3;i++){
					solidlava.add(BlockSolidLava.getInstance(i).blockID);
					lava.add(BlockLava.getInstance(i).blockID);
				}
	
			}
		
		}
		worldObj.markBlockForUpdate(xCoord, yCoord, z);
	}
	
	private void setLava(double x, double y, double z)
	{
		if(!worldObj.isRemote)
		{
			int index = 2-typeid;
			for(int[] side : sidesA[index])//TODO revert if needed
			{
				int id = worldObj.getBlockId((int)x+side[0], (int)y, (int)z+side[1]);
				
				if((replaceable.contains(id)||solidlava.contains(id))||Block.blocksList[id]instanceof BlockMultipart)
				{
					worldObj.setBlock((int)x+side[0], (int)y, (int)z+side[1], BlockLava.getInstance(typeid).blockID, 15,3);
				}
				else if(lava.contains(id))
				{
					int meta = worldObj.getBlockMetadata((int)x+side[0], (int)y, (int)z+side[1]);
					if(meta!=15)
					{
						worldObj.setBlock((int)x+side[0], (int)y, (int)z+side[1], BlockLava.getInstance(typeid).blockID, 15,3);
					}
				}
			}
		}
	}
	
	private void setLava2(double x, double y, double z)
	{
		if(!worldObj.isRemote)
		{
			int index = 2-typeid;
			int id = Block.lavaStill.blockID;
			for(int[] side : sidesA[index])//TODO revert if needed
			{
				int id1 = worldObj.getBlockId((int)x+side[0], (int)y, (int)z+side[1]);
				
				if(((replaceable.contains(id1)||solidlava.contains(id1)))&&id!=id1)
				{
					worldObj.setBlock((int)x+side[0], (int)y, (int)z+side[1], id, 0,7);
				}
			}
		}
	}
	
	private void setLava3(double x, double y, double z)
	{
		if(!worldObj.isRemote)
		{
			int index = 2-typeid;
			int id = Block.lavaStill.blockID;
			for(int[] side : sidesA[index])//TODO revert if needed
			{
				int id1 = worldObj.getBlockId((int)x+side[0], (int)y, (int)z+side[1]);
				
				if(((replaceable.contains(id1)||solidlava.contains(id1)))&&id!=id1)
				{
					worldObj.setBlock((int)x+side[0], (int)y, (int)z+side[1], id, 0,7);
				}
			}
		}
	}
	
	void setSulfur(double x, double y, double z)
	{
		Vector3 location = new Vector3(x,y,z);
		int id = location.getBlockId(worldObj);
		if(!(replaceable.contains(id)||lava.contains(id)||solidlava.contains(id))||id==Blocks.sulfur.blockID||Block.blocksList[id]instanceof BlockMultipart)
			return;
		
		location.setBlock(worldObj, 0, 0);
		worldObj.removeBlockTileEntity(location.intX(), location.intY(), location.intZ());
		
		for(ForgeDirection side: ForgeDirection.VALID_DIRECTIONS)
		{
			
			id = location.offset(side).getBlockId(worldObj);
			if(!(replaceable.contains(id)||lava.contains(id)||solidlava.contains(id)))
				continue;
			if(id!=Blocks.sulfur.blockID&&!(Block.blocksList[id]instanceof BlockMultipart))
			{
				location.offset(side).setAir(worldObj);
				id=0;
			}
			for(ForgeDirection place: ForgeDirection.VALID_DIRECTIONS)
			{
				BlockCoord pos = new BlockCoord(location.offset(side).intX(),location.offset(side).intY(),location.offset(side).intZ());
				CrystalPart part = (CrystalPart)CrystalPart.placement(worldObj, pos, place.ordinal());
				if(part==null) continue;
				TileMultipart tile = TileMultipart.getOrConvertTile(worldObj, pos);
				//if(tile==null) continue;
				if(tile!=null&&tile.canAddPart(part))
				{
					tile.addPart(worldObj, pos, part);
				}
				else if(id==0)
				{
					location.offset(side).setBlock(worldObj,Blocks.sulfur.blockID, place.ordinal());
				}
			}
		}
	}
	
   public void writeToNBT(NBTTagCompound par1)
   {
	   super.writeToNBT(par1);
	   par1.setInteger("type", typeid);
	   par1.setInteger("h", height);
	   par1.setInteger("z location", z);
	   par1.setInteger("veinCount", ventCount);
	   par1.setInteger("sulfurveinCount", sulfurVents.size());
	   par1.setInteger("growth", growthTimes);
	   for(int i = 0; i<ventCount; i++)
	   {
		   Vect vec = sideVents.get(i);
		   if(vec!=null)
		  	vec.writeToNBT(par1, Integer.toString(i)+"side");
	   }
	   for(int i = 0; i<sulfurVents.size(); i++)
	   {
		   Vect vec = sulfurVents.get(i);
		   if(vec!=null)
			   vec.writeToNBT(par1, Integer.toString(i)+"sulfur");
	   }
	   mainVent.writeToNBT(par1, "main");
	   par1.setBoolean("active", active);
	   par1.setBoolean("erupted", erupted);
	   par1.setBoolean("grown", grown);
	   par1.setBoolean("dormant", dormant);
	   par1.setLong("age", age);
	   par1.setInteger("activeAge", activeCount);
   }

   public void readFromNBT(NBTTagCompound par1)
   {
      super.readFromNBT(par1);
      typeid = par1.getInteger("type");
      height = par1.getInteger("h");
      growthTimes = par1.getInteger("growth");
      z = par1.getInteger("z location");
      ventCount = par1.getInteger("veinCount");
	   for(int i = 0; i<ventCount; i++)
	   {
		   sideVents.add(Vect.readFromNBT(par1,Integer.toString(i)+"side"));
	   }
       sulfurVentCount = par1.getInteger("sulfurveinCount");
	   for(int i = 0; i<sulfurVentCount; i++)
	   {
		   sulfurVents.add(Vect.readFromNBT(par1,Integer.toString(i)+"sulfur"));
	   }
	   mainVent = Vect.readFromNBT(par1, "main");
	   mainVent.var1 = 0;
	   active = par1.getBoolean("active");
	   erupted = par1.getBoolean("erupted");
	   dormant = par1.getBoolean("dormant");
	   grown = par1.getBoolean("grown");
	   age = par1.getLong("age");
	   activeCount = par1.getInteger("activeAge");
	   if(typeid>2)
		   typeid=2;
	   if(ConfigHandler.debugPrints)
	   System.out.println("Loaded "+types[typeid]+" Volcano at location "+xCoord+" "+z+" of activity state: "+getState()+
			   ". Maximum Height of:  "+(height+64)+". Number of Vents: "+(sideVents.size()+1)+". Number of sulfur vents: "+(sulfurVents.size()));
   }

   public int getZCoord()
   {
	   return z;
   }
   
   public String getState()
   {
	   String dormancy = dormant?"dormant":"active";
	   String activity = active?"erupting":"not erupting";
	   String state = dormant?dormancy:dormancy+" "+activity;
	   return state;
   }
   
   private void volcanoTick()
	   {
	//   active = false;
		   if(active&&rand.nextGaussian()>0)
			{
			   if(grown)
				  for(Vect v:sideVents)
					  growVent(v);
			  
			  growVent(mainVent);
			  
			  if(rand.nextGaussian()>eruptionStopRate&&!worldObj.isRemote)
			  {
				active = false;
				v.activeFactor = 0.5;
				if(ConfigHandler.debugPrints)
				System.out.println(types[typeid]+" Volcano at location "+xCoord+" "+z+" changed to activity state: "+getState());
			  }
			  
			  activeCount++;
			}
		   if(grown&&Math.random()>0.8)
				  for(Vect v:sulfurVents)//TODO
					  growSulfur(v); 
		  if (!active&rand.nextGaussian()>eruptionStartRate&&!worldObj.isRemote)
		   {
			   active = true;
				v.activeFactor = 1;
			   if(ConfigHandler.debugPrints)
				System.out.println(types[typeid]+" Volcano at location "+xCoord+" "+z+" changed to activity state: "+getState());
		   }
	   }
   
   private void rapidGrowthTick()
   {
	   double rad = 16;
	   	x0 = xCoord+mainVent.x; y0 =  yCoord+mainVent.y+mainVent.var1; z0 = getZCoord()+mainVent.z;
	   	r0 = rad;
	   	num = 0;
	   	n = (int)(1*4000);
		worldObj.createExplosion(null, x0, y0, z0, (float) rad, false);
	   	addPlumeParticles(0.5, BlockLava.getInstance(typeid).blockID);
	   	erupted = true;
	   	doop = false;
   }
	      
   private void plumeTick()
	   {
		   if(!worldObj.isRemote&&(!grown||ashAmount>1&&particles.size()>0))
		   {
				Vector3 vec = new Vector3(xCoord+0.5, yCoord+1.5, z+0.5);
				vec = (vec.getNextSurfacePoint(worldObj, vec, new Vector3(0,1,0), 255-yCoord));
			    if((vec==null))
			    {
			    	if(!grown)
					{
						double factor = 1-((double)RAPIDRATE - (double)activeCount)/(double)RAPIDRATE;
						v.growthFactor = 1+(factor);
					}
					else
					{
						v.growthFactor = 1;
					}
					if(!plumes[0]&&particles.size()>0)
					{
//					    Thread nextBoom = new Thread(new Runnable() {
//						      public void run() { 
								plumes[0]=true;
								plumeCalculations(worldObj, particles);
								plumes[0]=false;
//						      }});
//					    nextBoom.start();
					}
			    }
		   }

			if((!grown&&activeCount>RAPIDRATE)&&!worldObj.isRemote)
			{
			//	if(ConfigHandler.debugPrints)
			//	System.out.println(types[typeid]+" Volcano at location "+xCoord+" "+z+" growth Event "+growthTimes+" "+worldObj);
				growthTimes++;
				activeCount = 0;
				v.growthFactor = 1;
				rapidGrowthTick();
				
			}
	   }

	private void plumeCalculations(final World worldObj,final List<PlumeParticle> particles)
	{

		List<PlumeParticle> deadParticles = new ArrayList<PlumeParticle>();
		if(particles.size()==0)
		{
			if(!active&&erupted)
			{
				active = true;
				if(ConfigHandler.debugPrints)
				System.out.println(types[typeid]+" Volcano at location "+xCoord+" "+z+" changed to activity state: "+getState());
			}
			erupted = false;
			return;
		}
		int count = 0;
		for(PlumeParticle p: particles)
		{
			Vector3 vec = new Vector3(p.x,p.y,p.z);
		//	int y = (int)p.y;
			int id = safe.getID(worldObj, vec);//vec.getBlockId(worldObj);// worldObj.getBlockId((int)p.x, y, (int)p.z);
			int idP = (int)p.vy;
			int h = vec.intY();
	    	Vector3 vec1 = vec.offset(DOWN);
	    	int id1 = safe.getID(worldObj, vec1);
	        boolean canBreak = BlockFluid.willBreak(idP,id);
	    	double dx=0,dz=0;
	    	double density = ((BlockFluid)Block.blocksList[idP]).getFluid().getDensity();
	    	
	    	double factor = vec.y>80?1/(density*density/1E6):0;
	    	boolean combineDown = ((BlockFluid)Block.blocksList[idP]).willCombine(idP, vec1, worldObj);
	        if(combineDown)
	    	{
		    	
	    		dx = ThreadSafeWorldOperations.getWind(worldObj, vec.x, vec.z).x*factor;
	    		dz = ThreadSafeWorldOperations.getWind(worldObj, vec.x, vec.z).y*factor;
	    		
		    	boolean combine;
		    	while(h>1)
		    	{
		    		vec1.set(vec.x+(int)(dx*(1+vec.y-h)), h-1, vec.z+(int)(dz*(1+vec.y-h)));
		    		dx = ThreadSafeWorldOperations.getWind(worldObj, vec.x+(int)(dx*(1+vec.y-h)), vec.z+(int)(dz*(1+vec.y-h))).x*factor;
		    		dz = ThreadSafeWorldOperations.getWind(worldObj, vec.x+(int)(dx*(1+vec.y-h)), vec.z+(int)(dz*(1+vec.y-h))).y*factor;
		    		id1 = safe.getID(worldObj, vec1);//vec1.getBlockId(worldObj);
		    		
		    		combine = ((BlockFluid)Block.blocksList[idP]).willCombine(idP, vec1, worldObj);
		    		if(!(combine))
		    		{
		    			break;
		    		}
		    		h--;
		    	}
	    	}
	        safe.set(worldObj, vec1.offset(UP), idP, 15);
	        if(idP==Blocks.getLava(typeid).blockID)
	        	setBiome(worldObj, vec1.intX(), vec1.intZ(), WorldCore.volcano);
	        deadParticles.add(p);
	        if(count > particles.size()/1)
	        	break;
	        count++;
		}
		
		particles.removeAll(deadParticles);
		if(ConfigHandler.debugPrints&&worldObj.isRemote&&WorldCore.proxy.getPlayer()!=null)
		{
			String message = "Particles Remaining"+(particles.size());
			WorldCore.proxy.getPlayer().addChatMessage(message);
		}
	}
	
	private void addPlumeParticles(double Hfactor, int id)
	{
		int typeFactor = typeid == 2? (int)(Hfactor*50+200): typeid==1? (int)(Hfactor*50+150): (int)(Hfactor*74+136);
		Random r = new Random();
		r0 = Math.min(r0,50); //Limits the size to "50"
		while(n>0)
		{
			double x,z, dx=0, dz=0, h;
		
			x = r0*r.nextGaussian();
			z = r0*r.nextGaussian();	

			int y=typeFactor-r.nextInt(50);
			h = y-1;	

	    	int id1 = worldObj.getBlockId((int)x+(int)(dx*(y-h)), (int)h, (int)z+(int)(dz*(y-h)));
			
	    	boolean fell = false;

	    	id1 = worldObj.getBlockId((int)x+(int)(dx*(y-h)), (int)h, (int)z+(int)(dz*(y-h)));
	    	if(!(id1==0||BlockFluid.willBreak(id, id1)||(id==BlockLava.getInstance(typeid).blockID&&(id1==Block.waterMoving.blockID||id1==Block.waterStill.blockID))))
	    	{
	    		n--;
	    	}
	    	else
	    	{
			PlumeParticle particle = new PlumeParticle();
			
			particle.x = x+x0;
			particle.y = h;
			particle.z = z+z0;
			
			particle.vx = num;
			particle.vy = id;
			particles.add(particle);
			n--;
	    	}
		}
		
	}
	
	void growVent(Vect vent)
	{
		double maxLength = vent.i;
		
		double x = vent.x,y = vent.y,z = vent.z, h = vent.j, e = vent.k, r = vent.r;
		int countMinor = vent.var2, countMajor = vent.var3;
		int majorRate = (int) (vent==mainVent?majorExplosionRate:0.85*majorExplosionRate);
		
		if(vent!=mainVent && h>mainVent.var1)
		{
			return;
		}

		int i = 1;
		int id = 1;

		boolean toErupt = false;
		
		while(id!=0&&i*y+h<=maxLength)
		{
			i++;
			id = worldObj.getBlockId( (int)(xCoord+i*x),(int) (yCoord+i*y+h),(int) (getZCoord()+i*z));
			int meta = worldObj.getBlockMetadata( (int)(xCoord+i*x),(int) (yCoord+i*y+h),(int) (getZCoord()+i*z));
			boolean sideVent = vent!=mainVent;
			
			if(!(lava.contains(id)||replaceable.contains(id)||solidlava.contains(id))) break;
			
			if(i*y+h<maxLength-1)
			setLava(xCoord+i*x, yCoord+i*y+h, getZCoord()+i*z);
			if(i*y+h==maxLength-1&&vent == mainVent)
			{
				setLava3(xCoord+i*x, yCoord+i*y+h, getZCoord()+i*z);
			}
			
			if(sideVent&&id == 0||(id==lavaID&&meta!=15)) break;
			
			if(vent == mainVent)
			{
				mainVent.var1 = (int) (i*y+h);
			}
			if(i*y+h==maxLength)
				setLava2(xCoord+i*x, yCoord+i*y+h, getZCoord()+i*z);
		}
		
		
		if((doop&&vent==mainVent)||mainVent.var1 > 0.75*maxLength&&countMajor>majorExplosionRate)
		{
			toErupt = true;
			active = false;
			if(ConfigHandler.debugPrints)
			System.out.println(types[typeid]+" Volcano at location "+xCoord+" "+this.z+" changed to activity state: "+getState()+" "+worldObj);
		}
		

	  if(age>2500&&countMinor>minorExplosionRate)
	  {
		  Vector3 centre = new Vector3(xCoord+i*x, yCoord+i*y+h, getZCoord()+i*z);
		  
		  if(!worldObj.isRemote)
		  {
			  if(ConfigHandler.debugPrints)
				  System.out.println(types[typeid]+" Volcano at location "+xCoord+" "+this.z+" minor Explosion "+centre.toString()+" "+worldObj);
			
			  	double rad = Math.random()*75;
			  	ExplosionCustom.doExplosion(centre, worldObj, 64, (float) rad);
				worldObj.createExplosion(null, x0, y0, z0, (float) rad, false);
		    	worldObj.playSoundEffect(xCoord+i*x, yCoord+i*y+h, getZCoord()+i*z, "random.explode", 10.0F, 1.0F);
		    	
		    	x0 = xCoord+i*x; y0 =  yCoord+i*y+h; z0 = getZCoord()+i*z;
		    	r0 = rad/5;
		    	num = 0;
		    	n = (int)(0.01*ashAmount*Math.random());
		    	addPlumeParticles(0.5, BlockLava.getInstance(typeid).blockID);
		    	n = (int)(0.01*ashAmount*Math.random());
		    	addPlumeParticles(0.75, dustId);
		    	//*/
				//ExplosionEvent evt = new ExplosionEvent(worldObj, getExplosion(1, typeid, "Minor Eruption"));
		  }
			
	    	vent.var2 = 0;
	    	erupted = true;
	    	doop = false;
	  }
		
		if(age>2500&&toErupt)
		{
			//*/
			if(!worldObj.isRemote)
			{
				Vector3 centre = new Vector3(xCoord+i*x, yCoord+i*y+h+5, getZCoord()+i*z);
				double rad = Math.random()*e;
				//if(ConfigHandler.debugPrints)
				  System.out.println(types[typeid]+" Volcano at location "+xCoord+" "+this.z+" major Explosion "+centre.toString()+" "+worldObj+":"+rad);

				  	ExplosionCustom.doExplosion(centre, worldObj, 64, (float) rad);
				worldObj.createExplosion(null, x0, y0, z0, (float) rad, false);
		    	worldObj.playSoundEffect(xCoord+i*x, grown&&vent==mainVent?yCoord+maxLength+5:yCoord+i*y+h, getZCoord()+i*z, "random.explode", 10.0F, 1.0F);
		    	x0 = xCoord+i*x; y0 =  yCoord+i*y+h; z0 = getZCoord()+i*z;
		    	r0 = e;
		    	num = 7;
		    	n = (int) (vent==mainVent?ashAmount*(typeid*typeid+1):0.1*ashAmount*(typeid*typeid+1));
		    	addPlumeParticles(vent==mainVent?1:0.5, dustId);
		    	//*/
		    	doop = false;
			}
		    erupted = true;
		    vent.var3 = 0;
		}
		vent.var2++;
		vent.var3++;
		
		double majorDiff = 1-((double)majorExplosionRate - (double)vent.var3)/(double)majorExplosionRate;
		double minorDiff = 1-((double)minorExplosionRate - (double)vent.var2)/(double)minorExplosionRate;
		
		v.majorFactor = 1+(majorDiff*majorDiff);
		v.minorFactor = 1+(minorDiff*minorDiff);
		
		//if(vent==mainVent&&Math.random()>0.5&&ConfigHandler.debugPrints)
		//	System.out.println("major Factor: "+v.majorFactor+", Minor Factor: "+v.minorFactor+" "+worldObj );
	}
	
	void growSulfur(Vect vent)
	{
		double maxLength = vent.i;
		
		double x = vent.x,y = vent.y,z = vent.z, h = vent.j, e = vent.k, r = vent.r;

		if(h>mainVent.var1)
		{
			return;
		}

		int i = 4;
		int id = 1;
		
		while(i*y+h<=maxLength)
		{
			i++;
			id = worldObj.getBlockId( (int)(xCoord+i*x),(int) (yCoord+i*y+h),(int) (getZCoord()+i*z));
			int meta = worldObj.getBlockMetadata( (int)(xCoord+i*x),(int) (yCoord+i*y+h),(int) (getZCoord()+i*z));
			
			if(!(lava.contains(id)||replaceable.contains(id)||solidlava.contains(id))) break;
			setSulfur(xCoord+i*x, yCoord+i*y+h, getZCoord()+i*z);
			
		//	if(id == 0||(id==lavaID&&meta!=0)) break;
		}		
		while(i*y+h<=maxLength)
		{
			i++;
			id = worldObj.getBlockId( (int)(xCoord+i*x),(int) (yCoord+i*y+h+1),(int) (getZCoord()+i*z));
			int meta = worldObj.getBlockMetadata( (int)(xCoord+i*x),(int) (yCoord+i*y+h+1),(int) (getZCoord()+i*z));
			
			if(!(lava.contains(id)||replaceable.contains(id)||solidlava.contains(id))) break;
			setSulfur(xCoord+i*x, yCoord+i*y+h+1, getZCoord()+i*z);
			
		//	if(id == 0||(id==lavaID&&meta!=0)) break;
		}
	}
	
	void maintainMagma()
	{
		
		setLava(xCoord, yCoord+1, z);
		if(yCoord>=40) return;
		
		for(int h = yCoord+1; h<40; h++)
		{
			int id = worldObj.getBlockId(xCoord, h, z);
			if(!(lava.contains(id)||replaceable.contains(id)||solidlava.contains(id))) break;
			
			setLava(xCoord,h, getZCoord());
		}
	}
	
    @Override
    public Packet getDescriptionPacket()
    {
    	if(v==null&&worldObj!=null)
    	{
    		v = Volcano.getVolcano(xCoord, z, worldObj);
    	}
    	if(v!=null)
    		return PacketVolcano.getPacket(this);
    	return null;
    }

    public byte booleansToByte()
    {
    	byte ret = 0;
    	
    	ret |= (active?1:0)<<0;
    	ret |= (dormant?1:0)<<1;
    	ret |= (grown?1:0)<<2;
    	
    	return ret;
    }
    
    public void byteToBools(byte b)
    {
    	active = ((b>>0)&1) == 1;
    	dormant = ((b>>1)&1) == 1;
    	grown = ((b>>2)&1) == 1;
    }
    
    public static void setBiome(World worldObj, int x, int z, BiomeGenBase biome)
    {
    	Vector3 loc = new Vector3(x,0,z);
    	loc.setBiome(biome, worldObj);
    }
    
   	public static class PlumeParticle
	{
		public double x,y,z,vx,vy,vz,dvy;
		
		public static PlumeParticle readFromNBT(NBTTagCompound cmpnd, String tag)
		{
			PlumeParticle tempParticle = new PlumeParticle();
			tempParticle.x = cmpnd.getDouble(tag+"x");
			tempParticle.y = cmpnd.getDouble(tag+"y");
			tempParticle.z = cmpnd.getDouble(tag+"z");
			tempParticle.vx = cmpnd.getDouble(tag+"vx");
			tempParticle.vy = cmpnd.getDouble(tag+"vy");
			tempParticle.vz = cmpnd.getDouble(tag+"vz");
			tempParticle.dvy = cmpnd.getDouble(tag+"dvy");
			
			if(tempParticle.x==tempParticle.y&&tempParticle.x==tempParticle.z&&tempParticle.x==0){
				return null;
			}
			return tempParticle;
			
		}
		
		public void writeToNBT(NBTTagCompound cmpnd,String tag){

			cmpnd.setDouble(tag+"x",this.x);
			cmpnd.setDouble(tag+"y",this.y);
			cmpnd.setDouble(tag+"z",this.z);
			cmpnd.setDouble(tag+"vx",this.vx);
			cmpnd.setDouble(tag+"vy",this.vy);
			cmpnd.setDouble(tag+"vz",this.vz);
			cmpnd.setDouble(tag+"dvy",this.dvy);
		}
		
		}
		
	public static class Vect
	 {
		 public double x,y,z,i,j,k,r;
		 public boolean bool, bool2;
		 
		 public int var1,var2,var3, var4 =0;
		 
		 public Vect(double x, double y, double z)
		 {
			 this.x = x;
			 this.y = y;
			 this.z = z;
		 }
		 
		 public Vect(double x, double y, double z, double t, double h, double e, double r)
		 {
			 this.x = x;
			 this.y = y;
			 this.z = z;
			 this.i = t;
			 this.j = h;
			 this.k = e;
			 this.r = r;
		 }
		 
		 public Vect(double[] vec,  double i, double j, double k, double r, boolean bool)
		 {
			 double[] dir = (new Vector3(vec)).normalize().toArray();
			 this.x = dir[0];
			 this.y = dir[1];
			 this.z = dir[2];
			 this.i = i;
			 this.j = j;
			 this.k = k;
			 this.r = r;
			 this.bool = bool;
		 }
		 
		 public Vect(){}
		 
			public static Vect readFromNBT(NBTTagCompound cmpnd, String tag)
			{
				Vect tempVect = new Vect();
				tempVect.x = cmpnd.getDouble(tag+"x");
				tempVect.y = cmpnd.getDouble(tag+"y");
				tempVect.z = cmpnd.getDouble(tag+"z");
				tempVect.i = cmpnd.getDouble(tag+"t");
				tempVect.j = cmpnd.getDouble(tag+"h");
				tempVect.k = cmpnd.getDouble(tag+"e");
				tempVect.r = cmpnd.getDouble(tag+"r");
				tempVect.bool = cmpnd.getBoolean(tag+"bool");
				tempVect.var1 = cmpnd.getInteger(tag+"var1");
				tempVect.var2 = cmpnd.getInteger(tag+"var2");
				tempVect.var3 = cmpnd.getInteger(tag+"var3");
				tempVect.var4 = cmpnd.getInteger(tag+"var4");
				
				if(tempVect.x==tempVect.y&&tempVect.x==tempVect.z&&tempVect.x==0){
					return null;
				}
				return tempVect;
			}
			
			public void writeToData(DataOutputStream dos)
			{
				try {
					
					dos.writeDouble(x);
					dos.writeDouble(y);
					dos.writeDouble(z);
					dos.writeDouble(i);
					dos.writeDouble(j);
					dos.writeDouble(k);
					dos.writeDouble(r);

					dos.writeInt(var1);
					dos.writeInt(var2);
					dos.writeInt(var3);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			public static Vect readFromData(ByteArrayDataInput dat)
			{
				Vect tempVect = new Vect();
				
				tempVect.x = dat.readDouble();
				tempVect.y = dat.readDouble();
				tempVect.z = dat.readDouble();
				tempVect.i = dat.readDouble();
				tempVect.j = dat.readDouble();
				tempVect.k = dat.readDouble();
				tempVect.r = dat.readDouble();
				tempVect.var1 = dat.readInt();
				tempVect.var2 = dat.readInt();
				tempVect.var3 = dat.readInt();
				
				return tempVect;
			}
			
			public void writeToNBT(NBTTagCompound cmpnd,String tag){

				cmpnd.setDouble(tag+"x",this.x);
				cmpnd.setDouble(tag+"y",this.y);
				cmpnd.setDouble(tag+"z",this.z);
				cmpnd.setDouble(tag+"t",this.i);
				cmpnd.setDouble(tag+"h",this.j);
				cmpnd.setDouble(tag+"e",this.k);
				cmpnd.setDouble(tag+"r",this.r);
				cmpnd.setBoolean(tag+"bool",this.bool);
				cmpnd.setInteger(tag+"var1", var1);
				cmpnd.setInteger(tag+"var2", var2);
				cmpnd.setInteger(tag+"var3", var3);
				cmpnd.setInteger(tag+"var4", var4);
			}
			
			
			public String toString()
			{
				return "x: "+Double.toString(x)+
					" y: "+Double.toString(y)+
					" z: "+Double.toString(z)+
					" t: "+Double.toString(i)+
					" h: "+Double.toString(j)+
					" e: "+Double.toString(k)+
					" r: "+Double.toString(r)+
					" bool: "+Boolean.toString(bool);
			}
	 }
	  
}
