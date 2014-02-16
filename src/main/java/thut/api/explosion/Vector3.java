package thut.api.explosion;

import static java.lang.Math.*;

import java.awt.Color;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import thut.api.entity.IMultiBox;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidBlock;

/**
 * @author Thutmose
 * 
 */
public class Vector3 {
	public double x;
	public double y;
	public double z;
	public static final int length = 3;
	public static final int dataSize = 12;
	public boolean valid = true;

	public static double SQRT_3 = Math.sqrt(3);
	public static double SQRT_2 = Math.sqrt(2);

	public static boolean collisionDamage = true;

	public static final Vector3 secondAxis = new Vector3(0, 1, 0);
	public static final Vector3 firstAxis = new Vector3(1, 0, 0);
	public static final Vector3 thirdAxis = new Vector3(0, 0, 1);

	public static class IntVec3 extends Vector3 {
		public IntVec3() {
			x = y = z = 0;
		}

		public IntVec3(byte[] a) {
			x = a[0];
			y = a[1];
			z = a[2];
		}

		public IntVec3(Vector3 vec) {
			x = vec.x;
			y = vec.y;
			z = vec.z;
		}

		public boolean equals(Object vec) {
			if (!(vec instanceof Vector3))
				return false;
			Vector3 v = (Vector3) vec;
			return sameBlock(v);
		}
	}

	public Vector3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * This takes degrees then converts to radians, as it seems most people like
	 * to work with degrees.
	 * 
	 * @param pitch
	 * @param yaw
	 */
	public Vector3(double pitch, double yaw) {
		this.x = 1;
		this.y = Math.toRadians(pitch);
		this.z = Math.toRadians(yaw);
	}

	public Vector3() {
		this.x = this.y = this.z = 0;
	}

	public Vector3(Entity e) {
		if (e != null) {
			this.x = e.posX;
			this.y = e.posY;
			this.z = e.posZ;
		}
	}

	public Vector3(Vec3 vec) {
		this.x = vec.xCoord;
		this.y = vec.yCoord;
		this.z = vec.zCoord;
	}

	public Vec3 toVec3() {
		return Vec3.createVectorHelper(x, y, z);
	}

	public Vector3(Entity e, boolean bool) {
		if (e != null && bool) {
			this.x = e.posX;
			this.y = e.posY + e.height / 2;
			this.z = e.posZ;
		} else if (e != null) {
			this.x = e.posX;
			this.y = e.posY + e.yOffset;
			this.z = e.posZ;
		}
	}

	public Vector3(TileEntity e) {
		this(e.xCoord, e.yCoord, e.zCoord);
	}

	public Vector3(double[] a) {
		this(a[0], a[1], a[2]);
	}

	public Vector3(Object a, Object b) {
		this();
		Vector3 A = new Vector3(a);
		Vector3 B = new Vector3(b);
		this.set(B.subtract(A));
	}

	public Vector3(Object a) {
		this();
		if (a instanceof Entity) {
			this.set(new Vector3((Entity) a));
		} else if (a instanceof TileEntity) {
			this.set(new Vector3((TileEntity) a));
		} else if (a instanceof double[]) {
			this.set(new Vector3((double[]) a));
		} else if (a instanceof ForgeDirection) {
			ForgeDirection side = (ForgeDirection) a;
			this.set(new Vector3(side.offsetX, side.offsetY, side.offsetZ));
		}else if(a instanceof Vector3){
			this.set((Vector3)a);
		}
	}

	public List<Entity> livingEntityInBox(World worldObj) {
		int x0 = intX(), y0 = intY(), z0 = intZ();
		List<Entity> targets = worldObj.getEntitiesWithinAABB(
				EntityLiving.class, AxisAlignedBB.getBoundingBox(x0, y0, z0,
						x0 + 1, y0 + 1, z0 + 1));
		return targets;
	}

	public void moveEntity(Entity e) {
		e.setPosition(x, y, z);
	}

	public boolean isNaN() {
		return Double.isNaN(x) || Double.isNaN(z) || Double.isNaN(y);
	}

	public boolean isEmpty() {
		return x == 0 && z == 0 && y == 0;
	}

	public List<Entity> livingEntityAtPoint(World worldObj) {
		int x0 = intX(), y0 = intY(), z0 = intZ();
		List<Entity> ret = new ArrayList<Entity>();
		List<Entity> targets = worldObj.getEntitiesWithinAABB(
				EntityLiving.class, AxisAlignedBB.getBoundingBox(x0, y0, z0,
						x0 + 1, y0 + 1, z0 + 1));
		for (Entity e : targets) {
			if (!isPointClearOfEntity(x, y, z, e)) {
				ret.add(e);
			}
		}
		return ret;
	}

	public List<Entity> livingEntityAtPointExcludingEntity(World worldObj,
			Entity entity) {
		int x0 = intX(), y0 = intY(), z0 = intZ();
		List<Entity> ret = new ArrayList<Entity>();
		List<Entity> targets = worldObj.getEntitiesWithinAABB(
				EntityLiving.class, AxisAlignedBB.getBoundingBox(x0, y0, z0,
						x0 + 1, y0 + 1, z0 + 1));
		for (Entity e : targets) {
			if (!isPointClearOfEntity(x, y, z, e) && e != entity) {
				ret.add(e);
			}
		}
		return ret;
	}

	public static double convertYawRadians(float rotationYaw) {
		return (PI * ((rotationYaw % 360) - 90) / 180);
	}

	public static double convertYaw(float rotationYaw) {
		return ((rotationYaw % 360) - 90);
	}

	public float convertPitch(float rotationPitch) {
		return 0;
	}

	public void addVelocities(Entity e) {
		e.addVelocity(x, y, z);
	}

	// */
	public boolean setBlockId(World worldObj, int id, int meta) {
		return setBlockId(worldObj, id, meta, 3);
	}

	public boolean setBlockId(World worldObj, int id, int meta, int flag) {
		return setBlock(worldObj, Block.getBlockById(id), meta, flag);
	}

	public boolean setBlockId(World worldObj, int id) {
		return setBlockId(worldObj, id, 0, 3);
	}

	// */
	public boolean setBlock(World worldObj, Block id, int meta) {
		return setBlock(worldObj, id, meta, 3);
	}

	public boolean setBlock(World worldObj, Block id, int meta, int flag) {
		if (worldObj.blockExists(intX(), intY(), intZ())) {
			worldObj.setBlock(intX(), intY(), intZ(), id, meta, flag);
			return true;
		} else {
			return false;
		}
	}

	public boolean setBlock(World worldObj, Block id) {
		return setBlock(worldObj, id, 0, 3);
	}

	public void setAir(World worldObj) {
		setBlock(worldObj, Blocks.air);
	}

	// */
	public boolean aabbClear(AxisAlignedBB aabb) {
		if (y <= aabb.maxY && y >= aabb.minY)
			return false;
		if (z <= aabb.maxZ && z >= aabb.minZ)
			return false;
		if (x <= aabb.maxX && x >= aabb.minX)
			return false;

		return true;
	}

	public Vector3 offset(ForgeDirection side) {
		return add(new Vector3(side));
	}

	public boolean inMatBox(Matrix3 box) {
		Vector3 min = box.get(0);
		Vector3 max = box.get(1);
		boolean ycheck = false, xcheck = false, zcheck = false;

		if (y <= max.y && y >= min.y)
			ycheck = true;
		if (z <= max.z && z >= min.z)
			zcheck = true;
		if (x <= max.x && x >= min.x)
			xcheck = true;

		return ycheck && zcheck && xcheck;
	}

	public double perpendicularDistance(Vector3 vec) {
		Vector3 a = this.subtract(vec);
		Vector3 b = this.normalize().scalarMult(this.normalize().dot(a));

		return (a.subtract(b)).mag();

	}

	public List<Entity> anyEntity(World worldObj) {
		int x0 = intX(), y0 = intY(), z0 = intZ();
		List<Entity> targets = worldObj.getEntitiesWithinAABB(Entity.class,
				AxisAlignedBB
						.getBoundingBox(x0, y0, z0, x0 + 1, y0 + 1, z0 + 1));

		return targets;
	}

	public void setTileEntity(World worldObj, TileEntity te) {
		worldObj.setTileEntity(intX(), intY(), intZ(), te);
	}

	public double distToEntity(Entity e) {
		return vectorMag(vectorSubtract(this, entity(e)));
	}

	public double distanceTo(Vector3 vec) {
		return vectorMag(vectorSubtract(this, vec));
	}

	public boolean equals(Object vec) {
		if (!(vec instanceof Vector3))
			return false;
		Vector3 v = (Vector3) vec;
		
		return sameBlock(v);
	}

	public boolean sameBlock(Vector3 vec) {
		return this.intX() == vec.intX() && this.intY() == vec.intY()
				&& this.intZ() == vec.intZ();
	}

	public Vector3 set(Vector3 vec) {
		this.x = vec.x;
		this.y = vec.y;
		this.z = vec.z;

		return this;
	}

	public void set(double[] vec) {
		this.x = vec[0];
		this.y = vec[1];
		this.z = vec[2];
	}

	public Vector3 set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public double get(int i) {
		assert (i < 3);
		return i == 0 ? x : i == 1 ? y : z;
	}

	public void set(int i, double j) {
		if (i == 0) {
			x = j;
		} else if (i == 1) {
			y = j;
		} else if (i == 2) {
			z = j;
		}
	}

	public void add(int i, double j) {
		if (i == 0) {
			x += j;
		} else if (i == 1) {
			y += j;
		} else if (i == 2) {
			z += j;
		}
	}

	public float getExplosionResistance(World worldObj) {
		Block block = getBlock(worldObj);

		if (block != null && !block.isAir(worldObj, intX(), intY(), intZ())) {
			return block.getExplosionResistance((Entity) null, worldObj,
					(int) x, (int) y, (int) z, 0d, 0d, 0d);
		}
		return 0;

	}

	public void doExplosion(World worldObj, float strength, boolean real) {
		worldObj.createExplosion(null, x, y, z, strength, real);
	}

	public void playSoundEffect(World worldObj, float volume, float pitch,
			String sound) {
		worldObj.playSoundEffect(x, y, z, sound, volume, pitch);
	}

	public int intX() {
		return MathHelper.floor_double(x);
	}

	public int intY() {
		return MathHelper.floor_double(y);
	}

	public int intZ() {
		return MathHelper.floor_double(z);
	}

	public Vector3 intVec() {
		return new Vector3(intX(), intY(), intZ());
	}

	public static Vector3 ArrayTo(double[] a) {
		assert (a.length == 3);
		return new Vector3(a[0], a[1], a[2]);
	}

	public static Vector3 ArrayTo(Double[] a) {
		assert (a.length == 3);
		return new Vector3(a[0], a[1], a[2]);
	}

	public static Vector3 entity(Entity e) {
		if (e != null)
			return new Vector3(e.posX, e.posY+e.height/2, e.posZ);
		return null;
	}

	public double[] toArray() {
		return new double[] { x, y, z };
	}

	public Double[] toArrayD() {
		return new Double[] { x, y, z };
	}

	public static int Int(double x) {
		return MathHelper.floor_double(x);
	}

	public String toString() {
		return "x:" + x + " y:" + y + " z:" + z;
	}

	public String toString(boolean bool) {
		return "x:" + intX() + " y:" + intY() + " z:" + intZ();
	}

	public double HorizonalDist(Vector3 vec) {
		return Math.sqrt((x - vec.x) * (x - vec.x) + (z - vec.z) * (z - vec.z));
	}

