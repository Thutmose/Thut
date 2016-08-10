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
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// import com.sun.org.apache.xerces.internal.dom.DeferredElementImpl;

import MappedXML.MappedTruncate;
import dorfgen.WorldGenerator;
import dorfgen.conversion.DorfMap.ConstructionType;
import dorfgen.conversion.DorfMap.Region;
import dorfgen.conversion.DorfMap.RegionType;
import dorfgen.conversion.DorfMap.Site;
import dorfgen.conversion.DorfMap.SiteType;
import dorfgen.conversion.DorfMap.Structure;
import dorfgen.conversion.DorfMap.StructureType;
import dorfgen.conversion.DorfMap.WorldConstruction;
import net.minecraft.world.biome.Biome;

public class FileLoader
{

    public static String                          biomes                 = "";

    public static File                            resourceDir            = null;

    public String                                 elevation              = "";
    public String                                 elevationWater         = "";
    public String                                 biome                  = "";
    public String                                 temperature            = "";
    public String                                 evil                   = "";
    public String                                 rain                   = "";
    public String                                 volcanism              = "";
    public String                                 vegitation             = "";
    public String                                 structs                = "";
    public String                                 legends                = "";
    public String                                 legendsPlus            = "";
    public String                                 constructionFineCoords = "";
    public String                                 siteInfo               = "";

    public static HashMap<Integer, BufferedImage> sites                  = new HashMap<Integer, BufferedImage>();

    public FileLoader()
    {
        File temp = new File(biomes.replace("biomes.csv", ""));
        if (!temp.exists())
        {
            temp.mkdirs();
        }
        resourceDir = temp;
        for (File f : temp.listFiles())
        {
            String s = f.getName();
            if (f.isDirectory() && s.contains("site_maps"))
            {
                for (File f1 : f.listFiles())
                {
                    s = f1.getName();
                    if (s.contains("-site_map-"))
                    {
                        String[] args = s.split("-");
                        String s1 = args[args.length - 1].replace(".png", "").replace(".bmp", "");
                        Integer id = Integer.parseInt(s1);
                        BufferedImage site = getImage(f1.getAbsolutePath());
                        if (site != null) sites.put(id, site);
                        else new NullPointerException("Site " + id + " did not read correctly.").printStackTrace();
                    }
                }
            }
            else if (f.isDirectory() && s.contains("region_maps"))
            {
                for (File f1 : f.listFiles())
                {
                    s = f1.getAbsolutePath();
                    if (s.contains("-el."))
                    {
                        elevation = s;
                    }
                    else if (s.contains("-elw."))
                    {
                        elevationWater = s;
                    }
                    else if (s.contains("-bm."))
                    {
                        biome = s;
                    }
                    else if (s.contains("-rain."))
                    {
                        rain = s;
                    }
                    else if (s.contains("-tmp."))
                    {
                        temperature = s;
                    }
                    else if (s.contains("-vol."))
                    {
                        volcanism = s;
                    }
                    else if (s.contains("-veg."))
                    {
                        vegitation = s;
                    }
                    else if (s.contains("-evil."))
                    {
                        evil = s;
                    }
                    else if (s.contains("-str."))
                    {
                        structs = s;
                    }
                }
            }
            else if (!f.isDirectory())
            {
                s = f.getAbsolutePath();
                if (s.contains("-legends") && !s.contains("plus"))
                {
                    legends = s;
                }
                else if (s.contains("-legends_plus"))
                {
                    legendsPlus = s;
                }
                else if (s.contains("constructs.txt"))
                {
                    constructionFineCoords = s;
                }
                else if (s.contains("sites.txt"))
                {
                    siteInfo = s;
                }
            }
        }

        if (!legends.contains("trunc"))
        {
            MappedTruncate.ReadTruncateAndOutput(legends, legends.replace(".xml", "_trunc.xml"),
                    "</world_constructions>", "\n</df_world>");
            legends = legends.replace(".xml", "_trunc.xml");
        }
        if (!legendsPlus.contains("trunc"))
        {
            MappedTruncate.ReadTruncateAndOutput(legendsPlus, legendsPlus.replace(".xml", "_trunc.xml"),
                    "</world_constructions>", "\n</df_world>");
            legendsPlus = legendsPlus.replace(".xml", "_trunc.xml");
        }
        loadLegends(legends);
        loadLegendsPlus(legendsPlus);
        loadFineConstructLocations(constructionFineCoords);
        loadSiteInfo(siteInfo);

        WorldGenerator.instance.biomeMap = getImage(biome);
        WorldGenerator.instance.elevationMap = getImage(elevation);
        WorldGenerator.instance.elevationWaterMap = getImage(elevationWater);
        WorldGenerator.instance.temperatureMap = getImage(temperature);
        WorldGenerator.instance.vegitationMap = getImage(vegitation);
        WorldGenerator.instance.structuresMap = getImage(structs);

        loadBiomes(biomes);
    }

