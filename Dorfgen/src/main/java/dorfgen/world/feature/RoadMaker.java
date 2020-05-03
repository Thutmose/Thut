package dorfgen.world.feature;

import static net.minecraft.util.Direction.EAST;
import static net.minecraft.util.Direction.NORTH;
import static net.minecraft.util.Direction.SOUTH;
import static net.minecraft.util.Direction.WEST;

import java.util.HashSet;
import java.util.Set;

import dorfgen.Dorfgen;
import dorfgen.conversion.DorfMap;
import dorfgen.conversion.DorfMap.ConstructionType;
import dorfgen.conversion.DorfMap.Site;
import dorfgen.conversion.DorfMap.WorldConstruction;
import dorfgen.conversion.Interpolator.BicubicInterpolator;
import dorfgen.conversion.SiteMapColours;
import dorfgen.conversion.SiteStructureGenerator;
import dorfgen.conversion.SiteStructureGenerator.RoadExit;
import dorfgen.conversion.SiteStructureGenerator.SiteStructures;
import dorfgen.conversion.SiteStructureGenerator.StructureSpace;
import dorfgen.conversion.SiteTerrain;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.IChunk;

public class RoadMaker
{
    public static BicubicInterpolator bicubicInterpolator = new BicubicInterpolator();
    final DorfMap                     dorfs;
    int                               scale;

    public RoadMaker(final IWorld world)
    {
        this.dorfs = Dorfgen.instance.getDorfs(world);
        this.scale = this.dorfs.scale;
    }

    public boolean shouldConstruct(final int chunkX, final int chunkZ, final ConstructionType type)
    {
        int x = chunkX * 16;
        int z = chunkZ * 16;
        x -= Dorfgen.shift.getX();
        z -= Dorfgen.shift.getZ();

        if (x >= 0 && z >= 0 && (x + 16) / this.scale <= this.dorfs.biomeMap.length && (z + 16)
                / this.scale <= this.dorfs.biomeMap[0].length)
        {
            final int x1 = x / this.scale / 16;
            final int z1 = z / this.scale / 16;
            final int key = x1 + 2048 * z1;

            if (this.dorfs.constructionsByCoord.containsKey(key))
            {
                for (final WorldConstruction construct : this.dorfs.constructionsByCoord.get(key))
                    if (construct.type == type && construct.isInConstruct(x, 0, z)) return true;
                return false;
            }
            return false;
        }
        return false;
    }

    private SiteMapColours getSiteMapColour(final Site s, final int x, final int z)
    {
        final int offset = this.scale / 2;
        int rgb;

        if (s.rgbmap == null || !s.isInSite(x, z)) return null;

        final int shiftX = (x - s.corners[0][0] * this.scale - offset) * SiteStructureGenerator.SITETOBLOCK
                / this.scale;
        final int shiftZ = (z - s.corners[0][1] * this.scale - offset) * SiteStructureGenerator.SITETOBLOCK
                / this.scale;
        if (shiftX >= s.rgbmap.length || shiftZ >= s.rgbmap[0].length) return null;
        if (shiftX < 0 || shiftZ < 0) return null;
        rgb = s.rgbmap[shiftX][shiftZ];
        final SiteMapColours siteCol = SiteMapColours.getMatch(rgb);

        return siteCol;
    }

