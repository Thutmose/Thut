package thut.tech.client.render;

import java.awt.Color;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderState.TextureState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import thut.tech.common.blocks.lift.ControllerTile;

public class ControllerRenderer<T extends TileEntity> extends TileEntityRenderer<T>
{

    private static final ResourceLocation overlay   = new ResourceLocation("thuttech:textures/blocks/overlay.png");
    private static final ResourceLocation overlay_1 = new ResourceLocation("thuttech:textures/blocks/overlay_1.png");
    private static final ResourceLocation font      = new ResourceLocation("thuttech:textures/blocks/font.png");

    private static void render(RenderType type, MatrixStack mat, IRenderTypeBuffer buff, float x1, float y1, float x2,
            float y2, float r, float g, float b, float a, float[] uvs)
    {
        render(type, mat, buff, x1, y1, x2, y2, r, g, b, a, uvs[0], uvs[1], uvs[2], uvs[3]);
    }

    private static void render(RenderType type, MatrixStack mat, IRenderTypeBuffer buff, float x1, float y1, float x2,
            float y2, float r, float g, float b, float a, float u1, float u2, float v1, float v2)
    {
        IVertexBuilder buffer = buff.getBuffer(type);
        Matrix4f o = mat.getLast().getPositionMatrix();
        buffer.pos(o, x2, y2, 0).color(r, g, b, a).tex(u1, v1).endVertex();
        buffer.pos(o, x2, y1, 0).color(r, g, b, a).tex(u1, v2).endVertex();
        buffer.pos(o, x1, y1, 0).color(r, g, b, a).tex(u2, v2).endVertex();
        buffer.pos(o, x1, y2, 0).color(r, g, b, a).tex(u2, v1).endVertex();
    }

    private static RenderType NUMBERS   = RenderType.get("thuttech:font", DefaultVertexFormats.POSITION_COLOR_TEX, 7,
            256, false, true, RenderType.State.builder().texture(new TextureState(font, false, false))
                    .transparency(new RenderState.TransparencyState("translucent_transparency", () ->
                                                        {
                                                            RenderSystem.enableBlend();
                                                        },
                            () ->
                            {
                                RenderSystem.disableBlend();
                                RenderSystem.defaultBlendFunc();
                            }))
                    .writeMask(new RenderState.WriteMaskState(true, false)).build(false));
    private static RenderType OVERLAY_1 = RenderType.get("thuttech:overlay_1", DefaultVertexFormats.POSITION_COLOR_TEX,
            7, 256, false, true,
            RenderType.State.builder().texture(new TextureState(overlay_1, false, false))
                    .alpha(new RenderState.AlphaState(0.003921569F))
                    .transparency(new RenderState.TransparencyState("translucent_transparency", () ->
                                                        {
                                                            RenderSystem.enableBlend();
                                                            RenderSystem.defaultBlendFunc();
                                                        },
                            () ->
                            {
                                RenderSystem.disableBlend();
                            }))
                    .writeMask(new RenderState.WriteMaskState(true, false)).build(false));
    private static RenderType OVERLAY   = RenderType.get("thuttech:overlay", DefaultVertexFormats.POSITION_COLOR_TEX, 7,
            256, false, true,
            RenderType.State.builder().texture(new TextureState(overlay, false, false))
                    .alpha(new RenderState.AlphaState(0.003921569F))
                    .transparency(new RenderState.TransparencyState("translucent_transparency", () ->
                                                        {
                                                            RenderSystem.enableBlend();
                                                            RenderSystem.defaultBlendFunc();
                                                        },
                            () ->
                            {
                                RenderSystem.disableBlend();
                            }))
                    .writeMask(new RenderState.WriteMaskState(true, false)).build(false));

    public ControllerRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
    {
        super(rendererDispatcherIn);
    }

