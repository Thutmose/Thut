package thut.api.terrain;

import net.minecraft.nbt.NBTTagCompound;

@Deprecated
public class WorldTerrain
{
    final int dimID;

    public WorldTerrain(int dimID)
    {
        this.dimID = dimID;
    }

    public TerrainSegment[] loadTerrain(NBTTagCompound nbt)
    {
        int x = nbt.getInteger("xCoord");
        int z = nbt.getInteger("zCoord");
        TerrainSegment[] ret = new TerrainSegment[16];
        for (int i = 0; i < 16; i++)
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
            }
            if (t == null)
            {
                t = new TerrainSegment(x, i, z);
            }
            ret[i] = t;
        }
        return ret;
    }
}
