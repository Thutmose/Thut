package thut.api.terrain;

import java.io.DataInputStream;
import java.io.File;
import java.util.HashMap;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class WorldTerrain
{

    public static class TerrainMap
    {
        final HashMap<Long, TerrainSegment> terrain = new HashMap<Long, TerrainSegment>();

        public TerrainMap()
        {
        }

        private Long getKey(int x, int y, int z)
        {
            long l = x + (((long) z) << 21) + (((long) y) << 42);
            return new Long(l);
        }

        public TerrainSegment getSegment(int x, int y, int z)
        {
            y = Math.max(0, y);
            y = Math.min(y, 15);

            Long key = getKey(x, y, z);
            return terrain.get(key);
        }

        public void setSegment(TerrainSegment t, int x, int y, int z)
        {
            y = Math.max(0, y);
            y = Math.min(y, 15);

            Long key = getKey(x, y, z);
            if (t != null) terrain.put(key, t);
            else terrain.remove(key);
        }
    }

    final World        world;
    public final int   dimID;

    private TerrainMap terrainMap = new TerrainMap();

    public WorldTerrain(int dimID)
    {
        this.dimID = dimID;
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null) this.world = server.worldServerForDimension(dimID);
        else this.world = null;
    }

    public void addTerrain(TerrainSegment terrain)
    {
        if (terrain != null)
        {
            terrainMap.setSegment(terrain, terrain.chunkX, terrain.chunkY, terrain.chunkZ);
        }
    }

    public TerrainSegment getTerrain(int chunkX, int chunkY, int chunkZ)
    {
        return getTerrain(chunkX, chunkY, chunkZ, false);
    }

    public TerrainSegment getTerrain(int chunkX, int chunkY, int chunkZ, boolean saving)
    {
        TerrainSegment ret = null;

        ret = terrainMap.getSegment(chunkX, chunkY, chunkZ);
        if (ret == null && !saving)
        {
            // TODO here we need to check the appropriate mca file for data, and
            // if it is there, use that.
            load:
            if (world != null && world.provider != null && !TerrainSegment.noLoad)
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
                        DataInputStream datainputstream = RegionFileCache.getChunkInputStream(file, chunkX, chunkZ);

                        if (datainputstream != null)
                        {

                            NBTTagCompound tag = dataFixer.process(FixTypes.CHUNK,
                                    CompressedStreamTools.read(datainputstream));
                            try
                            {
                                NBTTagCompound nbt = tag.getCompoundTag(TerrainManager.TERRAIN);
                                tag = nbt
                                        .getCompoundTag("terrain" + chunkX + "," + chunkY + "," + chunkZ + "," + dimID);
                                if (tag != null && !tag.hasNoTags())
                                {
                                    ret = new TerrainSegment(chunkX, chunkY, chunkZ);
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
                ret = new TerrainSegment(chunkX, chunkY, chunkZ);
            }
            terrainMap.setSegment(ret, chunkX, chunkY, chunkZ);
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
        for (int i = 0; i < max; i++)
        {
            NBTTagCompound terrainTag = null;
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
                getTerrain(x, i, z);
            }
        }
    }

    public void removeTerrain(int chunkX, int chunkZ)
    {
        for (int i = 0; i < 16; i++)
        {
            terrainMap.setSegment(null, chunkX, i, chunkZ);
        }
    }

    public boolean saveTerrain(NBTTagCompound nbt, int x, int z)
    {
        nbt.setInteger("xCoord", x);
        nbt.setInteger("zCoord", z);
        boolean saved = false;
        for (int i = 0; i < 16; i++)
        {
            TerrainSegment t = getTerrain(x, i, z, true);
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