    public void drawEditOverlay(MatrixStack mat, IRenderTypeBuffer buff, ControllerTile monitor, final Direction side)
    {
        // Call button toggle
        // Draw the white background
        Color colour = new Color(255, 255, 255, 255);
        drawOverLay(mat, buff, monitor, 1, colour, side, false, 0);
        colour = monitor.callFaces[side.ordinal()] ? new Color(0, 255, 0, 255) : new Color(255, 0, 0, 255);
        drawOverLay(mat, buff, monitor, 1, colour, side, false, 1);

        // Floor Display toggle
        // Draw the white background
        colour = new Color(255, 255, 255, 255);
        drawOverLay(mat, buff, monitor, 2, colour, side, false, 0);
        colour = monitor.floorDisplay[side.ordinal()] ? new Color(0, 255, 0, 255) : new Color(255, 0, 0, 255);
        drawOverLay(mat, buff, monitor, 2, colour, side, false, 1);
    }

    public void drawFloorNumbers(MatrixStack mat, IRenderTypeBuffer buffer, final int page)
    {
        for (int floor = 0; floor < 16; floor++)
            this.drawNumber(mat, buffer, floor + page * 16, floor);
    }

    private void drawNumber(MatrixStack mat, IRenderTypeBuffer buffer, int number, final int floor)
    {
        mat.push();
        final float dz = -0.006f;
        final boolean minus = number >= 64;
        if (minus) number -= 64;

        final double x = (double) (3 - floor & 3) / (double) 4, y = ((double) 3 - (floor >> 2)) / 4;
        final int actFloor = number;
        float[] uvs = this.locationFromNumber((actFloor + 1) % 10);
        final float[] uvs1 = this.locationFromNumber((actFloor + 1) / 10);
        float r = 0, g = 0, b = 0, a = 1f;

        if (actFloor > 8)
        {
            mat.translate(x + 0.01, y + 0.06, dz);
            float dx = minus ? -0.03f : 0;
            float dy = -0.0f;
            render(NUMBERS, mat, buffer, 0 + dx, 0, 0.15f + dx, 0.15f + dy, r, g, b, a, uvs);
            render(NUMBERS, mat, buffer, 0.1f + dx, 0, 0.25f + dx, 0.15f + dy, r, g, b, a, uvs1);
            if (minus)
            {
                uvs = this.locationFromNumber(-3);
                dx = 0.135f;
                dy = -0.0175f;
                render(NUMBERS, mat, buffer, 0 + dx, 0, 0.15f + dx, 0.15f + dy, r, g, b, a, uvs);
            }
        }
        else
        {
            mat.translate(x + 0.05, y + 0.06, dz);
            render(NUMBERS, mat, buffer, 0, 0, 0.15f, 0.15f, r, g, b, a, uvs);

            if (minus)
            {
                uvs = this.locationFromNumber(-3);
                final float dx = 0.075f;
                render(NUMBERS, mat, buffer, 0 + dx, 0, 0.15f + dx, 0.15f, r, g, b, a, uvs);
            }
        }
        mat.pop();
    }

    public void drawOverLay(MatrixStack mat, IRenderTypeBuffer buffer, final ControllerTile monitor, int floor,
            final Color colour, final Direction side, final boolean wide, final int order)
    {
        if (!wide) floor = floor - monitor.getSidePage(side) * 16;
        RenderType type = wide ? OVERLAY_1 : OVERLAY;
        if (floor > 0 && floor < 17)
        {
            mat.push();
            // System.out.println(order);
            float dz = -0.001f * (1 + order);
            floor -= 1;
            final double x = (double) (3 - floor & 3) / (double) 4, y = ((double) 3 - (floor >> 2)) / 4;
            final float r = colour.getRed() / 255f;
            final float g = colour.getGreen() / 255f;
            final float b = colour.getBlue() / 255f;
            final float a = colour.getAlpha() / 255f;
            mat.translate(x, y, dz);
            final float amount = wide ? 0.25f : 0;
            render(type, mat, buffer, 0, 0, 0.25f + amount, 0.25f, r, g, b, a, 0, 1, 0, 1);
            mat.pop();
        }
    }

    public float[] locationFromNumber(final int number)
    {
        final float[] ret = new float[4];

        final int index = 16 + number;
        int dx, dz;
        dx = index % 10;
        dz = index / 10;

        ret[0] = dx / 10f;
        ret[2] = dz / 10f;

        ret[1] = (1 + dx) / 10f;
        ret[3] = (1 + dz) / 10f;

        return ret;
    }

