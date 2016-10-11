package thut.essentials.util;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import thut.essentials.commands.CommandManager;

public abstract class BaseCommand extends CommandBase
{
    final String key;
    final int    perm;

    public BaseCommand(String key, int perms, String... aliases)
    {
        this.key = key;
        perm = perms;
        if (aliases.length > 0)
        {
            List<String> alii = CommandManager.commands.get(key);
            if (alii == null) CommandManager.commands.put(key, alii = Lists.newArrayList(key));
            for (String s : aliases)
                alii.add(s);
        }
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return perm;
    }

    /** Check if the given ICommandSender has permission to execute this
     * command */
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        if (!(sender instanceof EntityPlayer) || !server.isDedicatedServer()) return true;
        EntityPlayer player = null;
        try
        {
            player = getCommandSenderAsPlayer(sender);
        }
        catch (PlayerNotFoundException e)
        {
            return false;
        }
        UserListOpsEntry userlistopsentry = server.getPlayerList().getOppedPlayers().getEntry(player.getGameProfile());
        return userlistopsentry != null ? userlistopsentry.getPermissionLevel() >= perm : perm <= 0;
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

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args,
            @Nullable BlockPos pos)
    {
        int last = args.length - 1;
        if (last >= 0 && isUsernameIndex(args,
                last)) { return getListOfStringsMatchingLastWord(args, server.getAllUsernames()); }
        return Collections.<String> emptyList();
    }

    public void destroy()
    {
        MinecraftForge.EVENT_BUS.unregister(this);
    }
}
