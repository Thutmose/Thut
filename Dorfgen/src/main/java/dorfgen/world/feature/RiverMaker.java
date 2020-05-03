package dorfgen.world.feature;

import static net.minecraft.util.Direction.EAST;
import static net.minecraft.util.Direction.NORTH;
import static net.minecraft.util.Direction.SOUTH;
import static net.minecraft.util.Direction.WEST;

import java.awt.Color;
import java.util.HashSet;

import dorfgen.Dorfgen;
import dorfgen.conversion.DorfMap;
import dorfgen.conversion.DorfMap.Site;
import dorfgen.conversion.Interpolator.BicubicInterpolator;
import dorfgen.conversion.SiteStructureGenerator;
import dorfgen.conversion.SiteStructureGenerator.RiverExit;
import dorfgen.conversion.SiteStructureGenerator.SiteStructures;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;

public class RiverMaker
{
    public static BicubicInterpolator bicubicInterpolator = new BicubicInterpolator();
    int                               scale;
    private final DorfMap             dorfs;

    public RiverMaker(final IWorld world)
    {
        this.dorfs = Dorfgen.instance.getDorfs(world);
        this.scale = this.dorfs.scale;
    }

    public void makeRiversForChunk(final IChunk primer, final BlockPos.Mutable pos)
    {
        final int chunkX = primer.getPos().x;
        final int chunkZ = primer.getPos().z;
        final int x = chunkX * 16 - Dorfgen.shift.getX();
        final int z = chunkZ * 16 - Dorfgen.shift.getZ();
        int x1, z1, h;
        for (int i1 = 0; i1 < 16; i1++)
            for (int k1 = 0; k1 < 16; k1++)
            {
                x1 = x + i1;
                z1 = z + k1;
                if (x1 >= this.dorfs.waterMap.length || z1 >= this.dorfs.waterMap[0].length) h = 1;
                else h = RiverMaker.bicubicInterpolator.interpolate(this.dorfs.elevationMap, x + i1, z + k1,
                        this.scale);
                final boolean river = this.isInRiver(x1, z1);
                if (!river) continue;
                final int j = h - 1;
                pos.setPos(i1, j, k1);
                primer.setBlockState(pos, Blocks.WATER.getDefaultState(), false);
                pos.setPos(i1, j - 1, k1);
                primer.setBlockState(pos, Blocks.WATER.getDefaultState(), false);
                pos.setPos(i1, j - 2, k1);
                primer.setBlockState(pos, Blocks.WATER.getDefaultState(), false);

            }
    }

    Color       STRMAPRIVER = new Color(0, 192, 255);
    Direction[] DIRS        = { EAST, WEST, NORTH, SOUTH };

    public boolean[] getRiverDirection(final int xAbs, final int zAbs)
    {
        final boolean[] ret = new boolean[4];

        if (!this.isRiver(xAbs, zAbs)) return ret;
        if (this.isRiver(xAbs - this.scale, zAbs)) ret[1] = true;
        if (this.isRiver(xAbs + this.scale, zAbs)) ret[0] = true;
        if (this.isRiver(xAbs, zAbs - this.scale)) ret[2] = true;
        if (this.isRiver(xAbs, zAbs + this.scale)) ret[3] = true;
        return ret;
    }

    private boolean isRiver(final int x, final int z)
    {
        final int kx = x / this.scale;// Abs/(scale);
        final int kz = z / this.scale;// Abs/(scale);
        final int key = kx + 8192 * kz;
        if (kx >= this.dorfs.waterMap.length || kz >= this.dorfs.waterMap[0].length) return false;

        int rgb = this.dorfs.structureMap[kx][kz];
        final Color col1 = new Color(rgb);

        rgb = this.dorfs.riverMap[kx][kz];

        final Color col2 = new Color(rgb);

        final Color WHITE = new Color(255, 255, 255);

        final boolean river = col1.equals(this.STRMAPRIVER) || !WHITE.equals(col2) && col2.getBlue() > 0;
        if (river) return river;

        final HashSet<Site> ret = this.dorfs.sitesByCoord.get(key);

        if (ret != null) for (final Site s : ret)
        {
            if (!s.isInSite(x, z)) continue;

            final SiteStructures structs = this.dorfs.structureGen.getStructuresForSite(s);
            if (!structs.rivers.isEmpty()) for (final RiverExit riv : structs.rivers)
            {
                final int[] exit = riv.getEdgeMid(s, this.scale);
                final int dx = exit[0] - x;
                final int dz = exit[1] - z;
                if (dx * dx + dz * dz < this.scale * this.scale / 4) return true;
            }
        }

        return river;
    }

