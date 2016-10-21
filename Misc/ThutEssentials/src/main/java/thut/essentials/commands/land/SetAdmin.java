package thut.essentials.commands.land;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import thut.essentials.land.LandManager;
import thut.essentials.util.BaseCommand;

public class SetAdmin extends BaseCommand
{

    public SetAdmin()
    {
        super("setTeamAdmin", 0);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayer player = getPlayer(server, sender, args[0]);
        String teamName = LandManager.getTeam(player).teamName;
        if (LandManager.getInstance().isAdmin(player.getUniqueID()))
        {
            LandManager.getInstance().addAdmin(player.getUniqueID(), teamName);
            sender.addChatMessage(new TextComponentString(player + " added as an Admin for Team " + teamName));

        }
        else
        {
            throw new CommandException("You do not have permission to do that");
        }
    }

}