    @Override
    public void render(T tileentity, float partialTicks, MatrixStack mat, IRenderTypeBuffer buff, int combinedLightIn,
            int combinedOverlayIn)
    {
        final ControllerTile monitor = (ControllerTile) tileentity;

        for (int i = 0; i < 6; i++)
        {
            final Direction dir = Direction.byIndex(i);

            if (!monitor.isSideOn(dir)) continue;
            mat.push();
            float f = dir.getHorizontalAngle();
            mat.translate(0.5D, 0.5D, 0.5D);
            mat.rotate(Vector3f.YN.rotationDegrees(f + 180));
            mat.translate(-0.5D, -0.5D, -0.5D);

            int a = 64;
            if (monitor.editFace[dir.ordinal()]) this.drawEditOverlay(mat, buff, monitor, dir);
            else if (monitor.floorDisplay[dir.ordinal()])
            {
                // Draw the white background
                Color colour = new Color(255, 255, 255, 255);
                drawOverLay(mat, buff, monitor, 1, colour, dir, true, 0);

                this.drawNumber(mat, buff, monitor.currentFloor - 1, 1);

                // Draw highlight over the background.
                if (monitor.calledFloor == monitor.floor)
                {
                    colour = new Color(255, 255, 0, a);
                    drawOverLay(mat, buff, monitor, 1, colour, dir, true, 1);
                }
                else if (monitor.currentFloor == monitor.floor)
                {
                    colour = new Color(0, 128, 255, a);
                    drawOverLay(mat, buff, monitor, 1, colour, dir, true, 2);
                }
            }
            else if (monitor.callFaces[dir.ordinal()])
            {
                // Draw the white background
                Color colour = new Color(255, 255, 255, 255);

                drawOverLay(mat, buff, monitor, 1, colour, dir, true, 0);
                this.drawNumber(mat, buff, monitor.floor - 1, 1);

                // Draw highlight over the background.
                if (monitor.calledFloor == monitor.floor)
                {
                    colour = new Color(255, 255, 0, a);
                    drawOverLay(mat, buff, monitor, 1, colour, dir, true, 1);
                }
                else if (monitor.currentFloor == monitor.floor)
                {
                    colour = new Color(0, 128, 255, a);
                    drawOverLay(mat, buff, monitor, 1, colour, dir, true, 2);
                }
            }
            else
            {
                // Draw numbers on top
                if (monitor.getLift() == null) this.drawFloorNumbers(monitor.getSidePage(dir));
                else
                {
                    final int page = monitor.getSidePage(dir);
                    if (monitor.getLift() != null) for (int floor = 0; floor < 16; floor++)
                        if (monitor.getLift().hasFloors[floor + page * 16]) this.drawNumber(floor + page * 16, floor);
                }

                if (monitor.getLift() != null)
                {
                    a = 64;
                    final Color mapped = new Color(255, 255, 255, 220);
                    Color colour = new Color(0, 255, 0, a);
                    drawOverLay(mat, buff, monitor, monitor.floor, colour, dir, false, 3);
                    colour = new Color(255, 255, 0, a);            
                    this.drawOverLay(mat, buff, monitor, monitor.getLift().getDestinationFloor(), colour, dir, false, 1);
                    colour = new Color(0, 128, 255, a);
                    this.drawOverLay(monitor, monitor.getLift().getCurrentFloor(), colour, dir, false, 2);
                    for (int j = monitor.getSidePage(dir) * 16; j < 16 + monitor.getSidePage(dir) * 16; j++)
                        if (monitor.getLift().hasFloors[j]) this.drawOverLay(mat, buff, monitor, j + 1, mapped, dir, false, 3);

                    // Draw numbers on top
                    final int page = monitor.getSidePage(dir);
                    for (int floor = 0; floor < 16; floor++)
                        if (monitor.lift.hasFloors[floor + page * 16])
                            this.drawNumber(mat, buff, floor + page * 16, floor);
                        
                }
                else
                {
                    // Draw background slots
                    final Color colour = new Color(255, 255, 255, 255);
                    for (int j = monitor.getSidePage(dir) * 16; j < 16 + monitor.getSidePage(dir) * 16; j++)
                    {
                        drawOverLay(mat, buff, monitor, j + 1, colour, dir, false, 0);
                    }
                    this.drawFloorNumbers(mat, buff, monitor.getSidePage(dir));
                }
            }
            mat.pop();
        }
    }
}
