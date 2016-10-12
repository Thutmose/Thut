package thut.permissions.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import thut.permissions.Group;
import thut.permissions.GroupManager;
import thut.permissions.ThutPerms;
import thut.permissions.util.BaseCommand;

public class RemoveGroup extends BaseCommand
{

    public RemoveGroup()
    {
    }

    @Override
    public String getCommandName()
    {
        return "removeGroup";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/removeGroup <name>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        String groupName = args[0];
        Group g = ThutPerms.getGroup(groupName);
        if (g == null) { throw new CommandException("Error, specified Group does not exist."); }
        GroupManager.instance.groups.remove(g);
        GroupManager.instance.groupNameMap.remove(groupName);
        ThutPerms.savePerms();
        sender.addChatMessage(new TextComponentString("Removed group " + groupName));
    }

}
