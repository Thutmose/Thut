package thut.essentials.commands.warps;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import thut.essentials.util.BaseCommand;
import thut.essentials.util.WarpManager;

public class DelWarp extends BaseCommand
{

    public DelWarp()
    {
        super("delwarp", 2);
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/" + getCommandName() + " <warpName>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        String warpName = args.length > 0 ? args[0] : null;
        if (warpName == null) throw new CommandException("You need to specify the warp name");
        for (int i = 1; i < args.length; i++)
        {
            warpName = warpName + " " + args[i];
        }
        try
        {
            WarpManager.delWarp(warpName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
