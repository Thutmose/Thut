package thut.core.common.world.mobs.data;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
        Set<? extends PlayerEntity> players = world.getEntityTracker().getTrackingPlayers(event.getEntity());
        boolean sendSelf = event.getEntity() instanceof ServerPlayerEntity;
        List<ServerPlayerEntity> playerList = Lists.newArrayList();
        for (PlayerEntity player : players)
        {
            sendSelf = sendSelf && player != event.getEntity();
            playerList.add((ServerPlayerEntity) player);
        }
        if (sendSelf)
        {
            playerList.add((ServerPlayerEntity) event.getEntity());
        }
        if (!playerList.isEmpty()) PacketDataSync.sync(playerList, data, event.getEntity().getEntityId(), false);
    }

    @SubscribeEvent
    public void startTracking(StartTracking event)
    {
        if (event.getTarget().getEntityWorld().isRemote) return;
        DataSync data = getData(event.getTarget());
        if (data == null) return;
        PacketDataSync.sync((ServerPlayerEntity) event.getPlayerEntity(), data, event.getTarget().getEntityId(), true);
    }
}
