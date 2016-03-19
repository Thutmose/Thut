package dorfgen.conversion;

import java.awt.Color;

public class BiomeConversion     
{
    Color colorKey;
    public int mineCraftBiome;
    public BiomeConversion(Color color, int biome)
    {
        colorKey = color;
        mineCraftBiome = biome;
    }
    
    public boolean matches(int rgb)
    {
        return colorKey.getRGB() == rgb;
    }
}
