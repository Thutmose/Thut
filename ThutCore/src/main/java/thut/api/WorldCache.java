package thut.api;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

/** This class is used for thread-safe world access.
 * 
 * @author Thutmose */
public class WorldCache implements IBlockAccess
{
    public final World             world;
    ConcurrentHashMap<Long, Chunk> chunks = new ConcurrentHashMap<Long, Chunk>();

    public WorldCache(World world_)
    {
        world = world_;
    }

    synchronized void addChunk(Chunk chunk)
    {
        long key = ChunkCoordIntPair.chunkXZ2Int(chunk.xPosition, chunk.zPosition);
        chunks.put(key, chunk);
    }

    synchronized void removeChunk(Chunk chunk)
    {
        long key = ChunkCoordIntPair.chunkXZ2Int(chunk.xPosition, chunk.zPosition);
        chunks.remove(key);
    }

    synchronized public Chunk getChunk(int chunkX, int chunkZ)
    {
        long key = ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ);
        return chunks.get(key);
    }

    @Override
    synchronized public boolean extendedLevelsInChunkCache()
    {
        return false;
    }

    @Override
    synchronized public TileEntity getTileEntity(BlockPos pos)
    {
        int l = (pos.getX() >> 4);
        int i1 = (pos.getZ() >> 4);
        long key = ChunkCoordIntPair.chunkXZ2Int(l, i1);
        Chunk chunk = chunks.get(key);
        if (chunk == null) return null;
        return chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
    }

    @Override
    synchronized public int getCombinedLight(BlockPos pos, int p_175626_2_)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    synchronized public IBlockState getBlockState(BlockPos pos)
    {
        int l = (pos.getX() >> 4);
        int i1 = (pos.getZ() >> 4);
        long key = ChunkCoordIntPair.chunkXZ2Int(l, i1);
        Chunk chunk = chunks.get(key);
        if (chunk == null) return null;
        return chunk.getBlockState(pos);
    }

    @Override
    synchronized public boolean isAirBlock(BlockPos pos)
    {
        return getBlockState(pos) == null || getBlockState(pos).getBlock().isAir(this, pos);
    }

    @Override
    synchronized public BiomeGenBase getBiomeGenForCoords(BlockPos pos)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    synchronized public int getStrongPower(BlockPos pos, EnumFacing direction)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    synchronized public WorldType getWorldType()
    {
        return world.getWorldInfo().getTerrainType();
    }

    @Override
    synchronized public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default)
    {
        int l = (pos.getX() >> 4);
        int i1 = (pos.getZ() >> 4);
        long key = ChunkCoordIntPair.chunkXZ2Int(l, i1);
        Chunk chunk = chunks.get(key);
        if (chunk == null || chunk.isEmpty()) return _default;
        return getBlockState(pos).getBlock().isSideSolid(this, pos, side);
    }

}
