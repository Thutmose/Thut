package pokecube.alternative.client.gui;

import java.util.UUID;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.alternative.Config;
import pokecube.alternative.Reference;
import pokecube.alternative.container.belt.BeltPlayerData;
import pokecube.alternative.container.belt.IPokemobBelt;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.Tools;
import thut.lib.CompatWrapper;

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
        if (!Config.instance.isEnabled || event.isCancelable()
                || event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE
                || mc.currentScreen instanceof GuiChat) { return; }
        int i, j;
        GL11.glPushMatrix();
        GuiDisplayPokecubeInfo.applyTransform("middle_left", new int[] { 0, Config.instance.shift },
                new int[] { 130, 25 }, Config.instance.scale);
        ResourceLocation bar = new ResourceLocation(Reference.MODID, "textures/gui/pokemon_hotbar.png");
        this.mc.renderEngine.bindTexture(bar);
        int texW = 27;
        int texH = 167;
        // Render the bar
        int xPos = 6; // Distance from left to start
        int yPos = -texH / 2; // Distance from top to start

        int x0 = 11;
        int y0 = 0;
        // yPos = 0;

        this.drawTexturedModalRect(xPos, yPos, x0, y0, texW, texH);

        boolean infoBarsForAll = showAllTags;
        boolean infoBarForSelected = showSelectedTag;

        IPokemobBelt capability = BeltPlayerData.getBelt(Minecraft.getMinecraft().player);
        int selected = capability.getSlot();
        int selectorSize = 28;

        // Render the arrow
        int selectorXPos = 0;
        int selectorYPos = (yPos + 0) + selectorSize * (selected);
        this.drawTexturedModalRect(selectorXPos, selectorYPos, -xPos - 1, texH + 1, 30, 25);

        // Render the pokemon
        for (int pokemonNumber = 0; pokemonNumber < 6; pokemonNumber++)
        {
            // Set the amount to shift by for the mob's index
            i = 14 + xPos;
            j = (yPos - 1) + (21 + selectorSize * pokemonNumber);

            EntityLiving entity = null;
            ItemStack pokemonItemstack = capability.getCube(pokemonNumber);
            if (!CompatWrapper.isValid(pokemonItemstack) && capability.getSlotID(pokemonNumber) == null) continue;
            IPokemob pokemob = null;
            IPokemob realMob = null;
            boolean inWorld = false;
            if (pokemob == null && CompatWrapper.isValid(pokemonItemstack))
            {
                try
                {
                    pokemob = realMob = EventsHandlerClient.getPokemobForRender(pokemonItemstack.copy(), mc.world);
                    entity = pokemob.getEntity();
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if (pokemob == null && capability.getSlotID(pokemonNumber) != null)
            {
                UUID id = capability.getSlotID(pokemonNumber);
                for (IPokemob testMob : GuiDisplayPokecubeInfo.instance().getPokemobsToDisplay())
                {
                    if (testMob != null && testMob.getEntity().getUniqueID().equals(id))
                    {
                        pokemob = realMob = testMob;
                        entity = testMob.getEntity();
                        inWorld = true;
                        break;
                    }
                }
            }
            if (pokemob != null)
            {
                pokemob = EventsHandlerClient.getRenderMob(pokemob.getPokedexEntry(), mc.world);
            }

            if (pokemob == null || entity == null) continue;

            // Set the mob's stance and rotation
            pokemob.getEntity().rotationYaw = 0;
            pokemob.getEntity().renderYawOffset = 0;
            pokemob.getEntity().prevRenderYawOffset = 0;
            pokemob.getEntity().prevRotationPitch = 0;
            pokemob.getEntity().ticksExisted = 0;
            pokemob.getEntity().rotationPitch = 0;
            pokemob.getEntity().prevRotationYawHead = 0;
            pokemob.getEntity().rotationYawHead = 0;
            pokemob.setPokemonAIState(IMoveConstants.SITTING, true);
            pokemob.getEntity().onGround = true;

            // Get the amount to scale the mob by
            float mobScale = realMob.getSize();
            float size = Math.max(realMob.getPokedexEntry().width * mobScale,
                    realMob.getPokedexEntry().height * mobScale);
            float zoom = (float) (15f / Math.pow(size, 0.7));

            // Brightness
            int i1 = 15728880;
            int j1 = i1 % 65536;
            int k1 = i1 / 65536;
            if (infoBarsForAll || (infoBarForSelected && selected == pokemonNumber))
            {
                // Draw background plate.
                this.mc.renderEngine.bindTexture(bar);
                selectorXPos = i + 7;
                selectorYPos = j - 20;
                ITextComponent nameComp = realMob.getPokemonDisplayName();
                float s = 0.75F;
                this.drawTexturedModalRect(selectorXPos, selectorYPos, 38, 0, 75, 26);
                this.drawTexturedModalRect(0, selectorYPos, 0, 0, 10, 26);

                // Draw health
                selectorXPos = i + 18;
                selectorYPos = j - 6;
                this.mc.renderEngine.bindTexture(bar);
                int healthLength = 56;
                float relHp = entity.getHealth() / entity.getMaxHealth();
                this.drawTexturedModalRect(selectorXPos, selectorYPos, 2, 197, (int) (healthLength * relHp), 3);

                // Draw EXP
                int exp = realMob.getExp() - Tools.levelToXp(realMob.getExperienceMode(), realMob.getLevel());
                float maxExp = Tools.levelToXp(realMob.getExperienceMode(), realMob.getLevel() + 1)
                        - Tools.levelToXp(realMob.getExperienceMode(), realMob.getLevel());
                if (realMob.getLevel() == 100) maxExp = exp = 1;
                int expLength = 52;
                float expSize = exp / maxExp;
                this.drawTexturedModalRect(selectorXPos - 2, selectorYPos + 6, 3, 205, (int) (expLength * expSize), 2);

                // Draw text things over
                GL11.glPushMatrix();
                // translate to start, then scale.
                GL11.glTranslatef(i + 12 + 0.5f, j - 14, 0);
                GL11.glScaled(s, s, s);
                // Draw name
                String name = I18n.format(nameComp.getFormattedText());
                name = mc.fontRenderer.trimStringToWidth(name, 55);
                mc.fontRenderer.drawString(name, 2, 0, inWorld ? 0x8888FF : relHp == 0 ? 0xFF8888 : 0xFFFFFF);
                // Undo scaling
                GL11.glScaled(1 / s, 1 / s, 1 / s);

                // Draw gender
                int colour = realMob.getSexe() == IPokemob.MALE ? 0x0011FF : 0xCC5555;
                String gender = realMob.getSexe() == IPokemob.MALE ? "\u2642"
                        : realMob.getSexe() == IPokemob.FEMALE ? "\u2640" : "";
                int sexX = -30;
                int sexY = 10;
                mc.fontRenderer.drawString(gender, sexX, sexY, colour);

                // Draw Level
                s = 0.75f;
                GL11.glScaled(s, s, s);
                GL11.glTranslatef(0, 0.5f, 0);
                String lvlStr = "L." + realMob.getLevel();

                int lvlx = (int) 85 - mc.fontRenderer.getStringWidth(lvlStr);
                int lvly = 0;
                mc.fontRenderer.drawString(lvlStr, lvlx, lvly, 0xffb80e);
                GL11.glPopMatrix();
            }

            // GL Calls to actually draw pokemob
            GL11.glPushMatrix();
            GL11.glTranslatef(i, j, 10f);
            GL11.glPushMatrix();
            GL11.glScalef(-zoom, zoom, zoom);
            GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
            RenderHelper.enableStandardItemLighting();
            GL11.glTranslatef(0.0F, (float) pokemob.getEntity().getYOffset(), 0.0F);
            if (entity.getHealth() == 0)
            {
                pokemob.getEntity().deathTime = 2;
                GL11.glTranslatef(-pokemob.getSize() * pokemob.getPokedexEntry().height * 0.5f, 0.0f, 0.0F);
            }
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j1 / 1.0F, k1 / 1.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getMinecraft().getRenderManager().doRenderEntity(pokemob.getEntity(), 0, 0, 0, 0, 1.5F, false);
            pokemob.getEntity().deathTime = 0;
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.disableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glPopMatrix();
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
    }
}