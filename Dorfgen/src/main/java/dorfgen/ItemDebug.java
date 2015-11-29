package dorfgen;

import static dorfgen.WorldGenerator.scale;

import java.util.HashSet;

import dorfgen.conversion.DorfMap;
import dorfgen.conversion.DorfMap.Region;
import dorfgen.conversion.DorfMap.Site;
import dorfgen.conversion.SiteStructureGenerator.SiteStructures;
import dorfgen.conversion.SiteStructureGenerator.StructureSpace;
import dorfgen.conversion.SiteStructureGenerator.WallSegment;
import dorfgen.worldgen.RiverMaker;
import dorfgen.worldgen.WorldConstructionMaker;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class ItemDebug extends Item
{

    public ItemDebug()
    {
        super();
        this.setUnlocalizedName("debug");
        this.setCreativeTab(CreativeTabs.tabTools);
    }

    @SuppressWarnings("unused")
    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
    {

        int x = MathHelper.floor_double(player.posX);
        int y = (int) player.posY;
        int z = MathHelper.floor_double(player.posZ);

        if (world.isRemote) return itemstack;

        DorfMap dorfs = WorldGenerator.instance.dorfs;
        int n = 0;
        Region region = dorfs.getRegionForCoords(x, z);
        HashSet<Site> sites = dorfs.getSiteForCoords(x, z);
        String mess = "";
        if (region != null)
        {
            mess += region.name + " " + region.type + " ";
        }
        WorldConstructionMaker maker = new WorldConstructionMaker();

        int h = WorldConstructionMaker.bicubicInterpolator.interpolate(WorldGenerator.instance.dorfs.elevationMap, x, z, scale);
        int r = WorldConstructionMaker.bicubicInterpolator.interpolate(WorldGenerator.instance.dorfs.riverMap, x, z, scale);
        mess = "";
        //
        int embarkX = (x / scale);
        int embarkZ = (z / scale);
        Site site = null;
        if (sites != null)
        {
            for (Site s : sites)
            {
                site = s;
                mess += site;
            }
        }

        int kx = x / (scale);
        int kz = z / (scale);
        int key = kx + 8192 * kz;

        boolean middle = false;
        HashSet<Site> ret = DorfMap.sitesByCoord.get(key);
        boolean hasRivers = false;
        if (site != null && site.rgbmap != null)
        {
            SiteStructures structures = WorldGenerator.instance.structureGen.getStructuresForSite(site);

            StructureSpace space = structures.getStructure(x, z, scale);
            WallSegment wall = structures.getWall(x, z, scale);
            System.out.println(space + " " + wall);
            if (wall != null)
            {
                boolean surrounded = true;
                boolean nearStruct = false;
                if (!nearStruct)
                {
                    nearStruct = structures.isStructure(x - 1, z, scale);
                    if (nearStruct)
                    {
                        boolean t1 = !wall.isInWall(site, x, z - 1, scale);
                        boolean t2 = !wall.isInWall(site, x, z + 1, scale);
                        surrounded = !(t1 || t2);
                    }
                }
                if (!nearStruct)
                {
                    nearStruct = structures.isStructure(x + 1, z, scale);
                    if (nearStruct)
                    {
                        boolean t1 = !wall.isInWall(site, x, z - 1, scale);
                        boolean t2 = !wall.isInWall(site, x, z + 1, scale);
                        surrounded = !(t1 || t2);
                    }
                }
                if (!nearStruct)
                {
                    nearStruct = structures.isStructure(x, z - 1, scale);
                    if (nearStruct)
                    {
                        boolean t1 = !wall.isInWall(site, x - 1, z, scale);
                        boolean t2 = !wall.isInWall(site, x + 1, z, scale);
                        surrounded = !(t1 || t2);
                    }
                }
                if (!nearStruct)
                {
                    nearStruct = structures.isStructure(x, z + 1, scale);
                    if (nearStruct)
                    {
                        boolean t1 = !wall.isInWall(site, x - 1, z, scale);
                        boolean t2 = !wall.isInWall(site, x + 1, z, scale);
                        surrounded = !(t1 || t2);
                    }
                }
                if (!nearStruct)
                {
                    // if(WorldGenerator.scale/SiteStructureGenerator.SITETOBLOCK
                    // > 1)
                    {
                        if (surrounded) surrounded = wall.isInWall(site, x - 1, z - 1, scale);
                        if (surrounded) surrounded = wall.isInWall(site, x + 1, z - 1, scale);
                        if (surrounded) surrounded = wall.isInWall(site, x - 1, z + 1, scale);
                        if (surrounded) surrounded = wall.isInWall(site, x + 1, z + 1, scale);
                    }
                    // else
                    // {
                    //// if(surrounded)
                    //// surrounded = wall.isInWall(site, x - 1, z, scale);
                    //// if(surrounded)
                    //// surrounded = wall.isInWall(site, x + 1, z, scale);
                    //// if(surrounded)
                    //// surrounded = wall.isInWall(site, x, z + 1, scale);
                    //// if(surrounded)
                    //// surrounded = wall.isInWall(site, x, z + 1, scale);
                    // }
                }
                // System.out.println("surrounded "+surrounded);
                middle = surrounded;
            }

        }
        if (site != null)
        {
            // embarkX = (x/scale)*scale;
            // embarkZ = (z/scale)*scale;
            //
            // if(embarkX/scale != site.x || embarkZ/scale != site.z)
            // {
            // middle = false;
            // }
            // else
            // {
            // int relX = x%scale + 8;
            // int relZ = z%scale + 8;
            // middle = relX/16 == scale/32 && relZ/16 == scale/32;
            // System.out.println(relX+" "+relZ);
            // }
            // System.out.println(MapGenSites.shouldSiteSpawn(x, z, site));
        }

        int biome = dorfs.biomeMap[kx][kz];
        mess += " " + middle;
//        if(site!=null)
//        mess += Arrays.toString(WorldConstructionMaker.getClosestRoadEnd(x, z, site));
         mess += " In a River: "+RiverMaker.isInRiver(x, z)+" "+dorfs.riverMap[kx][kz];

        player.addChatMessage(new ChatComponentText(mess));

        return itemstack;
    }

}
