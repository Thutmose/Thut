package thut.api.maths;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.atan2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;
import thut.lib.CompatWrapper;

/** @author Thutmose */
public class Vector3
{
    public static final class MutableBlockPos extends BlockPos
    {
        /** Mutable X Coordinate */
        public int x;
        /** Mutable Y Coordinate */
        public int y;
        /** Mutable Z Coordinate */
        public int z;

        public MutableBlockPos(int x_, int y_, int z_)
        {
            super(0, 0, 0);
            this.x = x_;
            this.y = y_;
            this.z = z_;
        }

        MutableBlockPos(int p_i46025_1_, int p_i46025_2_, int p_i46025_3_, Object p_i46025_4_)
        {
            this(p_i46025_1_, p_i46025_2_, p_i46025_3_);
        }

        /** Calculate the cross product of this and the given Vector */
        @Override
        public BlockPos crossProduct(Vec3i vec)
        {
            return super.crossProduct(vec);
        }

        /** Get the X coordinate */
        @Override
        public int getX()
        {
            return this.x;
        }

        /** Get the Y coordinate */
        @Override
        public int getY()
        {
            return this.y;
        }

        /** Get the Z coordinate */
        @Override
        public int getZ()
        {
            return this.z;
        }

        void setTo(Vector3 vector)
        {
            x = vector.intX();
            y = vector.intY();
            z = vector.intZ();
        }
    }

    public static final Vector3      secondAxis    = Vector3.getNewVector().set(0, 1, 0);
    public static final Vector3      secondAxisNeg = Vector3.getNewVector().set(0, -1, 0);
    public static final Vector3      firstAxis     = Vector3.getNewVector().set(1, 0, 0);
    public static final Vector3      firstAxisNeg  = Vector3.getNewVector().set(-1, 0, 0);
    public static final Vector3      thirdAxis     = Vector3.getNewVector().set(0, 0, 1);
    public static final Vector3      thirdAxisNeg  = Vector3.getNewVector().set(0, 0, -1);

    public static final Vector3      empty         = Vector3.getNewVector();
    public static final int          length        = 3;
    public static Vector3            vecMult       = Vector3.getNewVector();
    public static double[][]         rotBox        = new double[3][3];

    public static Map<String, Fluid> fluids;

    static Vector3                   move1         = Vector3.getNewVector();

    static Vector3                   move2         = Vector3.getNewVector();

    public static Vector3 entity(Entity e)
    {
        if (e != null) return Vector3.getNewVector().set(e.posX, e.posY + e.getEyeHeight(), e.posZ);
        return null;
    }

    public static Vector3 findMidPoint(List<Vector3> points)
    {
        Vector3 mid = Vector3.getNewVector();
        for (int j = 0; j < points.size(); j++)
        {
            mid.addTo(points.get(j));
        }
        if (points.size() > 0)
        {
            mid.scalarMultBy(1 / ((double) points.size()));
        }
        return mid;
    }

    /** Locates the first solid block in the line indicated by the direction
     * vector, starting from the source if range is given as 0, it will check
     * out to 320 blocks.
     * 
     * @param world
     * @param source
     * @param direction
     * @param range
     * @return */
    public static Vector3 findNextSolidBlock(IBlockAccess world, Vector3 source, Vector3 direction, double range)
    {
        direction = direction.normalize();
        double xprev = source.x, yprev = source.y, zprev = source.z;
        double dx, dy, dz;
        Vector3 test = Vector3.getNewVector();
        for (double i = 0; i < range; i += 1)
        {
            dx = i * direction.x;
            dy = i * direction.y;
            dz = i * direction.z;

            double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

            if (ytest > 255) return null;

            if (!(Int(xtest) == Int(xprev) && Int(ytest) == Int(yprev) && Int(ztest) == Int(zprev)))
            {
                test.set(xtest, ytest, ztest);
                boolean clear = test.clearOfBlocks(world);

                if (!clear) { return Vector3.getNewVector().set(Int(xtest), Int(ytest), Int(ztest)); }
            }

            yprev = ytest;
            xprev = xtest;
            zprev = ztest;
        }
        return null;
    }

    public static Vector3 getNewVector()
    {
        return new Vector3();
    }

    /** determines whether the source can see out as far as range in the given
     * direction.
     * 
     * @param world
     * @param source
     * @param direction
     * @param range
     * @return */
    public static Vector3 getNextSurfacePoint(IBlockAccess world, Vector3 source, Vector3 direction, double range)
    {
        direction = direction.normalize();

        double dx, dy, dz;

        for (double i = 0; i < range; i += 0.0625)
        {
            dx = i * direction.x;
            dy = i * direction.y;
            dz = i * direction.z;

            double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

            boolean check = isPointClearBlocks(xtest, ytest - dy, ztest - dz, world);
            check = check && isPointClearBlocks(xtest - dx, ytest, ztest - dz, world);
            check = check && isPointClearBlocks(xtest - dx, ytest - dy, ztest, world);
            if (!check) { return Vector3.getNewVector().set(xtest, ytest, ztest); }
        }
        return null;
    }

    /** determines whether the source can see out as far as range in the given
     * direction. This version ignores blocks like leaves and wood, used for
     * finding the surface of the ground.
     * 
     * @param world
     * @param source
     * @param direction
     * @param range
     * @return */
    public static Vector3 getNextSurfacePoint2(IBlockAccess world, Vector3 source, Vector3 direction, double range)
    {
        direction = direction.normalize();
        double dx, dy, dz;
        Vector3 temp = Vector3.getNewVector();
        for (double i = 0; i < range; i += 0.5)
        {
            dx = i * direction.x;
            dy = i * direction.y;
            dz = i * direction.z;

            double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

            boolean check = isNotSurfaceBlock((World) world, temp.set(xtest, ytest, ztest));// isPointClearBlocks(xtest,
                                                                                            // ytest,
                                                                                            // ztest,
                                                                                            // world);
            if (!check) { return Vector3.getNewVector().set(xtest, ytest, ztest); }
        }
        return null;
    }

    public static int Int(double x)
    {
        return MathHelper.floor(x);
    }

    public static boolean isNotSurfaceBlock(World world, Vector3 v)
    {
        IBlockState state = v.getBlockState(world);
        Block b = state.getBlock();

        if (b instanceof IFluidBlock)
        {
            Fluid f = ((IFluidBlock) b).getFluid();
            return f.getViscosity() < Integer.MAX_VALUE;
        }

        boolean ret = (b == null || v.getBlockMaterial(world).isReplaceable() || v.isClearOfBlocks(world)
                || !state.isNormalCube() || b.isLeaves(state, world, v.getPos()) || b.isWood(world, v.getPos()))
                && v.y > 1;

        return ret;
    }

