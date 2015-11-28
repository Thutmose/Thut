package thut.api.maths;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import thut.api.TickHandler;
import thut.api.TickHandler.BlockChange;
import thut.api.WorldCache;
import thut.api.network.PacketHandler;
import thut.api.network.PacketHandler.MessageClient;

public class ExplosionCustom extends Explosion
{

	public static int	MAX_RADIUS	= 31;
	public static Block	melt;
	public static Block	dust;
	IBlockAccess		worldObj;
	int					dimension;
	private World		world;
	Vector3				centre;
	float				strength;
	public boolean		meteor		= false;

	public EntityPlayer	owner	= null;
	List<Entity>		targets	= new ArrayList<Entity>();
	private double explosionX;
	private double explosionY;
	private double explosionZ;
	private float explosionSize;
	private boolean isSmoking = false;
	Entity exploder;
	public Set affectedBlockPositions = new HashSet();

	public ExplosionCustom(World world, Entity par2Entity, double x, double y, double z, float power)
	{
		super(world, par2Entity, x, y, z, power, false, false);
		exploder = par2Entity;
        this.explosionX = x;
        this.explosionY = y;
        this.explosionZ = z;
        explosionSize = power;
		this.world = world;
		dimension = world.provider.getDimensionId();
		strength = power;
		centre = Vector3.getNewVectorFromPool().set(x, y, z);

		worldObj = TickHandler.getInstance().getWorldCache(dimension);

	}

	public ExplosionCustom(World world, Entity par2Entity, Vector3 center, float power)
	{
		super(world, par2Entity, center.x, center.y, center.z, power, false, false);
		this.world = world;
		exploder = par2Entity;
		strength = power;
        this.explosionX = center.x;
        this.explosionY = center.y;
        this.explosionZ = center.z;
        explosionSize = power;
		this.centre = center.copy();
		if (!toClear[1])
		{
			toClear[1] = true;
		}
		if (world == null) return;

		dimension = world.provider.getDimensionId();
		worldObj = TickHandler.getInstance().getWorldCache(dimension);
	}

	public boolean canBreak(Vector3 location)
	{
		boolean ret = true;

		if (owner != null)
		{
			BreakEvent evt = new BreakEvent(world, location.getPos(), location.getBlockState(world), owner);
			MinecraftForge.EVENT_BUS.post(evt);
			if (evt.isCanceled()) return false;
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
		this.world.playSoundEffect(this.explosionX, this.explosionY, this.explosionZ, "random.explode", 4.0F,
				(1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);

		if (this.explosionSize >= 2.0F && this.isSmoking)
		{
			this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.explosionX, this.explosionY, this.explosionZ, 1.0D, 0.0D,
					0.0D);
		}
		else
		{
			this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.explosionX, this.explosionY, this.explosionZ, 1.0D, 0.0D,
					0.0D);
		}

		explosions.addElement(new ExplosionStuff(this, MAX_RADIUS, strength, worldObj, centre));
	}

