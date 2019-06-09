package thut.api;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import thut.api.block.IOwnableTE;
import thut.api.network.PacketHandler;

public class TickHandler
{
    private static TickHandler                 instance;

    public static int                          maxChanges = 200;

    public static TickHandler getInstance()
    {
        if (instance == null) new TickHandler();
        return instance;
    }

    public TickHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
        new PacketHandler();
        instance = this;
    }

    public static Map<UUID, Integer> playerTickTracker = Maps.newHashMap();

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    /** This is used to re-set view bobbing for when a player walks off a block
     * entity. */
    public void PlayerTick(PlayerTickEvent event)
    {
        if (event.phase == Phase.END && playerTickTracker.containsKey(event.player.getUniqueID()))
        {
            Integer time = playerTickTracker.get(event.player.getUniqueID());
            if (time < (int) (System.currentTimeMillis() % 2000) - 100)
            {
                Minecraft.getInstance().gameSettings.viewBobbing = true;
            }
        }
        /** This deals with the massive hunger reduction for standing on the
         * block entities. */
        if (event.phase == Phase.END && event.side == LogicalSide.CLIENT)
        {
            if (event.player.ticksExisted == event.player.getEntityData().getInt("lastStandTick") + 1)
            {
                event.player.onGround = true;
            }
        }
    }

    @SubscribeEvent
    public void placeEvent(EntityPlaceEvent event)
    {
        TileEntity te = event.getWorld().getTileEntity(event.getPos());
        if (te != null && te instanceof IOwnableTE)
        {
            IOwnableTE ownable = (IOwnableTE) te;
            ownable.setPlacer(event.getEntity());
        }
    }
}
