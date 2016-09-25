package pokecube.alternative.client.keybindings;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import pokecube.alternative.client.gui.GuiPokemonBar;
import pokecube.alternative.container.BeltPlayerData;
import pokecube.alternative.container.IPokemobBelt;
import pokecube.alternative.network.PacketHandler;
import pokecube.alternative.network.PacketKeyUse;
import pokecube.core.client.ClientProxyPokecube;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.interfaces.IPokemob;

public class KeyHandler
{

    public static KeyBinding nextPoke;
    public static KeyBinding prevPoke;
    public static KeyBinding sendOutPoke;
    public static KeyBinding toggleBarControl;
    public static KeyBinding cycleGuiState;

    public static void init()
    {
        nextPoke = new KeyBinding(I18n.format("keybind.nextpoke"), Keyboard.KEY_DOWN,
                I18n.format("key.categories.pokecube_alternative"));
        prevPoke = new KeyBinding(I18n.format("keybind.prevpoke"), Keyboard.KEY_UP,
                I18n.format("key.categories.pokecube_alternative"));
        toggleBarControl = new KeyBinding(I18n.format("keybind.togglebarcontrol"), Keyboard.KEY_LCONTROL,
                I18n.format("key.categories.pokecube_alternative"));
        cycleGuiState = new KeyBinding(I18n.format("keybind.cycleGuiState"), Keyboard.KEY_O,
                I18n.format("key.categories.pokecube_alternative"));
        ClientRegistry.registerKeyBinding(nextPoke);
        ClientRegistry.registerKeyBinding(prevPoke);
        ClientRegistry.registerKeyBinding(toggleBarControl);
        ClientRegistry.registerKeyBinding(cycleGuiState);
    }

    long ticks = 0;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event)
    {
        sendOutPoke = ClientProxyPokecube.mobBack;
        IPokemob mob = GuiDisplayPokecubeInfo.instance().getCurrentPokemob();
        if (toggleBarControl.isKeyDown() || mob == null)
        {
            if (nextPoke.isPressed() && nextPoke.isKeyDown())
            {
                PacketKeyUse packet = new PacketKeyUse(PacketKeyUse.SLOTDOWN);
                PacketHandler.INSTANCE.sendToServer(packet);
            }
            else if (prevPoke.isPressed() && prevPoke.isKeyDown())
            {
                PacketKeyUse packet = new PacketKeyUse(PacketKeyUse.SLOTUP);
                PacketHandler.INSTANCE.sendToServer(packet);
            }
        }
        if (cycleGuiState.isPressed())
        {
            int state = 0;
            if (GuiPokemonBar.showAllTags) state += 2;
            if (GuiPokemonBar.showSelectedTag) state += 1;
            if (GuiScreen.isShiftKeyDown()) state = (state - 1) % 4;
            else state = (state + 1) % 3;
            GuiPokemonBar.showAllTags = (state & 2) > 0;
            GuiPokemonBar.showSelectedTag = (state & 1) > 0;
        }
        if (sendOutPoke.isPressed())
        {
            IPokemobBelt cap = BeltPlayerData.getBelt(Minecraft.getMinecraft().thePlayer);
            if (cap.getCube(cap.getSlot()) != null) ticks = Minecraft.getSystemTime();
            else
            {
                if (mob != null)
                {
                    PacketKeyUse packet = new PacketKeyUse(PacketKeyUse.RECALL, ((Entity) mob).getEntityId());
                    PacketHandler.INSTANCE.sendToServer(packet);
                }
                ticks = 0;
            }
        }
        else if (ticks != 0)
        {
            long diff = Minecraft.getSystemTime() - ticks;
            int dt = (int) Math.max(2000 - diff / 20, 1000);
            PacketKeyUse packet = new PacketKeyUse(PacketKeyUse.SENDOUT, dt);
            PacketHandler.INSTANCE.sendToServer(packet);
            ticks = 0;
        }
    }

}
