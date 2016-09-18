/**
 *
 */
package pokecube.wiki;

import static pokecube.core.utils.PokeType.flying;

import java.io.IOException;
import java.util.HashMap;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.FMLClientHandler;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;

/** @author Manchou */
public class GuiGifCapture extends GuiScreen
{
    protected IPokemob         pokemob          = null;
    protected EntityPlayer     entityPlayer     = null;

    public static PokedexEntry pokedexEntry     = null;

    /** The X size of the inventory window in pixels. */
    protected int              xSize            = 127;

    /** The Y size of the inventory window in pixels. */
    protected int              ySize            = 180;  // old:166
    private float              yRenderAngle     = 10;
    private float              xRenderAngle     = 0;
    private float              yHeadRenderAngle = 10;
    private float              xHeadRenderAngle = 0;
    private int                mouseRotateControl;
    public static boolean      shiny            = false;

    /**
     *
     */
    public GuiGifCapture(IPokemob pokemob, EntityPlayer entityPlayer)
    {
        this.pokemob = pokemob;
        this.entityPlayer = entityPlayer;
        if (pokemob != null)
        {
            pokedexEntry = pokemob.getPokedexEntry();
        }
        else if (pokedexEntry == null)
        {
            pokedexEntry = Pokedex.getInstance().getFirstEntry();
        }
    }

    @Override
    public void initGui()
    {
        x = this.width;
        y = this.height;
        buttonList.clear();
    }

    @Override
    protected void keyTyped(char par1, int par2)
    {
        if (par2 != 54 && par2 != 58 && par2 != 42 && par2 != 199)
        {
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        }
    }

    int prevX = 0;
    int prevY = 0;

    @Override
    public void handleMouseInput() throws IOException
    {
        int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        this.handleMouseMove(x, y, Mouse.getEventButton());
        super.handleMouseInput();
    }

    private void handleMouseMove(int x, int y, int mouseButton)
    {
        // System.out.println("handleMouseMove("+x+", "+y+", "+mouseButton+")");
        if (mouseButton != -1)
        {
            mouseRotateControl = -1;
        }

        if (mouseRotateControl == 0)
        {
            yRenderAngle += (x - prevX) * 2;
            prevX = x;
            xRenderAngle += y - prevY;
            prevY = y;

            if (xRenderAngle > 20)
            {
                xRenderAngle = 20;
            }

            if (xRenderAngle < -30)
            {
                xRenderAngle = -30;
            }
        }

        if (mouseRotateControl == 1)
        {
            yHeadRenderAngle += (prevX - x) * 2;
            prevX = x;
            xHeadRenderAngle += y - prevY;
            prevY = y;

            if (xHeadRenderAngle > 20)
            {
                xHeadRenderAngle = 20;
            }

            if (xHeadRenderAngle < -30)
            {
                xHeadRenderAngle = -30;
            }

            if (yHeadRenderAngle > 40)
            {
                yHeadRenderAngle = 40;
            }

            if (yHeadRenderAngle < -40)
            {
                yHeadRenderAngle = -40;
            }
        }
    }

    /** Called when the mouse is clicked. */
    @Override
    protected void mouseClicked(int x, int y, int mouseButton)
    {
    }

    private static final ResourceLocation GUIIMG = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/" + "wikiCapture.png");