    public static boolean isPointClearBlocks(double x, double y, double z, IBlockAccess world)
    {
        int x0 = MathHelper.floor(x), y0 = MathHelper.floor(y), z0 = MathHelper.floor(z);
        BlockPos pos;
        IBlockState state = world.getBlockState(pos = new BlockPos(x0, y0, z0));
        if (state == null) return true;
        Block block = state.getBlock();

        if (state.isNormalCube()) return false;

        if (block == null || block == Blocks.AIR || !block.isCollidable()) return true;

        List<AxisAlignedBB> aabbs = new ArrayList<AxisAlignedBB>();
        Vector3 v = getNewVector().set(x, y, z);

        if (world instanceof World)
            state.addCollisionBoxToList((World) world, pos, v.getAABB().grow(0.03, 0.03, 0.03), aabbs, null, false);
        if (aabbs.size() == 0) return true;

        for (AxisAlignedBB aabb : aabbs)
        {
            if (aabb != null)
            {
                if (y <= aabb.maxY && y >= aabb.minY) return false;
                if (z <= aabb.maxZ && z >= aabb.minZ) return false;
                if (x <= aabb.maxX && x >= aabb.minX) return false;
            }
        }

        return true;
    }

    public static boolean isVisibleEntityFromEntity(Entity looker, Entity target)
    {
        if (looker == null || target == null) return false;
        return looker.getEntityWorld().rayTraceBlocks(
                new Vec3d(looker.posX, looker.posY + looker.getEyeHeight(), looker.posZ),
                new Vec3d(target.posX, target.posY + target.getEyeHeight(), target.posZ), false, true, false) == null;
    }

    /** determines whether the source can see out as far as range in the given
     * direction.
     * 
     * @param world
     * @param source
     * @param direction
     * @param range
     * @return */
    public static boolean isVisibleRange(IBlockAccess world, Vector3 source, Vector3 direction, double range)
    {
        direction = direction.normalize();

        if (world instanceof World)
        {
            direction.scalarMultBy(range).addTo(source);
            return ((World) world).rayTraceBlocks(new Vec3d(source.x, source.y, source.z),
                    new Vec3d(direction.x, direction.y, direction.z), false, true, false) == null;
        }

        double dx, dy, dz;
        for (double i = 0; i < range; i += 0.0625)
        {
            dx = i * direction.x;
            dy = i * direction.y;
            dz = i * direction.z;

            double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

            boolean check = isPointClearBlocks(xtest, ytest, ztest, world);
            if (!check) { return false; }
        }
        return true;
    }

    public static boolean movePointOutOfBlocks(Vector3 v, World world)
    {
        Vector3 v1 = move1.set(v);
        Vector3 v2 = move2.set(v);

        long start = System.nanoTime();

        if (!v.clearOfBlocks(world))
        {
            int n = 0;

            clear:
            while (!v.clearOfBlocks(world))
            {
                for (EnumFacing side : EnumFacing.values())
                {
                    v2.set(v);
                    if (v.offsetBy(side).clearOfBlocks(world))
                    {
                        break clear;
                    }
                    v.set(v2);
                }
                boolean step = true;
                if (n < 2)
                {
                    v.offset(EnumFacing.UP);
                }
                else if (n < 4)
                {
                    if (step)
                    {
                        step = false;
                        v.set(v1);
                    }
                    v.offsetBy(EnumFacing.NORTH);
                }
                else if (n < 6)
                {
                    if (!step)
                    {
                        step = true;
                        v.set(v1);
                    }
                    v.offsetBy(EnumFacing.SOUTH);
                }
                else if (n < 8)
                {
                    if (step)
                    {
                        step = false;
                        v.set(v1);
                    }
                    v.offsetBy(EnumFacing.EAST);
                }
                else if (n < 10)
                {
                    if (!step)
                    {
                        step = true;
                        v.set(v1);
                    }
                    v.offsetBy(EnumFacing.WEST);
                }
                else if (n < 12)
                {
                    if (step)
                    {
                        step = false;
                        v.set(v1);
                    }
                    v.offsetBy(EnumFacing.DOWN);
                }
                n++;
                if (n >= 12) break;
            }

            long end = System.nanoTime() - start;

            double time = (end / (1000000000D));
            if (time > 0.001) System.out.println("Took " + time + "s to check");

            if (v.clearOfBlocks(world)) { return true; }
            return false;
        }
        return true;
    }

    public static Vector3 readFromBuff(ByteBuf dat)
    {
        Vector3 ret = Vector3.getNewVector();
        ret.x = dat.readDouble();
        ret.y = dat.readDouble();
        ret.z = dat.readDouble();
        return ret;
    }

    public static Vector3 readFromNBT(NBTTagCompound nbt, String tag)
    {
        if (!nbt.hasKey(tag + "x")) return null;

        Vector3 ret = Vector3.getNewVector();
        ret.x = nbt.getDouble(tag + "x");
        ret.y = nbt.getDouble(tag + "y");
        ret.z = nbt.getDouble(tag + "z");
        return ret;
    }

    public static void rotateAboutAngles(Vector3[] points, double pitch, double yaw, Vector3 temp, Vector3 temp1)
    {
        for (int i = 0; i < points.length; i++)
        {
            points[i] = points[i].rotateAboutAngles(pitch, yaw, temp, temp1);
        }
    }

    /** Returns the dot (scalar) product of the two vectors
     * 
     * @param vector1
     * @param vector2
     * @return */
    public static double vectorDot(Vector3 vector1, Vector3 vector2)
    {
        double dot = 0;
        for (int i = 0; i < Vector3.length; i++)
        {
            dot += vector1.get(i) * vector2.get(i);
        }
        return dot;
    }

    public double   x;

    public double   y;

    public double   z;

    MutableBlockPos pos;

    private Vector3()
    {
        this.x = this.y = this.z = 0;
    }

    /** This takes degrees then converts to radians, as it seems most people
     * like to work with degrees.
     * 
     * @param pitch
     * @param yaw */
    private Vector3(double pitch, double yaw)
    {
        this.x = 1;
        this.y = Math.toRadians(pitch);
        this.z = Math.toRadians(yaw);
    }