    public void buildSites(final IChunk blocks, final Mutable pos)
    {
        final int chunkX = blocks.getPos().x;
        final int chunkZ = blocks.getPos().z;
        if (this.dorfs.structureMap.length == 0) return;
        final SiteStructureGenerator structureGen = this.dorfs.structureGen;
        final int x = chunkX * 16 - Dorfgen.shift.getX();
        final int z = chunkZ * 16 - Dorfgen.shift.getZ();
        int x1, z1, h;
        final boolean flag = false;

        for (int i1 = 0; i1 < 16; i1++)
            for (int k1 = 0; k1 < 16; k1++)
            {
                x1 = x + i1;// / scale;
                z1 = z + k1;// / scale;

                final Set<Site> sites = this.dorfs.getSiteForCoords(x1, z1);

                if (sites == null) continue;

                for (final Site s : sites)
                {
                    final SiteMapColours siteCol = this.getSiteMapColour(s, x1, z1);
                    if (siteCol == null) continue;

                    h = RoadMaker.bicubicInterpolator.interpolate(this.dorfs.elevationMap, x1, z1, this.scale);
                    int j = h - 1;
                    final SiteStructures structs = structureGen.getStructuresForSite(s);
                    final StructureSpace struct = structs.getStructure(x1, z1, this.scale);
                    if (struct != null)
                    {
                        j = struct.getFloor(s, this.scale) - 1;
                        h = j + 1;
                        continue;// TODO move stuff into SiteStructureGenerator
                    }

                    final BlockState[] repBlocks = SiteMapColours.getSurfaceBlocks(siteCol);

                    pos.setPos(i1, j, k1);

                    BlockState surface = repBlocks[1];
                    final BlockState above = repBlocks[2];

                    final boolean wall = siteCol == SiteMapColours.TOWNWALL;
                    final boolean roof = siteCol.toString().contains("ROOF");
                    final boolean farm = siteCol.toString().contains("FARM");
                    // TODO see about modifying the biomes?
                    // if (farm) biomes[i1 + 16 * k1] = BiomeGenBase.plains;
                    if (farm && blocks.getBiomes() != null) blocks.getBiomes().biomes[i1 + 16 * k1] = Biomes.PLAINS;

                    if (surface == null && siteCol.toString().contains("ROOF")) surface = Blocks.BRICKS
                            .getDefaultState();

                    if (surface == null) // || blocks[index - 1] == Blocks.water
                                         // || blocks[index] == Blocks.water)
                        continue;
                    blocks.setBlockState(pos, surface, flag);
                    pos.setPos(i1, j - 1, k1);
                    blocks.setBlockState(pos, repBlocks[0], flag);
                    pos.setPos(i1, j + 1, k1);
                    if (above != null) blocks.setBlockState(pos, above, flag);
                    final boolean tower = siteCol.toString().contains("TOWER");
                    if (wall || roof)
                    {
                        int j1 = j;
                        final int num = tower ? 10 : 3;
                        while (j1 < h + 1)
                        {
                            j1 = j1 + 1;
                            pos.setPos(i1, j1, k1);
                            blocks.setBlockState(pos, Blocks.AIR.getDefaultState(), flag);
                            pos.setPos(i1, h + num, k1);
                            blocks.setBlockState(pos, surface, flag);
                            ;
                        }
                        j1 = j;
                        if (wall) while (j1 < h + num)
                        {
                            j1 = j1 + 1;
                            pos.setPos(i1, j1, k1);
                            blocks.setBlockState(pos, surface, flag);
                        }
                    }

                    if (siteCol.toString().contains("ROAD")) if (i1 > 0 && i1 < 15 && k1 > 0 && k1 < 15)
                    {
                        h = RoadMaker.bicubicInterpolator.interpolate(this.dorfs.elevationMap, x1, z1, this.scale);
                        int h2;

                        SiteMapColours px, nx, pz, nz;
                        px = this.getSiteMapColour(s, x1 + 1, z1);
                        nx = this.getSiteMapColour(s, x1 - 1, z1);
                        pz = this.getSiteMapColour(s, x1, z1 + 1);
                        nz = this.getSiteMapColour(s, x1, z1 - 1);

                        if (px != null && !px.toString().contains("ROAD") && z1 % 8 == 0)
                        {
                            h2 = RoadMaker.bicubicInterpolator.interpolate(this.dorfs.elevationMap, x1 + 1, z1,
                                    this.scale);
                            pos.setPos(i1 + 1, h2, k1);
                            blocks.setBlockState(pos, Blocks.TORCH.getDefaultState(), flag);
                        }

                        if (nx != null && !nx.toString().contains("ROAD") && z1 % 8 == 0)
                        {
                            h2 = RoadMaker.bicubicInterpolator.interpolate(this.dorfs.elevationMap, x1 - 1, z1,
                                    this.scale);
                            pos.setPos(i1 - 1, h2, k1);
                            blocks.setBlockState(pos, Blocks.TORCH.getDefaultState(), flag);
                        }

                        if (pz != null && !pz.toString().contains("ROAD") && x1 % 8 == 0)
                        {
                            h2 = RoadMaker.bicubicInterpolator.interpolate(this.dorfs.elevationMap, x1, z1 + 1,
                                    this.scale);
                            pos.setPos(i1, h2, k1 + 1);
                            blocks.setBlockState(pos, Blocks.TORCH.getDefaultState(), flag);
                        }

                        if (nz != null && !nz.toString().contains("ROAD") && x1 % 8 == 0)
                        {
                            h2 = RoadMaker.bicubicInterpolator.interpolate(this.dorfs.elevationMap, x1, z1 - 1,
                                    this.scale);
                            pos.setPos(i1, h2, k1 - 1);
                            blocks.setBlockState(pos, Blocks.TORCH.getDefaultState(), flag);
                        }
                    }
                }
            }
    }

