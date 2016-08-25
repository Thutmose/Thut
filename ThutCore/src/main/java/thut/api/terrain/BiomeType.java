package thut.api.terrain;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import net.minecraftforge.fml.common.registry.PersistentRegistryManager;
import net.minecraftforge.fml.common.registry.RegistryDelegate;

public class BiomeType extends IForgeRegistryEntry.Impl<BiomeType>
{
    private static final ArrayList<BiomeType> values = Lists.newArrayList();
    public static final BiomeType             NONE   = new BiomeType("none", "none"), SKY = new BiomeType("sky", "Sky"),
            FLOWER = new BiomeType("flower", "Flowers"), LAKE = new BiomeType("lake", "Lake"),
            INDUSTRIAL = new BiomeType("industrial", "Industrial Area"),
            METEOR = new BiomeType("meteor", "Meteor Area"), RUIN = new BiomeType("ruin", "Ruins"),
            CAVE = new BiomeType("cave", "Cave"), CAVE_WATER = new BiomeType("cavewater", "Cave Lake"),
            VILLAGE = new BiomeType("village", "Village"), ALL = new BiomeType("all", "All");

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
        return values;
    }

    public final String                      name;
    private final ResourceLocation           key;

    public final String                      readableName;

    public final RegistryDelegate<BiomeType> delegate = PersistentRegistryManager.makeDelegate(this, BiomeType.class);

    @SuppressWarnings("deprecation")
    private BiomeType(String name, String readableName)
    {
        this.name = name;
        this.readableName = readableName;
        key = new ResourceLocation("thutcore", name);
        values.add(this);
        BiomeDatabase.biomeTypeRegistry.register(-1, key, this);
    }

    public int getType()
    {
        return BiomeDatabase.biomeTypeRegistry.getId(this);
    }
}
