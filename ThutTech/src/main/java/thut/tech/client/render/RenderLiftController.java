package thut.tech.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import thut.api.ThutBlocks;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;

@SuppressWarnings("rawtypes")
public class RenderLiftController extends TileEntitySpecialRenderer
{

    // public static final int ID =
    // RenderingRegistry.getNextAvailableRenderId();

    private ResourceLocation    texture;

    public static class ModelLiftController extends ModelBase
    {
        private ModelRenderer _main;

        public ModelLiftController()
        {
            textureWidth = 64;
            textureHeight = 32;

            _main = new ModelRenderer(this, 0, 0);
            _main.addBox(0F, 0F, 0F, 1, 1, 1);
            _main.setRotationPoint(0F, 0F, 0F);
            _main.setTextureSize(64, 32);
            _main.mirror = true;
            setRotation(_main, 0F, 0F, 0F);
        }

        public void render(TileEntity te)
        {
            _main.render(1F);
        }

        private void setRotation(ModelRenderer model, float x, float y, float z)
        {
            model.rotateAngleX = x;
            model.rotateAngleY = y;
            model.rotateAngleZ = z;
        }
    }

    public RenderLiftController()
    {
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f, int i1)
    {
        TileEntityLiftAccess monitor = (TileEntityLiftAccess) tileentity;
        if (monitor.getBlockType() == ThutBlocks.liftRail) return;

        for (int i = 0; i < 6; i++)
        {
            EnumFacing dir = EnumFacing.getFront(i);

            if (!monitor.isSideOn(dir)) continue;

            GL11.glPushMatrix();

            GL11.glTranslatef((float) x, (float) y, (float) z);

            if (dir == EnumFacing.EAST)
            {
                GL11.glTranslatef(1, 0, 0);
                GL11.glRotatef(270, 0, 1, 0);
            }
            else if (dir == EnumFacing.SOUTH)
            {
                GL11.glTranslatef(1, 0, 1);
                GL11.glRotatef(180, 0, 1, 0);
            }
            else if (dir == EnumFacing.WEST)
            {
                GL11.glTranslatef(0, 0, 1);
                GL11.glRotatef(90, 0, 1, 0);
            }

            TextureManager renderengine = Minecraft.getMinecraft().renderEngine;

            GL11.glPushMatrix();
            if (renderengine != null)
            {
                texture = new ResourceLocation("thuttech:textures/blocks/controlPanel_1.png");
                renderengine.bindTexture(texture);
            }
            GL11.glPushAttrib(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
            RenderHelper.disableStandardItemLighting();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

            Tessellator t = Tessellator.getInstance();
            t.getWorldRenderer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);//.startDrawing(GL11.GL_QUADS);

            GL11.glTranslated(0, 0, -0.001 * (0 + 0.5));
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1F);
            {
                t.getWorldRenderer().pos(1, 1, 0).tex(0, 0).endVertex();
                t.getWorldRenderer().pos(1, 0, 0).tex(0, 1).endVertex();

                t.getWorldRenderer().pos(0, 0, 0).tex(1, 1).endVertex();
                t.getWorldRenderer().pos(0, 1, 0).tex(1, 0).endVertex();
            }

            t.draw();

            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glPopAttrib();
            GL11.glPopAttrib();
            GL11.glPopMatrix();

            drawFloorNumbers(monitor.getSidePage(dir));
            if(monitor.lift!=null)
            {
                drawOverLay(monitor, monitor.floor, 0, dir);
                drawOverLay(monitor, monitor.lift.getDestinationFloor(), 1, dir);
                drawOverLay(monitor, monitor.lift.getCurrentFloor(), 2, dir);
            }
            
            if (monitor.lift != null)
                for (int j = monitor.getSidePage(dir) * 16; j < 16 + monitor.getSidePage(dir) * 16; j++)
            {
                if ((monitor.lift.floors[j] < 0))
                {
                    drawOverLay(monitor, j + 1, 3, dir);
                }
            }
            RenderHelper.enableStandardItemLighting();

            GL11.glPopMatrix();
        }
    }

    public void drawFloorNumbers(int page)
    {
        for (int floor = 0; floor < (16); floor++)
        {
            TextureManager renderengine = Minecraft.getMinecraft().renderEngine;

            GL11.glPushMatrix();
            if (renderengine != null)
            {
                texture = new ResourceLocation("thuttech:textures/blocks/font.png");
                renderengine.bindTexture(texture);
            }
            GL11.glPushAttrib(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);

            Tessellator t = Tessellator.getInstance();
            t.getWorldRenderer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);//.startDrawing(GL11.GL_QUADS);
            double x = ((double) (3 - floor & 3)) / (double) 4, y = ((double) 3 - (floor >> 2)) / (double) 4;
            int actFloor = floor + page * 16;
            double[] uvs = locationFromNumber((actFloor + 1) % 10);
            double[] uvs1 = locationFromNumber((actFloor + 1) / 10);
            GL11.glColor4f(0.0F, 0.0F, 0.0F, 1F);

            if (actFloor > 8)
            {

                GL11.glTranslated(x + 0.01, y + 0.06, -0.001 * (5 + 1));
                t.getWorldRenderer().pos(0.15, 0.15, 0).tex(uvs[0], uvs[2]).endVertex();
                t.getWorldRenderer().pos(0.15, 0.0, 0).tex(uvs[0], uvs[3]).endVertex();

                t.getWorldRenderer().pos(0, 0.0, 0).tex(uvs[1], uvs[3]).endVertex();
                t.getWorldRenderer().pos(0, 0.15, 0).tex(uvs[1], uvs[2]).endVertex();

                t.getWorldRenderer().pos(0.15 + 0.1, 0.15, 0).tex(uvs1[0], uvs1[2]).endVertex();
                t.getWorldRenderer().pos(0.15 + 0.1, 0, 0).tex(uvs1[0], uvs1[3]).endVertex();

                t.getWorldRenderer().pos(0 + 0.1, 0, 0).tex(uvs1[1], uvs1[3]).endVertex();
                t.getWorldRenderer().pos(0 + 0.1, 0.15, 0).tex(uvs1[1], uvs1[2]).endVertex();
            }
            else
            {
                GL11.glTranslated(x + 0.05, y + 0.06, -0.001 * (5 + 1));
                t.getWorldRenderer().pos(0.15, 0.15, 0).tex(uvs[0], uvs[2]).endVertex();
                t.getWorldRenderer().pos(0.15, 0.0, 0).tex(uvs[0], uvs[3]).endVertex();

                t.getWorldRenderer().pos(0, 0.0, 0).tex(uvs[1], uvs[3]).endVertex();
                t.getWorldRenderer().pos(0, 0.15, 0).tex(uvs[1], uvs[2]).endVertex();
            }

            t.draw();

            GL11.glEnable(GL11.GL_LIGHTING);
            // GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glPopAttrib();
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }

    }

    public void drawOverLay(TileEntityLiftAccess monitor, int floor, int colour, EnumFacing side)
    {
        floor = floor - monitor.getSidePage(side) * 16;
        if (monitor.getBlockMetadata() == 1 && monitor.getBlockType() == ThutBlocks.lift && floor > 0 && floor < 17)
        {

            TextureManager renderengine = Minecraft.getMinecraft().renderEngine;
            String col = colour == 0 ? "green" : colour == 1 ? "orange" : colour == 2 ? "blue" : "gray";

            GL11.glPushMatrix();
            if (renderengine != null)
            {
                texture = new ResourceLocation("thuttech:textures/blocks/" + col + "Overlay.png");
                renderengine.bindTexture(texture);
            }

            GL11.glPushAttrib(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
            
            floor -= 1;
            double x = ((double) (3 - floor & 3)) / (double) 4, y = ((double) 3 - (floor >> 2)) / (double) 4;
            GL11.glTranslated(x, y, -0.001 * (colour + 1));
            Tessellator t = Tessellator.getInstance();
            t.getWorldRenderer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);//.startDrawing(GL11.GL_QUADS);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);

            t.getWorldRenderer().pos(0.25, 0.25, 0).tex(0, 0).endVertex();
            t.getWorldRenderer().pos(0.25, 0, 0).tex(0, 1).endVertex();

            t.getWorldRenderer().pos(0, 0, 0).tex(1, 1).endVertex();
            t.getWorldRenderer().pos(0, 0.25, 0).tex(1, 0).endVertex();

            t.draw();
            
            GL11.glEnable(GL11.GL_LIGHTING);
            // GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glPopAttrib();
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }

    }

    public double[] locationFromNumber(int number)
    {
        double[] ret = new double[4];

        if (number > 9 || number < 0) return ret;
        int index = 16 + number;

        ret[0] = (double) (index % 10) / 10;
        ret[2] = (double) (index / 10) / 10;

        ret[1] = (double) (1 + (index) % 10) / 10;
        ret[3] = (double) (1 + (index) / 10) / 10;

        return ret;
    }
}
