package thut.bling.recipe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.oredict.OreDictionary;

public class RecipeLoader
{
    static final QName TEX = new QName("tex");

    @XmlRootElement(name = "Item")
    public static class XMLItem
    {
        @XmlAnyAttribute
        Map<QName, String> values = Maps.newHashMap();
        @XmlElement(name = "tag")
        String             tag;
    }

    @XmlRootElement(name = "Items")
    public static class XMLStarterItems
    {
        @XmlElement(name = "Item")
        private List<XMLItem> items = Lists.newArrayList();
    }

    public static RecipeLoader    instance;

    public Map<ItemStack, String> knownTextures = Maps.newHashMap();
    final File                    dir;

    public RecipeLoader(FMLPreInitializationEvent event)
    {
        dir = event.getModConfigurationDirectory();
    }

    public void init()
    {
        File temp1 = new File(dir, "thut_bling.xml");
        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(XMLStarterItems.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            FileReader reader = new FileReader(temp1);
            XMLStarterItems database = (XMLStarterItems) unmarshaller.unmarshal(reader);
            reader.close();
            for (XMLItem drop : database.items)
            {
                ItemStack stack = getStackFromXMLItem(drop);
                if (stack != null)
                {
                    String tex = drop.values.get(TEX);
                    if (tex == null)
                    {
                        ResourceLocation location;
                        Block block = Block.getBlockFromItem(stack.getItem());
                        if (block == null)
                        {
                            location = stack.getItem().getRegistryName();
                            tex = location.getResourceDomain() + ":textures/items/" + location.getResourcePath()
                                    + ".png";
                        }
                        else
                        {
                            location = block.getRegistryName();
                            tex = location.getResourceDomain() + ":textures/blocks/" + location.getResourcePath()
                                    + ".png";
                        }
                    }
                    knownTextures.put(stack, tex);
                }
            }
        }
        catch (FileNotFoundException e)
        {
            try
            {
                writeDefault(temp1);
                init();
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void writeDefault(File file) throws Exception
    {
        FileWriter fwriter = new FileWriter(file);
        PrintWriter out = new PrintWriter(fwriter);
        out.println("<?xml version=\"1.0\"?>");
        out.println("<Items>");
        out.println("    <Item id=\"minecraft:redstone\" tex=\"minecraft:textures/items/redstone_dust.png\"/>");
        out.println("    <Item id=\"minecraft:diamond\"/>");
        out.println("    <Item id=\"minecraft:emerald\"/>");
        out.println("    <Item id=\"minecraft:nether_star\"/>");
        out.println("</Items>");
        out.close();
        fwriter.close();
    }

    public static ItemStack getStackFromXMLItem(XMLItem d)
    {
        Map<QName, String> values = d.values;
        if (d.tag != null)
        {
            QName name = new QName("tag");
            values.put(name, d.tag);
        }
        return getStack(d.values);
    }

    public static boolean isSameStack(ItemStack a, ItemStack b)
    {
        if ((a == null || a.getItem() == null) || (b == null || b.getItem() == null)) return false;
        int[] aID = OreDictionary.getOreIDs(a);
        int[] bID = OreDictionary.getOreIDs(b);
        boolean check = a.getItem() == b.getItem();
        if (!check)
        {
            outer:
            for (int i : aID)
            {
                for (int i1 : bID)
                {
                    if (i == i1)
                    {
                        check = true;
                        break outer;
                    }
                }
            }
        }
        if (!check) { return false; }
        check = (!a.isItemStackDamageable() && a.getItemDamage() != b.getItemDamage());
        if (!a.isItemStackDamageable() && (a.getItemDamage() == OreDictionary.WILDCARD_VALUE
                || b.getItemDamage() == OreDictionary.WILDCARD_VALUE))
            check = false;
        if (check) return false;
        NBTBase tag;
        if (a.hasTagCompound() && ((tag = a.getTagCompound().getTag("ForgeCaps")) != null) && tag.hasNoTags())
        {
            a.getTagCompound().removeTag("ForgeCaps");
        }
        if (b.hasTagCompound() && ((tag = b.getTagCompound().getTag("ForgeCaps")) != null) && tag.hasNoTags())
        {
            b.getTagCompound().removeTag("ForgeCaps");
        }
        return ItemStack.areItemStackTagsEqual(a, b);
    }

    public static ItemStack getStack(Map<QName, String> values)
    {
        int meta = -1;
        String id = "";
        int size = 1;
        boolean resource = false;
        String tag = "";

        for (QName key : values.keySet())
        {
            if (key.toString().equals("id"))
            {
                id = values.get(key);
            }
            else if (key.toString().equals("d"))
            {
                meta = Integer.parseInt(values.get(key));
            }
            else if (key.toString().equals("tag"))
            {
                tag = values.get(key);
            }
        }
        if (id.isEmpty()) return null;
        resource = id.contains(":");
        ItemStack stack = null;
        Item item = null;
        if (resource)
        {
            item = Item.REGISTRY.getObject(new ResourceLocation(id));
        }
        else
        {
            item = Item.REGISTRY.getObject(new ResourceLocation("minecraft:" + id));
        }
        if (item == null) return null;
        if (meta == -1) meta = 0;
        stack = new ItemStack(item, 1, meta);
        stack.stackSize = size;
        if (!tag.isEmpty())
        {
            try
            {
                stack.setTagCompound(JsonToNBT.getTagFromJson(tag));
            }
            catch (NBTException e)
            {
                e.printStackTrace();
            }
        }
        return stack;
    }
}
