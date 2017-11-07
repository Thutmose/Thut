package thut.api;

import java.util.Set;

import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;

/** This class is used for thread-safe world access.
 * 
 * @author Thutmose */
public class WorldCache implements IBlockAccess
{
    public static class ChunkCache
    {
        Chunk chunk;

        public ChunkCache(Chunk chunk)
        {
            this.chunk = chunk;
        }

        public IBlockState getBlockState(final BlockPos pos)
        {
            IBlockState ret = null;
            synchronized (chunk)
            {
                ret = chunk.getBlockState(pos);
                if (ret == null) return Blocks.AIR.getDefaultState();
            }
            return ret;
        }

        public TileEntity getTileEntity(BlockPos pos, EnumCreateEntityType immediate)
        {
            TileEntity ret = null;
            synchronized (chunk)
            {
                ret = chunk.getTileEntity(pos, immediate);
            }
            return ret;
        }

        public boolean isEmpty()
        {
            return false;
        }
    }

    public static boolean                    synchronizeWorlds = true;

    public final World                       world;
    private final Long2ObjectMap<ChunkCache> map               = new Long2ObjectOpenHashMap<>();

    final Set<ChunkCache>                    cache             = Sets.newConcurrentHashSet();

    public WorldCache(World world_)
    {
        world = world_;
    }

    void addChunk(Chunk chunk)
    {
        long key = asLong(chunk.x, chunk.z);
        ChunkCache chunkcache = new ChunkCache(chunk);
        map.put(key, chunkcache);
        cache.add(chunkcache);
    }

    // @Override
    public boolean extendedLevelsInChunkCache()
    {
        return false;
    }

    @Override
    public Biome getBiome(BlockPos pos)
    {
        Biome ret = null;
        synchronized (world)
        {
            ret = world.getBiome(pos);
        }
        return ret;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos)
    {
        if (synchronizeWorlds)
        {
            synchronized (world)
            {
                return world.getBlockState(pos);
            }
        }
        int l = (pos.getX() >> 4);
        int i1 = (pos.getZ() >> 4);
        long key = asLong(l, i1);
        ChunkCache chunk = map.get(key);
        if (chunk == null) return Blocks.AIR.getDefaultState();
        return chunk.getBlockState(pos);
    }

    public Chunk getChunk(int chunkX, int chunkZ)
    {
        if (synchronizeWorlds)
        {
            synchronized (world)
            {
                return world.getChunkFromChunkCoords(chunkX, chunkZ);
            }
        }
        long key = asLong(chunkX, chunkZ);
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
        if (synchronizeWorlds)
        {
            synchronized (world)
            {
                return world.getTileEntity(pos);
            }
        }
        int l = (pos.getX() >> 4);
        int i1 = (pos.getZ() >> 4);
        long key = asLong(l, i1);
        ChunkCache chunk = map.get(key);
        if (chunk == null) return null;
        return chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
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
        long key = asLong(l, i1);
        ChunkCache chunk = map.get(key);
        if (chunk == null || chunk.isEmpty()) return _default;
        return getBlockState(pos).getBlockFaceShape(this, pos, side) == BlockFaceShape.SOLID;
    }

    void removeChunk(Chunk chunk)
    {
        long key = asLong(chunk.x, chunk.z);
        ChunkCache chunkcache = map.remove(key);
        if (chunkcache != null) cache.remove(chunkcache);
    }

    /** Converts the chunk coordinate pair to a long */
    public static long asLong(int x, int z)
    {
        return (long) x & 4294967295L | ((long) z & 4294967295L) << 32;
    }
}
