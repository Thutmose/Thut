package thut.permissions.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import thut.permissions.Group;
import thut.permissions.ThutPerms;
import thut.permissions.util.BaseCommand;

public class AddGroup extends BaseCommand
{
    public AddGroup()
    {
    }

    @Override
    public String getCommandName()
    {
        return "addGroup";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/addGroup <group>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        String groupName = args[0];
        Group g = ThutPerms.getGroup(groupName);
        if (g != null) { throw new CommandException("Error, Group already exists, cannot create again."); }
        g = ThutPerms.addGroup(groupName);
        ThutPerms.savePerms();
        sender.addChatMessage(new TextComponentString("Created group " + groupName));
    }

}
