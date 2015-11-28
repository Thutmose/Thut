package dorfgen.worldgen;

import static dorfgen.WorldGenerator.scale;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.NORTH;
import static net.minecraft.util.EnumFacing.SOUTH;
import static net.minecraft.util.EnumFacing.WEST;

import java.util.HashSet;

import dorfgen.BlockRoadSurface;
import dorfgen.WorldGenerator;
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
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.ChunkPrimer;

public class WorldConstructionMaker
{
    public static BicubicInterpolator bicubicInterpolator = new BicubicInterpolator();
    final DorfMap                     dorfs;

    public WorldConstructionMaker()
    {
        dorfs = WorldGenerator.instance.dorfs;
    }

    public boolean shouldConstruct(int chunkX, int chunkZ, ConstructionType type)
    {
        int x = chunkX * 16;
        int z = chunkZ * 16;
        x -= WorldGenerator.shift.getX();
        z -= WorldGenerator.shift.getZ();

        if (x >= 0 && z >= 0 && (x + 16) / scale <= WorldGenerator.instance.dorfs.biomeMap.length
                && (z + 16) / scale <= WorldGenerator.instance.dorfs.biomeMap[0].length)
        {
            int x1 = (x / scale) / 16;
            int z1 = (z / scale) / 16;
            int key = (x1) + 2048 * (z1);

            if (DorfMap.constructionsByCoord.containsKey(key))
            {
                for (WorldConstruction construct : DorfMap.constructionsByCoord.get(key))
                {
                    if (construct.type == type && construct.isInConstruct(x, 0, z)) return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }

    private SiteMapColours getSiteMapColour(Site s, int x, int z)
    {
        int offset = scale / 2, rgb;

        if (s.rgbmap == null || !s.isInSite(x, z)) return null;

        int shiftX = (x - s.corners[0][0] * scale - offset) * SiteStructureGenerator.SITETOBLOCK / scale;
        int shiftZ = (z - s.corners[0][1] * scale - offset) * SiteStructureGenerator.SITETOBLOCK / scale;
        if (shiftX >= s.rgbmap.length || shiftZ >= s.rgbmap[0].length) return null;
        if (shiftX < 0 || shiftZ < 0) return null;
        rgb = s.rgbmap[shiftX][shiftZ];
        SiteMapColours siteCol = SiteMapColours.getMatch(rgb);

        return siteCol;
    }

    public void buildSites(World world, int chunkX, int chunkZ, ChunkPrimer blocks, BiomeGenBase[] biomes)
    {
        if (dorfs.structureMap.length == 0) return;
        SiteStructureGenerator structureGen = WorldGenerator.instance.structureGen;
        int index;
        int x = (chunkX * 16 - WorldGenerator.shift.getX());
        int z = (chunkZ * 16 - WorldGenerator.shift.getZ());
        int x1, z1, h;

        for (int i1 = 0; i1 < 16; i1++)
        {
            for (int k1 = 0; k1 < 16; k1++)
            {
                x1 = (x + i1);// / scale;
                z1 = (z + k1);// / scale;

                HashSet<Site> sites = dorfs.getSiteForCoords(x1, z1);

                if (sites == null) continue;

                for (Site s : sites)
                {
                    SiteMapColours siteCol = getSiteMapColour(s, x1, z1);
                    if (siteCol == null) continue;

                    h = bicubicInterpolator.interpolate(WorldGenerator.instance.dorfs.elevationMap, x1, z1, scale);
                    int j = h - 1;
                    SiteStructures structs = structureGen.getStructuresForSite(s);
                    StructureSpace struct = structs.getStructure(x1, z1, scale);
                    if (struct != null)
                    {
                        j = struct.getFloor(s, scale) - 1;
                        h = j + 1;
                        continue;// TODO move stuff into SiteStructureGenerator
                    }

                    IBlockState[] repBlocks = SiteMapColours.getSurfaceBlocks(siteCol);

                    index = j << 0 | (i1) << 12 | (k1) << 8;

                    IBlockState surface = repBlocks[1];
                    IBlockState above = repBlocks[2];

                    boolean wall = siteCol == SiteMapColours.TOWNWALL;
                    boolean roof = siteCol.toString().contains("ROOF");
                    boolean farm = siteCol.toString().contains("FARM");
                    if (farm) biomes[i1 + 16 * k1] = BiomeGenBase.plains;

                    if (surface == null && siteCol.toString().contains("ROOF"))
                        surface = Blocks.brick_block.getDefaultState();

                    if (surface == null) // || blocks[index - 1] == Blocks.water
                                         // || blocks[index] == Blocks.water)
                        continue;
                    blocks.setBlockState(index, surface);
                    index = (j - 1) << 0 | (i1) << 12 | (k1) << 8;
                    blocks.setBlockState(index, repBlocks[0]);
                    index = (j + 1) << 0 | (i1) << 12 | (k1) << 8;
                    if (above != null) blocks.setBlockState(index, above);
                    boolean tower = siteCol.toString().contains("TOWER");
                    if (wall || roof)
                    {
                        int j1 = j;
                        int num = tower ? 10 : 3;
                        while (j1 < h + 1)
                        {
                            j1 = j1 + 1;
                            index = (j1) << 0 | (i1) << 12 | (k1) << 8;
                            blocks.setBlockState(index, Blocks.air.getDefaultState());
                            index = (h + num) << 0 | (i1) << 12 | (k1) << 8;
                            blocks.setBlockState(index, surface);
                            ;
                        }
                        j1 = j;
                        if (wall)
                        {
                            while (j1 < h + num)
                            {
                                j1 = j1 + 1;
                                index = (j1) << 0 | (i1) << 12 | (k1) << 8;
                                blocks.setBlockState(index, surface);
                            }
                        }
                    }

                    if (siteCol.toString().contains("ROAD"))
                    {
                        if (i1 > 0 && i1 < 15 && k1 > 0 && k1 < 15)
                        {
                            h = bicubicInterpolator.interpolate(WorldGenerator.instance.dorfs.elevationMap, x1, z1,
                                    scale);
                            int h2;

                            SiteMapColours px, nx, pz, nz;
                            px = getSiteMapColour(s, x1 + 1, z1);
                            nx = getSiteMapColour(s, x1 - 1, z1);
                            pz = getSiteMapColour(s, x1, z1 + 1);
                            nz = getSiteMapColour(s, x1, z1 - 1);

                            if (px != null && !px.toString().contains("ROAD") && z1 % 8 == 0)
                            {
                                h2 = bicubicInterpolator.interpolate(WorldGenerator.instance.dorfs.elevationMap, x1 + 1,
                                        z1, scale);
                                index = (h2) << 0 | (i1 + 1) << 12 | (k1) << 8;
                                blocks.setBlockState(index, Blocks.torch.getDefaultState());
                            }

                            if (nx != null && !nx.toString().contains("ROAD") && z1 % 8 == 0)
                            {
                                h2 = bicubicInterpolator.interpolate(WorldGenerator.instance.dorfs.elevationMap, x1 - 1,
                                        z1, scale);
                                index = (h2) << 0 | (i1 - 1) << 12 | (k1) << 8;
                                blocks.setBlockState(index, Blocks.torch.getDefaultState());
                            }

                            if (pz != null && !pz.toString().contains("ROAD") && x1 % 8 == 0)
                            {
                                h2 = bicubicInterpolator.interpolate(WorldGenerator.instance.dorfs.elevationMap, x1,
                                        z1 + 1, scale);
                                index = (h2) << 0 | (i1) << 12 | (k1 + 1) << 8;
                                blocks.setBlockState(index, Blocks.torch.getDefaultState());
                            }

                            if (nz != null && !nz.toString().contains("ROAD") && x1 % 8 == 0)
                            {
                                h2 = bicubicInterpolator.interpolate(WorldGenerator.instance.dorfs.elevationMap, x1,
                                        z1 - 1, scale);
                                index = (h2) << 0 | (i1) << 12 | (k1 - 1) << 8;
                                blocks.setBlockState(index, Blocks.torch.getDefaultState());
                            }
                        }
                    }
                }
            }
        }
    }

    private static final EnumFacing[] DIRS        = { EAST, WEST, NORTH, SOUTH };
    private static double[]           DIR_TO_RELX = { ((double) scale), 0., ((double) scale) / 2.,
            ((double) scale) / 2. };
    private static double[]           DIR_TO_RELZ = { ((double) scale) / 2., ((double) scale) / 2., 0,
            ((double) scale) };

    private int dirToIndex(EnumFacing dir)
    {
        if (dir == EAST) return 0;
        if (dir == WEST) return 1;
        if (dir == NORTH) return 2;
        if (dir == SOUTH) return 3;
        return 0;
    }

    private void safeSetToRoad(int x, int z, int h, int chunkX, int chunkZ, ChunkPrimer blocks, Block block)
    {
        int index;

        int x1 = x - chunkX;
        int z1 = z - chunkZ;

        index = (h - 1) << 0 | (x1) << 12 | (z1) << 8;

        if (index >= 0 && x1 < 16 && z1 < 16 && x1 >= 0 && z1 >= 0)
        {
            if (index + 3 < 255) blocks.setBlockState(index + 3, Blocks.air.getDefaultState());
            if (index + 2 < 255) blocks.setBlockState(index + 2, Blocks.air.getDefaultState());
            if (index + 1 < 255) blocks.setBlockState(index + 1, Blocks.air.getDefaultState());
            blocks.setBlockState(index, block.getDefaultState());
            blocks.setBlockState(index - 1, Blocks.cobblestone.getDefaultState());
            blocks.setBlockState(index - 2, Blocks.cobblestone.getDefaultState());
        }
    }

    private void safeSetToRoad(int x, int z, int h, int chunkX, int chunkZ, ChunkPrimer blocks)
    {
        safeSetToRoad(x, z, h, chunkX, chunkZ, blocks, BlockRoadSurface.uggrass);
    }

    private static final int ROADWIDTH = 3;

    private void genSingleRoad(EnumFacing begin, EnumFacing end, int x, int z, int chunkX, int chunkZ,
            ChunkPrimer blocks)
    {
        int nearestEmbarkX = x - (x % scale);
        int nearestEmbarkZ = z - (z % scale);
        double interX, interZ;
        int nearestX, nearestZ;
        int h;
        double startX = DIR_TO_RELX[dirToIndex(begin)];
        double startZ = DIR_TO_RELZ[dirToIndex(begin)];
        double endX = DIR_TO_RELX[dirToIndex(end)];
        double endZ = DIR_TO_RELZ[dirToIndex(end)];

        double c = ((double) scale) / 2.;

        for (double i = -0.2; i <= 1.2; i += 0.02)
        {
            interX = (1. - i) * (1. - i) * startX + 2. * (1. - i) * i * c + i * i * endX;
            interZ = (1. - i) * (1. - i) * startZ + 2. * (1. - i) * i * c + i * i * endZ;

            nearestX = (int) interX;
            nearestZ = (int) interZ;

            for (int w = -ROADWIDTH; w <= ROADWIDTH; w++)
            {
                for (int w2 = -ROADWIDTH; w2 <= ROADWIDTH; w2++)
                {
                    h = bicubicInterpolator.interpolate(WorldGenerator.instance.dorfs.elevationMap,
                            nearestX + nearestEmbarkX + w, nearestZ + nearestEmbarkZ + w2, scale);
                    safeSetToRoad(nearestX + nearestEmbarkX + w, nearestZ + nearestEmbarkZ + w2, h, chunkX, chunkZ,
                            blocks);
                }
            }
        }
    }

    private void genSingleRoadToPos(int x, int z, int chunkX, int chunkZ, int toRoadX, int toRoadZ, ChunkPrimer blocks)
    {
        int nearestEmbarkX = x - (x % scale);
        int nearestEmbarkZ = z - (z % scale);
        double interX, interZ;
        int nearestX, nearestZ;
        int h;

        double c = ((double) scale) / 2.;

        double startX = c, startZ = c, distSqr;
        double minDistSqr = Integer.MAX_VALUE;

        double endX = toRoadX - nearestEmbarkX;
        double endZ = toRoadZ - nearestEmbarkZ;

        boolean[] dirs = getRoadDirection(nearestEmbarkX, nearestEmbarkZ);

        for (EnumFacing dir : DIRS)
        {
            if (!dirs[dirToIndex(dir)]) continue;
            distSqr = (DIR_TO_RELX[dirToIndex(dir)] - ((double) endX))
                    * (DIR_TO_RELX[dirToIndex(dir)] - ((double) endX))
                    + (DIR_TO_RELZ[dirToIndex(dir)] - ((double) endZ))
                            * (DIR_TO_RELZ[dirToIndex(dir)] - ((double) endZ));
            if (distSqr < minDistSqr)
            {
                minDistSqr = distSqr;
                startX = DIR_TO_RELX[dirToIndex(dir)];
                startZ = DIR_TO_RELZ[dirToIndex(dir)];
            }

        }

        for (double i = -0.05; i <= 1.05; i += 0.01)
        {
            // interX = (1.-i)*(1.-i)*startX + 2.*(1.-i)*i*c + i*i*endX;
            // interZ = (1.-i)*(1.-i)*startZ + 2.*(1.-i)*i*c + i*i*endZ;

            interX = startX * (1.0 - i) + endX * i;
            interZ = startZ * (1.0 - i) + endZ * i;

            nearestX = (int) interX;
            nearestZ = (int) interZ;

            for (int w = -ROADWIDTH; w <= ROADWIDTH; w++)
            {
                for (int w2 = -ROADWIDTH; w2 <= ROADWIDTH; w2++)
                {
                    if ((w < 1 - ROADWIDTH || w > ROADWIDTH - 1) && (w2 < 1 - ROADWIDTH || w2 > ROADWIDTH - 1))
                        continue; // take the corners off
                    h = bicubicInterpolator.interpolate(WorldGenerator.instance.dorfs.elevationMap,
                            nearestX + nearestEmbarkX + w, nearestZ + nearestEmbarkZ + w2, scale);
                    safeSetToRoad(nearestX + nearestEmbarkX + w, nearestZ + nearestEmbarkZ + w2, h, chunkX, chunkZ,
                            blocks);
                }
            }
        }
    }

    private boolean isInSite(int x, int z)
    {
        int kx = x / scale;
        int kz = z / scale;

        int key = kx + 8192 * kz;

        HashSet<Site> sites = DorfMap.sitesByCoord.get(key);

        if (sites != null)
        {
            for (Site site : sites)
            {
                SiteStructures structs = WorldGenerator.instance.structureGen.getStructuresForSite(site);
                if (structs != null && !structs.roads.isEmpty()) return true;
            }
        }

        return false;
    }

    public static int[] getClosestRoadEnd(int x, int z, Site site)
    {
        int[] edge = null;
        int[] result = null;

        int minDistanceSqr = Integer.MAX_VALUE;

        SiteStructures structures = WorldGenerator.instance.structureGen.getStructuresForSite(site);
        for (RoadExit exit : structures.roads)
        {
            edge = exit.getEdgeMid(site, scale);
            if (minDistanceSqr > (x - edge[0]) * (x - edge[0]) + (z - edge[1]) * (z - edge[1]))
            {
                minDistanceSqr = (x - edge[0]) * (x - edge[0]) + (z - edge[1]) * (z - edge[1]);
                result = edge;
            }
        }

        return result;
    }

    private int roundToEmbark(int a)
    {
        return a - (a % scale);
    }

    static private final int ROAD_SEARCH_AREA = 3;

    private boolean isNearSiteRoadEnd(int x, int z)
    {
        HashSet<Site> sites = new HashSet<Site>(), subSites;

        int kx = x / scale;
        int kz = z / scale;

        for (int xsearch = -ROAD_SEARCH_AREA; xsearch <= ROAD_SEARCH_AREA; xsearch++)
        {
            for (int zsearch = -ROAD_SEARCH_AREA; zsearch <= ROAD_SEARCH_AREA; zsearch++)
            {
                subSites = DorfMap.sitesByCoord.get((kx + xsearch) + 8192 * (kz + zsearch));
                if (subSites != null) sites.addAll(subSites);
            }
        }

        if (sites.size() == 0) return false;

        for (Site site : sites)
        {
            int[] edge = getClosestRoadEnd(x, z, site);
            if (edge == null) continue;
            if (roundToEmbark(x) == roundToEmbark(edge[0]) && roundToEmbark(z) == roundToEmbark(edge[1])
                    || roundToEmbark(x) - scale == roundToEmbark(edge[0]) && roundToEmbark(z) == roundToEmbark(edge[1])
                    || roundToEmbark(x) + scale == roundToEmbark(edge[0]) && roundToEmbark(z) == roundToEmbark(edge[1])
                    || roundToEmbark(x) == roundToEmbark(edge[0]) && roundToEmbark(z) + scale == roundToEmbark(edge[1])
                    || roundToEmbark(x) == roundToEmbark(edge[0])
                            && roundToEmbark(z) - scale == roundToEmbark(edge[1])) { return true; }
        }

        return false;
    }

    private int[] getSiteRoadEnd(int x, int z)
    {
        HashSet<Site> sites = new HashSet<Site>(), subSites;

        int kx = x / scale;
        int kz = z / scale;

        for (int xsearch = -ROAD_SEARCH_AREA; xsearch <= ROAD_SEARCH_AREA; xsearch++)
        {
            for (int zsearch = -ROAD_SEARCH_AREA; zsearch <= ROAD_SEARCH_AREA; zsearch++)
            {
                subSites = DorfMap.sitesByCoord.get((kx + xsearch) + 8192 * (kz + zsearch));
                if (subSites != null) sites.addAll(subSites);
            }
        }

        if (sites.size() == 0) return null;

        for (Site site : sites)
        {
            int[] edge = getClosestRoadEnd(x, z, site);
            if (edge == null) continue;
            if (roundToEmbark(x) == roundToEmbark(edge[0]) && roundToEmbark(z) == roundToEmbark(edge[1])
                    || roundToEmbark(x) - scale == roundToEmbark(edge[0]) && roundToEmbark(z) == roundToEmbark(edge[1])
                    || roundToEmbark(x) + scale == roundToEmbark(edge[0]) && roundToEmbark(z) == roundToEmbark(edge[1])
                    || roundToEmbark(x) == roundToEmbark(edge[0]) && roundToEmbark(z) + scale == roundToEmbark(edge[1])
                    || roundToEmbark(x) == roundToEmbark(edge[0])
                            && roundToEmbark(z) - scale == roundToEmbark(edge[1])) { return edge; }
        }

        return null;
    }

    private void genRoads(int x, int z, int chunkX, int chunkZ, ChunkPrimer blocks)
    {
        int nearestEmbarkX = x - (x % scale);
        int nearestEmbarkZ = z - (z % scale);

        boolean dirs[] = getRoadDirection(nearestEmbarkX, nearestEmbarkZ);

        for (int i = 0; i < 3; i++)
        {
            for (int j = i + 1; j < 4; j++)
            {
                if (dirs[i] && dirs[j])
                {
                    genSingleRoad(DIRS[i], DIRS[j], x, z, chunkX, chunkZ, blocks);
                }
            }
        }
    }

    boolean hasRoad(int x, int z)
    {
        HashSet<WorldConstruction> cons = WorldGenerator.instance.dorfs.getConstructionsForCoords(x, z);

        if (cons == null || cons.isEmpty()) return false;

        for (WorldConstruction con : cons)
        {
            if (con.type == DorfMap.ConstructionType.ROAD)
            {
                if (con.isInConstruct(x, 0, z)) { return true; }
            }
        }

        return false;
    }

    public void debugPrint(int x, int z)
    {
        int embarkX = roundToEmbark(x);
        int embarkZ = roundToEmbark(z);

        if (isInSite(x, z))
        {
            System.out.println("Embark location x: " + embarkX + " z: " + embarkZ + " is in a site");
        }

        if (hasRoad(x, z))
        {
            System.out.println("Embark location x: " + embarkX + " z: " + embarkZ + " has a road");
        }

        if (WorldGenerator.instance.dorfs.getConstructionsForCoords(x, z) != null)
        {
            for (WorldConstruction constr : WorldGenerator.instance.dorfs.getConstructionsForCoords(x, z))
            {
                if (constr.isInConstruct(x, 0, z))
                {
                    System.out.println("Location x: " + x + " z: " + z + " is in a construction");
                    System.out.println("    Construction is " + constr.toString());
                }
            }
        }

        if (WorldGenerator.instance.dorfs.getConstructionsForCoords(embarkX, embarkZ) != null)
        {
            for (WorldConstruction constr : WorldGenerator.instance.dorfs.getConstructionsForCoords(embarkX, embarkZ))
            {
                if (constr.isInConstruct(embarkX, 0, embarkZ))
                {
                    System.out.println("Location x: " + embarkX + " z: " + embarkZ + " is in a construction");
                    System.out.println("    Construction is " + constr.toString());
                }
            }
        }

        if (isNearSiteRoadEnd(x, z))
        {
            System.out.println("Embark location x: " + embarkX + " z: " + embarkZ + " is near a site road end");
            int[] roadEnd = getSiteRoadEnd(x, z);
            System.out.println("Site road end is at x: " + roadEnd[0] + " z: " + roadEnd[1]);

            int minDistSqr = Integer.MAX_VALUE, dist;
            int x1, z1;
            int embarkX1 = 0, embarkZ1 = 0;
            int roadEndX = roadEnd[0];
            int roadEndZ = roadEnd[1];

            for (int xsearch = -ROAD_SEARCH_AREA; xsearch <= ROAD_SEARCH_AREA; xsearch++)
            {
                for (int zsearch = -ROAD_SEARCH_AREA; zsearch <= ROAD_SEARCH_AREA; zsearch++)
                {
                    x1 = roundToEmbark(roadEndX + (xsearch * scale));
                    z1 = roundToEmbark(roadEndZ + (zsearch * scale));

                    if (isInSite(x1, z1)) continue;
                    if (!hasRoad(x1, z1)) continue;

                    dist = (x1 - roadEndX) * (x1 - roadEndX) + (z1 - roadEndZ) * (z1 - roadEndZ);

                    if (dist < minDistSqr)
                    {
                        minDistSqr = dist;
                        embarkX1 = x1;
                        embarkZ1 = z1;
                    }
                }
            }
            if (minDistSqr != Integer.MAX_VALUE)
            {
                System.out.println("Nearest embark to road end found at x: " + embarkX1 + " z: " + embarkZ1);

                EnumFacing closestdir = EAST;
                double distSqr2;
                double minDistSqr2 = Integer.MAX_VALUE;

                double endX = roadEndX - embarkX;
                double endZ = roadEndZ - embarkZ;

                boolean[] dirs = getRoadDirection(embarkX, embarkZ);

                for (EnumFacing dir : DIRS)
                {
                    if (!dirs[dirToIndex(dir)]) continue;
                    distSqr2 = (DIR_TO_RELX[dirToIndex(dir)] - ((double) endX))
                            * (DIR_TO_RELX[dirToIndex(dir)] - ((double) endX))
                            + (DIR_TO_RELZ[dirToIndex(dir)] - ((double) endZ))
                                    * (DIR_TO_RELZ[dirToIndex(dir)] - ((double) endZ));
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

    private void genRoadEndConnector(int roadEndX, int roadEndZ, int chunkX, int chunkZ, ChunkPrimer blocks)
    {
        int minDistSqr = Integer.MAX_VALUE, dist;
        int x1, z1;
        int embarkX = 0, embarkZ = 0;

        for (int xsearch = -ROAD_SEARCH_AREA; xsearch <= ROAD_SEARCH_AREA; xsearch++)
        {
            for (int zsearch = -ROAD_SEARCH_AREA; zsearch <= ROAD_SEARCH_AREA; zsearch++)
            {
                x1 = roundToEmbark(roadEndX + (xsearch * scale));
                z1 = roundToEmbark(roadEndZ + (zsearch * scale));

                if (isInSite(x1, z1)) continue;
                if (!hasRoad(x1, z1)) continue;

                dist = (x1 - roadEndX) * (x1 - roadEndX) + (z1 - roadEndZ) * (z1 - roadEndZ);

                if (dist < minDistSqr)
                {
                    minDistSqr = dist;
                    embarkX = x1;
                    embarkZ = z1;
                }
            }
        }

        if (minDistSqr != Integer.MAX_VALUE)
        {
            genSingleRoadToPos(embarkX, embarkZ, chunkX, chunkZ, roadEndX, roadEndZ, blocks);
        }
        else
        {
            System.out.println("Search failed to generate attachment road at x: " + roadEndX + " z: " + roadEndZ);
        }
    }

    public void buildRoads(World world, int chunkX, int chunkZ, ChunkPrimer blocks, BiomeGenBase[] biomes)
    {
        int x = (chunkX * 16 - WorldGenerator.shift.getX());
        int z = (chunkZ * 16 - WorldGenerator.shift.getZ());

        if (isNearSiteRoadEnd(x, z))
        {
            int[] roadEnd = getSiteRoadEnd(x, z);
            genRoadEndConnector(roadEnd[0], roadEnd[1], x, z, blocks);
        }

        if (isInSite(x, z)) return;

        genRoads(x - (x % scale), z - (z % scale), x, z, blocks);

        if ((x + 16) - ((x + 16) % scale) > x - (x % scale))
        {
            if ((z + 16) - ((z + 16) % scale) > z - (z % scale))
            {
                genRoads((x + 16) - ((x + 16) % scale), (z + 16) - ((z + 16) % scale), x, z, blocks);
            }
            else
            {
                genRoads((x + 16) - ((x + 16) % scale), z - (z % scale), x, z, blocks);
            }
        }
        else if ((z + 16) - ((z + 16) % scale) > z - (z % scale))
        {
            genRoads(x - (x % scale), (z + 16) - ((z + 16) % scale), x, z, blocks);
        }
    }

    public static boolean[] getRoadDirection(int xAbs, int zAbs)
    {
        boolean[] ret = new boolean[4];

        HashSet<WorldConstruction> constructs = WorldGenerator.instance.dorfs.getConstructionsForCoords(xAbs, zAbs);

        if (constructs == null) return ret;

        for (WorldConstruction con : constructs)
        {
            if (!con.isInConstruct(xAbs, 0, zAbs)) continue;

            if (con.isInConstruct(xAbs - scale, 0, zAbs))
            {
                ret[1] = true;
            }
            if (con.isInConstruct(xAbs + scale, 0, zAbs))
            {
                ret[0] = true;
            }
            if (con.isInConstruct(xAbs, 0, zAbs - scale))
            {
                ret[2] = true;
            }
            if (con.isInConstruct(xAbs, 0, zAbs + scale))
            {
                ret[3] = true;
            }
        }
        return ret;
    }

    public static Block getSurfaceBlockForSite(SiteTerrain site, int num)
    {
        switch (site)
        {
        case BUILDINGS:
            return num == 0 ? Blocks.brick_block : null;
        case WALLS:
            return Blocks.stonebrick;
        case FARMYELLOW:
            return num == 0 ? Blocks.sand : null;
        case FARMORANGE:
            return num == 0 ? Blocks.dirt : null;
        case FARMLIMEGREEN:
            return num == 0 ? Blocks.clay : null;
        case FARMORANGELIGHT:
            return num == 0 ? Blocks.hardened_clay : null;
        case FARMGREEN:
            return num == 0 ? Blocks.stained_hardened_clay : null;
        default:
            return null;
        }
    }
}
