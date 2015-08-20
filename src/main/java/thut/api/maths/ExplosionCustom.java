package thut.api.maths;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import thut.api.ThutBlocks;
import thut.api.TickHandler;
import thut.api.TickHandler.BlockChange;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

public class ExplosionCustom extends Explosion{

	public static int MAX_RADIUS =31;
	World worldObj;
	Vector3 centre;
	float strength;
	public boolean meteor = false;
	
	EntityPlayer owner = null;
	List<Entity> targets = new ArrayList<Entity>();
	
	public ExplosionCustom(World par1World, Entity par2Entity, double par3,
			double par5, double par7, float par9) 
	{
		super(par1World, par2Entity, par3, par5, par7, par9);
		worldObj = par1World;
		strength = par9;
		centre = Vector3.getNewVectorFromPool().set(par3, par5, par7);
		
	}
	
	public ExplosionCustom(World par1World, Entity par2Entity, Vector3 center, float par9) 
	{
		super(par1World, par2Entity, center.x, center.y, center.z, par9);
		worldObj = par1World;
		strength = par9;
		this.centre = center.copy();
		if(!toClear[1])
		{
			toClear[1] = true;
//			boomThread.start();
		}
		
	}
	
	public boolean canBreak(Vector3 location)
	{
		boolean ret = true;
		
		if(owner!=null)
		{
			BreakEvent evt = new BreakEvent(location.intX(), location.intY(), location.intZ(), worldObj
					, location.getBlock(worldObj), location.getBlockMetadata(worldObj), owner);
			MinecraftForge.EVENT_BUS.post(evt);
		//	System.out.println(evt.isCanceled());
			if(evt.isCanceled())
				return false;
		}
		
		return ret;
	}
	
	public ExplosionCustom setMeteor(boolean meteor)
	{
		this.meteor = meteor;
		return this;
	}

	public void doExplosion()
	{
		doExplosionA();
		doExplosionB(false);
		affectedBlockPositions.clear();
	}

	public void doKineticImpactor(World worldObj, Vector3 velocity,
			Vector3 hitLocation, Vector3 acceleration, float density, float energy) {
		if (density < 0 || energy <= 0) {
			return;
		}
		affectedBlockPositions.clear();
		if(acceleration==null)
			acceleration = Vector3.empty;
		double eid = Math.random();
		int n = 0;
		List<Vector3> locations = new ArrayList<Vector3>();
		List<Float> blasts = new ArrayList<Float>();

		float resist = hitLocation.getExplosionResistance(worldObj);
		float blast = (float) Math.min((energy*(resist/density)), energy);

		if (resist > density) {
			hitLocation = hitLocation.subtract(velocity.normalize());
			destroyInRangeV4(hitLocation, worldObj,
					(int) (int) Math.max(blast, 10), blast, true, false);
			return;
		}
		Vector3 absorbedLoc = Vector3.getNewVectorFromPool();
		float remainingEnergy = 0;
		int id = hitLocation.getBlockId(worldObj);
		density -= resist;

		while (energy > 0) {
			locations.add(hitLocation.subtract(velocity.normalize()));
			blasts.add(blast);
			hitLocation = hitLocation.add(velocity.normalize());
			id = hitLocation.getBlockId(worldObj);
			velocity.add(acceleration);
			resist = (float) Math.max(
					hitLocation.getExplosionResistance(worldObj), 0);
			blast = (float) Math.min(energy*(resist/density), energy);
			if (resist > density) {
				absorbedLoc.set(hitLocation);
				remainingEnergy = energy;
				break;
			}
			energy -= energy*(resist/density);
			density -= (resist + 0.1);
		}
		
		n = locations.size();
		if (n != 0)

			for (int i = 0; i < n; i++) {
				Vector3 source = locations.get(i);
				float strength = Math.min(blasts.get(i), 256);
				if (worldObj.doChunksNearChunkExist(source.intX(),
						source.intY(), source.intZ(), (int) MAX_RADIUS)) {
					if (strength != 0)
					{//TODO move this over to a multithreaded thing
//						destroyInRangeV4(source, worldObj,
//								(int) Math.max(10, strength / 2), strength,
//								true,false);
						ExplosionCustom boo = new ExplosionCustom(worldObj, exploder, centre, strength);
						ExplosionStuff boom = new ExplosionStuff(boo, (int) Math.max(30, strength / 2), strength, worldObj, source);
						explosions.add(boom);
						
					}
				}
			}
		if (remainingEnergy > 10) {
			absorbedLoc = absorbedLoc.subtract(velocity.normalize());
			destroyInRangeV4(absorbedLoc, worldObj, 50,
					remainingEnergy, true, false);
		}
		absorbedLoc.freeVectorFromPool();
	}

