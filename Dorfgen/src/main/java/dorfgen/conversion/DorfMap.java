package dorfgen.conversion;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import dorfgen.conversion.FileLoader.FilesList;
import dorfgen.conversion.Interpolator.BicubicInterpolator;
import dorfgen.conversion.Interpolator.CachedBicubicInterpolator;
import net.minecraft.world.biome.Biomes;

public class DorfMap
{

    public int[][]                                      biomeMap             = new int[0][0];
    public int[][]                                      elevationMap         = new int[0][0];
    public int[][]                                      waterMap             = new int[0][0];
    public int[][]                                      riverMap             = new int[0][0];
    public int[][]                                      evilMap              = new int[0][0];
    public int[][]                                      rainMap              = new int[0][0];
    public int[][]                                      drainageMap          = new int[0][0];
    public int[][]                                      temperatureMap       = new int[0][0];
    public int[][]                                      volcanismMap         = new int[0][0];
    public int[][]                                      vegitationMap        = new int[0][0];
    public int[][]                                      structureMap         = new int[0][0];
    /** Coords are embark tile resolution and are x + 8192 * z */
    public HashMap<Integer, HashSet<Site>>              sitesByCoord         = new HashMap<>();
    public HashMap<Integer, Site>                       sitesById            = new HashMap<>();
    public HashMap<String, Site>                        sitesByName          = new HashMap<>();
    public HashMap<Integer, Region>                     regionsById          = new HashMap<>();
    /** Coords are world tile resolution and are x + 2048 * z */
    public HashMap<Integer, Region>                     regionsByCoord       = new HashMap<>();
    public HashMap<Integer, Region>                     ugRegionsById        = new HashMap<>();
    /** Coords are world tile resolution and are x + 2048 * z */
    public HashMap<Integer, Region>                     ugRegionsByCoord     = new HashMap<>();
    public HashMap<Integer, WorldConstruction>          constructionsById    = new HashMap<>();
    /** Coords are world tile resolution and are x + 2048 * z */
    public HashMap<Integer, HashSet<WorldConstruction>> constructionsByCoord = new HashMap<>();
    static int                                          waterShift           = -35;

    public BicubicInterpolator       biomeInterpolator  = new BicubicInterpolator();
    public CachedBicubicInterpolator heightInterpolator = new CachedBicubicInterpolator();
    public CachedBicubicInterpolator miscInterpolator   = new CachedBicubicInterpolator();

    public BufferedImage _elevationMap;
    public BufferedImage _elevationWaterMap;
    public BufferedImage _biomeMap;
    public BufferedImage _evilMap;
    public BufferedImage _temperatureMap;
    public BufferedImage _rainMap;
    public BufferedImage _drainageMap;
    public BufferedImage _vegitationMap;
    public BufferedImage _structuresMap;

    public final SiteStructureGenerator structureGen = new SiteStructureGenerator(this);

    public int scale = 51;

    public String name    = "region_1";
    public String altName = "World of Stuff";

    protected FilesList files;

    public void addSiteByCoord(final int x, final int z, final Site site)
    {
        final int coord = x + 8192 * z;
        HashSet<Site> sites = this.sitesByCoord.get(coord);
        if (sites == null)
        {
            sites = new HashSet<>();
            this.sitesByCoord.put(coord, sites);
        }
        sites.add(site);
    }

    public DorfMap()
    {
    }

    public void init(final FilesList files)
    {
        this.files = files;
        this.populateBiomeMap();
        this.populateElevationMap();
        this.populateWaterMap();
        this.populateTemperatureMap();
        this.populateVegitationMap();
        this.populateDrainageMap();
        this.populateRainMap();
        this.populateStructureMap();

        this.postProcessRegions();
        if (this.biomeMap.length > 0) this.postProcessBiomeMap();

        this.structureGen.init();
    }

