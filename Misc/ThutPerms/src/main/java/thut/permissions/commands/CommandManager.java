package thut.permissions.commands;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public class CommandManager
{
    public static class ClassFinder
    {

        private static final char   DOT               = '.';

        private static final char   SLASH             = '/';

        private static final String CLASS_SUFFIX      = ".class";

        private static final String BAD_PACKAGE_ERROR = "Unable to get resources from path '%s'. Are you sure the package '%s' exists?";

        public static List<Class<?>> find(String scannedPackage) throws UnsupportedEncodingException
        {
            String scannedPath = scannedPackage.replace(DOT, SLASH);
            URL scannedUrl = Thread.currentThread().getContextClassLoader().getResource(scannedPath);
            if (scannedUrl == null) { throw new IllegalArgumentException(
                    String.format(BAD_PACKAGE_ERROR, scannedPath, scannedPackage)); }
            File scannedDir = new File(
                    java.net.URLDecoder.decode(scannedUrl.getFile(), Charset.defaultCharset().name()));

            List<Class<?>> classes = new ArrayList<Class<?>>();
            if (scannedDir.exists()) for (File file : scannedDir.listFiles())
            {
                classes.addAll(findInFolder(file, scannedPackage));
            }
            else if (scannedDir.toString().contains("file:") && scannedDir.toString().contains(".jar"))
            {
                String name = scannedDir.toString();
                String pack = name.split("!")[1].replace(File.separatorChar, SLASH).substring(1) + SLASH;
                name = name.replace("file:", "");
                name = name.replaceAll("(.jar)(.*)", ".jar");
                scannedDir = new File(name);
                try
                {
                    ZipFile zip = new ZipFile(scannedDir);
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    int n = 0;
                    while (entries.hasMoreElements() && n < 10)
                    {
                        ZipEntry entry = entries.nextElement();
                        String s = entry.getName();
                        if (s.contains(pack) && s.endsWith(CLASS_SUFFIX))
                        {
                            try
                            {
                                classes.add(Class.forName(s.replace(CLASS_SUFFIX, "").replace(SLASH, DOT)));
                            }
                            catch (ClassNotFoundException ignore)
                            {
                            }
                        }
                    }
                    zip.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return classes;
        }

        private static List<Class<?>> findInFolder(File file, String scannedPackage)
        {
            List<Class<?>> classes = new ArrayList<Class<?>>();
            String resource = scannedPackage + DOT + file.getName();
            if (file.isDirectory())
            {
                for (File child : file.listFiles())
                {
                    classes.addAll(findInFolder(child, resource));
                }
            }
            else if (resource.endsWith(CLASS_SUFFIX))
            {
                int endIndex = resource.length() - CLASS_SUFFIX.length();
                String className = resource.substring(0, endIndex);
                try
                {
                    classes.add(Class.forName(className));
                }
                catch (ClassNotFoundException ignore)
                {
                }
            }
            return classes;
        }

    }

    public static ITextComponent makeFormattedComponent(String text, TextFormatting colour, boolean bold)
    {
        return new TextComponentString(text).setStyle(new Style().setBold(bold).setColor(colour));
    }

    public static ITextComponent makeFormattedComponent(String text, TextFormatting colour)
    {
        return new TextComponentString(text).setStyle(new Style().setColor(colour));
    }

    public static ITextComponent makeFormattedCommandLink(String text, String command, TextFormatting colour,
            boolean bold)
    {
        return new TextComponentString(text).setStyle(
                new Style().setBold(bold).setColor(colour).setClickEvent(new ClickEvent(Action.RUN_COMMAND, command)));
    }

    public static boolean isOp(ICommandSender sender)
    {
        if (FMLCommonHandler.instance().getMinecraftServerInstance() != null
                && !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer()) { return true; }

        if (sender instanceof EntityPlayer)
        {
            EntityPlayer player = sender.getEntityWorld().getPlayerEntityByName(sender.getName());
            UserListOpsEntry userentry = ((EntityPlayerMP) player).mcServer.getPlayerList().getOppedPlayers()
                    .getEntry(player.getGameProfile());
            return userentry != null && userentry.getPermissionLevel() >= 4;
        }
        else if (sender instanceof TileEntityCommandBlock) { return true; }
        return sender.getName().equalsIgnoreCase("@") || sender.getName().equals("Server");
    }

    public static Map<String, List<String>> commands = Maps.newHashMap();

    static
    {
        commands.put("colour", Lists.newArrayList("colour", "color", "col"));
        commands.put("tpa", Lists.newArrayList("tpa"));
        commands.put("tptoggle", Lists.newArrayList("tptoggle"));
        commands.put("fly", Lists.newArrayList("fly"));
        commands.put("spawn", Lists.newArrayList("spawn"));
        commands.put("back", Lists.newArrayList("back"));

        commands.put("staff", Lists.newArrayList("staff", "sc"));

        commands.put("heal", Lists.newArrayList("heal"));
        commands.put("repair", Lists.newArrayList("repair"));

        // Warp related
        commands.put("setwarp", Lists.newArrayList("setwarp"));
        commands.put("delwarp", Lists.newArrayList("delwarp"));
        commands.put("warp", Lists.newArrayList("warp"));

        // Home related
        commands.put("sethome", Lists.newArrayList("sethome"));
        commands.put("delhome", Lists.newArrayList("delhome"));
        commands.put("home", Lists.newArrayList("home"));

        // Rules
        commands.put("rules", Lists.newArrayList("rules"));
        commands.put("addrule", Lists.newArrayList("addrule"));
        commands.put("delrule", Lists.newArrayList("delrule"));
    }

    public CommandManager(FMLServerStartingEvent event)
    {
        List<Class<?>> foundClasses;
        // Register moves.
        try
        {
            foundClasses = ClassFinder.find(CommandManager.class.getPackage().getName());
            for (Class<?> candidateClass : foundClasses)
            {
                if (CommandBase.class.isAssignableFrom(candidateClass) && candidateClass.getEnclosingClass() == null)
                {
                    CommandBase move = null;
                    try
                    {
                        move = (CommandBase) candidateClass.getConstructor().newInstance();
                    }
                    catch (Exception e)
                    {
                        System.out.println("Error with " + candidateClass);
                    }
                    if (move != null && move.getCommandName() != null)
                    {
                        event.registerServerCommand(move);
                    }
                    else
                    {
                        System.err.println(candidateClass + " is not completed.");
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
