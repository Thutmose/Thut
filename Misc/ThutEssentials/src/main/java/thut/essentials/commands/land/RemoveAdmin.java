package thut.essentials.commands.land;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import thut.essentials.land.LandManager;
import thut.essentials.util.BaseCommand;

public class RemoveAdmin extends BaseCommand
{

    public RemoveAdmin()
    {
        super("removeTeamAdmin", 0);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayer player = getPlayer(server, sender, args[1]);
        String teamName = getCommandSenderAsPlayer(server).getTeam().getRegisteredName();
        if (LandManager.getInstance().isAdmin(player.getUniqueID()))
        {
            LandManager.getInstance().removeAdmin(player.getUniqueID());
            sender.addChatMessage(new TextComponentString(player + " removed as an Admin for Team " + teamName));
        }
        else
        {
            throw new CommandException("You do not have permission to do that.");
        }
    }

}
