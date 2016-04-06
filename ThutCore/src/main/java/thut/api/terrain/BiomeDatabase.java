package thut.api.terrain;

import java.util.HashMap;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.registry.FMLControlledNamespacedRegistry;
import net.minecraftforge.fml.common.registry.PersistentRegistryManager;
import thut.api.maths.Vector3;

public class BiomeDatabase
{
    public static HashMap<BiomeGenBase, Type[]>                    biomeTypes        = new HashMap<BiomeGenBase, Type[]>();

    public static final FMLControlledNamespacedRegistry<BiomeType> biomeTypeRegistry = PersistentRegistryManager
            .createRegistry(new ResourceLocation("thutcore:biometypes"), BiomeType.class, null, 256, 1024, true, null, null, null);

    public static boolean contains(BiomeGenBase b, Type type)
    {
        boolean ret = false;
        if (b == null) return ret;

        Type[] arr = biomeTypes.get(b);
        if (arr == null)
        {
            arr = BiomeDictionary.getTypesForBiome(b);
            biomeTypes.put(b, arr);
        }

        for (Type t : arr)
        {
            ret = ret || t.equals(type);
            if (ret) break;
        }
        return ret;
    }

    public static BiomeType getBiome(BiomeGenBase b)
    {
        if (b != null)
        {
            if (b.getBiomeName().toLowerCase().contains("flower")) return BiomeType.FLOWER;
        }
        return BiomeType.NONE;
    }

    public static String getBiome(World world, Vector3 v)
    {
        int type = v.getBiomeID(world);
        return getNameFromType(type);
    }

    public static String getBiome(World world, Vector3 v, boolean checkIndandVillage)
    {
        String ret = "";

        if (checkIndandVillage)
        {
            Village village = world.villageCollectionObj
                    .getNearestVillage(new BlockPos(MathHelper.floor_double(v.intX()),
                            MathHelper.floor_double(v.intY()), MathHelper.floor_double(v.intZ())), 2);
            if (village != null) return "village";
        }
        BiomeGenBase biome = v.getBiome(world);
        ret = getBiome(biome).name;

        return ret;
    }

    public static int getBiomeType(BiomeGenBase biome)
    {
        return BiomeGenBase.getIdForBiome(biome);
    }

    public static BiomeGenBase getBiome(String name)
    {
        return BiomeGenBase.biomeRegistry.getObject(new ResourceLocation(name));
    }

    public static int getBiomeType(String name)
    {
        for (BiomeType b : BiomeType.values())
        {
            if (b.name.equalsIgnoreCase(name)) return (byte) b.getType();
        }
        for (ResourceLocation key : BiomeGenBase.biomeRegistry.getKeys())
        {
            BiomeGenBase b = BiomeGenBase.biomeRegistry.getObject(key);
            if (b != null) if (b.getBiomeName().equalsIgnoreCase(name)) return getBiomeType(b);
        }
        return BiomeType.NONE.getType();
    }

    public static String getNameFromType(int type)
    {
        if (type > 255) return biomeTypeRegistry.getObjectById(type).name;
        else if (BiomeGenBase.getBiome(type) != null) return BiomeGenBase.getBiome(type).getBiomeName();
        else return "none";
    }

    public static String getReadableNameFromType(int type)
    {
        if (type > 255) return biomeTypeRegistry.getObjectById(type).readableName;
        else if (BiomeGenBase.getBiome(type) != null) return BiomeGenBase.getBiome(type).getBiomeName();
        else return "None " + type;
    }

}