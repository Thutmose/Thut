package thut.wearables.client.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.ContainerWearables;

public class GuiWearables extends InventoryEffectRenderer
{
    public static final ResourceLocation background = new ResourceLocation(ThutWearables.MODID,
            "textures/gui/wearables.png");

    /** The old x position of the mouse pointer */
    private float                        oldMouseX;
    /** The old y position of the mouse pointer */
    private float                        oldMouseY;

    public GuiWearables(EntityPlayer player)
    {
        super(new ContainerWearables(player));
    }

    /** Called from the main game loop to update the screen. */
    @Override
    public void updateScreen()
    {
        super.updateScreen();
    }

    /** Adds the buttons (and other controls) to the screen in question. */
    @Override
    public void initGui()
    {
        this.buttonList.clear();
        super.initGui();
    }

    /** Draw the foreground layer for the GuiContainer (everything in front of
     * the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    /** Draws the screen and all the components in it. */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.oldMouseX = mouseX;
        this.oldMouseY = mouseY;
    }

    /** Draws either a gradient over the background screen (when it exists) or a
     * flat gradient over background.png */
    @Override
    public void drawDefaultBackground()
    {
        super.drawDefaultBackground();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        GuiInventory.drawEntityOnScreen(i + 51, j + 75, 30, i + 51 - this.oldMouseX,
                j + 75 - 50 - this.oldMouseY, this.mc.thePlayer);
    }

    /** Called when the mouse is clicked. Args : mouseX, mouseY,
     * clickedButton */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
    }
}
