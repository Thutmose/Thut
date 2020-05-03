package dorfgen.conversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import dorfgen.Dorfgen;
import dorfgen.conversion.DorfMap.Site;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.state.properties.Half;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;

public class SiteStructureGenerator
{
    final DorfMap                    dorfs;
    public static int                SITETOBLOCK  = 51;
    HashMap<Integer, SiteStructures> structureMap = new HashMap<>();

    public SiteStructureGenerator(final DorfMap dorfs_)
    {
        this.dorfs = dorfs_;
    }

    public void init()
    {
        Dorfgen.LOGGER.info("Processing Site Maps for structures");
        for (final Integer i : this.dorfs.sitesById.keySet())
            this.structureMap.put(i, new SiteStructures(this.dorfs.sitesById.get(i), this.dorfs));
        Dorfgen.LOGGER.info("Processed Site Maps for structures");
    }

    public SiteStructures getStructuresForSite(final Site site)
    {
        return this.structureMap.get(site.id);
    }

    private boolean isIn(final IWorld region, final BlockPos pos)
    {
        return region.chunkExists(pos.getX() >> 4, pos.getZ() >> 4);
    }

    private void placeOnLever(final IWorld world, final int x2, final int y, final int z2, final Mutable mutablePos)
    {
        mutablePos.setPos(x2, y, z2);
        BlockState state = Blocks.LEVER.getDefaultState();
        state = state.with(LeverBlock.POWERED, true);
        world.setBlockState(mutablePos, state, 3);
    }

    private boolean isBlockSurroundedByWall(final Site site, final SiteStructures structures, final WallSegment wall,
            final int x1, final int z1)
    {
        boolean surrounded = true;
        boolean nearStruct = false;
        if (!nearStruct)
        {
            nearStruct = structures.isStructure(x1 - 1, z1, this.dorfs.scale);
            if (nearStruct)
            {
                final boolean t1 = !wall.isInWall(site, x1, z1 - 1, this.dorfs.scale);
                final boolean t2 = !wall.isInWall(site, x1, z1 + 1, this.dorfs.scale);
                surrounded = !(t1 || t2);
            }
        }
        if (!nearStruct)
        {
            nearStruct = structures.isStructure(x1 + 1, z1, this.dorfs.scale);
            if (nearStruct)
            {
                final boolean t1 = !wall.isInWall(site, x1, z1 - 1, this.dorfs.scale);
                final boolean t2 = !wall.isInWall(site, x1, z1 + 1, this.dorfs.scale);
                surrounded = !(t1 || t2);
            }
        }
        if (!nearStruct)
        {
            nearStruct = structures.isStructure(x1, z1 - 1, this.dorfs.scale);
            if (nearStruct)
            {
                final boolean t1 = !wall.isInWall(site, x1 - 1, z1, this.dorfs.scale);
                final boolean t2 = !wall.isInWall(site, x1 + 1, z1, this.dorfs.scale);
                surrounded = !(t1 || t2);
            }
        }
        if (!nearStruct)
        {
            nearStruct = structures.isStructure(x1, z1 + 1, this.dorfs.scale);
            if (nearStruct)
            {
                final boolean t1 = !wall.isInWall(site, x1 - 1, z1, this.dorfs.scale);
                final boolean t2 = !wall.isInWall(site, x1 + 1, z1, this.dorfs.scale);
                surrounded = !(t1 || t2);
            }
        }
        if (!nearStruct)
        {

            if (surrounded) surrounded = wall.isInWall(site, x1 - 1, z1 - 1, this.dorfs.scale);
            if (surrounded) surrounded = wall.isInWall(site, x1 + 1, z1 - 1, this.dorfs.scale);
            if (surrounded) surrounded = wall.isInWall(site, x1 - 1, z1 + 1, this.dorfs.scale);
            if (surrounded) surrounded = wall.isInWall(site, x1 + 1, z1 + 1, this.dorfs.scale);

        }

        return surrounded;
    }

