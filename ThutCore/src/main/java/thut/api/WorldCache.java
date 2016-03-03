package thut.api;

import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.collect.Sets;

import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

/** This class is used for thread-safe world access.
 * 
 * @author Thutmose */
public class WorldCache implements IBlockAccess
{
    public final World                    world;
    private final LongHashMap<ChunkCache> map   = new LongHashMap<>();
    final Set<ChunkCache>                 cache = Sets.newConcurrentHashSet();

    public WorldCache(World world_)
    {
        world = world_;
    }

    void addChunk(Chunk chunk)
    {
        long key = ChunkCoordIntPair.chunkXZ2Int(chunk.xPosition, chunk.zPosition);
        ChunkCache chunkcache = new ChunkCache(chunk);
        map.add(key, chunkcache);
        cache.add(chunkcache);
    }

    void removeChunk(Chunk chunk)
    {
        long key = ChunkCoordIntPair.chunkXZ2Int(chunk.xPosition, chunk.zPosition);
        ChunkCache chunkcache = map.remove(key);
        cache.remove(chunkcache);
    }

    public Chunk getChunk(int chunkX, int chunkZ)
    {
        long key = ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ);
        ChunkCache chunkcache = map.getValueByKey(key);
        if (chunkcache == null) return null;
        return chunkcache.chunk;
    }

    @Override
    public boolean extendedLevelsInChunkCache()
    {
        return false;
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos)
    {
        int l = (pos.getX() >> 4);
        int i1 = (pos.getZ() >> 4);
        long key = ChunkCoordIntPair.chunkXZ2Int(l, i1);
        ChunkCache chunk = map.getValueByKey(key);
        if (chunk == null) return null;
        return chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
    }

    @Override
    public int getCombinedLight(BlockPos pos, int p_175626_2_)
    {
        return 0;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos)
    {
        int l = (pos.getX() >> 4);
        int i1 = (pos.getZ() >> 4);
        long key = ChunkCoordIntPair.chunkXZ2Int(l, i1);
        ChunkCache chunk = map.getValueByKey(key);
        if (chunk == null) return null;
        return chunk.getBlockState(pos);
    }

    @Override
    public boolean isAirBlock(BlockPos pos)
    {
        return getBlockState(pos) == null || getBlockState(pos).getBlock().isAir(this, pos);
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(BlockPos pos)
    {
        return null;
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction)
    {
        return 0;
    }

    @Override
    public WorldType getWorldType()
    {
        return world.getWorldInfo().getTerrainType();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default)
    {
        int l = (pos.getX() >> 4);
        int i1 = (pos.getZ() >> 4);
        long key = ChunkCoordIntPair.chunkXZ2Int(l, i1);
        ChunkCache chunk = map.getValueByKey(key);
        if (chunk == null || chunk.isEmpty()) return _default;
        return getBlockState(pos).getBlock().isSideSolid(this, pos, side);
    }

    public static class ChunkCache
    {
        Chunk                          chunk;
        private ExtendedBlockStorage[] storageArrays;

        public ChunkCache(Chunk chunk)
        {
            this.chunk = chunk;
            update();
        }

        public boolean isEmpty()
        {
            return false;
        }

        public IBlockState getBlockState(final BlockPos pos)
        {
            try
            {
                if (pos.getY() >= 0 && pos.getY() >> 4 < this.storageArrays.length)
                {
                    ExtendedBlockStorage extendedblockstorage = this.storageArrays[pos.getY() >> 4];

                    if (extendedblockstorage != null)
                    {
                        int j = pos.getX() & 15;
                        int k = pos.getY() & 15;
                        int i = pos.getZ() & 15;
                        return extendedblockstorage.get(j, k, i);
                    }
                }

                return Blocks.air.getDefaultState();
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Getting block state");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being got");
                crashreportcategory.addCrashSectionCallable("Location", new Callable<String>()
                {
                    public String call() throws Exception
                    {
                        return CrashReportCategory.getCoordinateInfo(pos);
                    }
                });
                throw new ReportedException(crashreport);
            }
        }

        public TileEntity getTileEntity(BlockPos pos, EnumCreateEntityType immediate)
        {
            return chunk.getTileEntity(pos, immediate);
        }

        public synchronized void update()
        {
            if (storageArrays == null) storageArrays = new ExtendedBlockStorage[chunk.getBlockStorageArray().length];
            for (int i = 0; i < storageArrays.length; i++)
            {
                if (chunk.getBlockStorageArray()[i] != null)
                {
                    char[] to;
                    char[] from = chunk.getBlockStorageArray()[i].getData();
                    if (storageArrays[i] == null)
                    {
                        storageArrays[i] = new ExtendedBlockStorage(i, false);
                    }
                    to = storageArrays[i].getData();
                    System.arraycopy(from, 0, to, 0, to.length);
                    storageArrays[i].setData(to);
                }
                else
                {
                    storageArrays[i] = null;
                }
            }
        }

    }
}