	/**
	 * Returns the unit vector in with the same direction as vector.
	 * 
	 * @param vector
	 * @return unit vector in direction of vector.
	 */
	public static Vector3 vectorNormalize(Vector3 vector) {
		double vmag = vectorMag(vector);
		Vector3 vhat = vectorScalarMult(vector, 1 / vmag);
		return vhat;
	}

	/**
	 * Returns the unit vector in with the same direction as vector.
	 * 
	 * @param vector
	 * @return unit vector in direction of vector.
	 */
	public Vector3 normalize() {
		double vmag = vectorMag(this);
		if (vmag == 0)
			return new Vector3();
		Vector3 vhat = vectorScalarMult(this, 1 / vmag);
		return vhat;
	}

	/**
	 * Returns the unit vector in with the same direction as vector.
	 * 
	 * @param vector
	 * @return unit vector in direction of vector.
	 */
	public static Vector3 vectorToSpherical(Vector3 vector, boolean minecraft) {
		Vector3 vectorSpher = new Vector3();
		vectorSpher.x = vectorMag(vector);
		vectorSpher.y = acos(vector.get(minecraft ? 1 : 2) / vectorSpher.x)
				- PI / 2;
		vectorSpher.z = atan2(vector.get(minecraft ? 2 : 1), vector.x);

		return vectorSpher;
	}

	/**
	 * Returns the unit vector in with the same direction as vector.
	 * 
	 * @param vector
	 * @return unit vector in direction of vector.
	 */
	public Vector3 toSpherical() {
		Vector3 vectorSpher = new Vector3();
		vectorSpher.x = vectorMag(this);
		vectorSpher.y = acos(this.get(1) / vectorSpher.x) - PI / 2;
		vectorSpher.z = atan2(this.get(2), this.x);
		return vectorSpher;
	}

	/**
	 * Returns the unit vector in with the same direction as vector.
	 * 
	 * @param vector
	 * @return unit vector in direction of vector.
	 */
	public Vector3 toCartesian() {
		Vector3 vectorCart = new Vector3();
		vectorCart.x = x * cos(y) * cos(z);
		vectorCart.z = x * cos(y) * sin(z);
		vectorCart.y = x * sin(y);
		return vectorCart;
	}

	public Vector3 anglesTo(Vector3 target) {
		Vector3 ret = new Vector3();
		ret = (this.toSpherical()).subtract(target.toSpherical());
		return ret;
	}

	public static Vector3 horizonalPerp(Vector3 vector) {
		Vector3 vectorH = new Vector3(vector.x, 0, vector.z);
		return vectorRotateAboutLine(vectorH, secondAxis, PI / 2);
	}

	public Vector3 horizonalPerp() {
		Vector3 vectorH = new Vector3(x, 0, z);
		return vectorNormalize(vectorRotateAboutLine(vectorH, secondAxis,
				PI / 2));
	}

	/**
	 * Adds vectorA to vectorB
	 * 
	 * @param vectorA
	 * @param vectorB
	 * @return
	 */
	public static Vector3 vectorAdd(Vector3 vectorA, Vector3 vectorB) {
		Vector3 vectorC = new Vector3();
		for (int i = 0; i < vectorA.length; i++) {
			vectorC.set(i, vectorA.get(i) + vectorB.get(i));
		}
		return vectorC;
	}

	/**
	 * Adds vectorA to vectorB
	 * 
	 * @param vectorA
	 * @param vectorB
	 * @return
	 */
	public Vector3 add(Vector3 vectorB) {
		Vector3 vectorC = new Vector3();
		for (int i = 0; i < 3; i++) {
			vectorC.set(i, this.get(i) + vectorB.get(i));
		}
		return vectorC;
	}

	/**
	 * Subtracts vectorB from vectorA
	 * 
	 * @param vectorA
	 * @param vectorB
	 * @return
	 */
	public static Vector3 vectorSubtract(Vector3 vectorA, Vector3 vectorB) {
		Vector3 vectorC = new Vector3();
		for (int i = 0; i < vectorA.length; i++) {
			vectorC.set(i, vectorA.get(i) - vectorB.get(i));
		}
		return vectorC;
	}

	/**
	 * Subtracts vectorB from vectorA
	 * 
	 * @param vectorA
	 * @param vectorB
	 * @return
	 */
	public Vector3 subtract(Vector3 vectorB) {
		Vector3 vectorC = new Vector3();
		for (int i = 0; i < 3; i++) {
			vectorC.set(i, this.get(i) - vectorB.get(i));
		}
		return vectorC;
	}

	public static double moduloPi(double num) {
		double newnum = num;
		if (num > PI) {
			while (newnum > PI) {
				newnum -= PI;
			}
		} else if (num < -PI) {
			while (newnum < -PI) {
				newnum += PI;
			}
		}
		return newnum;
	}

	/**
	 * Returns the magnitude of vector
	 * 
	 * @param vector
	 * @return
	 */
	public static double vectorMag(Vector3 vector) {
		double vmag = 0;
		for (int i = 0; i < 3; i = i + 1) {
			vmag = vmag + vector.get(i) * vector.get(i);
		}
		vmag = Math.sqrt(vmag);
		return vmag;
	}

	/**
	 * Returns the magnitude of vector squared
	 * 
	 * @param vector
	 * @return
	 */
	public double vectorMagSq(Vector3 vector) {
		double vmag = 0;
		for (int i = 0; i < vector.length; i = i + 1) {
			vmag = vmag + vector.get(i) * vector.get(i);
		}
		return vmag;
	}

	/**
	 * Returns the magnitude of vector
	 * 
	 * @param vector
	 * @return
	 */
	public double mag() {
		double vmag = Math.sqrt(magSq());
		return vmag;
	}

	/**
	 * Returns the magnitude of vector squared
	 * 
	 * @param vector
	 * @return
	 */
	public double magSq() {
		double vmag = 0;
		for (int i = 0; i < this.length; i = i + 1) {
			vmag = vmag + this.get(i) * this.get(i);
		}
		return vmag;
	}

	/**
	 * Multiplies the vector by the constant.
	 * 
	 * @param vector
	 * @param constant
	 * @return
	 */
	public static Vector3 vectorScalarMult(Vector3 vector, double constant) {
		Vector3 newVector = new Vector3();
		for (int i = 0; i < vector.length; i = i + 1) {
			newVector.set(i, constant * vector.get(i));
		}
		return newVector;
	}

	/**
	 * Multiplies the vector by the constant.
	 * 
	 * @param vector
	 * @param constant
	 * @return
	 */
	public Vector3 scalarMult(double constant) {
		Vector3 newVector = new Vector3();
		for (int i = 0; i < this.length; i++) {
			newVector.set(i, constant * this.get(i));
		}
		return newVector;
	}