    BufferedImage getImage(String file)
    {
        BufferedImage ret = null;
        try
        {
            InputStream res = new FileInputStream(file);
            ret = ImageIO.read(res);
        }
        catch (Exception e)
        {
            System.err.println("Cannot find " + file);
        }

        return ret;
    }

    public static void loadLegends(String file)
    {
        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new FileInputStream(file));
            doc.getDocumentElement().normalize();

            NodeList siteList = doc.getElementsByTagName("site");

            for (int i = 0; i < siteList.getLength(); i++)
            {
                Node siteNode = siteList.item(i);
                int id = -1;
                String typeName = null;
                String name = null;
                String coords = null;
                for (int j = 0; j < siteNode.getChildNodes().getLength(); j++)
                {
                    Node node = siteNode.getChildNodes().item(j);
                    String nodeName = node.getNodeName();
                    if (nodeName.equals("id"))
                    {
                        id = Integer.parseInt(node.getFirstChild().getNodeValue());
                    }
                    if (nodeName.equals("name"))
                    {
                        name = node.getFirstChild().getNodeValue();
                    }
                    if (nodeName.equals("type"))
                    {
                        typeName = node.getFirstChild().getNodeValue();
                    }
                    if (nodeName.equals("coords"))
                    {
                        coords = node.getFirstChild().getNodeValue();
                    }
                }
                if (id == -1) continue;
                SiteType type = SiteType.getSite(typeName);
                String[] args = coords.split(",");
                int x = Integer.parseInt(args[0]);
                int z = Integer.parseInt(args[1]);
                Site site = new Site(name, id, type, x, z);
                if (sites.containsKey(id))
                {
                    BufferedImage image = sites.get(id);
                    site.rgbmap = new int[image.getWidth()][image.getHeight()];
                    for (x = 0; x < image.getWidth(); x++)
                    {
                        for (z = 0; z < image.getHeight(); z++)
                        {
                            site.rgbmap[x][z] = image.getRGB(x, z);
                        }
                    }
                    sites.remove(id);
                }
                DorfMap.sitesById.put(id, site);
            }

            NodeList regionList = doc.getElementsByTagName("region");
            for (int i = 0; i < regionList.getLength(); i++)
            {
                Node regionNode = regionList.item(i);
                int id = -1;
                String typeName = null;
                String name = null;
                for (int j = 0; j < regionNode.getChildNodes().getLength(); j++)
                {
                    Node node = regionNode.getChildNodes().item(j);
                    String nodeName = node.getNodeName();
                    if (nodeName.equals("id"))
                    {
                        id = Integer.parseInt(node.getFirstChild().getNodeValue());
                    }
                    if (nodeName.equals("name"))
                    {
                        name = node.getFirstChild().getNodeValue();
                    }
                    if (nodeName.equals("type"))
                    {
                        typeName = node.getFirstChild().getNodeValue();
                    }
                }
                Region region = new Region(id, name, RegionType.valueOf(typeName.toUpperCase()));
                DorfMap.regionsById.put(id, region);
            }

