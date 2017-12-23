package thut.lootcrates;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class CommandReload extends CommandBase
{

    public CommandReload()
    {
    }

    @Override
    public String getName()
    {
        return "lc";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/lc reload";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args[0].equals("reload"))
        {
            XMLStuff.instance.init();
        }
    }

}
