package thut.essentials.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import thut.essentials.ThutEssentials;

public class Fly extends CommandBase
{

    @Override
    public String getCommandName()
    {
        return ThutEssentials.commands.get("fly").get(0);
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/" + getCommandName();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        player.capabilities.allowFlying = !player.capabilities.allowFlying;
        if (!player.capabilities.allowFlying)
        {
            player.capabilities.isFlying = false;
        }
        player.sendPlayerAbilities();
    }
}