            regionList = doc.getElementsByTagName("underground_region");
            for (int i = 0; i < regionList.getLength(); i++)
            {
                Node regionNode = regionList.item(i);
                int id = -1;
                String typeName = null;
                String name = null;
                int depth = 0;
                for (int j = 0; j < regionNode.getChildNodes().getLength(); j++)
                {
                    Node node = regionNode.getChildNodes().item(j);
                    String nodeName = node.getNodeName();
                    if (nodeName.equals("id"))
                    {
                        id = Integer.parseInt(node.getFirstChild().getNodeValue());
                    }
                    if (nodeName.equals("name"))
                    {
                        name = node.getFirstChild().getNodeValue();
                    }
                    if (nodeName.equals("type"))
                    {
                        typeName = node.getFirstChild().getNodeValue();
                    }
                    if (nodeName.equals("depth"))
                    {
                        depth = Integer.parseInt(node.getFirstChild().getNodeValue());
                    }
                }

                Region region = new Region(id, name, depth, RegionType.valueOf(typeName.toUpperCase()));
                DorfMap.ugRegionsById.put(id, region);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void loadLegendsPlus(String file)
    {
        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new FileInputStream(file));
            doc.getDocumentElement().normalize();

            NodeList siteList = doc.getElementsByTagName("site");

            for (int i = 0; i < siteList.getLength(); i++)
            {
                Node siteNode = siteList.item(i);
                int id = -1;
                HashSet<Structure> toAdd = new HashSet<Structure>();
                for (int j = 0; j < siteNode.getChildNodes().getLength(); j++)
                {
                    Node node = siteNode.getChildNodes().item(j);
                    String nodeName = node.getNodeName();
                    if (nodeName.equals("id"))
                    {
                        id = Integer.parseInt(node.getFirstChild().getNodeValue());
                    }
                    if (nodeName.equals("structures"))
                    {
                        NodeList structures = node.getChildNodes();
                        for (int k = 0; k < structures.getLength(); k++)
                        {
                            Node structure = structures.item(k);
                            if (structure.getNodeName().equals("structure"))
                            {
                                NodeList structureList = structure.getChildNodes();
                                int structId = -1;
                                StructureType structType = null;
                                String name = "";
                                String name2 = "";

                                for (int l = 0; l < structureList.getLength(); l++)
                                {
                                    String subName = structureList.item(l).getNodeName();
                                    if (subName.equals("id"))
                                    {
                                        structId = Integer
                                                .parseInt(structureList.item(l).getFirstChild().getNodeValue());
                                    }
                                    if (subName.equals("type"))
                                    {
                                        String typeName = structureList.item(l).getFirstChild().getNodeValue();
                                        for (StructureType t : StructureType.values())
                                        {
                                            if (t.name.equals(typeName))
                                            {
                                                structType = t;
                                                break;
                                            }
                                        }
                                    }
                                    if (subName.equals("name"))
                                    {
                                        name = structureList.item(l).getFirstChild().getNodeValue();
                                    }
                                    if (subName.equals("name2"))
                                    {
                                        name2 = structureList.item(l).getFirstChild().getNodeValue();
                                    }
                                }
                                if (structId == -1) continue;
                                toAdd.add(new Structure(name, name2, structId, structType));
                            }
                        }
                    }
                }
                if (id == -1) continue;
                Site site = DorfMap.sitesById.get(id);
                if (site != null)
                {
                    site.structures.addAll(toAdd);
                }
                else
                {
                    new Exception().printStackTrace();
                }

            }

            NodeList regionList = doc.getElementsByTagName("region");
            for (int i = 0; i < regionList.getLength(); i++)
            {
                Node regionNode = regionList.item(i);
                int id = -1;
                String coords = "";
                String[] toAdd = null;
                for (int j = 0; j < regionNode.getChildNodes().getLength(); j++)
                {
                    Node node = regionNode.getChildNodes().item(j);
                    String nodeName = node.getNodeName();
                    if (nodeName.equals("id"))
                    {
                        id = Integer.parseInt(node.getFirstChild().getNodeValue());
                    }
                    if (nodeName.equals("coords"))
                    {
                        coords = node.getFirstChild().getNodeValue();
                    }
                }
                toAdd = coords.split("\\|");
                // System.out.println(toAdd.length+": "+coords.length()+":
                // "+coords);
                if (id != -1 && toAdd != null)
                {
                    Region region = DorfMap.regionsById.get(id);
                    if (region != null)
                    {
                        region.coords.clear();
                        for (String s : toAdd)
                        {
                            int x = Integer.parseInt(s.split(",")[0]);
                            int z = Integer.parseInt(s.split(",")[1]);
                            region.coords.add(x + 2048 * z + region.depth * 4194304);
                        }
                    }
                    else
                    {
                        new Exception().printStackTrace();
                    }
                }
            }

            regionList = doc.getElementsByTagName("underground_region");
            for (int i = 0; i < regionList.getLength(); i++)
            {
                Node regionNode = regionList.item(i);
                int id = -1;
                String coords = "";
                String[] toAdd = null;
                for (int j = 0; j < regionNode.getChildNodes().getLength(); j++)
                {
                    Node node = regionNode.getChildNodes().item(j);
                    String nodeName = node.getNodeName();
                    if (nodeName.equals("id"))
                    {
                        id = Integer.parseInt(node.getFirstChild().getNodeValue());
                    }
                    if (nodeName.equals("coords"))
                    {
                        coords = node.getFirstChild().getNodeValue();
                    }
                }
                toAdd = coords.split("\\|");
                // System.out.println(toAdd.length+": "+coords.length()+":
                // "+coords);
                if (id != -1 && toAdd != null)
                {
                    Region region = DorfMap.ugRegionsById.get(id);
                    if (region != null)
                    {
                        region.coords.clear();
                        for (String s : toAdd)
                        {
                            int x = Integer.parseInt(s.split(",")[0]);
                            int z = Integer.parseInt(s.split(",")[1]);
                            region.coords.add(x + 2048 * z + region.depth * 4194304);
                        }
                    }
                    else
                    {
                        new Exception().printStackTrace();
                    }
                }
            }

            regionList = doc.getElementsByTagName("world_construction");
            for (int i = 0; i < regionList.getLength(); i++)
            {
                Node regionNode = regionList.item(i);
                int id = -1;
                String name = "";
                String type = "";
                String coords = "";
                String[] toAdd = null;
                for (int j = 0; j < regionNode.getChildNodes().getLength(); j++)
                {
                    Node node = regionNode.getChildNodes().item(j);
                    String nodeName = node.getNodeName();
                    if (nodeName.equals("id"))
                    {
                        id = Integer.parseInt(node.getFirstChild().getNodeValue());
                    }
                    if (nodeName.equals("coords"))
                    {
                        coords = node.getFirstChild().getNodeValue();
                    }
                    if (nodeName.equals("name"))
                    {
                        name = node.getFirstChild().getNodeValue();
                    }
                    if (nodeName.equals("type"))
                    {
                        type = node.getFirstChild().getNodeValue();
                    }
                }
                toAdd = coords.split("\\|");
                // System.out.println(toAdd.length+": "+coords.length()+":
                // "+coords);
                if (id != -1 && toAdd != null)
                {
                    ConstructionType cType = ConstructionType.valueOf(type.toUpperCase());
                    if (cType != null)
                    {
                        WorldConstruction construct = new WorldConstruction(id, name, cType);
                        construct.worldCoords.clear();
                        for (String s : toAdd)
                        {
                            int x = Integer.parseInt(s.split(",")[0]);
                            int z = Integer.parseInt(s.split(",")[1]);
                            int index = x + 2048 * z;
                            construct.worldCoords.add(index);
                            HashSet<WorldConstruction> constructs = DorfMap.constructionsByCoord.get(index);
                            if (constructs == null)
                            {
                                constructs = new HashSet<>();
                                DorfMap.constructionsByCoord.put(index, constructs);
                            }
                            constructs.add(construct);
                        }
                        DorfMap.constructionsById.put(id, construct);
                    }
                    else
                    {
                        new NullPointerException("Unknown Construction Type").printStackTrace();
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void loadFineConstructLocations(String file)
    {
        ArrayList<String> rows = new ArrayList<String>();
        BufferedReader br = null;
        String line = "";

        try
        {
            InputStream res = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(res));
            while ((line = br.readLine()) != null)
            {
                rows.add(line);
            }

            for (String entry : rows)
            {
                String[] args = entry.split(":");
                int id = Integer.parseInt(args[0]);
                WorldConstruction construct = DorfMap.constructionsById.get(id);
                if (construct != null)
                {
                    for (int i = 1; i < args.length; i++)
                    {
                        String[] coordString = args[i].split(",");
                        int x = Integer.parseInt(coordString[0]);
                        int y = Integer.parseInt(coordString[1]);
                        int z = Integer.parseInt(coordString[2]);
                        int index = x + 8192 * z;
                        construct.embarkCoords.put(index, y);
                    }
                }
                else
                {
                    new NullPointerException("Cannot Find Construction for id:" + id).printStackTrace();
                }
            }
        }
        catch (Exception e)
        {

        }
    }

    public static void loadSiteInfo(String file)
    {
        ArrayList<String> rows = new ArrayList<String>();
        BufferedReader br = null;
        String line = "";

        try
        {
            InputStream res = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(res));
            int n = 0;
            while ((line = br.readLine()) != null)
            {
                rows.add(line);
            }

            for (String entry : rows)
            {
                String[] args = entry.split(":");
                int id = Integer.parseInt(args[0]);

                Site site = DorfMap.sitesById.get(id);

                if (site != null)
                {
                    String[] corner1 = args[1].split("->")[0].split(",");
                    int x1 = Integer.parseInt(corner1[0]);
                    int y1 = Integer.parseInt(corner1[1]);
                    String[] corner2 = args[1].split("->")[1].split(",");
                    int x2 = Integer.parseInt(corner2[0]);
                    int y2 = Integer.parseInt(corner2[1]);
                    site.setSiteLocation(x1, y1, x2, y2);
                    n++;
                }
                else
                {
                    new NullPointerException("Cannot Find Site for id:" + id).printStackTrace();
                }
            }
            System.out.println("Imported locations for " + n + " Sites");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void loadBiomes(String file)
    {
        ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try
        {
            InputStream res = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(res));
            int n = 0;
            while ((line = br.readLine()) != null)
            {

                String[] row = line.split(cvsSplitBy);
                rows.add(new ArrayList<String>());
                for (int i = 0; i < row.length; i++)
                {
                    rows.get(n).add(row[i]);
                }
                n++;
            }

        }
        catch (FileNotFoundException e)
        {

            FileWriter fwriter;
            PrintWriter out;
            try
            {
                fwriter = new FileWriter(file);
                out = new PrintWriter(fwriter);

                String defaultPath = "/assets/dorfgen/biomes.csv";
                ArrayList<String> lines = new ArrayList<String>();
                InputStream res;
                try
                {
                    res = getClass().getResourceAsStream(defaultPath);

                    br = new BufferedReader(new InputStreamReader(res));
                    int n = 0;

                    while ((line = br.readLine()) != null)
                    {
                        lines.add(line);
                        String[] row = line.split(cvsSplitBy);
                        rows.add(new ArrayList<String>());
                        for (int i = 0; i < row.length; i++)
                        {
                            rows.get(n).add(row[i]);
                        }
                        n++;
                    }
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }
                for (String s : lines)
                {

                    out.println(s);
                }

                out.close();
                fwriter.close();

            }
            catch (IOException e2)
            {
            }

        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        for (int i = 1; i < rows.size(); i++)
        {
            ArrayList<String> row = rows.get(i);
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
            catch (Exception e)
            {
            }
            if (row.size() > 4) biomeName = row.get(4);

            if (biomeId < 0)
            {
                Iterator<Biome> iterator = Biome.REGISTRY.iterator();
                while (iterator.hasNext())
                {
                    Biome biome = iterator.next();
                    if (biome != null
                            && biome.getBiomeName().replace(" ", "").equalsIgnoreCase(biomeName.replace(" ", "")))
                    {
                        biomeId = Biome.getIdForBiome(biome);
                        break;
                    }
                }
            }
            if (biomeId < 0)
            {
                System.out.println("Error in row " + i + " " + row);
                continue;
            }
            Color c = new Color(r, g, b);
            BiomeList.biomes.put(c.getRGB(), new BiomeConversion(c, Biome.getBiome(biomeId)));
        }

    }

}
