package dorfgen.conversion;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// import com.sun.org.apache.xerces.internal.dom.DeferredElementImpl;

import MappedXML.MappedTruncate;
import dorfgen.Dorfgen;
import dorfgen.conversion.DorfMap.ConstructionType;
import dorfgen.conversion.DorfMap.Region;
import dorfgen.conversion.DorfMap.RegionType;
import dorfgen.conversion.DorfMap.Site;
import dorfgen.conversion.DorfMap.SiteType;
import dorfgen.conversion.DorfMap.Structure;
import dorfgen.conversion.DorfMap.StructureType;
import dorfgen.conversion.DorfMap.WorldConstruction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

public class FileLoader
{
    public static class FilesList
    {
        public final File mainDir;
        public String     elevation              = "";
        public String     elevationWater         = "";
        public String     biome                  = "";
        public String     temperature            = "";
        public String     evil                   = "";
        public String     rain                   = "";
        public String     volcanism              = "";
        public String     vegitation             = "";
        public String     structs                = "";
        public String     legends                = "";
        public String     legendsPlus            = "";
        public String     constructionFineCoords = "";
        public String     siteInfo               = "";

        public FilesList(final File dir)
        {
            this.mainDir = dir;
        }
    }

    public static HashMap<Integer, BufferedImage> sites = new HashMap<>();

    public FileLoader(final File initialDir, final File biomesFile)
    {
        this.loadBiomes(biomesFile);
        DorfMap dorfs = this.loadFromFolder(new FilesList(initialDir));
        // First test is for a randomly added dorfmap, not in a subfolder
        if (dorfs != null) Dorfgen.instance.addDorfMap(dorfs);
        else for (final File f : initialDir.listFiles())
        {
            Dorfgen.LOGGER.info("Testing  {}", f);
            // Scan the sub-directories
            if (f.isDirectory())
            {
                dorfs = this.loadFromFolder(new FilesList(f));
                if (dorfs != null) Dorfgen.instance.addDorfMap(dorfs);
            }
        }
    }

    private DorfMap loadFromFolder(final FilesList files)
    {
        Dorfgen.LOGGER.info("Scanning  {}", files.mainDir);
        final DorfMap dorfs = new DorfMap();
        for (final File f : files.mainDir.listFiles())
        {
            String s = f.getName();
            if (f.isDirectory() && s.contains("site_maps")) for (final File f1 : f.listFiles())
            {
                s = f1.getName();
                if (s.contains("-site_map-"))
                {
                    final String[] args = s.split("-");
                    final String s1 = args[args.length - 1].replace(".png", "").replace(".bmp", "");
                    final Integer id = Integer.parseInt(s1);
                    final BufferedImage site = this.getImage(f1.getAbsolutePath());
                    if (site != null) FileLoader.sites.put(id, site);
                    else new NullPointerException("Site " + id + " did not read correctly.").printStackTrace();
                }
            }
            else if (f.isDirectory() && s.contains("region_maps")) for (final File f1 : f.listFiles())
            {
                s = f1.getAbsolutePath();
                if (s.contains("-el.")) files.elevation = s;
                else if (s.contains("-elw.")) files.elevationWater = s;
                else if (s.contains("-bm.")) files.biome = s;
                else if (s.contains("-rain.")) files.rain = s;
                else if (s.contains("-tmp.")) files.temperature = s;
                else if (s.contains("-vol.")) files.volcanism = s;
                else if (s.contains("-veg.")) files.vegitation = s;
                else if (s.contains("-evil.")) files.evil = s;
                else if (s.contains("-str.")) files.structs = s;
            }
            else if (!f.isDirectory())
            {
                s = f.getAbsolutePath();
                if (s.contains("-legends_plus")) files.legendsPlus = s;
                else if (s.contains("-legends")) files.legends = s;
                else if (s.contains("constructs.txt")) files.constructionFineCoords = s;
                else if (s.contains("sites.txt")) files.siteInfo = s;
            }
        }

        if (files.elevation.isEmpty()) return null;

        try
        {
            if (!files.legends.isEmpty())
            {
                if (!files.legends.contains("trunc"))
                {
                    MappedTruncate.ReadTruncateAndOutput(files.legends, files.legends.replace(".xml", "_trunc.xml"),
                            "</world_constructions>", "\n</df_world>");
                    files.legends = files.legends.replace(".xml", "_trunc.xml");
                }
                FileLoader.loadLegends(files.legends, dorfs);
            }
        }
        catch (final Throwable e)
        {
            Dorfgen.LOGGER.error("Error loading legends {}", files.legends);
            Dorfgen.LOGGER.error("caught: ", e);
        }
        try
        {
            if (!files.legendsPlus.isEmpty())
            {
                if (!files.legendsPlus.contains("trunc"))
                {
                    MappedTruncate.ReadTruncateAndOutput(files.legendsPlus, files.legendsPlus.replace(".xml",
                            "_trunc.xml"), "</world_constructions>", "\n</df_world>");
                    files.legendsPlus = files.legendsPlus.replace(".xml", "_trunc.xml");
                }
                FileLoader.loadLegendsPlus(files.legendsPlus, dorfs);
            }
        }
        catch (final Throwable e)
        {
            Dorfgen.LOGGER.error("Error loading legends-plus {}", files.legendsPlus);
            Dorfgen.LOGGER.error("caught: ", e);
        }
        try
        {
            if (!files.constructionFineCoords.isEmpty()) FileLoader.loadFineConstructLocations(
                    files.constructionFineCoords, dorfs);
        }
        catch (final Throwable e)
        {
            Dorfgen.LOGGER.error("Error loading structure coords {}", files.constructionFineCoords);
            Dorfgen.LOGGER.error("caught: ", e);
        }
        try
        {
            if (!files.siteInfo.isEmpty()) FileLoader.loadSiteInfo(files.siteInfo, dorfs);
        }
        catch (final Throwable e)
        {
            Dorfgen.LOGGER.error("Error loading site info {}", files.siteInfo);
            Dorfgen.LOGGER.error("caught: ", e);
        }

        dorfs._biomeMap = this.getImage(files.biome);
        dorfs._elevationMap = this.getImage(files.elevation);
        dorfs._elevationWaterMap = this.getImage(files.elevationWater);
        dorfs._temperatureMap = this.getImage(files.temperature);
        dorfs._vegitationMap = this.getImage(files.vegitation);
        dorfs._structuresMap = this.getImage(files.structs);

        if (dorfs._elevationMap == null) return null;
        dorfs.init(files);
        return dorfs;
    }