    private final Direction[] DIRS        = { EAST, WEST, NORTH, SOUTH };
    private final double[]    DIR_TO_RELX = { this.scale, 0., this.scale / 2., this.scale / 2. };
    private final double[]    DIR_TO_RELZ = { this.scale / 2., this.scale / 2., 0, this.scale };

    private int dirToIndex(final Direction dir)
    {
        if (dir == EAST) return 0;
        if (dir == WEST) return 1;
        if (dir == NORTH) return 2;
        if (dir == SOUTH) return 3;
        return 0;
    }

    private void safeSetToRoad(final int x, final int z, final int h, final IChunk blocks, final Block block,
            final Mutable pos)
    {
        final int chunkX = blocks.getPos().x;
        final int chunkZ = blocks.getPos().z;

        final int x1 = x - chunkX;
        final int z1 = z - chunkZ;

        pos.setPos(x1, h - 1, z1);
        blocks.setBlockState(pos, block.getDefaultState(), false);
        blocks.setBlockState(pos.setPos(x1, h - 2, z1), Blocks.COBBLESTONE.getDefaultState(), false);
        blocks.setBlockState(pos.setPos(x1, h - 3, z1), Blocks.COBBLESTONE.getDefaultState(), false);
        blocks.setBlockState(pos.setPos(x1, h, z1), Blocks.AIR.getDefaultState(), false);
        blocks.setBlockState(pos.setPos(x1, h + 1, z1), Blocks.AIR.getDefaultState(), false);
        blocks.setBlockState(pos.setPos(x1, h + 2, z1), Blocks.AIR.getDefaultState(), false);

    }

    private void safeSetToRoad(final int x, final int z, final int h, final IChunk blocks, final Mutable pos)
    {
        this.safeSetToRoad(x, z, h, blocks, Blocks.GRASS_PATH, pos);
    }

    private static final int ROADWIDTH = 3;

    private void genSingleRoad(final Direction begin, final Direction end, final int x, final int z,
            final IChunk blocks, final Mutable pos)
    {
        final int nearestEmbarkX = x - x % this.scale;
        final int nearestEmbarkZ = z - z % this.scale;
        double interX, interZ;
        int nearestX, nearestZ;
        int h;
        final double startX = this.DIR_TO_RELX[this.dirToIndex(begin)];
        final double startZ = this.DIR_TO_RELZ[this.dirToIndex(begin)];
        final double endX = this.DIR_TO_RELX[this.dirToIndex(end)];
        final double endZ = this.DIR_TO_RELZ[this.dirToIndex(end)];

        final double c = this.scale / 2.;

        for (double i = -0.2; i <= 1.2; i += 0.02)
        {
            interX = (1. - i) * (1. - i) * startX + 2. * (1. - i) * i * c + i * i * endX;
            interZ = (1. - i) * (1. - i) * startZ + 2. * (1. - i) * i * c + i * i * endZ;

            nearestX = (int) interX;
            nearestZ = (int) interZ;

            for (int w = -RoadMaker.ROADWIDTH; w <= RoadMaker.ROADWIDTH; w++)
                for (int w2 = -RoadMaker.ROADWIDTH; w2 <= RoadMaker.ROADWIDTH; w2++)
                {
                    h = RoadMaker.bicubicInterpolator.interpolate(this.dorfs.elevationMap, nearestX + nearestEmbarkX
                            + w, nearestZ + nearestEmbarkZ + w2, this.scale);
                    this.safeSetToRoad(nearestX + nearestEmbarkX + w, nearestZ + nearestEmbarkZ + w2, h, blocks, pos);
                }
        }
    }

