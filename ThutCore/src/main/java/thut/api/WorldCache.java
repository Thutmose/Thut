package thut.api;

import java.util.Set;

import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

/** This class is used for thread-safe world access.
 * 
 * @author Thutmose */
public class WorldCache implements IBlockAccess
{
    public static class ChunkCache
    {
        Chunk                          chunk;
        private ExtendedBlockStorage[] storageArrays;

        public ChunkCache(Chunk chunk)
        {
            this.chunk = chunk;
            update();
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

                return Blocks.AIR.getDefaultState();
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Getting block state");
                throw new ReportedException(crashreport);
            }
        }

        public TileEntity getTileEntity(BlockPos pos, EnumCreateEntityType immediate)
        {
            return chunk.getTileEntity(pos, immediate);
        }

        public boolean isEmpty()
        {
            return false;
        }

        public synchronized void update()
        {
            if (storageArrays == null) storageArrays = new ExtendedBlockStorage[chunk.getBlockStorageArray().length];
            for (int i = 0; i < storageArrays.length; i++)
            {
                if (chunk.getBlockStorageArray()[i] != null)
                {
                    // BlockStateContainer to;
                    // BlockStateContainer from =
                    // chunk.getBlockStorageArray()[i].getData();
                    if (storageArrays[i] == null)
                    {
                        storageArrays[i] = chunk.getBlockStorageArray()[i];// TODO
                                                                           // figure
                                                                           // out
                                                                           // copying
                                                                           // this.
                    }
                    // to = storageArrays[i].getData();
                    // storageArrays[i]..setData(to);
                }
                else
                {
                    storageArrays[i] = null;
                }
            }
        }

    }

    public final World                    world;
    private final Long2ObjectMap<ChunkCache> map   = new Long2ObjectOpenHashMap<>();

    final Set<ChunkCache>                 cache = Sets.newConcurrentHashSet();

    public WorldCache(World world_)
    {
        world = world_;
    }

    void addChunk(Chunk chunk)
    {
        long key = ChunkPos.chunkXZ2Int(chunk.xPosition, chunk.zPosition);
        ChunkCache chunkcache = new ChunkCache(chunk);
        map.put(key, chunkcache);
        cache.add(chunkcache);
    }

    @Override
    public boolean extendedLevelsInChunkCache()
    {
        return false;
    }

    @Override
    public Biome getBiomeGenForCoords(BlockPos pos)
    {
        return null;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos)
    {
        int l = (pos.getX() >> 4);
        int i1 = (pos.getZ() >> 4);
        long key = ChunkPos.chunkXZ2Int(l, i1);
        ChunkCache chunk = map.get(key);
        if (chunk == null) return null;
        return chunk.getBlockState(pos);
    }

    public Chunk getChunk(int chunkX, int chunkZ)
    {
        long key = ChunkPos.chunkXZ2Int(chunkX, chunkZ);
        ChunkCache chunkcache = map.get(key);
        if (chunkcache == null) return null;
        return chunkcache.chunk;
    }

    @Override
    public int getCombinedLight(BlockPos pos, int p_175626_2_)
    {
        return 0;
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction)
    {
        return 0;
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos)
    {
        int l = (pos.getX() >> 4);
        int i1 = (pos.getZ() >> 4);
        long key = ChunkPos.chunkXZ2Int(l, i1);
        ChunkCache chunk = map.get(key);
        if (chunk == null) return null;
        return chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
    }

    @Override
    public WorldType getWorldType()
    {
        return world.getWorldInfo().getTerrainType();
    }

    @Override
    public boolean isAirBlock(BlockPos pos)
    {
        IBlockState state;
        return (state = getBlockState(pos)) == null || state.getBlock().isAir(state, this, pos);
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default)
    {
        int l = (pos.getX() >> 4);
        int i1 = (pos.getZ() >> 4);
        long key = ChunkPos.chunkXZ2Int(l, i1);
        ChunkCache chunk = map.get(key);
        if (chunk == null || chunk.isEmpty()) return _default;
        IBlockState state;
        return (state = getBlockState(pos)).getBlock().isSideSolid(state, this, pos, side);
    }

    void removeChunk(Chunk chunk)
    {
        long key = ChunkPos.chunkXZ2Int(chunk.xPosition, chunk.zPosition);
        ChunkCache chunkcache = map.remove(key);
        if (chunkcache != null) cache.remove(chunkcache);
    }
}
