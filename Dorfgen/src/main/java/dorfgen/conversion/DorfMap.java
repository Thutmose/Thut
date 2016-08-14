package dorfgen.conversion;

import static dorfgen.WorldGenerator.scale;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import dorfgen.WorldGenerator;
import dorfgen.conversion.Interpolator.BicubicInterpolator;
import dorfgen.conversion.Interpolator.CachedBicubicInterpolator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

public class DorfMap
{

    public Biome[][]                                           biomeMap             = new Biome[0][0];
    public int[][]                                             elevationMap         = new int[0][0];
    public int[][]                                             waterMap             = new int[0][0];
    public int[][]                                             riverMap             = new int[0][0];
    public int[][]                                             evilMap              = new int[0][0];
    public int[][]                                             rainMap              = new int[0][0];
    public int[][]                                             drainageMap          = new int[0][0];
    public int[][]                                             temperatureMap       = new int[0][0];
    public int[][]                                             volcanismMap         = new int[0][0];
    public int[][]                                             vegitationMap        = new int[0][0];
    public int[][]                                             structureMap         = new int[0][0];
    /** Coords are embark tile resolution and are x + 8192 * z */
    public static HashMap<Integer, HashSet<Site>>              sitesByCoord         = new HashMap<Integer, HashSet<Site>>();
    public static HashMap<Integer, Site>                       sitesById            = new HashMap<Integer, Site>();
    public static HashMap<Integer, Region>                     regionsById          = new HashMap<Integer, Region>();
    /** Coords are world tile resolution and are x + 2048 * z */
    public static HashMap<Integer, Region>                     regionsByCoord       = new HashMap<Integer, Region>();
    public static HashMap<Integer, Region>                     ugRegionsById        = new HashMap<Integer, Region>();
    /** Coords are world tile resolution and are x + 2048 * z */
    public static HashMap<Integer, Region>                     ugRegionsByCoord     = new HashMap<Integer, Region>();
    public static HashMap<Integer, WorldConstruction>          constructionsById    = new HashMap<Integer, WorldConstruction>();
    /** Coords are world tile resolution and are x + 2048 * z */
    public static HashMap<Integer, HashSet<WorldConstruction>> constructionsByCoord = new HashMap<Integer, HashSet<WorldConstruction>>();
    static int                                                 waterShift           = -35;

    public BicubicInterpolator                                 biomeInterpolator    = new BicubicInterpolator();
    public CachedBicubicInterpolator                           heightInterpolator   = new CachedBicubicInterpolator();
    public CachedBicubicInterpolator                           miscInterpolator     = new CachedBicubicInterpolator();

    static void addSiteByCoord(int x, int z, Site site)
    {
        int coord = x + 8192 * z;
        HashSet<Site> sites = sitesByCoord.get(coord);
        if (sites == null)
        {
            sites = new HashSet<Site>();
            sitesByCoord.put(coord, sites);
        }
        sites.add(site);
    }

    public DorfMap()
    {
    }

    public void init()
    {
        populateBiomeMap();
        populateElevationMap();
        populateWaterMap();
        populateTemperatureMap();
        populateVegitationMap();
        populateDrainageMap();
        populateRainMap();
        populateStructureMap();

        postProcessRegions();
        if (biomeMap.length > 0)
        {
            postProcessBiomeMap();
        }
    }