    private Vector3(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private Vector3(Entity e, boolean bool)
    {
        if (e != null && bool)
        {
            this.x = e.posX;
            this.y = e.posY + e.height / 2;
            this.z = e.posZ;
        }
        else if (e != null)
        {
            this.x = e.posX;
            this.y = e.posY + e.getEyeHeight();
            this.z = e.posZ;
        }
    }

    //
    private Vector3(Object a)
    {
        this();
        set(a);
    }

    private Vector3(Object a, Object b)
    {
        this();
        Vector3 A = Vector3.getNewVector().set(a);
        Vector3 B = Vector3.getNewVector().set(b);
        this.set(B.subtract(A));
    }

    private Vector3(Vec3d vec)
    {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }

    public Vector3 add(double i, double j, double k)
    {
        return Vector3.getNewVector().set(x + i, j + y, k + z);
    }

    public void add(int i, double j)
    {
        if (i == 0)
        {
            x += j;
        }
        else if (i == 1)
        {
            y += j;
        }
        else if (i == 2)
        {
            z += j;
        }
    }

    /** Adds vectorA to vectorB
     * 
     * @param vectorA
     * @param vectorB
     * @return */
    public Vector3 add(Vector3 vectorB)
    {
        Vector3 vectorC = Vector3.getNewVector();
        for (int i = 0; i < 3; i++)
        {
            vectorC.set(i, this.get(i) + vectorB.get(i));
        }
        return vectorC;
    }

    public Vector3 addTo(double i, double j, double k)
    {
        x += i;
        y += j;
        z += k;
        return this;
    }

    /** Adds vectorA to vectorB
     * 
     * @param vectorA
     * @param vectorB
     * @return */
    public Vector3 addTo(Vector3 b)
    {
        if (b == null) return this;
        x += b.x;
        y += b.y;
        z += b.z;
        return this;
    }

    public void addVelocities(Entity e)
    {
        e.addVelocity(x, y, z);
    }

    public List<Entity> allEntityLocationExcluding(int range, double size, Vector3 direction, Vector3 source,
            World world, Entity excluded)
    {
        direction = direction.normalize();

        double dx, dy, dz;
        List<Entity> ret = new ArrayList<Entity>();

        for (double i = 0; i < range; i += size)
        {
            dx = i * direction.x;
            dy = i * direction.y;
            dz = i * direction.z;

            double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

            boolean check = isPointClearBlocks(xtest, ytest, ztest, world);

            if (!check)
            {
                break;
            }

            double x0 = xtest, y0 = ytest, z0 = ztest;
            List<Entity> targets = world.getEntitiesWithinAABBExcludingEntity(excluded,
                    new AxisAlignedBB(x0 - size, y0 - size, z0 - size, x0 + size, y0 + size, z0 + size));
            if (targets != null && targets.size() > 0)
            {
                for (Entity e : targets)
                {
                    if (e instanceof EntityLivingBase && !ret.contains(e) && !e.isRidingOrBeingRiddenBy(excluded))
                    {
                        ret.add(e);
                    }
                }
            }
        }
        return ret;
    }

    public int blockCount(IBlockAccess world, Block block, int range)
    {
        int ret = 0;
        Vector3 v = this.copy();
        for (int i = -range; i <= range; i++)
            for (int j = -range; j <= range; j++)
                for (int k = -range; k <= range; k++)
                {
                    Vector3 test = v.set(this).addTo(i, j, k);
                    if (test.getBlock(world) == block)
                    {
                        ret++;
                    }
                }

        return ret;
    }

    public int blockCount2(World world, Block block, int range)
    {
        int ret = 0;
        Vector3 v = this.copy();
        Chunk chunk = world.getChunkFromBlockCoords(new BlockPos(intX(), 0, intZ()));
        Block testBlock;
        for (int i = -range / 2; i <= range / 2; i++)
            for (int j = -range / 2; j <= range / 2; j++)
                for (int k = -range / 2; k <= range / 2; k++)
                {
                    int i1 = MathHelper.floor(intX() / 16.0D);
                    int k1 = MathHelper.floor(intZ() / 16.0D);
                    int j1 = MathHelper.floor(intY() / 16.0D);

                    int i2 = MathHelper.floor((intX() + i) / 16.0D);
                    int k2 = MathHelper.floor((intZ() + k) / 16.0D);
                    int j2 = MathHelper.floor((intY() + j) / 16.0D);

                    if (!(i1 == i2 && k1 == k2 && j1 == j2)) continue;
                    v.set(this);
                    Vector3 test = v.addTo(i, j, k);
                    testBlock = chunk.getBlockState(test.intX(), test.intY(), test.intZ()).getBlock();
                    if (testBlock == block)
                    {
                        ret++;
                    }
                }

        return ret;
    }

    public void breakBlock(World world, boolean drop)
    {
        if (getBlock(world) != null)
        {
            world.destroyBlock(getPos(), drop);
        }
    }

    public void breakBlock(World world, int fortune, boolean drop)
    {
        if (getBlock(world) != null)
        {
            if (drop) getBlock(world).dropBlockAsItem(world, getPos(), getBlockState(world), fortune);
            world.destroyBlock(getPos(), false);
        }
    }

    public boolean canSeeSky(IBlockAccess world)
    {
        return getTopBlockY(world) <= y;
    }

    public Vector3 clear()
    {
        return this.set(0, 0, 0);
    }

    public boolean clearOfBlocks(IBlockAccess worldMap)
    {

        IBlockState state = getBlockState(worldMap);
        if (state == null) return true;
        Block block = state.getBlock();

        if (state.isNormalCube()) return false;

        if (block == null || block == Blocks.AIR || !state.getMaterial().blocksMovement() || !block.isCollidable())
            return true;

        List<AxisAlignedBB> aabbs = new ArrayList<AxisAlignedBB>();

        aabbs.add(state.getBoundingBox(worldMap, getPos()));

        if (aabbs.size() == 0) return true;

        for (AxisAlignedBB aabb : aabbs)
        {
            if (aabb != null)
            {
                if (y <= aabb.maxY && y >= aabb.minY) return false;
                if (z <= aabb.maxZ && z >= aabb.minZ) return false;
                if (x <= aabb.maxX && x >= aabb.minX) return false;
            }
        }

        return true;
    }

    public Vector3 copy()
    {
        Vector3 newVector = Vector3.getNewVector().set(x, y, z);
        return newVector;
    }

    public double distanceTo(Vector3 vec)
    {
        return (this.subtract(vec)).mag();
    }

    public double distTo(Vector3 pointB)
    {
        return this.subtract(pointB).mag();
    }

    public double distToEntity(Entity e)
    {
        return distanceTo(entity(e));
    }

    public double distToSq(Vector3 pointB)
    {
        return subtract(pointB).magSq();
    }

    public boolean doChunksExist(World world, int distance)
    {
        return world.isAreaLoaded(getPos(), distance);
    }

    /** Returns the dot (scalar) product of the two vectors
     * 
     * @param vector1
     * @param vector2
     * @return */
    public double dot(Vector3 vector2)
    {
        double dot = 0;
        for (int i = 0; i < 3; i++)
        {
            dot += this.get(i) * vector2.get(i);
        }
        return dot;
    }

    @Override
    public boolean equals(Object vec)
    {
        if (!(vec instanceof Vector3)) return false;
        Vector3 v = (Vector3) vec;

        return v.x == x && v.y == y && v.z == z;// sameBlock(v);
    }

    @SuppressWarnings("unchecked")
    public Vector3 findClosestVisibleObject(IBlockAccess world, boolean water, int sightDistance, Object matching)
    {
        int size = Math.min(sightDistance, 30);
        List<Object> list = new ArrayList<Object>();
        Block seekingBlock = null;
        Class<?> seekingClass = null;
        boolean predicate = matching instanceof Predicate<?>;
        Predicate<Object> matcher = null;
        if (predicate) matcher = (Predicate<Object>) matching;
        boolean isInterface = false;
        boolean blockList = false;
        boolean predicateList = false;
        Vector3 temp = getNewVector();
        if (matching instanceof Block)
        {
            seekingBlock = (Block) matching;
        }
        if (matching instanceof Class)
        {
            seekingClass = (Class<?>) matching;
            if (seekingClass.isInterface())
            {
                isInterface = true;
            }
        }
        if (matching instanceof Collection<?> && ((Collection<?>) matching).toArray()[0] instanceof Block)
        {
            blockList = true;
            list.addAll((Collection<?>) matching);
        }
        if (matching instanceof Collection<?> && ((Collection<?>) matching).toArray()[0] instanceof Predicate<?>)
        {
            predicateList = true;
            list.addAll((Collection<?>) matching);
        }
        double rMag;
        Vector3 r = Vector3.getNewVector(), rAbs = Vector3.getNewVector(), rHat = Vector3.getNewVector(),
                rTest = Vector3.getNewVector(), rTestPrev = Vector3.getNewVector(), rTestAbs = Vector3.getNewVector(),
                ret = Vector3.getNewVector();

        HashMap<Class<?>, List<Object>> interfaces = new HashMap<>();

        loop:
        for (int i = 0; i < size * size * size; i++)
        {

            Cruncher.indexToVals(i, r);
            rAbs.set(r).addTo(this);
            rHat.set(temp.set(r).norm());
            double rm;
            if ((rm = r.mag()) > size || rm > sightDistance) continue;

            if (rAbs.isAir(world) && !(r.isEmpty())) continue;

            rTest.clear();
            rTestPrev.clear();
            rMag = r.mag();
            float dj = 1;
            for (float j = 0F; j <= rMag; j += dj)
            {
                rTest = temp.set(rHat).scalarMultBy(j);
                if (!(rTest.sameBlock(rTestPrev)))
                {
                    rTestAbs.set(rTest).addTo(this);
                    IBlockState state = rTestAbs.getBlockState(world);
                    if (state == null) continue loop;
                    Block b = state.getBlock();
                    if (predicateList)
                    {
                        for (Object o : list)
                        {
                            if (((Predicate<IBlockState>) o).apply(state))
                            {
                                ret.set(rTestAbs);
                                return ret;
                            }
                        }
                    }
                    if (isInterface)
                    {
                        List<Object> tempList;
                        if ((tempList = interfaces.get(b.getClass())) != null)
                        {
                        }
                        else
                        {
                            tempList = new ArrayList<>();
                            interfaces.put(b.getClass(), tempList);
                            for (Object o : b.getClass().getInterfaces())
                            {
                                tempList.add(o);
                            }
                        }
                        list = tempList;
                    }
                    if (matcher != null && matcher.apply(state))
                    {
                        ret.set(rTestAbs);
                        return ret;
                    }
                    else if (seekingBlock != null && b == seekingBlock)
                    {
                        ret.set(rTestAbs);
                        return ret;
                    }
                    else if (!isInterface && seekingClass != null && b.getClass().isAssignableFrom(seekingClass))
                    {
                        ret.set(rTestAbs);
                        return ret;
                    }
                    else if (seekingClass != null && list.contains(seekingClass))
                    {
                        ret.set(rTestAbs);
                        return ret;
                    }
                    else if (blockList && list.contains(b))
                    {
                        ret.set(rTestAbs);
                        return ret;
                    }
                    else if (!rTestAbs.isClearOfBlocks(world))
                    {
                        continue loop;
                    }
                    else if (!water && state.getMaterial() == Material.WATER)
                    {
                        continue loop;
                    }
                }
            }
        }
        return null;
    }

    public Vector3 findNextSolidBlock(IBlockAccess world, Vector3 direction, double range)
    {
        return findNextSolidBlock(world, this, direction, range);
    }

    public Entity firstEntityExcluding(double range, Vector3 direction, World world, boolean effect, Entity excluded)
    {
        List<Entity> toExclude = new ArrayList<Entity>();
        if (excluded != null)
        {
            toExclude.add(excluded);
            toExclude.addAll(excluded.getRecursivePassengers());
        }
        return firstEntityExcluding(range, direction, world, effect, toExclude);
    }

    public Entity firstEntityExcluding(double range, Vector3 direction, World world, boolean effect,
            List<Entity> excluded)
    {
        direction = direction.normalize();
        double xprev = x, yprev = y, zprev = z;
        double dx, dy, dz;

        for (double i = 0; i < range; i += 0.0625)
        {
            dx = i * direction.x;
            dy = i * direction.y;
            dz = i * direction.z;

            double xtest = (x + dx), ytest = (y + dy), ztest = (z + dz);

            boolean check = isPointClearBlocks(xtest, ytest, ztest, world);

            if (!check)
            {
                break;
            }

            if (effect && world.isRemote)
            {
                world.spawnParticle(EnumParticleTypes.TOWN_AURA, xtest, ytest, ztest, 0, 0, 0);
            }

            if (!((int) xtest == (int) xprev && (int) ytest == (int) yprev && (int) ztest == (int) zprev))
            {
                int x0 = (xtest > 0 ? (int) xtest : (int) xtest - 1), y0 = (ytest > 0 ? (int) ytest : (int) ytest - 1),
                        z0 = (ztest > 0 ? (int) ztest : (int) ztest - 1);
                List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class,
                        new AxisAlignedBB(x0 - 0.5, y0 - 0.5, z0 - 0.5, x0 + 0.5, y0 + 0.5, z0 + 0.5));
                if (targets != null && targets.size() > 0)
                {
                    List<Entity> ret = new ArrayList<Entity>();
                    for (Entity e : targets)
                    {
                        if (e instanceof EntityLivingBase && !excluded.contains(e))
                        {
                            ret.add(e);
                        }
                    }
                    if (ret.size() > 0) return ret.get(0);
                }
            }
            yprev = ytest;
            xprev = xtest;
            zprev = ztest;
        }

        return null;
    }