	// TODO Revisit this to make blast energy more conserved
	// @Deprecated
	public void doKineticImpactor(World worldObj, Vector3 velocity, Vector3 hitLocation, Vector3 acceleration,
			float density, float energy)
	{
		if (density < 0 || energy <= 0) { return; }
		int max = 63;
		affectedBlockPositions.clear();
		if (acceleration == null) acceleration = Vector3.empty;
		float factor = 1;
		int n = 0;
		List<Vector3> locations = new ArrayList<Vector3>();
		List<Float> blasts = new ArrayList<Float>();

		float resist = hitLocation.getExplosionResistance(this, worldObj);
		float blast = Math.min((energy * (resist / density)), energy);

		if (resist > density)
		{
			hitLocation = hitLocation.subtract(velocity.normalize());
			ExplosionCustom boo = new ExplosionCustom(worldObj, exploder, hitLocation, blast * factor);
			// ExplosionStuff boom = new ExplosionStuff(boo, max, blast *
			// factor, worldObj, hitLocation);
			boo.doExplosion();
			// explosions.add(boom);
			return;
		}
		Vector3 absorbedLoc = Vector3.getNewVectorFromPool();
		float remainingEnergy = 0;
		int id = hitLocation.getBlockId(worldObj);
		density -= resist;

		while (energy > 0 && density > 0)
		{
			locations.add(hitLocation.subtract(velocity.normalize()));
			blasts.add(blast);
			hitLocation = hitLocation.add(velocity.normalize());
			id = hitLocation.getBlockId(worldObj);
			velocity.add(acceleration);
			resist = Math.max(hitLocation.getExplosionResistance(this, worldObj), 0);
			blast = Math.min(energy * (resist / density), energy);
			if (resist > density)
			{
				absorbedLoc.set(hitLocation);
				remainingEnergy = energy;
				break;
			}
			energy -= energy * (resist / density);
			density -= (resist + 0.1);
		}

		n = locations.size();
		if (n != 0)

		for (int i = 0; i < n; i++)
		{
			Vector3 source = locations.get(i);
			float strength = Math.min(blasts.get(i), 256);
			if (worldObj.isAreaLoaded(source.getPos(), max))
			{
				if (strength != 0)
				{
					int rad = (int) Math.max(max, Math.min(max, strength * 2));
					ExplosionCustom boo = new ExplosionCustom(worldObj, exploder, source, strength * factor);
					// ExplosionStuff boom = new ExplosionStuff(boo, max,
					// strength * factor, worldObj, source);
					// System.out.println("radius: "+rad);
					boo.doExplosion();
					// explosions.add(boom);

				}
			}
		}
		if (remainingEnergy > 10)
		{
			absorbedLoc = absorbedLoc.subtract(velocity.normalize());
			ExplosionCustom boo = new ExplosionCustom(worldObj, exploder, absorbedLoc, remainingEnergy * factor);
			// ExplosionStuff boom = new ExplosionStuff(boo, max,
			// remainingEnergy * factor, worldObj, absorbedLoc);
			System.out.println("radius: " + max + " " + absorbedLoc);
			boo.doExplosion();
			// explosions.add(boom);
		}
		absorbedLoc.freeVectorFromPool();
	}

	Map<EntityLivingBase, Float>	damages		= new HashMap<EntityLivingBase, Float>();
	List<Chunk>						affected	= new ArrayList<Chunk>();

	@Override
	public void doExplosionA()
	{
		this.affectedBlockPositions.clear();

		if (true)
		{
			System.err.println("This should not be run anymore");
			new Exception().printStackTrace();
			return;
		}
	}

	/** Handles the actual block removal, has a meteor argument to allow
	 * converting to ash or dust on impact
	 * 
	 * @param destroyed
	 * @param location */
	public void doMeteorStuff(Block destroyed, Vector3 location)
	{
		BlockChange change;
		if (!meteor)
		{
			change = new BlockChange(location, dimension, Blocks.air);
			change.flag = 2;
			TickHandler.addBlockChange(change, dimension);
			return;
		}
		float resistance = location.getExplosionResistance(this, worldObj);

		if (melt == null) melt = Blocks.air;
		if (dust == null) dust = Blocks.air;

		if (resistance > 2 && !location.getBlockMaterial(worldObj).isLiquid())
		{
			int meta = (int) Math.min(resistance / 2, 15);
			change = new BlockChange(location, dimension, melt, meta);
			change.flag = 2;
			TickHandler.addBlockChange(change, dimension);
		}
		else
		{
			change = new BlockChange(location, dimension, dust);
			change.flag = 2;
			TickHandler.addBlockChange(change, dimension);
		}
	}

	public void addChunkPosition(Vector3 v)
	{
		affectedBlockPositions.add(new BlockPos(v.intX(), v.intY(), v.intZ()));
	}