    public void placeTorch(final IWorld world, final int x, final int y, final int z, final int flag,
            final Mutable mutablePos)
    {
        mutablePos.setPos(x, y, z);
        BlockState state = Blocks.TORCH.getDefaultState();
        final Iterator<?> iterator = Direction.Plane.HORIZONTAL.iterator();
        Direction Direction1 = Direction.EAST;
        do
        {
            if (!iterator.hasNext()) break;
            Direction1 = (Direction) iterator.next();
        }
        while (!Block.hasEnoughSolidSide(world, mutablePos, Direction1.getOpposite()));
        state = Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.HORIZONTAL_FACING, Direction1);
        world.setBlockState(mutablePos, state, flag);
    }

    /**
     * Called by ItemBlocks after a block is set in the world, to allow
     * post-place logic
     */
    public void placeDoor(final IWorld worldIn, final BlockPos pos, final BlockState state, final int flag)
    {
        worldIn.setBlockState(pos, state.with(DoorBlock.HALF, DoubleBlockHalf.LOWER), flag);
        worldIn.setBlockState(pos.up(), state.with(DoorBlock.HALF, DoubleBlockHalf.UPPER), flag);
    }

    /**
     * Takes Chunk Coordinates
     *
     * @param s
     * @param x
     * @param z
     * @param world
     */
    public void generate(final IChunk chunk, final IWorld world, final Mutable mutablePos)
    {
        final int chunkX = chunk.getPos().x;
        final int chunkZ = chunk.getPos().z;
        final int flag = 3;
        final int flag_torch = 3;
        final int scale = this.dorfs.scale;
        int x = chunkX, z = chunkZ, x1, x2, z1, z2;
        x *= 16;
        z *= 16;
        x -= Dorfgen.shift.getX();
        z -= Dorfgen.shift.getZ();
        int h;
        for (int i = 0; i < 16; i++)
            for (int j = 0; j < 16; j++)
            {
                // These are the positions shifted to the map coordinates
                x1 = x + i;
                z1 = z + j;
                // These are the actual world coordinates.
                x2 = chunk.getPos().getXStart() + i;
                z2 = chunk.getPos().getZStart() + j;

                mutablePos.setPos(x2, 0, z2);

                final Set<Site> sites = this.dorfs.getSiteForCoords(x2, z2);
                Site site = null;
                if (sites.isEmpty() || !this.isIn(world, mutablePos)) continue;
                // Loop Over sites and do the structures
                for (final Site s : sites)
                {
                    site = s;
                    final SiteStructures structures = this.getStructuresForSite(site);
                    if (structures == null) continue;

                    // Generate walls first, so towers can make doors into them
                    final WallSegment wall = structures.getWall(x1, z1, scale);
                    if (wall != null)
                    {
                        h = this.dorfs.biomeInterpolator.interpolate(this.dorfs.elevationMap, x1, z1, scale);

                        final boolean surrounded = this.isBlockSurroundedByWall(site, structures, wall, x1, z1);

                        world.setBlockState(mutablePos.setPos(x2, h - 1, z2), Blocks.STONE_BRICKS.getDefaultState(),
                                flag);
                        world.setBlockState(mutablePos.setPos(x2, h - 2, z2), Blocks.STONE_BRICKS.getDefaultState(),
                                flag);
                        for (int k = h; k < h + 6; k++)
                            if (k < h + 3 || k >= h + 4)
                            {
                                if (!surrounded) if (k == h + 5)
                                {
                                    if ((x1 + z1) % 3 > 0) world.setBlockState(mutablePos.setPos(x2, k, z2),
                                            Blocks.STONE_BRICKS.getDefaultState(), flag);
                                }
                                else world.setBlockState(mutablePos.setPos(x2, k, z2), Blocks.STONE_BRICKS
                                        .getDefaultState(), flag);
                            }
                            else world.setBlockState(mutablePos.setPos(x2, k, z2), Blocks.STONE_BRICKS
                                    .getDefaultState(), flag);
                    }

                    final StructureSpace struct = structures.getStructure(x1, z1, scale);
                    if (struct != null) // Generate Building
                    {
                        Block material = Blocks.OAK_PLANKS;
                        int height = 3;
                        boolean mid;
                        boolean villager = mid = x1 == struct.getMid(site, scale)[0] && z1 == struct.getMid(site,
                                scale)[1];
                        final boolean tower = struct.roofType == SiteMapColours.TOWERROOF;

                        if (tower)
                        {
                            material = Blocks.STONE_BRICKS;
                            height = 10;
                            villager = false;
                        }

                        h = struct.getFloor(site, scale);
                        boolean inWall;
                        if (inWall = struct.inWall(site, x1, z1, scale)) for (int l = 0; l < height; l++)
                            world.setBlockState(mutablePos.setPos(x2, h + l, z2), material.getDefaultState(), 2);
                        else
                        {
                            for (int l = height - 1; l >= -1; l--)
                                world.setBlockState(mutablePos.setPos(x2, h + l, z2), Blocks.AIR.getDefaultState(), 2);
                            if (struct.roofType != SiteMapColours.TOWERROOF) world.setBlockState(mutablePos.setPos(x2,
                                    h, z2), Blocks.WHITE_CARPET.getDefaultState(), flag);
                        }

                        if (!tower)
                        {
                            world.setBlockState(mutablePos.setPos(x2, h - 1, z2), material.getDefaultState(), flag);
                            world.setBlockState(mutablePos.setPos(x2, h + height + 1, z2), material.getDefaultState(),
                                    flag);
                        }
                        else
                        {
                            // Floor
                            world.setBlockState(mutablePos.setPos(x2, h - 1, z2), material.getDefaultState(), flag);
                            // Crenellation
                            if (inWall && (x1 + z1) % 3 > 0) world.setBlockState(mutablePos.setPos(x2, h + height + 1,
                                    z2), Blocks.STONE_BRICKS.getDefaultState(), flag);
                        }
                        // Place the doors
                        if (struct.shouldBeDoor(site, x1, z1, scale))
                        {
                            mutablePos.setPos(x2, h, z2);
                            final Direction dir = struct.getDoorDirection(site, x1, z1, scale, structures);
                            final BlockState state = Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.FACING, dir);
                            this.placeDoor(world, mutablePos, state, flag);
                        }

                        // Pace the torches in roof
                        if (struct.shouldBeTorch(site, x1, z1, scale))
                        {
                            // Not for towers
                            if (struct.roofType != SiteMapColours.TOWERROOF) this.placeTorch(world, x2, h + height, z2,
                                    flag, mutablePos);
                            else
                            {
                                // Towers get lamps with levers under them
                                world.setBlockState(mutablePos.setPos(x2, h - 1, z2), Blocks.REDSTONE_LAMP
                                        .getDefaultState(), flag);
                                world.setBlockState(mutablePos.setPos(x2, h - 3, z2), material.getDefaultState(), flag);
                                this.placeOnLever(world, x2, h - 2, z2, mutablePos);
                            }
                        }
                        else // Otherwise fill in roof
                            if (!tower) world.setBlockState(mutablePos.setPos(x2, h + height, z2), material
                                    .getDefaultState(), flag);
                        if (tower)
                        {
                            final boolean air = world.isAirBlock(mutablePos.setPos(x2, h + height - 1, z2));
                            // Floor lowered due to Crenellated walls.
                            if (!(mid || inWall) && air) world.setBlockState(mutablePos.setPos(x2, h + height - 1, z2),
                                    material.getDefaultState(), flag);
                            // place blocks under wall
                            if (inWall) world.setBlockState(mutablePos.setPos(x2, h + height, z2), material
                                    .getDefaultState(), flag);
                        }

                        if (villager)
                        {
                            // TODO decide on a villager to spawn
                            // final EntityVillager entityvillager = new
                            // EntityVillager(world, 0);
                            // entityvillager.setLocationAndAngles(x2 + 0.5D,
                            // (double) h, z2 + 0.5D, 0.0F, 0.0F);
                            // world.spawnEntityInWorld(entityvillager);
                        }
                    }
                }
            }

        // second pass
        for (int i = 0; i < 16; i++)
            for (int j = 0; j < 16; j++)
            {
                // These are the positions shifted to the map coordinates
                x1 = x + i;
                z1 = z + j;
                // These are the actual world coordinates.
                x2 = chunk.getPos().getXStart() + i;
                z2 = chunk.getPos().getZStart() + j;

                mutablePos.setPos(x2, 0, z2);

                final Set<Site> sites = this.dorfs.getSiteForCoords(x2, z2);
                Site site = null;
                if (sites.isEmpty()) continue;
                // Loop Over sites and do the structures
                for (final Site s : sites)
                {
                    site = s;
                    final SiteStructures structures = this.getStructuresForSite(site);
                    if (structures == null) continue;

                    // Generate torches inside the walls
                    final WallSegment wall = structures.getWall(x1, z1, scale);
                    if (wall != null)
                    {
                        h = this.dorfs.biomeInterpolator.interpolate(this.dorfs.elevationMap, x1, z1, scale);

                        final boolean surrounded = this.isBlockSurroundedByWall(site, structures, wall, x1, z1);

                        for (int k = h; k < h + 6; k++)
                            if (!(k < h + 3 || k >= h + 4)) if (surrounded)
                            {
                                boolean pos = false, neg = false;

                                // if this block is surrounded by wall, but
                                // the block next to it is not,
                                // then we must be on the edge of the inside
                                // of the corridor:
                                // this is where we want to place torches

                                if (!(pos = this.isBlockSurroundedByWall(site, structures, wall, x1 + 1, z1))
                                        || (neg = !this.isBlockSurroundedByWall(site, structures, wall, x1 - 1, z1)))
                                {
                                    // place torches every n blocks
                                    if (z1 % 3 == 0)
                                    {
                                        Block b;
                                        // this is a second pass, so the
                                        // buildings connected to the
                                        // corridors
                                        // have already been generated. We
                                        // want avoid erasing the walls, but
                                        // we
                                        // don't want to not place torches
                                        // on those walls, either. By
                                        // checking pos
                                        // and neg, we can move one block
                                        // toward the center of the
                                        // corridor, and
                                        // place a torch there
                                        if ((b = world.getBlockState(mutablePos.setPos(x2, h + 1, z2))
                                                .getBlock()) != Blocks.STONE_BRICKS && !(b instanceof DoorBlock)) this
                                                        .placeTorch(world, x2, h + 1, z2, flag_torch, mutablePos);
                                        else if (this.isIn(world, mutablePos.setPos(x2 - 1, h + 1, z2))) if (pos
                                                && (b = world.getBlockState(mutablePos.setPos(x2 - 1, h + 1, z2))
                                                        .getBlock()) != Blocks.STONE_BRICKS
                                                && !(b instanceof DoorBlock)) this.placeTorch(world, x2 - 1, h + 1, z2,
                                                        flag_torch, mutablePos);
                                        else if (this.isIn(world, mutablePos.setPos(x2 + 1, h + 1, z2))) if (neg
                                                && (b = world.getBlockState(mutablePos.setPos(x2 + 1, h + 1, z2))
                                                        .getBlock()) != Blocks.STONE_BRICKS
                                                && !(b instanceof DoorBlock)) this.placeTorch(world, x2 + 1, h + 1, z2,
                                                        flag_torch, mutablePos);
                                    }
                                }
                                else if (!(pos = this.isBlockSurroundedByWall(site, structures, wall, x1, z1 + 1))
                                        || !(neg = this.isBlockSurroundedByWall(site, structures, wall, x1, z1 - 1)))
                                    if (x1 % 3 == 0)
                                {
                                    Block b;
                                    if ((b = world.getBlockState(mutablePos.setPos(x2, h + 1, z2))
                                            .getBlock()) != Blocks.STONE_BRICKS && !(b instanceof DoorBlock)) this
                                                    .placeTorch(world, x2, h + 1, z2, flag_torch, mutablePos);

                                    else if (this.isIn(world, mutablePos.setPos(x2, h + 1, z2 - 1))) if (pos
                                            && (b = world.getBlockState(mutablePos.setPos(x2, h + 1, z2 - 1))
                                                    .getBlock()) != Blocks.STONE_BRICKS && !(b instanceof DoorBlock))
                                        this.placeTorch(world, x2, h + 1, z2 - 1, flag_torch, mutablePos);

                                    else if (this.isIn(world, mutablePos.setPos(x2, h + 1, z2 + 1))) if (neg
                                            && (b = world.getBlockState(mutablePos.setPos(x2 + 1, h + 1, z2))
                                                    .getBlock()) != Blocks.STONE_BRICKS && !(b instanceof DoorBlock))
                                        this.placeTorch(world, x2, h + 1, z2 + 1, flag_torch, mutablePos);
                                }
                            }
                    }

                    final StructureSpace struct = structures.getStructure(x1, z1, scale);
                    if (struct != null)
                    {
                        final Block material = Blocks.STONE_BRICKS;
                        final int height = 10;
                        boolean mid;
                        h = struct.getFloor(site, scale);
                        mid = x1 == struct.getMid(site, scale)[0] && z1 == struct.getMid(site, scale)[1];
                        final boolean tower = struct.roofType == SiteMapColours.TOWERROOF;

                        // put the trap doors on the tops of the towers, and the
                        // ladders up the middle
                        if (tower && mid)
                        {
                            for (int l = 0; l < height - 1; l++)
                            {
                                // TODO see about moving the ladder elsewhere if
                                // needed
                                if (this.isIn(world, mutablePos.setPos(x2 - 1, h + l, z2))) world.setBlockState(
                                        mutablePos, material.getDefaultState(), 2);
                                mutablePos.setPos(x2, h + l, z2);
                                BlockState state = Blocks.LADDER.getDefaultState();
                                final Direction Direction1 = Direction.EAST;
                                state = Blocks.LADDER.getDefaultState().with(LadderBlock.FACING, Direction1);
                                world.setBlockState(mutablePos, state, flag);

                            }
                            if (this.isIn(world, mutablePos.setPos(x2 + 1, h + height - 1, z2))) world.setBlockState(
                                    mutablePos, material.getDefaultState(), flag);
                            world.setBlockState(mutablePos.setPos(x2, h + height - 1, z2), Blocks.OAK_TRAPDOOR
                                    .getDefaultState().with(TrapDoorBlock.HALF, Half.TOP).with(
                                            HorizontalBlock.HORIZONTAL_FACING, Direction.WEST), flag);

                        }
                    }
                }
            }
    }

    public static class SiteStructures
    {
        private final DorfMap                dorfs;
        public final Site                    site;
        public final HashSet<StructureSpace> structures = new HashSet<>();
        public final HashSet<WallSegment>    walls      = new HashSet<>();
        public final HashSet<RoadExit>       roads      = new HashSet<>();
        public final HashSet<RiverExit>      rivers     = new HashSet<>();

        public SiteStructures(final Site site_, final DorfMap dorfs)
        {
            this.site = site_;
            this.dorfs = dorfs;
            this.initStructures();
            this.initRoadsAndRivers();
        }

        private void initRoadsAndRivers()
        {
            if (this.site.rgbmap != null)
            {
                final int h = this.site.rgbmap.length;
                final int w = this.site.rgbmap[0].length;

                boolean found1 = false;
                boolean found2 = false;
                int i1 = -1, i2 = -1;
                int n1 = 0, n2 = 0;

                // first 2 Edges
                for (int i = 0; i < h; i++)
                {
                    final int side1 = this.site.rgbmap[i][0];
                    final int side2 = this.site.rgbmap[i][w - 1];

                    final SiteMapColours colour1 = SiteMapColours.getMatch(side1);
                    final SiteMapColours colour2 = SiteMapColours.getMatch(side2);

                    // Roads
                    if (!found1 && colour1 == SiteMapColours.ROAD)
                    {
                        found1 = true;
                        this.roads.add(new RoadExit(i + 3, 0));
                    }
                    else if (found1 && colour1 != SiteMapColours.ROAD) found1 = false;
                    if (!found2 && colour2 == SiteMapColours.ROAD)
                    {
                        found2 = true;
                        this.roads.add(new RoadExit(i + 3, w - 1));
                    }
                    else if (found2 && colour2 != SiteMapColours.ROAD) found2 = false;
                    // Rivers
                    if (i1 == -1 && colour1 == SiteMapColours.RIVER)
                    {
                        i1 = i;
                        n1 = 0;
                    }
                    else if (i1 != -1 && colour1 != SiteMapColours.RIVER)
                    {
                        this.rivers.add(new RiverExit(i1, 0, n1++, false));
                        i1 = -1;
                    }
                    else if (i1 != -1) n1++;
                    if (i2 == -1 && colour2 == SiteMapColours.RIVER)
                    {
                        i2 = i;
                        n2 = 0;
                    }
                    else if (i2 != -1 && colour2 != SiteMapColours.RIVER)
                    {
                        this.rivers.add(new RiverExit(i2, w - 1, n2++, false));
                        i2 = -1;
                    }
                    else if (i2 != -1) n2++;
                }
                found1 = false;
                found2 = false;
                i1 = i2 = -1;
                n1 = n2 = 0;
                // second 2 Edges
                for (int i = 0; i < w; i++)
                {
                    final int side1 = this.site.rgbmap[0][i];
                    final int side2 = this.site.rgbmap[h - 1][i];

                    final SiteMapColours colour1 = SiteMapColours.getMatch(side1);
                    final SiteMapColours colour2 = SiteMapColours.getMatch(side2);
                    // Roads
                    if (!found1 && colour1 == SiteMapColours.ROAD)
                    {
                        found1 = true;
                        this.roads.add(new RoadExit(0, i + 3));
                    }
                    else if (found1 && colour1 != SiteMapColours.ROAD) found1 = false;
                    if (!found2 && colour2 == SiteMapColours.ROAD)
                    {
                        found2 = true;
                        this.roads.add(new RoadExit(h - 1, i + 3));
                    }
                    else if (found2 && colour2 != SiteMapColours.ROAD) found2 = false;
                    // Rivers
                    if (i1 == -1 && colour1 == SiteMapColours.RIVER)
                    {
                        i1 = i;
                        n1 = 0;
                    }
                    else if (i1 != -1 && colour1 != SiteMapColours.RIVER)
                    {
                        this.rivers.add(new RiverExit(0, i1, n1++, true));
                        i1 = -1;
                    }
                    else if (i1 != -1) n1++;
                    if (i2 == -1 && colour2 == SiteMapColours.RIVER)
                    {
                        i2 = i;
                        n2 = 0;
                    }
                    else if (i2 != -1 && colour2 != SiteMapColours.RIVER)
                    {
                        this.rivers.add(new RiverExit(h - 1, i2, n2++, true));
                        i2 = -1;
                    }
                    else if (i2 != -1) n2++;
                }

            }
        }

        private void initStructures()
        {
            if (!this.readFromFile() && this.site.rgbmap != null)
            {
                final HashSet<Integer> found = new HashSet<>();

                boolean newStruct = true;
                int n = 0;
                while (newStruct && n < 10000)
                {
                    n++;
                    final int[] corner1 = { -1, -1 };
                    final int[] corner2 = { -1, -1 };
                    newStruct = false;
                    SiteMapColours roof = null;
                    loopToFindNew:
                    for (int x = 1; x < this.site.rgbmap.length - 1; x++)
                        for (int y = 1; y < this.site.rgbmap[0].length - 1; y++)
                        {
                            final int rgb = this.site.rgbmap[x][y];
                            final int index = x + 2048 * y;

                            final SiteMapColours colour = SiteMapColours.getMatch(rgb);
                            if (this.isRoof(colour) && !found.contains(index) && !this.isInStructure(x, y))
                            {
                                roof = colour;
                                newStruct = true;
                                corner1[0] = x;
                                corner1[1] = y;
                                found.add(index);
                                break loopToFindNew;
                            }
                            else if (WallSegment.WALLBITS.contains(rgb) && !this.isInWall(x, y)) this.walls.add(
                                    new WallSegment(this.site, x, y));
                        }
                    // TODO make it look for borders around, to
                    // allow the bright green roofs to work as well.
                    if (newStruct)
                    {
                        loopx:
                        for (int x = corner1[0]; x < this.site.rgbmap.length - 1; x++)
                        {
                            final int y = corner1[1];
                            final int rgb = this.site.rgbmap[x + 1][y];
                            final SiteMapColours colour = SiteMapColours.getMatch(rgb);

                            if (colour != roof)
                            {
                                corner2[0] = x;
                                break loopx;
                            }
                        }
                        loopy:
                        for (int y = corner1[1]; y < this.site.rgbmap[0].length - 1; y++)
                        {
                            final int x = corner1[0];
                            final int rgb = this.site.rgbmap[x][y + 1];
                            final SiteMapColours colour = SiteMapColours.getMatch(rgb);

                            if (colour != roof)
                            {
                                corner2[1] = y;
                                break loopy;
                            }
                        }
                        // Expand out to include the walls.
                        corner1[0]--;
                        corner1[1]--;
                        corner2[0]++;
                        corner2[1]++;

                        StructureSpace structure;
                        if (roof == SiteMapColours.TOWERROOF) structure = new WallTowerSpace(corner1, corner2,
                                this.dorfs);
                        else structure = new StructureSpace(corner1, corner2, roof, this.dorfs);
                        this.structures.add(structure);
                    }

                }
                this.writeToFile();
            }
        }

        private boolean readFromFile()
        {
            final File sites = new File(this.dorfs.files.mainDir, "site_caches");
            final File file = new File(sites, "site_" + this.site.id);
            if (file.exists())
            {
                Dorfgen.LOGGER.debug("Reading from Cache for {} -> {}", this.site.name, "site_" + this.site.id);
                BufferedReader br = null;
                try
                {
                    final InputStream res = new FileInputStream(file);
                    br = new BufferedReader(new InputStreamReader(res));
                    String line;
                    String[] args;
                    while ((line = br.readLine()) != null)
                    {
                        args = line.split(":");
                        final String type = args[0];
                        if (type.equalsIgnoreCase("structurespace"))
                        {
                            final int roofrgb = Integer.parseInt(args[1]);
                            final int minx = Integer.parseInt(args[2]);
                            final int miny = Integer.parseInt(args[3]);
                            final int maxx = Integer.parseInt(args[4]);
                            final int maxy = Integer.parseInt(args[5]);
                            final SiteMapColours colour = SiteMapColours.getMatch(roofrgb);
                            final int[] min = { minx, miny };
                            final int[] max = { maxx, maxy };
                            StructureSpace structure;
                            if (colour == SiteMapColours.TOWERROOF) structure = new WallTowerSpace(min, max,
                                    this.dorfs);
                            else structure = new StructureSpace(min, max, colour, this.dorfs);
                            this.structures.add(structure);
                        }
                        else if (type.equalsIgnoreCase("wallsegment"))
                        {
                            final HashSet<Integer> pixels = new HashSet<>();
                            for (int i = 1; i < args.length; i++)
                                pixels.add(Integer.parseInt(args[i]));
                            this.walls.add(new WallSegment(pixels));
                        }
                    }
                    br.close();
                    return true;
                }
                catch (final Exception e)
                {
                    return false;
                }
            }
            return false;
        }

        private void writeToFile()
        {
            final File sites = new File(this.dorfs.files.mainDir, "site_caches");
            if (!sites.exists()) sites.mkdirs();
            final File file = new File(sites, "site_" + this.site.id);
            Dorfgen.LOGGER.debug("Writing Cache for {} -> {}", this.site.name, "site_" + this.site.id);

            FileWriter fwriter;
            PrintWriter out;
            try
            {
                fwriter = new FileWriter(file);
                out = new PrintWriter(fwriter);

                for (final StructureSpace struct : this.structures)
                {
                    String line = "structurespace";
                    line += ":" + struct.roofType.colour.getRGB();
                    line += ":" + struct.min[0];
                    line += ":" + struct.min[1];
                    line += ":" + struct.max[0];
                    line += ":" + struct.max[1];
                    out.println(line);
                }
                for (final WallSegment wall : this.walls)
                {
                    String line = "wallsegment";
                    for (final Integer i : wall.pixels)
                        line += ":" + i;
                    out.println(line);
                }

                out.close();
                fwriter.close();
            }
            catch (final Exception e)
            {

            }

        }

        /**
         * Takes site map pixel Coordinates.
         *
         * @param x
         * @param y
         * @return
         */
        boolean isInStructure(final int x, final int y)
        {
            for (final StructureSpace struct : this.structures)
                if (x >= struct.min[0] && x <= struct.max[0] && y >= struct.min[1] && y <= struct.max[1]) return true;
            return false;
        }

        /**
         * Takes site map pixel Coordinates
         *
         * @param x
         * @param y
         * @return
         */
        boolean isInWall(final int x, final int y)
        {
            for (final WallSegment wall : this.walls)
                if (wall.isInSegment(x, y)) return true;
            return false;
        }

        /**
         * Takes block coordinates
         *
         * @param x
         * @param y
         * @return
         */
        public boolean isStructure(final int x, final int y, final int scale)
        {
            return this.getStructure(x, y, scale) != null;
        }

        /**
         * Takes block coordinates
         *
         * @param x
         * @param y
         * @return
         */
        public StructureSpace getStructure(final int x, final int y, final int scale)
        {
            for (final StructureSpace struct : this.structures)
            {
                final int[][] bounds = struct.getBounds(this.site, scale);
                if (x >= bounds[0][0] && x <= bounds[1][0] && y >= bounds[0][1] && y <= bounds[1][1]) return struct;
            }
            return null;
        }

        public WallSegment getWall(final int x, final int y, final int scale)
        {
            for (final WallSegment wall : this.walls)
                if (wall.isInWall(this.site, x, y, scale)) return wall;
            return null;
        }

        boolean isRoof(final SiteMapColours colour)
        {
            if (colour == null) return false;
            return colour.toString().contains("ROOF");
        }

        boolean isHouseWall(final SiteMapColours colour)
        {
            if (colour == null) return false;
            return colour.toString().contains("BUILDINGWALL");
        }
    }

    public static class StructureSpace
    {
        private final DorfMap       dorfs;
        public final SiteMapColours roofType;
        /** Pixel Coordinates in the site map image */
        public final int[]          min;
        /** Pixel Coordinates in the site map image */
        public final int[]          max;

        protected int[][] bounds;
        protected int[]   mid;
        Site              lastSite  = null;
        int               lastScale = 0;

        public StructureSpace(final int[] minCoords, final int[] maxCoords, final SiteMapColours roof,
                final DorfMap dorfs)
        {
            this.min = minCoords;
            this.max = maxCoords;
            this.roofType = roof;
            this.dorfs = dorfs;
        }

        public int[][] getBounds(final Site site, final int scale)
        {
            if (this.bounds == null || site != this.lastSite || scale != this.lastScale)
            {
                final int width = scale / SiteStructureGenerator.SITETOBLOCK;
                final int offset = scale / 2;
                if (this.bounds == null) this.bounds = new int[2][2];
                this.bounds[0][0] = this.min[0] * width + site.corners[0][0] * scale + offset;
                this.bounds[0][1] = this.min[1] * width + site.corners[0][1] * scale + offset;
                this.bounds[1][0] = this.max[0] * width + site.corners[0][0] * scale + offset + width;
                this.bounds[1][1] = this.max[1] * width + site.corners[0][1] * scale + offset + width;
            }
            return this.bounds;
        }

        /**
         * Takes Block Coordinates
         *
         * @param site
         * @param x
         * @param z
         * @param scale
         * @return
         */
        public boolean shouldBeDoor(final Site site, final int x, final int z, final int scale)
        {
            this.getBounds(site, scale);

            final int midx = (this.bounds[0][0] + this.bounds[1][0]) / 2;
            final int midz = (this.bounds[0][1] + this.bounds[1][1]) / 2;

            // middle of a wall
            if (x == midx && (z == this.bounds[0][1] || z == this.bounds[1][1]) || z == midz && (x == this.bounds[0][0]
                    || x == this.bounds[1][0]))
            {
                final SiteStructures structs = this.dorfs.structureGen.getStructuresForSite(site);
                final Direction dir = this.getDoorDirection(site, x, z, scale, structs).getOpposite();
                final StructureSpace other = structs.getStructure(x + dir.getXOffset(), z + dir.getZOffset(), scale);
                if (other != null && other.getFloor(site, scale) != this.getFloor(site, scale)) return false;
                return true;
            }
            return false;
        }

        public Direction getDoorDirection(final Site site, final int x, final int z, final int scale,
                final SiteStructures structures)
        {
            final Direction ret = Direction.UP;

            for (final Direction dir : Direction.Plane.HORIZONTAL)
            {
                final StructureSpace other = structures.getStructure(x + dir.getXOffset(), z + dir.getZOffset(), scale);
                if (other != this) return dir.getOpposite();
            }
            return ret;
        }

        public boolean shouldBeTorch(final Site site, final int x, final int z, final int scale)
        {
            this.getBounds(site, scale);

            if (z > this.bounds[0][1] && z < this.bounds[1][1] && x > this.bounds[0][0] && x < this.bounds[1][0])
                return z % 4 == 0 && x % 4 == 0;

            return false;
        }

        public boolean inWall(final Site site, final int x, final int z, final int scale)
        {
            this.getBounds(site, scale);
            if (z == this.bounds[0][1] || z == this.bounds[1][1] || x == this.bounds[0][0] || x == this.bounds[1][0])
                return true;
            return false;
        }

        public int getFloor(final Site site, final int scale)
        {
            this.getBounds(site, scale);
            int floor = 0;
            final int[] corners = new int[4];
            corners[0] = this.dorfs.biomeInterpolator.interpolate(this.dorfs.elevationMap, this.bounds[0][0],
                    this.bounds[0][1], scale);
            corners[1] = this.dorfs.biomeInterpolator.interpolate(this.dorfs.elevationMap, this.bounds[1][0],
                    this.bounds[1][1], scale);
            corners[2] = this.dorfs.biomeInterpolator.interpolate(this.dorfs.elevationMap, this.bounds[1][0],
                    this.bounds[0][1], scale);
            corners[3] = this.dorfs.biomeInterpolator.interpolate(this.dorfs.elevationMap, this.bounds[0][0],
                    this.bounds[1][1], scale);

            floor = corners[0] + corners[1] + corners[2] + corners[3];
            floor /= 4;

            return floor;
        }

        public int[] getMid(final Site site, final int scale)
        {
            if (this.mid != null) return this.mid;
            this.getBounds(site, scale);
            return this.mid = new int[] { this.bounds[0][0] + (this.bounds[1][0] - this.bounds[0][0]) / 2,
                    this.bounds[0][1] + (this.bounds[1][1] - this.bounds[0][1]) / 2 };
        }
    }

    public static class WallTowerSpace extends StructureSpace
    {
        static int              WALLCOLOUR   = SiteMapColours.TOWNWALL.colour.getRGB();
        /** The two other towers connected to this one by wall segments */
        public WallTowerSpace[] connected    = new WallTowerSpace[2];
        /**
         * The pixel coordinates of the centre of a face of this tower, which
         * are closest to the center of the faces of the connected towers
         */
        int[][]                 wallConnects = new int[2][2];

        public WallTowerSpace(final int[] minCoords, final int[] maxCoords, final DorfMap dorfs)
        {
            super(minCoords, maxCoords, SiteMapColours.TOWERROOF, dorfs);
        }

        public int countConnects()
        {
            return this.connected[0] == null ? 0 : this.connected[1] == null ? 1 : 2;
        }

        @Override
        public boolean inWall(final Site site, final int x, final int z, final int scale)
        {
            this.getBounds(site, scale);

            if (z == this.bounds[0][1] || z == this.bounds[1][1] || x == this.bounds[0][0] || x == this.bounds[1][0])
                return true;
            // //TODO possibly make this choose a different direction for the
            // ladder site
            // boolean mid = x - 1 == getMid(site, scale)[0] && z ==
            // getMid(site, scale)[1];
            return false;
        }

        /**
         * Adds this tower as a connected tower
         *
         * @param tower
         * @param site
         * @return whether the tower can be added
         */
        public boolean addConnected(final WallTowerSpace tower, final Site site)
        {
            if (this.connected[0] == null)
            {
                this.connected[0] = tower;
                this.initWallConnects(tower, site, 0);
                return true;
            }
            if (this.connected[1] == null)
            {
                this.connected[1] = tower;
                this.initWallConnects(tower, site, 1);
                return true;
            }
            return false;
        }

        /**
         * Returns if this Tower actually has any wall pixels near it. If this
         * is false, it should not be added to other walls.
         *
         * @return
         */
        public boolean hasWallConnect(final Site site)
        {
            final int[][] thisConnects = this.getWallAttachments(site);

            return thisConnects[0][0] != -1;
        }

        private void initWallConnects(final WallTowerSpace tower, final Site site, final int index)
        {
            final int[][] towerConnects = tower.getWallAttachments(site);
            final int[][] thisConnects = this.getWallAttachments(site);
            int[] closest = null;
            int distSq = Integer.MAX_VALUE;

            for (final int[] i : thisConnects)
                for (final int[] i1 : towerConnects)
                {
                    final int dx = i[0] - i1[0];
                    final int dy = i[1] - i1[1];
                    final int temp = dx * dx + dy * dy;
                    if (temp < distSq)
                    {
                        distSq = temp;
                        closest = i;
                    }
                }
            if (closest != null && closest[0] != -1) this.wallConnects[index] = closest;
        }

        int[][] getWallAttachments(final Site site)
        {
            final int[][] thisConnects = { { -1, -1 }, { -1, -1 } };
            final int minx = this.min[0], miny = this.min[1], maxx = this.max[0], maxy = this.max[1];
            final int midx = minx + (maxx - minx) / 2;
            final int midy = miny + (maxy - miny) / 2;

            // Check both y edges for wall pixels
            for (int x = minx; x <= maxx; x++)
            {
                final int rgbmin = site.rgbmap[x][miny - 1];
                final int rgbmax = site.rgbmap[x][maxy + 1];
                if (thisConnects[0][0] == -1 && rgbmin == WallTowerSpace.WALLCOLOUR)
                {
                    thisConnects[0][0] = midx;
                    thisConnects[0][1] = miny;
                }
                if (thisConnects[0][0] == -1 && rgbmax == WallTowerSpace.WALLCOLOUR)
                {
                    thisConnects[0][0] = midx;
                    thisConnects[0][1] = maxy;
                }
            }
            for (int x = minx; x <= maxx; x++)
            {
                final int rgbmin = site.rgbmap[x][miny - 1];
                final int rgbmax = site.rgbmap[x][maxy + 1];
                if (thisConnects[1][0] == -1 && rgbmin == WallTowerSpace.WALLCOLOUR)
                {
                    thisConnects[1][0] = midx;
                    thisConnects[1][1] = miny;
                }
                if (thisConnects[1][0] == -1 && rgbmax == WallTowerSpace.WALLCOLOUR)
                {
                    thisConnects[1][0] = midx;
                    thisConnects[1][1] = maxy;
                }
            }

            if (thisConnects[0][0] == -1 || thisConnects[1][0] == -1)
            {
                // If there is a missing entry, check both x edges for wall
                // pixels
                for (int y = miny; y <= maxy; y++)
                {
                    final int rgbmin = site.rgbmap[minx - 1][y];
                    final int rgbmax = site.rgbmap[maxx + 1][y];
                    if (thisConnects[0][0] == -1 && rgbmin == WallTowerSpace.WALLCOLOUR)
                    {
                        thisConnects[0][0] = minx;
                        thisConnects[0][1] = midy;
                    }
                    if (thisConnects[0][0] == -1 && rgbmax == WallTowerSpace.WALLCOLOUR)
                    {
                        thisConnects[0][0] = minx;
                        thisConnects[0][1] = midy;
                    }
                }
                for (int y = miny; y <= maxy; y++)
                {
                    final int rgbmin = site.rgbmap[minx - 1][y];
                    final int rgbmax = site.rgbmap[maxx + 1][y];
                    if (thisConnects[1][0] == -1 && rgbmin == WallTowerSpace.WALLCOLOUR)
                    {
                        thisConnects[1][0] = minx;
                        thisConnects[1][1] = midy;
                    }
                    if (thisConnects[1][0] == -1 && rgbmax == WallTowerSpace.WALLCOLOUR)
                    {
                        thisConnects[1][0] = minx;
                        thisConnects[1][1] = midy;
                    }
                }
            }
            return thisConnects;
        }
    }

    public static class WallSegment
    {
        public static final HashSet<Integer> WALLBITS = new HashSet<>();

        static
        {
            WallSegment.WALLBITS.add(SiteMapColours.TOWNWALL.colour.getRGB());
            WallSegment.WALLBITS.add(SiteMapColours.TOWNWALLMID.colour.getRGB());
        }

        public final HashSet<Integer> pixels = new HashSet<>();

        public WallSegment(final Site site, final int x, final int y)
        {
            this.initWallSegment(site, x, y);
        }

        public WallSegment(final Collection<Integer> pixels_)
        {
            this.pixels.addAll(pixels_);
        }

        /**
         * Checks in site map pixel coordinates.
         *
         * @param x
         * @param y
         * @return
         */
        public boolean isInSegment(final int x, final int y)
        {
            return this.pixels.contains(x + 2048 * y);
        }

        /**
         * Takes Block Coordinates
         *
         * @param site
         * @param x
         * @param y
         * @param scale
         * @return
         */
        public boolean isInWall(final Site site, final int x, final int y, final int scale)
        {
            int width = scale / SiteStructureGenerator.SITETOBLOCK;
            width = Math.max(1, width);
            int pixelX = (x - site.corners[0][0] * scale - scale / 2 - width / 2) / width;
            int pixelY = (y - site.corners[0][1] * scale - scale / 2 - width / 2) / width;
            boolean ret = this.pixels.contains(pixelX + 2048 * pixelY);
            if (width <= 1)
            {
                pixelX = (x - 1 - site.corners[0][0] * scale - scale / 2 - width / 2) / width;
                pixelY = (y - 1 - site.corners[0][1] * scale - scale / 2 - width / 2) / width;
                ret = ret || this.pixels.contains(pixelX + 2048 * pixelY);
                pixelX = (x + 1 - site.corners[0][0] * scale - scale / 2 - width / 2) / width;
                pixelY = (y + 1 - site.corners[0][1] * scale - scale / 2 - width / 2) / width;
                ret = ret || this.pixels.contains(pixelX + 2048 * pixelY);
            }
            return ret;
        }

        public void initWallSegment(final Site site, final int x, final int y)
        {
            final BitSet blocked = new BitSet();
            final BitSet checked = new BitSet();
            final HashSet<Integer> valid = new HashSet<>();
            final int[][] map = site.rgbmap;
            this.checkAround(map, x, y, blocked, checked, valid);
            this.pixels.addAll(valid);

        }

        private boolean checkAround(final int[][] map, final int x, final int y, final BitSet blocked,
                final BitSet checked, final HashSet<Integer> valid)
        {
            final int[] index = new int[4];
            boolean cont = false;
            final int[][] dirs = this.getDirs(map, x, y);
            index[0] = x - 1 + y * 2048;
            index[1] = x + 1 + y * 2048;
            index[2] = x + (y - 1) * 2048;
            index[3] = x + (y + 1) * 2048;
            for (int i = 0; i < 4; i++)
            {
                if (checked.get(index[i])) continue;
                if (dirs[i] != null)
                {
                    valid.add(index[i]);
                    cont = true;
                }
                else blocked.set(index[i]);
                if (valid.contains(index[i]))
                {
                    checked.set(index[i]);
                    this.checkAround(map, index[i] & 2047, index[i] / 2048, blocked, checked, valid);
                }
            }
            return cont;
        }

        /**
         * 0 = -x, 1 = x, 2 = -y, 3 = y
         *
         * @param map
         * @param x
         * @param y
         * @return
         */
        private int[][] getDirs(final int[][] map, final int x, final int y)
        {
            final int[][] ret = new int[4][];
            int rgb = map[x - 1][y];
            if (WallSegment.WALLBITS.contains(rgb)) ret[0] = new int[] { x - 1, y };
            rgb = map[x + 1][y];
            if (WallSegment.WALLBITS.contains(rgb)) ret[1] = new int[] { x + 1, y };
            rgb = map[x][y - 1];
            if (WallSegment.WALLBITS.contains(rgb)) ret[2] = new int[] { x, y - 1 };
            rgb = map[x][y + 1];
            if (WallSegment.WALLBITS.contains(rgb)) ret[3] = new int[] { x, y + 1 };
            return ret;
        }
    }

    public static class TownWall
    {
        final HashSet<WallTowerSpace> towers = new HashSet<>();

    }

    public static class RoadExit
    {
        final int midPixelX;
        final int midPixelY;
        int[]     location;

        public RoadExit(final int x, final int y)
        {
            this.midPixelX = x;
            this.midPixelY = y;
        }

        public int[] getEdgeMid(final Site site, final int scale)
        {
            if (this.location == null)
            {
                this.location = new int[2];
                final int offset = scale / 2;
                this.location[0] = this.midPixelX * (scale / SiteStructureGenerator.SITETOBLOCK) + site.corners[0][0]
                        * scale + offset;
                this.location[1] = this.midPixelY * (scale / SiteStructureGenerator.SITETOBLOCK) + site.corners[0][1]
                        * scale + offset;
                // TODO make roads properly connect on the south side of
                // not-square sites. See rusticmeal at 45924 43563 when scale is
                // 51
            }
            return this.location;
        }
    }

    public static class RiverExit
    {
        final int     midPixelX;
        final int     midPixelY;
        final int     width;
        final boolean xEdge;
        int[]         location;

        public RiverExit(final int x, final int y, final int w, final boolean onX)
        {
            this.midPixelX = x;
            this.midPixelY = y;
            this.width = w;
            this.xEdge = onX;
        }

        public int[] getEdgeMid(final Site site, final int scale)
        {

            // if (location == null)
            {
                this.location = new int[3];

                final int offset = scale / 2;
                this.location[0] = this.midPixelX * (scale / SiteStructureGenerator.SITETOBLOCK) + site.corners[0][0]
                        * scale + offset;
                this.location[1] = this.midPixelY * (scale / SiteStructureGenerator.SITETOBLOCK) + site.corners[0][1]
                        * scale + offset;
                this.location[2] = this.width * (scale / SiteStructureGenerator.SITETOBLOCK);
                if (!this.xEdge) this.location[0] += this.location[2] / 2;
                else this.location[1] += this.location[2] / 2;
                final int mapX = site.rgbmap.length;
                final int mapY = site.rgbmap[0].length;
                if (this.midPixelX > 0 && this.midPixelY > 0 && mapY != mapX)
                {
                    final int scaleY = this.midPixelY < mapY - 2 ? 3 : 1;
                    final int testx = 16 - mapX / SiteStructureGenerator.SITETOBLOCK - 2 * this.location[2];
                    final int testy = 16 - mapY / SiteStructureGenerator.SITETOBLOCK - scaleY * this.location[2];
                    if (this.midPixelX < mapX - 2) this.location[0] -= testx;

                    this.location[1] -= testy;
                }

            }
            return this.location;
        }
    }

    public static enum StructureType
    {
        WALL, HOUSE, TOWER,
    }
}
