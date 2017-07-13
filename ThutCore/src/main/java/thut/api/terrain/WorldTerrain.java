package thut.api.terrain;

import java.io.DataInputStream;
import java.io.File;
import java.util.HashMap;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import thut.core.common.ThutCore;

public class WorldTerrain
{

    public static class TerrainMap
    {
        final HashMap<BlockPos, TerrainSegment> terrain = new HashMap<BlockPos, TerrainSegment>();

        public TerrainMap()
        {
        }

        public TerrainSegment getSegment(BlockPos pos)
        {
            return terrain.get(pos);
        }

        public void setSegment(TerrainSegment t, BlockPos pos)
        {
            if (t != null) terrain.put(pos, t);
            else terrain.remove(pos);
        }
    }

    World                world;
    public final int     dimID;

    private TerrainMap   terrainMap = new TerrainMap();
    protected TerrainMap spawnMap   = new TerrainMap();

    public WorldTerrain(int dimID)
    {
        this.dimID = dimID;
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) this.world = server.worldServerForDimension(dimID);
        else this.world = ThutCore.proxy.getWorld();
    }

    public void addTerrain(TerrainSegment terrain)
    {
        if (terrain != null)
        {
            if (world.isSpawnChunk(terrain.chunkX, terrain.chunkZ)) spawnMap.setSegment(terrain, terrain.pos);
            else terrainMap.setSegment(terrain, terrain.pos);
        }
    }

    public TerrainSegment getTerrain(int chunkX, int chunkY, int chunkZ)
    {
        PooledMutableBlockPos pos = PooledMutableBlockPos.retain(chunkX, chunkY, chunkZ);
        TerrainSegment segment = getTerrain(pos, false);
        pos.release();
        return segment;
    }

    public TerrainSegment getTerrain(BlockPos pos)
    {
        return getTerrain(pos, false);
    }

    public TerrainSegment getTerrain(BlockPos pos, boolean saving)
    {
        TerrainSegment ret = null;
        boolean spawn = world.isSpawnChunk(pos.getX(), pos.getZ());
        if (spawn) ret = spawnMap.getSegment(pos);
        else ret = terrainMap.getSegment(pos);
        if (ret == null && !saving)
        {
            // TODO here we need to check the appropriate mca file for data, and
            // if it is there, use that.
            load:
            if (world != null && !world.isRemote && world.provider != null && !TerrainSegment.noLoad)
            {
                ISaveHandler saveHandler = world.getSaveHandler();
                File file = saveHandler.getWorldDirectory();
                if (file == null || !file.exists()) break load;
                if (world.provider.getSaveFolder() != null) file = new File(file, world.provider.getSaveFolder());
                if (file.exists())
                {
                    try
                    {
                        DataFixer dataFixer = ReflectionHelper.getPrivateValue(SaveHandler.class,
                                (SaveHandler) saveHandler, "field_186341_a", "a", "dataFixer");
                        DataInputStream datainputstream = RegionFileCache.getChunkInputStream(file, pos.getX(),
                                pos.getZ());

                        if (datainputstream != null)
                        {

                            NBTTagCompound tag = dataFixer.process(FixTypes.CHUNK,
                                    CompressedStreamTools.read(datainputstream));
                            try
                            {
                                NBTTagCompound nbt = tag.getCompoundTag(TerrainManager.TERRAIN);
                                tag = nbt.getCompoundTag(
                                        "terrain" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "," + dimID);
                                if (tag != null && !tag.hasNoTags())
                                {
                                    ret = new TerrainSegment(pos.getX(), pos.getY(), pos.getZ());
                                    TerrainSegment.readFromNBT(ret, tag);
                                }
                            }
                            catch (Exception e)
                            {

                            }
                            datainputstream.close();
                        }

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            if (ret == null)
            {
                ret = new TerrainSegment(pos.getX(), pos.getY(), pos.getZ());
                if (spawn) spawnMap.setSegment(ret, pos.toImmutable());
                else terrainMap.setSegment(ret, pos.toImmutable());
            }
        }
        return ret;
    }

    public void loadTerrain(NBTTagCompound nbt)
    {
        loadTerrain(nbt, 16);
    }

    public void loadTerrain(NBTTagCompound nbt, int max)
    {
        int x = nbt.getInteger("xCoord");
        int z = nbt.getInteger("zCoord");
        max = Math.min(16, max + 1);
        PooledMutableBlockPos pos = PooledMutableBlockPos.retain();
        for (int i = 0; i < max; i++)
        {
            NBTTagCompound terrainTag = null;
            pos.setPos(x, i, z);
            try
            {
                terrainTag = nbt.getCompoundTag("terrain" + x + "," + i + "," + z + "," + dimID);
            }
            catch (Exception e)
            {

            }
            TerrainSegment t = null;
            if (terrainTag != null && !terrainTag.hasNoTags() && !TerrainSegment.noLoad)
            {
                t = new TerrainSegment(x, i, z);
                TerrainSegment.readFromNBT(t, terrainTag);
                addTerrain(t);
            }
            if (t == null)
            {
                t = getTerrain(pos, true);
                if (t == null)
                {
                    t = new TerrainSegment(x, i, z);
                    addTerrain(t);
                }
            }
        }
        pos.release();
    }

    public void removeTerrain(int chunkX, int chunkZ)
    {
        for (int i = 0; i < 16; i++)
        {
            terrainMap.setSegment(null, new BlockPos(chunkX, i, chunkZ));
        }
    }

    public boolean saveTerrain(NBTTagCompound nbt, int x, int z)
    {
        nbt.setInteger("xCoord", x);
        nbt.setInteger("zCoord", z);
        boolean saved = false;
        for (int i = 0; i < 16; i++)
        {
            TerrainSegment t = getTerrain(new BlockPos(x, i, z), true);
            if (t == null) continue;
            t.checkToSave();
            if (!t.toSave)
            {
                continue;
            }
            saved = true;
            NBTTagCompound terrainTag = new NBTTagCompound();
            t.saveToNBT(terrainTag);
            nbt.setTag("terrain" + x + "," + i + "," + z + "," + dimID, terrainTag);
        }
        return saved;
    }
}
