package thut.permissions;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;

@InterfaceList({ @Interface(iface = "com.sk89q.worldedit.forge.ForgePermissionsProvider", modid = "worldedit") })
public class WorldEditPermissions implements com.sk89q.worldedit.forge.ForgePermissionsProvider
{
    Map<String, Set<String>> commandsMap = Maps.newHashMap();

    @Optional.Method(modid = "worldedit")
    public boolean hasPermission(EntityPlayerMP player, String permission)
    {
        Set<String> commands = commandsMap.get(permission);
        Group g = GroupManager.instance.groupIDMap.get(player.getUniqueID());
        if (commands != null && g != null)
        {
            if (g.all) return true;
            for (String s : commands)
            {
                if (g.allowedCommands.contains(s)) { return true; }
            }
        }
        return false;
    }

    @Optional.Method(modid = "worldedit")
    public void registerPermission(ICommand command, String permission)
    {
        if (command == null || permission == null) return;
        Set<String> commands = commandsMap.get(permission);
        if (commands == null) commandsMap.put(permission, commands = Sets.newHashSet());
        commands.add(command.getClass().getName());
    }

}
