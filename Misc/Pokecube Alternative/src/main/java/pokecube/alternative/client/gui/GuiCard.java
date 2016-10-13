package pokecube.alternative.client.gui;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pokecube.alternative.Reference;
import pokecube.alternative.container.belt.BeltPlayerData;
import pokecube.alternative.container.belt.IPokemobBelt;
import pokecube.alternative.container.card.ContainerCard;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;

public class GuiCard extends InventoryEffectRenderer
{
    public static final ResourceLocation background = new ResourceLocation(Reference.MODID,
            "textures/gui/trainer_card.png");

    /** The old x position of the mouse pointer */
    private float                        oldMouseX;
    /** The old y position of the mouse pointer */
    private float                        oldMouseY;

    public GuiCard(EntityPlayer player)
    {
        super(new ContainerCard(player));
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
        GuiInventory.drawEntityOnScreen(i + 155, j + 58, 25, i + 51 - this.oldMouseX, j + 75 - 50 - this.oldMouseY,
                this.mc.thePlayer);
        IPokemobBelt belt = BeltPlayerData.getBelt(this.mc.thePlayer);
        int xPos = 135;
        int yPos = 15;
        int selectorSize = 9;
        String name = this.mc.thePlayer.getDisplayNameString();
        drawString(fontRendererObj, name, i + 7, j + 28, 0xffffff);
        String num = "" + CaptureStats.getNumberUniqueCaughtBy(this.mc.thePlayer.getCachedUniqueIdString());
        drawString(fontRendererObj, num, i + 7, j + 45, 0xffffff);

        for (int i1 = 0; i1 < 6; i1++)
        {
            // Set the amount to shift by for the mob's index
            yPos = 15 + i1 * selectorSize;
            ItemStack pokemonItemstack = belt.getCube(i1);
            if (pokemonItemstack == null) continue;
            IPokemob pokemob = EventsHandlerClient.getPokemobForRender(pokemonItemstack, mc.theWorld);
            if (pokemob == null) continue;
            EntityLiving entity = (EntityLiving) pokemob;

            // Set the mob's stance and rotation
            entity.rotationYaw = 0;
            entity.rotationPitch = 0;
            entity.rotationYawHead = 0;
            pokemob.setPokemonAIState(IMoveConstants.SITTING, true);
            entity.onGround = true;

            // Get the amount to scale the mob by
            float mobScale = pokemob.getSize();
            float size = Math.max(pokemob.getPokedexEntry().width * mobScale,
                    pokemob.getPokedexEntry().height * mobScale);
            float zoom = (float) (7f / Math.pow(size, 0.7));

            // Brightness
            int i2 = 15728880;
            int j1 = i2 % 65536;
            int k1 = i2 / 65536;

            // GL Calls to actually draw pokemob
            GL11.glPushMatrix();
            GL11.glTranslatef(i + xPos, j + yPos, 10f);
            GL11.glPushMatrix();
            GL11.glScalef(-zoom, zoom, zoom);
            GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
            RenderHelper.enableStandardItemLighting();
            GL11.glTranslatef(0.0F, (float) entity.getYOffset(), 0.0F);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j1 / 1.0F, k1 / 1.0F);
            Minecraft.getMinecraft().getRenderManager().doRenderEntity(entity, 0, 0, 0, 0, 1.5F, false);
            RenderHelper.disableStandardItemLighting();
            GL11.glPopMatrix();
            GL11.glPopMatrix();
        }
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
