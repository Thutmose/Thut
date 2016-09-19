package pokecube.alternative.client.gui;

import java.util.Set;

import org.lwjgl.input.Mouse;

import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent.Post;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiEvents
{
    public static Set<String> whitelistedGuis = Sets.newHashSet();

    @SideOnly(value = Side.CLIENT)
    @SubscribeEvent
    public void guiPostInit(GuiScreenEvent.InitGuiEvent event)
    {
        if (event.getGui() instanceof GuiInventory || event.getGui() instanceof GuiPlayerPokemon)
        {
            // GuiContainer gui = (GuiContainer) event.getGui();
            // event.getButtonList()
            // .add(new GuiPokemonButton(42, gui.guiLeft, gui.guiTop, 64, 21,
            // 10, 10,
            // I18n.format((event.getGui() instanceof GuiInventory)
            // ? Reference.MODID + ".pokebelt.inventoryButton"
            // : Reference.MODID + ".pokebelt.normalInventoryButton", new
            // Object[0])));
        }
    }

    @SideOnly(value = Side.CLIENT)
    @SubscribeEvent
    public void guiPostAction(GuiScreenEvent.ActionPerformedEvent.Post event)
    {
        if (event.getGui() instanceof GuiInventory)
        {
            // if (event.getButton().id == 42)
            // {
            // PacketHandler.INSTANCE.sendToServer(new
            // PacketOpenPokemonInventory());
            // }
        }
    }

    GuiScreen        lastcontainer;
    GuiPlayerPokemon gui;

    @SideOnly(value = Side.CLIENT)
    @SubscribeEvent
    public void guiEvent(GuiScreenEvent event)
    {
        if (event.getGui() == gui) return;
        // if (event.getGui() != null) return;

        if (!whitelistedGuis.contains("pokecube.core.client.gui.blocks.GuiHealTable"))
        {
            whitelistedGuis.add("pokecube.core.client.gui.blocks.GuiHealTable");
            whitelistedGuis.add("pokecube.core.client.gui.blocks.GuiPC");
            whitelistedGuis.add("pokecube.core.client.gui.blocks.GuiTradingTable");
        }

        if (event.getGui() instanceof GuiContainer && whitelistedGuis.contains(event.getGui().getClass().getName()))
        {
            if (event.getGui() != lastcontainer)
            {
                gui = new GuiPlayerPokemon(Minecraft.getMinecraft().thePlayer);
                lastcontainer = event.getGui();
                gui.initToOther(lastcontainer);
            }
            if (event instanceof InitGuiEvent.Post)
            {

            }
            if (event instanceof DrawScreenEvent.Post)
            {
                DrawScreenEvent.Post evt = (Post) event;
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

}