	/**
	 * Left multiplies the Matrix by the Vector
	 * 
	 * @param Matrix
	 * @param vector
	 * @return
	 */
	public static Vector3 vectorMatrixMult(Matrix3 Matrix, Vector3 vector) {
		Vector3 newVect = new Vector3();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < vector.length; j++) {
				newVect.add(i, Matrix.get(i).get(j) * vector.get(j));
			}
		}
		return newVect;
	}

	/**
	 * Reflects the vector off the corresponding plane. The plane vector is the
	 * coefficient a,b,c of ax + by + cz = 0
	 * 
	 * @param vector
	 * @param plane
	 * @return
	 */
	public Vector3 vectorReflect(Vector3 vector, Vector3 plane) {
		Vector3 ret;
		double a = plane.x, b = plane.y, c = plane.z;
		double vMag = vectorMag(vector);
		ret = vectorNormalize(vector);
		Matrix3 Tmatrix = new Matrix3(new double[] { 1 - 2 * a * a, -2 * a * b,
				-2 * a * c }, new double[] { -2 * a * b, 1 - 2 * b * b,
				-2 * b * c }, new double[] { -2 * c * a, -2 * c * b,
				1 - 2 * c * c });
		ret = vectorMatrixMult(Tmatrix, vector);
		return vectorScalarMult(ret, vMag);
	}

	public static Vector3 linearInterpolate(Vector3 A, Vector3 B, double t) {
		return vectorAdd(A, vectorScalarMult(vectorSubtract(B, A), t));
	}

	/**
	 * Rotates the given vector around the given line by the given angle. This
	 * internally normalizes the line incase it is not already normalized
	 * 
	 * @param vectorH
	 * @param line
	 * @param angle
	 * @return
	 */
	public static Vector3 vectorRotateAboutLine(Vector3 vector, Vector3 line,
			double angle) {
		line = line.normalize();
		Vector3 ret;

		Matrix3 TransMatrix = new Matrix3();

		TransMatrix.get(0).x = line.get(0) * line.get(0)
				* (1 - Math.cos(angle)) + Math.cos(angle);
		TransMatrix.get(0).y = line.get(0) * line.get(1)
				* (1 - Math.cos(angle)) - line.get(2) * Math.sin(angle);
		TransMatrix.get(0).z = line.get(0) * line.get(2)
				* (1 - Math.cos(angle)) + line.get(1) * Math.sin(angle);

		TransMatrix.get(1).x = line.get(1) * line.get(0)
				* (1 - Math.cos(angle)) + line.get(2) * Math.sin(angle);
		TransMatrix.get(1).y = line.get(1) * line.get(1)
				* (1 - Math.cos(angle)) + Math.cos(angle);
		TransMatrix.get(1).z = line.get(1) * line.get(2)
				* (1 - Math.cos(angle)) - line.get(0) * Math.sin(angle);

		TransMatrix.get(2).x = line.get(2) * line.get(0)
				* (1 - Math.cos(angle)) - line.get(1) * Math.sin(angle);
		TransMatrix.get(2).y = line.get(2) * line.get(1)
				* (1 - Math.cos(angle)) + line.get(0) * Math.sin(angle);
		TransMatrix.get(2).z = line.get(2) * line.get(2)
				* (1 - Math.cos(angle)) + Math.cos(angle);

		ret = vectorMatrixMult(TransMatrix, vector);

		return ret;
	}

	/**
	 * Rotates the given vector around the given line by the given angle. This
	 * internally normalizes the line incase it is not already normalized
	 * 
	 * @param vectorH
	 * @param line
	 * @param angle
	 * @return
	 */
	public Vector3 rotateAboutLine(Vector3 line, double angle) {
		line = vectorNormalize(line);
		Vector3 ret;
		Matrix3 TransMatrix = new Matrix3();

		TransMatrix.get(0).x = line.get(0) * line.get(0)
				* (1 - Math.cos(angle)) + Math.cos(angle);
		TransMatrix.get(0).y = line.get(0) * line.get(1)
				* (1 - Math.cos(angle)) - line.get(2) * Math.sin(angle);
		TransMatrix.get(0).z = line.get(0) * line.get(2)
				* (1 - Math.cos(angle)) + line.get(1) * Math.sin(angle);

		TransMatrix.get(1).x = line.get(1) * line.get(0)
				* (1 - Math.cos(angle)) + line.get(2) * Math.sin(angle);
		TransMatrix.get(1).y = line.get(1) * line.get(1)
				* (1 - Math.cos(angle)) + Math.cos(angle);
		TransMatrix.get(1).z = line.get(1) * line.get(2)
				* (1 - Math.cos(angle)) - line.get(0) * Math.sin(angle);

		TransMatrix.get(2).x = line.get(2) * line.get(0)
				* (1 - Math.cos(angle)) - line.get(1) * Math.sin(angle);
		TransMatrix.get(2).y = line.get(2) * line.get(1)
				* (1 - Math.cos(angle)) + line.get(0) * Math.sin(angle);
		TransMatrix.get(2).z = line.get(2) * line.get(2)
				* (1 - Math.cos(angle)) + Math.cos(angle);

		ret = vectorMatrixMult(TransMatrix, this);
		return ret;
	}

	/**
	 * Rotates the given vector by the given amounts of pitch and yaw.
	 * 
	 * @param vector
	 * @param pitch
	 * @param yaw
	 * @return
	 */
	public static Vector3 vectorRotateAboutAngles(Vector3 vector, double pitch,
			double yaw) {
		return vectorRotateAboutLine(
				vectorRotateAboutLine(vector, secondAxis, yaw),
				horizonalPerp(vector), pitch);
	}

	/**
	 * Rotates the given vector by the given amounts of pitch and yaw.
	 * 
	 * @param vector
	 * @param pitch
	 * @param yaw
	 * @return
	 */
	public Vector3 rotateAboutAngles(double pitch, double yaw) {
		if (this.isEmpty() || pitch == 0 && yaw == 0) {
			return this;
		}
		Vector3 ret = vectorRotateAboutLine(
				vectorRotateAboutLine(this, secondAxis, yaw),
				horizonalPerp(this), pitch);
		if (ret.isNaN()) {
			return this;
		}

		return ret;
	}

	public static void rotateAboutAngles(Vector3[] points, double pitch,
			double yaw) {
		for (Vector3 p : points) {
			p = p.rotateAboutAngles(pitch, yaw);
		}
	}

	public static void rotateAboutAngles(Vector3[] points, Vector3 angles) {
		for (Vector3 p : points) {
			p = p.rotateAboutAngles(angles.y, angles.z);
		}
	}

	/**
	 * Returns the dot (scalar) product of the two vectors
	 * 
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public static double vectorDot(Vector3 vector1, Vector3 vector2) {
		double dot = 0;
		for (int i = 0; i < vector1.length; i++) {
			dot += vector1.get(i) * vector2.get(i);
		}
		return dot;
	}

	/**
	 * Returns the dot (scalar) product of the two vectors
	 * 
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public double dot(Vector3 vector2) {
		double dot = 0;
		for (int i = 0; i < 3; i++) {
			dot += this.get(i) * vector2.get(i);
		}
		return dot;
	}

	/**
	 * Returns the angle between two vectors
	 * 
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public static double vectorAngle(Vector3 vector1, Vector3 vector2) {
		return Math.acos(vectorDot(vectorNormalize(vector1),
				vectorNormalize(vector2)));
	}

	/**
	 * Returns the angle between two vectors
	 * 
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public double angle(Vector3 vector2) {
		return Math.acos(vectorDot(vectorNormalize(this),
				vectorNormalize(vector2)));
	}

	/**
	 * Swaps the ith and jth element of the vector useful for converting from
	 * x,y,z to x,z,y
	 * 
	 * @param vector
	 * @param i
	 * @param j
	 * @return
	 */
	public static Vector3 vectorSwap(Vector3 vector, int i, int j) {
		Vector3 ret = new Vector3();
		ret.set(i, vector.get(j));
		ret.set(j, vector.get(i));
		return vector;
	}

	/**
	 * Swaps the ith and jth element of the vector useful for converting from
	 * x,y,z to x,z,y
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	public void swap(int i, int j) {
		this.set(i, this.get(j));
		this.set(j, this.get(i));
	}

	/**
	 * Returns the cross (vector) product of the two vectors.
	 * 
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public static Vector3 vectorCross(Vector3 vector1, Vector3 vector2) {
		Vector3 vector3 = new Vector3();
		for (int i = 0; i < 3; i++) {
			vector3.set(i, vector1.get((i + 1) % 3) * vector2.get((i + 2) % 3)
					- vector1.get((i + 2) % 3) * vector2.get((i + 1) % 3));
		}
		return vector3;
	}

	/**
	 * Returns the cross (vector) product of the two vectors.
	 * 
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public Vector3 cross(Vector3 vector2) {
		Vector3 vector3 = new Vector3();
		for (int i = 0; i < 3; i++) {
			vector3.set(i, this.get((i + 1) % 3) * vector2.get((i + 2) % 3)
					- this.get((i + 2) % 3) * vector2.get((i + 1) % 3));
		}
		return vector3;
	}

	public static Vector3 findMidPointD(List<Double[]> points) {
		Vector3 mid = new Vector3();
		for (int j = 0; j < points.size(); j++) {
			mid = vectorAdd(Vector3.ArrayTo(points.get(j)), mid);
		}
		if (points.size() != 0) {
			mid = vectorScalarMult(mid, 1 / points.size());
		}
		return mid;
	}

	public static Vector3 findMidPoint(List<Vector3> points) {
		Vector3 mid = new Vector3();
		for (int j = 0; j < points.size(); j++) {
			mid = vectorAdd(points.get(j), mid);
		}
		if (points.size() != 0) {
			mid = vectorScalarMult(mid, 1 / points.size());
		}
		return mid;
	}

	public static double distBetween(Vector3 pointA, Vector3 pointB) {
		return vectorMag(vectorSubtract(pointA, pointB));
	}

	public double distTo(Vector3 pointB) {
		return vectorMag(vectorSubtract(this, pointB));
	}

	public double distToSq(Vector3 pointB) {
		return vectorMagSq(vectorSubtract(this, pointB));
	}

	public static double distToEntity(Vector3 pointA, Entity target) {
		if (target != null)
			return vectorMag(vectorSubtract(pointA, new Vector3(target.posX,
					target.posY, target.posZ)));
		return -1;
	}

	public Vector3 Copy() {
		Vector3 newVector = new Vector3(x, y, z);
		return newVector;
	}

	// public void moveEntity(Entity e)
	// {
	// e.posX=x;
	// e.posY=y;
	// e.posZ=z;
	// }

	public void writeToNBT(NBTTagCompound nbt, String tag) {
		nbt.setDouble(tag + "x", x);
		nbt.setDouble(tag + "y", y);
		nbt.setDouble(tag + "z", z);
	}

	public static Vector3 readFromNBT(NBTTagCompound nbt, String tag) {
		Vector3 ret = new Vector3();
		ret.x = nbt.getDouble(tag + "x");
		ret.y = nbt.getDouble(tag + "y");
		ret.z = nbt.getDouble(tag + "z");
		return ret;
	}

	public void writeToOutputStream(DataOutputStream dos) {
		try {
			dos.writeDouble(x);
			dos.writeDouble(y);
			dos.writeDouble(z);
		} catch (IOException e) {
			System.err.println("error in writing Vector3 to stream");
			e.printStackTrace();
		}
	}

	public void writeToOutputStream(ByteArrayDataOutput data) {
		data.writeDouble(x);
		data.writeDouble(y);
		data.writeDouble(z);
	}

	public static Vector3 readFromInputSteam(ByteArrayDataInput dat) {
		Vector3 ret = new Vector3();
		ret.x = dat.readDouble();
		ret.y = dat.readDouble();
		ret.z = dat.readDouble();
		return ret;
	}

	public Vector3 findNextSolidBlock(IBlockAccess worldObj, Vector3 direction,
			double range) {
		return findNextSolidBlock((World) worldObj, this, direction, range);
	}

	public int getTopBlockY(World world)
	{
		int ret = world.getHeightValue(intX(), intZ());
		return ret;
	}
	
	public Vector3 getTopBlockPos(World world)
	{
		int y = getTopBlockY(world);
		return new Vector3(intX(), y, intZ());
	}
	
	public Chunk getChunkfromBlockCoords(World world)
	{
        if(!world.checkChunksExist(intX(), intY(), intZ(), intX(), intY(), intZ()))
        {
        	return null;
        }
		return world.getChunkFromBlockCoords(intX(), intZ());
	}
	
	public Chunk getChunkFromChunkCoords(World world)
	{
		return world.getChunkFromChunkCoords(intX(), intY());
	}
	
	/**
	 * Locates the first solid block in the line indicated by the direction
	 * vector, starting from the source if range is given as 0, it will check
	 * out to 320 blocks.
	 * 
	 * @param worldObj
	 * @param source
	 * @param direction
	 * @param range
	 * @return
	 */
	public static Vector3 findNextSolidBlock(World worldObj, Vector3 source,
			Vector3 direction, double range) {
		direction = vectorNormalize(direction);
		int n = 0;
		double xprev = source.x, yprev = source.y, zprev = source.z;
		double dx, dy, dz;

		for (double i = 0; i < range; i += 1) {
			dx = i * direction.x;
			dy = i * direction.y;
			dz = i * direction.z;

			double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);
			
			if(ytest>255) return null;

			if (!(Int(xtest) == Int(xprev) && Int(ytest) == Int(yprev) && Int(ztest) == Int(zprev))) {

				Block block = worldObj.getBlock(Int(xtest),
						Int(ytest), Int(ztest));

				boolean clear = block == null
						|| block.isAir(worldObj, Int(xtest), Int(ytest),
								Int(ztest))
						|| (new Vector3(xtest, ytest, ztest)).isFluid(worldObj);

				if (!clear) {
					return new Vector3(Int(xtest), Int(ytest), Int(ztest));
				}
			}

			yprev = ytest;
			xprev = xtest;
			zprev = ztest;
		}
		return null;
	}

	public static Vector3 getVectorToEntity(Vector3 source, Entity target) {
		if (target != null)
			return vectorSubtract(new Vector3(target.posX, target.posY,
					target.posZ), source);
		else
			return new Vector3();
	}

	/**
	 * determines whether the source can see out as far as range in the given
	 * direction.
	 * 
	 * @param worldObj
	 * @param source
	 * @param direction
	 * @param range
	 * @return
	 */
	public static boolean isVisibleRange(World worldObj, Vector3 source,
			Vector3 direction, double range) {
		direction = vectorNormalize(direction);
		int n = 0;
		double xprev = source.x, yprev = source.y, zprev = source.z;
		double dx, dy, dz;
		boolean notNull = true;
		double closest = range;
		double blocked = 0;

		for (double i = 0; i < range; i += 0.0625) {
			dx = i * direction.x;
			dy = i * direction.y;
			dz = i * direction.z;

			double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

			boolean check = isPointClearBlocks(xtest, ytest, ztest, worldObj);
			blocked = Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (!check) {
				return false;
			}
			yprev = ytest;
			xprev = xtest;
			zprev = ztest;
		}
		return true;
	}

	/**
	 * determines whether the source can see out as far as range in the given
	 * direction.
	 * 
	 * @param worldObj
	 * @param source
	 * @param direction
	 * @param range
	 * @return
	 */
	public static Vector3 getNextSurfacePoint(IBlockAccess worldObj,
			Vector3 source, Vector3 direction, double range) {
		direction = vectorNormalize(direction);

		double xprev = source.x, yprev = source.y, zprev = source.z;
		double dx, dy, dz;

		for (double i = 0; i < range; i += 0.0625) {
			dx = i * direction.x;
			dy = i * direction.y;
			dz = i * direction.z;

			double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

			boolean check = isPointClearBlocks(xtest, ytest, ztest, worldObj);
			if (!check) {
				return new Vector3(xtest, ytest, ztest);
			}
			yprev = ytest;
			xprev = xtest;
			zprev = ztest;
		}
		return null;
	}

	/**
	 * determines whether the source can see out as far as range in the given
	 * direction.  This version ignores blocks like leaves and wood, used for 
	 * finding the surface of the ground.
	 * 
	 * @param worldObj
	 * @param source
	 * @param direction
	 * @param range
	 * @return
	 */
	public static Vector3 getNextSurfacePoint2(IBlockAccess worldObj,
			Vector3 source, Vector3 direction, double range) {
		direction = vectorNormalize(direction);

		double xprev = source.x, yprev = source.y, zprev = source.z;
		double dx, dy, dz;

		for (double i = 0; i < range; i += 0.5) {
			dx = i * direction.x;
			dy = i * direction.y;
			dz = i * direction.z;

			double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

			boolean check = isNotSurfaceBlock((World) worldObj, new Vector3(xtest, ytest, ztest));//isPointClearBlocks(xtest, ytest, ztest, worldObj);
			if (!check) {
				return new Vector3(xtest, ytest, ztest);
			}
			yprev = ytest;
			xprev = xtest;
			zprev = ztest;
		}
		return null;
	}

	/**
	 * determines whether the source can see out as far as range in the given
	 * direction.
	 * 
	 * @param worldObj
	 * @param source
	 * @param direction
	 * @param range
	 * @return
	 */
	public static Vector3 getNextSurfacePointFunction(World worldObj,
			Vector3 source, Vector3 direction, Vector3 acceleration,
			double range) {
		direction = vectorNormalize(direction);

		double xprev = source.x, yprev = source.y, zprev = source.z;
		double dx, dy, dz;

		for (double i = 0; i < range; i += 0.0625) {
			dx = i * (direction.x + i * acceleration.x / 2);
			dy = i * (direction.y + i * acceleration.y / 2);
			dz = i * (direction.z + i * acceleration.z / 2);

			double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

			boolean check = isPointClearBlocks(xtest, ytest, ztest, worldObj);
			if (!check) {
				return new Vector3(xtest, ytest, ztest);
			}
			yprev = ytest;
			xprev = xtest;
			zprev = ztest;
		}
		return null;
	}

	/**
	 * determines whether the location given is visible from source.
	 * 
	 * @param worldObj
	 * @param source
	 * @param direction
	 * @param range
	 * @return
	 */
	public static boolean isVisibleLocation(World worldObj, Vector3 source,
			Vector3 location) {
		Vector3 direction = vectorSubtract(location, source);
		double range = direction.mag();
		return isVisibleRange(worldObj, source, direction, range);
	}

	public static boolean isVisibleEntityFromEntity(Entity looker,
			Entity target) {
		
		if(looker==null||target==null) return false;
		
		return isVisibleRange(looker.worldObj, entity(target), entity(looker),
				entity(looker).distToEntity(target));
	}

	public static Vector3 firstEntityLocation(double range, Vector3 direction,
			Vector3 source, World worldObj, boolean effect) {
		direction = vectorNormalize(direction);
		int n = 0;
		double xprev = source.x, yprev = source.y, zprev = source.z;
		double dx, dy, dz;
		boolean notNull = true;
		double closest = range;
		double blocked = 0;

		Vector3 temp = new Vector3();

		for (double i = 0; i < range; i += 0.0625) {
			dx = i * direction.x;
			dy = i * direction.y;
			dz = i * direction.z;

			double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

			boolean check = isPointClearBlocks(xtest, ytest, ztest, worldObj);
			blocked = Math.sqrt(dx * dx + dy * dy + dz * dz);

			if (!check) {
				break;
			}

			if (effect && worldObj.isRemote) {
				worldObj.spawnParticle("flame", xtest, ytest, ztest, 0, 0, 0);
			}

			if (!((int) xtest == (int) xprev && (int) ytest == (int) yprev && (int) ztest == (int) zprev)) {
				int x0 = (xtest > 0 ? (int) xtest : (int) xtest - 1), y0 = (ytest > 0 ? (int) ytest
						: (int) ytest - 1), z0 = (ztest > 0 ? (int) ztest
						: (int) ztest - 1);
				List<Entity> targets = worldObj.getEntitiesWithinAABB(
						EntityLiving.class, AxisAlignedBB.getBoundingBox(x0,
								y0, z0, x0 + 1, y0 + 1, z0 + 1));
				if (targets != null && targets.size() > 0) {
					return new Vector3(xtest, ytest, ztest);
				}
			}
			yprev = ytest;
			xprev = xtest;
			zprev = ztest;
		}

		return null;
	}

	public static Vector3 firstEntityLocationExcluding(double range,
			Vector3 direction, Vector3 source, World worldObj, boolean effect,
			Entity excluded) {
		direction = vectorNormalize(direction);
		int n = 0;
		double xprev = source.x, yprev = source.y, zprev = source.z;
		double dx, dy, dz;
		double blocked = 0;

		Vector3 temp = new Vector3();

		for (double i = 0; i < range; i += 0.0625) {
			dx = i * direction.x;
			dy = i * direction.y;
			dz = i * direction.z;

			double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

			boolean check = isPointClearBlocks(xtest, ytest, ztest, worldObj);
			blocked = Math.sqrt(dx * dx + dy * dy + dz * dz);

			if (!check) {
				break;
			}

			if (effect && worldObj.isRemote) {
				worldObj.spawnParticle("flame", xtest, ytest, ztest, 0, 0, 0);
			}

			if (!((int) xtest == (int) xprev && (int) ytest == (int) yprev && (int) ztest == (int) zprev)) {
				int x0 = (xtest > 0 ? (int) xtest : (int) xtest - 1), y0 = (ytest > 0 ? (int) ytest
						: (int) ytest - 1), z0 = (ztest > 0 ? (int) ztest
						: (int) ztest - 1);
				List<Entity> targets = worldObj
						.getEntitiesWithinAABBExcludingEntity(excluded,
								AxisAlignedBB.getBoundingBox(x0, y0, z0,
										x0 + 1, y0 + 1, z0 + 1));
				if (targets != null && targets.size() > 0) {
					List<Entity> ret = new ArrayList<Entity>();
					for (Entity e : targets) {
						if (e instanceof EntityLivingBase) {
							ret.add(e);
						}
					}
					if (ret != null && ret.size() > 0)
						return new Vector3(xtest, ytest, ztest);
				}
			}
			yprev = ytest;
			xprev = xtest;
			zprev = ztest;
		}

		return null;
	}
	
	public static Entity firstEntityExcluding(double range,
			Vector3 direction, Vector3 source, World worldObj, boolean effect,
			Entity excluded) {
		direction = vectorNormalize(direction);
		int n = 0;
		double xprev = source.x, yprev = source.y, zprev = source.z;
		double dx, dy, dz;
		double blocked = 0;

		Vector3 temp = new Vector3();

		for (double i = 0; i < range; i += 0.0625) {
			dx = i * direction.x;
			dy = i * direction.y;
			dz = i * direction.z;

			double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

			boolean check = isPointClearBlocks(xtest, ytest, ztest, worldObj);
			blocked = Math.sqrt(dx * dx + dy * dy + dz * dz);

			if (!check) {
				break;
			}

			if (effect && worldObj.isRemote) {
				worldObj.spawnParticle("flame", xtest, ytest, ztest, 0, 0, 0);
			}

			if (!((int) xtest == (int) xprev && (int) ytest == (int) yprev && (int) ztest == (int) zprev)) {
				int x0 = (xtest > 0 ? (int) xtest : (int) xtest - 1), y0 = (ytest > 0 ? (int) ytest
						: (int) ytest - 1), z0 = (ztest > 0 ? (int) ztest
						: (int) ztest - 1);
				List<Entity> targets = worldObj
						.getEntitiesWithinAABBExcludingEntity(excluded,
								AxisAlignedBB.getBoundingBox(x0, y0, z0,
										x0 + 1, y0 + 1, z0 + 1));
				if (targets != null && targets.size() > 0) {
					List<Entity> ret = new ArrayList<Entity>();
					for (Entity e : targets) {
						if (e instanceof EntityLivingBase) {
							ret.add(e);
						}
					}
					if (ret != null && ret.size() > 0)
						return ret.get(0);
				}
			}
			yprev = ytest;
			xprev = xtest;
			zprev = ztest;
		}

		return null;
	}

	public static Vector3 firstEntityLocationFunctionExcluding(double range,
			Vector3 direction, Vector3 source, Vector3 acceleration,
			World worldObj, boolean effect, Entity excluded) {
		Vector3 normalizedDirection = vectorNormalize(direction);
		int n = 0;
		double xprev = source.x, yprev = source.y, zprev = source.z;
		double dx, dy, dz;
		boolean notNull = true;
		double closest = range;
		double blocked = 0;

		Vector3 temp = new Vector3();

		for (double i = 0; i < range; i += 0.0625) {
			dx = i * (normalizedDirection.x);
			dy = i * (normalizedDirection.y);
			dz = i * (normalizedDirection.z);
			direction = direction.add(acceleration);
			normalizedDirection = vectorNormalize(direction);
			double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

			boolean check = isPointClearBlocks(xtest, ytest, ztest, worldObj);
			blocked = Math.sqrt(dx * dx + dy * dy + dz * dz);

			if (!check) {
				break;
			}

			if (effect && worldObj.isRemote) {
				worldObj.spawnParticle("flame", xtest, ytest, ztest, 0, 0, 0);
			}

			if (!((int) xtest == (int) xprev && (int) ytest == (int) yprev && (int) ztest == (int) zprev)) {
				int x0 = (xtest > 0 ? (int) xtest : (int) xtest - 1), y0 = (ytest > 0 ? (int) ytest
						: (int) ytest - 1), z0 = (ztest > 0 ? (int) ztest
						: (int) ztest - 1);
				List<Entity> targets = worldObj.getEntitiesWithinAABB(
						EntityLivingBase.class, AxisAlignedBB.getBoundingBox(x0,
								y0, z0, x0 + 1, y0 + 1, z0 + 1));
				if (targets != null && targets.size() > 0) {
					if (!(targets.size() == 1 && targets.contains(excluded)))
						return new Vector3(xtest, ytest, ztest);
				}
			}
			yprev = ytest;
			xprev = xtest;
			zprev = ztest;
		}

		return null;
	}

	public static boolean isEntityVisibleInDirection(Entity target,
			Vector3 direction, Vector3 source, World worldObj) {
		direction = vectorNormalize(direction);
		Vector3 vec = firstEntityLocation(distToEntity(source, target),
				direction, source, worldObj, false);
		return target == null ? false : vec == null ? false : vec
				.livingEntityInBox(worldObj) == null ? false : vec
				.livingEntityInBox(worldObj).contains(target);
	}
	
	public boolean isClearOfBlocks(IBlockAccess worldObj)
	{
		boolean ret = false;
		Block b = getBlock(worldObj);
		ret = ret || isAir(worldObj);
		ret = ret || getBlockMaterial(worldObj).isLiquid();
		ret = ret || getBlockMaterial(worldObj).isReplaceable();
		
		return ret;//isPointClearBlocks(x, y, z, worldObj);
	}
	
	public boolean isEntityClearOfBlocks(IBlockAccess worldObj, Entity e)
	{
		boolean ret = true;
		
		for(int i = -1; i<=1; i++)
			for(int j = -1; j<=1; j++)
				ret = ret && this.add(new Vector3(i*e.width/2,0,j*e.width/2)).isClearOfBlocks(worldObj);
		
//		ret = ret && 
//		ret = ret && this.add(new Vector3(0,0,-e.width/2)).isClearOfBlocks(worldObj);
//		ret = ret && this.add(new Vector3(e.width/2,0,0)).isClearOfBlocks(worldObj);
//		ret = ret && this.add(new Vector3(-e.width/2,0,0)).isClearOfBlocks(worldObj);
//		ret = ret && this.add(new Vector3(0,e.height,0)).isClearOfBlocks(worldObj);
		
		return ret;
	}

	public static boolean isPointClearBlocks(double x, double y, double z,
			IBlockAccess worldObj) {
		Vector3 v = new Vector3(x,y,z);
		int x0 = v.intX(), y0 = v.intY(), z0 = v.intZ();

		Block block = worldObj.getBlock(x0, y0, z0);

		if(worldObj.getBlock(x0, y0, z0).isNormalCube()) return false;
		
		if (block == null)
			return true;

		List<AxisAlignedBB> aabbs = new ArrayList();

		if (worldObj instanceof World)
			block.addCollisionBoxesToList((World) worldObj, x0, y0, z0,
					AxisAlignedBB.getBoundingBox(x, y, z, x, y, z), aabbs, null);

		if (aabbs.size() == 0)
			return true;

		for (AxisAlignedBB aabb : aabbs) {
			if (aabb != null) {
				if (y <= aabb.maxY && y >= aabb.minY)
					return false;
				if (z <= aabb.maxZ && z >= aabb.minZ)
					return false;
				if (x <= aabb.maxX && x >= aabb.minX)
					return false;
			}
		}

		return true;
	}

	public boolean clearOfBlocks(World worldObj) {
		return isPointClearBlocks(x, y, z, worldObj);
	}

	public boolean pointClear(World worldObj) {
		return isPointClearBlocks(x, y, z, worldObj)
				&& livingEntityAtPoint(worldObj) == null;
	}

	public boolean pointClearExcludingEntity(World worldObj, Entity e) {
		return isPointClearBlocks(x, y, z, worldObj)
				&& livingEntityAtPointExcludingEntity(worldObj, e) == null;
	}

	public boolean isPointClearOfaabb(double x, double y, double z,
			AxisAlignedBB aabb) {
		if (y <= aabb.maxY && y >= aabb.minY)
			return false;
		if (z <= aabb.maxZ && z >= aabb.minZ)
			return false;
		if (x <= aabb.maxX && x >= aabb.minX)
			return false;

		return true;
	}

	public boolean isPointClearOfEntity(double x, double y, double z, Entity e) {
		AxisAlignedBB aabb = e.boundingBox;

		if (y <= aabb.maxY && y >= aabb.minY)
			return false;
		if (z <= aabb.maxZ && z >= aabb.minZ)
			return false;
		if (x <= aabb.maxX && x >= aabb.minX)
			return false;

		return true;
	}

	public static class Matrix3 {
		public Vector3[] Rows = new Vector3[3];
		int size = 3;

		public Matrix3() {
			Rows[0] = new Vector3();
			Rows[1] = new Vector3();
			Rows[2] = new Vector3();
		}

		public Matrix3(double[] a, double[] b, double[] c) {
			Rows[0] = Vector3.ArrayTo(a);
			Rows[1] = Vector3.ArrayTo(b);
			Rows[2] = Vector3.ArrayTo(c);
		}

		public Matrix3(Vector3 a, Vector3 b, Vector3 c) {
			Rows[0] = a.Copy();
			Rows[1] = b.Copy();
			Rows[2] = c.Copy();
		}

		public Matrix3(Vector3 a, Vector3 b) {
			this(a, b, new Vector3(0, 0, 0));
		}

		public Matrix3(double d, double e, double f) {
			this();
			Rows[1] = new Vector3(d, e, f);
		}

		public Matrix3(double d, double e, double f, Vector3 vector3) {
			this(d, e, f);
			Rows[2] = vector3;
		}

		public Vector3 get(int i) {
			assert (i < 3);
			return Rows[i];
		}

		public Vector3 boxMin() {
			return Rows[0];
		}

		public Vector3 boxMax() {
			return Rows[1];
		}

		public Vector3 boxRotation() {
			return Rows[2];
		}

		public double get(int i, int j) {
			assert (i < 3);
			return Rows[i].get(j);
		}

		public double boxZLength() {
			return Math.abs(get(1, 2) - get(0, 2));
		}

		public double boxYLength() {
			return Math.abs(get(1, 1) - get(0, 1));
		}

		public double boxXLength() {
			return Math.abs(get(1, 0) - get(0, 0));
		}

		public Vector3[] corners() {
			Vector3[] corners = new Vector3[8];

			corners[0] = boxMin();
			corners[1] = boxMax();

			corners[2] = new Vector3(boxMin().x, boxMin().y, boxMax().z);
			corners[3] = new Vector3(boxMin().x, boxMax().y, boxMin().z);
			corners[4] = new Vector3(boxMax().x, boxMin().y, boxMin().z);

			corners[5] = new Vector3(boxMin().x, boxMax().y, boxMax().z);
			corners[6] = new Vector3(boxMax().x, boxMin().y, boxMax().z);
			corners[7] = new Vector3(boxMax().x, boxMax().y, boxMin().z);

			if (!boxRotation().isEmpty()) {
				Vector3.rotateAboutAngles(corners, boxRotation().y,
						boxRotation().z);
			}

			return corners;
		}

		public Matrix3 addOffset(Vector3 pushOffset) {
			Matrix3 ret = this.copy();
			ret.Rows[0] = ret.Rows[0].add(pushOffset);
			ret.Rows[1] = ret.Rows[1].add(pushOffset);
			return ret;
		}

		public Matrix3 copy() {
			Matrix3 ret = new Matrix3(Rows[0], Rows[1], Rows[2]);
			return ret;
		}

		public void set(int i, Vector3 j) {
			assert (i < 3);
			Rows[i] = j;
		}

		public void set(int i, int j, double k) {
			Rows[i].set(j, k);
		}

		public double[][] toArray() {
			return new double[][] { Rows[0].toArray(), Rows[1].toArray(),
					Rows[2].toArray() };
		}

		public Matrix3 addToRows(Vector3 vec) {
			return new Matrix3(Rows[0].add(vec), Rows[1].add(vec),
					Rows[2].add(vec));
		}

		public String toString() {
			String eol = System.getProperty("line.separator");
			return eol + "0: " + Rows[0].toString() + eol + "1: "
					+ Rows[1].toString() + eol + "2 : " + Rows[2].toString();
		}

		/**
		 * Multiplies MatrixA by MatrixB in the form AB.
		 * 
		 * @param MatrixA
		 * @param MatrixB
		 * @return
		 */
		public static Matrix3 matrixMatrixMult(Matrix3 MatrixA, Matrix3 MatrixB) {
			Matrix3 MatrixC = new Matrix3();
			MatrixB = matrixTranspose(MatrixB);
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					MatrixC.set(i, j, vectorDot(MatrixA.get(i), MatrixB.get(j)));
				}
			}
			return MatrixC;
		}

		/**
		 * Transposes the given Matrix
		 * 
		 * @param Matrix
		 * @return
		 */
		public static Matrix3 matrixTranspose(Matrix3 Matrix) {
			Matrix3 MatrixT = new Matrix3();
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					MatrixT.set(i, j, Matrix.get(j, i));
				}
			}
			return MatrixT;
		}

		/**
		 * Computes the Inverse of the matrix
		 * 
		 * @param Matrix
		 * @return
		 */
		public static Matrix3 matrixInverse(Matrix3 Matrix) {
			Matrix3 Inverse = new Matrix3();
			double det = matrixDet(Matrix);
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					Inverse.set(i, j, Math.pow(-1, i + j)
							* matrixDet(matrixMinor(Matrix, i, j)) / det);
				}
			}
			Inverse = matrixTranspose(Inverse);
			return Inverse;
		}

		/**
		 * Computes the Determinant of the given matrix, Matrix must be square.
		 * 
		 * @param Matrix
		 * @return
		 */
		public static double matrixDet(Matrix3 Matrix) {
			double det = 0;
			int n = Matrix.size;
			if (n == 2) {
				det = Matrix.get(0, 0) * Matrix.get(1, 1) - Matrix.get(1, 0)
						* Matrix.get(0, 1);
			} else {
				for (int i = 0; i < n; i++) {
					det += Math.pow(-1, i) * Matrix.get(0, i)
							* matrixDet(matrixMinor(Matrix, 0, i));
				}
			}
			return det;
		}

		/**
		 * Computes the minor matrix formed from removal of the ith row and jth
		 * column of matrix.
		 * 
		 * @param Matrix
		 * @param i
		 * @param j
		 * @return
		 */
		public static Matrix3 matrixMinor(Matrix3 input, int i, int j) {
			double[][] Matrix = input.toArray();
			int n = Matrix.length;
			int m = Matrix[0].length;
			Double[][] TempMinor = new Double[m - 1][n - 1];
			List<ArrayList<Double>> row = new ArrayList<ArrayList<Double>>();
			for (int k = 0; k < n; k++) {
				if (k != i) {
					row.add(new ArrayList<Double>());
					for (int l = 0; l < m; l++) {
						if (l != j) {
							row.get(k - (k > i ? 1 : 0)).add(Matrix[k][l]);
						}
					}
				}
			}
			for (int k = 0; k < n - 1; k++) {
				TempMinor[k] = row.get(k).toArray(new Double[0]);
			}
			Matrix3 Minor = new Matrix3();
			Minor.size = n - 1;
			for (int k = 0; k < n - 1; k++) {
				for (int l = 0; l < m - 1; l++) {
					Minor.set(k, l, TempMinor[k][l]);
				}
			}
			return Minor;
		}

		/**
		 * Adds MatrixA and MatrixB
		 * 
		 * @param MatrixA
		 * @param MatrixB
		 * @return
		 */
		public static Matrix3 matrixAddition(Matrix3 MatrixA, Matrix3 MatrixB) {
			Matrix3 MatrixC = new Matrix3();
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					MatrixC.set(i, i, MatrixA.get(i, j) + MatrixB.get(i, j));
				}
			}
			return MatrixC;
		}

		/**
		 * Subtracts MatrixB from MatrixA
		 * 
		 * @param MatrixA
		 * @param MatrixB
		 * @return
		 */
		public static Matrix3 matrixSubtraction(Matrix3 MatrixA, Matrix3 MatrixB) {
			Matrix3 MatrixC = new Matrix3();
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					MatrixC.set(i, i, MatrixA.get(i, j) - MatrixB.get(i, j));
				}
			}
			return MatrixC;
		}

		/**
		 * Multiplies the Matrix by the scalar
		 * 
		 * @param Matrix
		 * @param scalar
		 * @return
		 */
		public static Matrix3 matrixScalarMuli(Matrix3 Matrix, double scalar) {
			Matrix3 ret = new Matrix3();
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					ret.set(i, j, Matrix.get(i, j) * scalar);
				}
			}
			return ret;
		}

		public boolean pushOutOfBox(Entity pusher, Entity e, Vector3 offset) {
			boolean ret = false;
			ret = doEntityCollision(pusher, e, offset);
			return ret;
		}

		public boolean doEntityCollision(Entity pusher, Entity e, Vector3 offset) {
			return doEntityCollision(pusher, e, offset, new Vector3());
		}

		public boolean doEntityCollision(Entity pusher, Entity e,
				Vector3 offset, Vector3 entity) {
			return doCollision(new Vector3(pusher), new Vector3(pusher.motionX,
					pusher.motionY, pusher.motionZ), e, offset, entity);
		}

		public boolean doTileCollision(Vector3 tile, Entity e) {
			return doCollision(tile, new Vector3(), e, new Vector3(),
					new Vector3());
		}

		public boolean doCollision(Vector3 push, Vector3 v, Entity e,
				Vector3 offset, Vector3 entity) {
			boolean ret = false;

			offset.y += e.yOffset;
			if (entity.isNaN()) {
				System.out.println("HOW DIS NAN " + entity.toString());
				entity.clear();
			}

			Vector3 r = ((new Vector3(e)).subtract(offset).subtract(push))
					.add(entity);

			boolean rot = false;
			Vector3 rotation = boxRotation();

			if (!(rotation.y == 0 && rotation.z == 0)) {
				rot = true;
				r = r.rotateAboutAngles(rotation.y, rotation.z);
			}

			Vector3 min = boxMin();
			min.y = -e.yOffset;
			Vector3 max = boxMax();

			boolean flag = !(e instanceof IMultiBox);

			double posxOffset = flag ? e.width / 2 : ((IMultiBox) e).bounds(
					push).boxMax().x;
			double negxOffset = flag ? -e.width / 2 : ((IMultiBox) e).bounds(
					push).boxMin().x;
			double posyOffset = flag ? 0 : ((IMultiBox) e).bounds(push)
					.boxMax().y;
			double negyOffset = flag ? 0 : ((IMultiBox) e).bounds(push)
					.boxMin().y;
			double poszOffset = flag ? e.width / 2 : ((IMultiBox) e).bounds(
					push).boxMax().z;
			double negzOffset = flag ? -e.width / 2 : ((IMultiBox) e).bounds(
					push).boxMin().z;

			if (flag
					&& !(r.inMatBox(this)
							|| r.add(new Vector3(posxOffset, 0, 0)).inMatBox(
									this)
							|| r.add(new Vector3(negxOffset, 0, 0)).inMatBox(
									this)

							|| r.add(new Vector3(0, 0, poszOffset)).inMatBox(
									this) || r.add(
							new Vector3(0, 0, negzOffset)).inMatBox(this))) {
				// System.out.println();
				return ret;
			} else if (!(r.inMatBox(this)
					|| r.add(new Vector3(posxOffset, 0, 0)).inMatBox(this)
					|| r.add(new Vector3(negxOffset, 0, 0)).inMatBox(this)

					|| r.add(new Vector3(0, 0, poszOffset)).inMatBox(this)
					|| r.add(new Vector3(0, 0, negzOffset)).inMatBox(this)

					|| r.add(new Vector3(0, posyOffset, 0)).inMatBox(this) || r
					.add(new Vector3(0, negyOffset, 0)).inMatBox(this)

			))

			{
				return ret;
			}
			// if(e instanceof IMultiBox)
			{

			}

			Vector3 pushDir = new Vector3();
			Vector3 pushLoc = new Vector3();

			double x = (r.x - (boxXLength()) / 2) / (boxXLength());
			double y = (r.y - (boxYLength()) / 2) / (boxYLength());
			double z = (r.z - (boxZLength()) / 2) / (boxZLength());
			double yoffset = 0;

			double yDiff = push.y + max.y + offset.y;
			double rho = Math.sqrt(r.x * r.x + r.z * r.z);

			if (rot) {
				yDiff += rho * Math.sin(-rotation.y);
			}

			boolean movedx = false, movedy = false, movedz = false;

			Vector3 location = new Vector3(x, y, z);

			if (location.isNaN()) {
				location.clear();
			}
			{
				boolean entitymovingup = (e.motionY - v.y) > 0;
				location = location.add(new Vector3(0.5, 0, 0.5));

				boolean xpositive = location.x > 0
						&& location.x >= Math.abs(location.z);
				boolean xnegative = location.x < 0
						&& -location.x >= Math.abs(location.z);

				boolean zpositive = location.z > 0
						&& location.z >= Math.abs(location.x);
				boolean znegative = location.z < 0
						&& -location.z >= Math.abs(location.x);

				boolean ynegative = (entitymovingup || (e.motionY - v.y) > 0.2)
						&& location.y < 0;
				boolean ypositive = Math.abs(yDiff - e.posY) <= e.stepHeight
						|| ((e.motionY - v.y) < -0.2 && (e.posY - e.motionY > yDiff));// &&!entitymovingup;//||(((pusher.motionY)<0)&&(yDiff-e.posY)<=e.stepHeight)||(flag&&(yDiff-e.posY)<=e.stepHeight);

				double f = 0.005;
				double fx = f, fy = f, fz = f;
				if (ypositive) {
					pushDir.y = v.y > 0 ? v.y : 0;
					if (e instanceof EntityLiving && collisionDamage) {
						int damage = Math.max(
								(int) ((-e.motionY + v.y + 0.15) * (-e.motionY
										+ v.y + 0.15)), 0);
						if (damage > 0)
							((EntityLiving) e).attackEntityFrom(
									DamageSource.fall, damage);
					}
					e.motionY = v.y > 0 ? v.y : 0;
					e.setPosition(e.posX + v.x, yDiff, e.posZ + v.z);
					e.isAirBorne = false;
					e.onGround = true;
					e.fallDistance = 0;
					movedy = true;
				} else if (ynegative) {
					pushDir.y = -e.motionY - fy + v.y;
					movedy = true;

					if (e instanceof EntityLiving && collisionDamage) {
						int damage = Math.max(
								(int) ((e.motionY + v.y + 0.15) * (e.motionY
										+ v.y + 0.15)), 0);
						((EntityLiving) e).attackEntityFrom(DamageSource.fall,
								damage);
					}
				}

				if (!movedy) {
					if (xnegative) {
						pushDir.x = -fx;
						pushLoc.x = min.x;
						// System.out.println("pushed1"+location.toString());
						movedx = true;
					} else if (xpositive) {
						pushDir.x = fx;
						pushLoc.x = max.x;
						// System.out.println("pushed2"+location.toString());
						movedx = true;
					}
					if (znegative) {
						pushDir.z = -fz;
						pushLoc.z = min.z;
						// System.out.println("pushed3"+location.toString());
						movedz = true;
					} else if (zpositive) {
						pushDir.z = fz;
						pushLoc.z = max.z;
						// System.out.println("pushed4"+location.toString());
						movedz = true;
					}
				}

			}

			if (rot) {
				pushDir = pushDir.rotateAboutAngles(-rotation.y, -rotation.z);
				pushLoc = pushLoc.rotateAboutAngles(-rotation.y, -rotation.z);
			}
			if (movedy || movedx || movedz) {
				if (pushDir.y != 0 && !Double.isNaN(pushDir.y)) {
					e.motionY = pushDir.y;

				}
				if (pushDir.x != 0 && !Double.isNaN(pushDir.x)) {
					if (pushLoc.x != 0 && !pushLoc.isNaN()) {
						e.setPosition(push.x + pushLoc.x + pushDir.x + offset.x
								+ (pushLoc.x > 0 ? e.width / 2 : -e.width / 2),
								e.posY, e.posZ);
					}
				}
				if (pushDir.z != 0 && !Double.isNaN(pushDir.z)) {
					if (pushLoc.z != 0 && !pushLoc.isNaN()) {
						e.setPosition(e.posX, e.posY, push.z + pushLoc.z
								+ pushDir.z + offset.z
								+ (pushLoc.z > 0 ? e.width / 2 : -e.width / 2));
					}

				}
				ret = movedx || movedz || movedy;
			}
			if (e instanceof EntityItem) {
				if (movedy) {
					e.motionX = 0;
					e.motionZ = 0;
				}
			}
			return movedx || movedz || movedy;
		}

		public Vector3 getEntityCollisionVector(Entity pusher, Entity e,
				Vector3 offset, Vector3 entity) {
			return getCollisionVector(new Vector3(pusher), new Vector3(
					pusher.motionX, pusher.motionY, pusher.motionZ), e, offset,
					entity);
		}

		public Vector3 getCollisionVector(Vector3 push, Vector3 v, Entity e,
				Vector3 offset, Vector3 entity) {
			offset.y += e.yOffset;
			if (entity.isNaN()) {
				System.out.println("HOW DIS NAN " + entity.toString());
				entity.clear();
			}

			Vector3 pushDir = new Vector3();
			Vector3 pushLoc = new Vector3();

			// Vector3 push = new Vector3(pusher);
			Vector3 r = ((new Vector3(e)).subtract(offset).subtract(push))
					.add(entity);

			boolean rot = false;
			Vector3 rotation = boxRotation();

			if (!(rotation.y == 0 && rotation.z == 0)) {
				rot = true;
				r = r.rotateAboutAngles(rotation.y, rotation.z);
			}

			Vector3 min = boxMin();
			min.y = -e.yOffset;
			Vector3 max = boxMax();

			boolean flag = !(e instanceof IMultiBox);

			double posxOffset = flag ? e.width / 2 : ((IMultiBox) e).bounds(
					push).boxMax().x;
			double negxOffset = flag ? -e.width / 2 : ((IMultiBox) e).bounds(
					push).boxMin().x;
			double posyOffset = flag ? 0 : ((IMultiBox) e).bounds(push)
					.boxMax().y;
			double negyOffset = flag ? 0 : ((IMultiBox) e).bounds(push)
					.boxMin().y;
			double poszOffset = flag ? e.width / 2 : ((IMultiBox) e).bounds(
					push).boxMax().z;
			double negzOffset = flag ? -e.width / 2 : ((IMultiBox) e).bounds(
					push).boxMin().z;

			if (flag
					&& !(r.inMatBox(this)
							|| r.add(new Vector3(posxOffset, 0, 0)).inMatBox(
									this)
							|| r.add(new Vector3(negxOffset, 0, 0)).inMatBox(
									this)

							|| r.add(new Vector3(0, 0, poszOffset)).inMatBox(
									this) || r.add(
							new Vector3(0, 0, negzOffset)).inMatBox(this))) {
				// System.out.println();
				return pushDir;
			} else if (!(r.inMatBox(this)
					|| r.add(new Vector3(posxOffset, 0, 0)).inMatBox(this)
					|| r.add(new Vector3(negxOffset, 0, 0)).inMatBox(this)

					|| r.add(new Vector3(0, 0, poszOffset)).inMatBox(this)
					|| r.add(new Vector3(0, 0, negzOffset)).inMatBox(this)

					|| r.add(new Vector3(0, posyOffset, 0)).inMatBox(this) || r
					.add(new Vector3(0, negyOffset, 0)).inMatBox(this)

			))

			{
				return pushDir;
			}

			double x = (r.x - (boxXLength()) / 2) / (boxXLength());
			double y = (r.y - (boxYLength()) / 2) / (boxYLength());
			double z = (r.z - (boxZLength()) / 2) / (boxZLength());
			double yoffset = 0;

			double yDiff = push.y + max.y + offset.y;
			double rho = Math.sqrt(r.x * r.x + r.z * r.z);

			if (rot) {
				yDiff += rho * Math.sin(-rotation.y);
			}

			boolean movedx = false, movedy = false, movedz = false;

			Vector3 location = new Vector3(x, y, z);

			if (location.isNaN()) {
				location.clear();
			}
			{
				boolean entitymovingup = (e.motionY - v.y) > 0;
				location = location.add(new Vector3(0.5, 0, 0.5));

				boolean xpositive = location.x > 0
						&& location.x >= Math.abs(location.z);
				boolean xnegative = location.x < 0
						&& -location.x >= Math.abs(location.z);

				boolean zpositive = location.z > 0
						&& location.z >= Math.abs(location.x);
				boolean znegative = location.z < 0
						&& -location.z >= Math.abs(location.x);

				boolean ynegative = (entitymovingup || (e.motionY - v.y) > 0.2)
						&& location.y < 0;
				boolean ypositive = Math.abs(yDiff - e.posY) <= e.stepHeight
						|| ((e.motionY - v.y) < -0.2 && (e.posY - e.motionY > yDiff));// &&!entitymovingup;//||(((pusher.motionY)<0)&&(yDiff-e.posY)<=e.stepHeight)||(flag&&(yDiff-e.posY)<=e.stepHeight);

				// System.out.println(location.toString());

				double f = 0.010;

				if (ypositive) {
					// System.out.println(yDiff+" "+push.y+" "+max.y+" "+offset.y+" "+(Math.sqrt(r.x*r.x+r.z*r.z)*Math.sin(-rotation.y))+" "+(-rotation.y));
					pushDir.y = v.y > 0 ? v.y : 0;
					// System.out.println((yDiff-e.posY)+" "+e.stepHeight+" "+(e.motionY-pusher.motionY));
					if (e instanceof EntityLiving && collisionDamage) {
						int damage = Math.max(
								(int) ((-e.motionY + v.y + 0.15) * (-e.motionY
										+ v.y + 0.15)), 0);
						// System.out.println(damage+" "+(-e.motionY+pusher.motionY)+" "+pusher.motionY);
						if (damage > 0)
							((EntityLiving) e).attackEntityFrom(
									DamageSource.fall, damage);
					}
					e.motionY = v.y > 0 ? v.y : 0;
					e.setPosition(e.posX + v.x, yDiff, e.posZ + v.z);
					e.motionX = (e.motionX + v.x) * 0.9;
					e.motionZ = (e.motionZ + v.z) * 0.9;
					e.isAirBorne = false;
					e.onGround = true;
					e.fallDistance = 0;
					movedy = true;
					// System.out.println("pushed+y");
				} else if (ynegative) {
					pushDir.y = -e.motionY - f + v.y;
					movedy = true;

					if (e instanceof EntityLiving && collisionDamage) {
						int damage = Math.max(
								(int) ((e.motionY + v.y + 0.15) * (e.motionY
										+ v.y + 0.15)), 0);
						// System.out.println(damage+" "+(-e.motionY+pusher.motionY)+" "+pusher.motionY);
						if (damage > 0)
							((EntityLiving) e).attackEntityFrom(
									DamageSource.fall, damage);
					}
					// System.out.println("pushed-y");
				}

				if (!movedy) {
					if (xnegative) {
						pushDir.x = -f;
						pushLoc.x = min.x;
						// System.out.println("pushed1"+location.toString());
						movedx = true;
					} else if (xpositive) {
						pushDir.x = f;
						pushLoc.x = max.x;
						// System.out.println("pushed2"+location.toString());
						movedx = true;
					}
					if (znegative) {
						pushDir.z = -f;
						pushLoc.x = min.z;
						// System.out.println("pushed3"+location.toString());
						movedz = true;
					} else if (zpositive) {
						pushDir.z = f;
						pushLoc.x = max.z;
						// System.out.println("pushed4"+location.toString());
						movedz = true;
					}
				}

			}

			if (rot) {
				pushDir = pushDir.rotateAboutAngles(-rotation.y, -rotation.z);
				pushLoc = pushLoc.rotateAboutAngles(-rotation.y, -rotation.z);
			}
			// System.out.println(pushLoc.toString()+" "+toString());
			if (movedy || movedx || movedz) {
				// System.out.println("pushDir"+pushDir);
				if (pushDir.y != 0 && !Double.isNaN(pushDir.y)) {
					e.motionY = pushDir.y;

				}
				if (pushDir.x != 0 && !Double.isNaN(pushDir.x)) {
					e.motionX = pushDir.x;
					if (pushLoc.x != 0 && !pushLoc.isNaN()) {
						// e.setPosition(pusher.posX + pushLoc.x, e.posY +
						// pushLoc.y, e.posZ + pushLoc.z);
					}
				}
				if (pushDir.z != 0 && !Double.isNaN(pushDir.z)) {
					e.motionZ = pushDir.z;
					if (pushLoc.x != 0 && !pushLoc.isNaN()) {
						// e.setPosition(e.posX + pushLoc.x, e.posY + pushLoc.y,
						// pusher.posZ + pushLoc.z);
					}

				}
			}
			if (e instanceof EntityItem) {
				if (movedy) {
					e.motionX = 0;
					e.motionZ = 0;
				}
			}
			return !pushDir.isNaN() ? pushDir : new Vector3();
		}

		public static boolean multiBoxIntersect(Entity pusher, Entity e,
				Vector3 offset, Vector3 intersect) {
			boolean ret = false;
			if (!(pusher instanceof IMultiBox && e instanceof IMultiBox))
				return ret;

			Vector3 entity = new Vector3(e, true);
			Vector3 push = new Vector3(pusher, true);

			for (String pusherKey : ((IMultiBox) pusher).getBoxes().keySet()) {
				for (String eKey : ((IMultiBox) e).getBoxes().keySet()) {
					Vector3 pushOffset = ((IMultiBox) pusher).getOffsets()
							.contains(pusherKey) ? ((IMultiBox) pusher)
							.getOffsets().get(pusherKey) : new Vector3();
					Vector3 eOffset = ((IMultiBox) e).getOffsets().contains(
							eKey) ? ((IMultiBox) e).getOffsets().get(eKey)
							: new Vector3();

					Matrix3 pushBox = ((IMultiBox) pusher).getBoxes()
							.get(pusherKey).addOffset(pushOffset)
							.addOffset(push);
					Matrix3 eBox = ((IMultiBox) e).getBoxes().get(eKey)
							.addOffset(eOffset).addOffset(entity);

					ret = pushBox.intersects(eBox);

					if (ret) {
						intersect = pushBox.intersect(eBox).subtract(entity);
						break;
					}
				}
				if (ret)
					break;
			}

			return ret;
		}

		public boolean intersects(Matrix3 otherBox) {
			boolean ret = false;
			Vector3[] corners = otherBox.corners();
			Vector3.rotateAboutAngles(corners, boxRotation());
			for (Vector3 r : corners) {
				if (r.inMatBox(this)) {
					ret = true;
					break;
				}
			}
			return ret;
		}

		public Vector3 intersect(Matrix3 otherBox) {
			Vector3 ret = new Vector3();
			Vector3[] corners = otherBox.corners();
			Vector3.rotateAboutAngles(corners, boxRotation());
			for (Vector3 r : corners) {
				if (r.inMatBox(this)) {
					ret = r;
					break;
				}
			}
			return ret;
		}

		public Vector3 getMatrixPushDir(Matrix3 box) {
			Vector3 ret = new Vector3();

			return ret;
		}

		public Vector3 getPushDirOutOfBlocks(Entity e, Vector3 offset) {
			Vector3 dir = new Vector3();

			return dir;
		}

		public void pushEntityOutOfAABB(Matrix3 aabb, Entity e, Vector3 source) {
			double xMin = aabb.boxMin().x;
			double xMax = aabb.boxMax().x;

			double yMin = aabb.boxMin().y;
			double yMax = aabb.boxMax().y;

			double zMin = aabb.boxMin().z;
			double zMax = aabb.boxMax().z;

			Vector3 r = new Vector3(e);

			double posxOffset = e.width / 2;
			double negxOffset = -e.width / 2;
			double posyOffset = 0;
			double negyOffset = 0;
			double poszOffset = e.width / 2;
			double negzOffset = -e.width / 2;

			if (!(r.inMatBox(this)
					|| r.add(new Vector3(posxOffset, 0, 0)).inMatBox(this)
					|| r.add(new Vector3(negxOffset, 0, 0)).inMatBox(this)

					|| r.add(new Vector3(0, 0, poszOffset)).inMatBox(this)
					|| r.add(new Vector3(0, 0, negzOffset)).inMatBox(this)

					|| r.add(new Vector3(0, posyOffset, 0)).inMatBox(this) || r
					.add(new Vector3(0, negyOffset, 0)).inMatBox(this)

			))

			{
				return;
			}

			Vector3 mid = new Vector3();
			mid.x = (xMin - xMax) / 2;
			mid.y = (yMin - yMax) / 2;
			mid.z = (zMin - zMax) / 2;
			source = source.add(mid);
			Vector3 finalPos = new Vector3();

			double xDiff = Math.abs((r.x - source.x) / aabb.boxXLength());
			double yDiff = Math.abs((r.y - source.y) / aabb.boxYLength());
			double zDiff = Math.abs((r.z - source.z) / aabb.boxZLength());

			if (r.x < source.x && xDiff > zDiff) {
				finalPos.x = source.x - xMin - 0.05;
			}
			if (r.y < source.y) {
				finalPos.y = source.y - yMin - 0.05;
			}

			if (r.z < source.z && zDiff > xDiff) {
				finalPos.z = source.z - zMin - 0.05;
			}

			if (r.x > source.x && xDiff > zDiff) {
				finalPos.x = source.x + xMax + 0.05;
			}
			if (r.y > source.y) {
				finalPos.y = source.y + yMax + 0.05;
			}

			if (r.z > source.z && zDiff > xDiff) {
				finalPos.z = source.z + zMax + 0.05;
			}
			finalPos.moveEntity(e);

		}

	}

	public static Map<String, Fluid> fluids;

	/** TODO fix this once forge fluids work again
	 * Whether or not a certain block is considered a fluid.
	 * 
	 * @param world
	 *            - world the block is in
	 * @return if the block is a liquid
	 */
	public boolean isFluid(World world) {
		boolean ret = false;
		int id = getBlockId(world);
		int meta = getBlockMetadata(world);

		boolean fluidCheck = false;

		if (fluids == null)
			fluids = FluidRegistry.getRegisteredFluids();

//		for (String s : fluids.keySet()) {
//			fluidCheck = fluids.get(s).getBlockID() == id
//					&& fluids.get(s).getViscosity() != Integer.MAX_VALUE;
//			if (fluidCheck)
//				return true;
//		}
//
//		ret = fluidCheck || id == Block.waterMoving.blockID
//				|| id == Block.lavaMoving.blockID;

		return ret;
	}

	public void clear() {
		this.set(new Vector3());
	}

	// /**
	// * Gets a liquid from a certain location.
	// * @param world - world the block is in
	// * @param x - x coordinate
	// * @param y - y coordinate
	// * @param z - z coordinate
	// * @return the liquid at the certain location, null if it doesn't exist
	// */
	// public static synchronized LiquidStack getFluid(World world, int x, int
	// y, int z)
	// {
	// int id = world.getBlockId(x, y, z);
	// int meta = world.getBlockMetadata(x, y, z);
	//
	// if(id == 0)
	// {
	// return null;
	// }
	//
	// if((id == Block.waterStill.blockID || id == Block.waterMoving.blockID) &&
	// meta == 0)
	// {
	// return new LiquidStack(Block.waterStill.blockID,
	// LiquidContainerRegistry.BUCKET_VOLUME, 0);
	// }
	// else if((id == Block.lavaStill.blockID || id == Block.lavaMoving.blockID)
	// && meta == 0)
	// {
	// return new LiquidStack(Block.lavaStill.blockID,
	// LiquidContainerRegistry.BUCKET_VOLUME, 0);
	// }
	// else if(Block.blocksList[id] instanceof ILiquid)
	// {
	// ILiquid liquid = (ILiquid)Block.blocksList[id];
	//
	// if(liquid.isMetaSensitive())
	// {
	// return new LiquidStack(liquid.stillLiquidId(),
	// LiquidContainerRegistry.BUCKET_VOLUME, liquid.stillLiquidMeta());
	// }
	// else if(meta == 0)
	// {
	// return new LiquidStack(liquid.stillLiquidId(),
	// LiquidContainerRegistry.BUCKET_VOLUME, 0);
	// }
	// }
	//
	// return null;
	// }

	public Block getBlock(IBlockAccess worldObj) {
		return worldObj.getBlock(intX(), intY(), intZ());
	}

	public Block getBlock(IBlockAccess worldObj, ForgeDirection side) {
		return worldObj.getBlock(intX() + side.offsetX,
				intY() + side.offsetY, intZ() + side.offsetZ);
	}

	public int getBlockId(IBlockAccess worldObj) {
		return Block.getIdFromBlock(getBlock(worldObj));
	}

	public int getBlockId(IBlockAccess worldObj, ForgeDirection side) {
		return Block.getIdFromBlock(worldObj.getBlock(intX() + side.offsetX,
				intY() + side.offsetY, intZ() + side.offsetZ));
	}

	public int getBlockMetadata(IBlockAccess worldObj, ForgeDirection side) {
		return worldObj.getBlockMetadata(intX() + side.offsetX, intY()
				+ side.offsetY, intZ() + side.offsetZ);
	}

	public int getBlockMetadata(IBlockAccess worldObj) {
		return worldObj.getBlockMetadata(intX(), intY(), intZ());
	}

	public Material getBlockMaterial(IBlockAccess worldObj) {
		return getBlock(worldObj).getMaterial();
	}

	public boolean isAir(IBlockAccess worldObj) {
		if (worldObj instanceof World) {
			return getBlock(worldObj) == null
					|| getBlockMaterial(worldObj) == null
					|| getBlock(worldObj).isAir((World) worldObj, intX(),
							intY(), intZ())
					|| getBlockMaterial(worldObj) == Material.air;// ||worldObj.isAirBlock(intX(),
																	// intY(),
																	// intZ())
		}
		return getBlockMaterial(worldObj) == null
				|| getBlockMaterial(worldObj) == Material.air;// ||worldObj.isAirBlock(intX(),
																// intY(),
																// intZ())
	}
	

	public TileEntity getTileEntity(IBlockAccess worldObj) {
		return worldObj.getTileEntity(intX(), intY(), intZ());
	}

	public TileEntity getTileEntity(IBlockAccess worldObj, ForgeDirection side) {
		return worldObj.getTileEntity(intX() + side.offsetX, intY()
				+ side.offsetY, intZ() + side.offsetZ);
	}

	public void scheduleUpdate(World worldObj) {
		if (getBlock(worldObj) != null)
			worldObj.scheduleBlockUpdate(intX(), intY(), intZ(),
					getBlock(worldObj), getBlock(worldObj).tickRate(worldObj));
	}

	public void breakBlock(World worldObj, boolean drop) {
		if (getBlock(worldObj) != null)
			worldObj.func_147480_a(intX(), intY(), intZ(), drop);
	}

	public void breakBlock(World worldObj, int fortune, boolean drop) {
		if (getBlock(worldObj) != null)
		{
			if(drop)
				getBlock(worldObj).dropBlockAsItem(worldObj, intX(), intY(), intZ(), getBlockMetadata(worldObj), fortune);
			worldObj.func_147480_a(intX(), intY(), intZ(), false);
		}
	}

	public boolean isBlockOnSideSolid(IBlockAccess worldObj, ForgeDirection side) {
		boolean ret = worldObj.isSideSolid(intX() + side.offsetX, intY()
				+ side.offsetY, intZ() + side.offsetZ, side.getOpposite(),
				false);
		return ret;
	}

	public List<Entity> firstEntityLocationExcluding(int range, double size,
			Vector3 direction, Vector3 source, World worldObj, Entity excluded) {
		direction = vectorNormalize(direction);
		int n = 0;
		double xprev = source.x, yprev = source.y, zprev = source.z;
		double dx, dy, dz;
		double blocked = 0;

		Vector3 temp = new Vector3();

		for (double i = 0; i < range; i += 0.0625) {
			dx = i * direction.x;
			dy = i * direction.y;
			dz = i * direction.z;

			double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

			boolean check = isPointClearBlocks(xtest, ytest, ztest, worldObj);
			blocked = Math.sqrt(dx * dx + dy * dy + dz * dz);

			if (!check) {
				break;
			}

			// if(!((int)xtest==(int)xprev&&(int)ytest==(int)yprev&&(int)ztest==(int)zprev))
			{
				double x0 = (xtest > 0 ? (int) xtest : (int) xtest - 1), y0 = (ytest > 0 ? (int) ytest
						: (int) ytest - 1), z0 = (ztest > 0 ? (int) ztest
						: (int) ztest - 1);
				List<Entity> targets = worldObj
						.getEntitiesWithinAABBExcludingEntity(excluded,
								AxisAlignedBB.getBoundingBox(x0 - size, y0
										- size, z0 - size, x0 + size,
										y0 + size, z0 + size));
				if (targets != null && targets.size() > 0) {
					List<Entity> ret = new ArrayList<Entity>();
					for (Entity e : targets) {
						if (e instanceof EntityLiving) {
							ret.add(e);
						}
					}
					if (ret != null && ret.size() > 0)
						return ret;
				}
			}
			yprev = ytest;
			xprev = xtest;
			zprev = ztest;
		}

		return null;
	}

	public List<Entity> allEntityLocationExcluding(int range, double size,
			Vector3 direction, Vector3 source, World worldObj, Entity excluded) {
		direction = vectorNormalize(direction);
		int n = 0;
		double dx, dy, dz;
		List<Entity> ret = new ArrayList<Entity>();
		Vector3 temp = new Vector3();

		for (double i = 0; i < range; i += 0.0625) {
			dx = i * direction.x;
			dy = i * direction.y;
			dz = i * direction.z;

			double xtest = (source.x + dx), ytest = (source.y + dy), ztest = (source.z + dz);

			boolean check = isPointClearBlocks(xtest, ytest, ztest, worldObj);

			if (!check) {
				//System.out.println("not clear"+new Vector3(xtest, ytest, ztest).getBlock(worldObj));
				break;
			}

			// if(!((int)xtest==(int)xprev&&(int)ytest==(int)yprev&&(int)ztest==(int)zprev))
			{
				double x0 = xtest, y0 = ytest, z0 = ztest;
				List<Entity> targets = worldObj
						.getEntitiesWithinAABBExcludingEntity(excluded,
								AxisAlignedBB.getBoundingBox(x0 - size, y0
										- size, z0 - size, x0 + size,
										y0 + size, z0 + size));
				if (targets != null && targets.size() > 0) {
					for (Entity e : targets) {
						if (e instanceof EntityLivingBase && !ret.contains(e) && e!=excluded.riddenByEntity) {
								ret.add(e);
						}
					}
				}
			}
		}

		return ret;
	}

	public AxisAlignedBB getAABB() {
		return AxisAlignedBB.getBoundingBox(x, y, z, x, y, z);
	}
	

    public void setBiome(BiomeGenBase biome, World worldObj)
    {
       int x = intX();
       int z = intZ();
            
      Chunk chunk = worldObj.getChunkFromBlockCoords(x, z);
      byte[] biomes = chunk.getBiomeArray();
      
      byte newBiome = (byte) biome.biomeID;
      
      int chunkX = Math.abs(x&15);
      int chunkZ = Math.abs(z&15);
      
      int point = chunkX+16*chunkZ;
      
      if(biomes[point]!= newBiome)
      {
              biomes[point] = newBiome;
              chunk.setBiomeArray(biomes);
              chunk.setChunkModified();
      }
    }
    
    public byte getBiomeID(World worldObj)
    {
         int x = intX();
         int z = intZ();
        
          Chunk chunk = worldObj.getChunkFromBlockCoords(x, z);
          byte[] biomes = chunk.getBiomeArray();
          
          int chunkX = Math.abs(x&15);
          int chunkZ = Math.abs(z&15);
          
          int point = chunkX + 16*chunkZ;
          
          return biomes[point];
    }
    
    public BiomeGenBase getBiome(World worldObj)
    {
	      return worldObj.getBiomeGenForCoords(intX(), intZ());
    }
    
    /**
     * Gets the Average height starting at the corner specified by the x and z values, and increasing from there, to the maximum range.
     * @param world
     * @param range
     * @return
     */
	public int getAverageHeight(World world, int range)
	{
		int y = 0;
		int minY = 255;
		int count = 0;
		for(int i = 0; i<range; i++)
			for(int j = 0; j<range; j++)
			{
				if(getMaxY(world, intX()+i, intZ()+j)<minY)
					minY = getMaxY(world, intX()+i, intZ()+j);
			}
		
		for(int i = 0; i<9; i++)
			for(int j = 0; j<9; j++)
			{
				y += getMaxY(world, intX()+i, intZ()+j);
				count++;
			}
		y /= count;
		if((minY < y - 5)&&!(minY < y - 10))
			y = minY;
		return y;
	}
	
	public double getAverageSlope(World world, int range)
	{
		double slope = 0;
		
		double prevY = getMaxY(world);
		
		double dy = 0;
		double dz = 0;
		
		int count = 0;
		for(int i = -range; i<=range; i++)
		{
			dz = 0;
			for(int j = -range; j<=range; j++)
			{
				dy += Math.abs((getMaxY(world, intX()+i, intZ()+j) - prevY));
				dz++;
				count++;
			}
			slope += (dy/dz);
		}
		
		return slope/count;
	}

	
	public double getAverageSlope2(World world, int range)
	{
		double slope = 0;
		
		double prevY = getMaxY(world);
		
		double dy = 0;
		double dz = 0;
		
		int count = 0;
		for(int i = 0; i<range; i++)
		{
			dz = 0;
			for(int j = 0; j<range; j++)
			{
				dy += Math.abs((getMaxY(world, intX()+i, intZ()+j) - prevY));
				dz++;
				count++;
			}
			slope += (dy/dz);
		}
		
		return slope/count;
	}
	
	public int[] getMinMaxY(World world, int range)
	{
		int[] ret = new int[2];
		
		int minY = 255;
		int maxY = 0;
		int count = 0;
		for(int i = 0; i<range; i++)
			for(int j = 0; j<range; j++)
			{
				if(getMaxY(world, intX()+i, intZ()+j)<minY)
					minY = getMaxY(world, intX()+i, intZ()+j);
				if(getMaxY(world, intX()+i, intZ()+j)>maxY)
					maxY = getMaxY(world, intX()+i, intZ()+j);
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
		Vector3 temp = new Vector3(x,y,z);
		int y = temp.getTopBlockY(world);
		
		if(Int(y)==intY()) return y;
		
		while (isNotSurfaceBlock(world, temp))
		{
			y--;
			temp.y = y;
		}
		return y;
	}
	
	public boolean canSeeSky(World world)
	{
		return getTopBlockY(world)<=y;
	}
	
	public boolean isOnSurface(World world)
	{
		return getMaxY(world)<=y;
	}
	
	public boolean canSeeSky(World world, int range)
	{
		return getMinMaxY(world, range)[0]<y;
	}
	
	public boolean isLeaves(World world)
	{
		if(getBlock(world)!=null)
		return getBlock(world).isLeaves(world, intX(), intY(), intZ());
		return false;
	}
	
	public boolean isWood(World world)
	{
		if(getBlock(world)!=null)
		return getBlock(world).isWood(world, intX(), intY(), intZ());
		return false;
	}
	
	public int blockCount(World world, Block block, int range)
	{
		int ret = 0;
		for(int i = -range; i<=range; i++)
			for(int j = -range; j<=range; j++)
				for(int k = -range; k<=range; k++)
				{
					Vector3 test = this.add(new Vector3(i,j,k));
					if(test.getBlock(world) == block)
					{
						ret++;
					}
				}
		
		return ret;
	}
	
	public int blockCount2(World world, Block block, int range)
	{
		int ret = 0;
		for(int i = 0; i<range; i++)
			for(int j = 0; j<range; j++)
				for(int k = 0; k<range; k++)
				{
					Vector3 test = this.add(new Vector3(i,j,k));
					if(test.getBlock(world) == block)
					{
						ret++;
					}
				}
		
		return ret;
	}
	
	public static boolean isNotSurfaceBlock(World world, Vector3 v)
	{
		Block b = v.getBlock(world);
		boolean ret = (b==null
				||v.getBlockMaterial(world).isReplaceable()
				||v.isClearOfBlocks(world)
				||!b.isNormalCube()
				||b.isLeaves(world, v.intX(), v.intY(), v.intZ())
				||b.isWood(world, v.intX(), v.intY(), v.intZ()))&&v.y>1;
		
		return ret;
	}
	
	public Vector3 add(double i, double j, double k)
	{
		return new Vector3(x+i, j+y, k+z);
	}
	
	public int getLightValue(World world)
	{
		return world.getBlockLightValue(intX(), intY(), intZ());
	}
}