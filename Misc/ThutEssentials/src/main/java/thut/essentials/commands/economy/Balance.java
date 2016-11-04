package thut.essentials.commands.economy;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import thut.essentials.economy.EconomyManager;
import thut.essentials.util.BaseCommand;

public class Balance extends BaseCommand
{

    public Balance()
    {
        super("bal", 0);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        int amount = EconomyManager.getBalance(player);
        player.addChatMessage(new TextComponentString("Your Balance is " + amount));
    }

}
