package pokecube.wiki;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.compat.Compat;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.PokedexEntry.SpawnData.SpawnEntry;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;
import thut.api.terrain.BiomeType;

public class PokecubeWikiWriter
{
    private static PrintWriter out;
    private static FileWriter  fwriter;

    static String              pokemobDir = "https://github.com/Thutmose/Pokecube/wiki/";
    static String              gifDir     = "https://raw.githubusercontent.com/wiki/Thutmose/Pokecube/pokemobs/img/";
    public static String       pagePrefix = "";

    static String formatLink(String link, String name)
    {
        return "[" + name + "](" + link + ")";
    }

    static String formatPokemobLink(PokedexEntry entry)
    {
        return formatLink(pokemobDir + pagePrefix + entry.getName(), entry.getTranslatedName());
    }

    // static String formatLink(String dir, String name, String ext, String
    // pref)
    // {
    // return "[" + name + "](" + dir + name + ext + ")";
    // }

    static void writeWiki()
    {
        pokemobDir = "https://github.com/Thutmose/Pokecube/wiki/";

        String code = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
        if (code.equals("en_US"))
        {
            pagePrefix = "";
        }
        else
        {
            pagePrefix = code + "-";
        }

        for (PokedexEntry entry : Database.baseFormes.values())
        {
            outputPokemonWikiInfo2(entry);
        }
        writeWikiPokemobList();
    }