	Map<EntityLivingBase, Float> damages = new HashMap<EntityLivingBase, Float>();
	List<Chunk> affected = new ArrayList<Chunk>();
	
	public void doExplosionA() {
		this.affectedBlockPositions.clear();
		if(worldObj.isRemote) return;
		worldObj.theProfiler.startSection("explosion");
		long startTime = System.nanoTime();
		final double scaleFactor = 1;
		HashMap<Integer, Integer> counts = new HashMap<Integer, Integer>();

		int linearFactor = Cruncher.size;
		int size = Cruncher.size;

		double x0 = centre.x;
		double y0 = centre.y;
		double z0 = centre.z;

		double rad;

		final int radius = Math.min(
				(int) Math.sqrt(strength * scaleFactor / 0.25),
				Cruncher.size);

		int index;

		double radSq = radius * radius, rMag;

		int n = 0;
		int l = 0;
		int g = 0;
		int f = 0, x, y, z;
		float resist;

		Vector3 r = Vector3.getNewVectorFromPool(),
				rAbs = Vector3.getNewVectorFromPool(),
				rHat = Vector3.getNewVectorFromPool(), 
				rTest = Vector3.getNewVectorFromPool(),
				rTestPrev = Vector3.getNewVectorFromPool(),
				rTestAbs = Vector3.getNewVectorFromPool();

		damages.clear();
		affected.clear();
		targets.clear();
		
		long estimatedTime;
		final Set<Integer> map = new HashSet<Integer>();
		final List<Vector3> removed = new ArrayList<Vector3>();

		final Map<Integer, Float> damps = new HashMap<Integer, Float>();
		final BitSet blocked = new BitSet();
		final List<Integer> toRemove = new ArrayList<Integer>();

		final Map<Integer, Float> resists = new HashMap<Integer, Float>();
		int id;
		int count = 0;
		
		for (int i : Cruncher.locs) {
			byte[] s = new byte[] { (byte) ((i & 255) - size),
					(byte) (((i / 256) & 255) - size),
					(byte) (((i / (256 * 256)) & 255) - size) };

			x = s[0];
			y = s[1];
			z = s[2];
			double rSq = x * x + y * y + z * z;
			if (rSq > radSq)
				continue;

			r.set(x, y, z);
			rAbs.set(r).addTo(centre);
			//TODO make these draw from a list of BlockChanges and be done on a seperate thread
			if (rAbs.isAir(worldObj) && !(x == 0 && y == 0 && z == 0))
				continue;
			if(rAbs.getExplosionResistance(worldObj)>strength * scaleFactor)// / r.magSq())
				continue;
		//	if(!PokecubeSpawner.checkNoSpawnerInArea(worldObj, rAbs.intX(),rAbs.intY(),rAbs.intZ()))
			if(!canBreak(rAbs))
			{
				continue;
			}
			rHat = r.normalize();

			index = Cruncher.getIndex(rHat, linearFactor);

			if (blocked.get(index)) {
				continue;
			}
			boolean stop = false;
			rTest.clear();
			rTestPrev.clear();
			rMag = r.mag();
			float dj = 1;
			resist = 0;

			for (float j = 0F; j <= rMag; j += dj) {
				rTest = rHat.scalarMult(j);

				if (!(rTest.sameBlock(rTestPrev))) {
					rTestAbs = rTest.add(centre);

					resist += rTestAbs.getExplosionResistance(worldObj);
					//if(!PokecubeSpawner.checkNoSpawnerInArea(worldObj, rTestAbs.intX(),rTestAbs.intY(),rTestAbs.intZ()))
					if(!canBreak(rTestAbs))
					{
						blocked.set(index);
						removed.add(r);
						stop = true;
						break;
					}
					if (resist > strength * scaleFactor / rTest.magSq()) {
						blocked.set(index);
						removed.add(r);
						stop = true;
						break;
					}
				}

				rTestPrev.set(rTest);
			}
			if (stop)
				continue;

			toRemove.add(i);
			id = rAbs.getBlockId(worldObj);
			if(counts.containsKey(id))
			{
				counts.put(id, counts.get(id)+1);
			}
			else
			{
				counts.put(id, 1);
				if(id!=0)
					count++;
			}
		}
		int num1 = 0;
		//System.out.println("igoboom");
		for (int j = 0; j < toRemove.size(); j++) {
			int i = toRemove.get(j);

			byte[] s = new byte[] { (byte) ((i & 255) - size),
					(byte) (((i / 256) & 255) - size),
					(byte) (((i / (256 * 256)) & 255) - size) };

			x = s[0];
			y = s[1];
			z = s[2];

			r.set(x, y, z);
			rAbs.set(r).addTo(centre);

			Chunk chunk = worldObj.getChunkFromBlockCoords(rAbs.intX(),
					rAbs.intZ());
			
			if (!affected.contains(chunk))
				affected.add(chunk);

			Block block = rAbs.getBlock(worldObj);


			if(meteor)
			{
				doMeteorStuff(block, rAbs);
			}
			else
			{
				TickHandler.addBlockChange(rAbs, worldObj, Blocks.air);
				num1++;
			}
            if (radius<32&&block!=null&&block.canDropFromExplosion(this)&&!meteor)
            {
            	float num = counts.containsKey(Block.getIdFromBlock(block))?counts.get(Block.getIdFromBlock(block)):0;
            	if(num > 64)
            	{
            		num = 64/num;
            	}
            	else
            	{
            		num = 1;
            	}
            	
            	float chance = 1f*num;
            	
                block.dropBlockAsItemWithChance(this.worldObj, rAbs.intX(), rAbs.intY(), rAbs.intZ(), rAbs.getBlockMetadata(worldObj), chance, 0);
            }
            
			if (block!=null)
            {
				block.onBlockDestroyedByExplosion(this.worldObj, rAbs.intX(), rAbs.intY(), rAbs.intZ(), this);
            }
			addChunkPosition(rAbs);
			worldObj.markBlockForUpdate(rAbs.intX(), rAbs.intY(),
					rAbs.intZ());
		}
		worldObj.theProfiler.endSection();
		removed.clear();
		r.freeVectorFromPool(); 
		rAbs.freeVectorFromPool(); 
		rHat.freeVectorFromPool(); 
		rTest.freeVectorFromPool(); 
		rTestPrev.freeVectorFromPool(); 
		rTestAbs.freeVectorFromPool();
	}
	
