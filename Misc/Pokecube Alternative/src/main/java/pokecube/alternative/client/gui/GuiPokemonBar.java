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

public class GuiPokemonBar extends Gui {

    private Minecraft mc;

    public GuiPokemonBar(Minecraft mc) {
        super();
        this.mc = mc;
    }

    @SubscribeEvent
    public void onRenderPokemonBar(RenderGameOverlayEvent event) {
        //Make sure that the bar renders after the experince bar
        if(event.isCancelable() || event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            return;
        }

        //Render the bar
        int xPos = 0; //Distance from left to start
        int yPos = 70; //Distance from top to start
        ResourceLocation bar = new ResourceLocation(Reference.MODID, "textures/gui/pokemon_hotbar.png");
        this.mc.renderEngine.bindTexture(bar);
        this.drawTexturedModalRect(xPos, yPos, 0, 0, 32, 120);
        //Render the arrow
        IBeltCapability capability = Minecraft.getMinecraft().thePlayer.getCapability(EventHandlerCommon.BELTAI_CAP, null);
        int selected = capability.getSlot();
        int selectorXPos = 2;
        int selectorYPos = (yPos + 11) + 18 * (selected);
        this.drawTexturedModalRect(selectorXPos, selectorYPos, 33, 0, 4, 7);
        //Render the pokemon
        for(int pokemonNumber = 0; pokemonNumber < 6; pokemonNumber++) {
            ItemStack pokemonItemstack = capability.getCube(pokemonNumber);
            if (pokemonItemstack == null) {
                continue;
            }
            IPokemob pokemob = EventsHandlerClient.getPokemobForRender(pokemonItemstack, mc.theWorld);
            if (pokemob == null) continue;
            int i, j;
            i = 15 + xPos;
            j = (yPos - 1) + (21 + 18 * pokemonNumber);
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
    }
}
