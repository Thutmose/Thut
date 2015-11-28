package dorfgen.conversion;

import java.awt.Color;
import java.util.HashMap;

public enum SiteTerrain
{
	BUILDINGS(255,255,255),
	WALLS(128,128,128),
	FARMLIMEGREEN(64,255,0),
	FARMGREEN(0,255,0),
	FARMYELLOW(255,192,0),
	FARMORANGELIGHT(255,160,0),
	FARMORANGE(255,128,0),
	DARKGREEN(0,128,0),
	ELFGREEN(0,160,0)
	;
	
    private static HashMap<Integer, SiteTerrain> colourMap = new HashMap<Integer, SiteTerrain>();
	public static boolean init = false;
	final Color colour;
	SiteTerrain(int red, int green, int blue)
	{
		colour = new Color(red, green, blue);
	}
	
	public boolean matches(int rgb)
	{
		return colour.getRGB() == rgb;
	}
	
	public static SiteTerrain getMatch(int rgb)
	{
		if(!init)
		{
			init = true;
			colourMap.clear();
			for(SiteTerrain t: values())
			{
				colourMap.put(t.colour.getRGB(), t);
			}
		}
		return colourMap.get(rgb);
	}
}