	public void doMeteorStuff(Block destroyed, Vector3 location)
	{
		if(!meteor)
			TickHandler.addBlockChange(location, worldObj, Blocks.air);
		
		
		float resistance = location.getExplosionResistance(worldObj);
		Block melt = ThutBlocks.lavas[0];
		Block dust = ThutBlocks.dust;
		
		if(melt==null)
			melt = Blocks.air;
		if(dust==null)
			dust = Blocks.air;
		
		if(resistance > 2 && !location.getBlockMaterial(worldObj).isLiquid())
		{
			int meta = (int) Math.min(resistance/2, 15);
			TickHandler.addBlockChange(location, worldObj, melt, meta);
		}
		else
		{
			TickHandler.addBlockChange(location, worldObj, dust);
		}
	}
	
	public void applyDamage(Entity e, float damage)
	{
			boolean damagePlayerTest = true;
			if(damagePlayerTest)
				e.attackEntityFrom(DamageSource.setExplosionSource(this), damage);
	}
	
	public void addChunkPosition(Vector3 v)
	{
		affectedBlockPositions.add(new ChunkPosition(v.intX(), v.intY(), v.intZ()));
	}
    /**
     * Does the second part of the explosion (sound, particles, drop spawn)
     */
    @Override
    public void doExplosionB(boolean par1)
    {
        this.worldObj.playSoundEffect(this.explosionX, this.explosionY, this.explosionZ, "random.explode", 4.0F, (1.0F + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F) * 0.7F);

        if (this.explosionSize >= 2.0F && this.isSmoking)
        {
            this.worldObj.spawnParticle("hugeexplosion", this.explosionX, this.explosionY, this.explosionZ, 1.0D, 0.0D, 0.0D);
        }
        else
        {
            this.worldObj.spawnParticle("largeexplode", this.explosionX, this.explosionY, this.explosionZ, 1.0D, 0.0D, 0.0D);
        }
		double radius = MAX_RADIUS;
		double scaleFactor = 1;
		List<Entity> targets = worldObj
		.getEntitiesWithinAABBExcludingEntity(exploder, centre
				.getAABB().expand(radius, radius, radius));
		List<Entity> toRemove = new ArrayList<Entity>();
		Vector3 v = Vector3.getNewVectorFromPool();
		for (Entity e : targets) {
			
			if (!centre.isVisible(worldObj,
					v.set(e)))
			{
				toRemove.add(e);
				continue;
			}
			if (e instanceof EntityLivingBase
					&& !damages.containsKey((EntityLivingBase) e)&&e!=exploder) {
				EntityLivingBase ent = (EntityLivingBase) e;
					float damage = (float) (strength * scaleFactor / (centre
							.distToSq(v.set(e))));
					damages.put(ent, damage);
		
			}
		}
		for(Entity e: toRemove)
			targets.remove(e);
		
        Vec3 vec3 = Vec3.createVectorHelper(this.explosionX, this.explosionY, this.explosionZ);

        for (int i1 = 0; i1 < targets.size(); ++i1)
        {
            Entity entity = (Entity)targets.get(i1);
            double d4 = entity.getDistance(this.explosionX, this.explosionY, this.explosionZ) / (double)this.explosionSize;

            if (d4 <= 1.0D)
            {
                double d5 = entity.posX - this.explosionX;
                double d6 = entity.posY + (double)entity.getEyeHeight() - this.explosionY;
                double d7 = entity.posZ - this.explosionZ;
                double d9 = (double)MathHelper.sqrt_double(d5 * d5 + d6 * d6 + d7 * d7);

                if (d9 != 0.0D)
                {
                    d5 /= d9;
                    d6 /= d9;
                    d7 /= d9;
                    double d10 = (double)this.worldObj.getBlockDensity(vec3, entity.boundingBox);
                    double d11 = (1.0D - d4) * d10;
                    entity.attackEntityFrom(DamageSource.setExplosionSource(this), (float)((int)((d11 * d11 + d11) / 2.0D * 8.0D * (double)this.explosionSize + 1.0D)));
                    double d8 = EnchantmentProtection.func_92092_a(entity, d11);
                    entity.motionX += d5 * d8;
                    entity.motionY += d6 * d8;
                    entity.motionZ += d7 * d8;

                    if (entity instanceof EntityPlayer)
                    {
                       	Vec3 vec = Vec3.createVectorHelper(d5 * d11, d6 * d11, d7 * d11);
                        EntityPlayer entityplayer = (EntityPlayer) entity;
                        
                        if (centre.distanceTo(v.set(entity)) < 4096.0D)
                        {
                            ((EntityPlayerMP)entityplayer).playerNetServerHandler.sendPacket(new S27PacketExplosion(centre.x, centre.y, centre.z, strength, affectedBlockPositions, vec));
                        }
                        
                    }
                }
            }
        }

		for (Chunk c : affected) {
			c.setChunkModified();
		}
		if(!worldObj.isRemote)
		for (EntityLivingBase e : damages.keySet()) {
			if(damages.get(e)>0.1)
				applyDamage(e, damages.get(e));;
		}
		v.freeVectorFromPool();
    }
    
