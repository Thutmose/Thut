package thut.essentials.commands.land;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import thut.essentials.land.LandChunk;
import thut.essentials.land.LandManager;
import thut.essentials.land.LandManager.LandTeam;
import thut.essentials.util.BaseCommand;

public class UnClaim extends BaseCommand
{

    public UnClaim()
    {
        super("unclaim", 0);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        net.minecraft.scoreboard.Team team;
        if ((team = getCommandSenderAsPlayer(sender).getTeam()) == null)
            throw new CommandException("You are not in a team.");
        if (!LandManager.getInstance().isAdmin(sender.getName(), team)
                || team.getRegisteredName().equalsIgnoreCase("Trainers"))
        {
            sender.addChatMessage(new TextComponentString("You are not Authorized to unclaim land for your team"));
            return;
        }
        boolean up = false;
        int num = 1;

        if (args.length > 1)
        {
            try
            {
                if (args[0].equalsIgnoreCase("up") || args[0].equalsIgnoreCase("down"))
                {
                    num = Integer.parseInt(args[1]);
                    up = args[0].equalsIgnoreCase("up");
                }
            }
            catch (NumberFormatException e)
            {
                // e.printStackTrace();
            }
        }
        if (args.length > 1 && args[0].equalsIgnoreCase("all"))
        {
            LandTeam team1 = LandManager.getInstance().getTeam(team.getRegisteredName(), false);
            team1.land.land.clear();
            sender.addChatMessage(new TextComponentString("Unclaimed all land for Team" + team.getRegisteredName()));
            return;
        }
        int n = 0;
        for (int i = 0; i < num; i++)
        {
            int dir = up ? -1 : 1;
            int x = MathHelper.floor_double(sender.getPosition().getX() / 16f);
            int y = MathHelper.floor_double(sender.getPosition().getY() / 16f) + dir * i;
            int z = MathHelper.floor_double(sender.getPosition().getZ() / 16f);
            int dim = sender.getEntityWorld().provider.getDimension();
            if (y < 0 || y > 15) continue;
            n++;
            LandManager.getInstance().removeTeamLand(team.getRegisteredName(), new LandChunk(x, y, z, dim));
        }
        if (n > 0)
            sender.addChatMessage(new TextComponentString("Unclaimed This land for Team" + team.getRegisteredName()));
    }

}
