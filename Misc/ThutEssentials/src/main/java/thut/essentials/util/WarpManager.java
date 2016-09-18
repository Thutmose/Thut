package thut.essentials.util;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.ClickEvent.Action;

public class WarpManager
{
    public static Map<String, int[]> warpLocs;
    final static Field               warpsField;

    static
    {
        Field temp = null;
        try
        {
            temp = ConfigManager.class.getDeclaredField("warps");
        }
        catch (SecurityException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        warpsField = temp;
    }

    static void init()
    {
        warpLocs = Maps.newHashMap();
        for (String s : ConfigManager.INSTANCE.warps)
        {
            String[] args = s.split(":");
            warpLocs.put(args[0], getInt(args[1]));
        }
    }

    static int[] getInt(String val)
    {
        String[] args = val.split(" ");
        int dim = args.length == 4 ? Integer.parseInt(args[3]) : 0;
        return new int[] { Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), dim };
    }

    public static void setWarp(BlockPos center, int dimension, String name) throws Exception
    {
        List<String> warps = Lists.newArrayList(ConfigManager.INSTANCE.warps);
        for (String s : warps)
        {
            String[] args = s.split(":");
            if (args[0].equals(s)) { throw new CommandException(
                    "Requested warp already exits, try /delwarp " + name + " to remove it"); }
        }
        String warp = name + ":" + center.getX() + " " + center.getY() + " " + center.getZ() + " " + dimension;
        warps.add(warp);
        warpLocs.put(name, new int[] { center.getX(), center.getY(), center.getZ(), dimension });
        ConfigManager.INSTANCE.updateField(warpsField, warps.toArray(new String[0]));
    }

    public static void delWarp(String name) throws Exception
    {
        List<String> warps = Lists.newArrayList(ConfigManager.INSTANCE.warps);
        for (String s : warps)
        {
            String[] args = s.split(":");
            if (args[0].equals(name))
            {
                warps.remove(s);
                warpLocs.remove(name);
                ConfigManager.INSTANCE.updateField(warpsField, warps.toArray(new String[0]));
                return;
            }
        }
        throw new CommandException("Warp " + name + " does not exist");
    }

    public static int[] getWarp(String name)
    {
        return warpLocs.get(name);
    }

    public static void sendWarpsList(EntityPlayer player)
    {
        player.addChatMessage(new TextComponentString("================"));
        player.addChatMessage(new TextComponentString("      Warps     "));
        player.addChatMessage(new TextComponentString("================"));
        for (String s : warpLocs.keySet())
        {
            Style style = new Style();
            style.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/warp " + s));
            player.addChatMessage(new TextComponentString(s).setStyle(style));
        }
        player.addChatMessage(new TextComponentString("================"));
    }
}
