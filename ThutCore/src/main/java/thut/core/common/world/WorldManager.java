package thut.core.common.world;

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.world.World;

public class WorldManager
{
    private static WorldManager instance = new WorldManager();

    public static WorldManager instance()
    {
        return instance;
    }

    Map<Integer, World> worldDimMap = Maps.newHashMap();

    @SubscribeEvent
    public void WorldLoadEvent(Load evt)
    {
        worldDimMap.put(evt.getWorld().provider.getDimension(), new World_Impl(evt.getWorld()));
    }

    @SubscribeEvent
    public void WorldUnLoadEvent(Unload evt)
    {
        worldDimMap.remove(evt.getWorld().provider.getDimension());
    }

    @Nullable
    public World getWorld(Integer dim)
    {
        return worldDimMap.get(dim);
    }
}
