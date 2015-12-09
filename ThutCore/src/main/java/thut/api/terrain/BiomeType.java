package thut.api.terrain;

import java.util.Arrays;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.PersistentRegistryManager;
import net.minecraftforge.fml.common.registry.RegistryDelegate;

public enum BiomeType
{
    NONE("none", "none"), SKY("sky", "Sky"),

    FLOWER("flower", "Flowers"), LAKE("lake", "Lake"), INDUSTRIAL("industrial", "Industrial Area"), METEOR("meteor",
            "Meteor Area"), RUIN("ruin", "Ruins"),

    CAVE("cave", "Cave"), CAVE_WATER("cavewater", "Cave Lake"), VILLAGE("village", "Village"),

    ALL("all", "All");

    public final String name;
    private final ResourceLocation key;
    public final String readableName;
    public final RegistryDelegate<BiomeType> delegate = PersistentRegistryManager.makeDelegate(this, BiomeType.class);

    public int getType()
    {
        return BiomeDatabase.biomeTypeRegistry.getId(this);
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
            BiomeType ret = EnumHelper.addEnum(BiomeType.class, name.toUpperCase(),
                    new Class[] { String.class, String.class }, new Object[] { name.toLowerCase(), name });
            System.out.println(ret + " " + ret.name + " " + ret.readableName + " " + Arrays.toString(values()) + " "
                    + ret.getType());
            return ret;
        }
        return NONE;
    }

    @SuppressWarnings("deprecation")
    private BiomeType(String name, String readableName)
    {
        this.name = name;
        this.readableName = readableName;
        key = new ResourceLocation("thutcore", name);
        BiomeDatabase.biomeTypeRegistry.register(-1, key, this);
    }
}
