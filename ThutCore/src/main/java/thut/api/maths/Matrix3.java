package thut.api.maths;

import static java.lang.Math.max;
import static java.lang.Math.signum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class Matrix3
{

    private static boolean containsOrigin(List<Vector3> points)
    {
        int index = 0;
        Vector3 base = points.get(index);
        double dist = Double.MAX_VALUE;
        for (int i = 0; i < points.size(); i++)
        {
            Vector3 v = points.get(i);
            if (v == null)
            {
                // new Exception().printStackTrace();
                continue;
            }
            double d = v.magSq();
            if (d < dist)
            {
                base = points.get(i);
                dist = d;
                index = i;
            }
        }

        Vector3 mid = Vector3.findMidPoint(points);
        points.remove(index);
        boolean ret = false;
        for (int i = 0; i < points.size(); i++)
        {
            Vector3 v = points.get(i);
            if (v == null)
            {
                // new Exception().printStackTrace();
                continue;
            }
            double d = v.dot(base);
            double d1 = v.dot(mid);

            if (d <= 0)
            {
                if (d1 <= d && signum(d) == signum(d1))
                {
                    ret = true;
                    return true;
                }
            }

        }
        return ret;
    }

    public static AxisAlignedBB copyAndChange(AxisAlignedBB box, int index, double value)
    {
        double x1 = box.minX;
        double x2 = box.maxX;
        double y1 = box.minY;
        double y2 = box.maxY;
        double z1 = box.minZ;
        double z2 = box.maxZ;
        if (index == 0) x1 = value;
        if (index == 1) y1 = value;
        if (index == 2) z1 = value;
        if (index == 3) x2 = value;
        if (index == 4) y2 = value;
        if (index == 5) z2 = value;

        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }

    /** Merges aabbs together, anything closer than dx, dy or dz are considered
     * same box.
     * 
     * @param aabbs
     * @param dx
     * @param dy
     * @param dz */
    public static void mergeAABBs(List<AxisAlignedBB> aabbs, double dx, double dy, double dz)
    {
        Comparator<AxisAlignedBB> comparator = new Comparator<AxisAlignedBB>()
        {
            @Override
            public int compare(AxisAlignedBB arg0, AxisAlignedBB arg1)
            {
                int minX0 = (int) (arg0.minX * 32);
                int minY0 = (int) (arg0.minY * 32);
                int minZ0 = (int) (arg0.minZ * 32);
                int minX1 = (int) (arg1.minX * 32);
                int minY1 = (int) (arg1.minY * 32);
                int minZ1 = (int) (arg1.minZ * 32);
                if (minX0 == minX1)
                {
                    if (minZ0 == minZ1) return minY0 - minY1;
                    return minZ0 - minZ1;
                }
                return minX0 - minX1;
            }
        };
        AxisAlignedBB[] boxes = aabbs.toArray(new AxisAlignedBB[aabbs.size()]);
        aabbs.clear();
        Arrays.sort(boxes, comparator);
        AxisAlignedBB b1;
        AxisAlignedBB b2;
        for (int i = 0; i < boxes.length; i++)
        {
            b1 = boxes[i];
            if (b1 == null) continue;
            for (int j = 0; j < boxes.length; j++)
            {
                b2 = boxes[j];
                if (i == j || b2 == null) continue;
                if (Math.abs(b2.maxY - b1.maxY) <= dy && Math.abs(b2.minY - b1.minY) <= dy
                        && Math.abs(b2.maxX - b1.maxX) <= dx && Math.abs(b2.minX - b1.minX) <= dx
                        && Math.abs(b2.minZ - b1.maxZ) <= dz)
                {
                    b1 = b1.union(b2);
                    boxes[i] = b1;
                    boxes[j] = null;
                }
            }
        }
        for (int i = 0; i < boxes.length; i++)
        {
            b1 = boxes[i];
            if (b1 == null) continue;
            for (int j = 0; j < boxes.length; j++)
            {
                b2 = boxes[j];
                if (i == j || b2 == null) continue;
                if (Math.abs(b2.maxY - b1.maxY) <= dy && Math.abs(b2.minY - b1.minY) <= dy
                        && Math.abs(b2.maxZ - b1.maxZ) <= dz && Math.abs(b2.minZ - b1.minZ) <= dz
                        && Math.abs(b2.minX - b1.maxX) <= dx)
                {
                    b1 = b1.union(b2);
                    boxes[i] = b1;
                    boxes[j] = null;
                }
            }
        }
        for (int i = 0; i < boxes.length; i++)
        {
            b1 = boxes[i];
            if (b1 == null) continue;
            for (int j = 0; j < boxes.length; j++)
            {
                b2 = boxes[j];
                if (i == j || b2 == null) continue;
                if (Math.abs(b2.maxX - b1.maxX) <= dx && Math.abs(b2.minX - b1.minX) <= dx
                        && Math.abs(b2.maxZ - b1.maxZ) <= dz && Math.abs(b2.minZ - b1.minZ) <= dz
                        && Math.abs(b2.minY - b1.maxY) <= dy)
                {
                    b1 = b1.union(b2);
                    boxes[i] = b1;
                    boxes[j] = null;
                }
            }
        }

        for (int i = 0; i < boxes.length; i++)
        {
            b1 = boxes[i];
            if (b1 == null) continue;
            for (int j = 0; j < boxes.length; j++)
            {
                b2 = boxes[j];
                if (i == j || b2 == null) continue;
                // Check if subbox after previous passes, if so, combine.
                if (b2.maxX <= b1.maxX && b2.maxY <= b1.maxY && b2.maxZ <= b1.maxZ && b2.minX >= b1.minX
                        && b2.minY >= b1.minY && b2.minZ >= b1.minZ)
                {
                    boxes[i] = b1;
                    boxes[j] = null;
                }
            }
        }
        for (AxisAlignedBB b : boxes)
        {
            if (b != null)
            {
                aabbs.add(b);
            }
        }
    }

    public static void expandAABBs(List<AxisAlignedBB> aabbs, AxisAlignedBB reference)
    {
        double mx = reference.minX + (reference.maxX - reference.minX) / 2;
        double my = reference.minY + (reference.maxY - reference.minY) / 2;
        double mz = reference.minZ + (reference.maxZ - reference.minZ) / 2;

        int to = 100;

        int xMax = (int) (mx + to);
        int xMin = (int) (mx - to);
        int yMax = (int) (my + to);
        int yMin = (int) (my - to);
        int zMax = (int) (mz + to);
        int zMin = (int) (mz - to);

        double x0, y0, z0, x1, y1, z1;

        for (int i = 0; i < aabbs.size(); i++)
        {
            AxisAlignedBB box = aabbs.get(i);
            boolean yMinus = box.minY - to <= reference.minY && reference.minY >= box.minY;
            boolean yPlus = box.maxY + to >= reference.maxY && reference.maxY <= box.maxY;
            if (yMinus && !yPlus)
            {
                y0 = yMin;
            }
            else
            {
                y0 = box.minY;
            }
            if (yPlus && !yMinus)
            {
                y1 = yMax;
            }
            else
            {
                y1 = box.maxY;
            }
            boolean xMinus = box.minX - to <= reference.minX && reference.minX >= box.minX;
            boolean xPlus = box.maxX + to >= reference.maxX && reference.maxX <= box.maxX;
            if (xMinus && !xPlus)
            {
                x0 = xMin;
            }
            else
            {
                x0 = box.minX;
            }
            if (xPlus && !xMinus)
            {
                x1 = xMax;
            }
            else
            {
                x1 = box.maxX;
            }
            boolean zMinus = box.minZ - to <= reference.minZ && reference.minZ >= box.minZ;
            boolean zPlus = box.maxZ + to >= reference.maxZ && reference.maxZ <= box.maxZ;
            if (zMinus && !zPlus)
            {
                z0 = zMin;
            }
            else
            {
                z0 = box.minZ;
            }
            if (zPlus && !zMinus)
            {
                z1 = zMax;
            }
            else
            {
                z1 = box.maxZ;
            }
            aabbs.set(i, new AxisAlignedBB(x0, y0, z0, x1, y1, z1));
        }
    }

    /** Fills temp1 with the offsets
     * 
     * @param aabbs
     * @param entityBox
     * @param e
     * @param diffs
     * @param temp1
     * @return */
    public static boolean doCollision(List<AxisAlignedBB> aabbs, AxisAlignedBB entityBox, Entity e, double yShift,
            Vector3 diffs, Vector3 temp1)
    {
        double minX = entityBox.minX;
        double minY = entityBox.minY;
        double minZ = entityBox.minZ;
        double maxX = entityBox.maxX;
        double maxY = entityBox.maxY;
        double maxZ = entityBox.maxZ;
        double factor = 0.75d;
        double dx = max(maxX - minX, 0.5) / factor + e.motionX, dz = max(maxZ - minZ, 0.5) / factor + e.motionZ, r;

        boolean collide = false;
        AxisAlignedBB boundingBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);

        mergeAABBs(aabbs, maxX - minX, maxY - minY, maxZ - minZ);

        double yTop = Math.min(e.stepHeight + e.posY + yShift, maxY);

        boolean floor = false;
        boolean ceiling = false;
        double yMaxFloor = minY;

        for (AxisAlignedBB aabb : aabbs)
        {
            dx = 10e3;
            dz = 10e3;
            boolean thisFloor = false;
            boolean thisCieling = false;
            boolean collidesX = ((maxZ <= aabb.maxZ) && (maxZ >= aabb.minZ))
                    || ((minZ <= aabb.maxZ) && (minZ >= aabb.minZ)) || ((minZ <= aabb.minZ) && (maxZ >= aabb.maxZ));

            boolean collidesY = ((minY >= aabb.minY) && (minY <= aabb.maxY))
                    || ((maxY <= aabb.maxY) && (maxY >= aabb.minY)) || ((minY <= aabb.minY) && (maxY >= aabb.maxY));

            boolean collidesZ = ((maxX <= aabb.maxX) && (maxX >= aabb.minX))
                    || ((minX <= aabb.maxX) && (minX >= aabb.minX)) || ((minX <= aabb.minX) && (maxX >= aabb.maxX));

            collidesZ = collidesZ && (collidesX || collidesY);
            collidesX = collidesX && (collidesZ || collidesY);

            if (collidesX && collidesZ && yTop >= aabb.maxY
                    && boundingBox.minY - e.stepHeight - yShift <= aabb.maxY - diffs.y)
            {
                if (!floor)
                {
                    temp1.y = Math.max(aabb.maxY - boundingBox.minY, temp1.y);
                }
                floor = true;
                thisFloor = aabb.maxY >= yMaxFloor;
                if (thisFloor) yMaxFloor = aabb.maxY;
            }
            if (collidesX && collidesZ && boundingBox.maxY >= aabb.minY && boundingBox.minY < aabb.minY)
            {
                if (!(floor || ceiling))
                {
                    double dy = aabb.minY - boundingBox.maxY - diffs.y;
                    temp1.y = Math.min(dy, temp1.y);
                }
                thisCieling = !(thisFloor || floor);
                ceiling = true;
            }

            boolean vert = thisFloor || thisCieling;

            if (collidesX && !vert && collidesY && boundingBox.maxX >= aabb.maxX && boundingBox.minX <= aabb.maxX)
            {
                r = Math.max(aabb.maxX - boundingBox.minX, temp1.x);
                dx = Math.min(dx, r);
            }
            if (collidesX && !vert && collidesY && boundingBox.maxX >= aabb.minX && boundingBox.minX < aabb.minX)
            {
                r = Math.min(aabb.minX - boundingBox.maxX, temp1.x);
                dx = Math.min(dx, r);
            }
            if (collidesZ && !vert && collidesY && boundingBox.maxZ >= aabb.maxZ && boundingBox.minZ <= aabb.maxZ)
            {
                r = Math.max(aabb.maxZ - boundingBox.minZ, temp1.z);
                dz = Math.min(dz, r);
            }
            if (collidesZ && !vert && collidesY && boundingBox.maxZ >= aabb.minZ && boundingBox.minZ < aabb.minZ)
            {
                r = Math.min(aabb.minZ - boundingBox.maxZ, temp1.z);
                dz = Math.min(dz, r);
            }
            if (Math.abs(dx) > Math.abs(dz) && dx < 10e2 || dx == 10e3 && dz < 10e2)
            {
                temp1.z = dz;
            }
            else if (dx < 10e2)
            {
                temp1.x = dx;
            }
        }
        return collide;
    }

    public static AxisAlignedBB getAABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
    {
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /** Computes the Determinant of the given matrix, Matrix must be square.
     * 
     * @param Matrix
     * @return */
    public static double matrixDet(Matrix3 Matrix)
    {
        double det = 0;
        int n = Matrix.size;
        if (n == 2)
        {
            det = Matrix.get(0, 0) * Matrix.get(1, 1) - Matrix.get(1, 0) * Matrix.get(0, 1);
        }
        else
        {
            for (int i = 0; i < n; i++)
            {
                det += Math.pow(-1, i) * Matrix.get(0, i) * matrixDet(matrixMinor(Matrix, 0, i));
            }
        }
        return det;
    }

    /** Computes the minor matrix formed from removal of the ith row and jth
     * column of matrix.
     * 
     * @param Matrix
     * @param i
     * @param j
     * @return */
    public static Matrix3 matrixMinor(Matrix3 input, int i, int j)
    {
        double[][] Matrix = input.toArray();
        int n = Matrix.length;
        int m = Matrix[0].length;
        Double[][] TempMinor = new Double[m - 1][n - 1];
        List<ArrayList<Double>> row = new ArrayList<ArrayList<Double>>();
        for (int k = 0; k < n; k++)
        {
            if (k != i)
            {
                row.add(new ArrayList<Double>());
                for (int l = 0; l < m; l++)
                {
                    if (l != j)
                    {
                        row.get(k - (k > i ? 1 : 0)).add(Matrix[k][l]);
                    }
                }
            }
        }
        for (int k = 0; k < n - 1; k++)
        {
            TempMinor[k] = row.get(k).toArray(new Double[0]);
        }
        Matrix3 Minor = new Matrix3();
        Minor.size = n - 1;
        for (int k = 0; k < n - 1; k++)
        {
            for (int l = 0; l < m - 1; l++)
            {
                Minor.set(k, l, TempMinor[k][l]);
            }
        }
        return Minor;
    }

    /** Transposes the given Matrix
     * 
     * @param Matrix
     * @return */
    public static Matrix3 matrixTranspose(Matrix3 Matrix)
    {
        Matrix3 MatrixT = new Matrix3();
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                MatrixT.set(i, j, Matrix.get(j, i));
            }
        }
        return MatrixT;
    }

    static List<Vector3> toMesh(ArrayList<Matrix3> boxes)
    {
        List<Vector3> ret = new ArrayList<Vector3>();
        for (Matrix3 box : boxes)
        {
            Vector3 vc = box.boxCentre();
            for (Vector3 v : box.corners(vc))
            {
                boolean has = false;
                for (Vector3 v1 : ret)
                {
                    if (v1.equals(v))
                    {
                        has = true;
                        break;
                    }
                }
                if (!has) ret.add(v);
            }
        }
        return ret;
    }

    public Vector3[]                 rows = new Vector3[3];

    int                              size = 3;

    Vector3[]                        pointSet;

    private ArrayList<AxisAlignedBB> collidingBoundingBoxes;

    public Matrix3()
    {
        rows[0] = Vector3.getNewVector();
        rows[1] = Vector3.getNewVector();
        rows[2] = Vector3.getNewVector();
    }

    public Matrix3(double d, double e, double f)
    {
        this();
        rows[1].set(d, e, f);
    }

    public Matrix3(double[] a, double[] b, double[] c)
    {
        this();
        rows[0].set(a[0], a[1], a[2]);
        rows[1].set(b[0], b[1], b[2]);
        rows[2].set(c[0], c[1], c[2]);
    }

    public Matrix3(Vector3 a, Vector3 b)
    {
        this(a, b, Vector3.empty);
    }

    public Matrix3(Vector3 a, Vector3 b, Vector3 c)
    {
        rows[0] = a.copy();
        rows[1] = b.copy();
        rows[2] = c.copy();
    }

    public Matrix3 addOffsetTo(Vector3 pushOffset)
    {
        rows[0].addTo(pushOffset);
        rows[1].addTo(pushOffset);
        return this;
    }

    public Vector3 boxCentre()
    {
        Vector3 mid = Vector3.getNewVector();
        Vector3 temp1 = boxMax().copy();
        Vector3 temp2 = boxMax().copy();
        mid.set(temp2.subtractFrom((temp1.subtractFrom(boxMin())).scalarMultBy(0.5)));
        return mid;
    }

    public Vector3 boxMax()
    {
        return rows[1];
    }

    public Vector3 boxMin()
    {
        return rows[0];
    }

    public Vector3 boxRotation()
    {
        return rows[2];
    }

    public Matrix3 clear()
    {
        rows[0].clear();
        rows[1].clear();
        rows[2].clear();
        return this;
    }

    public Matrix3 copy()
    {
        Matrix3 ret = new Matrix3();
        ret.rows[0].set(rows[0]);
        ret.rows[1].set(rows[1]);
        ret.rows[2].set(rows[2]);
        return ret;
    }

    public List<Vector3> corners(boolean rotate)
    {
        // if (corners.isEmpty())
        List<Vector3> corners = new ArrayList<Vector3>();

        for (int i = 0; i < 8; i++)
            corners.add(Vector3.getNewVector());

        corners.get(0).set(boxMin());
        corners.get(1).set(boxMax());

        corners.get(2).set(boxMin().x, boxMin().y, boxMax().z);
        corners.get(3).set(boxMin().x, boxMax().y, boxMin().z);
        corners.get(4).set(boxMax().x, boxMin().y, boxMin().z);

        corners.get(5).set(boxMin().x, boxMax().y, boxMax().z);
        corners.get(6).set(boxMax().x, boxMin().y, boxMax().z);
        corners.get(7).set(boxMax().x, boxMax().y, boxMin().z);
        Vector3 mid;
        if (rotate && !boxRotation().isEmpty()) mid = boxCentre();
        else mid = null;
        if (!boxRotation().isEmpty() && mid != null)
        {
            Vector3 temp = Vector3.getNewVector();
            Vector3 temp2 = Vector3.getNewVector();
            for (int i = 0; i < 8; i++)
            {
                corners.get(i).subtractFrom(mid);
                temp2.clear();
                temp.clear();
                corners.get(i).set(corners.get(i).rotateAboutAngles(boxRotation().y, boxRotation().z, temp2, temp));
                corners.get(i).addTo(mid);
            }
        }

        return corners;
    }

    /** 0 = min, min, min; 1 = max, max, max; 2 = min, min, max; 3 = min, max,
     * min; 4 = max, min, min; 5 = min, max, max; 6 = max, min, max; 7 = max.
     * max, min;
     * 
     * @return */
    public Vector3[] corners(Vector3 mid)
    {
        return corners(mid != null).toArray((new Vector3[8]));
    }

    private List<Vector3> diff(List<Vector3> cornersA, List<Vector3> cornersB)
    {
        ArrayList<Vector3> ret = new ArrayList<Vector3>();
        Vector3 c = Vector3.getNewVector();
        if (pointSet == null) pointSet = new Vector3[100];

        // Vector3[] pointSet = new Vector3[cornersA.size() * cornersB.size()];

        int n = 0;
        for (Vector3 a : cornersA)
        {
            for (Vector3 b : cornersB)
            {
                c.set(a).subtractFrom(b);
                pointSet[n++] = c.copy();
            }
        }
        for (int i = 0; i < n; i++)
        {
            Vector3 v = pointSet[i];
            ret.add(v);
            pointSet[i] = null;
        }
        // ret.addAll(pointSet);
        return ret;
    }

    public boolean doCollision(Vector3 boxVelocity, Entity e)
    {
        if (e == null) return false;
        Vector3 ent = Vector3.getNewVector();
        ent.set(e);
        corners(true);
        Entity[] parts = e.getParts();
        Matrix3 box = new Matrix3();
        box.set(e.getEntityBoundingBox());
        boolean hit = box.intersects(this);
        if (parts != null)
        {
            for (Entity e1 : parts)
            {
                if (hit) break;
                box.set(e1.getEntityBoundingBox());
                hit = hit || box.intersects(this);
            }
        }
        box = null;
        return hit;
    }

    public Vector3 doTileCollision(IBlockAccess world, Entity e, Vector3 offset, Vector3 diffs, boolean move)
    {
        Matrix3 box = copy().addOffsetTo(offset);
        Vector3 v1 = box.boxCentre();
        Vector3[] corners = box.corners(v1);

        double minX = Double.MAX_VALUE, minZ = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (Vector3 v : corners)
        {
            if (v.x > maxX) maxX = v.x;
            if (v.y > maxY) maxY = v.y;
            if (v.z > maxZ) maxZ = v.z;
            if (v.x < minX) minX = v.x;
            if (v.y < minY) minY = v.y;
            if (v.z < minZ) minZ = v.z;
        }
        double factor = 0.275d;
        double dx = max(maxX - minX, 1) / factor + e.motionX, dy = max(maxY - minY, 1) / factor + e.motionY,
                dz = max(maxZ - minZ, 1) / factor + e.motionZ;
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        dz = Math.abs(dz);
        dx = Math.max(dx, 1.5);
        dy = Math.max(dy, 1.5);
        dz = Math.max(dz, 1.5);
        AxisAlignedBB b1 = box.boxCentre().getAABB().expand(dx, dy, dz);
        List<AxisAlignedBB> aabbs = getCollidingBoxes(b1, e.getEntityWorld(), world);
        AxisAlignedBB b2;
        AxisAlignedBB[] boxes = aabbs.toArray(new AxisAlignedBB[aabbs.size()]);
        aabbs.clear();
        dx = maxX - minX;
        dy = maxY - minY;
        dz = maxZ - minZ;
        Arrays.sort(boxes, new Comparator<AxisAlignedBB>()
        {
            @Override
            public int compare(AxisAlignedBB arg0, AxisAlignedBB arg1)
            {
                int minX0 = (int) (arg0.minX * 32);
                int minY0 = (int) (arg0.minY * 32);
                int minZ0 = (int) (arg0.minZ * 32);
                int minX1 = (int) (arg1.minX * 32);
                int minY1 = (int) (arg1.minY * 32);
                int minZ1 = (int) (arg1.minZ * 32);
                if (minX0 == minX1)
                {
                    if (minZ0 == minZ1) return minY0 - minY1;
                    return minZ0 - minZ1;
                }
                return minX0 - minX1;
            }
        });
        double minBoxY = Integer.MAX_VALUE;
        double maxBoxY = Integer.MIN_VALUE;
        for (int i = 0; i < boxes.length; i++)
        {
            b1 = boxes[i];
            if (b1 == null) continue;
            for (int j = 0; j < boxes.length; j++)
            {
                b2 = boxes[j];
                if (i == j || b2 == null) continue;
                factor = 16;
                if (MathHelper.floor(b2.maxX * factor) == MathHelper.floor(b1.maxX * factor)
                        && MathHelper.floor(b2.minX * factor) == MathHelper.floor(b1.minX * factor)
                        && MathHelper.floor(b2.maxZ * factor) == MathHelper.floor(b1.maxZ * factor)
                        && MathHelper.floor(b2.minZ * factor) == MathHelper.floor(b1.minZ * factor)
                        && Math.abs(b2.minY - b1.maxY) < dy)
                {
                    b1 = copyAndChange(b1, 4, b2.maxY);
                    boxes[i] = b1;
                    if (b1.minY < minBoxY)
                    {
                        minBoxY = b1.minY;
                    }
                    if (b1.maxY > maxBoxY)
                    {
                        maxBoxY = b1.maxY;
                    }
                    boxes[j] = null;
                }

            }
        }
        for (int i = 0; i < boxes.length; i++)
        {
            b1 = boxes[i];
            if (b1 == null) continue;
            for (int j = 0; j < boxes.length; j++)
            {
                b2 = boxes[j];
                if (i == j || b2 == null) continue;
                factor = 16;
                if (MathHelper.floor(b2.maxY * factor) == MathHelper.floor(b1.maxY * factor)
                        && MathHelper.floor(b2.minY * factor) == MathHelper.floor(b1.minY * factor)
                        && MathHelper.floor(b2.maxZ * factor) == MathHelper.floor(b1.maxZ * factor)
                        && MathHelper.floor(b2.minZ * factor) == MathHelper.floor(b1.minZ * factor)
                        && Math.abs(b2.minX - b1.maxX) < dx)
                {
                    b1 = copyAndChange(b1, 3, b2.maxX);
                    boxes[i] = b1;
                    boxes[j] = null;
                }
            }
        }
        for (int i = 0; i < boxes.length; i++)
        {
            b1 = boxes[i];
            if (b1 == null) continue;
            for (int j = 0; j < boxes.length; j++)
            {
                b2 = boxes[j];
                if (i == j || b2 == null) continue;
                factor = 16;
                if (MathHelper.floor(b2.maxY * factor) == MathHelper.floor(b1.maxY * factor)
                        && MathHelper.floor(b2.minY * factor) == MathHelper.floor(b1.minY * factor)
                        && MathHelper.floor(b2.maxX * factor) == MathHelper.floor(b1.maxX * factor)
                        && MathHelper.floor(b2.minX * factor) == MathHelper.floor(b1.minX * factor)
                        && Math.abs(b2.minZ - b1.maxZ) < dz)
                {
                    b1 = copyAndChange(b1, 5, b2.maxZ);
                    boxes[i] = b1;
                    boxes[j] = null;
                }
            }
        }
        for (AxisAlignedBB b : boxes)
        {
            if (b != null)
            {
                aabbs.add(b);
            }
        }
        return doTileCollision(world, aabbs, e, offset, diffs, true);
    }

    public Vector3 doTileCollision(IBlockAccess world, List<AxisAlignedBB> aabbs, Entity e, Vector3 offset,
            Vector3 diffs)
    {
        return doTileCollision(world, aabbs, e, offset, diffs, false);
    }

    public Vector3 doTileCollision(IBlockAccess world, List<AxisAlignedBB> aabbs, Entity e, Vector3 offset,
            Vector3 diffs, boolean moveEntity)
    {
        Vector3 temp1 = Vector3.getNewVector();

        Matrix3 box = copy().addOffsetTo(offset);
        Vector3 v1 = box.boxCentre();
        Vector3[] corners = box.corners(v1);

        double minX = Double.MAX_VALUE, minZ = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (Vector3 v : corners)
        {
            if (v.x > maxX) maxX = v.x;
            if (v.y > maxY) maxY = (float) v.y;
            if (v.z > maxZ) maxZ = v.z;
            if (v.x < minX) minX = v.x;
            if (v.y < minY) minY = (float) v.y;
            if (v.z < minZ) minZ = v.z;
        }
        if (!e.getRecursivePassengers().isEmpty())
        {
            double mY = 0;
            for (Entity e1 : e.getRecursivePassengers())
            {
                mY = Math.max(mY, e1.height + e.getMountedYOffset());
            }
            maxY += mY;
        }
        AxisAlignedBB boundingBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
        double dx, dy, dz;
        temp1.set(diffs);
        double yTop = Math.min(e.stepHeight + minY, maxY);
        boolean vert = false;
        for (AxisAlignedBB aabb : aabbs)
        {
            if (boundingBox.intersects(aabb))
            {
                dx = 10e3;
                dz = 10e3;
                double dxmax = 10e3, dxmin = -10e3, dzmax = 10e3, dzmin = -10e3;

                boolean collidesX = ((maxZ <= aabb.maxZ) && (maxZ >= aabb.minZ))
                        || ((minZ <= aabb.maxZ) && (minZ >= aabb.minZ)) || ((minZ <= aabb.minZ) && (maxZ >= aabb.maxZ));

                boolean collidesY = ((minY >= aabb.minY) && (minY <= aabb.maxY))
                        || ((maxY <= aabb.maxY) && (maxY >= aabb.minY)) || ((minY <= aabb.minY) && (maxY >= aabb.maxY));

                boolean collidesZ = ((maxX <= aabb.maxX) && (maxX >= aabb.minX))
                        || ((minX <= aabb.maxX) && (minX >= aabb.minX)) || ((minX <= aabb.minX) && (maxX >= aabb.maxX));

                collidesZ = collidesZ && (collidesX || collidesY);
                collidesX = collidesX && (collidesZ || collidesY);

                if (yTop >= aabb.maxY && boundingBox.minY - e.stepHeight <= aabb.maxY)
                {
                    temp1.y = (aabb.maxY - boundingBox.minY);
                    vert = true;
                }
                if (boundingBox.maxY >= aabb.minY && boundingBox.minY < aabb.minY && diffs.y > 0)
                {
                    temp1.y = (aabb.minY - boundingBox.maxY);
                    vert = true;
                }
                if (boundingBox.maxX > aabb.minX && boundingBox.minX < aabb.minX && !vert)
                {
                    dxmin = aabb.minX - boundingBox.maxX;
                }
                if (boundingBox.minX < aabb.maxX && boundingBox.maxX > aabb.maxX && !vert)
                {
                    dxmax = aabb.maxX - boundingBox.minX;
                }
                if (boundingBox.maxZ > aabb.minZ && boundingBox.minZ < aabb.minZ && !vert)
                {
                    dzmin = aabb.minZ - boundingBox.maxZ;
                }
                if (boundingBox.minZ < aabb.maxZ && boundingBox.maxZ > aabb.maxZ && !vert)
                {
                    dzmax = aabb.maxZ - boundingBox.minZ;
                }

                if (dxmin != -10e3)
                {
                    if (dzmax != 10e3)
                    {
                        if (Math.abs(dxmin) < dzmax)
                        {
                            temp1.x = dxmin;
                        }
                        else
                        {
                            temp1.z = dzmax;
                        }
                    }
                    else if (dzmin != -10e3)
                    {
                        if (dxmin > dzmin)
                        {
                            temp1.x = dxmin;
                        }
                        else
                        {
                            temp1.z = dzmin;
                        }
                    }
                    else
                    {
                        temp1.x = dxmin;
                    }
                }
                else if (dxmax != 10e3)
                {
                    if (dzmax != 10e3)
                    {
                        if (dxmax < dxmax)
                        {
                            temp1.x = dxmax;
                        }
                        else
                        {
                            temp1.z = dzmax;
                        }
                    }
                    else if (dzmin != -10e3)
                    {
                        if (Math.abs(dzmin) < dxmax)
                        {
                            temp1.z = dzmin;
                        }
                        else
                        {
                            temp1.x = dxmax;
                        }
                    }
                    else
                    {
                        temp1.x = dxmax;
                    }
                }
                else if (dzmin != -10e3)
                {
                    temp1.z = dzmin;
                }
                else if (dzmax != 10e3)
                {
                    temp1.z = dzmax;
                }
            }
            else
            {
                dy = -boundingBox.calculateYOffset(aabb, -temp1.y);
                if (dy != temp1.y)
                {
                    temp1.y = dy;
                }
                dx = -boundingBox.calculateXOffset(aabb, -temp1.x);
                if (dx != temp1.x)
                {
                    temp1.x = dx;
                }
                dz = -boundingBox.calculateZOffset(aabb, -temp1.z);
                if (dz != temp1.z)
                {
                    temp1.z = dz;
                }
            }
        }
        return temp1;
    }

    public boolean doTileCollision(IBlockAccess world, List<AxisAlignedBB> aabbs, Vector3 location, Entity e,
            Vector3 diffs)
    {

        Vector3 temp1 = Vector3.getNewVector();

        Matrix3 box = copy().addOffsetTo(location);
        Vector3 v1 = box.boxCentre();
        Vector3[] corners = box.corners(v1);

        double minX = Double.MAX_VALUE, minZ = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (Vector3 v : corners)
        {
            if (v.x > maxX) maxX = v.x;
            if (v.y > maxY) maxY = (float) v.y;
            if (v.z > maxZ) maxZ = v.z;
            if (v.x < minX) minX = v.x;
            if (v.y < minY) minY = (float) v.y;
            if (v.z < minZ) minZ = v.z;
        }
        if (!e.getRecursivePassengers().isEmpty())
        {
            double mY = 0;
            for (Entity e1 : e.getRecursivePassengers())
            {
                mY = Math.max(mY, e1.height + e.getMountedYOffset());
            }
            maxY += mY;
        }
        AxisAlignedBB boundingBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
        temp1.set(diffs);
        for (AxisAlignedBB aabb : aabbs)
        {
            if (boundingBox.intersects(aabb))
            {
                boolean collidesX = ((maxZ <= aabb.maxZ) && (maxZ >= aabb.minZ))
                        || ((minZ <= aabb.maxZ) && (minZ >= aabb.minZ)) || ((minZ <= aabb.minZ) && (maxZ >= aabb.maxZ));

                boolean collidesY = ((minY >= aabb.minY) && (minY <= aabb.maxY))
                        || ((maxY <= aabb.maxY) && (maxY >= aabb.minY)) || ((minY <= aabb.minY) && (maxY >= aabb.maxY));

                boolean collidesZ = ((maxX <= aabb.maxX) && (maxX >= aabb.minX))
                        || ((minX <= aabb.maxX) && (minX >= aabb.minX)) || ((minX <= aabb.minX) && (maxX >= aabb.maxX));
                collidesZ = collidesZ && (collidesX || collidesY);
                collidesX = collidesX && (collidesZ || collidesY);
                if (collidesZ || collidesX || collidesY) { return true; }
            }
        }
        return false;
    }

    public Vector3 get(int i)
    {
        assert (i < 3);
        return rows[i];
    }

    public double get(int i, int j)
    {
        assert (i < 3);
        return rows[i].get(j);
    }

    public List<AxisAlignedBB> getCollidingBoxes(AxisAlignedBB box, World world, IBlockAccess access)
    {
        if (collidingBoundingBoxes == null) collidingBoundingBoxes = new ArrayList<AxisAlignedBB>();

        this.collidingBoundingBoxes.clear();
        int i = MathHelper.floor(box.minX);
        int j = MathHelper.floor(box.maxX + 1.0D);
        int k = MathHelper.floor(box.minY);
        int l = MathHelper.floor(box.maxY + 1.0D);
        int i1 = MathHelper.floor(box.minZ);
        int j1 = MathHelper.floor(box.maxZ + 1.0D);

        for (int k1 = i; k1 < j; ++k1)
        {
            for (int l1 = i1; l1 < j1; ++l1)
            {
                for (int i2 = k - 1; i2 < l; ++i2)
                {
                    BlockPos blockpos = new BlockPos(k1, i2, l1);

                    IBlockState iblockstate = access.getBlockState(blockpos);
                    if (iblockstate == null) iblockstate = Blocks.AIR.getDefaultState();
                    Block block = iblockstate.getBlock();
                    if (block.isCollidable())
                    {
                        iblockstate.addCollisionBoxToList(world, blockpos, box, collidingBoundingBoxes, null, false);
                        net.minecraftforge.common.MinecraftForge.EVENT_BUS
                                .post(new net.minecraftforge.event.world.GetCollisionBoxesEvent(world, null, box,
                                        collidingBoundingBoxes));
                    }
                }
            }
        }
        return this.collidingBoundingBoxes;
    }

    public Matrix3 getOctant(int octant)
    {
        Matrix3 ret = copy();
        switch (octant)
        {
        case 0:
            ret.rows[0].x = rows[0].x + rows[1].x / 2;
            ret.rows[0].y = rows[0].y + rows[1].y / 2;
            ret.rows[0].z = rows[0].z + rows[1].z / 2;
            return ret;
        case 1:
            ret.rows[1].x = rows[1].x - rows[1].x / 2;
            ret.rows[0].y = rows[0].y + rows[1].y / 2;
            ret.rows[0].z = rows[0].z + rows[1].z / 2;
            return ret;
        case 2:
            ret.rows[1].x = rows[1].x - rows[1].x / 2;
            ret.rows[1].y = rows[1].y - rows[1].y / 2;
            ret.rows[0].z = rows[0].z + rows[1].z / 2;
            return ret;
        case 3:
            ret.rows[0].x = rows[0].x + rows[1].x / 2;
            ret.rows[1].y = rows[1].y - rows[1].y / 2;
            ret.rows[0].z = rows[0].z + rows[1].z / 2;
            return ret;
        case 4:
            ret.rows[0].x = rows[0].x + rows[1].x / 2;
            ret.rows[0].y = rows[0].y + rows[1].y / 2;
            ret.rows[1].z = rows[1].z - rows[1].z / 2;
            return ret;
        case 5:
            ret.rows[1].x = rows[1].x - rows[1].x / 2;
            ret.rows[0].y = rows[0].y + rows[1].y / 2;
            ret.rows[1].z = rows[1].z - rows[1].z / 2;
            return ret;
        case 6:
            ret.rows[1].x = rows[1].x - rows[1].x / 2;
            ret.rows[1].y = rows[1].y - rows[1].y / 2;
            ret.rows[1].z = rows[1].z - rows[1].z / 2;
            return ret;
        case 7:
            ret.rows[0].x = rows[0].x + rows[1].x / 2;
            ret.rows[1].y = rows[1].y - rows[1].y / 2;
            ret.rows[1].z = rows[1].z - rows[1].z / 2;
            return ret;
        }
        return ret;
    }

    public boolean intersects(List<Vector3> mesh)
    {
        List<Vector3> cornersA = new ArrayList<Vector3>();
        Vector3 v1 = boxCentre();
        for (Vector3 v : corners(v1))
            cornersA.add(v);
        List<Vector3> diffs = diff(cornersA, mesh);
        boolean temp = containsOrigin(diffs);
        return temp;

    }

    public boolean intersects(Matrix3 b)
    {
        List<Vector3> cornersB = new ArrayList<Vector3>();
        Vector3 v1 = boxCentre();
        for (Vector3 v : b.corners(v1))
            cornersB.add(v);
        return intersects(cornersB);
    }

    public boolean isInMaterial(IBlockAccess world, Vector3 location, Vector3 offset, Material m)
    {
        boolean ret = false;
        Vector3 ent = location;
        Vector3[] corners = corners(boxCentre());
        Vector3 temp = Vector3.getNewVector();
        Vector3 dir = Vector3.getNewVector();
        for (int i = 0; i < 8; i++)
        {
            Vector3 v = corners[i];
            dir.set(v);
            temp.set(dir.addTo(ent).addTo(offset));
            if (temp.getBlockMaterial(world) == m)
            {
                ret = true;
                break;
            }
            if (i % 2 == 0)
            {
                temp.addTo(0, 0.01, 0);
                if (temp.getBlockMaterial(world) == m)
                {
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    public AxisAlignedBB getBoundingBox()
    {
        Vector3 v1 = boxCentre();
        Vector3[] corners = corners(v1);

        double minX = Double.MAX_VALUE, minZ = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (Vector3 v : corners)
        {
            if (v.x > maxX) maxX = v.x;
            if (v.y > maxY) maxY = v.y;
            if (v.z > maxZ) maxZ = v.z;
            if (v.x < minX) minX = v.x;
            if (v.y < minY) minY = v.y;
            if (v.z < minZ) minZ = v.z;
        }
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
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

    public void set(AxisAlignedBB aabb)
    {
        rows[0].x = aabb.minX;
        rows[0].y = aabb.minY;
        rows[0].z = aabb.minZ;
        rows[1].x = aabb.maxX;
        rows[1].y = aabb.maxY;
        rows[1].z = aabb.maxZ;
        rows[2].clear();
    }

    public void set(int i, int j, double k)
    {
        rows[i].set(j, k);
    }

    public Matrix3 set(int i, Vector3 j)
    {
        assert (i < 3);
        rows[i] = j;
        return this;
    }

    public void set(Matrix3 box)
    {
        rows[0].set(box.rows[0]);
        rows[1].set(box.rows[1]);
        rows[2].set(box.rows[2]);
    }

    public Vector3[][] splitBox()
    {
        Vector3 v1 = boxCentre();
        Vector3[] corners = corners(v1);
        double dx = boxMax().x - boxMin().x;
        double dz = boxMax().z - boxMin().z;
        dx = Math.abs(dx);
        dz = Math.abs(dz);
        if (dx <= 1 && dz <= 1 || dx < 0.1 || dz < 0.1) { return new Vector3[][] { corners }; }

        dx = Math.max(dx, 1);

        if (dz > 2 * dx)
        {
            int num = (int) (dz / dx);
            Vector3[][] ret = new Vector3[num][8];

            Vector3 min1 = corners[0];
            Vector3 max1 = corners[2];
            Vector3 dir1 = max1.subtract(min1).scalarMultBy(1d / num);

            Vector3 min2 = corners[3];
            Vector3 max2 = corners[5];
            Vector3 dir2 = max2.subtract(min2).scalarMultBy(1d / num);

            Vector3 min3 = corners[7];
            Vector3 max3 = corners[1];
            Vector3 dir3 = max3.subtract(min3).scalarMultBy(1d / num);

            Vector3 min4 = corners[4];
            Vector3 max4 = corners[6];
            Vector3 dir4 = max4.subtract(min4).scalarMultBy(1d / num);

            for (int i = 0; i < num; i++)
            {
                ret[i][0] = dir1.scalarMult(i).addTo(min1);
                ret[i][1] = dir2.scalarMult(i).addTo(min2);
                ret[i][2] = dir3.scalarMult(i).addTo(min3);
                ret[i][3] = dir4.scalarMult(i).addTo(min4);

                ret[i][4] = dir1.scalarMult(i + 1).addTo(min1);
                ret[i][5] = dir2.scalarMult(i + 1).addTo(min2);
                ret[i][6] = dir3.scalarMult(i + 1).addTo(min3);
                ret[i][7] = dir4.scalarMult(i + 1).addTo(min4);
            }
            return ret;
        }
        return new Vector3[][] { corners };
    }

    public double[][] toArray()
    {
        return new double[][] { { rows[0].x, rows[0].y, rows[0].z }, { rows[1].x, rows[1].y, rows[1].z },
                { rows[2].x, rows[2].y, rows[2].z } };
    }

    @Override
    public String toString()
    {
        String eol = System.getProperty("line.separator");
        return eol + "0: " + rows[0].toString() + eol + "1: " + rows[1].toString() + eol + "2 : " + rows[2].toString();
    }

}
