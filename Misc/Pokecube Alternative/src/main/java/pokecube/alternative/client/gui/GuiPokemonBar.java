package pokecube.alternative.client.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
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
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
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
            IPokemob realMob = null;
            boolean inWorld = false;
            if (CompatWrapper.isValid(pokemonItemstack))
            {
                try
                {
                    realMob = PokecubeManager.itemToPokemob(pokemonItemstack.copy(), mc.world);
                    entity = realMob.getEntity();
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (entity == null) continue;

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
                float relHp = ((float) realMob.getStat(Stats.HP, true)) / realMob.getStat(Stats.HP, false);
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

            // GL Calls to actually draw pokemob's Icon
            GL11.glPushMatrix();
            GL11.glTranslatef(i, j, 10f);
            GL11.glPushMatrix();
            EventsHandlerClient.renderIcon(realMob, -10, -12, 16, 16);
            GL11.glPopMatrix();
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
    }
}