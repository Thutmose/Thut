package thut.permissions.commands;

import java.lang.reflect.Field;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import thut.permissions.Group;
import thut.permissions.ThutPerms;
import thut.permissions.util.BaseCommand;

public class EditPerms extends BaseCommand
{

    public EditPerms()
    {
    }

    @Override
    public String getCommandName()
    {
        return "editPerms";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/editPerms <group> <perm> <value> or /editPerms allowUse <optional|value>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length == 3)
        {
            String groupName = args[0];
            String command = args[1];
            boolean enable = Boolean.parseBoolean(args[2]);
            Group g = ThutPerms.getGroup(groupName);
            if (g == null) { throw new CommandException("Error, Group not found, please create it first."); }
            if (command.equalsIgnoreCase("all"))
            {
                g.all = enable;
                sender.addChatMessage(new TextComponentString("Set all Permission for " + groupName + " to " + enable));
                ThutPerms.savePerms();
                return;
            }
            try
            {
                Class<?> cmd = Class.forName(command);
                if (cmd == null) { throw new CommandException("Error, Command not found."); }
            }
            catch (Exception e)
            {
                throw new CommandException("Error, Command not found.");
            }
            if (enable)
            {
                g.allowedCommands.add(command);
            }
            else
            {
                g.allowedCommands.remove(command);
            }
            sender.addChatMessage(new TextComponentString("Set Permission for " + groupName + " " + enable));
            ThutPerms.savePerms();
            return;
        }
        else if (args[0].equals("allowUse"))
        {
            if (args.length == 2)
            {
                boolean enable = Boolean.parseBoolean(args[1]);
                ThutPerms.allCommandUse = enable;
                Configuration config = new Configuration(ThutPerms.configFile);
                config.load();
                config.get(Configuration.CATEGORY_GENERAL, "allCommandUse", enable).set(enable);
                config.save();
                Field f = ReflectionHelper.findField(PlayerList.class, "commandsAllowedForAll", "field_72407_n", "t");
                f.setAccessible(true);
                try
                {
                    f.set(server.getPlayerList(), enable);
                }
                catch (IllegalArgumentException | IllegalAccessException e)
                {
                    e.printStackTrace();
                }
                sender.addChatMessage(new TextComponentString(
                        "Set players able to use all commands allowed for their group to " + enable));
                ThutPerms.savePerms();
                return;
            }
            sender.addChatMessage(new TextComponentString(
                    "Players allowed to use all commands for group: " + ThutPerms.allCommandUse));
            return;
        }
    }

}
