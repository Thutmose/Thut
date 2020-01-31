package thut.tech.client.render;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import thut.api.entity.blockentity.world.IBlockEntityWorld;
import thut.tech.common.blocks.lift.ControllerTile;
import thut.tech.common.entity.EntityLift;

public class ControllerRenderer<T extends TileEntity> extends TileEntityRenderer<T>
{
    public static class ModelLiftController extends Model
    {
        private final RendererModel _main;

        public ModelLiftController()
        {
            this.textureWidth = 64;
            this.textureHeight = 32;

            this._main = new RendererModel(this, 0, 0);
            this._main.addBox(0F, 0F, 0F, 1, 1, 1);
            this._main.setRotationPoint(0F, 0F, 0F);
            this._main.setTextureSize(64, 32);
            this._main.mirror = true;
            this.setRotation(this._main, 0F, 0F, 0F);
        }

        public void render(final TileEntity te)
        {
            this._main.render(1F);
        }

        private void setRotation(final RendererModel model, final float x, final float y, final float z)
        {
            model.rotateAngleX = x;
            model.rotateAngleY = y;
            model.rotateAngleZ = z;
        }
    }

    private final ResourceLocation overlay   = new ResourceLocation("thuttech:textures/blocks/overlay.png");
    private final ResourceLocation overlay_1 = new ResourceLocation("thuttech:textures/blocks/overlay_1.png");
    private final ResourceLocation font      = new ResourceLocation("thuttech:textures/blocks/font.png");

    public ControllerRenderer()
    {
    }

    public void drawEditOverlay(final ControllerTile monitor, final Direction side)
    {
        // Call button toggle
        // Draw the white background
        GL11.glPushMatrix();
        Color colour = new Color(255, 255, 255, 255);
        this.drawOverLay(monitor, 1, colour, side, false, 0);
        colour = monitor.callFaces[side.ordinal()] ? new Color(0, 255, 0, 255) : new Color(255, 0, 0, 255);
        this.drawOverLay(monitor, 1, colour, side, false, 1);
        GL11.glPopMatrix();

        // Floor Display toggle
        // Draw the white background
        GL11.glPushMatrix();
        colour = new Color(255, 255, 255, 255);
        this.drawOverLay(monitor, 2, colour, side, false, 0);
        colour = monitor.floorDisplay[side.ordinal()] ? new Color(0, 255, 0, 255) : new Color(255, 0, 0, 255);
        this.drawOverLay(monitor, 2, colour, side, false, 1);
        GL11.glPopMatrix();
    }

    public void drawFloorNumbers(final int page)
    {
        for (int floor = 0; floor < 16; floor++)
            this.drawNumber(floor + page * 16, floor);
    }

