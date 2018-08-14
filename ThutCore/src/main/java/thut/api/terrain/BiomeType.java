package thut.api.terrain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BiomeType
{
    private static final Map<Integer, BiomeType> typeMap       = Maps.newHashMap();
    private static final Map<Integer, BiomeType> typeMapClient = Maps.newHashMap();
    private static int                           MAXID         = 256;
    public static final BiomeType                NONE          = new BiomeType("none", "None"),
            SKY = new BiomeType("sky", "Sky"), FLOWER = new BiomeType("flower", "Flowers"),
            LAKE = new BiomeType("lake", "Lake"), INDUSTRIAL = new BiomeType("industrial", "Industrial Area"),
            METEOR = new BiomeType("meteor", "Meteor Area"), RUIN = new BiomeType("ruin", "Ruins"),
            CAVE = new BiomeType("cave", "Cave"), CAVE_WATER = new BiomeType("cavewater", "Cave Lake"),
            VILLAGE = new BiomeType("village", "Village"), ALL = new BiomeType("all", "All");

    @SideOnly(Side.CLIENT)
    public static void setMap(Map<Integer, String> mapIn)
    {
        typeMapClient.clear();
        for (Integer i : mapIn.keySet())
        {
            String name = mapIn.get(i);
            BiomeType type = getBiome(name, true);
            typeMapClient.put(i, type);
        }
    }

    public static Map<Integer, String> getMap()
    {
        Map<Integer, String> map = Maps.newHashMap();
        for (BiomeType type : values())
        {
            map.put(type.getType(), type.name);
        }
        return map;
    }

    public static BiomeType getBiome(String name)
    {
        return getBiome(name, true);
    }

    public static BiomeType getBiome(String name, boolean generate)
    {
        for (BiomeType b : values())
        {
            if (b.name.equalsIgnoreCase(name) || b.readableName.equalsIgnoreCase(name)) return b;
        }
        if (generate)
        {
            BiomeType ret = new BiomeType(name.toLowerCase(java.util.Locale.ENGLISH), name);
            return ret;
        }
        return NONE;
    }

    public static ArrayList<BiomeType> values()
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            ArrayList<BiomeType> types = Lists.newArrayList();
            Collection<BiomeType> values = typeMapClient.values();
            synchronized (values)
            {
                types.addAll(values);
            }
            return types;
        }
        ArrayList<BiomeType> types = Lists.newArrayList();
        Collection<BiomeType> values = typeMap.values();
        synchronized (values)
        {
            types.addAll(values);
        }
        return types;
    }

    public static BiomeType getType(int id)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            return typeMapClient.containsKey(id) ? typeMapClient.get(id) : NONE;
        return typeMap.containsKey(id) ? typeMap.get(id) : NONE;
    }

    public final String name;
    private int         id;
    public final String readableName;

    private BiomeType(String name, String readableName)
    {
        this.name = name;
        this.readableName = readableName;
        id = -1;
        for (BiomeType type : typeMap.values())
        {
            if (type.name.equals(name))
            {
                id = type.id;
            }
        }
        if (id == -1) id = MAXID++;
        typeMap.put(id, this);
        typeMapClient.put(id, this);
    }

    public int getType()
    {
        return id;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof BiomeType) { return ((BiomeType) o).id == id; }
        return false;
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        return id;
    }
}
