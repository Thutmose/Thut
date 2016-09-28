package thut.permissions;

import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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

    @Deprecated
    public void readFromNBT(NBTTagCompound tag)
    {
        NBTTagList list = (NBTTagList) tag.getTag("commands");
        allowedCommands.clear();
        if (list != null) for (int i = 0; i < list.tagCount(); i++)
        {
            String command = list.getStringTagAt(i);
            allowedCommands.add(command);
        }
        suffix = tag.getString("suffix");
        prefix = tag.getString("prefix");
        list = (NBTTagList) tag.getTag("members");
        members.clear();
        if (list != null) for (int i = 0; i < list.tagCount(); i++)
        {
            String id = list.getStringTagAt(i);
            try
            {
                members.add(UUID.fromString(id));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        if (tag.hasKey("all")) all = tag.getBoolean("all");
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
