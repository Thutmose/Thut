package thut.essentials.commands.land;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import thut.essentials.commands.CommandManager;
import thut.essentials.land.LandManager;
import thut.essentials.util.BaseCommand;
import thut.essentials.util.ConfigManager;

public class Join extends BaseCommand
{

    public Join()
    {
        super("joinTeam", 0);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        boolean isOp = CommandManager.isOp(sender);

        String teamname = args[0];
        ScorePlayerTeam teamtojoin = sender.getEntityWorld().getScoreboard().getTeam(teamname);
        if (teamtojoin != null)
        {
            // TODO have default team settable in configs.
            boolean empty = teamtojoin.getRegisteredName().equalsIgnoreCase(ConfigManager.INSTANCE.defaultTeamName);
            if (empty)
            {
                empty = teamtojoin.getMembershipCollection() == null
                        || teamtojoin.getMembershipCollection().size() == 0;
                empty = empty && !LandManager.getInstance().getTeam(teamname, false).reserved;
            }
            if (empty || isOp)
            {
                LandManager.getInstance().addToTeam((EntityPlayer) sender, teamname);
                LandManager.getInstance().addToAdmins(sender.getName(), teamname);
                return;
            }
        }
        if (LandManager.getInstance().hasInvite(sender.getName(), teamname) || isOp)
            LandManager.getInstance().addToTeam((EntityPlayer) sender, teamname);
        else sender.addChatMessage(new TextComponentString("You do not have an invite for Team " + teamname));
    }

}