    private void drawNumber(int number, final int floor)
    {
        final TextureManager renderengine = Minecraft.getInstance().textureManager;

        GL11.glPushMatrix();
        if (renderengine != null) renderengine.bindTexture(this.font);
        final float dz = -0.001f;
        final boolean minus = number >= 64;
        if (minus) number -= 64;

        final Tessellator t = Tessellator.getInstance();
        t.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        final double x = (double) (3 - floor & 3) / (double) 4, y = ((double) 3 - (floor >> 2)) / 4;
        final int actFloor = number;
        double[] uvs = this.locationFromNumber((actFloor + 1) % 10);
        final double[] uvs1 = this.locationFromNumber((actFloor + 1) / 10);

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
                uvs = this.locationFromNumber(-3);
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
                uvs = this.locationFromNumber(-3);
                final float dx = 0.075f;
                t.getBuffer().pos(0.15 + dx, 0.15, 0).tex(uvs[0], uvs[2]).color(0, 0, 0, 255).endVertex();
                t.getBuffer().pos(0.15 + dx, 0.0, 0).tex(uvs[0], uvs[3]).color(0, 0, 0, 255).endVertex();

                t.getBuffer().pos(0 + dx, 0.0, 0).tex(uvs[1], uvs[3]).color(0, 0, 0, 255).endVertex();
                t.getBuffer().pos(0 + dx, 0.15, 0).tex(uvs[1], uvs[2]).color(0, 0, 0, 255).endVertex();
            }
        }
        t.draw();
        GL11.glPopMatrix();
    }

    public void drawOverLay(final ControllerTile monitor, int floor, final Color colour, final Direction side,
            final boolean wide, final int order)
    {
        if (!wide) floor = floor - monitor.getSidePage(side) * 16;
        if (floor > 0 && floor < 17)
        {
            final TextureManager renderengine = Minecraft.getInstance().textureManager;
            GL11.glPushMatrix();
            if (renderengine != null) if (wide) renderengine.bindTexture(this.overlay_1);
            else renderengine.bindTexture(this.overlay);
            // System.out.println(order);
            float dz = -0.001f * (1 + order);
            // TODO replace this with something like a built in tag?
            if (monitor.getWorld() instanceof IBlockEntityWorld) dz -= 0.004f;
            floor -= 1;
            final double x = (double) (3 - floor & 3) / (double) 4, y = ((double) 3 - (floor >> 2)) / 4;
            final float r = colour.getRed() / 255f;
            final float g = colour.getGreen() / 255f;
            final float b = colour.getBlue() / 255f;
            final float a = colour.getAlpha() / 255f;
            GL11.glTranslated(x, y, dz);
            final Tessellator t = Tessellator.getInstance();
            t.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            final double amount = wide ? 0.25 : 0;
            t.getBuffer().pos(0.25 + amount, 0.25, 0).tex(0, 0).color(r, g, b, a).endVertex();
            t.getBuffer().pos(0.25 + amount, 0, 0).tex(0, 1).color(r, g, b, a).endVertex();
            t.getBuffer().pos(0, 0, 0).tex(1, 1).color(r, g, b, a).endVertex();
            t.getBuffer().pos(0, 0.25, 0).tex(1, 0).color(r, g, b, a).endVertex();

            t.draw();
            GL11.glPopMatrix();
        }
    }

    public double[] locationFromNumber(final int number)
    {
        final double[] ret = new double[4];

        // if (number > 9 || number < 0) return ret;
        final int index = 16 + number;
        int dx, dz;
        dx = index % 10;
        dz = index / 10;
        // System.out.println(dx+" "+dz);

        ret[0] = dx / 10d;
        ret[2] = dz / 10d;

        ret[1] = (1 + dx) / 10d;
        ret[3] = (1 + dz) / 10d;

        return ret;
    }

    @Override
    public void render(final T tileentity, final double x, final double y, final double z, final float f, final int f2)
    {
        final ControllerTile monitor = (ControllerTile) tileentity;

        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableLighting();
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 200, 200);

        for (int i = 0; i < 6; i++)
        {
            final Direction dir = Direction.byIndex(i);

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
            final int a = 64;
            if (monitor.editFace[dir.ordinal()]) this.drawEditOverlay(monitor, dir);
            else if (monitor.floorDisplay[dir.ordinal()])
            {
                // Draw the white background
                GL11.glPushMatrix();
                GL11.glTranslated(-0.5, -0.095, 0);
                Color colour = new Color(255, 255, 255, 255);
                this.drawOverLay(monitor, 1, colour, dir, true, 0);
                GL11.glPopMatrix();

                GL11.glPushMatrix();
                GL11.glTranslated(-0.11, -0.1, 0);
                this.drawNumber(monitor.currentFloor - 1, 1);
                GL11.glPopMatrix();
                GL11.glPushMatrix();
                GL11.glTranslated(-0.5, -0.095, 0);

                // Draw highlight over the background.
                if (monitor.calledFloor == monitor.floor)
                {
                    colour = new Color(255, 255, 0, a);
                    this.drawOverLay(monitor, 1, colour, dir, true, 1);
                }
                else if (monitor.currentFloor == monitor.floor)
                {
                    colour = new Color(0, 128, 255, a);
                    this.drawOverLay(monitor, 1, colour, dir, true, 2);
                }
                GL11.glPopMatrix();
            }
            else if (monitor.callFaces[dir.ordinal()])
            {
                // Draw the white background
                GL11.glPushMatrix();
                GL11.glTranslated(-0.5, -0.095, 0);
                Color colour = new Color(255, 255, 255, 255);
                this.drawOverLay(monitor, 1, colour, dir, true, 0);
                GL11.glPopMatrix();

                GL11.glPushMatrix();
                GL11.glTranslated(-0.11, -0.1, 0);
                this.drawNumber(monitor.floor - 1, 1);
                GL11.glPopMatrix();
                GL11.glPushMatrix();
                GL11.glTranslated(-0.5, -0.095, 0);

                // Draw highlight over the background.
                if (monitor.calledFloor == monitor.floor)
                {
                    colour = new Color(255, 255, 0, a);
                    this.drawOverLay(monitor, 1, colour, dir, true, 1);
                }
                else if (monitor.currentFloor == monitor.floor)
                {
                    colour = new Color(0, 128, 255, a);
                    this.drawOverLay(monitor, 1, colour, dir, true, 2);
                }
                GL11.glPopMatrix();
            }
            else
            {
                // Draw numbers on top
                if (monitor.liftID == null) this.drawFloorNumbers(monitor.getSidePage(dir));
                else
                {
                    if (monitor.lift == null) monitor.lift = EntityLift.getLiftFromUUID(monitor.liftID, this
                            .getWorld());
                    final int page = monitor.getSidePage(dir);
                    if (monitor.lift != null) for (int floor = 0; floor < 16; floor++)
                        if (monitor.lift.hasFloors[floor + page * 16]) this.drawNumber(floor + page * 16, floor);
                }

                if (monitor.lift != null)
                {
                    final Color mapped = new Color(255, 255, 255, a);
                    Color colour = new Color(0, 255, 0, a);
                    this.drawOverLay(monitor, monitor.floor, colour, dir, false, 0);
                    colour = new Color(255, 255, 0, a);
                    this.drawOverLay(monitor, monitor.lift.getDestinationFloor(), colour, dir, false, 1);
                    colour = new Color(0, 128, 255, a);
                    this.drawOverLay(monitor, monitor.lift.getCurrentFloor(), colour, dir, false, 2);
                    for (int j = monitor.getSidePage(dir) * 16; j < 16 + monitor.getSidePage(dir) * 16; j++)
                        if (monitor.lift.hasFloors[j]) this.drawOverLay(monitor, j + 1, mapped, dir, false, 3);
                }
                else
                {
                    // Draw background slots
                    final Color colour = new Color(255, 255, 255, 255);
                    for (int j = monitor.getSidePage(dir) * 16; j < 16 + monitor.getSidePage(dir) * 16; j++)
                        this.drawOverLay(monitor, j + 1, colour, dir, false, 0);
                }
            }
            GL11.glPopMatrix();
        }
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
    }
}
