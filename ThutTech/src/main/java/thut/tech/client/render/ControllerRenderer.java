package thut.tech.client.render;

import java.awt.Color;
import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderState.TextureState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import thut.tech.common.blocks.lift.ControllerTile;

public class ControllerRenderer<T extends TileEntity> extends TileEntityRenderer<T>
{

    private static final ResourceLocation overlay   = new ResourceLocation("thuttech:textures/blocks/overlay.png");
    private static final ResourceLocation overlay_1 = new ResourceLocation("thuttech:textures/blocks/overlay_1.png");
    private static final ResourceLocation font      = new ResourceLocation("thuttech:textures/blocks/font.png");

    private static void render(final RenderType type, final MatrixStack mat, final IRenderTypeBuffer buff,
            final float x1, final float y1, final float x2, final float y2, final float r, final float g, final float b,
            final float a, final float[] uvs)
    {
        ControllerRenderer.render(type, mat, buff, x1, y1, x2, y2, r, g, b, a, uvs[0], uvs[1], uvs[2], uvs[3]);
    }

    private static void render(final RenderType type, final MatrixStack mat, final IRenderTypeBuffer buff,
            final float x1, final float y1, final float x2, final float y2, final float r, final float g, final float b,
            final float a, final float u1, final float u2, final float v1, final float v2)
    {
        final IVertexBuilder buffer = buff.getBuffer(type);
        final Matrix4f o = mat.getLast().getPositionMatrix();
        buffer.pos(o, x2, y2, 0).color(r, g, b, a).tex(u1, v1).endVertex();
        buffer.pos(o, x2, y1, 0).color(r, g, b, a).tex(u1, v2).endVertex();
        buffer.pos(o, x1, y1, 0).color(r, g, b, a).tex(u2, v2).endVertex();
        buffer.pos(o, x1, y2, 0).color(r, g, b, a).tex(u2, v1).endVertex();
    }

    private static RenderType NUMBERS   = RenderType.get("thuttech:font", DefaultVertexFormats.POSITION_COLOR_TEX, 7,
            256, false, true, RenderType.State.builder().texture(new TextureState(ControllerRenderer.font, false,
                    false)).transparency(new RenderState.TransparencyState("translucent_transparency", () ->
                                                        {
                                                            RenderSystem.enableBlend();
                                                        }, () ->
                                                        {
                                                            RenderSystem.disableBlend();
                                                            RenderSystem.defaultBlendFunc();
                                                        })).writeMask(new RenderState.WriteMaskState(true, false))
                    .build(false));
    private static RenderType OVERLAY_1 = RenderType.get("thuttech:overlay_1", DefaultVertexFormats.POSITION_COLOR_TEX,
            7, 256, false, true, RenderType.State.builder().texture(new TextureState(ControllerRenderer.overlay_1,
                    false, false)).alpha(new RenderState.AlphaState(0.003921569F)).transparency(
                            new RenderState.TransparencyState("translucent_transparency", () ->
                                                                {
                                                                    RenderSystem.enableBlend();
                                                                    RenderSystem.defaultBlendFunc();
                                                                }, () ->
                                                                {
                                                                    RenderSystem.disableBlend();
                                                                })).writeMask(new RenderState.WriteMaskState(true,
                                                                        false)).build(false));
    private static RenderType OVERLAY   = RenderType.get("thuttech:overlay", DefaultVertexFormats.POSITION_COLOR_TEX, 7,
            256, false, true, RenderType.State.builder().texture(new TextureState(ControllerRenderer.overlay, false,
                    false)).alpha(new RenderState.AlphaState(0.003921569F)).transparency(
                            new RenderState.TransparencyState("translucent_transparency", () ->
                                                                {
                                                                    RenderSystem.enableBlend();
                                                                    RenderSystem.defaultBlendFunc();
                                                                }, () ->
                                                                {
                                                                    RenderSystem.disableBlend();
                                                                })).writeMask(new RenderState.WriteMaskState(true,
                                                                        false)).build(false));

    public ControllerRenderer(final TileEntityRendererDispatcher rendererDispatcherIn)
    {
        super(rendererDispatcherIn);
    }

    public void drawEditOverlay(final MatrixStack mat, final IRenderTypeBuffer buff, final ControllerTile monitor,
            final Direction side)
    {
        Color colour;
        // Call button toggle
        colour = monitor.callFaces[side.ordinal()] ? new Color(0, 255, 0, 255) : new Color(255, 0, 0, 255);
        this.drawOverLay(mat, buff, monitor, 1, colour, side, false, 0);

        // Floor Display toggle
        colour = monitor.floorDisplay[side.ordinal()] ? new Color(0, 255, 0, 255) : new Color(255, 0, 0, 255);
        this.drawOverLay(mat, buff, monitor, 2, colour, side, false, 0);
    }