    private void genSingleRoadToPos(final int x, final int z, final int toRoadX, final int toRoadZ, final IChunk blocks,
            final Mutable pos)
    {
        final int nearestEmbarkX = x - x % this.scale;
        final int nearestEmbarkZ = z - z % this.scale;
        double interX, interZ;
        int nearestX, nearestZ;
        int h;

        final double c = this.scale / 2.;

        double startX = c, startZ = c, distSqr;
        double minDistSqr = Integer.MAX_VALUE;

        final double endX = toRoadX - nearestEmbarkX;
        final double endZ = toRoadZ - nearestEmbarkZ;

        final boolean[] dirs = this.getRoadDirection(nearestEmbarkX, nearestEmbarkZ);

        for (final Direction dir : this.DIRS)
        {
            if (!dirs[this.dirToIndex(dir)]) continue;
            distSqr = (this.DIR_TO_RELX[this.dirToIndex(dir)] - endX) * (this.DIR_TO_RELX[this.dirToIndex(dir)] - endX)
                    + (this.DIR_TO_RELZ[this.dirToIndex(dir)] - endZ) * (this.DIR_TO_RELZ[this.dirToIndex(dir)] - endZ);
            if (distSqr < minDistSqr)
            {
                minDistSqr = distSqr;
                startX = this.DIR_TO_RELX[this.dirToIndex(dir)];
                startZ = this.DIR_TO_RELZ[this.dirToIndex(dir)];
            }

        }

        for (double i = -0.05; i <= 1.05; i += 0.01)
        {
            interX = startX * (1.0 - i) + endX * i;
            interZ = startZ * (1.0 - i) + endZ * i;

            nearestX = (int) interX;
            nearestZ = (int) interZ;

            for (int w = -RoadMaker.ROADWIDTH; w <= RoadMaker.ROADWIDTH; w++)
                for (int w2 = -RoadMaker.ROADWIDTH; w2 <= RoadMaker.ROADWIDTH; w2++)
                {
                    if ((w < 1 - RoadMaker.ROADWIDTH || w > RoadMaker.ROADWIDTH - 1) && (w2 < 1 - RoadMaker.ROADWIDTH
                            || w2 > RoadMaker.ROADWIDTH - 1)) continue; // take
                                                                        // the
                                                                        // corners
                                                                        // off
                    h = RoadMaker.bicubicInterpolator.interpolate(this.dorfs.elevationMap, nearestX + nearestEmbarkX
                            + w, nearestZ + nearestEmbarkZ + w2, this.scale);
                    this.safeSetToRoad(nearestX + nearestEmbarkX + w, nearestZ + nearestEmbarkZ + w2, h, blocks, pos);
                }
        }
    }

    private boolean isInSite(final int x, final int z)
    {
        final int kx = x / this.scale;
        final int kz = z / this.scale;

        final int key = kx + 8192 * kz;

        final HashSet<Site> sites = this.dorfs.sitesByCoord.get(key);

        if (sites != null) for (final Site site : sites)
        {
            final SiteStructures structs = this.dorfs.structureGen.getStructuresForSite(site);
            if (structs != null && !structs.roads.isEmpty()) return true;
        }

        return false;
    }

    public int[] getClosestRoadEnd(final int x, final int z, final Site site)
    {
        int[] edge = null;
        int[] result = null;

        int minDistanceSqr = Integer.MAX_VALUE;

        final SiteStructures structures = this.dorfs.structureGen.getStructuresForSite(site);
        for (final RoadExit exit : structures.roads)
        {
            edge = exit.getEdgeMid(site, this.scale);
            if (minDistanceSqr > (x - edge[0]) * (x - edge[0]) + (z - edge[1]) * (z - edge[1]))
            {
                minDistanceSqr = (x - edge[0]) * (x - edge[0]) + (z - edge[1]) * (z - edge[1]);
                result = edge;
            }
        }

        return result;
    }

    private int roundToEmbark(final int a)
    {
        return a - a % this.scale;
    }

    static private final int ROAD_SEARCH_AREA = 3;

