package pokecube.alternative.client.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.alternative.Reference;
import pokecube.alternative.container.BeltPlayerData;
import pokecube.alternative.container.IPokemobBelt;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;

public class GuiPokemonBar extends Gui
{
    public static boolean showAllTags     = true;
    public static boolean showSelectedTag = true;

    private Minecraft     mc;

    public GuiPokemonBar(Minecraft mc)
    {
        super();
        this.mc = mc;
    }

    @SubscribeEvent
    public void onRenderPokemonBar(RenderGameOverlayEvent event)
    {
        // Make sure that the bar renders after the experince bar
        if (event.isCancelable() || event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE
                || mc.currentScreen instanceof GuiChat) { return; }

        int i, j;
        GL11.glPushMatrix();
        float scaleFactor = 1;
        boolean flag = mc.isUnicode();
        i = mc.gameSettings.guiScale;
        int scaledWidth = mc.displayWidth;
        int scaledHeight = mc.displayHeight;
        if (i == 0)
        {
            i = 1000;
        }
        while (scaleFactor < i && scaledWidth / (scaleFactor + 1) >= 320 && scaledHeight / (scaleFactor + 1) >= 240)
        {
            ++scaleFactor;
        }

        if (flag && scaleFactor % 2 != 0 && scaleFactor != 1)
        {
            --scaleFactor;
        }
        float scaleFactor2 = 1;
        i = 1000;
        while (scaleFactor2 < i && scaledWidth / (scaleFactor2 + 1) >= 320 && scaledHeight / (scaleFactor2 + 1) >= 240)
        {
            ++scaleFactor2;
        }

        if (flag && scaleFactor2 % 2 != 0 && scaleFactor2 != 1)
        {
            --scaleFactor2;
        }
        scaleFactor2 *= 0.8f;
        // scaleFactor2 *= 2.5;
        GL11.glScaled(scaleFactor2 / scaleFactor, scaleFactor2 / scaleFactor, scaleFactor2 / scaleFactor);
        ResourceLocation bar = new ResourceLocation(Reference.MODID, "textures/gui/pokemon_hotbar.png");
        this.mc.renderEngine.bindTexture(bar);
        int texW = 24;
        int texH = 129;
        // Render the bar
        int xPos = 0; // Distance from left to start
        int yPos = (int) (70); // Distance from top to start

        // yPos = 0;

        this.drawTexturedModalRect(xPos, yPos, 0, 0, texW, texH);
        // Render the arrow
        IPokemobBelt capability = BeltPlayerData.getBelt(Minecraft.getMinecraft().thePlayer);
        int selected = capability.getSlot();
        int selectorSize = 21;
        int selectorXPos = 0;
        int selectorYPos = (yPos + 0) + selectorSize * (selected);
        this.drawTexturedModalRect(selectorXPos, selectorYPos, 0, 130, 25, 25);

        boolean infoBarsForAll = showAllTags;
        boolean infoBarForSelected = showSelectedTag;

        // Render the pokemon
        for (int pokemonNumber = 0; pokemonNumber < 6; pokemonNumber++)
        {
            ItemStack pokemonItemstack = capability.getCube(pokemonNumber);
            if (pokemonItemstack == null)
            {
                continue;
            }
            IPokemob pokemob = EventsHandlerClient.getPokemobForRender(pokemonItemstack, mc.theWorld);
            if (pokemob == null) continue;

            // Set the amount to shift by for the mob's index
            i = 12 + xPos;
            j = (yPos - 1) + (21 + selectorSize * pokemonNumber);
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
            float zoom = (float) (15f / Math.pow(size, 0.7));

            // Brightness
            int i1 = 15728880;
            int j1 = i1 % 65536;
            int k1 = i1 / 65536;

            // GL Calls to actually draw pokemob
            GL11.glPushMatrix();
            GL11.glTranslatef(i, j, 10f);
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

            if (infoBarsForAll || (infoBarForSelected && selected == pokemonNumber))
            {
                pokemob = PokecubeManager.itemToPokemob(pokemonItemstack, mc.theWorld);
                this.mc.renderEngine.bindTexture(bar);
                selectorXPos = i + 11;
                selectorYPos = j - 20;
                ITextComponent nameComp = pokemob.getPokemonDisplayName();
                float s = 0.75F;
                float plateSize = 32;
                int secondRow = 10;
                this.drawTexturedModalRect(selectorXPos, selectorYPos, 25, 0, 71, 25);
                GL11.glPushMatrix();
                // translate to start, then scale.
                GL11.glTranslatef(i + 11 + 0.5f, j - 13, 0);
                GL11.glScaled(s, s, s);
                // Draw name
                String name = I18n.format(nameComp.getFormattedText());
                name = mc.fontRendererObj.trimStringToWidth(name, 100);
                mc.fontRendererObj.drawString(name, 2, 0, 0xFFFFFF);
                // Draw gender
                int colour = pokemob.getSexe() == IPokemob.MALE ? 0x0011CC : 0xCC5555;
                String gender = pokemob.getSexe() == IPokemob.MALE ? "\u2642"
                        : pokemob.getSexe() == IPokemob.FEMALE ? "\u2640" : "";

                mc.fontRendererObj.drawString(gender,
                        (int) (plateSize / (s * 1) * 2) - 2 - mc.fontRendererObj.getStringWidth(gender), 0, colour);

                // Draw Level
                String lvlStr = "L." + pokemob.getLevel();
                mc.fontRendererObj.drawString(lvlStr, 2, secondRow, 0xFFFFFF);

                // Draw health
                entity = (EntityLiving) pokemob;
                float maxHealth = entity.getMaxHealth();
                float health = Math.min(maxHealth, entity.getHealth());
                String maxHpStr = "" + (int) (Math.round(maxHealth * 100.0) / 100.0);
                String hpStr = "" + (int) (Math.round(health * 100.0) / 100.0);
                String healthStr = hpStr + "/" + maxHpStr;
                mc.fontRendererObj.drawString(healthStr,
                        (int) (plateSize / (s * 1) * 2) - 2 - mc.fontRendererObj.getStringWidth(healthStr), secondRow,
                        0xFFFFFF);

                GL11.glPopMatrix();
            }

        }
        GL11.glPopMatrix();
    }
}
// glPushMatrix();
// GL11.glPushAttrib(GL11.GL_BLEND);
// GL11.glEnable(GL11.GL_BLEND);
// GL11.glTranslatef(i - 1, j - 8.5f, 9F);
// glPushMatrix();
// glTranslatef(0.5F, 1.0f, 0.5F);
// glRotatef(-180, 1.0F, 0.0F, 0.0F);
// float scale = 28f;
// glScalef(scale, scale, scale);
// GL11.glRotatef(-45, 0.0F, 1.0F, 0.0F);
// GL11.glRotatef(-30, 1.0F, 0.0F, 0.0F);
// RenderHelper.disableStandardItemLighting();
// Minecraft.getMinecraft().getItemRenderer().renderItem(mc.thePlayer,
// pokemonItemstack, TransformType.GUI);
// glPopMatrix();
// GL11.glDisable(GL11.GL_BLEND);
// GL11.glPopAttrib();
// glPopMatrix();