package thut.essentials.commands.land;

import java.util.Collection;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import thut.essentials.land.LandManager;
import thut.essentials.util.BaseCommand;

public class List extends BaseCommand
{

    public List()
    {
        super("listMembers", 0);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        Team team = null;
        if (sender instanceof EntityPlayer)
        {
            team = LandManager.getTeam(getCommandSenderAsPlayer(sender));
        }
        String teamName = team.getRegisteredName();
        sender.addChatMessage(new TextComponentString("Members of Team " + teamName));
        Collection<?> c = team.getMembershipCollection();
        for (Object o : c)
        {
            sender.addChatMessage(new TextComponentString("" + o));
        }
    }

}