    private boolean isNearSiteRoadEnd(final int x, final int z)
    {
        final HashSet<Site> sites = new HashSet<>();
        HashSet<Site> subSites;

        final int kx = x / this.scale;
        final int kz = z / this.scale;

        for (int xsearch = -RoadMaker.ROAD_SEARCH_AREA; xsearch <= RoadMaker.ROAD_SEARCH_AREA; xsearch++)
            for (int zsearch = -RoadMaker.ROAD_SEARCH_AREA; zsearch <= RoadMaker.ROAD_SEARCH_AREA; zsearch++)
            {
                subSites = this.dorfs.sitesByCoord.get(kx + xsearch + 8192 * (kz + zsearch));
                if (subSites != null) sites.addAll(subSites);
            }

        if (sites.size() == 0) return false;

        for (final Site site : sites)
        {
            final int[] edge = this.getClosestRoadEnd(x, z, site);
            if (edge == null) continue;
            if (this.roundToEmbark(x) == this.roundToEmbark(edge[0]) && this.roundToEmbark(z) == this.roundToEmbark(
                    edge[1]) || this.roundToEmbark(x) - this.scale == this.roundToEmbark(edge[0]) && this.roundToEmbark(
                            z) == this.roundToEmbark(edge[1]) || this.roundToEmbark(x) + this.scale == this
                                    .roundToEmbark(edge[0]) && this.roundToEmbark(z) == this.roundToEmbark(edge[1])
                    || this.roundToEmbark(x) == this.roundToEmbark(edge[0]) && this.roundToEmbark(z)
                            + this.scale == this.roundToEmbark(edge[1]) || this.roundToEmbark(x) == this.roundToEmbark(
                                    edge[0]) && this.roundToEmbark(z) - this.scale == this.roundToEmbark(edge[1]))
                return true;
        }

        return false;
    }

    private int[] getSiteRoadEnd(final int x, final int z)
    {
        final HashSet<Site> sites = new HashSet<>();
        HashSet<Site> subSites;

        final int kx = x / this.scale;
        final int kz = z / this.scale;

        for (int xsearch = -RoadMaker.ROAD_SEARCH_AREA; xsearch <= RoadMaker.ROAD_SEARCH_AREA; xsearch++)
            for (int zsearch = -RoadMaker.ROAD_SEARCH_AREA; zsearch <= RoadMaker.ROAD_SEARCH_AREA; zsearch++)
            {
                subSites = this.dorfs.sitesByCoord.get(kx + xsearch + 8192 * (kz + zsearch));
                if (subSites != null) sites.addAll(subSites);
            }

        if (sites.size() == 0) return null;

        for (final Site site : sites)
        {
            final int[] edge = this.getClosestRoadEnd(x, z, site);
            if (edge == null) continue;
            if (this.roundToEmbark(x) == this.roundToEmbark(edge[0]) && this.roundToEmbark(z) == this.roundToEmbark(
                    edge[1]) || this.roundToEmbark(x) - this.scale == this.roundToEmbark(edge[0]) && this.roundToEmbark(
                            z) == this.roundToEmbark(edge[1]) || this.roundToEmbark(x) + this.scale == this
                                    .roundToEmbark(edge[0]) && this.roundToEmbark(z) == this.roundToEmbark(edge[1])
                    || this.roundToEmbark(x) == this.roundToEmbark(edge[0]) && this.roundToEmbark(z)
                            + this.scale == this.roundToEmbark(edge[1]) || this.roundToEmbark(x) == this.roundToEmbark(
                                    edge[0]) && this.roundToEmbark(z) - this.scale == this.roundToEmbark(edge[1]))
                return edge;
        }

        return null;
    }

    private void genRoads(final int x, final int z, final IChunk blocks, final Mutable pos)
    {
        final int nearestEmbarkX = x - x % this.scale;
        final int nearestEmbarkZ = z - z % this.scale;

        final boolean dirs[] = this.getRoadDirection(nearestEmbarkX, nearestEmbarkZ);

        for (int i = 0; i < 3; i++)
            for (int j = i + 1; j < 4; j++)
                if (dirs[i] && dirs[j]) this.genSingleRoad(this.DIRS[i], this.DIRS[j], x, z, blocks, pos);
    }

    boolean hasRoad(final int x, final int z)
    {
        final HashSet<WorldConstruction> cons = this.dorfs.getConstructionsForCoords(x, z);

        if (cons == null || cons.isEmpty()) return false;

        for (final WorldConstruction con : cons)
            if (con.type == DorfMap.ConstructionType.ROAD) if (con.isInConstruct(x, 0, z)) return true;

        return false;
    }

