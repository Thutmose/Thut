package thut.permissions;

import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class Group
{
    public String      name;
    public String      prefix          = "";
    public String      suffix          = "";
    public boolean     all             = false;
    public Set<String> allowedCommands = Sets.newHashSet();
    public Set<UUID>   members         = Sets.newHashSet();

    public Group(String name)
    {
        this.name = name;
        for (ICommand command : FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager()
                .getCommands().values())
        {
            if (command instanceof CommandBase)
            {
                CommandBase base = (CommandBase) command;
                if (base.getRequiredPermissionLevel() <= 0)
                {
                    allowedCommands.add(command.getClass().getName());
                }
            }
        }
    }

    public boolean hasPermission(String permission)
    {
        return all || allowedCommands.contains(permission);
    }

    public boolean canUse(ICommand command)
    {
        return hasPermission(command.getClass().getName());
    }
}
