package dorfgen.conversion;

import java.awt.Color;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

public class BiomeConversion
{
    public static int getBiomeID(final Biome biome)
    {
        final ForgeRegistry<Biome> reg = (ForgeRegistry<Biome>) ForgeRegistries.BIOMES;
        return reg.getID(biome);
    }

    public static Biome getBiome(final int id)
    {
        final ForgeRegistry<Biome> reg = (ForgeRegistry<Biome>) ForgeRegistries.BIOMES;
        final Biome b = reg.getValue(id);
        if (b == null) return Biomes.DEFAULT;
        return b;
    }

    Color      colorKey;
    public int mineCraftBiome;

    public BiomeConversion(final Color color, final int biome)
    {
        this.colorKey = color;
        this.mineCraftBiome = biome;
    }

    public boolean matches(final int rgb)
    {
        return this.colorKey.getRGB() == rgb;
    }
}
