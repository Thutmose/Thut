package dorfgen.worldgen;

import static dorfgen.WorldGenerator.scale;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.MINESHAFT;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.RAVINE;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.ANIMALS;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.DUNGEON;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.ICE;

import java.util.Random;

import dorfgen.WorldGenerator;
import dorfgen.conversion.Interpolator.BicubicInterpolator;
import dorfgen.conversion.Interpolator.CachedBicubicInterpolator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

public class ChunkProviderFinite extends ChunkProviderGenerate
{
    /** RNG. */
    private Random                   rand;
    /** Reference to the World object. */
    private World                    worldObj;
    /** are map structures going to be generated (e.g. strongholds) */
    private final boolean            mapFeaturesEnabled;
//    private MapGenBase               caveGenerator             = new MapGenUGRegions();
//    /** Holds Stronghold Generator */
//    private MapGenStronghold         strongholdGenerator       = new MapGenStronghold();
    /** Holds Village Generator */
    private MapGenVillage            villageGenerator          = new MapGenSites();
    /** Holds Mineshaft Generator */
    private MapGenMineshaft          mineshaftGenerator        = new MapGenMineshaft();
//    private MapGenScatteredFeature   scatteredFeatureGenerator = new MapGenScatteredFeature();
    /** Holds ravine generator */
    private MapGenBase               ravineGenerator           = new MapGenRavine();
    private RiverMaker               riverMaker                = new RiverMaker();
    private WorldConstructionMaker   constructor               = new WorldConstructionMaker();
    /** The biomes that are used to generate the chunk */
    private BiomeGenBase[]           biomesForGeneration;
    public BicubicInterpolator       bicubicInterpolator       = new BicubicInterpolator();
    public CachedBicubicInterpolator cachedInterpolator        = new CachedBicubicInterpolator();

    {
        mineshaftGenerator = (MapGenMineshaft) TerrainGen.getModdedMapGen(mineshaftGenerator, MINESHAFT);
        ravineGenerator = TerrainGen.getModdedMapGen(ravineGenerator, RAVINE);
    }

    public ChunkProviderFinite(World world, long seed, boolean features, String generatorOptions)
    {
        super(world, seed, features, generatorOptions);
        this.worldObj = world; // remove
        this.mapFeaturesEnabled = features;
        this.rand = new Random(seed);
    }

    /** Takes Chunk Coordinates */
    public void populateBlocksFromImage(int scale, int chunkX, int chunkZ, ChunkPrimer primer)
    {
        int index;
        int x1, z1, w;
        int x = chunkX * 16;
        int z = chunkZ * 16;
        boolean water = false;

        for (int i1 = 0; i1 < 16; i1++)
        {
            for (int k1 = 0; k1 < 16; k1++)
            {
                x1 = (x + i1) / scale;
                z1 = (z + k1) / scale;
                w = WorldGenerator.instance.dorfs.waterMap[x1][z1];
                int h1 = bicubicInterpolator.interpolate(WorldGenerator.instance.dorfs.elevationMap, x + i1, z + k1,
                        scale);
                h1 = Math.max(h1, 10);
                water = w > 0 || (WorldGenerator.instance.dorfs.countLarger(0, WorldGenerator.instance.dorfs.waterMap,
                        x1, z1, 1) > 0);

                double s = worldObj.provider.getHeight() / 256d;
                if (h1 > worldObj.provider.getHorizon()) h1 = (int) (h1 * s);

                for (int j = 0; j < h1; j++)
                {
                    index = j << 0 | (i1) << 12 | (k1) << 8;
                    primer.setBlockState(index, Blocks.stone.getDefaultState());
                    // blocks[index] = Blocks.stone;

                }
                if (w <= 0) w = (int) (worldObj.provider.getHorizon());
                if (water) for (int j = h1; j < w; j++)
                {
                    index = j << 0 | (i1) << 12 | (k1) << 8;
                    primer.setBlockState(index, Blocks.water.getDefaultState());
                    // blocks[index] = Blocks.water;
                }
            }
        }
    }