    public void drawFloorNumbers(final MatrixStack mat, final IRenderTypeBuffer buffer, final int page)
    {
        for (int floor = 0; floor < 16; floor++)
            this.drawNumber(mat, buffer, floor + page * 16, floor);
    }

    private void drawNumber(final MatrixStack mat, final IRenderTypeBuffer buffer, int number, final int floor)
    {
        mat.push();
        final float dz = -0.006f;
        final boolean minus = number >= 64;
        if (minus) number -= 64;

        final double x = (double) (3 - floor & 3) / (double) 4, y = ((double) 3 - (floor >> 2)) / 4;
        final int actFloor = number;
        float[] uvs = this.locationFromNumber((actFloor + 1) % 10);
        final float[] uvs1 = this.locationFromNumber((actFloor + 1) / 10);
        final float r = 0, g = 0, b = 0, a = 1f;

        if (actFloor > 8)
        {
            mat.translate(x + 0.01, y + 0.06, dz);
            float dx = minus ? -0.03f : 0;
            float dy = -0.0f;
            ControllerRenderer.render(ControllerRenderer.NUMBERS, mat, buffer, 0 + dx, 0, 0.15f + dx, 0.15f + dy, r, g,
                    b, a, uvs);
            ControllerRenderer.render(ControllerRenderer.NUMBERS, mat, buffer, 0.1f + dx, 0, 0.25f + dx, 0.15f + dy, r,
                    g, b, a, uvs1);
            if (minus)
            {
                uvs = this.locationFromNumber(-3);
                dx = 0.135f;
                dy = -0.0175f;
                ControllerRenderer.render(ControllerRenderer.NUMBERS, mat, buffer, 0 + dx, 0, 0.15f + dx, 0.15f + dy, r,
                        g, b, a, uvs);
            }
        }
        else
        {
            mat.translate(x + 0.05, y + 0.06, dz);
            ControllerRenderer.render(ControllerRenderer.NUMBERS, mat, buffer, 0, 0, 0.15f, 0.15f, r, g, b, a, uvs);

            if (minus)
            {
                uvs = this.locationFromNumber(-3);
                final float dx = 0.075f;
                ControllerRenderer.render(ControllerRenderer.NUMBERS, mat, buffer, 0 + dx, 0, 0.15f + dx, 0.15f, r, g,
                        b, a, uvs);
            }
        }
        mat.pop();
    }