    public List<Entity> firstEntityLocationExcluding(int range, double size, Vector3 direction, Vector3 source,
            World world, Entity excluded)
    {
        direction = direction.normalize();
        double dx, dy, dz;

        for (double i = 0; i < range; i += 0.0625)
        {
            dx = i * direction.x;
            dy = i * direction.y;
            dz = i * direction.z;

            double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

            boolean check = isPointClearBlocks(xtest, ytest, ztest, world);

            if (!check)
            {
                break;
            }

            double x0 = (xtest > 0 ? (int) xtest : (int) xtest - 1), y0 = (ytest > 0 ? (int) ytest : (int) ytest - 1),
                    z0 = (ztest > 0 ? (int) ztest : (int) ztest - 1);
            List<Entity> targets = world.getEntitiesWithinAABBExcludingEntity(excluded,
                    new AxisAlignedBB(x0 - size, y0 - size, z0 - size, x0 + size, y0 + size, z0 + size));
            if (targets != null && targets.size() > 0)
            {
                List<Entity> ret = new ArrayList<Entity>();
                for (Entity e : targets)
                {
                    if (e instanceof EntityLiving)
                    {
                        ret.add(e);
                    }
                }
                if (ret.size() > 0) return ret;
            }

        }

        return null;
    }

    public double get(int i)
    {
        assert (i < 3);
        return i == 0 ? x : i == 1 ? y : z;
    }

