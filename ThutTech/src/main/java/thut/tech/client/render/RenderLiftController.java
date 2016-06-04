package thut.tech.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import thut.api.ThutBlocks;
import thut.tech.common.blocks.lift.BlockLift;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;

@SuppressWarnings("rawtypes")
public class RenderLiftController extends TileEntitySpecialRenderer
{

    // public static final int ID =
    // RenderingRegistry.getNextAvailableRenderId();

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

    private ResourceLocation texture;

    public RenderLiftController()
    {
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

            Tessellator t = Tessellator.getInstance();
            t.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);// .startDrawing(GL11.GL_QUADS);
            double x = ((double) (3 - floor & 3)) / (double) 4, y = ((double) 3 - (floor >> 2)) / 4;
            int actFloor = floor + page * 16;
            double[] uvs = locationFromNumber((actFloor + 1) % 10);
            double[] uvs1 = locationFromNumber((actFloor + 1) / 10);

            if (actFloor > 8)
            {
                GL11.glTranslated(x + 0.01, y + 0.06, -0.001 * (5 + 1));
                t.getBuffer().pos(0.15, 0.15, 0).tex(uvs[0], uvs[2]).color(0, 0, 0, 255).endVertex();
                t.getBuffer().pos(0.15, 0.0, 0).tex(uvs[0], uvs[3]).color(0, 0, 0, 255).endVertex();

                t.getBuffer().pos(0, 0.0, 0).tex(uvs[1], uvs[3]).color(0, 0, 0, 255).endVertex();
                t.getBuffer().pos(0, 0.15, 0).tex(uvs[1], uvs[2]).color(0, 0, 0, 255).endVertex();

                t.getBuffer().pos(0.15 + 0.1, 0.15, 0).tex(uvs1[0], uvs1[2]).color(0, 0, 0, 255).endVertex();
                t.getBuffer().pos(0.15 + 0.1, 0, 0).tex(uvs1[0], uvs1[3]).color(0, 0, 0, 255).endVertex();

                t.getBuffer().pos(0 + 0.1, 0, 0).tex(uvs1[1], uvs1[3]).color(0, 0, 0, 255).endVertex();
                t.getBuffer().pos(0 + 0.1, 0.15, 0).tex(uvs1[1], uvs1[2]).color(0, 0, 0, 255).endVertex();
            }
            else
            {
                GL11.glTranslated(x + 0.05, y + 0.06, -0.001 * (5 + 1));
                t.getBuffer().pos(0.15, 0.15, 0).tex(uvs[0], uvs[2]).color(0, 0, 0, 255).endVertex();
                t.getBuffer().pos(0.15, 0.0, 0).tex(uvs[0], uvs[3]).color(0, 0, 0, 255).endVertex();

                t.getBuffer().pos(0, 0.0, 0).tex(uvs[1], uvs[3]).color(0, 0, 0, 255).endVertex();
                t.getBuffer().pos(0, 0.15, 0).tex(uvs[1], uvs[2]).color(0, 0, 0, 255).endVertex();
            }
            t.draw();

