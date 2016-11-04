package thut.permissions.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import thut.permissions.GroupManager;
import thut.permissions.ThutPerms;

public class SpawnProtector
{

    public SpawnProtector()
    {
    }

    /** Uses player interact here to also prevent opening of inventories.
     * 
     * @param evt */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void interactRightClickBlock(PlayerInteractEvent.RightClickBlock evt)
    {
        if (!ThutPerms.allCommandUse || evt.getWorld().isRemote || canEditSpawn(evt.getEntityPlayer())
                || FMLCommonHandler.instance().getSide() == Side.CLIENT)
            return;
        World world = evt.getWorld();
        MinecraftServer server = evt.getEntityPlayer().getServer();
        BlockPos pos = evt.getPos();
        BlockPos blockpos = world.getSpawnPoint();
        int i = MathHelper.abs_int(pos.getX() - blockpos.getX());
        int j = MathHelper.abs_int(pos.getZ() - blockpos.getZ());
        int k = Math.max(i, j);
        if (k <= server.getSpawnProtectionSize())
        {
            evt.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void interactRightClickItem(PlayerInteractEvent.RightClickItem evt)
    {
        if (!ThutPerms.allCommandUse || evt.getWorld().isRemote || canEditSpawn(evt.getEntityPlayer())
                || FMLCommonHandler.instance().getSide() == Side.CLIENT)
            return;
        World world = evt.getWorld();
        MinecraftServer server = evt.getEntityPlayer().getServer();
        BlockPos pos = evt.getPos();
        BlockPos blockpos = world.getSpawnPoint();
        int i = MathHelper.abs_int(pos.getX() - blockpos.getX());
        int j = MathHelper.abs_int(pos.getZ() - blockpos.getZ());
        int k = Math.max(i, j);
        if (k <= server.getSpawnProtectionSize())
        {
            evt.setCanceled(true);
        }

    }

    /** Uses player interact here to also prevent opening of inventories.
     * 
     * @param evt */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void interactLeftClickBlock(PlayerInteractEvent.LeftClickBlock evt)
    {
        if (!ThutPerms.allCommandUse || evt.getWorld().isRemote || canEditSpawn(evt.getEntityPlayer())
                || FMLCommonHandler.instance().getSide() == Side.CLIENT)
            return;
        World world = evt.getWorld();
        MinecraftServer server = evt.getEntityPlayer().getServer();
        BlockPos pos = evt.getPos();
        BlockPos blockpos = world.getSpawnPoint();
        int i = MathHelper.abs_int(pos.getX() - blockpos.getX());
        int j = MathHelper.abs_int(pos.getZ() - blockpos.getZ());
        int k = Math.max(i, j);
        if (k <= server.getSpawnProtectionSize())
        {
            evt.setCanceled(true);
        }

    }

    private boolean canEditSpawn(EntityPlayer player)
    {
        return GroupManager.instance.hasPermission(player.getUniqueID(), "editspawn");
    }

}
