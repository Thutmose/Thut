package thut.core.common.world.mobs.data;

import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thut.api.world.mobs.data.DataSync;

public class SyncHandler
{
    @CapabilityInject(DataSync.class)
    public static final Capability<DataSync> CAP = null;

    public static DataSync getData(Entity mob)
    {
        return mob.getCapability(CAP, null);
    }

    @SubscribeEvent
    public void EntityUpdate(LivingUpdateEvent event)
    {
        if (event.getEntity().getEntityWorld().isRemote) return;
        DataSync data = getData(event.getEntity());
        if (data == null) return;
        WorldServer world = (WorldServer) event.getEntity().getEntityWorld();
        Set<? extends EntityPlayer> players = world.getEntityTracker().getTrackingPlayers(event.getEntity());
        boolean sendSelf = event.getEntity() instanceof EntityPlayerMP;
        for (EntityPlayer player : players)
        {
            sendSelf = sendSelf && player != event.getEntity();
            if (player instanceof EntityPlayerMP)
                PacketDataSync.sync((EntityPlayerMP) player, data, event.getEntity().getEntityId(), false);
        }
        if (sendSelf)
        {
            PacketDataSync.sync((EntityPlayerMP) event.getEntity(), data, event.getEntity().getEntityId(), false);
        }
    }

    @SubscribeEvent
    public void startTracking(StartTracking event)
    {
        if (event.getTarget().getEntityWorld().isRemote) return;
        DataSync data = getData(event.getTarget());
        if (data == null) return;
        EntityPlayer player = event.getEntityPlayer();
        PacketDataSync.sync((EntityPlayerMP) player, data, event.getTarget().getEntityId(), true);
    }
}