	/** Does the second part of the explosion (sound, particles, drop spawn) */
	@Override
	public void doExplosionB(boolean par1)
	{
		// if(world.isRemote)
		// {
		// new Exception().printStackTrace();
		// return;
		// }
		// else
		// if(true)
		// {
		// return;
		// }
		//
		// double radius = MAX_RADIUS;
		// double scaleFactor = 10;
		// List<Entity> targets = world
		// .getEntitiesWithinAABBExcludingEntity(exploder, centre
		// .getAABB().expand(radius, radius, radius));
		// List<Entity> toRemove = new ArrayList<Entity>();
		// Vector3 v = Vector3.getNewVectorFromPool();
		// for (Entity e : targets) {
		//
		// if (!centre.isVisible(worldObj,
		// v.set(e)))
		// {
		// toRemove.add(e);
		// continue;
		// }
		// if (e instanceof EntityLivingBase
		// && !damages.containsKey(e)&&e!=exploder) {
		// EntityLivingBase ent = (EntityLivingBase) e;
		// float damage = (float) (strength * scaleFactor / (centre
		// .distToSq(v.set(e))));
		// damages.put(ent, damage);
		//
		// }
		// }
		// for(Entity e: toRemove)
		// targets.remove(e);
		//
		// Vec3 vec3 = Vec3.createVectorHelper(this.explosionX, this.explosionY,
		// this.explosionZ);
		//
		// for (int i1 = 0; i1 < targets.size(); ++i1)
		// {
		// Entity entity = targets.get(i1);
		// double d4 = entity.getDistance(this.explosionX, this.explosionY,
		// this.explosionZ) / this.explosionSize;
		//
		// if (d4 <= 1.0D)
		// {
		// double d5 = entity.posX - this.explosionX;
		// double d6 = entity.posY + entity.getEyeHeight() - this.explosionY;
		// double d7 = entity.posZ - this.explosionZ;
		// double d9 = MathHelper.sqrt_double(d5 * d5 + d6 * d6 + d7 * d7);
		//
		// if (d9 != 0.0D)
		// {
		// d5 /= d9;
		// d6 /= d9;
		// d7 /= d9;
		// double d10 = this.world.getBlockDensity(vec3, entity.boundingBox);
		// double d11 = (1.0D - d4) * d10;
		//// entity.attackEntityFrom(DamageSource.setExplosionSource(this),
		// (float)((int)((d11 * d11 + d11) / 2.0D * 8.0D *
		// (double)this.explosionSize + 1.0D)));
		//// double d8 = EnchantmentProtection.func_92092_a(entity, d11);
		//// entity.motionX += d5 * d8;//TODO put knockback in the victim stuff
		//// entity.motionY += d6 * d8;
		//// entity.motionZ += d7 * d8;
		// if (entity instanceof EntityPlayer)
		// {
		// Vec3 vec = Vec3.createVectorHelper(d5 * d11, d6 * d11, d7 * d11);
		// EntityPlayer entityplayer = (EntityPlayer) entity;
		//
		// if (centre.distanceTo(v.set(entity)) < 4096.0D)
		// {
		// ((EntityPlayerMP)entityplayer).playerNetServerHandler.sendPacket(new
		// S27PacketExplosion(centre.x, centre.y, centre.z, strength,
		// affectedBlockPositions, vec));
		// }
		//
		// }
		// }
		// }
		// }
		//
		//// for (Chunk c : affected) {
		//// c.setChunkModified();//TODO modified chunks
		//// }
		//// if(!world.isRemote)
		// for (EntityLivingBase e : damages.keySet()) {
		// if(damages.get(e)>0.1)
		// {
		// ExplosionVictimTicker.victims.addElement(new VictimStuff(e,
		// damages.get(e), this));
		// }
		// }
		// v.freeVectorFromPool();
	}