    public void drawOverLay(final MatrixStack mat, final IRenderTypeBuffer buffer, final ControllerTile monitor,
            int floor, final Color colour, final Direction side, final boolean wide, final int order)
    {
        if (!wide) floor = floor - monitor.getSidePage(side) * 16;
        final RenderType type = wide ? ControllerRenderer.OVERLAY_1 : ControllerRenderer.OVERLAY;
        if (floor > 0 && floor < 17)
        {
            mat.push();
            // System.out.println(order);
            final float dz = -0.001f * (1 + order);
            floor -= 1;
            final double x = (double) (3 - floor & 3) / (double) 4, y = ((double) 3 - (floor >> 2)) / 4;
            final float r = colour.getRed() / 255f;
            final float g = colour.getGreen() / 255f;
            final float b = colour.getBlue() / 255f;
            final float a = colour.getAlpha() / 255f;
            mat.translate(x, y, dz);
            final float amount = wide ? 0.25f : 0;
            ControllerRenderer.render(type, mat, buffer, 0, 0, 0.25f + amount, 0.25f, r, g, b, a, 0, 1, 0, 1);
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
    public void render(final T tileentity, final float partialTicks, final MatrixStack mat,
            final IRenderTypeBuffer buff, final int combinedLightIn, final int combinedOverlayIn)
    {
        final ControllerTile monitor = (ControllerTile) tileentity;

        if (monitor.getLift() != null)
        {
            monitor.calledFloor = monitor.getLift().getCalled() ? monitor.getLift().getDestinationFloor() : -1;
            monitor.currentFloor = monitor.getLift().getCurrentFloor();
        }

        final BlockState copied = monitor.copiedState;
        if (copied != null)
        {
            mat.push();
            final BlockRenderType blockrendertype = copied.getRenderType();
            if (blockrendertype == BlockRenderType.MODEL)
            {
                final IModelData data = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(copied)
                        .getModelData(monitor.getWorld(), monitor.getPos(), copied, EmptyModelData.INSTANCE);
                for (final RenderType type : RenderType.getBlockRenderTypes())
                    if (RenderTypeLookup.canRenderInLayer(copied, type))
                    {
                        final BlockRendererDispatcher blockRenderer = Minecraft.getInstance()
                                .getBlockRendererDispatcher();
                        final IBakedModel model = blockRenderer.getModelForState(copied);
                        blockRenderer.getBlockModelRenderer().renderModel(monitor.getWorld(), model, copied, monitor
                                .getPos(), mat, buff.getBuffer(type), true, new Random(), copied.getPositionRandom(
                                        monitor.getPos()), combinedOverlayIn, data);
                    }
            }
            mat.pop();
        }

        for (int i = 0; i < 6; i++)
        {
            final Direction dir = Direction.byIndex(i);

            if (!monitor.isSideOn(dir)) continue;
            mat.push();
            final float f = dir.getHorizontalAngle();
            mat.translate(0.5D, 0.5D, 0.5D);
            mat.rotate(Vector3f.YN.rotationDegrees(f + 180));
            mat.translate(-0.5D, -0.5D, -0.5D);

            int a = 64;
            if (monitor.editFace[dir.ordinal()]) this.drawEditOverlay(mat, buff, monitor, dir);
            else if (monitor.floorDisplay[dir.ordinal()])
            {
                // Draw the white background
                final Color colour = new Color(255, 255, 255, 255);
                mat.translate(-0.5, -0.095, 0);
                this.drawOverLay(mat, buff, monitor, 1, colour, dir, true, 0);

                mat.push();
                mat.translate(0.4, 0.0, 0);
                this.drawNumber(mat, buff, monitor.currentFloor - 1, 1);
                mat.pop();
            }
            else if (monitor.callFaces[dir.ordinal()])
            {
                // Draw the white background
                Color colour = new Color(255, 255, 255, 255);

                mat.translate(-0.5, -0.095, 0);
                this.drawOverLay(mat, buff, monitor, 1, colour, dir, true, 0);

                // Draw highlight over the background.
                if (monitor.calledFloor == monitor.floor)
                {
                    colour = new Color(255, 255, 0, a);
                    this.drawOverLay(mat, buff, monitor, 1, colour, dir, true, 1);
                }
                else if (monitor.currentFloor == monitor.floor)
                {
                    colour = new Color(0, 128, 255, a);
                    this.drawOverLay(mat, buff, monitor, 1, colour, dir, true, 2);
                }

                mat.push();
                mat.translate(0.4, 0.0, 0);
                this.drawNumber(mat, buff, monitor.floor - 1, 1);
                mat.pop();
            }
            else
            {
                // Draw numbers on top
                if (monitor.getLift() == null) this.drawFloorNumbers(mat, buff, monitor.getSidePage(dir));
                else
                {
                    final int page = monitor.getSidePage(dir);
                    if (monitor.getLift() != null) for (int floor = 0; floor < 16; floor++)
                        if (monitor.getLift().hasFloors[floor + page * 16]) this.drawNumber(mat, buff, floor + page
                                * 16, floor);
                }

                if (monitor.getLift() != null)
                {
                    a = 255;
                    final Color mapped = new Color(255, 255, 255, 220);
                    Color colour = new Color(0, 255, 0, a);
                    this.drawOverLay(mat, buff, monitor, monitor.floor, colour, dir, false, 3);
                    colour = new Color(255, 255, 0, a);
                    this.drawOverLay(mat, buff, monitor, monitor.getLift().getDestinationFloor(), colour, dir, false,
                            1);
                    colour = new Color(0, 128, 255, a);
                    this.drawOverLay(mat, buff, monitor, monitor.getLift().getCurrentFloor(), colour, dir, false, 2);
                    for (int j = monitor.getSidePage(dir) * 16; j < 16 + monitor.getSidePage(dir) * 16; j++)
                        if (monitor.getLift().hasFloors[j]) this.drawOverLay(mat, buff, monitor, j + 1, mapped, dir,
                                false, 3);

                    // Draw numbers on top
                    final int page = monitor.getSidePage(dir);
                    for (int floor = 0; floor < 16; floor++)
                        if (monitor.getLift().hasFloors[floor + page * 16]) this.drawNumber(mat, buff, floor + page
                                * 16, floor);

                }
                else
                {
                    // Draw background slots
                    final Color colour = new Color(255, 255, 255, 255);
                    for (int j = monitor.getSidePage(dir) * 16; j < 16 + monitor.getSidePage(dir) * 16; j++)
                        this.drawOverLay(mat, buff, monitor, j + 1, colour, dir, false, 0);
                    this.drawFloorNumbers(mat, buff, monitor.getSidePage(dir));
                }
            }
            mat.pop();
        }
    }
}
