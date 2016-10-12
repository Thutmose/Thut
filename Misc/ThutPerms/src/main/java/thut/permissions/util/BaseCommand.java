package thut.permissions.util;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public abstract class BaseCommand extends CommandBase
{

    public BaseCommand()
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public List<String> getCommandAliases()
    {
        return Lists.newArrayList(getCommandName(), getCommandName().toLowerCase(Locale.ENGLISH));
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
            @Nullable BlockPos pos)
    {
        int last = args.length - 1;
        if (last >= 0 && isUsernameIndex(args,
                last)) { return getListOfStringsMatchingLastWord(args, server.getAllUsernames()); }
        return Collections.<String> emptyList();
    }
}
