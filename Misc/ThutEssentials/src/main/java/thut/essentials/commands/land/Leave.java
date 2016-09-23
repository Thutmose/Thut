package thut.essentials.commands.land;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import thut.essentials.land.LandManager;
import thut.essentials.util.BaseCommand;

public class Leave extends BaseCommand
{

    public Leave()
    {
        super("leaveTeam", 0);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        net.minecraft.scoreboard.Team team;
        if ((team = getCommandSenderAsPlayer(sender).getTeam()) == null)
            throw new CommandException("You are not in a team.");
        String playerName = sender.getName();
        LandManager.getInstance().removeFromTeam((EntityPlayer) sender, team.getRegisteredName(), playerName);
        sender.addChatMessage(new TextComponentString("Left Team " + team.getRegisteredName()));

    }

}
