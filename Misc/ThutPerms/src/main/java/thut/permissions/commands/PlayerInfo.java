package thut.permissions.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class PlayerInfo extends CommandBase
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

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        // TODO Auto-generated method stub

    }

}
