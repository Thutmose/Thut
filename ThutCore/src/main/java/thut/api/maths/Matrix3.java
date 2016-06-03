package thut.api.maths;

import static java.lang.Math.max;
import static java.lang.Math.signum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import thut.api.entity.IMultibox;

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
        double dx = max(maxX - minX, 1) / factor + e.motionX, dz = max(maxZ - minZ, 1) / factor + e.motionZ, r;

        boolean collide = false;
        AxisAlignedBB b1;
        AxisAlignedBB boundingBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);

        AxisAlignedBB b2;
        AxisAlignedBB[] boxes = aabbs.toArray(new AxisAlignedBB[aabbs.size()]);
        aabbs.clear();

        Arrays.sort(boxes, new Comparator<AxisAlignedBB>()
        {
            @Override
            public int compare(AxisAlignedBB o1, AxisAlignedBB o2)
            {
                if (o1.minY == o1.minY)
                {
                    if (o1.minX == o2.minX)
                    {
                        return MathHelper.floor_double(o1.minZ * 16) - MathHelper.floor_double(o2.minZ * 16);
                    }
                    else
                    {
                        return MathHelper.floor_double(o1.minX * 16) - MathHelper.floor_double(o2.minX * 16);
                    }
                }
                return MathHelper.floor_double(o1.minY * 16) - MathHelper.floor_double(o2.minY * 16);
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
                factor = 15;
                if (MathHelper.floor_double(b2.maxX * factor) == MathHelper.floor_double(b1.maxX * factor)
                        && MathHelper.floor_double(b2.minX * factor) == MathHelper.floor_double(b1.minX * factor)
                        && MathHelper.floor_double(b2.maxZ * factor) == MathHelper.floor_double(b1.maxZ * factor)
                        && MathHelper.floor_double(b2.minZ * factor) == MathHelper.floor_double(b1.minZ * factor)
                        && Math.abs(b2.minY - b1.maxY) < maxY - minY)
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
        boolean dox = true, doz = true;
        if (dox) for (int i = 0; i < boxes.length; i++)
        {
            b1 = boxes[i];
            if (b1 == null) continue;
            for (int j = 0; j < boxes.length; j++)
            {
                b2 = boxes[j];
                if (i == j || b2 == null) continue;
                factor = 16;
                if (MathHelper.floor_double(b2.maxY * factor) == MathHelper.floor_double(b1.maxY * factor)
                        && MathHelper.floor_double(b2.minY * factor) == MathHelper.floor_double(b1.minY * factor)
                        && MathHelper.floor_double(b2.maxZ * factor) == MathHelper.floor_double(b1.maxZ * factor)
                        && MathHelper.floor_double(b2.minZ * factor) == MathHelper.floor_double(b1.minZ * factor)
                        && Math.abs(b2.minX - b1.maxX) < maxX - minX)
                {
                    b1 = copyAndChange(b1, 3, b2.maxX);
                    boxes[i] = b1;
                    boxes[j] = null;
                }
            }
        }
        if (doz) for (int i = 0; i < boxes.length; i++)
        {
            b1 = boxes[i];
            if (b1 == null) continue;
            for (int j = 0; j < boxes.length; j++)
            {
                b2 = boxes[j];
                if (i == j || b2 == null) continue;
                factor = 16;
                if (MathHelper.floor_double(b2.maxY * factor) == MathHelper.floor_double(b1.maxY * factor)
                        && MathHelper.floor_double(b2.minY * factor) == MathHelper.floor_double(b1.minY * factor)
                        && MathHelper.floor_double(b2.maxX * factor) == MathHelper.floor_double(b1.maxX * factor)
                        && MathHelper.floor_double(b2.minX * factor) == MathHelper.floor_double(b1.minX * factor)
                        && Math.abs(b2.minZ - b1.maxZ) < maxZ - minZ)
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

        double yTop = Math.min(e.stepHeight + e.posY + yShift, maxY);
        // System.out.println(yTop+" "+e.posY+" "+e.stepHeight+" "+yShift);
        for (AxisAlignedBB aabb : aabbs)
        {
            dx = 10e3;
            dz = 10e3;
            boolean collidesX = ((maxZ <= aabb.maxZ) && (maxZ >= aabb.minZ))
                    || ((minZ <= aabb.maxZ) && (minZ >= aabb.minZ)) || ((minZ <= aabb.minZ) && (maxZ >= aabb.maxZ));

            boolean collidesY = ((minY >= aabb.minY) && (minY <= aabb.maxY))
                    || ((maxY <= aabb.maxY) && (maxY >= aabb.minY)) || ((minY <= aabb.minY) && (maxY >= aabb.maxY));

            boolean collidesZ = ((maxX <= aabb.maxX) && (maxX >= aabb.minX))
                    || ((minX <= aabb.maxX) && (minX >= aabb.minX)) || ((minX <= aabb.minX) && (maxX >= aabb.maxX));

            collidesZ = collidesZ && (collidesX || collidesY);
            collidesX = collidesX && (collidesZ || collidesY);

            boolean floor = false;

            if (collidesX && collidesZ && yTop >= aabb.maxY
                    && boundingBox.minY - e.stepHeight - yShift <= aabb.maxY - diffs.y)
            {
                floor = true;
                if (diffs.y <= 0) temp1.y = Math.max(aabb.maxY - boundingBox.minY, temp1.y);
            }
            if (collidesX && collidesZ && boundingBox.maxY >= aabb.minY && boundingBox.minY < aabb.minY)
            {
                temp1.y = Math.min(aabb.minY - boundingBox.maxY, temp1.y);
            }

            if (collidesX && collidesY && boundingBox.maxX >= aabb.maxX && boundingBox.minX <= aabb.maxX && !floor)
            {
                r = Math.max(aabb.maxX - boundingBox.minX, temp1.x);
                dx = Math.min(dx, r);
            }
            if (collidesX && collidesY && boundingBox.maxX >= aabb.minX && boundingBox.minX < aabb.minX && !floor)
            {
                r = Math.min(aabb.minX - boundingBox.maxX, temp1.x);
                dx = Math.min(dx, r);
            }
            if (collidesZ && collidesY && boundingBox.maxZ >= aabb.maxZ && boundingBox.minZ <= aabb.maxZ && !floor)
            {
                r = Math.max(aabb.maxZ - boundingBox.minZ, temp1.z);
                dz = Math.min(dz, r);
            }
            if (collidesZ && collidesY && boundingBox.maxZ >= aabb.minZ && boundingBox.minZ < aabb.minZ && !floor)
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
        e.setEntityBoundingBox(
                new AxisAlignedBB(e.getEntityBoundingBox().minX, boundingBox.minY, e.getEntityBoundingBox().minZ,
                        e.getEntityBoundingBox().maxX, boundingBox.maxY, e.getEntityBoundingBox().maxZ));

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
        boolean ret = false;
        if (e == null) return false;
        Vector3 ent = Vector3.getNewVector();
        ent.set(e);
        corners(true);
        if (e instanceof IMultibox)
        {
            IMultibox e1 = (IMultibox) e;
            e1.setOffsets();
            e1.setBoxes();
            Map<String, Matrix3> boxes = e1.getBoxes();
            Map<String, Vector3> offsets = e1.getOffsets();
            for (String s : boxes.keySet())
            {
                Vector3 boxOff = offsets.containsKey(s) ? offsets.get(s) : Vector3.empty;
                Matrix3 box = boxes.get(s);
                box.addOffsetTo(boxOff).addOffsetTo(ent);
                boolean hit = box.intersects(this);
                box.addOffsetTo(boxOff.reverse()).addOffsetTo(ent.reverse());
                boxOff.reverse();
                ent.reverse();
                Vector3.empty.clear();
                if (hit) { return true; }
            }

        }
        else
        {
            Matrix3 box = new Matrix3();
            box.set(e.getEntityBoundingBox());
            boolean hit = box.intersects(this);
            box = null;
            if (hit) { return true; }
        }
        return ret;
    }

    public Vector3 doTileCollision(IBlockAccess world, Entity e, Vector3 offset, Vector3 diffs)
    {
        return doTileCollision(world, e, offset, diffs, true);
    }

    public Vector3 doTileCollision(IBlockAccess world, Entity e, Vector3 offset, Vector3 diffs, boolean moveEntity)
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
            if (v.y > maxY) maxY = v.y;
            if (v.z > maxZ) maxZ = v.z;
            if (v.x < minX) minX = v.x;
            if (v.y < minY) minY = v.y;
            if (v.z < minZ) minZ = v.z;
        }
        if (e.getLowestRidingEntity() != null) maxY += (e.getLowestRidingEntity().height + e.getMountedYOffset());
        AxisAlignedBB boundingBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);

        double yShift = 0;

        double factor = 0.75d;
        double dx = max(maxX - minX, 1) / factor + e.motionX, dy = max(maxY - minY, 1) / factor + e.motionY,
                dz = max(maxZ - minZ, 1) / factor + e.motionZ, r;
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        dz = Math.abs(dz);
        dx = Math.max(dx, 1.5);
        dy = Math.max(dy, 1.5);
        dz = Math.max(dz, 1.5);
        int max = 50;
        if (diffs.mag() > max)
        {
            System.err.println(e + " Is too large");
            System.out.println(offset);
            System.err.println(Vector3.getNewVector().setToVelocity(e) + " " + diffs);
            System.err.println(diffs.equals(offset));
            new Exception().printStackTrace();
            dx = dy = dz = 1;
            diffs.clear();
            diffs.setVelocities(e);
        }
        temp1.set(diffs);
        AxisAlignedBB b1 = box.boxCentre().getAABB().expand(dx, dy, dz);
        List<AxisAlignedBB> aabbs = getCollidingBoxes(b1, e.worldObj, world);
        Vector3[][] subBoxes = box.splitBox();

        box = null;

        for (Vector3[] corner : subBoxes)
        {
            minX = Double.MAX_VALUE;
            minZ = Double.MAX_VALUE;
            minY = Double.MAX_VALUE;
            maxX = -Double.MAX_VALUE;
            maxZ = -Double.MAX_VALUE;
            maxY = -Double.MAX_VALUE;

            for (Vector3 v : corner)
            {
                if (v.x > maxX) maxX = v.x;
                if (v.y > maxY) maxY = v.y;
                if (v.z > maxZ) maxZ = v.z;
                if (v.x < minX) minX = v.x;
                if (v.y < minY) minY = v.y;
                if (v.z < minZ) minZ = v.z;
            }

            boundingBox = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);

            AxisAlignedBB b2;
            AxisAlignedBB[] boxes = aabbs.toArray(new AxisAlignedBB[aabbs.size()]);
            aabbs.clear();

            Arrays.sort(boxes, new Comparator<AxisAlignedBB>()
            {
                @Override
                public int compare(AxisAlignedBB o1, AxisAlignedBB o2)
                {
                    if (o1.minY == o1.minY)
                    {
                        if (o1.minX == o2.minX)
                        {
                            return MathHelper.floor_double(o1.minZ * 16) - MathHelper.floor_double(o2.minZ * 16);
                        }
                        else
                        {
                            return MathHelper.floor_double(o1.minX * 16) - MathHelper.floor_double(o2.minX * 16);
                        }
                    }
                    return MathHelper.floor_double(o1.minY * 16) - MathHelper.floor_double(o2.minY * 16);
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
                    if (MathHelper.floor_double(b2.maxX * factor) == MathHelper.floor_double(b1.maxX * factor)
                            && MathHelper.floor_double(b2.minX * factor) == MathHelper.floor_double(b1.minX * factor)
                            && MathHelper.floor_double(b2.maxZ * factor) == MathHelper.floor_double(b1.maxZ * factor)
                            && MathHelper.floor_double(b2.minZ * factor) == MathHelper.floor_double(b1.minZ * factor)
                            && Math.abs(b2.minY - b1.maxY) < maxY - minY)
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
            boolean dox = true, doz = true;
            if (dox) for (int i = 0; i < boxes.length; i++)
            {
                b1 = boxes[i];
                if (b1 == null) continue;
                for (int j = 0; j < boxes.length; j++)
                {
                    b2 = boxes[j];
                    if (i == j || b2 == null) continue;
                    factor = 16;
                    if (MathHelper.floor_double(b2.maxY * factor) == MathHelper.floor_double(b1.maxY * factor)
                            && MathHelper.floor_double(b2.minY * factor) == MathHelper.floor_double(b1.minY * factor)
                            && MathHelper.floor_double(b2.maxZ * factor) == MathHelper.floor_double(b1.maxZ * factor)
                            && MathHelper.floor_double(b2.minZ * factor) == MathHelper.floor_double(b1.minZ * factor)
                            && Math.abs(b2.minX - b1.maxX) < maxX - minX)
                    {
                        b1 = copyAndChange(b1, 3, b2.maxX);
                        boxes[i] = b1;
                        boxes[j] = null;
                    }
                }
            }
            if (doz) for (int i = 0; i < boxes.length; i++)
            {
                b1 = boxes[i];
                if (b1 == null) continue;
                for (int j = 0; j < boxes.length; j++)
                {
                    b2 = boxes[j];
                    if (i == j || b2 == null) continue;
                    factor = 16;
                    if (MathHelper.floor_double(b2.maxY * factor) == MathHelper.floor_double(b1.maxY * factor)
                            && MathHelper.floor_double(b2.minY * factor) == MathHelper.floor_double(b1.minY * factor)
                            && MathHelper.floor_double(b2.maxX * factor) == MathHelper.floor_double(b1.maxX * factor)
                            && MathHelper.floor_double(b2.minX * factor) == MathHelper.floor_double(b1.minX * factor)
                            && Math.abs(b2.minZ - b1.maxZ) < maxZ - minZ)
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

            double yTop = Math.max(e.stepHeight + e.posY + 0.125 + yShift, maxY);

            for (AxisAlignedBB aabb : aabbs)
            {
                dx = 10e3;
                dz = 10e3;
                boolean collidesX = ((maxZ <= aabb.maxZ) && (maxZ >= aabb.minZ))
                        || ((minZ <= aabb.maxZ) && (minZ >= aabb.minZ)) || ((minZ <= aabb.minZ) && (maxZ >= aabb.maxZ));

                boolean collidesY = ((minY >= aabb.minY) && (minY <= aabb.maxY))
                        || ((maxY <= aabb.maxY) && (maxY >= aabb.minY)) || ((minY <= aabb.minY) && (maxY >= aabb.maxY));

                boolean collidesZ = ((maxX <= aabb.maxX) && (maxX >= aabb.minX))
                        || ((minX <= aabb.maxX) && (minX >= aabb.minX)) || ((minX <= aabb.minX) && (maxX >= aabb.maxX));

                collidesZ = collidesZ && (collidesX || collidesY);
                collidesX = collidesX && (collidesZ || collidesY);

                boolean floor = false;

                if (collidesX && collidesZ && yTop >= aabb.maxY
                        && boundingBox.minY - e.stepHeight - yShift <= aabb.maxY - diffs.y)
                {
                    floor = true;
                    if (diffs.y <= 0) temp1.y = Math.max(aabb.maxY - boundingBox.minY, temp1.y);
                }
                if (collidesX && collidesZ && boundingBox.maxY >= aabb.minY && boundingBox.minY < aabb.minY)
                {
                    temp1.y = Math.min(aabb.minY - boundingBox.maxY, temp1.y);
                }

                if (collidesX && collidesY && boundingBox.maxX >= aabb.maxX && boundingBox.minX <= aabb.maxX && !floor)
                {
                    r = Math.max(aabb.maxX - boundingBox.minX, temp1.x);
                    dx = Math.min(dx, r);
                }
                if (collidesX && collidesY && boundingBox.maxX >= aabb.minX && boundingBox.minX < aabb.minX && !floor)
                {
                    r = Math.min(aabb.minX - boundingBox.maxX, temp1.x);
                    dx = Math.min(dx, r);
                }
                if (collidesZ && collidesY && boundingBox.maxZ >= aabb.maxZ && boundingBox.minZ <= aabb.maxZ && !floor)
                {
                    r = Math.max(aabb.maxZ - boundingBox.minZ, temp1.z);
                    dz = Math.min(dz, r);
                }
                if (collidesZ && collidesY && boundingBox.maxZ >= aabb.minZ && boundingBox.minZ < aabb.minZ && !floor)
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
            if (moveEntity) e.setEntityBoundingBox(
                    new AxisAlignedBB(e.getEntityBoundingBox().minX, boundingBox.minY, e.getEntityBoundingBox().minZ,
                            e.getEntityBoundingBox().maxX, boundingBox.maxY, e.getEntityBoundingBox().maxZ));

        }
        return temp1;
    }

    public boolean doTileCollision(IBlockAccess world, Vector3 location, Entity e, Vector3 diffs)
    {
        Vector3 diffs1 = Vector3.getNewVector().set(diffs);
        Vector3 off = Vector3.getNewVector().set(location);

        diffs1.set(doTileCollision(world, e, off, diffs, false));
        double x = diffs1.x, y = diffs1.y, z = diffs1.z;
        return !(x == diffs.x && (y == diffs.y || Math.abs(y - diffs.y) < 0.5) && z == diffs.z);
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
        int i = MathHelper.floor_double(box.minX);
        int j = MathHelper.floor_double(box.maxX + 1.0D);
        int k = MathHelper.floor_double(box.minY);
        int l = MathHelper.floor_double(box.maxY + 1.0D);
        int i1 = MathHelper.floor_double(box.minZ);
        int j1 = MathHelper.floor_double(box.maxZ + 1.0D);

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
                        iblockstate.addCollisionBoxToList(world, blockpos, box, collidingBoundingBoxes, null);
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