    public AxisAlignedBB getAABB()
    {
        return Matrix3.getAABB(x, y, z, x, y, z);
    }

    public Biome getBiome(Chunk chunk, BiomeProvider mngr)
    {
        return chunk.getBiome(new BlockPos(intX() & 15, 0, intZ() & 15), mngr);// .getBiomeGenForWorldCoords();
    }

    public Biome getBiome(World world)
    {
        return world.getBiome(new BlockPos(intX(), 0, intZ()));
    }

    public int getBiomeID(World world)
    {
        return Biome.getIdForBiome(getBiome(world));
    }

    public Block getBlock(IBlockAccess worldMap)
    {
        IBlockState state = worldMap.getBlockState((getPos()));
        if (state == null) return Blocks.AIR;
        return state.getBlock();
    }

    public Block getBlock(IBlockAccess world, EnumFacing side)
    {
        Vector3 other = offset(side);
        Block ret = other.getBlock(world);
        return ret;
    }

    public int getBlockId(IBlockAccess world)
    {
        return Block.getIdFromBlock(world.getBlockState((getPos())).getBlock());
    }

    public Material getBlockMaterial(IBlockAccess world)
    {
        IBlockState state = world.getBlockState((getPos()));
        if (state == null || state.getBlock() == null) { return Material.AIR; }
        return state.getMaterial();
    }

    public int getBlockMetadata(IBlockAccess world)
    {
        IBlockState state = world.getBlockState((getPos()));
        Block b = state.getBlock();
        return b.getMetaFromState(state);
    }

    public IBlockState getBlockState(IBlockAccess world)
    {
        return world.getBlockState(getPos());
    }

    @SuppressWarnings("deprecation")
    public float getExplosionResistance(Explosion boom, IBlockAccess world)
    {
        IBlockState state = getBlockState(world);
        if (state == null) return 0;
        Block block = state.getBlock();
        if (block != null && !block.isAir(state, world, getPos())) { return world instanceof World
                ? block.getExplosionResistance((World) world, pos, boom.getExplosivePlacedBy(), boom)
                : block.getExplosionResistance(boom.getExplosivePlacedBy()); }
        return 0;

    }

    public int getLightValue(World world)
    {
        return world.getLight(getPos());
    }

    public int getMaxY(World world)
    {
        return getMaxY(world, intX(), intZ());
    }

    public int getMaxY(World world, int x, int z)
    {
        Vector3 temp = Vector3.getNewVector().set(x, y, z);
        Chunk chunk = world.getChunkFromBlockCoords(getPos());
        int y;
        for (y = chunk.getHeight(getPos()); y > 5; y--)
        {
            IBlockState state = world.getBlockState(new BlockPos(intX(), y, intZ()));
            if (state == null) continue;
            if (state.getMaterial().isSolid())
            {
                break;
            }
        }
        if (Int(y) == intY()) return y;
        while (isNotSurfaceBlock(world, temp))
        {
            y--;
            temp.y = y;
        }
        return y;
    }

    public int[] getMinMaxY(World world, int range)
    {
        int[] ret = new int[2];

        int minY = 255;
        int maxY = 0;
        for (int i = 0; i < range; i++)
            for (int j = 0; j < range; j++)
            {
                if (getMaxY(world, intX() + i, intZ() + j) < minY) minY = getMaxY(world, intX() + i, intZ() + j);
                if (getMaxY(world, intX() + i, intZ() + j) > maxY) maxY = getMaxY(world, intX() + i, intZ() + j);
            }

        ret[0] = minY;
        ret[1] = maxY;
        return ret;
    }

    public BlockPos getPos()
    {
        if (pos == null)
        {
            pos = new MutableBlockPos(intX(), intY(), intZ());
        }
        else
        {
            pos.setTo(this);
        }
        return pos;
    }

    public TileEntity getTileEntity(IBlockAccess world)
    {
        return world.getTileEntity(getPos());
    }

