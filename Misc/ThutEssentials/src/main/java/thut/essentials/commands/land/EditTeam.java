package thut.essentials.commands.land;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import thut.essentials.land.LandManager;
import thut.essentials.land.LandManager.LandTeam;
import thut.essentials.util.BaseCommand;
import thut.essentials.util.RuleManager;

public class EditTeam extends BaseCommand
{

    public EditTeam()
    {
        super("editteam", 0, "editTeam");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        String team = LandManager.getTeam(player).getRegisteredName();
        if (!LandManager.getInstance().isAdmin(player.getName(),
                team)) { throw new CommandException("you need to be a team admin to do that"); }
        LandTeam landTeam = LandManager.getInstance().getTeam(team, false);
        String arg = args[0];
        String message = "";
        if (args.length > 1) message = args[1];
        for (int i = 2; i < args.length; i++)
        {
            message = message + " " + args[i];
        }
        message = RuleManager.format(message);
        if (arg.equalsIgnoreCase("exit"))
        {
            landTeam.exitMessage = message;
            sender.addChatMessage(new TextComponentString(TextFormatting.GREEN + "Set Exit Message to " + message));
            return;
        }
        if (arg.equalsIgnoreCase("enter"))
        {
            landTeam.enterMessage = message;
            sender.addChatMessage(new TextComponentString(TextFormatting.GREEN + "Set Enter Message to " + message));
            return;
        }
        if (arg.equalsIgnoreCase("deny"))
        {
            landTeam.denyMessage = message;
            sender.addChatMessage(new TextComponentString(TextFormatting.GREEN + "Set Deny Message to " + message));
            return;
        }
        if (arg.equalsIgnoreCase("reserve"))
        {
            landTeam.reserved = Boolean.parseBoolean(message);
            sender.addChatMessage(
                    new TextComponentString(TextFormatting.GREEN + "Reserved set to " + landTeam.reserved));
            return;
        }
    }
}
