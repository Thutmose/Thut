package thut.essentials.compat;

import net.minecraft.entity.player.EntityPlayer;
import thut.essentials.ThutEssentials;
import thut.essentials.util.IPermissionHandler;
import thut.permissions.GroupManager;

public class ThutPermsCompat implements IPermissionHandler
{

    public ThutPermsCompat()
    {
        ThutEssentials.perms = this;
    }

    @Override
    public boolean hasPermission(EntityPlayer player, String permission)
    {
        return GroupManager.instance.hasPermission(player.getUniqueID(), permission);
    }

}
