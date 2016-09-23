package thut.essentials.commands.land;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import thut.essentials.land.LandManager;
import thut.essentials.util.BaseCommand;

public class Kick extends BaseCommand
{

    public Kick()
    {
        super("kickFromTeam", 0);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        net.minecraft.scoreboard.Team team;
        if ((team = getCommandSenderAsPlayer(sender).getTeam()) == null)
            throw new CommandException("You are not in a team.");
        String teamname = args[0];

        if (teamname.equalsIgnoreCase(sender.getName()) || LandManager.getInstance().isAdmin(sender.getName(), team))
        {
            LandManager.getInstance().removeFromTeam((EntityPlayer) sender, team.getRegisteredName(), teamname);
            sender.addChatMessage(
                    new TextComponentString("Removed " + teamname + " From Team " + team.getRegisteredName()));
        }
        else
        {
            throw new CommandException("You do not have permission to do that");
        }
    }

}
