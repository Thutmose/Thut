package thut.wearables.client.render;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

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
        List<LayerRenderer<?>> layerRenderers = ReflectionHelper.getPrivateValue(RenderLivingBase.class,
                event.getRenderer(), "layerRenderers", "field_177097_h", "i");
        layerRenderers.add(1, new WearablesRenderer(event.getRenderer()));
        addedBaubles.add(event.getRenderer());
    }
}
