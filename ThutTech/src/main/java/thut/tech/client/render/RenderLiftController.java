package thut.tech.client.render;

import java.awt.Color;

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
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import thut.api.ThutBlocks;
import thut.api.entity.blockentity.BlockEntityWorld;
import thut.tech.common.blocks.lift.BlockLift;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;

public class RenderLiftController<T extends TileEntity> extends TileEntitySpecialRenderer<T>
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

    private ResourceLocation overlay   = new ResourceLocation("thuttech:textures/blocks/overlay.png");
    private ResourceLocation overlay_1 = new ResourceLocation("thuttech:textures/blocks/overlay_1.png");
    private ResourceLocation font      = new ResourceLocation("thuttech:textures/blocks/font.png");

    public RenderLiftController()
    {
    }

    public void drawFloorNumbers(int page)
    {
        for (int floor = 0; floor < (16); floor++)
        {
            drawNumber(floor + page * 16, floor);
        }
    }

    private void drawNumber(int number, int floor)
    {
        TextureManager renderengine = Minecraft.getInstance().renderEngine;

        GL11.glPushMatrix();
        if (renderengine != null)
        {
            renderengine.bindTexture(font);
        }
        float dz = -0.001f;
        boolean minus = number >= 64;
        if (minus) number -= 64;

        Tessellator t = Tessellator.getInstance();
        t.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        double x = ((double) (3 - floor & 3)) / (double) 4, y = ((double) 3 - (floor >> 2)) / 4;
        int actFloor = number;
        double[] uvs = locationFromNumber((actFloor + 1) % 10);
        double[] uvs1 = locationFromNumber((actFloor + 1) / 10);

        if (actFloor > 8)
        {
            GL11.glTranslated(x + 0.01, y + 0.06, dz * (5 + 1));
            float dx = minus ? -0.03f : 0;
            float dy = -0.0f;
            t.getBuffer().pos(0.15 + dx, 0.15 + dy, 0).tex(uvs[0], uvs[2]).color(0, 0, 0, 255).endVertex();
            t.getBuffer().pos(0.15 + dx, 0.0 + dy, 0).tex(uvs[0], uvs[3]).color(0, 0, 0, 255).endVertex();

            t.getBuffer().pos(0 + dx, 0.0 + dy, 0).tex(uvs[1], uvs[3]).color(0, 0, 0, 255).endVertex();
            t.getBuffer().pos(0 + dx, 0.15 + dy, 0).tex(uvs[1], uvs[2]).color(0, 0, 0, 255).endVertex();

            t.getBuffer().pos(0.15 + dx + 0.1, 0.15 + dy, 0).tex(uvs1[0], uvs1[2]).color(0, 0, 0, 255).endVertex();
            t.getBuffer().pos(0.15 + dx + 0.1, 0 + dy, 0).tex(uvs1[0], uvs1[3]).color(0, 0, 0, 255).endVertex();

            t.getBuffer().pos(0 + dx + 0.1, 0 + dy, 0).tex(uvs1[1], uvs1[3]).color(0, 0, 0, 255).endVertex();
            t.getBuffer().pos(0 + dx + 0.1, 0.15 + dy, 0).tex(uvs1[1], uvs1[2]).color(0, 0, 0, 255).endVertex();

            if (minus)
            {
                uvs = locationFromNumber(-3);
                dx = 0.135f;
                dy = -0.0175f;
                t.getBuffer().pos(0.15 + dx, 0.15 + dy, 0).tex(uvs[0], uvs[2]).color(0, 0, 0, 255).endVertex();
                t.getBuffer().pos(0.15 + dx, 0.0 + dy, 0).tex(uvs[0], uvs[3]).color(0, 0, 0, 255).endVertex();

                t.getBuffer().pos(0 + dx, 0.0 + dy, 0).tex(uvs[1], uvs[3]).color(0, 0, 0, 255).endVertex();
                t.getBuffer().pos(0 + dx, 0.15 + dy, 0).tex(uvs[1], uvs[2]).color(0, 0, 0, 255).endVertex();
            }
        }
        else
        {
            GL11.glTranslated(x + 0.05, y + 0.06, dz * (5 + 1));
            t.getBuffer().pos(0.15, 0.15, 0).tex(uvs[0], uvs[2]).color(0, 0, 0, 255).endVertex();
            t.getBuffer().pos(0.15, 0.0, 0).tex(uvs[0], uvs[3]).color(0, 0, 0, 255).endVertex();

            t.getBuffer().pos(0, 0.0, 0).tex(uvs[1], uvs[3]).color(0, 0, 0, 255).endVertex();
            t.getBuffer().pos(0, 0.15, 0).tex(uvs[1], uvs[2]).color(0, 0, 0, 255).endVertex();

            if (minus)
            {
                uvs = locationFromNumber(-3);
                float dx = 0.075f;
                t.getBuffer().pos(0.15 + dx, 0.15, 0).tex(uvs[0], uvs[2]).color(0, 0, 0, 255).endVertex();
                t.getBuffer().pos(0.15 + dx, 0.0, 0).tex(uvs[0], uvs[3]).color(0, 0, 0, 255).endVertex();

                t.getBuffer().pos(0 + dx, 0.0, 0).tex(uvs[1], uvs[3]).color(0, 0, 0, 255).endVertex();
                t.getBuffer().pos(0 + dx, 0.15, 0).tex(uvs[1], uvs[2]).color(0, 0, 0, 255).endVertex();
            }
        }
        t.draw();
        GL11.glPopMatrix();
    }

    public void drawEditOverlay(TileEntityLiftAccess monitor, Direction side)
    {
        // Call button toggle
        // Draw the white background
        GL11.glPushMatrix();
        Color colour = new Color(255, 255, 255, 255);
        drawOverLay(monitor, 1, colour, side, false);
        colour = monitor.callFaces[side.ordinal()] ? new Color(0, 255, 0, 255) : new Color(255, 0, 0, 255);
        drawOverLay(monitor, 1, colour, side, false);
        GL11.glPopMatrix();

        // Floor Display toggle
        // Draw the white background
        GL11.glPushMatrix();
        colour = new Color(255, 255, 255, 255);
        drawOverLay(monitor, 2, colour, side, false);
        colour = monitor.floorDisplay[side.ordinal()] ? new Color(0, 255, 0, 255) : new Color(255, 0, 0, 255);
        drawOverLay(monitor, 2, colour, side, false);
        GL11.glPopMatrix();
    }

    public void drawOverLay(TileEntityLiftAccess monitor, int floor, Color colour, Direction side, boolean wide)
    {
        if (!wide) floor = floor - monitor.getSidePage(side) * 16;
        IBlockState state = monitor.getWorld().getBlockState(monitor.getPos());
        boolean isMonitor = state.getValue(BlockLift.VARIANT) == BlockLift.EnumType.CONTROLLER;
        if (isMonitor && monitor.getBlockType() == ThutBlocks.lift && floor > 0 && floor < 17)
        {
            TextureManager renderengine = Minecraft.getInstance().renderEngine;
            GL11.glPushMatrix();
            if (renderengine != null)
            {
                if (wide) renderengine.bindTexture(overlay_1);
                else renderengine.bindTexture(overlay);
            }
            float dz = -0.001f;
            if (monitor.getWorld() instanceof BlockEntityWorld) dz = -0.005f;
            floor -= 1;
            double x = ((double) (3 - floor & 3)) / (double) 4, y = ((double) 3 - (floor >> 2)) / 4;
            GL11.glTranslated(x, y, dz);
            Tessellator t = Tessellator.getInstance();
            t.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

            double amount = wide ? 0.25 : 0;
            t.getBuffer().pos(0.25 + amount, 0.25, 0).tex(0, 0)
                    .color(colour.getRed(), colour.getGreen(), colour.getBlue(), colour.getAlpha()).endVertex();
            t.getBuffer().pos(0.25 + amount, 0, 0).tex(0, 1)
                    .color(colour.getRed(), colour.getGreen(), colour.getBlue(), colour.getAlpha()).endVertex();

            t.getBuffer().pos(0, 0, 0).tex(1, 1)
                    .color(colour.getRed(), colour.getGreen(), colour.getBlue(), colour.getAlpha()).endVertex();
            t.getBuffer().pos(0, 0.25, 0).tex(1, 0)
                    .color(colour.getRed(), colour.getGreen(), colour.getBlue(), colour.getAlpha()).endVertex();

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
    public void render(T tileentity, double x, double y, double z, float f, int i1, float f2)
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

        render:
        {
            if (monitor.getBlockMetadata() == 0 && monitor.getBlockType() == ThutBlocks.lift)
            {
                break render;
            }
            for (int i = 0; i < 6; i++)
            {
                Direction dir = Direction.getFront(i);

                if (!monitor.isSideOn(dir)) continue;

                GL11.glPushMatrix();

                GL11.glTranslatef((float) x, (float) y, (float) z);

                if (dir == Direction.EAST)
                {
                    GL11.glTranslatef(1, 0, 0);
                    GL11.glRotatef(270, 0, 1, 0);
                }
                else if (dir == Direction.SOUTH)
                {
                    GL11.glTranslatef(1, 0, 1);
                    GL11.glRotatef(180, 0, 1, 0);
                }
                else if (dir == Direction.WEST)
                {
                    GL11.glTranslatef(0, 0, 1);
                    GL11.glRotatef(90, 0, 1, 0);
                }
                int a = 64;
                if (monitor.editFace[dir.ordinal()])
                {
                    drawEditOverlay(monitor, dir);
                }
                else if (monitor.floorDisplay[dir.ordinal()])
                {
                    // Draw the white background
                    GL11.glPushMatrix();
                    GL11.glTranslated(-0.5, -0.095, 0);
                    Color colour = new Color(255, 255, 255, 255);
                    drawOverLay(monitor, 1, colour, dir, true);
                    GL11.glPopMatrix();

                    GL11.glPushMatrix();
                    GL11.glTranslated(-0.11, -0.1, 0);
                    drawNumber(monitor.currentFloor-1, 1);
                    GL11.glPopMatrix();
                    GL11.glPushMatrix();
                    GL11.glTranslated(-0.5, -0.095, 0);

                    // Draw highlight over the background.
                    if (monitor.calledFloor == monitor.floor)
                    {
                        colour = new Color(255, 255, 0, a);
                        drawOverLay(monitor, 1, colour, dir, true);
                    }
                    else if (monitor.currentFloor == monitor.floor)
                    {
                        colour = new Color(0, 128, 255, a);
                        drawOverLay(monitor, 1, colour, dir, true);
                    }
                    GL11.glPopMatrix();
                }
                else if (monitor.callFaces[dir.ordinal()])
                {
                    // Draw the white background
                    GL11.glPushMatrix();
                    GL11.glTranslated(-0.5, -0.095, 0);
                    Color colour = new Color(255, 255, 255, 255);
                    drawOverLay(monitor, 1, colour, dir, true);
                    GL11.glPopMatrix();

                    GL11.glPushMatrix();
                    GL11.glTranslated(-0.11, -0.1, 0);
                    drawNumber(monitor.floor - 1, 1);
                    GL11.glPopMatrix();
                    GL11.glPushMatrix();
                    GL11.glTranslated(-0.5, -0.095, 0);

                    // Draw highlight over the background.
                    if (monitor.calledFloor == monitor.floor)
                    {
                        colour = new Color(255, 255, 0, a);
                        drawOverLay(monitor, 1, colour, dir, true);
                    }
                    else if (monitor.currentFloor == monitor.floor)
                    {
                        colour = new Color(0, 128, 255, a);
                        drawOverLay(monitor, 1, colour, dir, true);
                    }
                    GL11.glPopMatrix();
                }
                else
                {
                    drawFloorNumbers(monitor.getSidePage(dir));
                    // Draw background slots
                    Color colour = new Color(255, 255, 255, 255);
                    for (int j = monitor.getSidePage(dir) * 16; j < 16 + monitor.getSidePage(dir) * 16; j++)
                    {
                        drawOverLay(monitor, j + 1, colour, dir, false);
                    }
                    if (monitor.lift != null)
                    {
                        colour = new Color(0, 255, 0, a);
                        drawOverLay(monitor, monitor.floor, colour, dir, false);
                        colour = new Color(255, 255, 0, a);
                        drawOverLay(monitor, monitor.lift.getDestinationFloor(), colour, dir, false);
                        colour = new Color(0, 128, 255, a);
                        drawOverLay(monitor, monitor.lift.getCurrentFloor(), colour, dir, false);
                        for (int j = monitor.getSidePage(dir) * 16; j < 16 + monitor.getSidePage(dir) * 16; j++)
                        {
                            colour = new Color(10, 10, 10, 128);
                            if (!monitor.lift.hasFloors[j])
                            {
                                drawOverLay(monitor, j + 1, colour, dir, false);
                            }
                        }
                    }
                }
                GL11.glPopMatrix();
            }
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
