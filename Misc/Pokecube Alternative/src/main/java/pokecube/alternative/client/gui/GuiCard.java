package pokecube.alternative.client.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pokecube.alternative.Config;
import pokecube.alternative.Reference;
import pokecube.alternative.container.belt.BeltPlayerData;
import pokecube.alternative.container.belt.IPokemobBelt;
import pokecube.alternative.container.card.ContainerCard;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import thut.lib.CompatWrapper;

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

    /** Draws the screen and all the components in it. */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.oldMouseX = mouseX;
        this.oldMouseY = mouseY;
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);
        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        int playerX = 100;
        int playerY = 63;
        GuiInventory.drawEntityOnScreen(i + playerX, j + playerY, 25, i + 51 - this.oldMouseX,
                j + 75 - 50 - this.oldMouseY, this.mc.player);

        /** If config isn't enabled, there is no belt to render mobs from, so
         * return early. */
        if (!Config.instance.isEnabled) return;

        /** Render the mobs in the belt. */
        IPokemobBelt belt = BeltPlayerData.getBelt(this.mc.player);
        String name = this.mc.player.getDisplayNameString();
        drawString(fontRenderer, name, i + 7, j + 28, 0xffffff);
        String num = "" + CaptureStats.getNumberUniqueCaughtBy(this.mc.player.getUniqueID());
        drawString(fontRenderer, num, i + 7, j + 45, 0xffffff);
        float scaleA = 12;
        int selectorSize = 20;
        int x0 = 133;
        int y0 = 20;
        int xPos = x0;
        int yPos = y0;
        int columnGap = 5;
        for (int i1 = 0; i1 < 6; i1++)
        {
            // Set the amount to shift by for the mob's index
            yPos = y0 + (i1 % 3) * selectorSize;
            xPos = x0 + selectorSize * (i1 / 3) + (i1 / 3) * columnGap;
            ItemStack pokemonItemstack = belt.getCube(i1);
            if (!CompatWrapper.isValid(pokemonItemstack)) continue;
            IPokemob pokemob = EventsHandlerClient.getPokemobForRender(pokemonItemstack, mc.world);
            if (pokemob == null) continue;
            EntityLiving entity = pokemob.getEntity();

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
            float zoom = (float) (scaleA / Math.pow(size, 0.7));

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
}
