package thut.api.boom;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

public class ExplosionCustom extends Explosion
{
    public static interface IEntityHitter
    {
        void hitEntity(Entity e, float power, Explosion boom);
    }

    static class HitEntity
    {
        final Entity entity;
        final float  blastStrength;

        public HitEntity(Entity entity, float blastStrength)
        {
            this.entity = entity;
            this.blastStrength = blastStrength;
        }
    }

    static class BlastResult
    {
        final List<BlockPos>  results;
        final List<HitEntity> hit;
        final boolean         done;

        public BlastResult(List<BlockPos> results, List<HitEntity> hit, boolean done)
        {
            this.results = results;
            this.hit = hit;
            this.done = done;
        }
    }

    public static int            MAX_RADIUS             = 127;
    public static int[]          MAXPERTICK             = { 10000, 50000 };
    public static float          MINBLASTDAMAGE         = 0.1f;
    public static boolean        AFFECTINAIR            = true;
    public static Block          melt;
    public static Block          solidmelt;
    public static Block          dust;
    public IEntityHitter         hitter                 = new IEntityHitter()
                                                        {

                                                            @Override
                                                            public void hitEntity(Entity e, float power, Explosion boom)
                                                            {
                                                                float area = e.width * e.height;
                                                                float damage = area * power;
                                                                e.attackEntityFrom(
                                                                        DamageSource.causeExplosionDamage(boom),
                                                                        damage);
                                                            }
                                                        };
    int                          dimension;
    int                          currentIndex           = 0;
    int                          nextIndex              = 0;
    float                        minBlastDamage;
    public int[]                 maxPerTick;
    World                        world;
    Vector3                      centre;

    float                        strength;

    public boolean               meteor                 = false;

    public EntityPlayer          owner                  = null;

    List<Entity>                 targets                = new ArrayList<Entity>();

    private double               explosionX;

    private double               explosionY;

    private double               explosionZ;
    private float                explosionSize;

    private boolean              isSmoking              = false;

    Entity                       exploder;

    public Set<BlockPos>         affectedBlockPositions = new HashSet<BlockPos>();

    Map<EntityLivingBase, Float> damages                = new HashMap<EntityLivingBase, Float>();

    List<Chunk>                  affected               = new ArrayList<Chunk>();

    public ExplosionCustom(World world, Entity par2Entity, double x, double y, double z, float power)
    {
        this(world, par2Entity, Vector3.getNewVector().set(x, y, z), power);
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
        dimension = world.provider.getDimension();
        minBlastDamage = MINBLASTDAMAGE;
        maxPerTick = MAXPERTICK;
    }

    public void addChunkPosition(Vector3 v)
    {
        affectedBlockPositions.add(new BlockPos(v.intX(), v.intY(), v.intZ()));
    }

    public boolean canBreak(Vector3 location)
    {
        boolean ret = true;

        if (owner != null)
        {
            try
            {
                BreakEvent evt = new BreakEvent(world, location.getPos(), location.getBlockState(world), owner);
                MinecraftForge.EVENT_BUS.post(evt);
                if (evt.isCanceled()) return false;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return false;
            }
        }

        return ret;
    }

