package thut.essentials.util;

import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.common.MinecraftForge;
import thut.essentials.commands.CommandManager;

public abstract class BaseCommand extends CommandBase
{
    final String key;
    final int    perm;

    public BaseCommand(String key, int perms)
    {
        this.key = key;
        perm = perms;
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return perm;
    }

    @Override
    public String getCommandName()
    {
        if (CommandManager.commands.get(key) == null) { return key; }
        return CommandManager.commands.get(key).get(0);
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/" + getCommandName();
    }

    @Override
    public List<String> getCommandAliases()
    {
        if (CommandManager.commands.get(key) != null) { return CommandManager.commands.get(key); }
        return Collections.<String> emptyList();
    }

    public void destroy()
    {
        MinecraftForge.EVENT_BUS.unregister(this);
    }
}
