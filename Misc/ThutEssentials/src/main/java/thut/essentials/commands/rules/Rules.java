package thut.essentials.commands.rules;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import thut.essentials.util.BaseCommand;
import thut.essentials.util.ConfigManager;
import thut.essentials.util.RuleManager;

public class Rules extends BaseCommand
{
    public Rules()
    {
        super("rules", 0);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        List<String> rules = RuleManager.getRules();
        sender.addChatMessage(new TextComponentString(ConfigManager.INSTANCE.ruleHeader));
        for (String s : rules)
        {
            sender.addChatMessage(new TextComponentString(s));
        }
    }

}