    public void populateBiomeMap()
    {
        final BufferedImage img = this._biomeMap;
        if (img == null) return;
        this.biomeMap = new int[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
            {
                final int rgb = this._biomeMap.getRGB(x, y);
                this.biomeMap[x][y] = BiomeList.GetBiomeIndex(rgb);
            }
        this._biomeMap = null;
    }

    private int elevationSigmoid(final int preHeight)
    {
        final double a = preHeight;

        return (int) Math.min(Math.max((215. / (1 + Math.exp(-(a - 128.) / 20.)) + 45. + a) / 2., 10), 245) + 5;
    }

    public void populateElevationMap()
    {
        final BufferedImage img = this._elevationMap;
        if (img == null) return;
        final int shift = 10;
        this.elevationMap = new int[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
            {
                final int rgb = this._elevationMap.getRGB(x, y);

                final int r = rgb >> 16 & 0xFF, b = rgb >> 0 & 0xFF;
                int h = b - shift;
                if (r == 0) h = b + DorfMap.waterShift;
                h = Math.max(0, h);
                this.elevationMap[x][y] = this.elevationSigmoid(h);
                if (this.biomeMap.length > 0) if (h < 145 && this.biomeMap[x][y] == BiomeConversion.getBiomeID(
                        Biomes.MOUNTAINS)) this.biomeMap[x][y] = BiomeConversion.getBiomeID(Biomes.GRAVELLY_MOUNTAINS);
            }
        this._elevationMap = null;
    }

    public void populateWaterMap()
    {
        final BufferedImage img = this._elevationWaterMap;
        if (img == null) return;
        this.waterMap = new int[img.getWidth()][img.getHeight()];
        this.riverMap = new int[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
            {
                final int rgb = this._elevationWaterMap.getRGB(x, y);

                final int r = rgb >> 16 & 0xFF, g = rgb >> 8 & 0xFF, b = rgb >> 0 & 0xFF;
                final int biome = this.biomeMap[x][y];

                if (r == 0 && g == 0 && biome != BiomeConversion.getBiomeID(Biomes.RIVER))
                {
                    this.waterMap[x][y] = b + 25 + DorfMap.waterShift;
                    if (this.biomeMap.length > 0)
                    {
                        if (this.waterMap[x][y] < 50) this.biomeMap[x][y] = BiomeConversion.getBiomeID(
                                Biomes.DEEP_OCEAN);
                        else this.biomeMap[x][y] = BiomeConversion.getBiomeID(Biomes.OCEAN);
                        this.riverMap[x][y] = -1;
                    }
                }
                else if (r == 0 || biome == BiomeConversion.getBiomeID(Biomes.RIVER))
                {
                    this.waterMap[x][y] = -1;
                    this.riverMap[x][y] = biome == BiomeConversion.getBiomeID(Biomes.RIVER) ? b - DorfMap.waterShift
                            : b;
                }
                else
                {
                    this.waterMap[x][y] = -1;
                    this.riverMap[x][y] = -1;
                }
            }
        this.joinRivers();
        this._elevationWaterMap = null;
    }

    public void populateTemperatureMap()
    {
        final BufferedImage img = this._temperatureMap;
        if (img == null) return;
        this.temperatureMap = new int[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
            {
                final int rgb = img.getRGB(x, y);
                this.temperatureMap[x][y] = rgb & 255;
            }
        this._temperatureMap = null;
    }

    public void populateVegitationMap()
    {
        final BufferedImage img = this._vegitationMap;
        if (img == null) return;
        this.vegitationMap = new int[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
            {
                final int rgb = img.getRGB(x, y);
                this.vegitationMap[x][y] = rgb & 255;
            }
        this._vegitationMap = null;
    }

    public void populateDrainageMap()
    {
        final BufferedImage img = this._drainageMap;
        if (img == null) return;
        this.drainageMap = new int[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
            {
                final int rgb = img.getRGB(x, y);
                this.drainageMap[x][y] = rgb & 255;
            }
        this._drainageMap = null;
    }

    public void populateRainMap()
    {
        final BufferedImage img = this._rainMap;
        if (img == null) return;
        this.rainMap = new int[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
            {
                final int rgb = img.getRGB(x, y);
                this.rainMap[x][y] = rgb & 255;
            }
        this._rainMap = null;
    }

    public void populateStructureMap()
    {
        final BufferedImage img = this._structuresMap;
        if (img == null) return;
        this.structureMap = new int[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
            for (int x = 0; x < img.getWidth(); x++)
            {
                final int rgb = img.getRGB(x, y);
                this.structureMap[x][y] = rgb;
            }
        this._structuresMap = null;
    }

    private void joinRivers()
    {
        for (int y = 0; y < this.riverMap[0].length; y++)
            for (int x = 0; x < this.riverMap.length; x++)
            {
                final int r = this.riverMap[x][y];
                if (r > 0)
                {
                    final int num = this.countLarger(0, this.waterMap, x, y, 1);
                    final int num2 = this.countLarger(0, this.waterMap, x, y, 2);
                    if (num == 0 && num2 > 0)
                    {
                        final int[] dir = this.getDirToWater(x, y);
                        this.riverMap[x + dir[0]][y + dir[1]] = r;
                        this.riverMap[x + 2 * dir[0]][y + 2 * dir[1]] = r;

                    }
                }
            }
    }

    private int[] getDirToWater(final int x, final int y)
    {
        final int[] ret = new int[2];
        if (this.waterMap[x + 2][y] > 0) ret[0] = 1;
        else if (this.waterMap[x - 2][y] > 0) ret[0] = -1;
        else if (this.waterMap[x][y + 2] > 0) ret[1] = 1;
        else if (this.waterMap[x][y - 2] > 0) ret[1] = -1;

        return ret;
    }

    public int countNear(final int toCheck, final int[][] image, final int pixelX, final int pixelY, final int distance)
    {
        int ret = 0;
        for (int i = -distance; i <= distance; i++)
            for (int j = -distance; j <= distance; j++)
            {
                if (i == 0 && j == 0) continue;
                final int x = pixelX + i, y = pixelY + j;
                if (x >= 0 && x < image.length && y >= 0 && y < image[0].length) if (image[x][y] == toCheck) ret++;
            }
        return ret;
    }

    public int countLarger(final int toCheck, final int[][] image, final int pixelX, final int pixelY,
            final int distance)
    {
        int ret = 0;
        for (int i = -distance; i <= distance; i++)
            for (int j = -distance; j <= distance; j++)
            {
                if (i == 0 && j == 0) continue;
                final int x = pixelX + i, y = pixelY + j;
                if (x >= 0 && x < image.length && y >= 0 && y < image[0].length) if (image[x][y] > toCheck) ret++;
            }
        return ret;
    }

    public void postProcessBiomeMap()
    {
        final boolean hasThermalMap = this.temperatureMap.length > 0;

        for (int x = 0; x < this.biomeMap.length; x++)
            for (int z = 0; z < this.biomeMap[0].length; z++)
            {
                final int biome = this.biomeMap[x][z];
                final int temperature = hasThermalMap ? this.temperatureMap[x][z] : 128;
                final int drainage = this.drainageMap.length > 0 ? this.drainageMap[x][z] : 100;
                final int rain = this.rainMap.length > 0 ? this.rainMap[x][z] : 100;
                final int evil = this.evilMap.length > 0 ? this.evilMap[x][z] : 100;
                final Region region = this.getRegionForCoords(x * this.scale, z * this.scale);
                if (biome != BiomeConversion.getBiomeID(Biomes.RIVER))
                {
                    final int newBiome = BiomeList.getBiomeFromValues(biome, temperature, drainage, rain, evil, region);
                    this.biomeMap[x][z] = newBiome;
                }
            }
    }

    public Region getRegionForCoords(int x, int z)
    {
        x = x / (this.scale * 16);
        z = z / (this.scale * 16);
        final int key = x + 2048 * z;
        return this.regionsByCoord.get(key);
    }

    public Region getUgRegionForCoords(int x, final int depth, int z)
    {
        x = x / (this.scale * 16);
        z = z / (this.scale * 16);
        final int key = x + 2048 * z + depth * 4194304;
        return this.ugRegionsByCoord.get(key);
    }

    public Set<Site> getSiteForCoords(final int x, final int z)
    {
        final int kx = x / this.scale;
        final int kz = z / this.scale;
        final int key = kx + 8192 * kz;
        final HashSet<Site> ret = this.sitesByCoord.get(key);
        if (ret != null) for (final Site s : ret)
            if (s.isInSite(x, z)) return ret;
        return Collections.emptySet();
    }

    public HashSet<WorldConstruction> getConstructionsForCoords(int x, int z)
    {
        x = x / (this.scale * 16);
        z = z / (this.scale * 16);
        final int key = x + 2048 * z;
        return this.constructionsByCoord.get(key);
    }

    public void postProcessRegions()
    {
        for (final Region region : this.regionsById.values())
            for (final int i : region.coords)
                if (!this.regionsByCoord.containsKey(i)) this.regionsByCoord.put(i, region);
                else System.err.println("Existing region for " + (i & 2047) + " " + i / 2048);
        for (final Region region : this.ugRegionsById.values())
            for (final int i : region.coords)
                if (!this.ugRegionsByCoord.containsKey(i)) this.ugRegionsByCoord.put(i, region);
                else System.err.println("Existing region for " + (i & 2047) + " " + i / 2048);
    }

    public static enum SiteType
    {
        CAVE("cave"), FORTRESS("fortress"), TOWN("town"), HIPPYHUTS("forest retreat"), DARKFORTRESS(
                "dark fortress"), HAMLET("hamlet"), VAULT("vault"), DARKPITS("dark pits"), HILLOCKS("hillocks"), TOMB(
                        "tomb"), TOWER("tower"), MOUNTAINHALLS("mountain halls"), CAMP("camp"), LAIR("lair"), SHRINE(
                                "shrine"), LABYRINTH("labyrinth");

        public final String name;

        SiteType(final String name_)
        {
            this.name = name_;
        }

        public static SiteType getSite(final String name)
        {
            for (final SiteType t : SiteType.values())
                if (t.name.equalsIgnoreCase(name)) return t;
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
        public final Set<Structure> structures = new HashSet<>();
        private final DorfMap       parent;

        public Site(final String name_, final int id_, final SiteType type_, final DorfMap parent, final int x_,
                final int z_)
        {
            this.name = name_;
            this.id = id_;
            this.type = type_;
            this.x = x_;
            this.z = z_;
            this.parent = parent;
            if (this.type == null) throw new NullPointerException();
        }

        public void setSiteLocation(final int x1, final int z1, final int x2, final int z2)
        {
            this.corners[0][0] = x1;
            this.corners[0][1] = z1;
            this.corners[1][0] = x2;
            this.corners[1][1] = z2;
            this.x = (x1 + x2) / 2;
            this.z = (z1 + z2) / 2;
            for (int x = x1; x <= x2; x++)
                for (int z = z1; z <= z2; z++)
                    this.parent.addSiteByCoord(x, z, this);
        }

        @Override
        public String toString()
        {
            return this.name + " " + this.type + " " + this.id + " " + this.corners[0][0] * this.parent.scale + ","
                    + this.corners[0][1] * this.parent.scale + "->" + this.corners[1][0] * this.parent.scale + ","
                    + this.corners[1][1] * this.parent.scale;
        }

        @Override
        public int hashCode()
        {
            return this.id;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (o instanceof Site) return ((Site) o).id == this.id;
            return super.equals(o);
        }

        public boolean isInSite(final int x, final int z)
        {
            final int width = this.rgbmap != null ? this.parent.scale / 2 : 0;
            if (x < this.corners[0][0] * this.parent.scale + width || z < this.corners[0][1] * this.parent.scale
                    + width) return false;
            if (this.rgbmap != null) // System.out.println("test");
                // Equals as it starts at 0
                if (x >= this.corners[0][0] * this.parent.scale + this.rgbmap.length * this.parent.scale
                        / SiteStructureGenerator.SITETOBLOCK + this.parent.scale / 2 || z >= this.corners[0][1]
                                * this.parent.scale + this.rgbmap[0].length * this.parent.scale
                                        / SiteStructureGenerator.SITETOBLOCK + this.parent.scale / 2) return false;
            return true;
        }
    }

    public static enum StructureType
    {
        MARKET("market"), UNDERWORLDSPIRE("underworld spire"), TEMPLE("temple");

        public final String name;

        StructureType(final String name_)
        {
            this.name = name_;
        }
    }

    public static class Structure
    {
        final String        name;
        final String        name2;
        final int           id;
        final StructureType type;

        public Structure(String name_, String name2_, final int id_, final StructureType type_)
        {
            if (name_ == null) name_ = "";
            if (name2_ == null) name2_ = "";
            this.name = name_;
            this.name2 = name2_;
            this.id = id_;
            this.type = type_;
        }

        @Override
        public boolean equals(final Object o)
        {
            if (o instanceof Structure) return ((Structure) o).id == this.id;
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
        public final HashSet<Integer>          coords   = new HashSet<>();
        public final HashMap<Integer, Integer> biomeMap = new HashMap<>();
        private final DorfMap                  parent;

        public Region(final int id_, final String name_, final RegionType type_, final DorfMap parent)
        {
            this.id = id_;
            this.name = name_;
            this.type = type_;
            this.depth = 0;
            this.parent = parent;
        }

        public Region(final int id_, final String name_, final int depth_, final RegionType type_, final DorfMap parent)
        {
            this.id = id_;
            this.name = name_;
            this.type = type_;
            this.depth = depth_;
            this.parent = parent;
        }

        public boolean isInRegion(int x, int z)
        {
            x = x / (this.parent.scale * 16);
            z = z / (this.parent.scale * 16);
            final int key = x + 2048 * z + this.depth * 4194304;
            return this.coords.contains(key);
        }

        @Override
        public boolean equals(final Object o)
        {
            if (o instanceof Region) return ((Region) o).id == this.id;
            return super.equals(o);
        }

        @Override
        public String toString()
        {
            return this.id + " " + this.name + " " + this.type;
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
        public final HashSet<Integer>          worldCoords  = new HashSet<>();
        /** Key: x,z coordinate, Value: depth, -1 for surface */
        public final HashMap<Integer, Integer> embarkCoords = new HashMap<>();
        private final DorfMap                  parent;

        public WorldConstruction(final int id_, final String name_, final ConstructionType type_, final DorfMap parent)
        {
            this.id = id_;
            this.name = name_;
            this.type = type_;
            this.parent = parent;
        }

        public boolean isInRegion(int x, int z)
        {
            x = x / (this.parent.scale * 16);
            z = z / (this.parent.scale * 16);
            final int key = x + 2048 * z;
            return this.worldCoords.contains(key);
        }

        public boolean isInConstruct(int x, final int y, int z)
        {
            x = x / this.parent.scale;
            z = z / this.parent.scale;
            final int key = x + 8192 * z;
            return this.embarkCoords.containsKey(key);
        }

        @Override
        public boolean equals(final Object o)
        {
            if (o instanceof WorldConstruction) return ((WorldConstruction) o).id == this.id;
            return super.equals(o);
        }

        @Override
        public String toString()
        {
            return this.id + " " + this.name + " " + this.type;
        }
    }
}
