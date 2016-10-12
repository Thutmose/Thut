package thut.permissions.commands;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import thut.permissions.util.BaseCommand;

public class PlayerInfo extends BaseCommand
{

    public PlayerInfo()
    {
    }

    @Override
    public String getCommandName()
    {
        return "playerInfo";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/playerInfo <player> <arguments>";
    }

    /** Return whether the specified command parameter index is a username
     * parameter. */
    @Override
    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        // TODO Auto-generated method stub

    }

}
