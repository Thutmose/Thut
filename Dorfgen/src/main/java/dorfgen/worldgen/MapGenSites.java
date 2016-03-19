package dorfgen.worldgen;

import static dorfgen.WorldGenerator.scale;
import static net.minecraftforge.common.ChestGenHooks.DUNGEON_CHEST;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import dorfgen.WorldGenerator;
import dorfgen.conversion.DorfMap;
import dorfgen.conversion.DorfMap.Site;
import dorfgen.conversion.DorfMap.SiteType;
import net.minecraft.block.BlockLadder;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.ComponentScatteredFeaturePieces;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.StructureStrongholdPieces;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.DungeonHooks;

public class MapGenSites extends MapGenVillage
{
    HashSet<Integer> set       = new HashSet<Integer>();
    HashSet<Integer> made      = new HashSet<Integer>();
    Site             siteToGen = null;

    public MapGenSites()
    {
        super();
    }

    public MapGenSites(Map<String, String> p_i2093_1_)
    {
        super(p_i2093_1_);
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int x, int z)
    {
        x *= 16;
        z *= 16;
        x -= WorldGenerator.shift.getX();
        z -= WorldGenerator.shift.getZ();
        DorfMap dorfs = WorldGenerator.instance.dorfs;

        HashSet<Site> sites = dorfs.getSiteForCoords(x, z);

        if (sites == null) return false;

        for (Site site : sites)
        {
            if (!set.contains(site.id) && shouldSiteSpawn(x, z, site))
            {
                set.add(site.id);
                siteToGen = site;
                System.out.println("Chosen to gen " + site);
                return true;
            }
        }

        return false;
    }

    public static boolean shouldSiteSpawn(int x, int z, Site site)
    {
        if (site.type == SiteType.LAIR)
        {
            int embarkX = (x / scale) * scale;
            int embarkZ = (z / scale) * scale;

            if (embarkX / scale != site.x || embarkZ / scale != site.z) return false;
            for (int i = 0; i < 16; i++)
            {
                for (int j = 0; j < 16; j++)
                {
                    int relX = (x + i) % scale + 8;
                    int relZ = (z + j) % scale + 8;
                    boolean middle = relX / 16 == scale / 32 && relZ / 16 == scale / 32;
                    if (middle)
                    {
                        System.out.println(site);
                        return true;
                    }
                }
            }
            return false;

        }
        return false;
    }

    @Override
    protected StructureStart getStructureStart(int x, int z)
    {
        Site site = siteToGen;
        siteToGen = null;
        if (site == null) { return super.getStructureStart(x, z); }
        System.out.println("Generating Site " + site);
        made.add(site.id);
        if (site.type == SiteType.FORTRESS)
        {
            MapGenStronghold.Start start;

            for (start = new MapGenStronghold.Start(this.worldObj, this.rand, x,
                    z); start.getComponents().isEmpty() || ((StructureStrongholdPieces.Stairs2) start.getComponents()
                            .get(0)).strongholdPortalRoom == null; start = new MapGenStronghold.Start(this.worldObj,
                                    this.rand, x, z))
            {
                ;
            }
            return start;
        }
        else if (site.type == SiteType.DARKFORTRESS)
        {

        }
        else if (site.type == SiteType.DARKPITS)
        {

        }
        else if (site.type == SiteType.HIPPYHUTS)
        {
            return new Start(worldObj, rand, x, z, 0);
        }
        else if (site.type == SiteType.SHRINE)
        {
            return new Start(worldObj, rand, x, z, 2);
        }
        else if (site.type == SiteType.LAIR)
        {
            return new Start(worldObj, rand, x, z, 3);
        }
        else if (site.type == SiteType.CAVE) { return new Start(worldObj, rand, x, z, 1); }
        return super.getStructureStart(x, z);
    }

    public static class Start extends StructureStart
    {
        public Start()
        {
        }

