package thut.wearables.client.render;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class WearableEventHandler
{
    private Set<RenderPlayer> addedBaubles = Sets.newHashSet();

    public WearableEventHandler()
    {

    }

    @SubscribeEvent
    public void addBaubleRender(RenderPlayerEvent.Post event)
    {
        if (addedBaubles.contains(event.getRenderer())) { return; }
        event.getRenderer().addLayer(new WearablesRenderer(event.getRenderer()));
        addedBaubles.add(event.getRenderer());
    }
}