    static void writeWikiPokemobList()
    {
        try
        {
            String fileName = Compat.CUSTOMSPAWNSFILE.replace("spawns.xml", pagePrefix + "pokemobList.md");
            fwriter = new FileWriter(fileName);
            out = new PrintWriter(fwriter);
            out.println("#" + I18n.format("list.pokemobs.title"));
            out.println("|  |  |  |  |");
            out.println("| --- | --- | --- | --- |");
            int n = 0;
            boolean ended = false;

            List<PokedexEntry> entries = Lists.newArrayList(Database.baseFormes.values());
            Collections.sort(entries, new Comparator<PokedexEntry>()
            {
                @Override
                public int compare(PokedexEntry o1, PokedexEntry o2)
                {
                    return o1.getPokedexNb() - o1.getPokedexNb();
                }
            });
            for (PokedexEntry e : entries)
            {
                if (e == null) continue;
                ended = false;
                out.print("| " + formatLink(pokemobDir + pagePrefix + e.getName(), e.getTranslatedName()));
                if (n % 4 == 3)
                {
                    out.print("| \n");
                    ended = true;
                }
                n++;
            }
            if (!ended)
            {
                out.print("| \n");
            }
            out.close();
            fwriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    static void outputPokemonWikiInfo2(PokedexEntry entry)
    {
        try
        {
            String fileName = Compat.CUSTOMSPAWNSFILE.replace("spawns.xml",
                    "pokemobs/" + pagePrefix + File.separator + pagePrefix + entry.getName() + ".md");
            File temp = new File(fileName.replace(pagePrefix + entry.getName() + ".md", ""));
            if (!temp.exists())
            {
                temp.mkdirs();
            }
            fwriter = new FileWriter(fileName);
            out = new PrintWriter(fwriter);
            String typeString = WordUtils.capitalize(PokeType.getTranslatedName(entry.getType1()));
            if (entry.getType2() != PokeType.unknown)
                typeString += "/" + WordUtils.capitalize(PokeType.getTranslatedName(entry.getType2()));

            // Print links to other pokemon
            PokedexEntry nex = Pokedex.getInstance().getNext(entry, 1);
            PokedexEntry pre = Pokedex.getInstance().getPrevious(entry, 1);
            out.println("| | | ");
            out.println("| --- | --- | ");
            String otherPokemon = "<- | ->";
            String next = "";
            if (nex != entry)
            {
                next = formatLink(pokemobDir + pagePrefix + nex.getName(), nex.getTranslatedName());
            }
            String prev = "";
            if (pre != entry)
            {
                prev = formatLink(pokemobDir + pagePrefix + pre.getName(), pre.getTranslatedName());
            }
            otherPokemon = "| " + prev + otherPokemon + next + " |";

            out.println(otherPokemon);

            // Print the name and header
            out.println("#" + entry.getTranslatedName());
            String numString = entry.getPokedexNb() + "";
            if (entry.getPokedexNb() < 10) numString = "00" + numString;
            else if (entry.getPokedexNb() < 100) numString = "0" + numString;
            out.println(" | ");
            out.println("--- | ---");
            out.println("| " + I18n.format("pokemob.type", typeString) + "\n" + I18n.format("pokemob.number", numString)
                    + "| \n");
            if (entry.hasShiny)
            {
                out.println("[[" + gifDir + entry.getName() + ".png]]" + "[[" + gifDir + entry.getName() + "S.png]]");
            }
            else
            {
                out.println("[[" + gifDir + entry.getName() + ".png]]");
            }

            // Print the description
            out.println("##" + I18n.format("pokemob.description.header"));
            out.println(I18n.format("pokemob.description.type", entry.getTranslatedName(), typeString));
            if (entry.canEvolve())
            {
                for (EvolutionData d : entry.evolutions)
                {
                    if (d.evolution == null) continue;
                    nex = d.evolution;
                    String evoLink = formatLink(pokemobDir + pagePrefix + nex.getName(), nex.getTranslatedName());
                    String evoString = null;
                    if (d.level > 0)
                    {
                        evoString = I18n.format("pokemob.description.evolve.level", entry.getTranslatedName(), evoLink,
                                d.level);
                    }
                    else if (d.item != null && d.gender == 0)
                    {
                        evoString = I18n.format("pokemob.description.evolve.item", entry.getTranslatedName(), evoLink,
                                d.item.getDisplayName());
                    }
                    else if (d.item != null && d.gender == 1)
                    {
                        evoString = I18n.format("pokemob.description.evolve.item.male", entry.getTranslatedName(),
                                evoLink, d.item.getDisplayName());
                    }
                    else if (d.item != null && d.gender == 2)
                    {
                        evoString = I18n.format("pokemob.description.evolve.item.female", entry.getTranslatedName(),
                                evoLink, d.item.getDisplayName());
                    }
                    else if (d.traded && d.item != null)
                    {
                        evoString = I18n.format("pokemob.description.evolve.traded.item", entry.getTranslatedName(),
                                evoLink, d.item.getDisplayName());
                    }
                    else if (d.happy)
                    {
                        evoString = I18n.format("pokemob.description.evolve.happy", entry.getTranslatedName(), evoLink);
                    }
                    else if (d.traded)
                    {
                        evoString = I18n.format("pokemob.description.evolve.traded", entry.getTranslatedName(),
                                evoLink);
                    }
                    else if (d.move != null && !d.move.isEmpty())
                    {
                        evoString = I18n.format("pokemob.description.evolve.move", entry.getTranslatedName(), evoLink,
                                MovesUtils.getMoveName(d.move));
                    }
                    if (evoString != null) out.print(evoString);
                }
            }
            if (entry.evolvesFrom != null)
            {
                String evoString = formatLink(pokemobDir + pagePrefix + entry.evolvesFrom.getName(),
                        entry.evolvesFrom.getTranslatedName());
                out.println(I18n.format("pokemob.description.evolve.from", entry.getTranslatedName(), evoString));
            }
            out.println();

            // Print move list
            out.println("##" + I18n.format("pokemob.movelist.title"));
            out.println(I18n.format("pokemob.movelist.header"));
            out.println("| --- | --- | ");
            List<String> moves = Lists.newArrayList(entry.getMoves());
            List<String> used = Lists.newArrayList();
            for (int i = 1; i <= 100; i++)
            {
                List<String> newMoves = entry.getMovesForLevel(i, i - 1);
                if (!newMoves.isEmpty())
                {
                    for (String s : newMoves)
                    {
                        out.println("| " + (i == 1 ? "-" : i) + "| " + MovesUtils.getMoveName(s) + "| ");
                        for (String s1 : moves)
                        {
                            if (s1.equalsIgnoreCase(s)) used.add(s1);
                        }
                    }
                }
            }
            moves.removeAll(used);

            if (moves.size() > 0)
            {
                out.println("##" + I18n.format("pokemob.tmlist.title"));
                out.println("|  |  |  |  |");
                out.println("| --- | --- | --- | --- |");
                boolean ended = false;
                int n = 0;
                for (String s : moves)
                {
                    ended = false;
                    out.print("| " + MovesUtils.getMoveName(s));
                    if (n % 4 == 3)
                    {
                        out.print("| \n");
                        ended = true;
                    }
                    n++;
                }
                if (!ended)
                {
                    out.print("| \n");
                }
            }
            if (!entry.related.isEmpty())
            {
                out.println("##" + I18n.format("pokemob.breedinglist.title"));
                out.println("|  |  |  |  |");
                out.println("| --- | --- | --- | --- |");
                int n = 0;
                boolean ended = false;
                for (PokedexEntry e : entry.related)
                {
                    if (e == null) continue;
                    ended = false;
                    out.print("| " + formatPokemobLink(e));
                    if (n % 4 == 3)
                    {
                        out.print("| \n");
                        ended = true;
                    }
                    n++;
                }
                if (!ended)
                {
                    out.print("| \n");
                }
            }
            SpawnData data = entry.getSpawnData();
            if (data == null && Database.getEntry(entry.getChildNb()) != null)
            {
                data = Database.getEntry(entry.getChildNb()).getSpawnData();
            }
            if (data != null)
            {
                out.println("##" + I18n.format("pokemob.biomeslist.title"));
                out.println("|  |  |  |  |");
                out.println("| --- | --- | --- | --- |");
                int n = 0;
                boolean ended = false;
                boolean hasBiomes = false;
                Map<SpawnBiomeMatcher, SpawnEntry> matchers = data.matchers;
                List<String> biomes = Lists.newArrayList();
                for (SpawnBiomeMatcher matcher : matchers.keySet())
                {
                    String biomeString = matcher.spawnRule.values.get(SpawnBiomeMatcher.BIOMES);
                    typeString = matcher.spawnRule.values.get(SpawnBiomeMatcher.TYPES);
                    if (biomeString != null) hasBiomes = true;
                    else if (typeString != null)
                    {
                        String[] args = typeString.split(",");
                        BiomeType subBiome = null;
                        for (String s : args)
                        {
                            for (BiomeType b : BiomeType.values())
                            {
                                if (b.name.replaceAll(" ", "").equalsIgnoreCase(s))
                                {
                                    subBiome = b;
                                    break;
                                }
                            }
                            if (subBiome == null) hasBiomes = true;
                            subBiome = null;
                            if (hasBiomes) break;
                        }
                    }
                    if (hasBiomes) break;
                }
                if (hasBiomes) for (ResourceLocation key : Biome.REGISTRY.getKeys())
                {
                    Biome b = Biome.REGISTRY.getObject(key);
                    if (b != null)
                    {
                        if (data.isValid(b)) biomes.add(b.getBiomeName());
                    }
                }
                for (BiomeType b : BiomeType.values())
                {
                    if (data.isValid(b))
                    {
                        biomes.add(b.readableName);
                    }
                }
                for (String s : biomes)
                {
                    ended = false;
                    out.print("| " + s);
                    if (n % 4 == 3)
                    {
                        out.print("| \n");
                        ended = true;
                    }
                    n++;
                }
                if (!ended)
                {
                    out.print("| \n");
                }
            }

            if (!entry.forms.isEmpty())
            {
                out.println("##" + I18n.format("pokemob.alternateformes.title"));
                for (PokedexEntry entry1 : entry.forms.values())
                {
                    typeString = WordUtils.capitalize(PokeType.getTranslatedName(entry1.getType1()));
                    if (entry1.getType2() != PokeType.unknown)
                        typeString += "/" + WordUtils.capitalize(PokeType.getTranslatedName(entry1.getType2()));
                    // Print the name and header
                    out.println("##" + entry1.getTranslatedName());
                    out.println("| |");
                    out.println("| --- |");
                    out.println("| " + I18n.format("pokemob.type", typeString) + " |");
                    if (entry1.hasShiny)
                    {
                        out.println("[[" + gifDir + entry1.getName() + ".png]]" + "[[" + gifDir + entry1.getName()
                                + "S.png]]");
                    }
                    else
                    {
                        out.println("[[" + gifDir + entry1.getName() + ".png]]");
                    }
                }
            }

            out.println(formatLink(pagePrefix + "pokemobList", I18n.format("list.pokemobs.link")) + "-------"
                    + formatLink(pagePrefix + "Home", I18n.format("home.link")) + "\n");
            out.close();
            fwriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static boolean            gifCaptureState;
    public static boolean             gifs           = true;
    private static int                currentCaptureFrame;
    private static int                currentPokemob = 1;
    private static int                numberTaken    = 1;
    private static int                WINDOW_XPOS    = 1;
    private static int                WINDOW_YPOS    = 1;
    private static int                WINDOW_WIDTH   = 200;
    private static int                WINDOW_HEIGHT  = 200;
    private static List<PokedexEntry> sortedEntries  = Lists.newArrayList();
    private static int                index          = 0;
    public static boolean             one            = false;

    static private void openPokedex()
    {
        Minecraft.getMinecraft().thePlayer.openGui(WikiWriteMod.instance, 0,
                Minecraft.getMinecraft().thePlayer.getEntityWorld(), 0, 0, 0);
    }

    static private void setPokedexBeginning()
    {
        if (!gifs)
        {
            index = 0;
            sortedEntries.clear();
            sortedEntries.addAll(Database.allFormes);
            Collections.sort(sortedEntries, new Comparator<PokedexEntry>()
            {
                @Override
                public int compare(PokedexEntry o1, PokedexEntry o2)
                {
                    int diff = o1.getPokedexNb() - o2.getPokedexNb();
                    if (diff == 0)
                    {
                        if (o1.base && !o2.base) diff = -1;
                        else if (o2.base && !o1.base) diff = 1;
                    }
                    return diff;
                }
            });
            return;
        }
        GuiGifCapture.pokedexEntry = Pokedex.getInstance().getEntry(1);

    }

    static private void cyclePokedex()
    {
        if (!gifs)
        {
            GuiGifCapture.pokedexEntry = sortedEntries.get(index++);
            return;
        }
        GuiGifCapture.pokedexEntry = Pokedex.getInstance().getNext(GuiGifCapture.pokedexEntry, 1);
        if (GuiGifCapture.pokedexEntry != null) currentPokemob = GuiGifCapture.pokedexEntry.getPokedexNb();
    }

    static public void beginGifCapture()
    {
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT && !gifCaptureState)
        {
            gifCaptureState = true;
            openPokedex();
            setPokedexBeginning();
            System.out.println("Beginning gif capture...");
        }
    }

    static public boolean isCapturingGif()
    {
        return gifCaptureState;
    }

    public static void setCaptureTarget(int number)
    {
        GuiGifCapture.pokedexEntry = Database.getEntry(number);
    }

    static public void doCapturePokemobGif()
    {
        if (gifCaptureState && FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            doCapturePokemobGifClient();
        }
    }

    static private void doCapturePokemobGifClient()
    {
        int h = Minecraft.getMinecraft().displayHeight;
        int w = Minecraft.getMinecraft().displayWidth;
        int x = w / 2;
        int y = h / 2;

        WINDOW_XPOS = -250;
        WINDOW_YPOS = -250;
        WINDOW_WIDTH = 120;
        WINDOW_HEIGHT = 120;
        int xb, yb;

        xb = GuiGifCapture.x;
        yb = GuiGifCapture.y;
        int width = WINDOW_WIDTH * w / xb;
        int height = WINDOW_HEIGHT * h / yb;

        x += WINDOW_XPOS;
        y += WINDOW_YPOS;
        if (GuiGifCapture.pokedexEntry != null) currentPokemob = GuiGifCapture.pokedexEntry.getPokedexNb();
        else return;
        String pokename = Compat.CUSTOMSPAWNSFILE.replace("spawns.xml",
                new String("img" + File.separator + currentPokemob + "_"));
        GL11.glReadBuffer(GL11.GL_FRONT);
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        GL11.glReadPixels(x, y, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        String currentFrameSuffix = new String();

        if (currentCaptureFrame < 10) currentFrameSuffix = "0";

        String shinysuffix = GuiGifCapture.shiny && GuiGifCapture.pokedexEntry.hasShiny ? "S" : "";

        currentFrameSuffix += currentCaptureFrame + shinysuffix + ".png";
        String fileName = pokename + currentFrameSuffix;
        if (!gifs) fileName = Compat.CUSTOMSPAWNSFILE.replace("spawns.xml",
                new String("img" + File.separator + GuiGifCapture.pokedexEntry.getName() + shinysuffix + ".png"));
        File file = new File(fileName);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                int k = (i + (width * j)) * 4;
                int r = buffer.get(k) & 0xFF;
                int g = buffer.get(k + 1) & 0xFF;
                int b = buffer.get(k + 2) & 0xFF;
                image.setRGB(i, height - (j + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }

        try
        {
            ImageIO.write(image, "png", file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        currentCaptureFrame++;
        if (Keyboard.isKeyDown(Keyboard.KEY_HOME))
        {
            currentPokemob = 1;
            numberTaken = 1;
            gifCaptureState = false;
            System.out.println("Gif capture Aborted!");
            return;
        }
        if (currentCaptureFrame > 28 || !gifs)
        {
            currentCaptureFrame = 0;
            numberTaken++;
            if ((gifs && numberTaken >= Pokedex.getInstance().getEntries().size())
                    || (!gifs && index >= sortedEntries.size()) || one)
            {
                currentPokemob = 1;
                numberTaken = 1;
                gifCaptureState = false;
                System.out.println("Gif capture complete!");
            }
            else cyclePokedex();
        }
    }
}