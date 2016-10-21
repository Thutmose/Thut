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
        EntityPlayer kicker = getCommandSenderAsPlayer(sender);
        String toKick = args[0];
        EntityPlayer kickee = getPlayer(server, sender, toKick);
        if (toKick.equalsIgnoreCase(sender.getName()) || LandManager.getInstance().isAdmin(kicker.getUniqueID()))
        {
            LandManager.getInstance().removeFromTeam(kickee.getUniqueID());
            sender.addChatMessage(new TextComponentString("Removed " + toKick + " From Team."));
        }
        else
        {
            throw new CommandException("You do not have permission to do that");
        }
    }

}