    BufferedImage getImage(final String file)
    {
        BufferedImage ret = null;
        try
        {
            final InputStream res = new FileInputStream(file);
            ret = ImageIO.read(res);
        }
        catch (final Exception e)
        {
            Dorfgen.LOGGER.error("Cannot find " + file);
        }

        return ret;
    }

    public static void loadLegends(final String file, final DorfMap dorfs) throws Exception
    {

        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        final Document doc = dBuilder.parse(new FileInputStream(file));
        doc.getDocumentElement().normalize();

        final NodeList siteList = doc.getElementsByTagName("site");

        for (int i = 0; i < siteList.getLength(); i++)
        {
            final Node siteNode = siteList.item(i);
            int id = -1;
            String typeName = null;
            String name = null;
            String coords = null;
            for (int j = 0; j < siteNode.getChildNodes().getLength(); j++)
            {
                final Node node = siteNode.getChildNodes().item(j);
                final String nodeName = node.getNodeName();
                if (nodeName.equals("id")) id = Integer.parseInt(node.getFirstChild().getNodeValue());
                if (nodeName.equals("name")) name = node.getFirstChild().getNodeValue();
                if (nodeName.equals("type")) typeName = node.getFirstChild().getNodeValue();
                if (nodeName.equals("coords")) coords = node.getFirstChild().getNodeValue();
            }
            if (id == -1) continue;
            final SiteType type = SiteType.getSite(typeName);
            final String[] args = coords.split(",");
            int x = Integer.parseInt(args[0]);
            int z = Integer.parseInt(args[1]);
            final Site site = new Site(name, id, type, dorfs, x, z);
            if (FileLoader.sites.containsKey(id))
            {
                final BufferedImage image = FileLoader.sites.get(id);
                site.rgbmap = new int[image.getWidth()][image.getHeight()];
                for (x = 0; x < image.getWidth(); x++)
                    for (z = 0; z < image.getHeight(); z++)
                        site.rgbmap[x][z] = image.getRGB(x, z);
                FileLoader.sites.remove(id);
            }
            dorfs.sitesById.put(id, site);
            dorfs.sitesByName.put(site.name, site);
        }

        NodeList regionList = doc.getElementsByTagName("region");
        for (int i = 0; i < regionList.getLength(); i++)
        {
            final Node regionNode = regionList.item(i);
            int id = -1;
            String typeName = null;
            String name = null;
            for (int j = 0; j < regionNode.getChildNodes().getLength(); j++)
            {
                final Node node = regionNode.getChildNodes().item(j);
                final String nodeName = node.getNodeName();
                if (nodeName.equals("id")) id = Integer.parseInt(node.getFirstChild().getNodeValue());
                if (nodeName.equals("name")) name = node.getFirstChild().getNodeValue();
                if (nodeName.equals("type")) typeName = node.getFirstChild().getNodeValue();
            }
            final Region region = new Region(id, name, RegionType.valueOf(typeName.toUpperCase()), dorfs);
            dorfs.regionsById.put(id, region);
        }

        regionList = doc.getElementsByTagName("underground_region");
        for (int i = 0; i < regionList.getLength(); i++)
        {
            final Node regionNode = regionList.item(i);
            int id = -1;
            String typeName = null;
            String name = null;
            int depth = 0;
            for (int j = 0; j < regionNode.getChildNodes().getLength(); j++)
            {
                final Node node = regionNode.getChildNodes().item(j);
                final String nodeName = node.getNodeName();
                if (nodeName.equals("id")) id = Integer.parseInt(node.getFirstChild().getNodeValue());
                if (nodeName.equals("name")) name = node.getFirstChild().getNodeValue();
                if (nodeName.equals("type")) typeName = node.getFirstChild().getNodeValue();
                if (nodeName.equals("depth")) depth = Integer.parseInt(node.getFirstChild().getNodeValue());
            }

            final Region region = new Region(id, name, depth, RegionType.valueOf(typeName.toUpperCase()), dorfs);
            dorfs.ugRegionsById.put(id, region);
        }
    }

