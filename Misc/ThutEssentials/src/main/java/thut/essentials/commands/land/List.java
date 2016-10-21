package thut.essentials.commands.land;

import java.util.Collection;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import thut.essentials.land.LandManager;
import thut.essentials.land.LandManager.LandTeam;
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
        LandTeam team = LandManager.getTeam(getCommandSenderAsPlayer(sender));
        String teamName = team.teamName;
        sender.addChatMessage(new TextComponentString("Members of Team " + teamName));
        Collection<?> c = team.member;
        for (Object o : c)
        {
            GameProfile profile = server.getMinecraftSessionService()
                    .fillProfileProperties(new GameProfile((UUID) o, null), true);
            sender.addChatMessage(new TextComponentString("" + profile.getName()));
        }
    }

}