    public boolean isInRiver(int x1, int z1)
    {
        final int x = x1, z = z1;
        boolean river = false;
        final int kx = x1 / this.scale;
        final int kz = z1 / this.scale;
        final int offset = this.scale / 2;
        int key = kx + 8192 * kz;
        Site site;
        HashSet<Site> ret = this.dorfs.sitesByCoord.get(key);
        boolean hasRivers = false;
        if (ret != null) for (final Site s : ret)
        {
            if (!s.isInSite(x1, z1)) continue;

            final SiteStructures structs = this.dorfs.structureGen.getStructuresForSite(s);
            if (!structs.rivers.isEmpty())
            {
                hasRivers = true;
                break;
            }
        }
        final boolean[] dirs = this.getRiverDirection(x1, z1);
        final int width = 3 * this.scale / SiteStructureGenerator.SITETOBLOCK;
        river = dirs[0] || dirs[1] || dirs[2] || dirs[3];
        int[] point1 = null;
        int[] point2 = null;
        int[] point3 = null;
        int[] point4 = null;

        if (river && !hasRivers)
        {
            x1 = kx * this.scale;
            z1 = kz * this.scale;
            if (dirs[3])
            {
                key = kx + 8192 * (kz + 1);
                ret = this.dorfs.sitesByCoord.get(key);
                int[] nearest = null;
                if (ret != null) for (final Site s : ret)
                {
                    site = s;
                    final SiteStructures stuff = this.dorfs.structureGen.getStructuresForSite(site);

                    int[] temp;
                    int dist = Integer.MAX_VALUE;
                    for (final RiverExit exit : stuff.rivers)
                    {
                        temp = exit.getEdgeMid(site, this.scale);
                        final int tempDist = (temp[0] - x1) * (temp[0] - x1) + (temp[1] - z1) * (temp[1] - z1);
                        if (tempDist < dist)
                        {
                            nearest = temp;
                            dist = tempDist;
                        }
                    }
                }
                if (nearest == null) nearest = new int[] { kx * this.scale + offset, (kz + 1) * this.scale };
                if (ret == null || this.isRiver(nearest[0], nearest[1])) point1 = nearest;
            }
            if (dirs[1])
            {
                key = kx - 1 + 8192 * kz;
                ret = this.dorfs.sitesByCoord.get(key);
                int[] nearest = null;
                if (ret != null) for (final Site s : ret)
                {
                    site = s;
                    final SiteStructures stuff = this.dorfs.structureGen.getStructuresForSite(site);

                    int[] temp;
                    int dist = Integer.MAX_VALUE;
                    for (final RiverExit exit : stuff.rivers)
                    {
                        temp = exit.getEdgeMid(site, this.scale);
                        final int tempDist = (temp[0] - x1) * (temp[0] - x1) + (temp[1] - z1) * (temp[1] - z1);
                        if (tempDist < dist)
                        {
                            nearest = temp;
                            dist = tempDist;
                        }
                    }
                }
                if (nearest == null) nearest = new int[] { (kx - 1) * this.scale + this.scale, kz * this.scale
                        + offset };
                if (ret == null || this.isRiver(nearest[0], nearest[1])) point2 = nearest;
            }
            if (dirs[2])
            {
                key = kx + 8192 * (kz - 1);
                ret = this.dorfs.sitesByCoord.get(key);
                int[] nearest = null;
                if (ret != null) for (final Site s : ret)
                {
                    site = s;
                    final SiteStructures stuff = this.dorfs.structureGen.getStructuresForSite(site);

                    int[] temp;
                    int dist = Integer.MAX_VALUE;
                    for (final RiverExit exit : stuff.rivers)
                    {
                        temp = exit.getEdgeMid(site, this.scale);
                        final int tempDist = (temp[0] - x1) * (temp[0] - x1) + (temp[1] - z1) * (temp[1] - z1);
                        if (tempDist < dist)
                        {
                            nearest = temp;
                            dist = tempDist;
                        }
                    }
                }
                if (nearest == null) nearest = new int[] { kx * this.scale + offset, (kz - 1) * this.scale
                        + this.scale };
                if (ret == null || this.isRiver(nearest[0], nearest[1])) // System.out.println(Arrays.toString(nearest));
                    point3 = nearest;
            }
            if (dirs[0])
            {
                key = kx + 1 + 8192 * kz;
                ret = this.dorfs.sitesByCoord.get(key);
                int[] nearest = null;
                if (ret != null) for (final Site s : ret)
                {
                    site = s;
                    final SiteStructures stuff = this.dorfs.structureGen.getStructuresForSite(site);

                    int[] temp;
                    int dist = Integer.MAX_VALUE;
                    for (final RiverExit exit : stuff.rivers)
                    {
                        temp = exit.getEdgeMid(site, this.scale);
                        final int tempDist = (temp[0] - x1) * (temp[0] - x1) + (temp[1] - z1) * (temp[1] - z1);
                        if (tempDist < dist)
                        {
                            nearest = temp;
                            dist = tempDist;
                        }
                    }
                }
                if (nearest == null) nearest = new int[] { (kx + 1) * this.scale, kz * this.scale + offset };
                if (ret == null || this.isRiver(nearest[0], nearest[1])) point4 = nearest;

            }
        }

        try
        {
            // System.out.println(Arrays.toString(point1)+"
            // "+Arrays.toString(point2) +" "+Arrays.toString(point3)+"
            // "+Arrays.toString(point4)+" "+river);
        }
        catch (final Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (point1 != null && point2 != null)
        {
            Vec3d dir = new Vec3d(point1[0] - point2[0], 0, point1[1] - point2[1]);
            final double distance = dir.length();
            dir = dir.normalize();
            for (double i = 0; i < distance; i++)
            {
                final int tx = point2[0] + (int) (dir.x * i) - x;
                final int tz = point2[1] + (int) (dir.z * i) - z;
                if (Math.abs(tx) < width && Math.abs(tz) < width) return true;
            }
        }
        if (point1 != null && point3 != null)
        {
            Vec3d dir = new Vec3d(point1[0] - point3[0], 0, point1[1] - point3[1]);
            final double distance = dir.length();
            dir = dir.normalize();
            for (double i = 0; i < distance; i++)
            {
                int tx = point3[0] + (int) (dir.x * i) - x;
                int tz = point3[1] + (int) (dir.z * i) - z;
                tx = Math.abs(tx);
                tz = Math.abs(tz);
                if (tx < width && tz < width) return true;
            }
        }
        if (point1 != null && point4 != null)
        {
            Vec3d dir = new Vec3d(point1[0] - point4[0], 0, point1[1] - point4[1]);
            final double distance = dir.length();
            dir = dir.normalize();
            for (double i = 0; i < distance; i++)
            {
                final int tx = point4[0] + (int) (dir.x * i) - x;
                final int tz = point4[1] + (int) (dir.z * i) - z;
                if (Math.abs(tx) < width && Math.abs(tz) < width) return true;
            }
        }
        if (point2 != null && point3 != null)
        {
            Vec3d dir = new Vec3d(point2[0] - point3[0], 0, point2[1] - point3[1]);
            final double distance = dir.length();
            dir = dir.normalize();
            for (double i = 0; i < distance; i++)
            {
                final int tx = point3[0] + (int) (dir.x * i) - x;
                final int tz = point3[1] + (int) (dir.z * i) - z;
                if (Math.abs(tx) < width && Math.abs(tz) < width) return true;
            }
        }
        if (point2 != null && point4 != null)
        {
            Vec3d dir = new Vec3d(point2[0] - point4[0], 0, point2[1] - point4[1]);
            final double distance = dir.length();
            dir = dir.normalize();
            for (double i = 0; i < distance; i++)
            {
                final int tx = point4[0] + (int) (dir.x * i) - x;
                final int tz = point4[1] + (int) (dir.z * i) - z;
                if (Math.abs(tx) < width && Math.abs(tz) < width) return true;
            }
        }
        if (point4 != null && point3 != null)
        {
            Vec3d dir = new Vec3d(point4[0] - point3[0], 0, point4[1] - point3[1]);
            final double distance = dir.length();
            dir = dir.normalize();
            for (double i = 0; i < distance; i++)
            {
                final int tx = point3[0] + (int) (dir.x * i) - x;
                final int tz = point3[1] + (int) (dir.z * i) - z;
                if (Math.abs(tx) < width && Math.abs(tz) < width) return true;
            }
        }

        return false;
    }
}