            GL11.glPopMatrix();
        }

    }

    private void drawLiftBounds(int minX, int maxX, int minZ, int maxZ, int minY, int maxY)
    {
        TextureManager renderengine = Minecraft.getMinecraft().renderEngine;

        GL11.glPushMatrix();
        if (renderengine != null)
        {
            texture = new ResourceLocation("thuttech:textures/blocks/greenOverlay.png");
            renderengine.bindTexture(texture);
        }
        int floor = 5;
        double x = ((double) (3 - floor & 3)) / (double) 4, y = ((double) 3 - (floor >> 2)) / 4;
        GL11.glTranslated(x - 0.125, 1 + 0.001 * (1), y - 0.125);
        Tessellator t = Tessellator.getInstance();
        t.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        for (int i = -minX; i <= maxX; i++)
            for (int k = -minY; k <= maxY; k++)
                for (int j = -minZ; j <= maxZ; j++)
                {
                    t.getBuffer().pos(0.25 + i, k, 0.25 + j).tex(0, 0).color(255, 255, 255, 128).endVertex();
                    t.getBuffer().pos(0.25 + i, k, 0 + j).tex(0, 1).color(255, 255, 255, 128).endVertex();

                    t.getBuffer().pos(0 + i, k, 0 + j).tex(1, 1).color(255, 255, 255, 128).endVertex();
                    t.getBuffer().pos(0 + i, k, 0.25 + j).tex(1, 0).color(255, 255, 255, 128).endVertex();

                    t.getBuffer().pos(0 + i, k, 0.25 + j).tex(1, 0).color(255, 255, 255, 128).endVertex();
                    t.getBuffer().pos(0 + i, k, 0 + j).tex(1, 1).color(255, 255, 255, 128).endVertex();
                    t.getBuffer().pos(0.25 + i, k, 0 + j).tex(0, 1).color(255, 255, 255, 128).endVertex();
                    t.getBuffer().pos(0.25 + i, k, 0.25 + j).tex(0, 0).color(255, 255, 255, 128).endVertex();
                }

        t.draw();
        GL11.glPopMatrix();
    }

    private void drawLiftGui(TileEntityLiftAccess monitor)
    {
        int xMin = -monitor.boundMin.intX();
        int zMin = -monitor.boundMin.intZ();
        int yMin = -monitor.boundMin.intY();
        int xMax = monitor.boundMax.intX();
        int zMax = monitor.boundMax.intZ();
        int yMax = monitor.boundMax.intY();
        drawOnTop(xMin, 0);
        drawOnTop(12, 1);
        drawOnTop(14, 2);
        drawOnTop(xMin, 3);
        drawOnTop(zMin, 4);
        drawOnTop(12, 5);
        drawOnTop(14, 6);
        drawOnTop(zMin, 7);

        drawOnTop(xMax, 8);
        drawOnTop(12, 9);
        drawOnTop(14, 10);
        drawOnTop(xMax, 11);
        drawOnTop(zMax, 12);
        drawOnTop(12, 13);
        drawOnTop(14, 14);
        drawOnTop(zMax, 15);
        drawLiftBounds(xMin, xMax, zMin, zMax, yMin, yMax);
    }

    private void drawOnTop(int fontIndex, int positionIndex)
    {
        TextureManager renderengine = Minecraft.getMinecraft().renderEngine;
        GL11.glPushMatrix();
        if (renderengine != null)
        {
            texture = new ResourceLocation("thuttech:textures/blocks/font.png");
            renderengine.bindTexture(texture);
        }

        int floor = positionIndex;
        Tessellator t = Tessellator.getInstance();
        t.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);// .startDrawing(GL11.GL_QUADS);
        double x1 = ((double) (3 - floor & 3)) / (double) 4, y1 = ((double) 3 - (floor >> 2)) / 4;
        double[] uvs = locationFromNumber(fontIndex);

        GL11.glTranslated(x1 + 0.05, 1 + 0.002 * (0 + 0.5), y1 + 0.06);

        t.getBuffer().pos(0.15, 0.0, 0.15).tex(uvs[0], uvs[2]).color(0, 0, 0, 255).endVertex();
        t.getBuffer().pos(0.15, 0.0, 0).tex(uvs[0], uvs[3]).color(0, 0, 0, 255).endVertex();

        t.getBuffer().pos(0, 0.0, 0).tex(uvs[1], uvs[3]).color(0, 0, 0, 255).endVertex();
        t.getBuffer().pos(0, 0.0, 0.15).tex(uvs[1], uvs[2]).color(0, 0, 0, 255).endVertex();

        t.draw();

        GL11.glPopMatrix();
    }

    public void drawOverLay(TileEntityLiftAccess monitor, int floor, int colour, EnumFacing side)
    {
        floor = floor - monitor.getSidePage(side) * 16;
        IBlockState state = monitor.getWorld().getBlockState(monitor.getPos());
        boolean isMonitor = state.getValue(BlockLift.VARIANT) == BlockLift.EnumType.CONTROLLER;
        if (isMonitor && monitor.getBlockType() == ThutBlocks.lift && floor > 0 && floor < 17)
        {
            TextureManager renderengine = Minecraft.getMinecraft().renderEngine;
            String col = colour == 0 ? "green" : colour == 1 ? "orange" : colour == 2 ? "blue" : "gray";

            GL11.glPushMatrix();
            if (renderengine != null)
            {
                texture = new ResourceLocation("thuttech:textures/blocks/" + col + "Overlay.png");
                renderengine.bindTexture(texture);
            }

            floor -= 1;
            double x = ((double) (3 - floor & 3)) / (double) 4, y = ((double) 3 - (floor >> 2)) / 4;
            GL11.glTranslated(x, y, -0.001 * (colour + 1));
            Tessellator t = Tessellator.getInstance();
            t.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

            t.getBuffer().pos(0.25, 0.25, 0).tex(0, 0).color(255, 255, 255, 128).endVertex();
            t.getBuffer().pos(0.25, 0, 0).tex(0, 1).color(255, 255, 255, 128).endVertex();

            t.getBuffer().pos(0, 0, 0).tex(1, 1).color(255, 255, 255, 128).endVertex();
            t.getBuffer().pos(0, 0.25, 0).tex(1, 0).color(255, 255, 255, 128).endVertex();

            t.draw();
            GL11.glPopMatrix();
        }

    }

    public double[] locationFromNumber(int number)
    {
        double[] ret = new double[4];

        // if (number > 9 || number < 0) return ret;
        int index = 16 + number;
        int dx, dz;
        dx = (index % 10);
        dz = (index / 10);
        // System.out.println(dx+" "+dz);

        ret[0] = dx / 10d;
        ret[2] = dz / 10d;

        ret[1] = (1 + dx) / 10d;
        ret[3] = (1 + dz) / 10d;

        return ret;
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f, int i1)
    {
        TileEntityLiftAccess monitor = (TileEntityLiftAccess) tileentity;
        IBlockState state = monitor.getWorld().getBlockState(monitor.getPos());
        if (state.getBlock() != ThutBlocks.lift) return;

        boolean blend;

        boolean light;

        int src;

        int dst;
        float[] oldLight = { -1, -1 };

        oldLight[0] = OpenGlHelper.lastBrightnessX;
        oldLight[1] = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

        blend = GL11.glGetBoolean(GL11.GL_BLEND);
        light = GL11.glGetBoolean(GL11.GL_LIGHTING);
        src = GL11.glGetInteger(GL11.GL_BLEND_SRC);
        dst = GL11.glGetInteger(GL11.GL_BLEND_DST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (monitor.getBlockMetadata() == 0 && monitor.getBlockType() == ThutBlocks.lift)
        {
            GL11.glPushMatrix();

            GL11.glTranslatef((float) x, (float) y, (float) z);
            TextureManager renderengine = Minecraft.getMinecraft().renderEngine;

            GL11.glPushMatrix();
            if (renderengine != null)
            {
                texture = new ResourceLocation("thuttech:textures/blocks/controlPanel_1.png");
                renderengine.bindTexture(texture);
            }

            Tessellator t = Tessellator.getInstance();
            t.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);// .startDrawing(GL11.GL_QUADS);

            GL11.glTranslated(0, 1 + 0.001 * (0 + 0.5), 0);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1F);
            {
                t.getBuffer().pos(1, 0, 1).tex(0, 0).endVertex();
                t.getBuffer().pos(1, 0, 0).tex(0, 1).endVertex();

                t.getBuffer().pos(0, 0, 0).tex(1, 1).endVertex();
                t.getBuffer().pos(0, 0, 1).tex(1, 0).endVertex();
            }

            t.draw();
            GL11.glPopMatrix();

            drawLiftGui(monitor);

            GL11.glPopMatrix();

            if (light) GL11.glEnable(GL11.GL_LIGHTING);
            if (!blend) GL11.glDisable(GL11.GL_BLEND);
            GL11.glBlendFunc(src, dst);
            if (oldLight[0] != -1 && oldLight[1] != -1)
            {
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, oldLight[0], oldLight[1]);
            }
            return;
        }

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

            Tessellator t = Tessellator.getInstance();
            t.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);// .startDrawing(GL11.GL_QUADS);

            GL11.glTranslated(0, 0, -0.001 * (0 + 0.5));
            t.getBuffer().pos(1, 1, 0).tex(0, 0).endVertex();
            t.getBuffer().pos(1, 0, 0).tex(0, 1).endVertex();

            t.getBuffer().pos(0, 0, 0).tex(1, 1).endVertex();
            t.getBuffer().pos(0, 1, 0).tex(1, 0).endVertex();

            t.draw();
            GL11.glPopMatrix();

            drawFloorNumbers(monitor.getSidePage(dir));
            if (monitor.lift != null)
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
            GL11.glPopMatrix();
        }

        if (light) GL11.glEnable(GL11.GL_LIGHTING);
        if (!blend) GL11.glDisable(GL11.GL_BLEND);
        GL11.glBlendFunc(src, dst);
        if (oldLight[0] != -1 || oldLight[1] != -1)
        {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, oldLight[0], oldLight[1]);
        }
    }
}
