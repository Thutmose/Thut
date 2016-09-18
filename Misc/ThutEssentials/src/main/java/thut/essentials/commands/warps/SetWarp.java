package thut.essentials.commands.warps;

import java.util.Arrays;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import thut.essentials.util.BaseCommand;
import thut.essentials.util.WarpManager;

public class SetWarp extends BaseCommand
{

    public SetWarp()
    {
        super("setwarp", 2);
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
        if (warpName == null || warpName.isEmpty()) throw new CommandException("You need to specify the warp name");

        int size = 5;
        if (warpName.startsWith("'"))
        {
            warpName = warpName.substring(1, warpName.length());
            for (int i = 1; i < args.length; i++)
            {
                warpName = warpName + " "+ args[i];
                size++;
                if (args[i].endsWith("'"))
                {
                    warpName = warpName.substring(0, warpName.length() - 1);
                    break;
                }
            }
        }
        BlockPos pos = null;
        int dim = 0;
        if (args.length == size)
        {
            pos = new BlockPos(Integer.parseInt(args[size-4]), Integer.parseInt(args[size-3]), Integer.parseInt(args[size-2]));
            dim = Integer.parseInt(args[size-1]);
        }
        else
        {
            EntityPlayerMP player = getCommandSenderAsPlayer(sender);
            pos = player.getPosition();
            dim = player.dimension;
        }
        try
        {
            WarpManager.setWarp(pos, dim, warpName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        sender.addChatMessage(
                new TextComponentString("Set " + warpName + " to " + Arrays.toString(WarpManager.getWarp(warpName))));
    }

}
