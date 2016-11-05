package thut.essentials.util;

import net.minecraft.entity.player.EntityPlayer;

public interface IPermissionHandler
{
    boolean hasPermission(EntityPlayer player, String permission);
}
