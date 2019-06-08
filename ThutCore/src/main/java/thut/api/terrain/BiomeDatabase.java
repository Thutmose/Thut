package thut.api.terrain;

import java.util.HashMap;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.structure.VillagePieces.Village;
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

    public static String getReadableNameFromType(int type)
    {
        return BiomeType.getType(type).readableName;
    }

    private static final int INDEX = 17;

    public static String getBiomeName(Biome biome)
    {
        return ReflectionHelper.getPrivateValue(Biome.class, biome, INDEX);
    }

}