package thut.permissions.commands;

import java.util.UUID;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import thut.permissions.Group;
import thut.permissions.GroupManager;
import thut.permissions.ThutPerms;
import thut.permissions.util.BaseCommand;

public class RenameGroup extends BaseCommand
{
    public RenameGroup()
    {
    }

    @Override
    public String getCommandName()
    {
        return "renameGroup";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/renameGroup <oldname> <newname>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        String groupName = args[0];
        String newName = args[1];
        Group g = ThutPerms.getGroup(groupName);
        if (g == null) { throw new CommandException("Error, specified Group does not exist."); }
        Group g1 = ThutPerms.getGroup(newName);
        if (g1 != null) { throw new CommandException("Error, specified Group already exists."); }
        GroupManager.instance.groups.remove(g);
        GroupManager.instance.groupNameMap.remove(groupName);
        g1 = ThutPerms.addGroup(newName);
        if (g == GroupManager.instance.initial)
        {
            GroupManager.instance.initial = g1;
            GroupManager.instance.groups.remove(g1);
        }
        else if (g == GroupManager.instance.mods)
        {
            GroupManager.instance.mods = g1;
            GroupManager.instance.groups.remove(g1);
        }
        g1.allowedCommands.addAll(g.allowedCommands);
        for (UUID id : g.members)
        {
            ThutPerms.addToGroup(id, newName);
        }
        ThutPerms.savePerms();
        sender.addChatMessage(new TextComponentString("Renamed group " + groupName + " to " + newName));
    }
}