	List<Long> getRemovedBlocks(final double radius, final double strength, final IBlockAccess worldObj,
			final Vector3 centre)
	{
		List<Long> toRemove = new ArrayList<Long>();
		final ExplosionCustom boom = this;

		int count = 0;
//		System.out.println("Starting Explosion Algorithm");
		Long time = System.nanoTime();
		Vector3 r = Vector3.getNewVectorFromPool(), rAbs = Vector3.getNewVectorFromPool(),
				rHat = Vector3.getNewVectorFromPool(), rTest = Vector3.getNewVectorFromPool(),
				rTestPrev = Vector3.getNewVectorFromPool(), rTestAbs = Vector3.getNewVectorFromPool();
		int index;
		int index2;

		final double scaleFactor = 1500;

		double radSq = radius * radius, rMag;

		int n = 0;
		int l = 0;
		int g = 0;
		int f = 0, x, y, z;
		float resist;

		Float value;
		int prevX = 0, prevY = 0, prevZ = 0;
		int currentRadius = 0, subIndex = 0;
		int nextRadius = 1;

		int currentRadSq = 0;
		int nextRadCb = 1;

		int radCbDiff = 1;
		int radSqDiff = 1;
		int[] toFill = new int[3];

		double str;

		// TODO make this do a compounded resist instead, to lower ram use
		HashMap<Integer, Float> resists = new HashMap<Integer, Float>();
		BitSet blocked = new BitSet();
		// HashMap blocked = new HashMap();

		double absorbed = 0;

		int num = (int) (Math.sqrt(strength * scaleFactor / 0.5));
		int max = (int) MAX_RADIUS * 2;
		int blockedNum = 0;
		int blockedNum2 = 0;
		num = Math.min(num, max);
		num = Math.min(num, 1000);
		radSq = num * num / 4;

		Map<Integer, List<Entity>> victims = getEntitiesInRange(num / 2);
		List<Integer> affectedThisRadius = new ArrayList();
		for (int i = 0; i < num * num * num; i++)
		{

			if (i >= nextRadCb)
			{
				nextRadius++;
				currentRadius++;
				int temp = (2 * nextRadius - 1);
				nextRadCb = temp * temp * temp;
				temp = (2 * currentRadius - 1);
				currentRadSq = temp * temp * temp;
				radCbDiff = nextRadCb - currentRadSq;
				radSqDiff = (2 * nextRadius - 1) * (2 * nextRadius - 1) - temp * temp;
				blockedNum = 0;
				if(!affectedThisRadius.isEmpty())
				{
					ClientUpdateInfo info = new ClientUpdateInfo(affectedThisRadius, centre, dimension);
					ExplosionVictimTicker.clientUpdates.addElement(info);
					affectedThisRadius.clear();
				}
			}
			subIndex = (i - currentRadSq);

			Cruncher.indexToVals(currentRadius, subIndex, radSqDiff, radCbDiff, toFill);
			x = toFill[0];
			z = toFill[1];
			y = toFill[2];

			if (y + centre.y < 0 || y + centre.y > 255) continue;//TODO replace 255 with some way to get height

			double rSq = x * x + y * y + z * z;
			if (rSq > radSq) continue;

			r.set(x, y, z);
			rMag = Math.sqrt(rSq);
			rAbs.set(r).addTo(centre);
			rHat.set(r.normalize());
			index = Cruncher.getVectorInt(rHat.scalarMultBy(num / 2));
			rHat.scalarMultBy(1 / ((double) (num / 2)));
			if (blocked.get(index))
			{
				blockedNum++;
				blockedNum2++;
				continue;
			}
			if (i != 0 && blockedNum >= radCbDiff - 1)
			{
				System.out.println("completely blocked");
				break;
			}
			str = strength * scaleFactor / rSq;
			index2 = Cruncher.getVectorInt(r);
			if (rAbs.isAir(worldObj) && !(x == 0 && y == 0 && z == 0))
			{
				if (victims.containsKey(index2))
				{
					for (Entity e : victims.get(index2))
					{
						ExplosionVictimTicker.addVictim(e, (float) str, boom);
					}
				}
				if(rMag < 5)
				{
					affectedThisRadius.add(index2);//TODO decide if I want to do this in air
				}
				continue;
			}

			if (str <= 0.1)
			{
				System.out.println("Terminating at distance " + rMag);
				break;
			}
			if (!canBreak(rAbs))
			{
				// System.out.println("not allowed");
				blocked.set(index);
				continue;
			}
			float res;

			if (resists.containsKey(index2))
			{
				res = resists.get(index2);
			}
			else
			{
				res = getExplosionResistance(rAbs, boom, worldObj);
				if (res > 1) res = res * res;
				resists.put(index2, res);
			}
			if (res > str)
			{
				// Cruncher.setBlocked(blocked, rHat, rMag, num);
				blocked.set(index);
				absorbed += str;
				blockedNum++;
				continue;
			}
			boolean stop = false;
			rMag = r.mag();
			float dj = 1;
			resist = 0;

			for (float j = 0F; j <= rMag; j += dj)
			{
				rTest.set(rHat).scalarMultBy(j);

				if (!(rTest.sameBlock(rTestPrev)))
				{
					rTestAbs.set(rTest).addTo(centre);

					index2 = Cruncher.getVectorInt(rTest);

					if (resists.containsKey(index2))
					{
						res = resists.get(index2);
					}
					else
					{
						res = getExplosionResistance(rTestAbs, boom, worldObj);
						if (res > 1) res = res * res;
						resists.put(index2, res);
					}

					resist += res;

					if (!canBreak(rTestAbs))
					{
						stop = true;
						blocked.set(index);
						blockedNum++;
						break;
					}
					double d1 = rTest.magSq();
					double d = d1;
					str = strength * scaleFactor / d;// was using r.magSq()
					if (resist > str)
					{
						stop = true;
						blocked.set(index);
						blockedNum++;
						break;
					}
				}

				rTestPrev.set(rTest);
			}
			if (stop) continue;

			rAbs.set(r).addTo(centre);
			Chunk chunk = ((WorldCache) worldObj).getChunk(rAbs.intX() >> 4, rAbs.intZ() >> 4);

			if (chunk == null)
			{
				new Exception().printStackTrace();
			}
			if (!affected.contains(chunk)) affected.add(chunk);
			Block block = getBlock(rAbs, worldObj, boom.dimension);
			addChunkPosition(rAbs);
			count++;
			doMeteorStuff(block, rAbs);

			str = strength * scaleFactor / rSq;
			index2 = Cruncher.getVectorInt(r);
			if (victims.containsKey(index2))
			{
				for (Entity e : victims.get(index2))
				{
					ExplosionVictimTicker.addVictim(e, (float) str, boom);
				}
			}
			affectedThisRadius.add(index2);
		}
		double dt = (System.nanoTime() - time) / 1000000000d;

		float ratioBlocks = (blockedNum) / (float) (num * num * num);

//		System.out.println("Time elapsed: " + dt + "s value absorbed: " + absorbed + " Ratio Blocked:" + ratioBlocks);
//		System.out.println(" Removed:" + count + " Blocked:" + blocked.size() + " Radius:" + num / 2);// TODO
																										// remove
			
		doExplosionB(false);

		ExplosionEvent evt = new ExplosionEvent.Detonate(boom.world, boom, new ArrayList());
		MinecraftForge.EVENT_BUS.post(evt);

		r.freeVectorFromPool();
		rAbs.freeVectorFromPool();
		rHat.freeVectorFromPool();
		rTest.freeVectorFromPool();
		rTestPrev.freeVectorFromPool();
		rTestAbs.freeVectorFromPool();
		// }
		return toRemove;
	}