    public TileEntity getTileEntity(IBlockAccess world, EnumFacing side)
    {
        Vector3 other = offset(side);
        TileEntity ret = other.getTileEntity(world);
        return ret;
    }

    public Vector3 getTopBlockPos(World world)
    {
        int y = getTopBlockY(world);
        return Vector3.getNewVector().set(intX(), y, intZ());
    }

    public int getTopBlockY(IBlockAccess world)
    {
        int ret = 255;
        {
            for (ret = 255; ret > 1; ret--)
            {
                IBlockState state = world.getBlockState(new BlockPos(intX(), ret, intZ()));
                if (state == null) continue;
                if (state.getMaterial().isSolid()) { return ret; }
            }
        }
        return ret;
    }

    public Vector3 horizonalPerp()
    {
        Vector3 vectorH = getNewVector().set(-z, 0, x);
        return vectorH.norm();
    }

    // */
    public boolean inAABB(AxisAlignedBB aabb)
    {
        if (y >= aabb.maxY || y <= aabb.minY) return false;
        if (z >= aabb.maxZ || z <= aabb.minZ) return false;
        if (x >= aabb.maxX || x <= aabb.minX) return false;

        return true;
    }

    public boolean inMatBox(Matrix3 box)
    {
        Vector3 min = box.get(0);
        Vector3 max = box.get(1);
        boolean ycheck = false, xcheck = false, zcheck = false;

        if (y <= max.y && y >= min.y) ycheck = true;
        if (z <= max.z && z >= min.z) zcheck = true;
        if (x <= max.x && x >= min.x) xcheck = true;

        return ycheck && zcheck && xcheck;
    }

    public int intX()
    {
        return MathHelper.floor(x);
    }

    public int intY()
    {
        return MathHelper.floor(y);
    }

    public int intZ()
    {
        return MathHelper.floor(z);
    }

    public boolean isAir(IBlockAccess world)
    {
        Material m;
        if (world instanceof World)
        {
            IBlockState state = world.getBlockState((getPos()));
            return state.getBlock() == null || (m = getBlockMaterial(world)) == null || m == Material.AIR
                    || state.getBlock().isAir(state, world, getPos());// ||world.isAirBlock(intX(),
            // intY(),
            // intZ())
        }
        return (m = getBlockMaterial(world)) == null || m == Material.AIR;// ||world.isAirBlock(intX(),
                                                                          // intY(),
                                                                          // intZ())
    }

    public boolean isClearOfBlocks(IBlockAccess world)
    {
        boolean ret = false;
        IBlockState state = world.getBlockState(getPos());
        if (state == null) return true;

        ret = isAir(world);
        if (!ret) ret = ret || getBlockMaterial(world).isLiquid();
        if (!ret) ret = ret || getBlockMaterial(world).isReplaceable();
        if (!ret) ret = ret || !getBlockMaterial(world).blocksMovement();
        if (!ret)
        {
            Block block = state.getBlock();
            if (state.isNormalCube()) return false;
            if (block == null || block == Blocks.AIR || !block.isCollidable()) return true;
            List<AxisAlignedBB> aabbs = new ArrayList<AxisAlignedBB>();
            if (world instanceof World)
                state.addCollisionBoxToList((World) world, pos, getAABB().grow(0.03, 0.03, 0.03), aabbs, null, false);
            if (aabbs.size() == 0) return true;
            for (AxisAlignedBB aabb : aabbs)
            {
                if (aabb != null)
                {
                    if (y <= aabb.maxY && y >= aabb.minY) return false;
                    if (z <= aabb.maxZ && z >= aabb.minZ) return false;
                    if (x <= aabb.maxX && x >= aabb.minX) return false;
                }
            }
            return true;
        }
        return ret;
    }

    public boolean isEmpty()
    {
        return x == 0 && z == 0 && y == 0;
    }

    public boolean isEntityClearOfBlocks(IBlockAccess world, Entity e)
    {
        boolean ret = false;

        Vector3 v = Vector3.getNewVector();
        Vector3 v1 = Vector3.getNewVector();
        v.set(this);
        ret = v.addTo(v1.set(0, e.height, 0)).isClearOfBlocks(world);
        if (!ret) return ret;

        for (int i = -1; i <= 1; i++)
            for (int j = -1; j <= 1; j++)
                ret = ret && v.set(this).addTo(v1.set(i * e.width / 2, 0, j * e.width / 2)).isClearOfBlocks(world);
        if (!ret) return ret;

        for (int i = -1; i <= 1; i++)
            for (int j = -1; j <= 1; j++)
                ret = ret
                        && v.set(this).addTo(v1.set(i * e.width / 2, e.height, j * e.width / 2)).isClearOfBlocks(world);

        return ret;
    }

    /** TODO fix this once forge fluids work again Whether or not a certain
     * block is considered a fluid.
     * 
     * @param world
     *            - world the block is in
     * @return if the block is a liquid */
    public boolean isFluid(World world)
    {
        return FluidRegistry.lookupFluidForBlock(getBlock(world)) != null;
    }

    public boolean isNaN()
    {
        return Double.isNaN(x) || Double.isNaN(z) || Double.isNaN(y);
    }

    public boolean isOnSurface(Chunk chunk)
    {
        return chunk.getHeightValue(intX() & 15, intZ() & 15) <= y;
    }

    public boolean isOnSurface(World world)
    {
        return getMaxY(world) <= y;
    }

    public boolean isOnSurfaceIgnoringDecorationAndWater(Chunk chunk, IBlockAccess world)
    {
        int h = chunk.getHeightValue(intX() & 15, intZ() & 15);
        if (h <= y) return true;
        for (int i = h; i > y; i--)
        {
            Material m = world.getBlockState(new BlockPos(intX(), i, intZ())).getMaterial();
            if (!(m == Material.WATER || m == Material.AIR || m == Material.LEAVES || m == Material.WOOD)) return false;
        }
        return true;
    }

    public boolean isPointClearOfEntity(double x, double y, double z, Entity e)
    {
        AxisAlignedBB aabb = e.getEntityBoundingBox();

        if (y <= aabb.maxY && y >= aabb.minY) return false;
        if (z <= aabb.maxZ && z >= aabb.minZ) return false;
        if (x <= aabb.maxX && x >= aabb.minX) return false;

        return true;
    }

    public boolean isSideSolid(IBlockAccess world, EnumFacing side)
    {
        boolean ret = world.isSideSolid(getPos(), side, false);
        return ret;
    }

