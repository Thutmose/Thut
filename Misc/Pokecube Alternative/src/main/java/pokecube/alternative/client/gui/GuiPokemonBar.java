package pokecube.alternative.client.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.alternative.Reference;
import pokecube.alternative.capabilities.IBeltCapability;
import pokecube.alternative.event.EventHandlerCommon;
import pokecube.core.events.handlers.EventsHandlerClient;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;

public class GuiPokemonBar extends Gui
{

    private Minecraft mc;

    public GuiPokemonBar(Minecraft mc)
    {
        super();
        this.mc = mc;
    }

    @SubscribeEvent
    public void onRenderPokemonBar(RenderGameOverlayEvent event)
    {
        // Make sure that the bar renders after the experince bar
        if (event.isCancelable() || event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) { return; }
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
        scaleFactor2 *= 1.0f;
        GL11.glScaled(scaleFactor2 / scaleFactor, scaleFactor2 / scaleFactor, scaleFactor2 / scaleFactor);
        ResourceLocation bar = new ResourceLocation(Reference.MODID, "textures/gui/pokemon_hotbar.png");
        this.mc.renderEngine.bindTexture(bar);
        int texW = 24;
        int texH = 129;
        // Render the bar
        int xPos = 0; // Distance from left to start
        int yPos = (int) (80); // Distance from top to start
        this.drawTexturedModalRect(xPos, yPos, 0, 0, texW, texH);
        // Render the arrow
        IBeltCapability capability = Minecraft.getMinecraft().thePlayer.getCapability(EventHandlerCommon.BELTAI_CAP,
                null);
        int selected = capability.getSlot();
        int selectorSize = 21;
        int selectorXPos = 0;
        int selectorYPos = (yPos + 0) + selectorSize * (selected);
        this.drawTexturedModalRect(selectorXPos, selectorYPos, 0, 130, 25, 25);
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
            i = 12 + xPos;
            j = (yPos - 1) + (21 + selectorSize * pokemonNumber);
            GL11.glPushMatrix();
            GL11.glTranslatef(i, j, 0F);
            EntityLiving entity = (EntityLiving) pokemob;
            entity.rotationYaw = 0;
            entity.rotationPitch = 0;
            entity.rotationYawHead = 0;
            pokemob.setPokemonAIState(IMoveConstants.SITTING, true);
            entity.onGround = true;
            EventsHandlerClient.renderMob(pokemob, event.getPartialTicks(), false);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
    }
}