    public void debugPrint(final int x, final int z)
    {
        final int embarkX = this.roundToEmbark(x);
        final int embarkZ = this.roundToEmbark(z);

        if (this.isInSite(x, z)) System.out.println("Embark location x: " + embarkX + " z: " + embarkZ
                + " is in a site");

        if (this.hasRoad(x, z)) System.out.println("Embark location x: " + embarkX + " z: " + embarkZ + " has a road");

        if (this.dorfs.getConstructionsForCoords(x, z) != null) for (final WorldConstruction constr : this.dorfs
                .getConstructionsForCoords(x, z))
            if (constr.isInConstruct(x, 0, z))
            {
                System.out.println("Location x: " + x + " z: " + z + " is in a construction");
                System.out.println("    Construction is " + constr.toString());
            }

        if (this.dorfs.getConstructionsForCoords(embarkX, embarkZ) != null)
            for (final WorldConstruction constr : this.dorfs.getConstructionsForCoords(embarkX, embarkZ))
            if (constr.isInConstruct(embarkX, 0, embarkZ))
            {
                System.out.println("Location x: " + embarkX + " z: " + embarkZ + " is in a construction");
                System.out.println("    Construction is " + constr.toString());
            }

        if (this.isNearSiteRoadEnd(x, z))
        {
            System.out.println("Embark location x: " + embarkX + " z: " + embarkZ + " is near a site road end");
            final int[] roadEnd = this.getSiteRoadEnd(x, z);
            System.out.println("Site road end is at x: " + roadEnd[0] + " z: " + roadEnd[1]);

            int minDistSqr = Integer.MAX_VALUE, dist;
            int x1, z1;
            int embarkX1 = 0, embarkZ1 = 0;
            final int roadEndX = roadEnd[0];
            final int roadEndZ = roadEnd[1];

            for (int xsearch = -RoadMaker.ROAD_SEARCH_AREA; xsearch <= RoadMaker.ROAD_SEARCH_AREA; xsearch++)
                for (int zsearch = -RoadMaker.ROAD_SEARCH_AREA; zsearch <= RoadMaker.ROAD_SEARCH_AREA; zsearch++)
                {
                    x1 = this.roundToEmbark(roadEndX + xsearch * this.scale);
                    z1 = this.roundToEmbark(roadEndZ + zsearch * this.scale);

                    if (this.isInSite(x1, z1)) continue;
                    if (!this.hasRoad(x1, z1)) continue;

                    dist = (x1 - roadEndX) * (x1 - roadEndX) + (z1 - roadEndZ) * (z1 - roadEndZ);

                    if (dist < minDistSqr)
                    {
                        minDistSqr = dist;
                        embarkX1 = x1;
                        embarkZ1 = z1;
                    }
                }
            if (minDistSqr != Integer.MAX_VALUE)
            {
                System.out.println("Nearest embark to road end found at x: " + embarkX1 + " z: " + embarkZ1);

                Direction closestdir = EAST;
                double distSqr2;
                double minDistSqr2 = Integer.MAX_VALUE;

                final double endX = roadEndX - embarkX;
                final double endZ = roadEndZ - embarkZ;

                final boolean[] dirs = this.getRoadDirection(embarkX, embarkZ);

                for (final Direction dir : this.DIRS)
                {
                    if (!dirs[this.dirToIndex(dir)]) continue;
                    distSqr2 = (this.DIR_TO_RELX[this.dirToIndex(dir)] - endX) * (this.DIR_TO_RELX[this.dirToIndex(dir)]
                            - endX) + (this.DIR_TO_RELZ[this.dirToIndex(dir)] - endZ) * (this.DIR_TO_RELZ[this
                                    .dirToIndex(dir)] - endZ);
                    if (distSqr2 < minDistSqr2)
                    {
                        minDistSqr2 = distSqr2;
                        closestdir = dir;
                    }
                }

                if (closestdir == EAST) System.out.println("Closest dir is east");
                if (closestdir == WEST) System.out.println("Closest dir is west");
                if (closestdir == NORTH) System.out.println("Closest dir is north");
                if (closestdir == SOUTH) System.out.println("Closest dir is south");

                System.out.println("    with distance " + minDistSqr2);
            }
        }
    }