    public void fillOceans(int x, int z, ChunkPrimer primer)
    {
        byte b0 = (byte) (worldObj.provider.getHorizon());
        int index;
        for (int i = 0; i < 16; i++)
            for (int k = 0; k < 16; k++)
            {
                for (int j = 0; j < b0; j++)
                {
                    index = j << 0 | (i) << 12 | (k) << 8;
                    if (j < 10)
                    {
                        primer.setBlockState(index, Blocks.stone.getDefaultState());
                        // if (index < blocks.length)
                        // blocks[index] = Blocks.stone;
                        // else
                        // System.err.println(index + " " + i + " " + k);
                    }
                    else
                    {
                        primer.setBlockState(index, Blocks.water.getDefaultState());
                        // if (index < blocks.length)
                        // blocks[index] = Blocks.water;
                        // else
                        // System.err.println(index + " " + i + " " + k);
                    }
                }
                index = i + k * 16;

                biomesForGeneration[index] = BiomeGenBase.deepOcean;
            }

    }

    @Override
    /** Will return back a chunk, if it doesn't exist and its not a MP client it
     * will generates all the blocks for the specified chunk from the map seed
     * and chunk seed */
    public Chunk provideChunk(int chunkX, int chunkZ)
    {
        this.rand.setSeed((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L);

        ChunkPrimer primer = new ChunkPrimer();

        // Block[] ablock = new Block[256 * worldObj.getHeight()];
        // byte[] abyte = new byte[256 * worldObj.getHeight()];

        this.biomesForGeneration = this.worldObj.getWorldChunkManager().loadBlockGeneratorData(this.biomesForGeneration,
                chunkX * 16, chunkZ * 16, 16, 16);
        if (WorldGenerator.instance.dorfs.elevationMap.length == 0) WorldGenerator.finite = false;

        int imgX = chunkX * 16 - WorldGenerator.shift.getX();
        int imgZ = chunkZ * 16 - WorldGenerator.shift.getZ();

        if (imgX >= 0 && imgZ >= 0 && (imgX + 16) / scale <= WorldGenerator.instance.dorfs.elevationMap.length
                && (imgZ + 16) / scale <= WorldGenerator.instance.dorfs.elevationMap[0].length)
        {

            int x = imgX;
            int z = imgZ;
            populateBlocksFromImage(scale, chunkX, chunkZ, primer);
            riverMaker.makeRiversForChunk(worldObj, chunkX, chunkZ, primer, biomesForGeneration);
            constructor.buildSites(worldObj, chunkX, chunkZ, primer, biomesForGeneration);
            constructor.buildRoads(worldObj, chunkX, chunkZ, primer, biomesForGeneration);
            makeBeaches(scale, x / scale, z / scale, primer);
        }
        else if (WorldGenerator.finite)
        {
            this.fillOceans(chunkX, chunkZ, primer);
        }
        else
        {
            return super.provideChunk(chunkX, chunkZ);
        }

        this.replaceBlocksForBiome(chunkX, chunkZ, primer, this.biomesForGeneration);// Replace
                                                                                     // biome
                                                                                     // Blocks
        // this.caveGenerator.func_151539_a(this, this.worldObj, chunkX, chunkZ,
        // ablock);
        // this.ravineGenerator.func_151539_a(this, this.worldObj, chunkX,
        // chunkZ,
        // ablock);

        if (this.mapFeaturesEnabled)
        {
            // this.mineshaftGenerator.func_151539_a(this, this.worldObj,
            // chunkX,
            // chunkZ, ablock);
            // this.villageGenerator.func_151539_a(this, this.worldObj, chunkX,
            // chunkZ, ablock);
            // this.scatteredFeatureGenerator.func_151539_a(this, this.worldObj,
            // chunkX, chunkZ, ablock);
            this.villageGenerator.generate(this, this.worldObj, chunkX, chunkZ, primer);
        }

        Chunk chunk;// = new BigChunk(this.worldObj, ablock, abyte, chunkX,
                    // chunkZ);
        // try {
        // chunk = (Chunk) WorldGenerator.chunkClass.getConstructor(World.class,
        // Block[].class, byte[].class, int.class,
        // int.class).newInstance(this.worldObj, ablock, abyte, chunkX, chunkZ);
        // } catch (Exception e) {
        // chunk = null;
        // }
        chunk = new Chunk(worldObj, primer, chunkX, chunkZ);
        byte[] abyte1 = chunk.getBiomeArray();

        for (int k = 0; k < abyte1.length; ++k)
        {
            abyte1[k] = (byte) this.biomesForGeneration[k].biomeID;
        }

        chunk.generateSkylightMap();
        return chunk;
    }

    @Override
    public void recreateStructures(Chunk chunk, int p_82695_1_, int p_82695_2_)
    {
        if (true) // TODO find out why this keeps being called, it keeps
                  // spawning lairs.
            return;
//        if (this.mapFeaturesEnabled)
//        {
//            // this.mineshaftGenerator.func_151539_a(this, this.worldObj,
//            // p_82695_1_, p_82695_2_, (Block[])null);
//            // this.villageGenerator.func_151539_a(this, this.worldObj,
//            // p_82695_1_, p_82695_2_, (Block[])null);
//            // this.scatteredFeatureGenerator.func_151539_a(this, this.worldObj,
//            // p_82695_1_, p_82695_2_, (Block[])null);
//        }
    }

    @Override
    /** Populates chunk with ores etc etc */
    public void populate(IChunkProvider provider, int x, int z)
    {
        BlockFalling.fallInstantly = true;
        int k = x * 16;
        int l = z * 16;
        BlockPos blockpos = new BlockPos(k, 0, l);
        BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(new BlockPos(k + 16, 0, l + 16));
        this.rand.setSeed(this.worldObj.getSeed());
        long i1 = this.rand.nextLong() / 2L * 2L + 1L;
        long j1 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed((long) x * i1 + (long) z * j1 ^ this.worldObj.getSeed());
        boolean flag = false;

        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(provider, worldObj, rand, x, z, flag));

        // if(true)
        // return;
        if (this.mapFeaturesEnabled)
        {
            this.mineshaftGenerator.generateStructure(this.worldObj, this.rand, new ChunkCoordIntPair(x, z));// Generate
                                                                                                             // structures
                                                                                                             // in
                                                                                                             // chunk
            flag = this.villageGenerator.generateStructure(this.worldObj, this.rand, new ChunkCoordIntPair(x, z));// TODO
            // this.scatteredFeatureGenerator.generateStructuresInChunk(this.worldObj,
            // this.rand, p_73153_2_, p_73153_3_);
            WorldGenerator.instance.structureGen.generate(x, z, worldObj);
        }

        int k1;
        int l1;
//        int i2;

        // if (biomegenbase != BiomeGenBase.desert && biomegenbase !=
        // BiomeGenBase.desertHills && !flag && this.rand.nextInt(8) == 0
        // && TerrainGen.populate(provider, worldObj, rand, x, z, flag, LAKE))
        // {
        // k1 = k + this.rand.nextInt(16) + 8;
        // l1 = this.rand.nextInt(worldObj.getHeight());
        // i2 = l + this.rand.nextInt(16) + 8;
        // (new WorldGenLakes(Blocks.water)).generate(this.worldObj, this.rand,
        // k1, l1, i2);
        // }
        //
        // if (TerrainGen.populate(provider, worldObj, rand, x, z, flag, LAVA)
        // && !flag && this.rand.nextInt(16) == 0)
        // {
        // k1 = k + this.rand.nextInt(16) + 8;
        // l1 = this.rand.nextInt(this.rand.nextInt(worldObj.getHeight()-8) +
        // 8);
        // i2 = l + this.rand.nextInt(16) + 8;
        //
        // if (l1 < 63 || this.rand.nextInt(10) == 0)
        // {
        // (new WorldGenLakes(Blocks.lava)).generate(this.worldObj, this.rand,
        // k1, l1, i2);
        // }
        // }//TODO ponds and lakes in appropriate biomes

        boolean doGen = TerrainGen.populate(provider, worldObj, rand, x, z, flag, DUNGEON);
        for (k1 = 0; doGen && k1 < 8; ++k1)
        {
            // l1 = k + this.rand.nextInt(16) + 8;
            // i2 = this.rand.nextInt(worldObj.getHeight());
            // int j2 = l + this.rand.nextInt(16) + 8;
            // (new WorldGenDungeons()).generate(this.worldObj, this.rand,
            // blockpos.add(l1, i2, j2));//TODO
        }

        biomegenbase.decorate(this.worldObj, this.rand, new BlockPos(k, 0, l));
        if (TerrainGen.populate(provider, worldObj, rand, x, z, flag, ANIMALS))
        {
            // SpawnerAnimals.performWorldGenSpawning(this.worldObj,
            // biomegenbase, k + 8, l + 8, 16, 16, this.rand);
        }

        blockpos = blockpos.add(8, 0, 8);
        doGen = TerrainGen.populate(provider, worldObj, rand, x, z, flag, ICE);
        for (k1 = 0; doGen && k1 < 16; ++k1)
        {
            for (l1 = 0; l1 < 16; ++l1)
            {
                BlockPos blockpos1 = this.worldObj.getPrecipitationHeight(blockpos.add(k1, 0, l1));
                BlockPos blockpos2 = blockpos1.down();

                if (this.worldObj.canBlockFreezeWater(blockpos2)) // .func_175675_v(blockpos2))
                {
                    this.worldObj.setBlockState(blockpos2, Blocks.ice.getDefaultState(), 2);
                }

                if (this.worldObj.canSnowAt(blockpos1, true))
                {
                    this.worldObj.setBlockState(blockpos1, Blocks.snow_layer.getDefaultState(), 2);
                }
            }
        }

        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(provider, worldObj, rand, x, z, flag));

