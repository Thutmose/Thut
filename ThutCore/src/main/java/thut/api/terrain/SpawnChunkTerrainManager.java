package thut.api.terrain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

import com.google.common.collect.Maps;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import thut.api.terrain.WorldTerrain.TerrainMap;
import thut.core.common.handlers.PlayerDataHandler;

/** This class manages saving/loading spawn chunks, those are saved as
 * duplicate, in seperate location, as it seems that they randomly get their
 * data deleted at load time. */
public class SpawnChunkTerrainManager
{
    private static HashMap<Integer, Long> lastSaves = Maps.newHashMap();

    public static void clear()
    {
        lastSaves.clear();
    }

    public static void load(int dim, WorldTerrain world)
    {
        try
        {
            File file = PlayerDataHandler.getFileForUUID("Terrain", "dim-" + dim);
            if (file != null && file.exists())
            {
                FileInputStream fileinputstream = new FileInputStream(file);
                NBTTagCompound data = CompressedStreamTools.readCompressed(fileinputstream);
                NBTTagList list = (NBTTagList) data.getTag("Terrain");
                TerrainMap terrain = world.terrainMap;
                PooledMutableBlockPos pos = PooledMutableBlockPos.retain(0, 0, 0);
                for (int i = 0; i < list.tagCount(); i++)
                {
                    NBTTagCompound tag = list.getCompoundTagAt(i);
                    if (!tag.hasKey("x")) continue;
                    int x = tag.getInteger("x");
                    int y = tag.getInteger("y");
                    int z = tag.getInteger("z");
                    TerrainSegment segement = terrain.getSegment(pos.setPos(x, y, z));
                    if (segement == null)
                    {
                        segement = new TerrainSegment(x, y, z);
                        terrain.setSegment(segement, pos);
                    }
                    TerrainSegment.readFromNBT(segement, tag);
                    world.addTerrain(segement);
                }
                pos.release();
                fileinputstream.close();
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