	public void destroyInRangeV4(Vector3 centre, World worldObj, int radi,double strength,
			boolean dust, boolean drop) {
		worldObj.theProfiler.startSection("explosion");
		long startTime = System.nanoTime();
		//System.out.println("Starting Explosion Calculations");
		final double scaleFactor = 250;
		
		int linearFactor = Cruncher.size;

		double rad;
		worldObj.theProfiler.startSection("explosion");

		final int radius = Math.min(
				(int) Math.sqrt(strength * scaleFactor / 0.25),
				Math.min(radi, Cruncher.size));

		int index;

		double radSq = radius * radius, rMag;

		int n = 0;
		int l = 0;
		int g = 0;
		int f = 0, x, y, z;
		float resist;
		Vector3 r = Vector3.getNewVectorFromPool(),
				rAbs = Vector3.getNewVectorFromPool(),
				rHat = Vector3.getNewVectorFromPool(), 
				rTest = Vector3.getNewVectorFromPool(),
				rTestPrev = Vector3.getNewVectorFromPool(),
				rTestAbs = Vector3.getNewVectorFromPool();

		damages.clear();
		affected.clear();
		targets.clear();
		
		long estimatedTime;
		final Set<Integer> map = new HashSet<Integer>();
		final List<Vector3> removed = new ArrayList<Vector3>();

		final Map<Integer, Float> damps = new HashMap<Integer, Float>();
		final BitSet blocked = new BitSet();
		final List<Integer> toRemove = new ArrayList<Integer>();

		final Map<Integer, Float> resists = new HashMap<Integer, Float>();

		for (int i : Cruncher.locs) {
			byte[] s = new byte[] { (byte) ((i & 255) - Cruncher.size),
					(byte) (((i / 256) & 255) - Cruncher.size),
					(byte) (((i / (256 * 256)) & 255) - Cruncher.size) };

			x = s[0];
			y = s[1];
			z = s[2];
			double rSq = x * x + y * y + z * z;
			if (rSq > radSq)
				continue;

			r.set(x, y, z);
			rAbs.set(r).addTo(centre);

			if (rAbs.isAir(worldObj) && !(x == 0 && y == 0 && z == 0))
				continue;
			if(rAbs.getExplosionResistance(worldObj)>strength * scaleFactor / r.magSq())
				continue;
		//	if(!PokecubeSpawner.checkNoSpawnerInArea(worldObj, rAbs.intX(),rAbs.intY(),rAbs.intZ()))
			if(!canBreak(rAbs))
			{
				continue;
			}
			rHat = r.normalize();

			index = Cruncher.getIndex(rHat, linearFactor);

			if (blocked.get(index)) {
				continue;
			}
			boolean stop = false;
			rMag = r.mag();
			float dj = 1;
			resist = 0;

			for (float j = 0F; j <= rMag; j += dj) {
				rTest = rHat.scalarMult(j);

				if (!(rTest.sameBlock(rTestPrev))) {
					rTestAbs = rTest.add(centre);

					resist += rTestAbs.getExplosionResistance(worldObj);

			//		if(!PokecubeSpawner.checkNoSpawnerInArea(worldObj, rTestAbs.intX(),rTestAbs.intY(),rTestAbs.intZ()))

					if(!canBreak(rTestAbs))
					{
						blocked.set(index);
						removed.add(r);
						stop = true;
						break;
					}
					if (resist > strength * scaleFactor / r.magSq()) {
						blocked.set(index);
						removed.add(r);
						stop = true;
						break;
					}
				}

				rTestPrev.set(rTest);
			}
			if (stop)
				continue;

			toRemove.add(i);
		}
		
		estimatedTime = System.nanoTime() - startTime;
		
		//System.out.println("Calculations took "+((double)(estimatedTime/1000000000D))+"s");

		int count = 0;

		for (int j = 0; j < toRemove.size(); j++) {
			int i = toRemove.get(j);

			byte[] s = new byte[] { (byte) ((i & 255) - Cruncher.size),
					(byte) (((i / 256) & 255) - Cruncher.size),
					(byte) (((i / (256 * 256)) & 255) - Cruncher.size) };

			x = s[0];
			y = s[1];
			z = s[2];

			r.set(x, y, z);
			rAbs.set(r).addTo(centre);

			Chunk chunk = worldObj.getChunkFromBlockCoords(rAbs.intX(),
					rAbs.intZ());
			if (!affected.contains(chunk))
				affected.add(chunk);
			Block block = rAbs.getBlock(worldObj);

            if (drop&&(radius<32&&block!=null&&block.canDropFromExplosion(null)))
            {
                block.dropBlockAsItemWithChance(worldObj, rAbs.intX(), rAbs.intY(), rAbs.intZ(), rAbs.getBlockMetadata(worldObj), 1.0f, 0);
            }
			if (block!=null)
            {
				block.onBlockDestroyedByExplosion(worldObj, rAbs.intX(), rAbs.intY(), rAbs.intZ(), null);
            }

            
			if(meteor)
			{
				doMeteorStuff(block, rAbs);
			}
			else
			{
				//rAbs.setBlockId(worldObj, 0, 0, 3);
				TickHandler.addBlockChange(rAbs, worldObj, Blocks.air);
			}
			addChunkPosition(rAbs);
			worldObj.markBlockForUpdate(rAbs.intX(), rAbs.intY(),
					rAbs.intZ());
		}

//		System.out.println("Removal Took "+((System.nanoTime() - startTime - estimatedTime)/1000000000)+"s");
		//TODO
		//doExplosionB(false);

		for (Chunk c : affected) {
			c.setChunkModified();
		}
		worldObj.theProfiler.endSection();
		removed.clear();
		r.freeVectorFromPool(); 
		rAbs.freeVectorFromPool(); 
		rHat.freeVectorFromPool(); 
		rTest.freeVectorFromPool(); 
		rTestPrev.freeVectorFromPool(); 
		rTestAbs.freeVectorFromPool();
	}
	