    public void doExplosion()
    {
        this.world.playSound((EntityPlayer) null, this.explosionX, this.explosionY, this.explosionZ,
                SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F,
                (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);

        if (this.explosionSize >= 2.0F && this.isSmoking)
        {
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.explosionX, this.explosionY,
                    this.explosionZ, 1.0D, 0.0D, 0.0D);
        }
        else
        {
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.explosionX, this.explosionY,
                    this.explosionZ, 1.0D, 0.0D, 0.0D);
        }
        MinecraftForge.EVENT_BUS.register(this);
        nextIndex = currentIndex + MAXPERTICK[0];
    }

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

    /** Does the second part of the explosion (sound, particles, drop spawn) */
    @Override
    public void doExplosionB(boolean par1)
    {

    }

    // TODO Revisit this to make blast energy more conserved
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
            boo.doExplosion();
            return;
        }
        Vector3 absorbedLoc = Vector3.getNewVector();
        float remainingEnergy = 0;
        density -= resist;

        while (energy > 0 && density > 0)
        {
            locations.add(hitLocation.subtract(velocity.normalize()));
            blasts.add(blast);
            hitLocation = hitLocation.add(velocity.normalize());
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
            ExplosionCustom boo = new ExplosionCustom(worldObj, exploder, source, strength * factor);
            boo.doExplosion();

            }
            }
            }
        if (remainingEnergy > 10)
        {
            absorbedLoc = absorbedLoc.subtract(velocity.normalize());
            ExplosionCustom boo = new ExplosionCustom(worldObj, exploder, absorbedLoc, remainingEnergy * factor);
            boo.doExplosion();
        }
    }

    /** Handles the actual block removal, has a meteor argument to allow
     * converting to ash or dust on impact
     * 
     * @param destroyed
     * @param pos */
    public void doMeteorStuff(IBlockState destroyed, BlockPos pos)
    {
        if (!destroyed.getMaterial().isSolid() && !destroyed.getMaterial().isLiquid()) return;
        if (!meteor)
        {
            world.setBlockToAir(pos);
            return;
        }
        float resistance = destroyed.getBlock().getExplosionResistance(world, pos, exploder, this);
        if (melt == null) melt = Blocks.AIR;
        if (dust == null) dust = Blocks.AIR;
        if (resistance > 2 && !destroyed.getMaterial().isLiquid())
        {
            int meta = (int) Math.min(resistance / 2, 15);
            world.setBlockState(pos, CompatWrapper.getBlockStateFromMeta(melt, meta));
        }
        else
        {
            world.setBlockState(pos, dust.getDefaultState());
        }
    }

    HashMap<Integer, Float>      resists    = new HashMap<Integer, Float>(100000, 1);
    HashSet<Integer>             blockedSet = new HashSet<>(100000, 1);
    Int2ObjectOpenHashMap<Float> thisShell  = new Int2ObjectOpenHashMap<>();
    // used to speed up the checking of if a resist exists in the map
    BitSet                       checked    = new BitSet();
    Vector3                      r          = Vector3.getNewVector(), rAbs = Vector3.getNewVector(),
            rHat = Vector3.getNewVector(), rTest = Vector3.getNewVector(), rTestPrev = Vector3.getNewVector(),
            rTestAbs = Vector3.getNewVector();

    @SubscribeEvent
    public void WorldUnloadEvent(Unload evt)
    {
        if (evt.getWorld() == world) MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    void doRemoveBlocks(WorldTickEvent evt)
    {
        if (evt.phase == Phase.START || evt.world != world) return;
        BlastResult result = new Checker(this).getBlocksToRemove();
        applyBlockEffects(result.results);
        applyEntityEffects(result.hit);
        doExplosionB(false);
        ExplosionEvent evt2 = new ExplosionEvent.Detonate(this.world, this, targets);
        MinecraftForge.EVENT_BUS.post(evt2);
        if (result.done)
        {
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

    private void applyBlockEffects(List<BlockPos> toRemove)
    {
        this.getAffectedBlockPositions().clear();
        for (BlockPos pos : toRemove)
        {
            this.getAffectedBlockPositions().add(pos);
            IBlockState state = world.getBlockState(pos);
            doMeteorStuff(state, pos);
        }
    }

    private void applyEntityEffects(List<HitEntity> affected)
    {
        targets.clear();
        for (HitEntity e : affected)
        {
            Entity hit = e.entity;
            float power = e.blastStrength;
            if (power > 0)
            {
                hitter.hitEntity(hit, power, this);
                targets.add(hit);
            }
        }
    }

    public ExplosionCustom setMeteor(boolean meteor)
    {
        this.meteor = meteor;
        return this;
    }
}
