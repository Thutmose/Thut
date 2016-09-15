package thut.permissions;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;

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
            if (!done) done = done || tryEditPerm(server, sender, args);
            if (!done) done = done || getInfo(server, sender, args);
        }
        catch (Exception e)
        {
            if (e instanceof CommandException) throw (CommandException) e;
            done = false;
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
            Group g = ThutPerms.getGroup(groupName);
            if (g != null) { throw new CommandException("Error, Group already exists, cannot create again."); }
            g = ThutPerms.addGroup(groupName);
            sender.addChatMessage(new TextComponentString("Created group " + groupName));
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("removeGroup"))
        {
            String groupName = args[1];
            Group g = ThutPerms.getGroup(groupName);
            if (g == null) { throw new CommandException("Error, specified Group does not exist."); }
            ThutPerms.groups.remove(g);
            ThutPerms.groupNameMap.remove(groupName);
            sender.addChatMessage(new TextComponentString("Removed group " + groupName));
            return true;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("copyGroup"))
        {
            String groupFrom = args[1];
            Group gFrom = ThutPerms.getGroup(groupFrom);
            if (gFrom == null) { throw new CommandException(
                    "Error, specified Group " + groupFrom + " does not exist."); }
            String groupTo = args[2];
            Group gTo = ThutPerms.getGroup(groupTo);
            if (gTo == null) { throw new CommandException("Error, specified Group " + groupTo + " does not exist."); }
            gTo.allowedCommands.clear();
            gTo.all = gFrom.all;
            gTo.allowedCommands.addAll(gFrom.allowedCommands);
            sender.addChatMessage(new TextComponentString("Copied from " + groupFrom + " to " + groupTo));
            return true;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("renameGroup"))
        {
            String groupName = args[1];
            String newName = args[2];
            Group g = ThutPerms.getGroup(groupName);
            if (g == null) { throw new CommandException("Error, specified Group does not exist."); }
            Group g1 = ThutPerms.getGroup(newName);
            if (g1 != null) { throw new CommandException("Error, specified Group already exists."); }
            ThutPerms.groups.remove(g);
            ThutPerms.groupNameMap.remove(groupName);
            g1 = ThutPerms.addGroup(newName);
            if (g == ThutPerms.initial)
            {
                ThutPerms.initial = g1;
                ThutPerms.groups.remove(g1);
            }
            else if (g == ThutPerms.mods)
            {
                ThutPerms.mods = g1;
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
            Group g = ThutPerms.getGroup(groupName);
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
            boolean enable = Boolean.parseBoolean(args[3]);
            Group g = ThutPerms.getGroup(groupName);
            if (g == null) { throw new CommandException("Error, Group not found, please create it first."); }
            if (command.equalsIgnoreCase("all"))
            {
                g.all = enable;
                sender.addChatMessage(new TextComponentString("Set all Permission for " + groupName + " to " + enable));
                return true;
            }
            else
            {
                try
                {
                    Class<?> cmd = Class.forName(command);
                    if (cmd == null) { throw new CommandException("Error, Command not found."); }
                }
                catch (Exception e)
                {
                    throw new CommandException("Error, Command not found.");
                }
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
        }
        else if (args.length == 3 && args[0].equalsIgnoreCase("editPerms") && args[2].equals("reset"))
        {
            String groupName = args[1];
            Group g = ThutPerms.getGroup(groupName);
            if (g == null) { throw new CommandException("Error, Group not found, please create it first."); }
            g.allowedCommands.clear();
            g.all = false;
            for (ICommand command : FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager()
                    .getCommands().values())
            {
                if (command instanceof CommandBase)
                {
                    CommandBase base = (CommandBase) command;
                    if (base.getRequiredPermissionLevel() <= 0)
                    {
                        g.allowedCommands.add(command.getClass().getName());
                    }
                }
            }
            sender.addChatMessage(new TextComponentString("Reset Permissions for " + groupName));
            return true;
        }
        return false;
    }

    boolean getInfo(MinecraftServer server, ICommandSender sender, String[] args) throws Exception
    {
        if (args[0].equalsIgnoreCase("playerGroup"))
        {
            String playerName = args[1];
            GameProfile profile = new GameProfile(null, playerName);
            profile = TileEntitySkull.updateGameprofile(profile);
            if (profile.getId() == null) { throw new CommandException("Error, cannot find profile for " + playerName); }
            Group current = ThutPerms.groupIDMap.get(profile.getId());
            if (current == null) sender.addChatMessage(new TextComponentString(playerName + " is not in a group"));
            else sender.addChatMessage(new TextComponentString(playerName + " is currently in " + current.name));
            return true;
        }
        else if (args[0].equalsIgnoreCase("exists"))
        {
            String groupName = args[1];
            Group g = ThutPerms.getGroup(groupName);
            if (g != null) sender.addChatMessage(new TextComponentString("Group " + groupName + " exists."));
            else sender.addChatMessage(new TextComponentString("Group " + groupName + "does not exist."));
            return true;
        }
        else if (args[0].equalsIgnoreCase("hasPerms"))
        {
            String groupName = args[1];
            String perm = args[2];
            Group g = ThutPerms.getGroup(groupName);
            if (g == null) { throw new CommandException("Error, specified Group does not exist."); }
            if (g.allowedCommands.contains(perm) || g.all)
                sender.addChatMessage(new TextComponentString("Group " + groupName + " can use " + perm));
            else sender.addChatMessage(new TextComponentString("Group " + groupName + " can not use " + perm));
            return true;
        }
        else if (args[0].equalsIgnoreCase("listMembers"))
        {
            String groupName = args[1];
            Group g = ThutPerms.getGroup(groupName);
            if (g == null) { throw new CommandException("Error, specified Group does not exist."); }
            sender.addChatMessage(new TextComponentString("Members of Group " + groupName));
            for (UUID id : g.members)
            {
                GameProfile profile = new GameProfile(id, null);
                profile = server.getMinecraftSessionService().fillProfileProperties(profile, true);
                sender.addChatMessage(new TextComponentString(profile.getName()));
            }
            return true;
        }
        else if (args[0].equalsIgnoreCase("listGroups"))
        {
            sender.addChatMessage(new TextComponentString("List of existing Groups:"));
            sender.addChatMessage(new TextComponentString(ThutPerms.initial.name));
            sender.addChatMessage(new TextComponentString(ThutPerms.mods.name));
            for (Group g : ThutPerms.groups)
            {
                sender.addChatMessage(new TextComponentString(g.name));
            }
            return true;
        }
        else if (args[0].equalsIgnoreCase("listCommands"))
        {
            sender.addChatMessage(new TextComponentString("List of existing commands:"));
            for (ICommand command : FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager()
                    .getCommands().values())
            {
                String name = command.getCommandName();
                sender.addChatMessage(new TextComponentString(name + "->" + command.getClass().getName()));
            }
            return true;
        }
        return false;
    }
}
