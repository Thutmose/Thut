package thut.essentials.commands.itemcontrol;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import thut.essentials.itemcontrol.ItemControl;
import thut.essentials.util.BaseCommand;
import thut.essentials.util.ConfigManager;

public class ListItems extends BaseCommand
{
    public ListItems()
    {
        super("baditems", 0);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        sender.addChatMessage(
                new TextComponentString("Current Blackist State: " + ConfigManager.INSTANCE.itemControlEnabled));
        if (ConfigManager.INSTANCE.itemControlEnabled)
        {
            sender.addChatMessage(new TextComponentString("Current Item Blacklist:"));
            for (String s : ItemControl.blacklist)
            {
                sender.addChatMessage(new TextComponentString(s));
            }
        }
    }
}