    private void genRoadEndConnector(final int roadEndX, final int roadEndZ, final IChunk blocks, final Mutable pos)
    {
        int minDistSqr = Integer.MAX_VALUE, dist;
        int x1, z1;
        int embarkX = 0, embarkZ = 0;

        for (int xsearch = -RoadMaker.ROAD_SEARCH_AREA; xsearch <= RoadMaker.ROAD_SEARCH_AREA; xsearch++)
            for (int zsearch = -RoadMaker.ROAD_SEARCH_AREA; zsearch <= RoadMaker.ROAD_SEARCH_AREA; zsearch++)
            {
                x1 = this.roundToEmbark(roadEndX + xsearch * this.scale);
                z1 = this.roundToEmbark(roadEndZ + zsearch * this.scale);

                if (this.isInSite(x1, z1)) continue;
                if (!this.hasRoad(x1, z1)) continue;

                dist = (x1 - roadEndX) * (x1 - roadEndX) + (z1 - roadEndZ) * (z1 - roadEndZ);

                if (dist < minDistSqr)
                {
                    minDistSqr = dist;
                    embarkX = x1;
                    embarkZ = z1;
                }
            }

        if (minDistSqr != Integer.MAX_VALUE) this.genSingleRoadToPos(embarkX, embarkZ, roadEndX, roadEndZ, blocks, pos);
        else System.out.println("Search failed to generate attachment road at x: " + roadEndX + " z: " + roadEndZ);
    }

    public void buildRoads(final IChunk blocks, final Mutable pos)
    {
        final int chunkX = blocks.getPos().x;
        final int chunkZ = blocks.getPos().z;

        final int x = chunkX * 16 - Dorfgen.shift.getX();
        final int z = chunkZ * 16 - Dorfgen.shift.getZ();

        if (this.isNearSiteRoadEnd(x, z))
        {
            final int[] roadEnd = this.getSiteRoadEnd(x, z);
            this.genRoadEndConnector(roadEnd[0], roadEnd[1], blocks, pos);
        }

        if (this.isInSite(x, z)) return;

        this.genRoads(x - x % this.scale, z - z % this.scale, blocks, pos);

        if (x + 16 - (x + 16) % this.scale > x - x % this.scale)
        {
            if (z + 16 - (z + 16) % this.scale > z - z % this.scale) this.genRoads(x + 16 - (x + 16) % this.scale, z
                    + 16 - (z + 16) % this.scale, blocks, pos);
            else this.genRoads(x + 16 - (x + 16) % this.scale, z - z % this.scale, blocks, pos);
        }
        else if (z + 16 - (z + 16) % this.scale > z - z % this.scale) this.genRoads(x - x % this.scale, z + 16 - (z
                + 16) % this.scale, blocks, pos);
    }

    public boolean[] getRoadDirection(final int xAbs, final int zAbs)
    {
        final boolean[] ret = new boolean[4];
        final HashSet<WorldConstruction> constructs = this.dorfs.getConstructionsForCoords(xAbs, zAbs);

        if (constructs == null) return ret;

        for (final WorldConstruction con : constructs)
        {
            if (!con.isInConstruct(xAbs, 0, zAbs)) continue;

            if (con.isInConstruct(xAbs - this.scale, 0, zAbs)) ret[1] = true;
            if (con.isInConstruct(xAbs + this.scale, 0, zAbs)) ret[0] = true;
            if (con.isInConstruct(xAbs, 0, zAbs - this.scale)) ret[2] = true;
            if (con.isInConstruct(xAbs, 0, zAbs + this.scale)) ret[3] = true;
        }
        return ret;
    }

    public static Block getSurfaceBlockForSite(final SiteTerrain site, final int num)
    {
        switch (site)
        {
        case BUILDINGS:
            return num == 0 ? Blocks.BRICKS : null;
        case WALLS:
            return Blocks.STONE_BRICKS;
        case FARMYELLOW:
            return num == 0 ? Blocks.SAND : null;
        case FARMORANGE:
            return num == 0 ? Blocks.DIRT : null;
        case FARMLIMEGREEN:
            return num == 0 ? Blocks.CLAY : null;
        case FARMORANGELIGHT:
            return num == 0 ? Blocks.TERRACOTTA : null;
        case FARMGREEN:
            return num == 0 ? Blocks.GREEN_TERRACOTTA : null;
        default:
            return null;
        }
    }
}
