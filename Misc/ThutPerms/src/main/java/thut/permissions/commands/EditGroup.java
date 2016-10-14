package thut.permissions.commands;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import thut.permissions.Group;
import thut.permissions.GroupManager;
import thut.permissions.ThutPerms;
import thut.permissions.util.BaseCommand;

public class EditGroup extends BaseCommand
{
    Map<String, TextFormatting> charCodeMap = Maps.newHashMap();

    String format(String input)
    {
        boolean done = false;
        int index = 0;
        while (!done && index < input.length() && index >= 0)
        {
            try
            {
                done = !input.contains("&");
                index = input.indexOf('&', index);
                if (index < input.length() - 1 && index >= 0)
                {
                    String toReplace = input.substring(index, index + 2);
                    String num = toReplace.replace("&", "");
                    TextFormatting format = charCodeMap.get(num);
                    if (format != null) input = input.replaceAll(toReplace, format + "");
                    else index++;
                }
                else
                {
                    done = true;
                }
            }
            catch (Exception e)
            {
                done = true;
                e.printStackTrace();
            }
        }
        return input;
    }

    public EditGroup()
    {
        Field temp = ReflectionHelper.findField(TextFormatting.class, "formattingCode", "field_96329_z", "z");
        temp.setAccessible(true);
        for (TextFormatting format : TextFormatting.values())
        {
            try
            {
                char code = temp.getChar(format);
                charCodeMap.put(code + "", format);
            }
            catch (IllegalArgumentException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getCommandName()
    {
        return "editGroup";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/editGroup";
    }

    /** Return whether the specified command parameter index is a username
     * parameter. */
    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        if (args[0].equalsIgnoreCase("add")) return index == 1;
        return false;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 3 && args[0].equalsIgnoreCase("add"))
        {
            String groupName = args[2];
            String playerName = args[1];
            Group g = ThutPerms.getGroup(groupName);
            if (g == null) { throw new CommandException("Error, specified Group does not exist."); }
            GameProfile profile = new GameProfile(null, playerName);
            profile = TileEntitySkull.updateGameprofile(profile);
            if (profile.getId() == null) { throw new CommandException("Error, cannot find profile for " + playerName); }
            Group old = GroupManager.instance.getPlayerGroup(profile.getId());
            if (old != null) old.members.remove(profile.getId());
            GroupManager.instance.groupIDMap.remove(profile.getId());
            ThutPerms.addToGroup(profile.getId(), groupName);
            sender.addChatMessage(new TextComponentString("Added " + playerName + " to " + groupName));
            ThutPerms.savePerms();
            return;
        }
        else if (args.length == 2 && args[1].equals("reset"))
        {
            String groupName = args[0];
            Group g = ThutPerms.getGroup(groupName);
            if (g == null) { throw new CommandException("Error, Group not found, please create it first."); }
            g.allowedCommands.clear();
            g.all = false;
            for (ICommand command : FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager()
                    .getCommands().values())
            {
                if (command instanceof CommandBase)
                {
                    CommandBase base = (CommandBase) command;
                    if (base.getRequiredPermissionLevel() <= 0)
                    {
                        g.allowedCommands.add(command.getClass().getName());
                    }
                }
            }
            sender.addChatMessage(new TextComponentString("Reset Permissions for " + groupName));
            ThutPerms.savePerms();
            return;
        }
        if (args.length >= 2 && (args[0].equals("suffix")))
        {
            String groupName = args[1];
            Group g = ThutPerms.getGroup(groupName);
            String arg = "";
            if (g == null) { throw new CommandException("Error, Specifed group does not exist."); }
            if (args.length > 2)
            {
                arg = args[2];
                for (int i = 3; i < args.length; i++)
                {
                    arg = arg + " " + args[i];
                }
            }
            g.suffix = format(arg);
            sender.addChatMessage(new TextComponentString("Set suffix to " + g.suffix));
            for (UUID id : g.members)
            {
                EntityPlayer player = server.getPlayerList().getPlayerByUUID(id);
                if (player != null)
                {
                    player.refreshDisplayName();
                }
            }
            ThutPerms.savePerms();
            return;
        }
        if (args.length >= 2 && (args[0].equals("prefix")))
        {
            String groupName = args[1];
            Group g = ThutPerms.getGroup(groupName);
            String arg = "";
            if (g == null) { throw new CommandException("Error, Specifed group does not exist."); }
            if (args.length > 2)
            {
                arg = args[2];
                for (int i = 3; i < args.length; i++)
                {
                    arg = arg + " " + args[i];
                }
            }
            g.prefix = format(arg);
            sender.addChatMessage(new TextComponentString("Set prefix to " + g.prefix));
            for (UUID id : g.members)
            {
                EntityPlayer player = server.getPlayerList().getPlayerByUUID(id);
                if (player != null)
                {
                    player.refreshDisplayName();
                }
            }
            ThutPerms.savePerms();
            return;
        }
        throw new CommandException("invalid arguments");
    }

}