        BlockFalling.fallInstantly = false;
    }

    /** Takes Blocks Coordinates
     * 
     * @param scale
     *            - number of blocks per pixel
     * @param x
     *            - x coordinate of the pixel being used
     * @param z
     *            - y coordinate of the pixel being used
     * @param blocks */
    private void makeBeaches(int scale, int x, int z, ChunkPrimer blocks)
    {
        int index;
        int x1, z1, h1;
        for (int i1 = 0; i1 < 16; i1++)
        {
            for (int k1 = 0; k1 < 16; k1++)
            {
                x1 = x + i1 / scale;
                z1 = z + k1 / scale;
                h1 = WorldGenerator.instance.dorfs.elevationMap[x1][z1];
                int b1 = biomesForGeneration[i1 + 16 * k1].biomeID;
                boolean beach = false;

                if (b1 == BiomeGenBase.ocean.biomeID || b1 == BiomeGenBase.deepOcean.biomeID
                        || b1 == BiomeGenBase.beach.biomeID)
                {
                    for (int j = 100; j > 10; j--)
                    {
                        index = j << 0 | (i1) << 12 | (k1) << 8;
                        if (!isIndexEmpty(blocks, index) && getBlock(blocks, index) != Blocks.water)
                        {
                            h1 = j;
                            beach = true;
                            break;
                        }
                    }
                }
                if (beach)
                {
                    for (int j = h1 + 1; j < worldObj.provider.getHorizon(); j++)
                    {
                        index = j << 0 | (i1) << 12 | (k1) << 8;
                        blocks.setBlockState(index, Blocks.water.getDefaultState());
                    }
                }
            }
        }
    }

    public static Block getBlock(ChunkPrimer primer, int index)
    {
        IBlockState state = primer.getBlockState(index);
        return state != null ? state.getBlock() : Blocks.air;
    }

    public static boolean isIndexEmpty(ChunkPrimer primer, int index)
    {
        IBlockState state = primer.getBlockState(index);
        return state == null || state.getBlock() == Blocks.air;
    }

    /** Converts the instance data to a readable string. */
    public String makeString()
    {
        return "FiniteLevelSource";
    }
}