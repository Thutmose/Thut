package thut.permissions;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.text.TextComponentString;

public class Command extends CommandBase
{

    @Override
    public String getCommandName()
    {
        return "tperms";
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "todo write this.";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        boolean done;
        try
        {
            done = tryEditGroup(server, sender, args);
            done = done || tryEditPerm(server, sender, args);
        }
        catch (Exception e)
        {
            if (e instanceof CommandException) throw (CommandException) e;
            done = false;
            e.printStackTrace();
        }
        if (!done)
        {
            throw new CommandException("Error, illegal arguments");
        }
        else
        {
            ThutPerms.savePerms(server);
        }
    }

    boolean tryEditGroup(MinecraftServer server, ICommandSender sender, String[] args) throws Exception
    {
        if (args.length == 2 && args[0].equalsIgnoreCase("addGroup"))
        {
            String groupName = args[1];
            Group g = ThutPerms.groupNameMap.get(groupName);
            if (g != null) { throw new CommandException("Error, Group already exists, cannot create again."); }
            g = ThutPerms.addGroup(groupName);
            sender.addChatMessage(new TextComponentString("Created group " + groupName));
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("removeGroup"))
        {
            String groupName = args[1];
            Group g = ThutPerms.groupNameMap.get(groupName);
            if (g == null) { throw new CommandException("Error, specified Group does not exist."); }
            ThutPerms.groups.remove(g);
            ThutPerms.groupNameMap.remove(groupName);
            sender.addChatMessage(new TextComponentString("Removed group " + groupName));
            return true;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("renameGroup"))
        {
            String groupName = args[1];
            String newName = args[2];
            Group g = ThutPerms.groupNameMap.get(groupName);
            if (g == null) { throw new CommandException("Error, specified Group does not exist."); }
            Group g1 = ThutPerms.groupNameMap.get(newName);
            if (g1 != null) { throw new CommandException("Error, specified Group already exists."); }
            ThutPerms.groups.remove(g);
            ThutPerms.groupNameMap.remove(groupName);
            g1 = ThutPerms.addGroup(newName);
            if (g == ThutPerms.initial)
            {
                ThutPerms.initial = g1;
                ThutPerms.groups.remove(g1);
            }
            g1.allowedCommands.addAll(g.allowedCommands);
            for (UUID id : g.members)
            {
                ThutPerms.addToGroup(id, newName);
            }
            sender.addChatMessage(new TextComponentString("Renamed group " + groupName + " to " + newName));
            return true;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("addTo"))
        {
            String groupName = args[1];
            String playerName = args[2];
            Group g = ThutPerms.groupNameMap.get(groupName);
            if (g == null) { throw new CommandException("Error, specified Group does not exist."); }
            GameProfile profile = new GameProfile(null, playerName);
            profile = TileEntitySkull.updateGameprofile(profile);
            if (profile.getId() == null) { throw new CommandException("Error, cannot find profile for " + playerName); }
            Group old = ThutPerms.groupIDMap.get(profile.getId());
            if (old != null) old.members.remove(profile.getId());
            ThutPerms.groupIDMap.remove(profile.getId());
            ThutPerms.addToGroup(profile.getId(), groupName);
            sender.addChatMessage(new TextComponentString("Added " + playerName + " to " + groupName));
            return true;
        }
        return false;
    }

    boolean tryEditPerm(MinecraftServer server, ICommandSender sender, String[] args) throws Exception
    {
        if (args.length == 4 && args[0].equalsIgnoreCase("editPerms"))
        {
            String groupName = args[1];
            String command = args[2];
            try
            {
                Class<?> cmd = Class.forName(command);
                if (cmd == null) { throw new CommandException("Error, Command not found."); }
            }
            catch (Exception e)
            {
                throw new CommandException("Error, Command not found.");
            }
            boolean enable = Boolean.parseBoolean(args[3]);
            Group g = ThutPerms.groupNameMap.get(groupName);
            if (g == null) { throw new CommandException("Error, Group not found, please create it first."); }
            if (enable)
            {
                g.allowedCommands.add(command);
            }
            else
            {
                g.allowedCommands.remove(command);
            }
            sender.addChatMessage(new TextComponentString("Set Permission for " + groupName + " " + enable));
            return true;
        }
        return false;
    }
}
