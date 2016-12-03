package thut.api.entity.genetics;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class GeneRegistry
{
    static Map<ResourceLocation, Class<? extends Gene>> geneMap = Maps.newHashMap();

    public static void register(Class<? extends Gene> gene)
    {
        Gene temp;
        try
        {
            temp = gene.newInstance();
            geneMap.put(temp.getKey(), gene);
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    public static NBTTagCompound save(Gene gene)
    {
        NBTTagCompound tag = gene.save();
        tag.setString("K", gene.getKey().toString());
        return tag;
    }

    public static Gene load(NBTTagCompound tag) throws Exception
    {
        Gene ret = null;
        ResourceLocation resource = new ResourceLocation(tag.getString("K"));
        ret = geneMap.get(resource).newInstance();
        ret.load(tag);
        return ret;
    }

}
