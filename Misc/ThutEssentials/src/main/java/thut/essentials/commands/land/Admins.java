package thut.essentials.commands.land;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import thut.essentials.util.BaseCommand;

public class Admins extends BaseCommand
{

    public Admins()
    {
        super("listTeamAdmins", 0);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        String teamName = getCommandSenderAsPlayer(sender).getTeam().getRegisteredName();
        sender.addChatMessage(new TextComponentString("Admins of Team " + teamName));
        //TODO redo this to use uuids
//        Collection<?> c = LandManager.getInstance().getAdmins(teamName);
//        for (Object o : c)
//        {
//            sender.addChatMessage(new TextComponentString("" + o));
//        }
    }

}
