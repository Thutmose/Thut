package dorfgen.conversion;

import java.awt.Color;

import net.minecraft.world.biome.Biome;

public class BiomeConversion     
{
    Color colorKey;
    public Biome mineCraftBiome;
    public BiomeConversion(Color color, Biome biome)
    {
        colorKey = color;
        mineCraftBiome = biome;
    }
    
    public boolean matches(int rgb)
    {
        return colorKey.getRGB() == rgb;
    }
}
