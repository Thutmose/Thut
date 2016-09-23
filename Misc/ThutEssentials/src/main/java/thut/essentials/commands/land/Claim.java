package thut.essentials.commands.land;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import thut.essentials.commands.CommandManager;
import thut.essentials.land.LandChunk;
import thut.essentials.land.LandManager;
import thut.essentials.util.BaseCommand;
import thut.essentials.util.ConfigManager;

public class Claim extends BaseCommand
{

    public Claim()
    {
        super("claim", 0);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        net.minecraft.scoreboard.Team team;
        if ((team = getCommandSenderAsPlayer(sender).getTeam()) == null)
            throw new CommandException("You are not in a team.");
        boolean isOp = CommandManager.isOp(sender);
        if (!LandManager.getInstance().isAdmin(sender.getName(), team)
                || team.getRegisteredName().equalsIgnoreCase("Trainers"))
        {
            sender.addChatMessage(new TextComponentString("You are not Authorized to claim land for your team"));
            return;
        }
        int teamCount = team.getMembershipCollection().size();

        int count = LandManager.getInstance().countLand(team.getRegisteredName());

        boolean up = false;
        boolean all = false;
        int num = 1;

        if (args.length > 2)
        {
            try
            {
                if (args[1].equalsIgnoreCase("up") || args[1].equalsIgnoreCase("down"))
                {
                    num = Integer.parseInt(args[2]);
                    up = args[1].equalsIgnoreCase("up");
                }
                if (args[1].equalsIgnoreCase("all"))
                {
                    all = true;
                    up = true;
                    num = 16;
                }
            }
            catch (NumberFormatException e)
            {
                // e.printStackTrace();
            }
        }
        for (int i = 0; i < num; i++)
        {
            if (count < teamCount * ConfigManager.INSTANCE.teamLandPerPlayer || isOp)
            {
                int dir = up ? 1 : -1;
                teamCount = team.getMembershipCollection().size();
                count = LandManager.getInstance().countLand(team.getRegisteredName());

                int x = MathHelper.floor_double(sender.getPosition().getX() / 16f);
                int y = MathHelper.floor_double(sender.getPosition().getY() / 16f) + i * dir;
                if (all) y = i * dir;
                int z = MathHelper.floor_double(sender.getPosition().getZ() / 16f);
                int dim = sender.getEntityWorld().provider.getDimension();
                if (y < 0 || y > 15) continue;
                LandChunk chunk = new LandChunk(x, y, z, dim);
                String owner = LandManager.getInstance().getLandOwner(chunk);

                if (owner != null)
                {
                    if (owner.equals(team.getRegisteredName())) continue;

                    sender.addChatMessage(new TextComponentString("This land is already claimed by " + owner));
                    return;
                }
                sender.addChatMessage(new TextComponentString("Claimed This land for Team" + team.getRegisteredName()));
                LandManager.getInstance().addTeamLand(team.getRegisteredName(), chunk, true);
                num--;
            }
            else
            {
                num = 0;
            }
        }
    }

}
