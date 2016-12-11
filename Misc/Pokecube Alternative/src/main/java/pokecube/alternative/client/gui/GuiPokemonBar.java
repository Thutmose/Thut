package pokecube.alternative.client.gui;

import java.util.List;
import java.util.UUID;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
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
import pokecube.core.items.pokecubes.PokecubeManager;
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
        if (event.isCancelable() || event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE
                || mc.currentScreen instanceof GuiChat) { return; }
        int i, j;
        GL11.glPushMatrix();
        GuiDisplayPokecubeInfo.applyTransform("middle_left", new int[] { 0, Config.instance.shift },
                new int[] { 130, 25 }, Config.instance.scale);
        ResourceLocation bar = new ResourceLocation(Reference.MODID, "textures/gui/pokemon_hotbar.png");
        this.mc.renderEngine.bindTexture(bar);
        int texW = 25;
        int texH = 130;
        // Render the bar
        int xPos = 0; // Distance from left to start
        int yPos = -texH / 2; // Distance from top to start

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
            // Set the amount to shift by for the mob's index
            i = 12 + xPos;
            j = (yPos - 1) + (21 + selectorSize * pokemonNumber);

            EntityLiving entity = null;
            ItemStack pokemonItemstack = capability.getCube(pokemonNumber);
            if (!CompatWrapper.isValid(pokemonItemstack)) continue;
            IPokemob pokemob = null;// =
            if (capability.isOut(pokemonNumber))
            {
                final UUID id = PokecubeManager.getUUID(pokemonItemstack);
                List<EntityLivingBase> pokemobs = mc.theWorld.getEntities(EntityLivingBase.class,
                        new Predicate<EntityLivingBase>()
                        {
                            @Override
                            public boolean apply(EntityLivingBase input)
                            {
                                return input.getUniqueID().equals(id);
                            }
                        });
                if (!pokemobs.isEmpty())
                {
                    entity = (EntityLiving) pokemobs.get(0);
                    pokemob = EventsHandlerClient.getPokemobForRender(PokecubeManager.pokemobToItem((IPokemob) entity),
                            mc.theWorld);
                }
            }
            if (pokemob == null)
            {
                pokemob = EventsHandlerClient.getPokemobForRender(pokemonItemstack.copy(), mc.theWorld);
                entity = (EntityLiving) pokemob;
            }
            if (pokemob == null || entity == null) continue;

            // Set the mob's stance and rotation
            ((EntityLiving) pokemob).rotationYaw = 0;
            ((EntityLiving) pokemob).rotationPitch = 0;
            ((EntityLiving) pokemob).rotationYawHead = 0;
            pokemob.setPokemonAIState(IMoveConstants.SITTING, true);
            ((EntityLiving) pokemob).onGround = true;

            // Get the amount to scale the mob by
            float mobScale = pokemob.getSize();
            float size = Math.max(pokemob.getPokedexEntry().width * mobScale,
                    pokemob.getPokedexEntry().height * mobScale);
            float zoom = (float) (15f / Math.pow(size, 0.7));

            // Brightness
            int i1 = 15728880;
            int j1 = i1 % 65536;
            int k1 = i1 / 65536;

            if (infoBarsForAll || (infoBarForSelected && selected == pokemonNumber))
            {
                this.mc.renderEngine.bindTexture(bar);
                selectorXPos = i + 12;
                selectorYPos = j - 20;
                ITextComponent nameComp = pokemob.getPokemonDisplayName();
                float s = 0.75F;
                float plateSize = 31;
                this.drawTexturedModalRect(selectorXPos, selectorYPos, 25, 0, 71, 25);
                GL11.glPushMatrix();
                // translate to start, then scale.
                GL11.glTranslatef(i + 12 + 0.5f, j - 14, 0);
                GL11.glScaled(s, s, s);
                // Draw name
                String name = I18n.format(nameComp.getFormattedText());
                name = mc.fontRendererObj.trimStringToWidth(name, 55);
                mc.fontRendererObj.drawString(name, 2, 0, 0xFFFFFF);
                // Draw gender
                int colour = pokemob.getSexe() == IPokemob.MALE ? 0x0011CC : 0xCC5555;
                String gender = pokemob.getSexe() == IPokemob.MALE ? "\u2642"
                        : pokemob.getSexe() == IPokemob.FEMALE ? "\u2640" : "";
                mc.fontRendererObj.drawString(gender,
                        (int) (plateSize / (s * 1) * 2) - 2 - mc.fontRendererObj.getStringWidth(gender), 8, colour);

                // Draw Level
                GL11.glScaled(1 / s, 1 / s, 1 / s);
                s = 0.5f;
                GL11.glScaled(s, s, s);
                GL11.glTranslatef(0, 0.5f, 0);
                String lvlStr = "L." + pokemob.getLevel();
                int lvlLength = mc.fontRendererObj.getStringWidth(lvlStr);
                mc.fontRendererObj.drawString(lvlStr, (int) (plateSize / (s * 1) * 2) - 3 - lvlLength, 3, 0xFFFFFF);
                GL11.glPopMatrix();

                // Draw health
                selectorXPos = i + 13;
                selectorYPos = j - 7;
                this.mc.renderEngine.bindTexture(bar);
                float relHp = entity.getHealth() / entity.getMaxHealth();
                this.drawTexturedModalRect(selectorXPos, selectorYPos, 2, 159, (int) (56 * relHp), 3);

                // Draw EXP
                int exp = pokemob.getExp() - Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel());
                float maxExp = Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel() + 1)
                        - Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel());
                if (pokemob.getLevel() == 100) maxExp = exp = 1;
                float expSize = exp / maxExp;
                this.drawTexturedModalRect(selectorXPos, selectorYPos + 3, 2, 167, (int) (53 * expSize), 2);
            }

            // GL Calls to actually draw pokemob
            GL11.glPushMatrix();
            GL11.glTranslatef(i, j, 10f);
            GL11.glPushMatrix();
            GL11.glScalef(-zoom, zoom, zoom);
            GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
            RenderHelper.enableStandardItemLighting();
            GL11.glTranslatef(0.0F, (float) ((EntityLiving) pokemob).getYOffset(), 0.0F);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j1 / 1.0F, k1 / 1.0F);
            Minecraft.getMinecraft().getRenderManager().doRenderEntity(((EntityLiving) pokemob), 0, 0, 0, 0, 1.5F,
                    false);
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