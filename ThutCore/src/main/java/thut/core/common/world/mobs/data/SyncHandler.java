package thut.core.common.world.mobs.data;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import thut.api.world.mobs.data.DataSync;

public class SyncHandler
{
    @CapabilityInject(DataSync.class)
    public static final Capability<DataSync> CAP = null;

    public static DataSync getData(Entity mob)
    {
        return mob.getCapability(CAP, null);
    }

    private List<SyncPacket> syncSet = Lists.newArrayList();

    @SubscribeEvent
    public void WorldTick(WorldTickEvent event)
    {
        if (event.side == Side.CLIENT || event.phase == Phase.START) return;
        int m = syncSet.size();
        long time = event.world.getTotalWorldTime() - 10;
        for (int i = 0; i < m; i++)
        {
            SyncPacket packet = syncSet.get(i);
            if (packet.time < time)
            {
                syncSet.remove(i);
                i--;
                m--;
                PacketDataSync.sync(packet.sendTo, packet.data, packet.target.getEntityId(), true);
            }
        }
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
        boolean all = false;
        for (EntityPlayer player : players)
        {
            sendSelf = sendSelf && player != event.getEntity();
            if (player instanceof EntityPlayerMP)
                PacketDataSync.sync((EntityPlayerMP) player, data, event.getEntity().getEntityId(), all);
        }
        if (sendSelf)
        {
            PacketDataSync.sync((EntityPlayerMP) event.getEntity(), data, event.getEntity().getEntityId(), all);
        }
    }

    @SubscribeEvent
    public void startTracking(StartTracking event)
    {
        if (event.getTarget().getEntityWorld().isRemote) return;
        DataSync data = getData(event.getTarget());
        if (data == null) return;
        syncSet.add(new SyncPacket(event.getTarget().getEntityWorld().getTotalWorldTime(),
                (EntityPlayerMP) event.getEntityPlayer(), event.getEntity(), data));
    }

    private static class SyncPacket
    {
        final long           time;
        final EntityPlayerMP sendTo;
        final Entity         target;
        final DataSync       data;

        public SyncPacket(long time, EntityPlayerMP sendTo, Entity target, DataSync data)
        {
            this.time = time;
            this.sendTo = sendTo;
            this.target = target;
            this.data = data;
        }
    }
}
