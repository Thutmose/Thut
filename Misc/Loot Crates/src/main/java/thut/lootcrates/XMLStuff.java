package thut.lootcrates;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.registries.ForgeRegistries;

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
        private final List<XMLCrate> items = Lists.newArrayList();
    }

    @XmlRootElement(name = "Crate")
    public static class XMLCrate
    {
        @XmlElement(name = "Reward")
        private final List<XMLReward> items = Lists.newArrayList();
        @XmlElement(name = "Key")
        public XMLItem                key;
        @XmlAttribute
        public String                 name;
    }

    @XmlRootElement(name = "Reward")
    public static class XMLReward
    {
        @XmlElement(name = "Item")
        private final List<XMLItem> items    = Lists.newArrayList();
        @XmlElement(name = "Command")
        private final List<String>  commands = Lists.newArrayList();
        @XmlAttribute
        public int                  r;
        @XmlAttribute
        public float                p        = 0;
    }

    public static class Crate
    {
        List<Reward>     r_rewards = Lists.newArrayList();
        List<Reward>     p_rewards = Lists.newArrayList();
        public ItemStack key;

        public Crate(final XMLCrate crate)
        {
            for (final XMLReward reward : crate.items)
            {
                for (int i = 0; i < reward.r; i++)
                    this.r_rewards.add(new Reward(reward));
                if (reward.p > 0) this.p_rewards.add(new Reward(reward));
            }
            this.key = XMLStuff.getStackFromDrop(crate.key);
            if (this.r_rewards.isEmpty() && this.p_rewards.isEmpty() || this.key == null)
                throw new NullPointerException("Error with crate " + crate.name);

            TextFormatting colour = TextFormatting.RESET;
            if (crate.key.values.containsKey(XMLStuff.COLOUR)) colour = TextFormatting.getValueByName(crate.key.values
                    .get(XMLStuff.COLOUR));
            String name = crate.name;
            if (crate.key.values.containsKey(XMLStuff.NAME)) name = crate.key.values.get(XMLStuff.NAME);
            name = colour + name;
            this.key.setDisplayName(new StringTextComponent(name));
            if (!this.key.hasTag()) this.key.setTag(new CompoundNBT());
            this.key.getTag().putString("key", crate.name);
        }

        public ITextComponent getReward(final ServerPlayerEntity entityPlayer)
        {
            final Reward single = this.getSingleReward();
            if (single != null) return single.giveRewards(entityPlayer);
            ITextComponent message = new StringTextComponent("");
            final List<Reward> rewards = this.getRewards(new Random());
            int n = 0;
            for (final Reward r : rewards)
            {
                final ITextComponent part = r.giveRewards(entityPlayer);
                if (!part.getUnformattedComponentText().isEmpty())
                {
                    if (n > 0) message.appendSibling(new StringTextComponent(", "));
                    message.appendSibling(part);
                }
                else n--;
                n++;
            }
            if (rewards.isEmpty()) message = new StringTextComponent("Nothing!");
            return message;
        }

        public List<Reward> getRewards(final Random rand)
        {
            final List<Reward> ret = Lists.newArrayList();
            for (final Reward r : this.p_rewards)
                if (r.p > rand.nextFloat()) ret.add(r);
            return ret;
        }

        public Reward getSingleReward()
        {
            if (this.r_rewards.isEmpty()) return null;
            Collections.shuffle(this.r_rewards);
            return this.r_rewards.get(0);
        }
    }

    public static class Reward
    {
        List<ItemStack> rewards  = Lists.newArrayList();
        List<String>    commands = Lists.newArrayList();
        float           p        = 0;

        public Reward(final XMLReward reward)
        {
            this.p = reward.p;
            for (final XMLItem item : reward.items)
            {
                final ItemStack stack = XMLStuff.getStackFromDrop(item);
                if (stack != null) this.rewards.add(stack);
                else throw new NullPointerException("Error with item for reward");
            }
            this.commands.addAll(reward.commands);
        }

        public ITextComponent giveRewards(final ServerPlayerEntity entityPlayer)
        {
            String message = "";
            for (int i = 0; i < this.rewards.size(); i++)
            {
                final ItemStack reward = this.rewards.get(i);
                XMLStuff.giveItem(entityPlayer, reward.copy());
                message = message + reward.getDisplayName().getFormattedText() + " x" + reward.getCount();
                if (i < this.rewards.size() - 1) message = message + ", ";
            }
            for (String s : this.commands)
            {
                s = s.replace("@p", entityPlayer.getGameProfile().getName());
                s = s.replace("'x'", entityPlayer.posX + "");
                s = s.replace("'y'", entityPlayer.posY + 1 + "");
                s = s.replace("'z'", entityPlayer.posZ + "");
                // Send the commands as the server.
                entityPlayer.getServer().getCommandManager().handleCommand(entityPlayer.getServer().getCommandSource(),
                        s);
            }
            return new StringTextComponent(message);
        }
    }

    public static void giveItem(final ServerPlayerEntity entityplayer, final ItemStack itemstack)
    {
        final boolean flag = entityplayer.inventory.addItemStackToInventory(itemstack);
        if (flag)
        {
            entityplayer.getEntityWorld().playSound((ServerPlayerEntity) null, entityplayer.posX, entityplayer.posY,
                    entityplayer.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((entityplayer
                            .getRNG().nextFloat() - entityplayer.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            entityplayer.container.detectAndSendChanges();
        }
        else
        {
            final ItemEntity entityitem = entityplayer.dropItem(itemstack, false);
            if (entityitem != null)
            {
                entityitem.setNoPickupDelay();
                entityitem.setOwnerId(entityplayer.getUniqueID());
            }
        }
    }

    public static XMLStuff instance;

    public Map<String, Crate> map = Maps.newHashMap();

    public void init()
    {
        final File temp1 = new File(LootCrates.dir, "lootcrates.xml");
        try
        {
            final JAXBContext jaxbContext = JAXBContext.newInstance(XMLCrates.class);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            final FileReader reader = new FileReader(temp1);
            final XMLCrates database = (XMLCrates) unmarshaller.unmarshal(reader);
            reader.close();
            this.map.clear();
            for (final XMLCrate crate : database.items)
                this.map.put(crate.name, new Crate(crate));
        }
        catch (final FileNotFoundException e)
        {
            try
            {
                this.writeDefault(temp1);
                this.init();
            }
            catch (final Exception e1)
            {
                e1.printStackTrace();
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    private void writeDefault(final File file) throws Exception
    {
        final FileWriter fwriter = new FileWriter(file);
        final PrintWriter out = new PrintWriter(fwriter);
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

    public static ItemStack getStackFromDrop(final XMLItem d)
    {
        final Map<QName, String> values = d.values;
        if (d.tag != null)
        {
            final QName name = new QName("tag");
            values.put(name, d.tag);
        }
        return XMLStuff.getStack(d.values);
    }

    public static boolean isSameStack(final ItemStack a, final ItemStack b)
    {
        return XMLStuff.isSameStack(a, b, false);
    }

    public static boolean isSameStack(final ItemStack a, final ItemStack b, final boolean strict)
    {
        // TODO determine if to use the tags?
        return ItemStack.areItemsEqualIgnoreDurability(a, b);
    }

    public static ItemStack getStack(final Map<QName, String> values)
    {
        String id = "";
        int size = 1;
        String tag = "";

        for (final QName key : values.keySet())
            if (key.toString().equals("id")) id = values.get(key);
            else if (key.toString().equals("n")) size = Integer.parseInt(values.get(key));
            else if (key.toString().equals("tag")) tag = values.get(key).trim();
        if (id.isEmpty()) return ItemStack.EMPTY;
        final ResourceLocation loc = new ResourceLocation(id);
        ItemStack stack = ItemStack.EMPTY;
        Item item = ForgeRegistries.ITEMS.getValue(loc);
        if (item == null)
        {
            final Tag<Item> tags = ItemTags.getCollection().get(loc);
            if (tags != null)
            {
                item = tags.getRandomElement(new Random(2));
                if (item != null) return new ItemStack(item);
            }
        }
        if (item == null) return ItemStack.EMPTY;
        if (stack.isEmpty()) stack = new ItemStack(item, 1);
        stack.setCount(size);
        if (!tag.isEmpty()) try
        {
            stack.setTag(JsonToNBT.getTagFromJson(tag));
        }
        catch (final CommandSyntaxException e)
        {
            e.printStackTrace();
        }
        return stack;
    }
}
