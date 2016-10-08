package thut.wearables.client.render;

import java.util.List;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Sets;

import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;
import thut.wearables.network.PacketGui;

public class WearableEventHandler
{
    private Set<RenderPlayer> addedBaubles = Sets.newHashSet();
    KeyBinding[]              keys         = new KeyBinding[13];

    public WearableEventHandler()
    {
        for (int i = 0; i < 13; i++)
        {
            EnumWearable slot = EnumWearable.getWearable(i);
            int subIndex = EnumWearable.getSubIndex(i);
            String name = "Activate ";
            if (slot.slots == 1)
            {
                name = name + " " + slot;
            }
            else
            {
                name = name + " " + slot + " " + subIndex;
            }
            keys[i] = new KeyBinding(name, Keyboard.KEY_NONE, "Wearables");
            ClientRegistry.registerKeyBinding(keys[i]);
        }
    }

    @SubscribeEvent
    public void keyPress(KeyInputEvent event)
    {
        for (byte i = 0; i < 13; i++)
        {
            KeyBinding key = keys[i];
            if (key.isPressed())
            {
                PacketGui packet = new PacketGui();
                packet.data.setByte("S", i);
                ThutWearables.packetPipeline.sendToServer(packet);
            }
        }
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
