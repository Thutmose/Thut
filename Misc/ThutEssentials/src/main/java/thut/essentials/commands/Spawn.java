package thut.essentials.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class Spawn extends CommandBase
{
    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public String getCommandName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        // TODO Auto-generated method stub
        
    }

}
