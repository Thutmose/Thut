package thut.lootcrates;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.oredict.OreDictionary;

public class XMLStuff
{
    static final QName NAME   = new QName("name");
    static final QName COLOUR = new QName("color");

    @XmlRootElement(name = "Item")
    public static class XMLItem
    {
        @XmlAnyAttribute
        Map<QName, String> values = Maps.newHashMap();
        @XmlElement(name = "Tag")
        String             tag;
    }

    @XmlRootElement(name = "Crates")
    public static class XMLCrates
    {
        @XmlElement(name = "Crate")
        private List<XMLCrate> items = Lists.newArrayList();
    }

    @XmlRootElement(name = "Crate")
    public static class XMLCrate
    {
        @XmlElement(name = "Reward")
        private List<XMLReward> items = Lists.newArrayList();
        @XmlElement(name = "Key")
        public XMLItem          key;
        @XmlAttribute
        public String           name;
    }

    @XmlRootElement(name = "Reward")
    public static class XMLReward
    {
        @XmlElement(name = "Item")
        private List<XMLItem> items    = Lists.newArrayList();
        @XmlElement(name = "Command")
        private List<String>  commands = Lists.newArrayList();
        @XmlAttribute
        public int            r;
    }

    public static class Crate
    {
        List<Reward>     rewards = Lists.newArrayList();
        public ItemStack key;

        public Crate(XMLCrate crate)
        {
            for (XMLReward reward : crate.items)
            {
                for (int i = 0; i < reward.r; i++)
                {
                    rewards.add(new Reward(reward));
                }
            }
            key = getStackFromXMLItem(crate.key);
            if (rewards.isEmpty() || key == null) throw new NullPointerException("Error with crate " + crate.name);

            TextFormatting colour = TextFormatting.RESET;
            if (crate.key.values.containsKey(COLOUR))
                colour = TextFormatting.getValueByName(crate.key.values.get(COLOUR));
            String name = crate.name;
            if (crate.key.values.containsKey(NAME)) name = crate.key.values.get(NAME);
            name = colour + name;
            key.setStackDisplayName(name);
            if (!key.hasTagCompound()) key.setTagCompound(new NBTTagCompound());
            key.getTagCompound().setString("key", crate.name);
        }

        public Reward getReward()
        {
            Collections.shuffle(rewards);
            return rewards.get(0);
        }
    }

    public static class Reward
    {
        List<ItemStack> rewards  = Lists.newArrayList();
        List<String>    commands = Lists.newArrayList();

        public Reward(XMLReward reward)
        {
            for (XMLItem item : reward.items)
            {
                ItemStack stack = getStackFromXMLItem(item);
                if (stack != null) rewards.add(stack);
                else throw new NullPointerException("Error with item for reward");
            }
            commands.addAll(reward.commands);
        }

        public ITextComponent giveRewards(EntityPlayer entityPlayer)
        {
            String message = "";
            for (int i = 0; i < rewards.size(); i++)
            {
                ItemStack reward = rewards.get(i);
                giveItem(entityPlayer, reward.copy());
                message = message + reward.getDisplayName() + " x" + CompatWrapper.getStackSize(reward);
                if (i < rewards.size() - 1) message = message + ", ";
            }
            for (String s : commands)
            {
                s = s.replace("@p", entityPlayer.getGameProfile().getName());
                s = s.replace("'x'", entityPlayer.posX + "");
                s = s.replace("'y'", (entityPlayer.posY + 1) + "");
                s = s.replace("'z'", entityPlayer.posZ + "");
                entityPlayer.getServer().getCommandManager().executeCommand(entityPlayer.getServer(), s);
            }
            return new TextComponentString(message);
        }
    }

    public static void giveItem(EntityPlayer entityplayer, ItemStack itemstack)
    {
        boolean flag = entityplayer.inventory.addItemStackToInventory(itemstack);
        if (flag)
        {
            entityplayer.worldObj.playSound((EntityPlayer) null, entityplayer.posX, entityplayer.posY,
                    entityplayer.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F,
                    ((entityplayer.getRNG().nextFloat() - entityplayer.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            entityplayer.inventoryContainer.detectAndSendChanges();
        }
        else
        {
            EntityItem entityitem = entityplayer.dropItem(itemstack, false);
            if (entityitem != null)
            {
                entityitem.setNoPickupDelay();
                entityitem.setOwner(entityplayer.getName());
            }
        }
    }

    public static XMLStuff    instance;

    public Map<String, Crate> map = Maps.newHashMap();
    final File                dir;

    public XMLStuff(FMLPreInitializationEvent event)
    {
        dir = event.getModConfigurationDirectory();
    }

    public void init()
    {
        File temp1 = new File(dir, "lootcrates.xml");
        try
        {
            JAXBContext jaxbContext = JAXBContext.newInstance(XMLCrates.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            FileReader reader = new FileReader(temp1);
            XMLCrates database = (XMLCrates) unmarshaller.unmarshal(reader);
            reader.close();
            map.clear();
            for (XMLCrate crate : database.items)
            {
                map.put(crate.name, new Crate(crate));
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
        out.println("<Crates>");
        out.println("    <Crate name=\"TestCrate1\">");
        out.println("        <Key id=\"minecraft:stick\"/>");
        out.println("        <Reward r=\"10\">");
        out.println("            <Item id=\"minecraft:redstone\"/>");
        out.println("            <Item id=\"minecraft:emerald\"/>");
        out.println("        </Reward>");
        out.println("        <Reward r=\"10\">");
        out.println("            <Item id=\"minecraft:diamond\"/>");
        out.println("            <Item id=\"minecraft:nether_star\"/>");
        out.println("        </Reward>");
        out.println("        <Reward r=\"5\">");
        out.println("            <Item id=\"minecraft:diamond\">");
        out.println("                <Tag>{key:\"TestCrate2\"}</Tag>");
        out.println("            </Item>");
        out.println("            <Item id=\"minecraft:nether_star\"/>");
        out.println("        </Reward>");
        out.println("    </Crate>");
        out.println("    <Crate name=\"TestCrate2\">");
        out.println("        <Key id=\"minecraft:stick\"/>");
        out.println("        <Reward r=\"10\">");
        out.println("            <Item id=\"minecraft:redstone\"/>");
        out.println("            <Item id=\"minecraft:emerald\"/>");
        out.println("        </Reward>");
        out.println("        <Reward r=\"10\">");
        out.println("            <Item id=\"minecraft:diamond\">");
        out.println("                <Tag>{key:\"TestCrate1\"}</Tag>");
        out.println("            </Item>");
        out.println("            <Item id=\"minecraft:nether_star\"/>");
        out.println("        </Reward>");
        out.println("    </Crate>");
        out.println("</Crates>");
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
            else if (key.toString().equals("n"))
            {
                size = Integer.parseInt(values.get(key));
            }
            else if (key.toString().equals("tag"))
            {
                tag = values.get(key);
            }
        }
        if (id.isEmpty()) return CompatWrapper.nullStack;
        resource = id.contains(":");
        ItemStack stack = CompatWrapper.nullStack;
        Item item = null;
        if (resource)
        {
            item = Item.REGISTRY.getObject(new ResourceLocation(id));
        }
        else
        {
            item = Item.REGISTRY.getObject(new ResourceLocation("minecraft:" + id));
        }
        if (item == null) return CompatWrapper.nullStack;
        if (meta == -1) meta = 0;
        if (!CompatWrapper.isValid(stack)) stack = new ItemStack(item, 1, meta);
        CompatWrapper.setStackSize(stack, size);
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
