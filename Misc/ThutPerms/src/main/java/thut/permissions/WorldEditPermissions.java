package thut.permissions;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayerMP;

public class WorldEditPermissions implements com.sk89q.worldedit.forge.ForgePermissionsProvider
{
    Map<String, Set<String>> commandsMap = Maps.newHashMap();

    @Override
    public boolean hasPermission(EntityPlayerMP player, String permission)
    {
        Set<String> commands = commandsMap.get(permission);
        Group g = ThutPerms.groupIDMap.get(player.getUniqueID());
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

    @Override
    public void registerPermission(ICommand command, String permission)
    {
        Set<String> commands = commandsMap.get(permission);
        if (commands == null) commandsMap.put(permission, commands = Sets.newHashSet());
        commands.add(command.getClass().getName());
    }

}
