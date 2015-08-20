package thut.api.maths;

import static java.lang.Math.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import thut.api.entity.IMultibox;


public class Matrix3 {
	
	public Vector3[] rows = new Vector3[3];
	private List<Vector3> corners = new ArrayList();

	private static AxisAlignedBB[] pool = new AxisAlignedBB[10000];
	private static int index = 0;
	
	public static synchronized AxisAlignedBB getAABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
	{
		if(index > 0)
		{
			AxisAlignedBB ret = pool[index-1];
			index--;
			if(ret!=null)
				return ret.setBounds(minX, minY, minZ, maxX, maxY, maxZ);
		}
		
		return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	public static synchronized void freeAABB(AxisAlignedBB box)
	{
		if(index < pool.length - 1 && index >= 0)
		{
			pool[index] = box;
			index++;
		}
	}

	private Vector3 temp1;// = Vector3.getVector();
	private Vector3 temp;// =Vector3.getVector();
	private Vector3 ent;// = Vector3.getVector();
	private Vector3 dir;// = Vector3.getVector();
	private Vector3 temp2;// = Vector3.getVector();
	private Vector3 mid = Vector3.getNewVectorFromPool();
	private Matrix3 box;
	
	int size = 3;

	public Matrix3() {
		rows[0] = Vector3.getNewVectorFromPool();
		rows[1] = Vector3.getNewVectorFromPool();
		rows[2] = Vector3.getNewVectorFromPool();
//		for(int i = 0; i< 8; i++)
//			corners[i] = Vector3.getVector();
	}

	public Matrix3(double[] a, double[] b, double[] c) {
		this();
		rows[0].set(a[0], a[1], a[2]);
		rows[1].set(b[0], b[1], b[2]);
		rows[2].set(c[0], c[1], c[2]);
//		for(int i = 0; i< 8; i++)
//			corners[i] = Vector3.getVector();
	}

	public Matrix3(Vector3 a, Vector3 b, Vector3 c) {
		rows[0] = a.copy();
		rows[1] = b.copy();
		rows[2] = c.copy();
//		for(int i = 0; i< 8; i++)
//			corners[i] = Vector3.getVector();
	}

	public Matrix3(Vector3 a, Vector3 b) {
		this(a, b, Vector3.empty);
	}

	public Matrix3(double d, double e, double f) {
		this();
		rows[1].set(d, e, f);
	}

	public Vector3 get(int i) {
		assert (i < 3);
		return rows[i];
	}

	public Vector3 boxMin() {
		return rows[0];
	}

	public Vector3 boxMax() {
		return rows[1];
	}

	public Vector3 boxRotation() {
		return rows[2];
	}
	
	public Vector3 boxCentre() {
		if(temp==null)
			temp = Vector3.getNewVectorFromPool();
		if(temp1==null)
			temp1 = Vector3.getNewVectorFromPool();
		if(temp2==null)
			temp2 = Vector3.getNewVectorFromPool();
		temp.set(temp1);
		temp1.set(boxMax());
		temp2.set(temp1);
		mid.set(temp2.subtractFrom((temp1.subtractFrom(boxMin())).scalarMultBy(0.5)));
		temp1.set(temp);
		
		return mid;
	}

	public double get(int i, int j) {
		assert (i < 3);
		return rows[i].get(j);
	}

	/**
	 * 0 = min, min, min;
	 * 1 = max, max, max;
	 * 2 = min, min, max;
	 * 3 = min, max, min;
	 * 4 = max, min, min;
	 * 5 = min, max, max;
	 * 6 = max, min, max;
	 * 7 = max. max, min;
	 * @return
	 */
	public Vector3[] corners(Vector3 mid) {
		corners(mid!=null);
		return corners.toArray((new Vector3[8]));
	}

	private void corners(boolean rotate)
	{
		if(corners.isEmpty())
		{
			for(int i = 0; i< 8; i++)
				corners.add(Vector3.getNewVectorFromPool());
		}
		if(temp==null)
			temp = Vector3.getNewVectorFromPool();
		if(temp2==null)
			temp2 = Vector3.getNewVectorFromPool();
		corners.get(0).set(boxMin());
		corners.get(1).set(boxMax());

		corners.get(2).set(boxMin().x, boxMin().y, boxMax().z);
		corners.get(3).set(boxMin().x, boxMax().y, boxMin().z);
		corners.get(4).set(boxMax().x, boxMin().y, boxMin().z);

		corners.get(5).set(boxMin().x, boxMax().y, boxMax().z);
		corners.get(6).set(boxMax().x, boxMin().y, boxMax().z);
		corners.get(7).set(boxMax().x, boxMax().y, boxMin().z);

		if(rotate)
			mid = boxCentre();
		else
			mid = null;
		if (!boxRotation().isEmpty() && mid!=null) {
			for(int i = 0; i< 8; i++)
				corners.get(i).subtractFrom(mid);
			Vector3.rotateAboutAngles(corners.toArray(new Vector3[8]), boxRotation().y,
					boxRotation().z, temp2, temp);
			
			for(int i = 0; i< 8; i++)
			{
				corners.get(i).addTo(mid);
			}
		}
	}
	
	public Matrix3 addOffsetTo(Vector3 pushOffset) {
		rows[0].addTo(pushOffset);
		rows[1].addTo(pushOffset);
		return this;
	}

	public Matrix3 copy() {
		Matrix3 ret = new Matrix3();
		ret.rows[0].set(rows[0]);
		ret.rows[1].set(rows[1]);
		ret.rows[2].set(rows[2]);
		return ret;
	}

	public Matrix3 set(int i, Vector3 j) {
		assert (i < 3);
		rows[i] = j;
		return this;
	}

	public void set(int i, int j, double k) {
		rows[i].set(j, k);
	}

	public double[][] toArray() {
		return new double[][] { 
				{rows[0].x,rows[0].y,rows[0].z}, 
				{rows[1].x,rows[1].y,rows[1].z},
				{rows[2].x,rows[2].y,rows[2].z} 
				};
	}

	public String toString() {
		String eol = System.getProperty("line.separator");
		return eol + "0: " + rows[0].toString() + eol + "1: "
				+ rows[1].toString() + eol + "2 : " + rows[2].toString();
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
	
	public Matrix3 getOctant(int octant)
	{
		Matrix3 ret = copy();
		switch(octant)
		{
		case 0:
			ret.rows[0].x = rows[0].x + rows[1].x/2;
			ret.rows[0].y = rows[0].y + rows[1].y/2;
			ret.rows[0].z = rows[0].z + rows[1].z/2;
			return ret;
		case 1:
			ret.rows[1].x = rows[1].x - rows[1].x/2;
			ret.rows[0].y = rows[0].y + rows[1].y/2;
			ret.rows[0].z = rows[0].z + rows[1].z/2;
			return ret;
		case 2:
			ret.rows[1].x = rows[1].x - rows[1].x/2;
			ret.rows[1].y = rows[1].y - rows[1].y/2;
			ret.rows[0].z = rows[0].z + rows[1].z/2;
			return ret;
		case 3:
			ret.rows[0].x = rows[0].x + rows[1].x/2;
			ret.rows[1].y = rows[1].y - rows[1].y/2;
			ret.rows[0].z = rows[0].z + rows[1].z/2;
			return ret;
		case 4:
			ret.rows[0].x = rows[0].x + rows[1].x/2;
			ret.rows[0].y = rows[0].y + rows[1].y/2;
			ret.rows[1].z = rows[1].z - rows[1].z/2;
			return ret;
		case 5:
			ret.rows[1].x = rows[1].x - rows[1].x/2;
			ret.rows[0].y = rows[0].y + rows[1].y/2;
			ret.rows[1].z = rows[1].z - rows[1].z/2;
			return ret;
		case 6:
			ret.rows[1].x = rows[1].x - rows[1].x/2;
			ret.rows[1].y = rows[1].y - rows[1].y/2;
			ret.rows[1].z = rows[1].z - rows[1].z/2;
			return ret;
		case 7:
			ret.rows[0].x = rows[0].x + rows[1].x/2;
			ret.rows[1].y = rows[1].y - rows[1].y/2;
			ret.rows[1].z = rows[1].z - rows[1].z/2;
			return ret;
		}
		
		return ret;
	}
	
	public Matrix3 resizeBox(double x, double y, double z)
	{
		Matrix3 ret = copy();

		ret.boxMin().x -= x;
		ret.boxMin().y -= y;
		ret.boxMin().z -= z;
		
		ret.boxMax().x += x;
		ret.boxMax().y += y;
		ret.boxMax().z += z;
		
		return ret;
	}
	
	List<Vector3> cornersList = new ArrayList();

	public Vector3 doTileCollision(IBlockAccess world, Entity e, Vector3 offset, Vector3 diffs) {//TODO finally get this working
		if(temp==null)
			temp = Vector3.getNewVectorFromPool();
		if(dir==null)
			dir = Vector3.getNewVectorFromPool();
		if(temp1==null)
			temp1 = Vector3.getNewVectorFromPool();
		if(ent==null)
			ent = Vector3.getNewVectorFromPool();
		cornersList.clear();
		ent.set(e);
		temp.set(ent).addTo(diffs).addTo(offset);		
		temp1.set(diffs);
		if(box==null)
			box = copy();
		else
		{
			box.clear();
			box.set(this);
		}
		Matrix3 mob = box.addOffsetTo(temp);
		
		int n = 0;
		e.onGround = false;

		for(Vector3 v: mob.corners(mob.boxCentre()))
			cornersList.add(v);

		double minX = Double.MAX_VALUE, minZ = Double.MAX_VALUE, minY = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
		
		for(Vector3 v: cornersList)
		{
			if(v.x > maxX)
				maxX = v.x;
			if(v.y > maxY)
				maxY = v.y;
			if(v.z > maxZ)
				maxZ = v.z;
			if(v.x < minX)
				minX = v.x;
			if(v.y < minY)
				minY = v.y;
			if(v.z < minZ)
				minZ = v.z;
		}
		double dx = max(maxX - minX, 1), dy = max(maxY - minY, 1), dz = max(maxZ - minZ, 1);

		AxisAlignedBB aabb = mob.boxCentre().getAABB().expand(dx, dy, dz);
		
		List list = e.worldObj.func_147461_a(aabb);

		freeAABB(aabb);
		List<AxisAlignedBB> aabbs = list;
		double[] corners = {minY, maxY, minZ, maxZ, minX, maxX};
		double[] pens = {Double.MAX_VALUE,Double.MAX_VALUE,
				Double.MAX_VALUE,Double.MAX_VALUE,
				Double.MAX_VALUE,Double.MAX_VALUE};
		
		boolean justDown = true;

		double minCeiling = Integer.MAX_VALUE;
		double maxFloor = -1;//MathHelper.floor_double(e.posY);
		boolean[] dirs = new boolean[6];

		for(AxisAlignedBB box: aabbs)
		{
			if(!doAABBcollision(box, corners, pens, dirs, diffs))
				continue;
			if(dirs[0])
			{
				boolean downForBlock = (diffs.y < 0 && minY >= box.minY);
				justDown = justDown && downForBlock;
				e.onGround = true;
				if(downForBlock || ((box.maxY - e.posY) <= e.stepHeight))
				{
					maxFloor = Math.max(maxFloor, box.maxY);
				}
			}
			if(dirs[1])
			{
				minCeiling = Math.min(minCeiling, box.minY);
			}
			n++;
		}
		
		for(int i = 0; i<6; i++)
		{
			if(pens[i]==Double.MAX_VALUE)
				pens[i] = 0;
		}
		//If not only colliding with a floor or cieling.
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)//Hopefully this fixes some of the bouncing that occurs
		{
			justDown = dirs[0] && !dirs[1] && !dirs[2] && !dirs[3] && !dirs[4] && !dirs[5];
		}
		dir.clear();
		boolean test = false;
		if(!justDown)
		{
			
			for(int i = 1; i<6; i++)
			{
//				if(!dirs[i] || abs(pens[i]) < 0.01)
//					continue;
				dir.set(EnumFacing.getFront(i)).scalarMultBy(-0.01).scalarMultBy(pens[i]);
				
				if(abs(dir.x) >0)
				{
					temp1.x = dir.x;
				}
				if(abs(dir.z) >0)
				{
					temp1.z = dir.z;
				}
				if(abs(dir.y) >0)
				{
					temp1.y = dir.y;
				}
			}
		}
		if(dirs[2]||dirs[3]||dirs[4]||dirs[5])
		{
//			System.out.println(Arrays.toString(pens));
//			System.out.println(Arrays.toString(dirs));
		}
		
		
		
		if(dirs[1])
		{
			temp1.y = -pens[1];
		}
		if(dirs[0] && !(dirs[1] && pens[0] > 0.5))
		{
			test = true;
			temp1.y =  maxFloor - e.posY;
		}
		
		return temp1;
	}

	public void set(Matrix3 box) {
		rows[0].set(box.rows[0]);
		rows[1].set(box.rows[1]);
		rows[2].set(box.rows[2]);
	}

	public boolean doTileCollision(IBlockAccess world, Vector3 location, Entity e, Vector3 offset, Vector3 diffs) {
		if(!(e instanceof IMultibox))
			return false;
		return !doTileCollision(world, e, offset, diffs).equals(diffs);
	}

	public boolean isInMaterial(IBlockAccess world, Vector3 location, Entity e, Vector3 offset, Material m) {
		if(!(e instanceof IMultibox))
			return false;
		boolean ret = false;
		Vector3 ent = location;
		if(temp==null)
			temp = Vector3.getNewVectorFromPool();
		else
			temp.clear();
		if(dir==null)
			dir = Vector3.getNewVectorFromPool();
		else
			dir.clear();
		if(temp1==null)
			temp1 = Vector3.getNewVectorFromPool();
		else
			temp1.clear();
		Vector3[] corners = corners(boxCentre());
		
		for(int i = 0; i<8; i++)
		{
			Vector3 v = corners[i];
			dir.set(v);
			temp.set(dir.addTo(ent).addTo(offset));
			if(temp.getBlockMaterial(world)==m)
			{
				return true;
			}
			if(i%2==0)
			{
				temp.addTo(0, 0.01, 0);
				if(temp.getBlockMaterial(world)==m)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param box
	 * @param corners the extreme corners of this box
	 * @param pensthe array to store the penetration distances in
	 * @param dirs the array to store the penetration occurances in
	 * @param diffs the velocity of the collider
	 * @return if collides
	 */
	public boolean doAABBcollision(AxisAlignedBB box, double[] corners, double[] pens, boolean[] dirs, Vector3 diffs)
	{
		double minX = corners[4], minZ = corners[2], minY = corners[0];
		double maxX = corners[5], maxZ = corners[3], maxY = corners[1];
		boolean collidesX = ((maxZ <= box.maxZ) && (maxZ >= box.minZ))
				||((minZ <= box.maxZ) && (minZ >= box.minZ));

		boolean collidesY = ((minY >= box.minY) && (minY <= box.maxY)) 
						|| ((maxY <= box.maxY) && (maxY >= box.minY));
		
		boolean collidesZ = ((maxX <= box.maxX) && (maxX >= box.minX))
						||((minX <= box.maxX) && (minX >= box.minX));
		
		collidesZ = collidesZ && (collidesX || collidesY);
		collidesX = collidesX && (collidesZ || collidesY);
		
		boolean in = collidesZ && collidesX;
		
		if (!in) {
			return false;
		}
		
		if (collidesY) {
			if  ((minY >= box.minY) && (minY <= box.maxY))
			{
				pens[0] = Math.min(pens[0], abs(box.maxY - minY));
				dirs[0] = true;
			} else if ((maxY <= box.maxY) && (maxY >= box.minY))
			{
				pens[1] = Math.min(pens[1], abs(box.minY - maxY));
				dirs[1] = true;
			}
		}

		if (collidesZ) {
			if (diffs.z < 0) {
				pens[2] = Math.min(pens[2], abs(box.minZ - minZ));
				dirs[2] = true;
			} else if (diffs.z > 0) {
				pens[3] = Math.min(pens[3], abs(box.maxZ - maxZ));
				dirs[3] = true;
			}
		}
		if (collidesX) {
			if (diffs.x < 0) {
				pens[4] = Math.min(pens[4], abs(box.maxX - maxX));
				dirs[4] = true;
			} else if (diffs.x > 0) {
				pens[5] = Math.min(pens[5], abs(box.maxX - maxX));
				dirs[5] = true;
			}
		}
		return true;
	}

	public boolean doCollision(Vector3 boxVelocity, Entity e) {
		boolean ret = false;
		if(e==null)
			return false;
		if(dir==null)
			dir = Vector3.getNewVectorFromPool();
		if(ent==null)
			ent = Vector3.getNewVectorFromPool();
		if(temp2==null)
			temp2 = Vector3.getNewVectorFromPool();
		ent.set(e);
		corners(true);
		if(e instanceof IMultibox)
		{
			IMultibox e1 = (IMultibox) e;
			e1.setOffsets();
			e1.setBoxes();
			Map<String, Matrix3> boxes = e1.getBoxes();
			Map<String, Vector3> offsets = e1.getOffsets();
			for(String s: boxes.keySet())
			{
				Vector3 boxOff = offsets.containsKey(s)?offsets.get(s):Vector3.empty;
				Matrix3 box = boxes.get(s);
				box.addOffsetTo(boxOff).addOffsetTo(ent);
				boolean hit = box.intersects(this);
				box.addOffsetTo(boxOff.reverse()).addOffsetTo(ent.reverse());
				boxOff.reverse();
				ent.reverse();
				Vector3.empty.clear();
				if(hit)
				{
					return true;
				}
			}
			
		}
		else
		{
			if(box==null)
				box = new Matrix3();
			box.set(e.boundingBox);
			boolean hit = box.intersects(this);
			if(hit)
			{
				return true;
			}
		}
		
		return ret;
	}

	public void set(AxisAlignedBB aabb) {
		rows[0].x = aabb.minX;
		rows[0].y = aabb.minY;
		rows[0].z = aabb.minZ;
		rows[1].x = aabb.maxX;
		rows[1].y = aabb.maxY;
		rows[1].z = aabb.maxZ;
		rows[2].clear();
	}

	public boolean intersects(Matrix3 b)
	{
		List<Vector3> cornersB = new ArrayList();
		for(Vector3 v: b.corners(boxCentre()))
			cornersB.add(v);
		
		return intersects(cornersB);
	}
	
	public boolean intersects(List<Vector3> mesh)
	{
		List<Vector3> cornersA = new ArrayList();
		for(Vector3 v: corners(boxCentre()))
			cornersA.add(v);
		List<Vector3> diffs = diff(cornersA, mesh);
		boolean temp = containsOrigin(diffs);
		for(Vector3 v: diffs)
			v.freeVectorFromPool();
		return temp;
		
	}
	
	static List<Vector3> toMesh(ArrayList<Matrix3> boxes)
	{
		List<Vector3> ret = new ArrayList<Vector3>();
		for(Matrix3 box: boxes)
		{
			for(Vector3 v: box.corners(box.boxCentre()))
			{
				boolean has = false;
				for(Vector3 v1:ret)
				{
					if(v1.equals(v))
					{
						has = true;
						break;
					}
				}
				if(!has)
					ret.add(v);
			}
		}
		return ret;
	}
	
	Vector3[] pointSet;
	private List<Vector3> diff(List<Vector3> cornersA, List<Vector3> cornersB)
	{
		ArrayList<Vector3> ret = new ArrayList<Vector3>();
		Vector3 c = Vector3.getNewVectorFromPool();
		if(pointSet == null)
			pointSet = new Vector3[100];
		
		//Vector3[] pointSet = new Vector3[cornersA.size() * cornersB.size()];
		
		int n = 0;
		for(Vector3 a: cornersA)
		{
			for(Vector3 b: cornersB)
			{
				c.set(a).subtractFrom(b);
				pointSet[n++] = c.copy();
			}
		}
		c.freeVectorFromPool();
		for(int i = 0; i<n; i++)
		{
			Vector3 v = pointSet[i];
			ret.add(v);
			pointSet[i] = null;
		}
		//ret.addAll(pointSet);
		return ret;
	}
	
	private static boolean containsOrigin(List<Vector3> points)
	{
		int index = 0;
		int n = 0;
		Vector3 base = points.get(index);
		double dist = Double.MAX_VALUE;
		for(int i = 0; i< points.size(); i++)
		{
			double d = points.get(i).magSq();
			if(d < dist)
			{
				base = points.get(i);
				dist = d;
				index = i;
			}
		}

		Vector3 mid = Vector3.findMidPoint(points);
		points.remove(index);
		boolean ret = false;
		for(int i = 0; i< points.size(); i++)
		{
			Vector3 v = points.get(i);
			double d = v.dot(base);
			double d1 = v.dot(mid);
			
			if(d<=0)
			{
				if(d1<=d && signum(d)==signum(d1))
				{
					ret = true;
					n++;
					return true;
				}
			}
			
		}
		return ret;
	}

	public Matrix3 clear() {
		rows[0].clear();
		rows[1].clear();
		rows[2].clear();
		return this;
	}

}
