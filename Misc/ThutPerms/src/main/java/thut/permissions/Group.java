package thut.permissions;

import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class Group
{
    public final String name;
    boolean             all             = false;
    Set<String>         allowedCommands = Sets.newHashSet();
    Set<UUID>           members         = Sets.newHashSet();

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

    public void writeToNBT(NBTTagCompound tag)
    {
        NBTTagList commands = new NBTTagList();
        for (String s : allowedCommands)
        {
            commands.appendTag(new NBTTagString(s));
        }
        tag.setTag("commands", commands);
        NBTTagList members = new NBTTagList();
        for (UUID id : this.members)
        {
            members.appendTag(new NBTTagString(id.toString()));
        }
        tag.setTag("members", members);
        tag.setBoolean("all", all);
    }

    public void readFromNBT(NBTTagCompound tag)
    {
        NBTTagList list = (NBTTagList) tag.getTag("commands");
        allowedCommands.clear();
        if (list != null) for (int i = 0; i < list.tagCount(); i++)
        {
            String command = list.getStringTagAt(i);
            allowedCommands.add(command);
        }
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

    public boolean canUse(ICommand command)
    {
        return all || allowedCommands.contains(command.getClass().getName());
    }
}