    public boolean isVisible(IBlockAccess world, Vector3 location)
    {
        Vector3 direction = location.subtract(this);
        double range = direction.mag();
        return isVisibleRange(world, this, direction, range);
    }

    public List<Entity> livingEntityAtPoint(World world)
    {
        int x0 = intX(), y0 = intY(), z0 = intZ();
        List<Entity> ret = new ArrayList<Entity>();
        List<EntityLiving> targets = world.getEntitiesWithinAABB(EntityLiving.class,
                new AxisAlignedBB(x0, y0, z0, x0 + 1, y0 + 1, z0 + 1));
        for (Entity e : targets)
        {
            if (!isPointClearOfEntity(x, y, z, e))
            {
                ret.add(e);
            }
        }
        return ret;
    }

    public List<Entity> livingEntityAtPointExcludingEntity(World world, Entity entity)
    {
        int x0 = intX(), y0 = intY(), z0 = intZ();
        List<Entity> ret = new ArrayList<Entity>();
        List<EntityLiving> targets = world.getEntitiesWithinAABB(EntityLiving.class,
                new AxisAlignedBB(x0, y0, z0, x0 + 1, y0 + 1, z0 + 1));
        for (Entity e : targets)
        {
            if (!isPointClearOfEntity(x, y, z, e) && e != entity)
            {
                ret.add(e);
            }
        }
        return ret;
    }

    /** Returns the magnitude of vector
     * 
     * @param vector
     * @return */
    public double mag()
    {
        double vmag = Math.sqrt(magSq());
        return vmag;
    }

    /** Returns the magnitude of vector squared
     * 
     * @param vector
     * @return */
    public double magSq()
    {
        double vmag = 0;
        for (int i = 0; i < Vector3.length; i = i + 1)
        {
            vmag = vmag + this.get(i) * this.get(i);
        }
        return vmag;
    }