    public void populateBiomeMap()
    {
        BufferedImage img = WorldGenerator.instance.biomeMap;
        if (img == null) return;
        biomeMap = new Biome[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
                int rgb = WorldGenerator.instance.biomeMap.getRGB(x, y);
                biomeMap[x][y] = BiomeList.GetBiome(rgb);
            }
        }
        WorldGenerator.instance.biomeMap = null;
    }

    public static int elevationSigmoid(int preHeight)
    {
        double a = preHeight;

        return (int) (Math.min(Math.max(((215. / (1 + Math.exp(-(a - 128.) / 20.))) + 45. + a) / 2., 10), 245));
    }

    public void populateElevationMap()
    {
        BufferedImage img = WorldGenerator.instance.elevationMap;
        if (img == null) return;
        int shift = 10;
        elevationMap = new int[img.getWidth()][img.getHeight()];
        Biome hillsPlus = Biome.REGISTRY.getObject(new ResourceLocation("extreme_hills_with_trees"));
        Biome hills = Biome.REGISTRY.getObject(new ResourceLocation("extreme_hills"));
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
                int rgb = WorldGenerator.instance.elevationMap.getRGB(x, y);

                int r = (rgb >> 16) & 0xFF, b = (rgb >> 0) & 0xFF;
                int h = b - shift;
                if (r == 0)
                {
                    h = b + waterShift;
                }
                h = Math.max(10, h);
                elevationMap[x][y] = h;// elevationSigmoid(h);
                if (biomeMap.length > 0) if (h < 145 && biomeMap[x][y] == hillsPlus)
                {
                    biomeMap[x][y] = hills;
                }
            }
        }
        WorldGenerator.instance.elevationMap = null;
    }

    public void populateWaterMap()
    {
        BufferedImage img = WorldGenerator.instance.elevationWaterMap;
        if (img == null) return;
        waterMap = new int[img.getWidth()][img.getHeight()];
        riverMap = new int[img.getWidth()][img.getHeight()];
        Biome riverId = Biome.REGISTRY.getObject(new ResourceLocation("river"));
        Biome oceanId = Biome.REGISTRY.getObject(new ResourceLocation("ocean"));
        Biome deep_oceanId = Biome.REGISTRY.getObject(new ResourceLocation("deep_ocean"));
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
                int rgb = WorldGenerator.instance.elevationWaterMap.getRGB(x, y);

                int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = (rgb >> 0) & 0xFF;
                Biome biome = biomeMap[x][y];

                if (r == 0 && g == 0 && biome != riverId)
                {
                    waterMap[x][y] = b + 25 + waterShift;
                    if (biomeMap.length > 0)
                    {
                        if (waterMap[x][y] < 50)
                        {
                            biomeMap[x][y] = deep_oceanId;
                        }
                        else
                        {
                            biomeMap[x][y] = oceanId;
                        }
                        riverMap[x][y] = -1;
                    }
                }
                else if (r == 0 || biome == riverId)
                {
                    waterMap[x][y] = -1;
                    riverMap[x][y] = biome == riverId ? b - waterShift : b;
                }
                else
                {
                    waterMap[x][y] = -1;
                    riverMap[x][y] = -1;
                }
            }
        }
        joinRivers();
        WorldGenerator.instance.elevationWaterMap = null;
    }

    public void populateTemperatureMap()
    {
        BufferedImage img = WorldGenerator.instance.temperatureMap;
        if (img == null) return;
        temperatureMap = new int[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
                int rgb = img.getRGB(x, y);
                temperatureMap[x][y] = rgb & 255;
            }
        }
        WorldGenerator.instance.temperatureMap = null;
    }

    public void populateVegitationMap()
    {
        BufferedImage img = WorldGenerator.instance.vegitationMap;
        if (img == null) return;
        vegitationMap = new int[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
                int rgb = img.getRGB(x, y);
                vegitationMap[x][y] = rgb & 255;
            }
        }
        WorldGenerator.instance.vegitationMap = null;
    }

    public void populateDrainageMap()
    {
        BufferedImage img = WorldGenerator.instance.drainageMap;
        if (img == null) return;
        drainageMap = new int[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
                int rgb = img.getRGB(x, y);
                drainageMap[x][y] = rgb & 255;
            }
        }
        WorldGenerator.instance.drainageMap = null;
    }

    public void populateRainMap()
    {
        BufferedImage img = WorldGenerator.instance.rainMap;
        if (img == null) return;
        rainMap = new int[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
                int rgb = img.getRGB(x, y);
                rainMap[x][y] = rgb & 255;
            }
        }
        WorldGenerator.instance.rainMap = null;
    }

    public void populateStructureMap()
    {
        BufferedImage img = WorldGenerator.instance.structuresMap;
        if (img == null) return;
        structureMap = new int[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
                int rgb = img.getRGB(x, y);
                structureMap[x][y] = rgb;
            }
        }
        WorldGenerator.instance.structuresMap = null;
    }

    private void joinRivers()
    {
        for (int y = 0; y < riverMap[0].length; y++)
        {
            for (int x = 0; x < riverMap.length; x++)
            {
                int r = riverMap[x][y];
                if (r > 0)
                {
                    int num = countLarger(0, waterMap, x, y, 1);
                    int num2 = countLarger(0, waterMap, x, y, 2);
                    if (num == 0 && num2 > 0)
                    {
                        int[] dir = getDirToWater(x, y);
                        riverMap[x + dir[0]][y + dir[1]] = r;
                        riverMap[x + 2 * dir[0]][y + 2 * dir[1]] = r;

                    }
                }
            }
        }
    }

    private int[] getDirToWater(int x, int y)
    {
        int[] ret = new int[2];
        if (waterMap[x + 2][y] > 0) ret[0] = 1;
        else if (waterMap[x - 2][y] > 0) ret[0] = -1;
        else if (waterMap[x][y + 2] > 0) ret[1] = 1;
        else if (waterMap[x][y - 2] > 0) ret[1] = -1;

        return ret;
    }

    public int countNear(int toCheck, int[][] image, int pixelX, int pixelY, int distance)
    {
        int ret = 0;
        for (int i = -distance; i <= distance; i++)
        {
            for (int j = -distance; j <= distance; j++)
            {
                if (i == 0 && j == 0) continue;
                int x = pixelX + i, y = pixelY + j;
                if (x >= 0 && x < image.length && y >= 0 && y < image[0].length)
                {
                    if (image[x][y] == toCheck)
                    {
                        ret++;
                    }
                }
            }
        }
        return ret;
    }

    public int countLarger(int toCheck, int[][] image, int pixelX, int pixelY, int distance)
    {
        int ret = 0;
        for (int i = -distance; i <= distance; i++)
        {
            for (int j = -distance; j <= distance; j++)
            {
                if (i == 0 && j == 0) continue;
                int x = pixelX + i, y = pixelY + j;
                if (x >= 0 && x < image.length && y >= 0 && y < image[0].length)
                {
                    if (image[x][y] > toCheck)
                    {
                        ret++;
                    }
                }
            }
        }
        return ret;
    }

    public void postProcessBiomeMap()
    {
        boolean hasThermalMap = temperatureMap.length > 0;
        Biome riverId = Biome.REGISTRY.getObject(new ResourceLocation("river"));
        for (int x = 0; x < biomeMap.length; x++)
            for (int z = 0; z < biomeMap[0].length; z++)
            {
                Biome biome = biomeMap[x][z];
                int temperature = hasThermalMap ? temperatureMap[x][z] : 128;
                int drainage = drainageMap.length > 0 ? drainageMap[x][z] : 100;
                int rain = rainMap.length > 0 ? rainMap[x][z] : 100;
                int evil = evilMap.length > 0 ? evilMap[x][z] : 100;
                Region region = getRegionForCoords(x * scale, z * scale);
                if (biome != riverId)
                {
                    Biome newBiome = BiomeList.getBiomeFromValues(biome, temperature, drainage, rain, evil, region);
                    biomeMap[x][z] = newBiome;
                }
            }
    }

    public Region getRegionForCoords(int x, int z)
    {
        x = x / (scale * 16);
        z = z / (scale * 16);
        int key = x + 2048 * z;
        return regionsByCoord.get(key);
    }

    public Region getUgRegionForCoords(int x, int depth, int z)
    {
        x = x / (scale * 16);
        z = z / (scale * 16);
        int key = x + 2048 * z + depth * 4194304;
        return ugRegionsByCoord.get(key);
    }

    public HashSet<Site> getSiteForCoords(int x, int z)
    {
        int kx = x / (scale);
        int kz = z / (scale);
        int key = kx + 8192 * kz;

        HashSet<Site> ret = sitesByCoord.get(key);

        if (ret != null)
        {
            for (Site s : ret)
            {
                if (s.isInSite(x, z)) return ret;
            }
        }

        return null;
    }

    public HashSet<WorldConstruction> getConstructionsForCoords(int x, int z)
    {
        x = x / (scale * 16);
        z = z / (scale * 16);
        int key = x + 2048 * z;
        return constructionsByCoord.get(key);
    }

    public void postProcessRegions()
    {
        for (Region region : regionsById.values())
        {
            for (int i : region.coords)
            {
                if (!regionsByCoord.containsKey(i))
                {
                    regionsByCoord.put(i, region);
                }
                else
                {
                    System.err.println("Existing region for " + (i & 2047) + " " + (i / 2048));
                }
            }
        }
        for (Region region : ugRegionsById.values())
        {
            for (int i : region.coords)
            {
                if (!ugRegionsByCoord.containsKey(i))
                {
                    ugRegionsByCoord.put(i, region);
                }
                else
                {
                    System.err.println("Existing region for " + (i & 2047) + " " + (i / 2048));
                }
            }
        }
    }

    public static enum SiteType
    {
        CAVE("cave"), FORTRESS("fortress"), TOWN("town"), HIPPYHUTS("forest retreat"), DARKFORTRESS(
                "dark fortress"), HAMLET("hamlet"), VAULT("vault"), DARKPITS("dark pits"), HILLOCKS("hillocks"), TOMB(
                        "tomb"), TOWER("tower"), MOUNTAINHALLS(
                                "mountain halls"), CAMP("camp"), LAIR("lair"), SHRINE("shrine"), LABYRINTH("labyrinth");

        public final String name;

        SiteType(String name_)
        {
            name = name_;
        }

        public static SiteType getSite(String name)
        {
            for (SiteType t : SiteType.values())
            {
                if (t.name.equalsIgnoreCase(name)) { return t; }
            }
            return null;
        }

        public boolean isVillage()
        {
            return this == TOWN || this == HAMLET || this == HILLOCKS || this == HIPPYHUTS;
        }
    }

    public static class Site
    {
        public final String         name;
        public final int            id;
        public final SiteType       type;
        public int                  x;
        public int                  z;
        /** Corners in embark tile coordinates */
        public final int[][]        corners    = new int[2][2];
        public int[][]              rgbmap;
        public final Set<Structure> structures = new HashSet<DorfMap.Structure>();
        // public BufferedImage map;

        public Site(String name_, int id_, SiteType type_, int x_, int z_)
        {
            name = name_;
            id = id_;
            type = type_;
            x = x_;
            z = z_;
            if (type == null) { throw new NullPointerException(); }
        }

        public void setSiteLocation(int x1, int z1, int x2, int z2)
        {
            corners[0][0] = x1;
            corners[0][1] = z1;
            corners[1][0] = x2;
            corners[1][1] = z2;
            x = (x1 + x2) / 2;
            z = (z1 + z2) / 2;
            for (int x = x1; x <= x2; x++)
                for (int z = z1; z <= z2; z++)
                    addSiteByCoord(x, z, this);
        }

        @Override
        public String toString()
        {
            return name + " " + type + " " + id + " " + (corners[0][0] * scale) + "," + (corners[0][1] * scale) + "->"
                    + (corners[1][0] * scale) + "," + (corners[1][1] * scale);
        }

        @Override
        public int hashCode()
        {
            return id;
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof Site) { return ((Site) o).id == id; }
            return super.equals(o);
        }

        public boolean isInSite(int x, int z)
        {
            int width = rgbmap != null ? scale / 2 : 0;
            if (x < corners[0][0] * scale + width || z < corners[0][1] * scale + width) { return false; }
            if (rgbmap != null)
            {
                // System.out.println("test");
                // Equals as it starts at 0
                if (x >= (corners[0][0] * scale + rgbmap.length * scale / SiteStructureGenerator.SITETOBLOCK
                        + scale / 2)
                        || z >= (corners[0][1] * scale + rgbmap[0].length * scale / SiteStructureGenerator.SITETOBLOCK
                                + scale / 2))
                    return false;
            }
            return true;
        }
    }

    public static enum StructureType
    {
        MARKET("market"), UNDERWORLDSPIRE("underworld spire"), TEMPLE("temple");

        public final String name;

        StructureType(String name_)
        {
            name = name_;
        }
    }

    public static class Structure
    {
        final String        name;
        final String        name2;
        final int           id;
        final StructureType type;

        public Structure(String name_, String name2_, int id_, StructureType type_)
        {
            if (name_ == null)
            {
                name_ = "";
            }
            if (name2_ == null)
            {
                name2_ = "";
            }
            name = name_;
            name2 = name2_;
            id = id_;
            type = type_;
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof Structure) { return ((Structure) o).id == id; }
            return super.equals(o);
        }
    }

    public static enum RegionType
    {
        OCEAN, TUNDRA, GLACIER, FOREST, HILLS, GRASSLAND, WETLAND, MOUNTAINS, DESERT, LAKE, CAVERN, MAGMA, UNDERWORLD;
    }

    public static class Region
    {
        public final int                       id;
        public final String                    name;
        public final RegionType                type;
        final int                              depth;
        public final HashSet<Integer>          coords   = new HashSet<Integer>();
        public final HashMap<Integer, Integer> biomeMap = new HashMap<Integer, Integer>();

        public Region(int id_, String name_, RegionType type_)
        {
            id = id_;
            name = name_;
            type = type_;
            depth = 0;
        }

        public Region(int id_, String name_, int depth_, RegionType type_)
        {
            id = id_;
            name = name_;
            type = type_;
            depth = depth_;
        }

        public boolean isInRegion(int x, int z)
        {
            x = x / (scale * 16);
            z = z / (scale * 16);
            int key = x + 2048 * z + depth * 4194304;
            return coords.contains(key);
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof Region) { return ((Region) o).id == id; }
            return super.equals(o);
        }

        @Override
        public String toString()
        {
            return id + " " + name + " " + type;
        }
    }

    public static enum ConstructionType
    {
        ROAD, BRIDGE, TUNNEL;
    }

    public static class WorldConstruction
    {
        public final int                       id;
        public final String                    name;
        public final ConstructionType          type;
        public final HashSet<Integer>          worldCoords  = new HashSet<Integer>();
        /** Key: x,z coordinate, Value: depth, -1 for surface */
        public final HashMap<Integer, Integer> embarkCoords = new HashMap<Integer, Integer>();

        public WorldConstruction(int id_, String name_, ConstructionType type_)
        {
            id = id_;
            name = name_;
            type = type_;
        }

        public boolean isInRegion(int x, int z)
        {
            x = x / (scale * 16);
            z = z / (scale * 16);
            int key = x + 2048 * z;
            return worldCoords.contains(key);
        }

        public boolean isInConstruct(int x, int y, int z)
        {
            x = x / (scale);
            z = z / (scale);
            int key = x + 8192 * z;
            return embarkCoords.containsKey(key);
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof WorldConstruction) { return ((WorldConstruction) o).id == id; }
            return super.equals(o);
        }

        @Override
        public String toString()
        {
            return id + " " + name + " " + type;
        }
    }
}
