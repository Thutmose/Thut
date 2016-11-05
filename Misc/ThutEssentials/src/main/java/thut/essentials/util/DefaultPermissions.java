package thut.essentials.util;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;

public class DefaultPermissions implements IPermissionHandler
{
    private static DefaultPermissions instance;

    public static void init()
    {
        instance.perms.clear();
        for (String s : ConfigManager.INSTANCE.economyPermLvls)
        {
            String[] args = s.split(":");
            instance.perms.put(args[0], Integer.parseInt(args[1]));
        }
    }

    Map<String, Integer> perms = Maps.newHashMap();

    public DefaultPermissions()
    {
        instance = this;
    }

    @Override
    public boolean hasPermission(EntityPlayer player, String permission)
    {
        int perm = perms.get(permission);
        if (perm <= 0) return true;
        return player.getServer().getPlayerList().canSendCommands(player.getGameProfile());
    }

}
