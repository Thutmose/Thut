package thut.wearables.client.gui;

import java.util.Map;

import com.google.common.collect.Maps;

import baubles.client.gui.GuiPlayerExpanded;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.wearables.ThutWearables;
import thut.wearables.network.PacketGui;

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
        if (event.getGui() instanceof GuiInventory || event.getGui() instanceof GuiWearables)
        {
            active = event.getGui() instanceof GuiWearables;
            GuiContainer gui = (GuiContainer) event.getGui();
            event.getButtonList().add(new GuiWearableButton(56, gui.guiLeft, gui.guiTop, 26, 9, 10, 10,
                    I18n.format(active ? "button.wearables.off" : "button.wearables.on", new Object[0])));
        }
    }

    @Method(modid = "Baubles")
    @SideOnly(value = Side.CLIENT)
    @SubscribeEvent
    public void guiBaublesInit_old(GuiScreenEvent.InitGuiEvent.Post event)
    {
        if (event.getGui() instanceof GuiPlayerExpanded)
        {
            active = event.getGui() instanceof GuiWearables;
            GuiContainer gui = (GuiContainer) event.getGui();
            event.getButtonList().add(new GuiWearableButton(56, gui.guiLeft, gui.guiTop, 26, 9, 10, 10,
                    I18n.format(active ? "button.wearables.off" : "button.wearables.on", new Object[0])));
        }
    }

    @Method(modid = "baubles")
    @SideOnly(value = Side.CLIENT)
    @SubscribeEvent
    public void guiBaublesInit(GuiScreenEvent.InitGuiEvent.Post event)
    {
        if (event.getGui() instanceof GuiPlayerExpanded)
        {
            active = event.getGui() instanceof GuiWearables;
            GuiContainer gui = (GuiContainer) event.getGui();
            event.getButtonList().add(new GuiWearableButton(56, gui.guiLeft, gui.guiTop, 26, 9, 10, 10,
                    I18n.format(active ? "button.wearables.off" : "button.wearables.on", new Object[0])));
        }
    }

    @SideOnly(value = Side.CLIENT)
    @SubscribeEvent
    public void guiPostAction(GuiScreenEvent.ActionPerformedEvent.Post event)
    {
        if (event.getGui() instanceof GuiInventory || event.getGui() instanceof GuiWearables)
        {
            if (event.getButton() instanceof GuiWearableButton)
            {
                active = event.getGui() instanceof GuiWearables;
                if (active)
                {
                    event.getGui().mc.displayGuiScreen(new GuiInventory(event.getGui().mc.thePlayer));
                }
                else
                {
                    PacketGui packet = new PacketGui();
                    ThutWearables.packetPipeline.sendToServer(packet);
                }
                active = !active;
                event.getButton().displayString = I18n.format(active ? "button.wearables.off" : "button.wearables.on",
                        new Object[0]);
            }
        }
    }

    @Method(modid = "baubles")
    @SideOnly(value = Side.CLIENT)
    @SubscribeEvent
    public void guiPostBaubles(GuiScreenEvent.ActionPerformedEvent.Post event)
    {
        if (event.getGui() instanceof GuiPlayerExpanded)
        {
            if (event.getButton() instanceof GuiWearableButton)
            {
                active = false;
                PacketGui packet = new PacketGui();
                ThutWearables.packetPipeline.sendToServer(packet);
                active = !active;
                event.getButton().displayString = I18n.format(active ? "button.wearables.off" : "button.wearables.on",
                        new Object[0]);
            }
        }
    }

    @Method(modid = "Baubles")
    @SideOnly(value = Side.CLIENT)
    @SubscribeEvent
    public void guiPostBaubles_old(GuiScreenEvent.ActionPerformedEvent.Post event)
    {
        if (event.getGui() instanceof GuiPlayerExpanded)
        {
            if (event.getButton() instanceof GuiWearableButton)
            {
                active = false;
                PacketGui packet = new PacketGui();
                ThutWearables.packetPipeline.sendToServer(packet);
                active = !active;
                event.getButton().displayString = I18n.format(active ? "button.wearables.off" : "button.wearables.on",
                        new Object[0]);
            }
        }
    }
}