    @Override
    public void drawScreen(int i, int j, float f)
    {
        Minecraft minecraft = Minecraft.getMinecraft();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
        int j3 = 0xF0F0F0;// 61680;
        int k = j3 % 0x000100;
        int l = j3 / 0xFFFFFF;
        GL13.glMultiTexCoord2f(GL13.GL_TEXTURE1, k / 1.0F, l / 1.0F);
        minecraft.renderEngine.bindTexture(GUIIMG);
        int j2 = (width - xSize) / 2;
        int k2 = (height - ySize) / 2;
        this.drawGradientRect(j2, k2, xSize + j2, ySize + k2, 0xff000000, 0xff000000);
        GL11.glPushMatrix();
        renderMob();
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_LIGHTING);
        super.drawScreen(i, j, f);
    }

    @Override
    public void drawBackground(int n)
    {
        super.drawBackground(n);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    private static HashMap<PokedexEntry, EntityLiving> entityToDisplayMap = new HashMap<PokedexEntry, EntityLiving>();

    private EntityLiving getEntityToDisplay()
    {
        if (pokedexEntry == null)
        {
            pokedexEntry = Pokedex.getInstance().getFirstEntry();
        }
        EntityLiving pokemob = entityToDisplayMap.get(pokedexEntry);

        if (pokemob == null)
        {
            // int entityId =
            // mod_Pokecube.getEntityIdFromPokedexNumber(pokedexEntry.getPokedexNb());
            pokemob = (EntityLiving) PokecubeMod.core.createEntityByPokedexNb(pokedexEntry.getPokedexNb(),
                    entityPlayer.worldObj);

            if (pokemob != null)
            {
                pokemob = (EntityLiving) ((IPokemob) pokemob).changeForme(pokedexEntry.getName());
                ((IPokemob) pokemob).setSize(1);
                ((IPokemob) pokemob).setShiny(shiny);
                entityToDisplayMap.put(pokedexEntry, pokemob);
            }
        }

        return pokemob;
    }

    public static int x;
    public static int y;

    private void renderMob()
    {
        try
        {
            EntityLiving entity = getEntityToDisplay();
            float size = 0;
            int j = 0;
            int k = 0;
            if (entity instanceof IPokemob)
            {
                pokemob = (IPokemob) entity;
                pokemob.setShiny(shiny);
            }
            size = Math.max(pokemob.getPokedexEntry().height, pokemob.getPokedexEntry().width);
            size = Math.max(size, pokemob.getPokedexEntry().length);
            j = (width - xSize) / 2 + 5;
            k = (height - ySize) / 2 + 5;
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_COLOR_MATERIAL);
            GL11.glPushMatrix();
            GL11.glPushMatrix();
            GL11.glTranslatef(j + 55, k + 120, 50F);
            float zoom = (float) (25F / Math.sqrt(size));
            GL11.glScalef(-zoom, zoom, zoom);
            GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
            float f5 = ((k + 75) - 50) - ySize;
            GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);

            RenderHelper.enableStandardItemLighting();

            GL11.glRotatef(-135F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-(float) Math.atan(f5 / 40F) * 20F, 1.0F, 0.0F, 0.0F);
            entity.renderYawOffset = 0F;
            entity.rotationYaw = yHeadRenderAngle;
            entity.rotationPitch = xHeadRenderAngle;
            entity.rotationYawHead = entity.rotationYaw;
            yRenderAngle = -30;
            xRenderAngle = 0;
            GL11.glRotatef(yRenderAngle, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(xRenderAngle, 1.0F, 0.0F, 0.0F);
            ((EntityPokemob) entity).setPokemonAIState(IPokemob.SITTING, false);
            entity.setPosition(entityPlayer.posX, entityPlayer.posY + 1, entityPlayer.posZ);
            entity.limbSwing = 0;
            entity.limbSwingAmount = 0;
            entity.onGround = ((EntityPokemob) entity).getType1() != flying
                    && ((EntityPokemob) entity).getType2() != flying;
            int i = 15728880;
            int j1 = i % 65536;
            int k1 = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j1 / 1.0F, k1 / 1.0F);
            Minecraft.getMinecraft().getRenderManager().doRenderEntity(entity, 0, 0, 0, 0, POKEDEX_RENDER, false);
            GL11.glPopMatrix();

            EntityLivingBase owner = entityPlayer;
            if (owner != null)
            {
                GL11.glTranslatef(j + 55, k + 120, 50F);
                GL11.glScalef(-zoom, zoom, zoom);
                GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(0, -1.2, 1);
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                Minecraft.getMinecraft().getBlockRendererDispatcher()
                        .renderBlockBrightness(Blocks.STONE.getDefaultState(), 1.0F);
                // GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
                // Minecraft.getMinecraft().getRenderManager().doRenderEntity(owner,
                // 0, 0, 0, 0, POKEDEX_RENDER, false);
                // GlStateManager.disableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
            }
            GL11.glPopMatrix();

            float time = MathHelper.cos((float) ((System.currentTimeMillis() % 10000) / Math.PI));
            if (time != lastTime)
            {
                pokedexEntry = pokemob.getPokedexEntry();
                PokecubeWikiWriter.doCapturePokemobGif();
            }
            lastTime = time;
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    static float              lastTime       = 0;
    /** to pass as last parameter when rendering the mob so that the render
     * knows the rendering is asked by the pokedex gui */
    public final static float POKEDEX_RENDER = 1.5f;
}