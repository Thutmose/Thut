package thut.essentials.commands.land;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import thut.essentials.commands.CommandManager;
import thut.essentials.events.ClaimLandEvent;
import thut.essentials.land.LandChunk;
import thut.essentials.land.LandManager;
import thut.essentials.land.LandManager.LandTeam;
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
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        LandTeam team = LandManager.getTeam(player);
        if (team == null) throw new CommandException("You are not in a team.");
        boolean isOp = CommandManager.isOp(sender);
        if (!LandManager.getInstance().isAdmin(player.getUniqueID())
                || team.teamName.equalsIgnoreCase(ConfigManager.INSTANCE.defaultTeamName))
        {
            sender.addChatMessage(new TextComponentString("You are not Authorized to claim land for your team"));
            return;
        }
        int teamCount = team.member.size();

        int count = LandManager.getInstance().countLand(team.teamName);

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
        else if (args.length > 0)
        {
            if (args[0].equalsIgnoreCase("all"))
            {
                all = true;
                up = true;
                num = 16;
            }
        }
        for (int i = 0; i < num; i++)
        {
            if (count < teamCount * ConfigManager.INSTANCE.teamLandPerPlayer || isOp)
            {
                int dir = up ? 1 : -1;
                teamCount = team.member.size();
                count = LandManager.getInstance().countLand(team.teamName);
                int x = MathHelper.floor_double(sender.getPosition().getX() / 16f);
                int y = MathHelper.floor_double(sender.getPosition().getY() / 16f) + i * dir;
                if (all) y = i * dir;
                int z = MathHelper.floor_double(sender.getPosition().getZ() / 16f);
                int dim = sender.getEntityWorld().provider.getDimension();
                if (y < 0 || y > 15) continue;
                LandChunk chunk = new LandChunk(x, y, z, dim);
                String owner = LandManager.getInstance().getLandOwner(chunk);
                ClaimLandEvent event = new ClaimLandEvent(new BlockPos(x, y, z), dim, player, team.teamName);
                MinecraftForge.EVENT_BUS.post(event);
                if (event.isCanceled()) continue;
                if (owner != null)
                {
                    if (owner.equals(team.teamName)) continue;
                    sender.addChatMessage(new TextComponentString("This land is already claimed by " + owner));
                    continue;
                }
                sender.addChatMessage(new TextComponentString("Claimed This land for Team" + team.teamName));
                LandManager.getInstance().addTeamLand(team.teamName, chunk, true);
            }
            else
            {
                break;
            }
        }
    }

}
