package thut.api.maths;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.atan2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;

/** @author Thutmose */
public class Vector3
{
    public static final Vector3 secondAxis    = Vector3.getNewVector().set(0, 1, 0);
    public static final Vector3 secondAxisNeg = Vector3.getNewVector().set(0, -1, 0);
    public static final Vector3 firstAxis     = Vector3.getNewVector().set(1, 0, 0);
    public static final Vector3 firstAxisNeg  = Vector3.getNewVector().set(-1, 0, 0);
    public static final Vector3 thirdAxis     = Vector3.getNewVector().set(0, 0, 1);
    public static final Vector3 thirdAxisNeg  = Vector3.getNewVector().set(0, 0, -1);
    public static final Vector3 empty         = Vector3.getNewVector();

    public double               x;
    public double               y;
    public double               z;
    public static final int     length        = 3;

    private Vector3()
    {
        this.x = this.y = this.z = 0;
    }

    @Deprecated
    public static Vector3 getNewVectorFromPool()
    {
        return getNewVector();
    }

    public static Vector3 getNewVector()
    {
        return new Vector3();
    }

    @Deprecated
    public void freeVectorFromPool()
    {
    }

    private Vector3(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
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

    private Vector3(Vec3 vec)
    {
        this.x = vec.xCoord;
        this.y = vec.yCoord;
        this.z = vec.zCoord;
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

    private Vector3(Object a, Object b)
    {
        this();
        Vector3 A = Vector3.getNewVector().set(a);
        Vector3 B = Vector3.getNewVector().set(b);
        this.set(B.subtract(A));
    }

    //
    private Vector3(Object a)
    {
        this();
        set(a);
    }

    public void moveEntity(Entity e)
    {
        e.setPosition(x, y, z);
    }

    public boolean isNaN()
    {
        return Double.isNaN(x) || Double.isNaN(z) || Double.isNaN(y);
    }

    public boolean isEmpty()
    {
        return x == 0 && z == 0 && y == 0;
    }

    public List<Entity> livingEntityAtPoint(World worldObj)
    {
        int x0 = intX(), y0 = intY(), z0 = intZ();
        List<Entity> ret = new ArrayList<Entity>();
        List<EntityLiving> targets = worldObj.getEntitiesWithinAABB(EntityLiving.class,
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

    public List<Entity> livingEntityAtPointExcludingEntity(World worldObj, Entity entity)
    {
        int x0 = intX(), y0 = intY(), z0 = intZ();
        List<Entity> ret = new ArrayList<Entity>();
        List<EntityLiving> targets = worldObj.getEntitiesWithinAABB(EntityLiving.class,
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

    public void addVelocities(Entity e)
    {
        e.addVelocity(x, y, z);
    }

    public void setVelocities(Entity e)
    {
        e.motionX = x;
        e.motionY = y;
        e.motionZ = z;
    }

    public boolean setBlockId(World worldObj, int id, int meta, int flag)
    {
        return setBlock(worldObj, Block.getBlockById(id), meta, flag);
    }

    // */
    public boolean setBlock(World worldObj, Block id, int meta)
    {
        return setBlock(worldObj, id, meta, 3);
    }

    public boolean setBlock(World worldObj, Block id, int meta, int flag)
    {
        if (doChunksExist(worldObj, 1))
        {
            worldObj.setBlockState(getPos(), id.getStateFromMeta(meta), flag);
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean doChunksExist(World world, int distance)
    {
        return world.isAreaLoaded(getPos(), distance);
    }

    public boolean setBlock(World worldObj, Block id)
    {
        return setBlock(worldObj, id, 0, 3);
    }

    public void setAir(World worldObj)
    {
        worldObj.setBlockToAir(getPos());
    }

    // */
    public boolean inAABB(AxisAlignedBB aabb)
    {
        if (y >= aabb.maxY || y <= aabb.minY) return false;
        if (z >= aabb.maxZ || z <= aabb.minZ) return false;
        if (x >= aabb.maxX || x <= aabb.minX) return false;

        return true;
    }

    public Vector3 offset(EnumFacing side)
    {
        return add(Vector3.getNewVector().set(side));
    }

    public Vector3 offsetBy(EnumFacing side)
    {
        return addTo(side.getFrontOffsetX(), side.getFrontOffsetY(), side.getFrontOffsetZ());
    }

    public double distToEntity(Entity e)
    {
        return distanceTo(entity(e));
    }

    public double distanceTo(Vector3 vec)
    {
        return (this.subtract(vec)).mag();
    }

    @Override
    public boolean equals(Object vec)
    {
        if (!(vec instanceof Vector3)) return false;
        Vector3 v = (Vector3) vec;

        return v.x == x && v.y == y && v.z == z;// sameBlock(v);
    }

    public boolean sameBlock(Vector3 vec)
    {
        return this.intX() == vec.intX() && this.intY() == vec.intY() && this.intZ() == vec.intZ();
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

    public Vector3 set(EnumFacing dir)
    {
        this.x = dir.getFrontOffsetX();
        this.y = dir.getFrontOffsetY();
        this.z = dir.getFrontOffsetZ();
        return this;
    }

    public Vector3 set(double[] vec)
    {
        this.x = vec[0];
        this.y = vec[1];
        this.z = vec[2];
        return this;
    }

    public Vector3 set(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public double get(int i)
    {
        assert (i < 3);
        return i == 0 ? x : i == 1 ? y : z;
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

    public float getExplosionResistance(Explosion boom, IBlockAccess world)
    {
        Block block = getBlock(world);

        if (block != null
                && !block.isAir(world, getPos())) { return block.getExplosionResistance(boom.getExplosivePlacedBy()); }
        return 0;

    }

    public int intX()
    {
        return MathHelper.floor_double(x);
    }

    public int intY()
    {
        return MathHelper.floor_double(y);
    }

    public int intZ()
    {
        return MathHelper.floor_double(z);
    }

    public static Vector3 entity(Entity e)
    {
        if (e != null) return Vector3.getNewVector().set(e.posX, e.posY + e.height / 2, e.posZ);
        return null;
    }

    public static int Int(double x)
    {
        return MathHelper.floor_double(x);
    }

    @Override
    public String toString()
    {
        return "x:" + x + " y:" + y + " z:" + z;
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
    public Vector3 toSpherical()
    {
        Vector3 vectorSpher = getNewVector();
        vectorSpher.x = mag();
        vectorSpher.y = acos(this.get(1) / vectorSpher.x) - PI / 2;
        vectorSpher.z = atan2(this.get(2), this.x);
        return vectorSpher;
    }

    public Vector3 horizonalPerp()
    {
        Vector3 vectorH = getNewVector().set(x, 0, z);
        return vectorH.rotateAboutLine(secondAxis, PI / 2, vectorH).normalize();
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

    public static Vector3 vecMult = Vector3.getNewVector();

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

    public static double[][] rotBox = new double[3][3];

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
        double[][] mat = rotBox;// new double[3][3];
        // Matrix3 TransMatrix = rotBox.clear();

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
        double x = this.x, y = this.y, z = this.z;

        ret.x = mat[0][0] * x + mat[0][1] * y + mat[0][2] * z;
        ret.y = mat[1][0] * x + mat[1][1] * y + mat[1][2] * z;
        ret.z = mat[2][0] * x + mat[2][1] * y + mat[2][2] * z;

        return ret;
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

    public double distTo(Vector3 pointB)
    {
        return this.subtract(pointB).mag();
    }

    public double distToSq(Vector3 pointB)
    {
        return subtract(pointB).magSq();
    }

    public Vector3 copy()
    {
        Vector3 newVector = Vector3.getNewVector().set(x, y, z);
        return newVector;
    }

    public Vector3 setToVelocity(Entity e)
    {
        set(e.motionX, e.motionY, e.motionZ);
        return this;
    }

    public void writeToNBT(NBTTagCompound nbt, String tag)
    {
        nbt.setDouble(tag + "x", x);
        nbt.setDouble(tag + "y", y);
        nbt.setDouble(tag + "z", z);
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

    public void writeToBuff(ByteBuf data)
    {
        data.writeDouble(x);
        data.writeDouble(y);
        data.writeDouble(z);
    }

    public static Vector3 readFromBuff(ByteBuf dat)
    {
        Vector3 ret = Vector3.getNewVector();
        ret.x = dat.readDouble();
        ret.y = dat.readDouble();
        ret.z = dat.readDouble();
        return ret;
    }

    public Vector3 findNextSolidBlock(IBlockAccess worldObj, Vector3 direction, double range)
    {
        return findNextSolidBlock(worldObj, this, direction, range);
    }

    /** Locates the first solid block in the line indicated by the direction
     * vector, starting from the source if range is given as 0, it will check
     * out to 320 blocks.
     * 
     * @param worldObj
     * @param source
     * @param direction
     * @param range
     * @return */
    public static Vector3 findNextSolidBlock(IBlockAccess worldObj, Vector3 source, Vector3 direction, double range)
    {
        direction = direction.normalize();
        double xprev = source.x, yprev = source.y, zprev = source.z;
        double dx, dy, dz;

        for (double i = 0; i < range; i += 1)
        {
            dx = i * direction.x;
            dy = i * direction.y;
            dz = i * direction.z;

            double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

            if (ytest > 255) return null;

            if (!(Int(xtest) == Int(xprev) && Int(ytest) == Int(yprev) && Int(ztest) == Int(zprev)))
            {

                Vector3 test = Vector3.getNewVector().set(xtest, ytest, ztest);
                boolean clear = test.clearOfBlocks(worldObj);
                test.freeVectorFromPool();

                if (!clear) { return Vector3.getNewVector().set(Int(xtest), Int(ytest), Int(ztest)); }
            }

            yprev = ytest;
            xprev = xtest;
            zprev = ztest;
        }
        return null;
    }

    /** determines whether the source can see out as far as range in the given
     * direction.
     * 
     * @param worldObj
     * @param source
     * @param direction
     * @param range
     * @return */
    public static boolean isVisibleRange(IBlockAccess worldObj, Vector3 source, Vector3 direction, double range)
    {
        direction = direction.normalize();
        double dx, dy, dz;
        for (double i = 0; i < range; i += 0.0625)
        {
            dx = i * direction.x;
            dy = i * direction.y;
            dz = i * direction.z;

            double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

            boolean check = isPointClearBlocks(xtest, ytest, ztest, worldObj);
            if (!check) { return false; }
        }
        return true;
    }

    /** determines whether the source can see out as far as range in the given
     * direction.
     * 
     * @param worldObj
     * @param source
     * @param direction
     * @param range
     * @return */
    public static Vector3 getNextSurfacePoint(IBlockAccess worldObj, Vector3 source, Vector3 direction, double range)
    {
        direction = direction.normalize();

        double dx, dy, dz;

        for (double i = 0; i < range; i += 0.0625)
        {
            dx = i * direction.x;
            dy = i * direction.y;
            dz = i * direction.z;

            double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

            boolean check = isPointClearBlocks(xtest, ytest - dy, ztest - dz, worldObj);
            check = check && isPointClearBlocks(xtest - dx, ytest, ztest - dz, worldObj);
            check = check && isPointClearBlocks(xtest - dx, ytest - dy, ztest, worldObj);
            if (!check) { return Vector3.getNewVector().set(xtest, ytest, ztest); }
        }
        return null;
    }

    /** determines whether the source can see out as far as range in the given
     * direction. This version ignores blocks like leaves and wood, used for
     * finding the surface of the ground.
     * 
     * @param worldObj
     * @param source
     * @param direction
     * @param range
     * @return */
    public static Vector3 getNextSurfacePoint2(IBlockAccess worldObj, Vector3 source, Vector3 direction, double range)
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

            boolean check = isNotSurfaceBlock((World) worldObj, temp.set(xtest, ytest, ztest));// isPointClearBlocks(xtest,
                                                                                               // ytest,
                                                                                               // ztest,
                                                                                               // worldObj);
            if (!check) { return Vector3.getNewVector().set(xtest, ytest, ztest); }
        }
        return null;
    }

    public boolean isVisible(IBlockAccess world, Vector3 location)
    {
        Vector3 direction = location.subtract(this);
        double range = direction.mag();
        return isVisibleRange(world, this, direction, range);
    }

    public static boolean isVisibleEntityFromEntity(Entity looker, Entity target)
    {

        if (looker == null || target == null) return false;
        Vector3 look = entity(looker);
        Vector3 t = entity(target);
        return isVisibleRange(looker.worldObj, t, look, look.distanceTo(t));
    }

    public Entity firstEntityExcluding(double range, Vector3 direction, World worldObj, boolean effect, Entity excluded)
    {
        List<Entity> toExclude = new ArrayList<Entity>();
        if (excluded != null)
        {
            toExclude.add(excluded);
            if (excluded.ridingEntity != null)
            {
                toExclude.add(excluded.ridingEntity);
            }
            if (excluded.riddenByEntity != null)
            {
                toExclude.add(excluded.riddenByEntity);
            }
        }
        return firstEntityExcluding(range, direction, worldObj, effect, toExclude);
    }

    public Entity firstEntityExcluding(double range, Vector3 direction, World worldObj, boolean effect,
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

            boolean check = isPointClearBlocks(xtest, ytest, ztest, worldObj);

            if (!check)
            {
                break;
            }

            if (effect && worldObj.isRemote)
            {
                worldObj.spawnParticle(EnumParticleTypes.TOWN_AURA, xtest, ytest, ztest, 0, 0, 0);
            }

            if (!((int) xtest == (int) xprev && (int) ytest == (int) yprev && (int) ztest == (int) zprev))
            {
                int x0 = (xtest > 0 ? (int) xtest : (int) xtest - 1), y0 = (ytest > 0 ? (int) ytest : (int) ytest - 1),
                        z0 = (ztest > 0 ? (int) ztest : (int) ztest - 1);
                List<EntityLivingBase> targets = worldObj.getEntitiesWithinAABB(EntityLivingBase.class,
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
                    if (ret != null && ret.size() > 0) return ret.get(0);
                }
            }
            yprev = ytest;
            xprev = xtest;
            zprev = ztest;
        }

        return null;
    }

    public boolean isClearOfBlocks(IBlockAccess worldObj)
    {
        boolean ret = false;
        IBlockState state = worldObj.getBlockState(getPos());
        if (state == null) return true;

        ret = isAir(worldObj);
        if (!ret) ret = ret || getBlockMaterial(worldObj).isLiquid();
        if (!ret) ret = ret || getBlockMaterial(worldObj).isReplaceable();

        if (!ret)
        {
            ret = isPointClearBlocks(x, y, z, worldObj);
        }

        return ret;// isPointClearBlocks(x, y, z, worldObj);
    }

    public boolean isEntityClearOfBlocks(IBlockAccess worldObj, Entity e)
    {
        boolean ret = false;

        Vector3 v = Vector3.getNewVector();
        Vector3 v1 = Vector3.getNewVector();
        v.set(this);
        ret = v.addTo(v1.set(0, e.height, 0)).isClearOfBlocks(worldObj);
        if (!ret) return ret;

        for (int i = -1; i <= 1; i++)
            for (int j = -1; j <= 1; j++)
                ret = ret && v.set(this).addTo(v1.set(i * e.width / 2, 0, j * e.width / 2)).isClearOfBlocks(worldObj);
        if (!ret) return ret;

        for (int i = -1; i <= 1; i++)
            for (int j = -1; j <= 1; j++)
                ret = ret && v.set(this).addTo(v1.set(i * e.width / 2, e.height, j * e.width / 2))
                        .isClearOfBlocks(worldObj);

        return ret;
    }

    public static boolean isPointClearBlocks(double x, double y, double z, IBlockAccess worldObj)
    {
        int x0 = MathHelper.floor_double(x), y0 = MathHelper.floor_double(y), z0 = MathHelper.floor_double(z);
        BlockPos pos;
        IBlockState state = worldObj.getBlockState(pos = new BlockPos(x0, y0, z0));
        if (state == null) return true;
        Block block = state.getBlock();

        if (block.isNormalCube()) return false;

        if (block == null || block == Blocks.air || !block.isCollidable()) return true;

        List<AxisAlignedBB> aabbs = new ArrayList<AxisAlignedBB>();
        Vector3 v = getNewVector().set(x, y, z);

        if (worldObj instanceof World) block.addCollisionBoxesToList((World) worldObj, pos, state,
                v.getAABB().expand(0.03, 0.03, 0.03), aabbs, null);
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

    public boolean clearOfBlocks(IBlockAccess worldMap)
    {
        int x0 = intX(), y0 = intY(), z0 = intZ();

        Block block = worldMap.getBlockState(getPos()).getBlock();

        if (block.isNormalCube()) return false;

        if (block == null || block == Blocks.air || !block.getMaterial().blocksMovement() || !block.isCollidable())
            return true;

        List<AxisAlignedBB> aabbs = new ArrayList<AxisAlignedBB>();

        // if (worldMap instanceof World)
        // {
        // block.addCollisionBoxesToList((World) worldMap, x0, y0, z0,
        // new AxisAlignedBB(x, y, z, x, y, z), aabbs, null);
        // }
        // else
        // {
        aabbs.add(new AxisAlignedBB(x0 + block.getBlockBoundsMinX(), y0 + block.getBlockBoundsMinY(),
                z0 + block.getBlockBoundsMinZ(), x0 + block.getBlockBoundsMaxX(), y0 + block.getBlockBoundsMaxY(),
                z0 + block.getBlockBoundsMaxZ()));
        // }

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

    public boolean isPointClearOfEntity(double x, double y, double z, Entity e)
    {
        AxisAlignedBB aabb = e.getEntityBoundingBox();

        if (y <= aabb.maxY && y >= aabb.minY) return false;
        if (z <= aabb.maxZ && z >= aabb.minZ) return false;
        if (x <= aabb.maxX && x >= aabb.minX) return false;

        return true;
    }

    public static Map<String, Fluid> fluids;

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

    public Vector3 clear()
    {
        return this.set(0, 0, 0);
    }

    public IBlockState getBlockState(IBlockAccess world)
    {
        return world.getBlockState(getPos());
    }

    public Block getBlock(IBlockAccess worldMap)
    {
        IBlockState state = worldMap.getBlockState((getPos()));
        if (state == null) return Blocks.air;
        return state.getBlock();
    }

    public Block getBlock(IBlockAccess worldObj, EnumFacing side)
    {
        Vector3 other = offset(side);
        Block ret = other.getBlock(worldObj);
        other.freeVectorFromPool();
        return ret;
    }

    public int getBlockId(IBlockAccess worldObj)
    {
        return Block.getIdFromBlock(worldObj.getBlockState((getPos())).getBlock());
    }

    public int getBlockMetadata(IBlockAccess worldObj)
    {
        IBlockState state = worldObj.getBlockState((getPos()));
        Block b = state.getBlock();
        return b.getMetaFromState(state);
    }

    public Material getBlockMaterial(IBlockAccess worldObj)
    {
        IBlockState state = worldObj.getBlockState((getPos()));
        if (state == null || state.getBlock() == null)
        {
            // System.out.println(this);
            // new Exception().printStackTrace();
            return Material.air;
        }
        return state.getBlock().getMaterial();
    }

    public boolean isAir(IBlockAccess worldObj)
    {
        Material m;
        if (worldObj instanceof World) { return worldObj.getBlockState((getPos())).getBlock() == null
                || (m = getBlockMaterial(worldObj)) == null || m == Material.air
                || worldObj.getBlockState((getPos())).getBlock().isAir(worldObj, getPos());// ||worldObj.isAirBlock(intX(),
        // intY(),
        // intZ())
        }
        return (m = getBlockMaterial(worldObj)) == null || m == Material.air;// ||worldObj.isAirBlock(intX(),
                                                                             // intY(),
                                                                             // intZ())
    }

    public TileEntity getTileEntity(IBlockAccess worldObj)
    {
        return worldObj.getTileEntity(getPos());
    }

    public TileEntity getTileEntity(IBlockAccess worldObj, EnumFacing side)
    {
        Vector3 other = offset(side);
        TileEntity ret = other.getTileEntity(worldObj);
        other.freeVectorFromPool();
        return ret;
    }

    public void breakBlock(World worldObj, boolean drop)
    {
        if (getBlock(worldObj) != null)
        {
            worldObj.destroyBlock(getPos(), drop);
        }
    }

    public void breakBlock(World worldObj, int fortune, boolean drop)
    {
        if (getBlock(worldObj) != null)
        {
            worldObj.destroyBlock(getPos(), drop);
        }
    }

    public boolean isSideSolid(IBlockAccess worldObj, EnumFacing side)
    {
        boolean ret = worldObj.isSideSolid(getPos(), side, false);
        return ret;
    }

    public List<Entity> firstEntityLocationExcluding(int range, double size, Vector3 direction, Vector3 source,
            World worldObj, Entity excluded)
    {
        direction = direction.normalize();
        double dx, dy, dz;

        for (double i = 0; i < range; i += 0.0625)
        {
            dx = i * direction.x;
            dy = i * direction.y;
            dz = i * direction.z;

            double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

            boolean check = isPointClearBlocks(xtest, ytest, ztest, worldObj);

            if (!check)
            {
                break;
            }

            double x0 = (xtest > 0 ? (int) xtest : (int) xtest - 1), y0 = (ytest > 0 ? (int) ytest : (int) ytest - 1),
                    z0 = (ztest > 0 ? (int) ztest : (int) ztest - 1);
            List<Entity> targets = worldObj.getEntitiesWithinAABBExcludingEntity(excluded,
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
                if (ret != null && ret.size() > 0) return ret;
            }

        }

        return null;
    }

    public List<Entity> allEntityLocationExcluding(int range, double size, Vector3 direction, Vector3 source,
            World worldObj, Entity excluded)
    {
        direction = direction.normalize();

        double dx, dy, dz;
        List<Entity> ret = new ArrayList<Entity>();

        for (double i = 0; i < range; i += 0.0625)
        {
            dx = i * direction.x;
            dy = i * direction.y;
            dz = i * direction.z;

            double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

            boolean check = isPointClearBlocks(xtest, ytest, ztest, worldObj);

            if (!check)
            {
                break;
            }

            double x0 = xtest, y0 = ytest, z0 = ztest;
            List<Entity> targets = worldObj.getEntitiesWithinAABBExcludingEntity(excluded,
                    new AxisAlignedBB(x0 - size, y0 - size, z0 - size, x0 + size, y0 + size, z0 + size));
            if (targets != null && targets.size() > 0)
            {
                for (Entity e : targets)
                {
                    if (e instanceof EntityLivingBase && !ret.contains(e) && e != excluded.riddenByEntity)
                    {
                        ret.add(e);
                    }
                }
            }
        }
        return ret;
    }

    public AxisAlignedBB getAABB()
    {
        return Matrix3.getAABB(x, y, z, x, y, z);
    }

    public void setBiome(BiomeGenBase biome, World worldObj)
    {
        int x = intX();
        int z = intZ();

        Chunk chunk = worldObj.getChunkFromBlockCoords(new BlockPos(x, 0, z));
        byte[] biomes = chunk.getBiomeArray();

        byte newBiome = (byte) biome.biomeID;

        int chunkX = Math.abs(x & 15);
        int chunkZ = Math.abs(z & 15);

        int point = chunkX + 16 * chunkZ;

        if (biomes[point] != newBiome)
        {
            biomes[point] = newBiome;
            chunk.setBiomeArray(biomes);
            chunk.setChunkModified();
        }
    }

    public int getBiomeID(World worldObj)
    {
        return getBiome(worldObj).biomeID;
    }

    public BiomeGenBase getBiome(World worldObj)
    {
        return worldObj.getBiomeGenForCoords(new BlockPos(intX(), 0, intZ()));
    }

    public BiomeGenBase getBiome(Chunk chunk, WorldChunkManager mngr)
    {
        return chunk.getBiome(new BlockPos(intX() & 15, 0, intZ() & 15), mngr);// .getBiomeGenForWorldCoords();
    }

    public int getTopBlockY(IBlockAccess world)
    {
        int ret = 255;
        {
            for (ret = 255; ret > 5; ret--)
            {
                IBlockState state = world.getBlockState(new BlockPos(intX(), ret, intZ()));
                if (state == null) continue;
                if (state.getBlock().getMaterial().isSolid()) { return ret; }
            }
        }
        return ret;
    }

    public Vector3 getTopBlockPos(World world)
    {
        int y = getTopBlockY(world);
        return Vector3.getNewVector().set(intX(), y, intZ());
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

    public int getMaxY(World world)
    {
        return getMaxY(world, intX(), intZ());
    }

    public int getMaxY(World world, int x, int z)
    {
        Vector3 temp = Vector3.getNewVector().set(x, y, z);
        int y = temp.getTopBlockY(world);

        if (Int(y) == intY()) return y;

        while (isNotSurfaceBlock(world, temp))
        {
            y--;
            temp.y = y;
        }
        return y;
    }

    public boolean canSeeSky(IBlockAccess world)
    {
        return getTopBlockY(world) <= y;
    }

    public boolean isOnSurface(World world)
    {
        return getMaxY(world) <= y;
    }

    public boolean isOnSurface(Chunk chunk)
    {
        return chunk.getHeightValue(intX() & 15, intZ() & 15) <= y;
    }

    public boolean isOnSurfaceIgnoringWater(Chunk chunk, IBlockAccess world)
    {
        int h = chunk.getHeightValue(intX() & 15, intZ() & 15);
        if (h <= y) return true;
        for (int i = h; i > y; i--)
        {
            Material m = world.getBlockState(new BlockPos(intX(), i, intZ())).getBlock().getMaterial();
            if (!(m == Material.water || m == Material.air)) return false;
        }
        return true;
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
                    int i1 = MathHelper.floor_double(intX() / 16.0D);
                    int k1 = MathHelper.floor_double(intZ() / 16.0D);
                    int j1 = MathHelper.floor_double(intY() / 16.0D);

                    int i2 = MathHelper.floor_double((intX() + i) / 16.0D);
                    int k2 = MathHelper.floor_double((intZ() + k) / 16.0D);
                    int j2 = MathHelper.floor_double((intY() + j) / 16.0D);

                    if (!(i1 == i2 && k1 == k2 && j1 == j2)) continue;
                    v.set(this);
                    Vector3 test = v.addTo(i, j, k);
                    testBlock = chunk.getBlock(test.intX() & 15, test.intY(), test.intZ() & 15);
                    if (testBlock == block)
                    {
                        ret++;
                    }
                }

        return ret;
    }

    public static boolean isNotSurfaceBlock(World world, Vector3 v)
    {
        Block b = v.getBlock(world);

        if (b instanceof IFluidBlock)
        {
            Fluid f = ((IFluidBlock) b).getFluid();
            return f.getViscosity() < Integer.MAX_VALUE;
        }

        boolean ret = (b == null || v.getBlockMaterial(world).isReplaceable() || v.isClearOfBlocks(world)
                || !b.isNormalCube() || b.isLeaves(world, v.getPos()) || b.isWood(world, v.getPos())) && v.y > 1;

        return ret;
    }

    MutableBlockPos pos;

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

    public Vector3 add(double i, double j, double k)
    {
        return Vector3.getNewVector().set(x + i, j + y, k + z);
    }

    public Vector3 addTo(double i, double j, double k)
    {
        x += i;
        y += j;
        z += k;
        return this;
    }

    public int getLightValue(World world)
    {
        return world.getLight(getPos());
    }

    static Vector3 move1 = Vector3.getNewVector();
    static Vector3 move2 = Vector3.getNewVector();

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
                if (n < 3)
                {
                    v.offset(EnumFacing.UP);
                }
                else if (n < 6)
                {
                    if (step)
                    {
                        step = false;
                        v.set(v1);
                    }
                    v.offsetBy(EnumFacing.NORTH);
                }
                else if (n < 9)
                {
                    if (!step)
                    {
                        step = true;
                        v.set(v1);
                    }
                    v.offsetBy(EnumFacing.SOUTH);
                }
                else if (n < 12)
                {
                    if (step)
                    {
                        step = false;
                        v.set(v1);
                    }
                    v.offsetBy(EnumFacing.EAST);
                }
                else if (n < 15)
                {
                    if (!step)
                    {
                        step = true;
                        v.set(v1);
                    }
                    v.offsetBy(EnumFacing.WEST);
                }
                else if (n < 18)
                {
                    if (step)
                    {
                        step = false;
                        v.set(v1);
                    }
                    v.offsetBy(EnumFacing.DOWN);
                }
                n++;
                if (n > 24) break;
            }

            long end = System.nanoTime() - start;

            double time = (end / (1000000000D));
            if (time > 0.001) System.out.println("Took " + time + "s to check");

            if (v.clearOfBlocks(world))
            {
                return true;
            }
            else
            {
                // System.out.println("Cannot find Clear Location " + v);
                return false;
            }
        }
        return true;
    }

    public Vector3 scalarMultBy(double i)
    {
        x = x * i;
        y = y * i;
        z = z * i;
        return this;
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
            this.set(p.xCoord, p.yCoord, p.zCoord);
        }
        else if (o instanceof Vec3)
        {
            Vec3 p = (Vec3) o;
            this.set(p.xCoord, p.yCoord, p.zCoord);
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

    public Vector3 reverse()
    {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    public Vector3 findClosestVisibleObject(IBlockAccess world, boolean water, int sightDistance, Object matching)
    {
        int size = Math.min(sightDistance, 30);
        ArrayList<Object> list = new ArrayList<Object>();
        Block seekingBlock = null;
        Class<?> seekingClass = null;
        boolean isInterface = false;
        boolean blockList = false;
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
        double rMag;
        Vector3 r = Vector3.getNewVector(), rAbs = Vector3.getNewVector(), rHat = Vector3.getNewVector(),
                rTest = Vector3.getNewVector(), rTestPrev = Vector3.getNewVector(), rTestAbs = Vector3.getNewVector(),
                ret = Vector3.getNewVector();

        loop:
        for (int i = 0; i < size * size * size; i++)
        {

            Cruncher.indexToVals(i, r);
            rAbs.set(r).addTo(this);
            rHat.set(r.normalize());
            double rm;
            if ((rm = r.mag()) > size || rm > sightDistance) continue;

            if (rAbs.isAir(world) && !(r.isEmpty())) continue;

            rTest.clear();
            rTestPrev.clear();
            rMag = r.mag();
            float dj = 1;
            for (float j = 0F; j <= rMag; j += dj)
            {
                rTest = rHat.scalarMult(j);

                if (!(rTest.sameBlock(rTestPrev)))
                {
                    rTestAbs.set(rTest).addTo(this);
                    Block b = rTestAbs.getBlock(world);

                    if (isInterface)
                    {
                        list.clear();
                        for (Object o : b.getClass().getInterfaces())
                        {
                            list.add(o);
                        }
                    }

                    if (seekingBlock != null && b == seekingBlock)
                    {
                        ret.set(rTestAbs);
                        r.freeVectorFromPool();
                        rAbs.freeVectorFromPool();
                        rHat.freeVectorFromPool();
                        rTest.freeVectorFromPool();
                        rTestPrev.freeVectorFromPool();
                        rTestAbs.freeVectorFromPool();
                        return ret;
                    }
                    else if (!isInterface && seekingClass != null && b.getClass().isAssignableFrom(seekingClass))
                    {
                        ret.set(rTestAbs);
                        r.freeVectorFromPool();
                        rAbs.freeVectorFromPool();
                        rHat.freeVectorFromPool();
                        rTest.freeVectorFromPool();
                        rTestPrev.freeVectorFromPool();
                        rTestAbs.freeVectorFromPool();
                        return ret;
                    }
                    else if (seekingClass != null && list.contains(seekingClass))
                    {
                        ret.set(rTestAbs);
                        r.freeVectorFromPool();
                        rAbs.freeVectorFromPool();
                        rHat.freeVectorFromPool();
                        rTest.freeVectorFromPool();
                        rTestPrev.freeVectorFromPool();
                        rTestAbs.freeVectorFromPool();
                        return ret;
                    }
                    else if (blockList && list.contains(b))
                    {
                        ret.set(rTestAbs);
                        r.freeVectorFromPool();
                        rAbs.freeVectorFromPool();
                        rHat.freeVectorFromPool();
                        rTest.freeVectorFromPool();
                        rTestPrev.freeVectorFromPool();
                        rTestAbs.freeVectorFromPool();
                        return ret;
                    }
                    else if (!rTestAbs.isClearOfBlocks(world))
                    {
                        // blocked.set(index);
                        continue loop;
                    }
                    else if (!water && b.getMaterial() == Material.water)
                    {
                        // blocked.set(index);
                        continue loop;
                    }
                }
            }
        }

        r.freeVectorFromPool();
        rAbs.freeVectorFromPool();
        rHat.freeVectorFromPool();
        rTest.freeVectorFromPool();
        rTestPrev.freeVectorFromPool();
        rTestAbs.freeVectorFromPool();
        ret.freeVectorFromPool();
        return null;
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

    public void setBlock(World world, IBlockState defaultState)
    {
        world.setBlockState(getPos(), defaultState);
    }

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

        /** Get the X coordinate */
        public int getX()
        {
            return this.x;
        }

        /** Get the Y coordinate */
        public int getY()
        {
            return this.y;
        }

        /** Get the Z coordinate */
        public int getZ()
        {
            return this.z;
        }

        /** Calculate the cross product of this and the given Vector */
        public BlockPos crossProduct(Vec3i vec)
        {
            return super.crossProduct(vec);
        }

        void setTo(Vector3 vector)
        {
            x = vector.intX();
            y = vector.intY();
            z = vector.intZ();
        }

        MutableBlockPos(int p_i46025_1_, int p_i46025_2_, int p_i46025_3_, Object p_i46025_4_)
        {
            this(p_i46025_1_, p_i46025_2_, p_i46025_3_);
        }
    }
}