package thut.wearables.client.gui;

import java.util.Map;

import org.lwjgl.input.Mouse;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiEvents
{
    public static Map<String, int[]> whitelistedGuis = Maps.newHashMap();

    static
    {
        whitelistedGuis.put("net.minecraft.client.gui.inventory.GuiInventory", new int[2]);
    }

    public static void init()
    {
        MinecraftForge.EVENT_BUS.register(new GuiEvents());
    }

    private boolean active = false;

    public GuiEvents()
    {
    }

    @SideOnly(value = Side.CLIENT)
    @SubscribeEvent
    public void guiPostInit(GuiScreenEvent.InitGuiEvent.Post event)
    {
        if (event.getGui() instanceof GuiInventory)
        {
            GuiContainer gui = (GuiContainer) event.getGui();
            event.getButtonList().add(new GuiWearableButton(56, gui.guiLeft, gui.guiTop, 26, 9, 10, 10,
                    I18n.format(active ? "button.wearables.off" : "button.wearables.on", new Object[0])));
        }
    }

    GuiScreen    lastcontainer;
    GuiWearables gui;

    @SideOnly(value = Side.CLIENT)
    @SubscribeEvent
    public void guiEvent(GuiScreenEvent event)
    {
        if (event.getGui() == gui || !active) return;
        if (event.getGui() instanceof GuiContainer && whitelistedGuis.containsKey(event.getGui().getClass().getName()))
        {
            if (event.getGui() != lastcontainer)
            {
                int[] offset = whitelistedGuis.get(event.getGui().getClass().getName());
                gui = new GuiWearables(Minecraft.getMinecraft().thePlayer, event.getGui());
                lastcontainer = event.getGui();
                gui.initToOther(lastcontainer);
                gui.guiTop += offset[0];
                gui.guiLeft += offset[1] - 25;
            }
            if (event instanceof InitGuiEvent.Post)
            {

            }
            if (event instanceof DrawScreenEvent.Post)
            {
                DrawScreenEvent.Post evt = (DrawScreenEvent.Post) event;
                try
                {
                    gui.drawScreen(evt.getMouseX(), evt.getMouseY(), evt.getRenderPartialTicks());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            if (event instanceof ActionPerformedEvent.Post)
            {

            }
            if (event instanceof MouseInputEvent.Post)
            {
                try
                {
                    if (Mouse.getEventButtonState())
                    {
                        int i = Mouse.getEventX() * gui.width / gui.mc.displayWidth;
                        int j = gui.height - Mouse.getEventY() * gui.height / gui.mc.displayHeight - 1;
                        int k = Mouse.getEventButton();
                        gui.mouseClicked(i, j, k);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            if (event instanceof KeyboardInputEvent.Post)
            {
                try
                {
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @SideOnly(value = Side.CLIENT)
    @SubscribeEvent
    public void guiPostAction(GuiScreenEvent.ActionPerformedEvent.Post event)
    {
        if (event.getGui() instanceof GuiInventory)
        {
            if (event.getButton() instanceof GuiWearableButton)
            {
                active = !active;
                event.getButton().displayString = I18n.format(active ? "button.wearables.off" : "button.wearables.on",
                        new Object[0]);
            }
        }
    }
}