	List<Integer> getRemovedBlocks(final double radius, final double strength, final World worldObj, final Vector3 centre)
	{
		int index;

		final double scaleFactor = 150;

		int linearFactor = Cruncher.size;
		
		double radSq = radius * radius, rMag;

		int n = 0;
		int l = 0;
		int g = 0;
		int f = 0, x, y, z;
		float resist;

		Vector3 r = Vector3.getNewVectorFromPool(),
				rAbs = Vector3.getNewVectorFromPool(),
				rHat = Vector3.getNewVectorFromPool(), 
				rTest = Vector3.getNewVectorFromPool(),
				rTestPrev = Vector3.getNewVectorFromPool(),
				rTestAbs = Vector3.getNewVectorFromPool();
		
		List<Integer> toRemove = new ArrayList<Integer>();
		final Set<Integer> map = new HashSet<Integer>();
		final List<Vector3> removed = new ArrayList<Vector3>();

		final Map<Integer, Float> damps = new HashMap<Integer, Float>();
		final BitSet blocked = new BitSet();

		final Map<Integer, Float> resists = new HashMap<Integer, Float>();

		for (int i : Cruncher.locs) {
			byte[] s = new byte[] { (byte) ((i & 255) - Cruncher.size),
					(byte) (((i / 256) & 255) - Cruncher.size),
					(byte) (((i / (256 * 256)) & 255) - Cruncher.size) };

			x = s[0];
			y = s[1];
			z = s[2];
			double rSq = x * x + y * y + z * z;
			if (rSq > radSq)
				continue;

			r.set(x, y, z);
			rAbs = r.add(centre);

			if (rAbs.isAir(worldObj) && !(x == 0 && y == 0 && z == 0))
				continue;
			if(getExplosionResistance(rAbs, worldObj)>strength * scaleFactor / r.magSq())
				continue;
		//	if(!PokecubeSpawner.checkNoSpawnerInArea(worldObj, rAbs.intX(),rAbs.intY(),rAbs.intZ()))
			if(!canBreak(rAbs))
			{
				continue;
			}
			rHat = r.normalize();

			index = Cruncher.getIndex(rHat, linearFactor);

			if (blocked.get(index)) {
				continue;
			}
			boolean stop = false;
			rMag = r.mag();
			float dj = 1;
			resist = 0;

			for (float j = 0F; j <= rMag; j += dj) {
				rTest = rHat.scalarMult(j);

				if (!(rTest.sameBlock(rTestPrev))) {
					rTestAbs = rTest.add(centre);

					resist += getExplosionResistance(rTestAbs, worldObj);//rTestAbs.getExplosionResistance(worldObj);

			//		if(!PokecubeSpawner.checkNoSpawnerInArea(worldObj, rTestAbs.intX(),rTestAbs.intY(),rTestAbs.intZ()))

					if(!canBreak(rTestAbs))
					{
						blocked.set(index);
						removed.add(r);
						stop = true;
						break;
					}
					if (resist > strength * scaleFactor / r.magSq()) {
						blocked.set(index);
						removed.add(r);
						stop = true;
						break;
					}
				}

				rTestPrev.set(rTest);
			}
			if (stop)
				continue;

			toRemove.add(i);
		}
		
		int count = 0;

		for (int j = 0; j < toRemove.size(); j++) {
			int i = toRemove.get(j);

			byte[] s = new byte[] { (byte) ((i & 255) - Cruncher.size),
					(byte) (((i / 256) & 255) - Cruncher.size),
					(byte) (((i / (256 * 256)) & 255) - Cruncher.size) };

			x = s[0];
			y = s[1];
			z = s[2];

			r.set(x, y, z);
			rAbs.set(r).addTo(centre);

			Chunk chunk = worldObj.getChunkFromBlockCoords(rAbs.intX(),
					rAbs.intZ());
			if (!affected.contains(chunk))
				affected.add(chunk);
			Block block = getBlock(rAbs, worldObj);
			doMeteorStuff(block, rAbs);
		}
		r.freeVectorFromPool(); 
		rAbs.freeVectorFromPool(); 
		rHat.freeVectorFromPool(); 
		rTest.freeVectorFromPool(); 
		rTestPrev.freeVectorFromPool(); 
		rTestAbs.freeVectorFromPool();
		return toRemove;
	}
	
