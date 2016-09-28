package thut.permissions;

import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.minecraft.command.ICommand;

public class Player
{
    public UUID        id;
    public boolean     all             = false;
    public Set<String> allowedCommands = Sets.newHashSet();

    public Player()
    {
    }

    public boolean hasPermission(String permission)
    {
        return all || allowedCommands.contains(permission);
    }

    public boolean canUse(ICommand command)
    {
        System.out.println(
                command.getCommandName() + " " + all + " " + allowedCommands.contains(command.getClass().getName()));
        return all || allowedCommands.contains(command.getClass().getName());
    }

}