    public static void loadLegendsPlus(final String file, final DorfMap dorfs) throws Exception
    {

        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        final Document doc = dBuilder.parse(new FileInputStream(file));
        doc.getDocumentElement().normalize();

        final NodeList names = doc.getElementsByTagName("name");
        final NodeList altNames = doc.getElementsByTagName("altname");

        if (names.getLength() > 0) dorfs.name = names.item(0).getFirstChild().getNodeValue();
        if (altNames.getLength() > 0) dorfs.altName = altNames.item(0).getFirstChild().getNodeValue();

        System.out.println(names.getLength() + " " + names.item(0).getFirstChild().getNodeValue());
        System.out.println(altNames.getLength() + " " + altNames.item(0).getFirstChild().getNodeValue());

        final NodeList siteList = doc.getElementsByTagName("site");

        for (int i = 0; i < siteList.getLength(); i++)
        {
            final Node siteNode = siteList.item(i);
            int id = -1;
            final HashSet<Structure> toAdd = new HashSet<>();
            for (int j = 0; j < siteNode.getChildNodes().getLength(); j++)
            {
                final Node node = siteNode.getChildNodes().item(j);
                final String nodeName = node.getNodeName();
                if (nodeName.equals("id")) id = Integer.parseInt(node.getFirstChild().getNodeValue());
                if (nodeName.equals("structures"))
                {
                    final NodeList structures = node.getChildNodes();
                    for (int k = 0; k < structures.getLength(); k++)
                    {
                        final Node structure = structures.item(k);
                        if (structure.getNodeName().equals("structure"))
                        {
                            final NodeList structureList = structure.getChildNodes();
                            int structId = -1;
                            StructureType structType = null;
                            String name = "";
                            String name2 = "";

                            for (int l = 0; l < structureList.getLength(); l++)
                            {
                                final String subName = structureList.item(l).getNodeName();
                                if (subName.equals("id")) structId = Integer.parseInt(structureList.item(l)
                                        .getFirstChild().getNodeValue());
                                if (subName.equals("type"))
                                {
                                    final String typeName = structureList.item(l).getFirstChild().getNodeValue();
                                    for (final StructureType t : StructureType.values())
                                        if (t.name.equals(typeName))
                                        {
                                            structType = t;
                                            break;
                                        }
                                }
                                if (subName.equals("name")) name = structureList.item(l).getFirstChild().getNodeValue();
                                if (subName.equals("name2")) name2 = structureList.item(l).getFirstChild()
                                        .getNodeValue();
                            }
                            if (structId == -1) continue;
                            toAdd.add(new Structure(name, name2, structId, structType));
                        }
                    }
                }
            }
            if (id == -1) continue;
            final Site site = dorfs.sitesById.get(id);
            if (site != null) site.structures.addAll(toAdd);
            else new Exception().printStackTrace();

        }

        NodeList regionList = doc.getElementsByTagName("region");
        for (int i = 0; i < regionList.getLength(); i++)
        {
            final Node regionNode = regionList.item(i);
            int id = -1;
            String coords = "";
            String[] toAdd = null;
            for (int j = 0; j < regionNode.getChildNodes().getLength(); j++)
            {
                final Node node = regionNode.getChildNodes().item(j);
                final String nodeName = node.getNodeName();
                if (nodeName.equals("id")) id = Integer.parseInt(node.getFirstChild().getNodeValue());
                if (nodeName.equals("coords")) coords = node.getFirstChild().getNodeValue();
            }
            toAdd = coords.split("\\|");
            // System.out.println(toAdd.length+": "+coords.length()+":
            // "+coords);
            if (id != -1 && toAdd != null)
            {
                final Region region = dorfs.regionsById.get(id);
                if (region != null)
                {
                    region.coords.clear();
                    for (final String s : toAdd)
                    {
                        final int x = Integer.parseInt(s.split(",")[0]);
                        final int z = Integer.parseInt(s.split(",")[1]);
                        region.coords.add(x + 2048 * z + region.depth * 4194304);
                    }
                }
                else new Exception().printStackTrace();
            }
        }

        regionList = doc.getElementsByTagName("underground_region");
        for (int i = 0; i < regionList.getLength(); i++)
        {
            final Node regionNode = regionList.item(i);
            int id = -1;
            String coords = "";
            String[] toAdd = null;
            for (int j = 0; j < regionNode.getChildNodes().getLength(); j++)
            {
                final Node node = regionNode.getChildNodes().item(j);
                final String nodeName = node.getNodeName();
                if (nodeName.equals("id")) id = Integer.parseInt(node.getFirstChild().getNodeValue());
                if (nodeName.equals("coords")) coords = node.getFirstChild().getNodeValue();
            }
            toAdd = coords.split("\\|");
            // System.out.println(toAdd.length+": "+coords.length()+":
            // "+coords);
            if (id != -1 && toAdd != null)
            {
                final Region region = dorfs.ugRegionsById.get(id);
                if (region != null)
                {
                    region.coords.clear();
                    for (final String s : toAdd)
                    {
                        final int x = Integer.parseInt(s.split(",")[0]);
                        final int z = Integer.parseInt(s.split(",")[1]);
                        region.coords.add(x + 2048 * z + region.depth * 4194304);
                    }
                }
                else new Exception().printStackTrace();
            }
        }

        regionList = doc.getElementsByTagName("world_construction");
        for (int i = 0; i < regionList.getLength(); i++)
        {
            final Node regionNode = regionList.item(i);
            int id = -1;
            String name = "";
            String type = "";
            String coords = "";
            String[] toAdd = null;
            for (int j = 0; j < regionNode.getChildNodes().getLength(); j++)
            {
                final Node node = regionNode.getChildNodes().item(j);
                final String nodeName = node.getNodeName();
                if (nodeName.equals("id")) id = Integer.parseInt(node.getFirstChild().getNodeValue());
                if (nodeName.equals("coords")) coords = node.getFirstChild().getNodeValue();
                if (nodeName.equals("name")) name = node.getFirstChild().getNodeValue();
                if (nodeName.equals("type")) type = node.getFirstChild().getNodeValue();
            }
            toAdd = coords.split("\\|");
            // System.out.println(toAdd.length+": "+coords.length()+":
            // "+coords);
            if (id != -1 && toAdd != null)
            {
                final ConstructionType cType = ConstructionType.valueOf(type.toUpperCase());
                if (cType != null)
                {
                    final WorldConstruction construct = new WorldConstruction(id, name, cType, dorfs);
                    construct.worldCoords.clear();
                    for (final String s : toAdd)
                    {
                        final int x = Integer.parseInt(s.split(",")[0]);
                        final int z = Integer.parseInt(s.split(",")[1]);
                        final int index = x + 2048 * z;
                        construct.worldCoords.add(index);
                        HashSet<WorldConstruction> constructs = dorfs.constructionsByCoord.get(index);
                        if (constructs == null)
                        {
                            constructs = new HashSet<>();
                            dorfs.constructionsByCoord.put(index, constructs);
                        }
                        constructs.add(construct);
                    }
                    dorfs.constructionsById.put(id, construct);
                }
                else new NullPointerException("Unknown Construction Type").printStackTrace();
            }
        }
    }

