package pokecube.alternative.event;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pokecube.alternative.client.render.BeltRenderer;

public class BeltOverlayEventHandler
{

    private Set<RenderPlayer> addedBaubles = Sets.newHashSet();

    @SubscribeEvent
    public void renderGameOverlay(RenderGameOverlayEvent event)
    {

    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void addBaubleRender(RenderPlayerEvent.Post event)
    {
        if (addedBaubles.contains(event.getRenderer())) { return; }
        event.getRenderer().addLayer(new BeltRenderer(event.getRenderer()));
        addedBaubles.add(event.getRenderer());
    }

}