    /** Left multiplies the Matrix by the Vector
     * 
     * @param Matrix
     * @param vector
     * @return */
    public Vector3 matrixMult(Matrix3 Matrix)
    {
        Vector3 newVect = vecMult.clear();
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < length; j++)
            {
                newVect.add(i, Matrix.get(i).get(j) * get(j));
            }
        }
        return newVect;
    }

    public void moveEntity(Entity e)
    {
        e.setPosition(x, y, z);
    }

    /** Normalizes this vector.
     * 
     * @param vector
     * @return unit vector in direction of vector. */
    public Vector3 norm()
    {
        double vmag = mag();
        if (vmag == 0) return clear();
        this.scalarMultBy(1 / vmag);
        return this;
    }

    /** Returns the unit vector in with the same direction as vector.
     * 
     * @param vector
     * @return unit vector in direction of vector. */
    public Vector3 normalize()
    {
        double vmag = mag();
        Vector3 vHat = getNewVector();
        if (vmag == 0) return vHat.clear();
        vHat.set(this).scalarMultBy(1 / vmag);
        return vHat;
    }

    public Vector3 offset(EnumFacing side)
    {
        return add(Vector3.getNewVector().set(side));
    }

    public Vector3 offsetBy(EnumFacing side)
    {
        return addTo(side.getFrontOffsetX(), side.getFrontOffsetY(), side.getFrontOffsetZ());
    }

    public Vector3 reverse()
    {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    /** Rotates the given vector by the given amounts of pitch and yaw.
     * 
     * @param vector
     * @param pitch
     * @param yaw
     * @return */
    public Vector3 rotateAboutAngles(double pitch, double yaw, Vector3 temp, Vector3 temp1)
    {
        if (this.isEmpty() || pitch == 0 && yaw == 0) { return this; }
        temp.set(this);
        if (yaw != 0) rotateAboutLine(secondAxis, yaw, temp);
        if (pitch != 0)
        {
            rotateAboutLine(horizonalPerp(), pitch, temp);
        }

        if (temp.isNaN()) { return temp.clear(); }

        return temp;
    }

    /** Rotates the given vector around the given line by the given angle. This
     * internally normalizes the line incase it is not already normalized
     * 
     * @param vectorH
     * @param line
     * @param angle
     * @return */
    public Vector3 rotateAboutLine(Vector3 line, double angle, Vector3 ret)
    {
        if (line.magSq() != 1) line = line.normalize();

        if (ret == null) ret = Vector3.getNewVector();
        double[][] mat = rotBox;

        mat[0][0] = line.get(0) * line.get(0) * (1 - MathHelper.cos((float) angle)) + MathHelper.cos((float) angle);
        mat[0][1] = line.get(0) * line.get(1) * (1 - MathHelper.cos((float) angle))
                - line.get(2) * MathHelper.sin((float) angle);
        mat[0][2] = line.get(0) * line.get(2) * (1 - MathHelper.cos((float) angle))
                + line.get(1) * MathHelper.sin((float) angle);

        mat[1][0] = line.get(1) * line.get(0) * (1 - MathHelper.cos((float) angle))
                + line.get(2) * MathHelper.sin((float) angle);
        mat[1][1] = line.get(1) * line.get(1) * (1 - MathHelper.cos((float) angle)) + MathHelper.cos((float) angle);
        mat[1][2] = line.get(1) * line.get(2) * (1 - MathHelper.cos((float) angle))
                - line.get(0) * MathHelper.sin((float) angle);

        mat[2][0] = line.get(2) * line.get(0) * (1 - MathHelper.cos((float) angle))
                - line.get(1) * MathHelper.sin((float) angle);
        mat[2][1] = line.get(2) * line.get(1) * (1 - MathHelper.cos((float) angle))
                + line.get(0) * MathHelper.sin((float) angle);
        mat[2][2] = line.get(2) * line.get(2) * (1 - MathHelper.cos((float) angle)) + MathHelper.cos((float) angle);

        ret.x = mat[0][0] * x + mat[0][1] * y + mat[0][2] * z;
        ret.y = mat[1][0] * x + mat[1][1] * y + mat[1][2] * z;
        ret.z = mat[2][0] * x + mat[2][1] * y + mat[2][2] * z;

        return ret;
    }

    public boolean sameBlock(Vector3 vec)
    {
        return this.intX() == vec.intX() && this.intY() == vec.intY() && this.intZ() == vec.intZ();
    }

    /** Multiplies the vector by the constant.
     * 
     * @param vector
     * @param constant
     * @return */
    public Vector3 scalarMult(double constant)
    {
        Vector3 newVector = Vector3.getNewVector();
        for (int i = 0; i < Vector3.length; i++)
        {
            newVector.set(i, constant * this.get(i));
        }
        return newVector;
    }

    public Vector3 scalarMultBy(double i)
    {
        x = x * i;
        y = y * i;
        z = z * i;
        return this;
    }

    public Vector3 set(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3 set(double[] vec)
    {
        this.x = vec[0];
        this.y = vec[1];
        this.z = vec[2];
        return this;
    }

    public Vector3 set(Entity e, boolean b)
    {
        if (e != null && b)
        {
            this.x = e.posX;
            this.y = e.posY + e.height / 2;
            this.z = e.posZ;
        }
        else if (e != null)
        {
            this.x = e.posX;
            this.y = e.posY + e.getEyeHeight();
            this.z = e.posZ;
        }
        return this;
    }

    public Vector3 set(EnumFacing dir)
    {
        this.x = dir.getFrontOffsetX();
        this.y = dir.getFrontOffsetY();
        this.z = dir.getFrontOffsetZ();
        return this;
    }

    public void set(int i, double j)
    {
        if (i == 0)
        {
            x = j;
        }
        else if (i == 1)
        {
            y = j;
        }
        else if (i == 2)
        {
            z = j;
        }
    }

    public Vector3 set(Object o)
    {
        if (o instanceof Entity)
        {
            Entity e = (Entity) o;
            set(e.posX, e.posY, e.posZ);
        }
        else if (o instanceof TileEntity)
        {
            TileEntity te = (TileEntity) o;
            set(te.getPos());
        }
        else if (o instanceof double[])
        {
            double[] d = (double[]) o;
            this.set(d[0], d[1], d[2]);
        }
        else if (o instanceof EnumFacing)
        {
            EnumFacing side = (EnumFacing) o;
            this.set(side.getFrontOffsetX(), side.getFrontOffsetY(), side.getFrontOffsetZ());
        }
        else if (o instanceof Vector3)
        {
            this.set((Vector3) o);
        }
        else if (o instanceof BlockPos)
        {
            BlockPos c = (BlockPos) o;
            this.set(c.getX(), c.getY(), c.getZ());
        }
        else if (o instanceof ICommandSender)
        {
            ICommandSender c = (ICommandSender) o;
            this.set(c.getPosition());
        }
        else if (o instanceof PathPoint)
        {
            PathPoint p = (PathPoint) o;
            this.set(p.x, p.y, p.z);
        }
        else if (o instanceof Vec3d)
        {
            Vec3d p = (Vec3d) o;
            this.set(p.x, p.y, p.z);
        }
        else if (o instanceof int[])
        {
            int[] p = (int[]) o;
            this.set(p[0], p[1], p[2]);
        }
        else if (o instanceof Double)
        {
            x = y = z = (double) o;
        }
        return this;
    }

    public Vector3 set(Vector3 vec)
    {
        if (vec != null)
        {
            this.x = vec.x;
            this.y = vec.y;
            this.z = vec.z;
        }
        else
        {

        }
        return this;
    }

    public void setAir(World world)
    {
        world.setBlockToAir(getPos());
    }

    public void setBiome(Biome biome, World world)
    {
        int x = intX();
        int z = intZ();

        Chunk chunk = world.getChunkFromBlockCoords(new BlockPos(x, 0, z));
        byte[] biomes = chunk.getBiomeArray();
        byte newBiome = (byte) Biome.getIdForBiome(biome);

        int chunkX = Math.abs(x & 15);
        int chunkZ = Math.abs(z & 15);

        int point = chunkX + 16 * chunkZ;

        if (biomes[point] != newBiome)
        {
            biomes[point] = newBiome;
            chunk.setBiomeArray(biomes);
            chunk.markDirty();
        }
    }

    public boolean setBlock(World world, Block id)
    {
        return setBlock(world, id, 0, 3);
    }

    // */
    public boolean setBlock(World world, Block id, int meta)
    {
        return setBlock(world, id, meta, 3);
    }

    public boolean setBlock(World world, Block id, int meta, int flag)
    {
        if (doChunksExist(world, 1))
        {
            world.setBlockState(getPos(), CompatWrapper.getBlockStateFromMeta(id, meta), flag);
            return true;
        }
        return false;
    }

    public void setBlock(World world, IBlockState defaultState)
    {
        world.setBlockState(getPos(), defaultState);
    }

    public boolean setBlockId(World world, int id, int meta, int flag)
    {
        return setBlock(world, Block.getBlockById(id), meta, flag);
    }

    public Vector3 setToVelocity(Entity e)
    {
        set(e.motionX, e.motionY, e.motionZ);
        return this;
    }

    public void setVelocities(Entity e)
    {
        e.motionX = x;
        e.motionY = y;
        e.motionZ = z;
    }

    /** Subtracts vectorB from vectorA
     * 
     * @param vectorA
     * @param vectorB
     * @return */
    public Vector3 subtract(Vector3 vectorB)
    {
        Vector3 vectorC = Vector3.getNewVector();
        for (int i = 0; i < 3; i++)
        {
            vectorC.set(i, this.get(i) - vectorB.get(i));
        }
        return vectorC;
    }

    /** Subtracts vectorB from vectorA
     * 
     * @param vectorA
     * @param vectorB
     * @return */
    public Vector3 subtractFrom(Vector3 b)
    {
        if (b == null) return this;
        x -= b.x;
        y -= b.y;
        z -= b.z;
        return this;
    }

    /** Returns the unit vector in with the same direction as vector.
     * 
     * @param vector
     * @return unit vector in direction of vector. */
    public Vector3 toSpherical()
    {
        Vector3 vectorSpher = getNewVector();
        vectorSpher.x = mag();
        vectorSpher.y = acos(this.get(1) / vectorSpher.x) - PI / 2;
        vectorSpher.z = atan2(this.get(2), this.x);
        return vectorSpher;
    }

    @Override
    public String toString()
    {
        return "x:" + x + " y:" + y + " z:" + z;
    }

    public void writeToBuff(ByteBuf data)
    {
        data.writeDouble(x);
        data.writeDouble(y);
        data.writeDouble(z);
    }

    public void writeToNBT(NBTTagCompound nbt, String tag)
    {
        nbt.setDouble(tag + "x", x);
        nbt.setDouble(tag + "y", y);
        nbt.setDouble(tag + "z", z);
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }
}