    public static void loadFineConstructLocations(final String file, final DorfMap dorfs)
    {
        final ArrayList<String> rows = new ArrayList<>();
        BufferedReader br = null;
        String line = "";

        try
        {
            final InputStream res = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(res));
            while ((line = br.readLine()) != null)
                rows.add(line);

            for (final String entry : rows)
            {
                final String[] args = entry.split(":");
                final int id = Integer.parseInt(args[0]);
                final WorldConstruction construct = dorfs.constructionsById.get(id);
                if (construct != null) for (int i = 1; i < args.length; i++)
                {
                    final String[] coordString = args[i].split(",");
                    final int x = Integer.parseInt(coordString[0]);
                    final int y = Integer.parseInt(coordString[1]);
                    final int z = Integer.parseInt(coordString[2]);
                    final int index = x + 8192 * z;
                    construct.embarkCoords.put(index, y);
                }
                else new NullPointerException("Cannot Find Construction for id:" + id).printStackTrace();
            }
        }
        catch (final Exception e)
        {

        }
    }

    public static void loadSiteInfo(final String file, final DorfMap dorfs)
    {
        final ArrayList<String> rows = new ArrayList<>();
        BufferedReader br = null;
        String line = "";

        try
        {
            final InputStream res = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(res));
            int n = 0;
            while ((line = br.readLine()) != null)
                rows.add(line);

            for (final String entry : rows)
            {
                final String[] args = entry.split(":");
                final int id = Integer.parseInt(args[0]);

                final Site site = dorfs.sitesById.get(id);

                if (site != null)
                {
                    final String[] corner1 = args[1].split("->")[0].split(",");
                    final int x1 = Integer.parseInt(corner1[0]);
                    final int y1 = Integer.parseInt(corner1[1]);
                    final String[] corner2 = args[1].split("->")[1].split(",");
                    final int x2 = Integer.parseInt(corner2[0]);
                    final int y2 = Integer.parseInt(corner2[1]);
                    site.setSiteLocation(x1, y1, x2, y2);
                    n++;
                }
                else new NullPointerException("Cannot Find Site for id:" + id).printStackTrace();
            }
            Dorfgen.LOGGER.info("Imported locations for " + n + " Sites");
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    private void loadBiomes(final File file)
    {
        final ArrayList<ArrayList<String>> rows = new ArrayList<>();
        BufferedReader br = null;
        String line = "";
        final String cvsSplitBy = ",";

        try
        {
            final InputStream res = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(res));
            int n = 0;
            while ((line = br.readLine()) != null)
            {

                final String[] row = line.split(cvsSplitBy);
                rows.add(new ArrayList<String>());
                for (final String element : row)
                    rows.get(n).add(element);
                n++;
            }

        }
        catch (final FileNotFoundException e)
        {

            FileWriter fwriter;
            PrintWriter out;
            try
            {
                fwriter = new FileWriter(file);
                out = new PrintWriter(fwriter);

                final String defaultPath = "/assets/dorfgen/biomes.csv";
                final ArrayList<String> lines = new ArrayList<>();
                InputStream res;
                try
                {
                    res = this.getClass().getResourceAsStream(defaultPath);

                    br = new BufferedReader(new InputStreamReader(res));
                    int n = 0;

                    while ((line = br.readLine()) != null)
                    {
                        lines.add(line);
                        final String[] row = line.split(cvsSplitBy);
                        rows.add(new ArrayList<String>());
                        for (final String element : row)
                            rows.get(n).add(element);
                        n++;
                    }
                }
                catch (final Exception e1)
                {
                    e1.printStackTrace();
                }
                for (final String s : lines)
                    out.println(s);

                out.close();
                fwriter.close();

            }
            catch (final IOException e2)
            {
            }

        }
        catch (final NullPointerException e)
        {
            e.printStackTrace();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (br != null) try
            {
                br.close();
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }
        }

        for (int i = 1; i < rows.size(); i++)
        {
            final ArrayList<String> row = rows.get(i);
            int r;
            int g;
            int b;
            int biomeId = -1;
            String biomeName = "";
            r = Integer.parseInt(row.get(0));
            g = Integer.parseInt(row.get(1));
            b = Integer.parseInt(row.get(2));

            try
            {
                biomeId = Integer.parseInt(row.get(3));
            }
            catch (final Exception e)
            {
            }
            if (row.size() > 4) biomeName = row.get(4);
            final ResourceLocation biomeRes = new ResourceLocation(biomeName);
            final Biome biome = ForgeRegistries.BIOMES.getValue(biomeRes);
            if (biome == null)
            {
                Dorfgen.LOGGER.error("Error locating biome {}", biomeRes);
                Dorfgen.LOGGER.error("line {}", row);
                continue;
            }
            biomeId = BiomeConversion.getBiomeID(biome);
            final Color c = new Color(r, g, b);
            BiomeList.biomes.put(c.getRGB(), new BiomeConversion(c, biomeId));
        }

    }

}
