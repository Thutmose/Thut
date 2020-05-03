package dorfgen.world.gen;

import dorfgen.Dorfgen;
import dorfgen.conversion.DorfMap;
import dorfgen.conversion.Interpolator.BicubicInterpolator;
import dorfgen.conversion.SiteStructureGenerator;
import dorfgen.world.feature.BeachMaker;
import dorfgen.world.feature.RiverMaker;
import dorfgen.world.feature.RoadMaker;
import net.minecraft.block.BlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.GenerationStage.Carving;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;

public class DorfChunkGenerator extends ChunkGenerator<DorfSettings>
{
    public BicubicInterpolator bicubicInterpolator = new BicubicInterpolator();
    private final RiverMaker   riverMaker;
    private final RoadMaker    roadMaker;
    private final BeachMaker   beachMaker;
    private final DorfMap      dorfmap;

    private final DorfBiomeProvider biomes;

    public DorfChunkGenerator(final IWorld worldIn, final BiomeProvider biomeProviderIn,
            final DorfSettings generationSettingsIn)
    {
        super(worldIn, biomeProviderIn, generationSettingsIn);
        this.biomes = (DorfBiomeProvider) biomeProviderIn;
        this.dorfmap = Dorfgen.instance.getDorfs(worldIn);
        this.biomes.dorfs = this.dorfmap;
        this.beachMaker = new BeachMaker(worldIn, this.biomes);
        this.roadMaker = new RoadMaker(worldIn);
        this.riverMaker = new RiverMaker(worldIn);
    }

    @Override
    public void generateBiomes(final IChunk chunkIn)
    {
        this.biomes.forGen = true;
        super.generateBiomes(chunkIn);
        this.biomes.forGen = false;
    }

    @Override
    public void func_225551_a_(final WorldGenRegion region, final IChunk chunk)
    {
        // This should apply biome replacements, etc
        final int x0 = chunk.getPos().x << 4;
        final int z0 = chunk.getPos().z << 4;
        final SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
        sharedseedrandom.setBaseChunkSeed(chunk.getPos().x, chunk.getPos().z);

        final int imgX = x0 - Dorfgen.shift.getX();
        final int imgZ = z0 - Dorfgen.shift.getZ();
        final int scale = this.dorfmap.scale;

        final BlockPos.Mutable pos = new BlockPos.Mutable();
        final BicubicInterpolator interp = this.bicubicInterpolator;
        final int[][] map = this.dorfmap.elevationMap;

        this.beachMaker.makeBeaches(this.getSeaLevel(), this.getSettings(), chunk, pos);

        // Block coordinates
        int x, y, z;
        // Just do void for now if out of range, maybe do ocean later or
        // something?
        if (imgX < 0 || (imgX + 16) / scale >= map.length) return;
        if (imgZ < 0 || (imgZ + 16) / scale >= map[0].length) return;
        for (int dx = 0; dx < 16; dx++)
            for (int dz = 0; dz < 16; dz++)
            {
                x = x0 + dx;
                z = z0 + dz;
                y = interp.interpolate(map, x, z, scale);
                region.getBiome(pos.setPos(x, y, z)).buildSurface(sharedseedrandom, chunk, x, z, y, 1, this
                        .getSettings().getDefaultBlock(), this.getSettings().getDefaultFluid(), this.getSeaLevel(),
                        this.world.getSeed());
            }
        this.riverMaker.makeRiversForChunk(chunk, pos);
    }

    @Override
    public int getGroundHeight()
    {
        // TODO adjust this?
        return 64;
    }

