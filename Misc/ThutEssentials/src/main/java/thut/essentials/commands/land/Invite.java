package thut.essentials.commands.land;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import thut.essentials.land.LandManager;
import thut.essentials.util.BaseCommand;

public class Invite extends BaseCommand
{

    public Invite()
    {
        super("teamInvite", 0);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        String player = args[0];
        EntityPlayer adding = getPlayer(server, sender, player);
        boolean isPlayer = adding != null;
        Team team = null;
        if (sender instanceof EntityPlayer)
        {
            team = LandManager.getTeam(getCommandSenderAsPlayer(sender));
        }
        if (isPlayer)
        {
            LandManager.getInstance().invite(sender.getName(), adding.getName(), team.getRegisteredName());
            String links = "";
            String cmd = "joinTeam";
            String command = "/" + cmd + " " + team.getRegisteredName();
            String abilityJson = "{\"text\":\"" + team.getRegisteredName()
                    + "\",\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + command + ""
                    + "\"}}";
            links = abilityJson;
            adding.addChatMessage(new TextComponentString("New Invite to Team " + team.getRegisteredName()));
            ITextComponent message = ITextComponent.Serializer.jsonToComponent("[\" [\"," + links + ",\"]\"]");
            adding.addChatMessage(message);
        }
    }
}