	public static class ExplosionStuff
	{
		final ExplosionCustom boom;
		final double radius; 
		final double strength; 
		final World worldObj; 
		final Vector3 centre;
		
		public ExplosionStuff(ExplosionCustom boom, double radius, double strength, World worldObj, Vector3 centre)
		{
			this.boom = boom;
			this.radius = radius;
			this.strength = strength;
			this.worldObj = worldObj;
			this.centre = centre;
		}
		
		public void doBoom()
		{
			boom.getRemovedBlocks(radius, strength, worldObj, centre);
		}
	}
	static final boolean[] toClear = {false, false};
	
	static final ExplosionCustom instance = new ExplosionCustom(null, null, Vector3.getNewVectorFromPool(), 0);
	public static final Vector<ExplosionStuff> explosions = new Vector<ExplosionStuff>();
	
	private static final Thread boomThread = new Thread(new Runnable() {
		
		@Override
		public void run() {
			while (true)
			{
				boolean boomed = false;
				if(explosions.size()>0)
				{
					ExplosionStuff boom = explosions.get(0);
					long start = System.nanoTime();
					boom.doBoom();
					double dt = ((System.nanoTime() - start)/1000000d);
					System.out.println("boom "+dt);//TODO remove
					explosions.remove(0);
					boomed = true;
				}
				if(toClear[0])
				{
					explosions.clear();
					toClear[0] = false;
				}
				if(!boomed)
				{
					try {
						boomThread.sleep(100);
					} catch (InterruptedException e) {
						//e.printStackTrace();
					}
				}
			}
		}
	});
	static
	{
		boomThread.setName("explosionThread");
		System.out.println("Starting explosion thread");
		boomThread.start();
	}
	