	List getEntitiesWithinDistance(Vector3 centre, Class targetClass, int distance, int dimension)
	{
		Vector entities = ExplosionCustom.worldEntities.get(dimension);
		List list = new ArrayList();
		double dsq = distance * distance;
		Vector3 point = Vector3.getNewVectorFromPool();
		if (entities != null)
		{
			List temp = new ArrayList(entities);
			for (Object o : temp)
			{
				if (targetClass.isInstance(o))
				{
					point.set(o);
					if (point.distToSq(centre) < dsq)
					{
						list.add(o);
					}
				}
			}
		}
		point.freeVectorFromPool();
		return list;
	}

	HashMap<Integer, List<Entity>> getEntitiesInRange(int distance)
	{
		HashMap<Integer, List<Entity>> ret = new HashMap();
		List ents = getEntitiesWithinDistance(centre, Entity.class, distance, dimension);
		for (Object o : ents)
		{
			Entity e = (Entity) o;

			if (centre.distToEntity(e) > distance) continue;

			int x = MathHelper.floor_double(e.posX - centre.x);
			int y = MathHelper.floor_double(e.posY - centre.y);
			int z = MathHelper.floor_double(e.posZ - centre.z);
			int key = Cruncher.getVectorInt(x, y, z);
			List temp;
			if (ret.containsKey(key))
			{
				temp = ret.get(key);
			}
			else
			{
				temp = new ArrayList();
				ret.put(key, temp);
			}
			temp.add(e);
		}

		return ret;
	}