    @Override
    public void decorate(final WorldGenRegion region)
    {
        final int i = region.getMainChunkX();
        final int j = region.getMainChunkZ();
        final int k = i * 16;
        final int l = j * 16;
        final BlockPos blockpos = new BlockPos(k, 0, l);
        final Biome biome = this.getBiome(region.getBiomeManager(), blockpos.add(8, 8, 8));
        final SharedSeedRandom random = new SharedSeedRandom();
        final long i1 = random.setDecorationSeed(region.getSeed(), k, l);
        for (final GenerationStage.Decoration stage : GenerationStage.Decoration.values())
            try
            {
                if (stage == Decoration.LOCAL_MODIFICATIONS) continue;

                int featureIndex = 0;
                for (final ConfiguredFeature<?, ?> configuredfeature : biome.getFeatures(stage))
                {

                    if (configuredfeature.feature == Feature.LAKE)
                    {
                        ++featureIndex;
                        continue;
                    }
                    random.setFeatureSeed(i1, featureIndex, stage.ordinal());
                    try
                    {
                        configuredfeature.place(region, this, random, blockpos);
                    }
                    catch (final Exception exception)
                    {
                        final CrashReport crashreport = CrashReport.makeCrashReport(exception, "Feature placement");
                        crashreport.makeCategory("Feature").addDetail("Id", configuredfeature.feature.getRegistryName())
                                .addDetail("Description", () ->
                                {
                                    return configuredfeature.feature.toString();
                                });
                        throw new ReportedException(crashreport);
                    }
                    ++featureIndex;
                }
            }
            catch (final Exception exception)
            {
                final CrashReport crashreport = CrashReport.makeCrashReport(exception, "Biome decoration");
                crashreport.makeCategory("Generation").addDetail("CenterX", i).addDetail("CenterZ", j).addDetail("Step",
                        stage).addDetail("Seed", i1).addDetail("Biome", biome.getRegistryName());
                throw new ReportedException(crashreport);
            }

        final IChunk chunk = region.getChunk(i, j);
        final Mutable pos = new Mutable();
        if (this.dorfmap.scale >= SiteStructureGenerator.SITETOBLOCK)
        {
            // this.roadMaker.buildRoads(chunk, pos);
            this.dorfmap.structureGen.generate(chunk, region, pos);
            this.roadMaker.buildSites(chunk, pos);
        }
    }

    @Override
    public int func_222531_c(final int p_222531_1_, final int p_222531_2_, final Type heightmapType)
    {
        return super.func_222531_c(p_222531_1_, p_222531_2_, heightmapType);
    }

    @Override
    public int func_222532_b(final int p_222532_1_, final int p_222532_2_, final Type heightmapType)
    {
        return super.func_222532_b(p_222532_1_, p_222532_2_, heightmapType);
    }

    @Override
    public void func_225550_a_(final BiomeManager p_225550_1_, final IChunk p_225550_2_, final Carving p_225550_3_)
    {
        // super.func_225550_a_(p_225550_1_, p_225550_2_, p_225550_3_);
    }

    @Override
    public void makeBase(final IWorld worldIn, final IChunk chunkIn)
    {
        final ChunkPrimer chunkprimer = (ChunkPrimer) chunkIn;

        final int x0 = chunkIn.getPos().x << 4;
        final int z0 = chunkIn.getPos().z << 4;

        final int imgX = x0 - Dorfgen.shift.getX();
        final int imgZ = z0 - Dorfgen.shift.getZ();
        final int scale = this.dorfmap.scale;

        final BicubicInterpolator interp = this.bicubicInterpolator;
        final int[][] map = this.dorfmap.elevationMap;

        // Just do void for now if out of range, maybe do ocean later or
        // something?
        if (imgX < 0 || (imgX + 16) / scale >= map.length) return;
        if (imgZ < 0 || (imgZ + 16) / scale >= map[0].length) return;

        // Block coordinates
        int x, y, z;

        int ySeg = 0;
        final int ySegprev = 0;
        ChunkSection chunksection = chunkprimer.getSection(ySeg);
        chunksection.lock();
        for (ySeg = 0; ySeg < 16; ySeg++)
        {
            if (ySeg != ySegprev)
            {
                chunksection.unlock();
                chunksection = chunkprimer.getSection(ySeg);
                chunksection.lock();
            }
            for (int dx = 0; dx < 16; dx++)
                for (int dz = 0; dz < 16; dz++)
                {
                    x = imgX + dx;
                    z = imgZ + dz;
                    final int yMax = interp.interpolate(map, x, z, scale) - 1;
                    for (int dy = 0; dy < 16; dy++)
                    {
                        y = dy + (ySeg << 4);
                        final BlockState state = y > yMax ? y < this.getSeaLevel() ? this.getSettings()
                                .getDefaultFluid() : null : this.getSettings().getDefaultBlock();
                        if (state == null) break;
                        chunksection.setBlockState(dx, dy, dz, state);
                    }
                }
        }
        chunksection.unlock();
    }

    @Override
    /**
     * x and z are in world coordinates
     */
    public int func_222529_a(final int x, final int z, final Type heightmapType)
    {
        final int imgX = x - Dorfgen.shift.getX();
        final int imgZ = z - Dorfgen.shift.getZ();
        final int scale = this.dorfmap.scale;
        final BicubicInterpolator interp = this.bicubicInterpolator;
        final int[][] map = this.dorfmap.elevationMap;
        // Just do void for now if out of range, maybe do ocean later or
        // something?
        if (imgX < 0 || imgX / scale >= map.length) return 0;
        if (imgZ < 0 || imgZ / scale >= map[0].length) return 0;
        return interp.interpolate(map, imgX, imgZ, scale);
    }

}
