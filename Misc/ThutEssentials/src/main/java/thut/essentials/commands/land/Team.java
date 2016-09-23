package thut.essentials.commands.land;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import thut.essentials.util.BaseCommand;

public class Team extends BaseCommand
{

    public Team()
    {
        super("myTeam", 0);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        net.minecraft.scoreboard.Team team;
        if ((team = getCommandSenderAsPlayer(sender).getTeam()) == null)
            throw new CommandException("You are not in a team.");
        String teamName = team.getRegisteredName();
        sender.addChatMessage(new TextComponentString("Currently a member of Team " + teamName));
    }

}