        public Start(World world_, Random rand, int x, int z, int type)
        {
            super(x, z);
            if (type == 0)
            {
                for (int k = 0; k < 15; k++)
                {
                    int x1 = 40 - rand.nextInt(40);
                    int z1 = 40 - rand.nextInt(40);

                    for (int i = 0; i < rand.nextInt(20); i++)
                    {

                        ComponentScatteredFeaturePieces.SwampHut swamphut = new ComponentScatteredFeaturePieces.SwampHut(
                                rand, x * 16 + x1, z * 16 + z1);

                        this.components.add(swamphut);
                    }
                }
            }
            else if (type == 1)
            {
                ComponentScatteredFeaturePieces.DesertPyramid desertpyramid = new ComponentScatteredFeaturePieces.DesertPyramid(
                        rand, x * 16, z * 16);
                this.components.add(desertpyramid);
            }
            else if (type == 2)
            {
                ComponentScatteredFeaturePieces.JunglePyramid junglepyramid = new ComponentScatteredFeaturePieces.JunglePyramid(
                        rand, x * 16, z * 16);
                this.components.add(junglepyramid);
            }
            else if (type == 3)
            {
                System.out.println("Making a lair");

                int h = WorldGenerator.instance.dorfs.biomeInterpolator
                        .interpolate(WorldGenerator.instance.dorfs.elevationMap, x * 16, z * 16, scale);

                BlockPos pos = new BlockPos(x * 16, h - 5, z * 16);
                BlockPos blockpos1;
                int i = rand.nextInt(2) + 2;
                int j = -i - 1;
                int k = i + 1;
                int l = rand.nextInt(2) + 2;
                int i1 = -l - 1;
                int j1 = l + 1;
                int l1;
                int i2;
                int j2;
                for (l1 = j; l1 <= k; ++l1)
                {
                    for (i2 = 3; i2 >= -1; --i2)
                    {
                        for (j2 = i1; j2 <= j1; ++j2)
                        {
                            blockpos1 = pos.add(l1, i2, j2);
                            world_.setBlockState(blockpos1, Blocks.stone.getDefaultState(), 2);
                        }
                    }
                }
                world_.setBlockState(pos.add(k - 1, 4, 0), Blocks.trapdoor.getDefaultState(), 2);
                System.out.println(pos);

                // TODO re-copy dungeon code from 1.8 again for this
                for (l1 = j; l1 <= k; ++l1)
                {
                    for (i2 = 3; i2 >= -1; --i2)
                    {
                        for (j2 = i1; j2 <= j1; ++j2)
                        {
                            blockpos1 = pos.add(l1, i2, j2);

                            if (l1 != j && i2 != -1 && j2 != i1 && l1 != k && i2 != 4 && j2 != j1)
                            {
                                if (world_.getBlockState(blockpos1).getBlock() != Blocks.chest)
                                {
                                    world_.setBlockToAir(blockpos1);
                                }
                            }
                            else if (blockpos1.getY() >= 0
                                    && !world_.getBlockState(blockpos1.down()).getBlock().getMaterial().isSolid())
                            {
                                world_.setBlockToAir(blockpos1);
                            }
                            else if (world_.getBlockState(blockpos1).getBlock().getMaterial().isSolid()
                                    && world_.getBlockState(blockpos1).getBlock() != Blocks.chest)
                            {
                                if (i2 == -1 && rand.nextInt(4) != 0)
                                {
                                    world_.setBlockState(blockpos1, Blocks.mossy_cobblestone.getDefaultState(), 2);
                                }
                                else
                                {
                                    world_.setBlockState(blockpos1, Blocks.cobblestone.getDefaultState(), 2);
                                }
                            }
                        }
                    }
                }

                l1 = 0;

                while (l1 < 2)
                {
                    i2 = 0;

                    while (true)
                    {
                        if (i2 < 3)
                        {
                            label197:
                            {
                                j2 = pos.getX() + rand.nextInt(i * 2 + 1) - i;
                                int l2 = pos.getY();
                                int i3 = pos.getZ() + rand.nextInt(l * 2 + 1) - l;
                                BlockPos blockpos2 = new BlockPos(j2, l2, i3);

                                if (world_.isAirBlock(blockpos2))
                                {
                                    int k2 = 0;
                                    Iterator<?> iterator = EnumFacing.Plane.HORIZONTAL.iterator();

                                    while (iterator.hasNext())
                                    {
                                        EnumFacing enumfacing = (EnumFacing) iterator.next();

                                        if (world_.getBlockState(blockpos2.offset(enumfacing)).getBlock().getMaterial()
                                                .isSolid())
                                        {
                                            ++k2;
                                        }
                                    }

                                    if (k2 == 1)
                                    {
                                        world_.setBlockState(blockpos2, Blocks.chest.correctFacing(world_, blockpos2,
                                                Blocks.chest.getDefaultState()), 2);
                                        TileEntity tileentity1 = world_.getTileEntity(blockpos2);

                                        if (tileentity1 instanceof TileEntityChest)
                                        {
                                            WeightedRandomChestContent.generateChestContents(rand,
                                                    ChestGenHooks.getItems(DUNGEON_CHEST, rand),
                                                    (TileEntityChest) tileentity1,
                                                    ChestGenHooks.getCount(DUNGEON_CHEST, rand));
                                        }

                                        break label197;
                                    }
                                }

                                ++i2;
                                continue;
                            }
                        }

                        ++l1;
                        break;
                    }
                }
                // Build Ladder
                for (int h1 = 0; h1 < 4; h1++)
                {
                    world_.setBlockState(pos.add(k - 1, h1, 0),
                            Blocks.ladder.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.WEST), 2);
                }

                world_.setBlockState(pos, Blocks.mob_spawner.getDefaultState(), 2);
                TileEntity tileentity = world_.getTileEntity(pos);

                if (tileentity instanceof TileEntityMobSpawner)
                {
                    ((TileEntityMobSpawner) tileentity).getSpawnerBaseLogic()
                            .setEntityName(DungeonHooks.getRandomDungeonMob(rand));
                }
                else
                {
                    System.err.println("Failed to fetch mob spawner entity at (" + pos.getX() + ", " + pos.getY() + ", "
                            + pos.getZ() + ")");
                }
            }

            this.updateBoundingBox();
        }
    }
}
