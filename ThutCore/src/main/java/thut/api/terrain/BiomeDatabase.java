package thut.api.terrain;

import java.util.HashMap;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import thut.api.maths.Vector3;
import thut.lib.CompatWrapper;

public class BiomeDatabase
{
    public static HashMap<Biome, Type[]> biomeTypes = new HashMap<Biome, Type[]>();

    public static boolean contains(Biome b, Type type)
    {
        return CompatWrapper.isOfType(b, type);
    }

    public static BiomeType getBiome(Biome b)
    {
        if (b != null)
        {
            if (getBiomeName(b).toLowerCase(java.util.Locale.ENGLISH).contains("flower")) return BiomeType.FLOWER;
        }
        return BiomeType.NONE;
    }

    public static Biome getBiome(String name)
    {
        return Biome.REGISTRY.getObject(new ResourceLocation(name));
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
            Village village = world.villageCollection.getNearestVillage(
                    new BlockPos(MathHelper.floor(v.intX()), MathHelper.floor(v.intY()), MathHelper.floor(v.intZ())),
                    2);
            if (village != null) return "village";
        }
        Biome biome = v.getBiome(world);
        ret = getBiome(biome).name;

        return ret;
    }

    public static int getBiomeType(Biome biome)
    {
        return Biome.getIdForBiome(biome);
    }

    public static int getBiomeType(String name)
    {
        for (BiomeType b : BiomeType.values())
        {
            if (b.name.equalsIgnoreCase(name)) return (byte) b.getType();
        }
        for (ResourceLocation key : Biome.REGISTRY.getKeys())
        {
            Biome b = Biome.REGISTRY.getObject(key);
            if (b != null) if (getBiomeName(b).equalsIgnoreCase(name)) return getBiomeType(b);
        }
        return BiomeType.NONE.getType();
    }

    public static String getNameFromType(int type)
    {
        if (type > 255) return BiomeType.getType(type).name;
        else if (Biome.getBiome(type) != null) return getBiomeName(Biome.getBiome(type));
        else return "none";
    }

    public static String getReadableNameFromType(int type)
    {
        if (type > 255) return BiomeType.getType(type).readableName;
        else if (Biome.getBiome(type) != null) return getBiomeName(Biome.getBiome(type));
        else return "None " + type;
    }

    private static final int INDEX = 17;

    public static String getBiomeName(Biome biome)
    {
        return ReflectionHelper.getPrivateValue(Biome.class, biome, INDEX);
    }

}