	public static class ExplosionStuff
	{
		final ExplosionCustom	boom;
		final double			radius;
		final double			strength;
		final IBlockAccess		worldObj;
		final Vector3			centre;
		final boolean[]			lock	= { true, false };

		public ExplosionStuff(ExplosionCustom boom, double radius, double strength, IBlockAccess worldObj,
				Vector3 centre)
		{
			this.boom = boom;
			this.radius = radius;
			this.strength = strength;
			this.worldObj = worldObj;
			this.centre = centre;
		}

		public void doBoom()
		{
			if (lock[1]) return;
			lock[1] = true;
			Thread newBoom = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					boom.getRemovedBlocks(radius, strength, worldObj, centre);
					lock[0] = false;
				}
			});
			newBoom.setPriority(9);
			newBoom.setName(newBoom.getName().replace("Thread", "ExplosionThread"));
			newBoom.start();
		}
	}

	public static class ClientUpdateInfo
	{
		final int[]	affectedBlocks;
		final int	dim;
		final int[]	centre;

		public ClientUpdateInfo(List<Integer> affected, Vector3 mid, int dim_)
		{
			affectedBlocks = new int[affected.size()];
			for (int i = 0; i < affected.size(); i++)
			{
				affectedBlocks[i] = affected.get(i);
			}
			dim = dim_;
			centre = new int[3];
			centre[0] = mid.intX();
			centre[1] = mid.intY();
			centre[2] = mid.intZ();
		}

	}

	public static class VictimStuff
	{
		final int		entity;
		final int		dimension;
		final float		damage;
		final Explosion	explosion;

		public VictimStuff(Entity hit, float d, Explosion e)
		{
			entity = hit.getEntityId();
			dimension = hit.dimension;
			damage = d;
			explosion = e;
		}

		public VictimStuff(int id, int dim, float d, Explosion e)
		{
			entity = id;
			dimension = dim;
			damage = d;
			explosion = e;
		}

		@Override
		public boolean equals(Object o)
		{
			if (o instanceof VictimStuff) { return ((VictimStuff) o).entity == entity; }
			return super.equals(o);
		}
	}

	public static class ExplosionVictimTicker
	{
		public static Vector<VictimStuff>		victims			= new Vector<ExplosionCustom.VictimStuff>();
		public static Vector<ClientUpdateInfo>	clientUpdates	= new Vector<ExplosionCustom.ClientUpdateInfo>();
		public final static Map<Long, Float>	resists			= new ConcurrentHashMap<Long, Float>();
		private static HashSet					toRemove		= new HashSet();

		@SubscribeEvent
		public void tick(WorldTickEvent evt)
		{

			if (evt.phase == Phase.END)
			{
				if(clientUpdates.size() > 0)
				{
					//System.out.println("There are "+clientUpdates.size());
					ArrayList<ClientUpdateInfo> temp = new ArrayList(clientUpdates);
					for(ClientUpdateInfo i: temp)
					{
						NBTTagCompound nbt = new NBTTagCompound();
						nbt.setIntArray("mid", i.centre);
						nbt.setIntArray("affected", i.affectedBlocks);
						MessageClient message = new MessageClient(MessageClient.BLASTAFFECTED,nbt);
						PacketHandler.packetPipeline.sendToDimension(message, i.dim);
						toRemove.add(i);
					}
					clientUpdates.removeAll(toRemove);
					toRemove.clear();
				}
				if(victims.size() > 0)
				{
					ArrayList<VictimStuff> temp = new ArrayList(victims);
					for (VictimStuff v : temp)
					{
						applyDamage(v);
						toRemove.add(v);
					}
					victims.removeAll(toRemove);
					toRemove.clear();
				}
			}
			else
			{
				Vector entities = worldEntities.get(evt.world.provider.getDimensionId());
				if (entities == null)
				{
					entities = new Vector();
				}
				entities.clear();
				entities.addAll(evt.world.loadedEntityList);
				worldEntities.put(evt.world.provider.getDimensionId(), entities);
			}
		}

		public static void applyDamage(VictimStuff v)
		{
			boolean damagePlayerTest = true;
			World world = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(v.dimension);
			Entity hit = world.getEntityByID(v.entity);
			// System.out.println(v.entity+" "+v.damage);
			if (hit != null && !hit.isDead)
			{
				hit.attackEntityFrom(DamageSource.setExplosionSource(v.explosion), v.damage);
			}
		}

		public static void addVictim(Entity victim, float damage, Explosion blast)
		{
			VictimStuff v = new VictimStuff(victim, damage, blast);
			victims.add(v);
		}
	}

	static final boolean[] toClear = { false, false };

	static final ExplosionCustom							instance		= new ExplosionCustom(null, null,
			Vector3.getNewVectorFromPool(), 0);
	public static final Vector<ExplosionStuff>				explosions		= new Vector<ExplosionStuff>();
	public static final ConcurrentHashMap<Integer, Vector>	worldEntities	= new ConcurrentHashMap();
	private static int maxThreads = -1;
	private static final Thread boomThread = new Thread(new Runnable()
	{

		@Override
		public void run()
		{
			while (true)
			{
				boolean boomed = false;
				if (explosions.size() > 0)
				{
					int num;
					if(maxThreads == -1)
					{
						maxThreads = Runtime.getRuntime().availableProcessors();
					}
					num = maxThreads;
					ArrayList<ExplosionStuff> booms = new ArrayList(explosions);
					num = Math.min(num, booms.size());
					Set toRemove = new HashSet();
					for (int i = 0; i < num; i++)
					{
						ExplosionStuff boom = booms.get(i);
						if (boom.lock[0])
						{
							boom.doBoom();
						}
						else
						{
							toRemove.add(boom);
						}
						boomed = true;
					}
					explosions.removeAll(toRemove);
				}
				if (toClear[0])
				{
					explosions.clear();
					toClear[0] = false;
				}
				if (!boomed)
				{
					try
					{
						Thread.sleep(100);
					}
					catch (InterruptedException e)
					{
						// e.printStackTrace();
					}
				}
			}
		}
	});

	static
	{
		boomThread.setName("explosionThread");
		System.out.println("Starting explosion thread");
		FMLCommonHandler.instance().bus().register(new ExplosionVictimTicker());
		boomThread.start();
	}

	public static void clearInstance()
	{
		ExplosionCustom.blocks.clear();
		toClear[0] = true;
	}

	private static ExplosionCustom getInstance()
	{
		return instance;
	}

	static HashMap<Integer, Vector<BlockChange>> blocks = new HashMap();

	static Block getBlock(Vector3 location, IBlockAccess worldObj, int dimension)
	{
		BlockChange b1 = new BlockChange(location, dimension, null);
		getInstance();
		for (BlockChange b : ExplosionCustom.getListForWorld(dimension))
		{
			if (b.equals(b1)) return b.blockTo;
		}
		return location.getBlock(worldObj);
	}

	static float getExplosionResistance(Vector3 location, ExplosionCustom blast, IBlockAccess worldObj)
	{
		return location.getExplosionResistance(blast, worldObj);
	}

	public static void addBlockChange(Vector3 location, World worldObj, Block blockTo, int meta)
	{
		addBlockChange(new BlockChange(location, worldObj.provider.getDimensionId(), blockTo, meta), worldObj);
	}

	public static void addBlockChange(BlockChange b1, World worldObj)
	{

		if (b1.location.y > 255) return;

		getInstance();
		for (BlockChange b : ExplosionCustom.getListForWorld(worldObj.provider.getDimensionId()))
		{
			if (b.equals(b1)) return;
		}
		toClear[0] = false;
		getInstance();
		ExplosionCustom.getListForWorld(worldObj.provider.getDimensionId()).add(b1);
	}

	public static Vector<BlockChange> getListForWorld(int dimension)
	{
		getInstance();
		Vector<BlockChange> ret = ExplosionCustom.blocks.get(dimension);
		if (ret == null)
		{
			ret = new Vector<BlockChange>();
			getInstance();
			ExplosionCustom.blocks.put(dimension, ret);
		}
		return ret;
	}
}