	public static void clearInstance()
	{
		instance.blocks.clear();
		toClear[0] = true;
	}
	
	private static ExplosionCustom getInstance()
	{
		return instance;
	}
	
	static HashMap<Integer,Vector<BlockChange>> blocks = new HashMap();
	
	static Block getBlock(Vector3 location, World worldObj)
	{
		BlockChange b1 = new BlockChange(location, worldObj, null);
		for(BlockChange b:getInstance().getListForWorld(worldObj))
		{
			if(b.equals(b1))
				return b.blockTo;
		}
		return location.getBlock(worldObj);
	}
	
	static float getExplosionResistance(Vector3 location, World worldObj)
	{
		BlockChange b1 = new BlockChange(location, worldObj, null);
		for(BlockChange b:getInstance().getListForWorld(worldObj))
		{
			if(b.equals(b1))
				return b.blockTo.getExplosionResistance(null, worldObj, (int) b.location.x, (int) b.location.y, (int) b.location.z, 0d, 0d, 0d);
		}
		return location.getExplosionResistance(worldObj);
	}
	
    public static void addBlockChange(Vector3 location, World worldObj, Block blockTo, int meta)
    {
    	addBlockChange(new BlockChange(location, worldObj, blockTo, meta), worldObj);
    }

	public static void addBlockChange(BlockChange b1, World worldObj) {
		
		if(b1.location.y>255)
			return;
		
		for(BlockChange b:getInstance().getListForWorld(worldObj))
		{
			if(b.equals(b1))
				return;
		}
		toClear[0] = false;
		getInstance().getListForWorld(worldObj).add(b1);
	}

	public static Vector<BlockChange> getListForWorld(World worldObj)
	{
		Vector<BlockChange> ret = getInstance().blocks.get(worldObj.provider.dimensionId);
		if(ret==null)
		{
			ret = new Vector<BlockChange>();
			getInstance().blocks.put(worldObj.provider.dimensionId,ret);
		}
		return ret;
	}
}
