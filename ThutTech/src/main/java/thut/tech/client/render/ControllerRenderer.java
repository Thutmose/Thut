package thut.tech.client.render;

import java.awt.Color;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import thut.api.entity.blockentity.world.IBlockEntityWorld;
import thut.tech.common.blocks.lift.ControllerTile;
import thut.tech.common.entity.EntityLift;

public class ControllerRenderer<T extends TileEntity> extends TileEntityRenderer<T>
{

    private final ResourceLocation  overlay    = new ResourceLocation("thuttech:textures/blocks/overlay.png");
    private final ResourceLocation  overlay_1  = new ResourceLocation("thuttech:textures/blocks/overlay_1.png");
    private final ResourceLocation  font       = new ResourceLocation("thuttech:textures/blocks/font.png");

    private static RenderType RENDERTYPE = RenderType.get("translucent", DefaultVertexFormats.BLOCK, 7, 262144, true, true, RenderType.State.builder().build(false));

    public ControllerRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
    {
        super(rendererDispatcherIn);
    }

    public void drawEditOverlay(MatrixStack mat, IVertexBuilder buff, ControllerTile monitor, final Direction side)
    {
        // Call button toggle
        // Draw the white background
        mat.push();
        Color colour = new Color(255, 255, 255, 255);
        drawOverLay(mat, buff, monitor, 1, colour, side, false, 0);
        colour = monitor.callFaces[side.ordinal()] ? new Color(0, 255, 0, 255) : new Color(255, 0, 0, 255);
        drawOverLay(mat, buff, monitor, 1, colour, side, false, 1);
        mat.pop();

        // Floor Display toggle
        // Draw the white background
        mat.push();
        colour = new Color(255, 255, 255, 255);
        drawOverLay(mat, buff, monitor, 2, colour, side, false, 0);
        colour = monitor.floorDisplay[side.ordinal()] ? new Color(0, 255, 0, 255) : new Color(255, 0, 0, 255);
        drawOverLay(mat, buff, monitor, 2, colour, side, false, 1);
        mat.pop();
    }

    public void drawFloorNumbers(MatrixStack mat, IVertexBuilder buffer, final int page)
    {
        for (int floor = 0; floor < 16; floor++)
            this.drawNumber(mat, buffer, floor + page * 16, floor);
    }

    private void drawNumber(MatrixStack mat, IVertexBuilder buffer, int number, final int floor)
    {
        final TextureManager renderengine = Minecraft.getInstance().textureManager;

        mat.push();
        if (renderengine != null) renderengine.bindTexture(this.font);
        final float dz = -0.001f;
        final boolean minus = number >= 64;
        if (minus) number -= 64;

        final double x = (double) (3 - floor & 3) / (double) 4, y = ((double) 3 - (floor >> 2)) / 4;
        final int actFloor = number;
        float[] uvs = this.locationFromNumber((actFloor + 1) % 10);
        final float[] uvs1 = this.locationFromNumber((actFloor + 1) / 10);

        if (actFloor > 8)
        {
            mat.translate(x + 0.01, y + 0.06, dz * (5 + 1));
            float dx = minus ? -0.03f : 0;
            float dy = -0.0f;
            buffer.pos(0.15 + dx, 0.15 + dy, 0).color(0, 0, 0, 255).tex(uvs[0], uvs[2]).endVertex();
            buffer.pos(0.15 + dx, 0.0 + dy, 0).color(0, 0, 0, 255).tex(uvs[0], uvs[3]).endVertex();

            buffer.pos(0 + dx, 0.0 + dy, 0).color(0, 0, 0, 255).tex(uvs[1], uvs[3]).endVertex();
            buffer.pos(0 + dx, 0.15 + dy, 0).color(0, 0, 0, 255).tex(uvs[1], uvs[2]).endVertex();

            buffer.pos(0.15 + dx + 0.1, 0.15 + dy, 0).color(0, 0, 0, 255).tex(uvs1[0], uvs1[2]).endVertex();
            buffer.pos(0.15 + dx + 0.1, 0 + dy, 0).color(0, 0, 0, 255).tex(uvs1[0], uvs1[3]).endVertex();

            buffer.pos(0 + dx + 0.1, 0 + dy, 0).color(0, 0, 0, 255).tex(uvs1[1], uvs1[3]).endVertex();
            buffer.pos(0 + dx + 0.1, 0.15 + dy, 0).color(0, 0, 0, 255).tex(uvs1[1], uvs1[2]).endVertex();

            if (minus)
            {
                uvs = this.locationFromNumber(-3);
                dx = 0.135f;
                dy = -0.0175f;
                buffer.pos(0.15 + dx, 0.15 + dy, 0).color(0, 0, 0, 255).tex(uvs[0], uvs[2]).endVertex();
                buffer.pos(0.15 + dx, 0.0 + dy, 0).color(0, 0, 0, 255).tex(uvs[0], uvs[3]).endVertex();

                buffer.pos(0 + dx, 0.0 + dy, 0).color(0, 0, 0, 255).tex(uvs[1], uvs[3]).endVertex();
                buffer.pos(0 + dx, 0.15 + dy, 0).color(0, 0, 0, 255).tex(uvs[1], uvs[2]).endVertex();
            }
        }
        else
        {
            mat.translate(x + 0.05, y + 0.06, dz * (5 + 1));
            buffer.pos(0.15, 0.15, 0).color(0, 0, 0, 255).tex(uvs[0], uvs[2]).endVertex();
            buffer.pos(0.15, 0.0, 0).color(0, 0, 0, 255).tex(uvs[0], uvs[3]).endVertex();

            buffer.pos(0, 0.0, 0).color(0, 0, 0, 255).tex(uvs[1], uvs[3]).endVertex();
            buffer.pos(0, 0.15, 0).color(0, 0, 0, 255).tex(uvs[1], uvs[2]).endVertex();

            if (minus)
            {
                uvs = this.locationFromNumber(-3);
                final float dx = 0.075f;
                buffer.pos(0.15 + dx, 0.15, 0).color(0, 0, 0, 255).tex(uvs[0], uvs[2]).endVertex();
                buffer.pos(0.15 + dx, 0.0, 0).color(0, 0, 0, 255).tex(uvs[0], uvs[3]).endVertex();

                buffer.pos(0 + dx, 0.0, 0).color(0, 0, 0, 255).tex(uvs[1], uvs[3]).endVertex();
                buffer.pos(0 + dx, 0.15, 0).color(0, 0, 0, 255).tex(uvs[1], uvs[2]).endVertex();
            }
        }
        mat.pop();
    }

    public void drawOverLay(MatrixStack mat, IVertexBuilder buffer, final ControllerTile monitor, int floor,
            final Color colour, final Direction side, final boolean wide, final int order)
    {
        if (!wide) floor = floor - monitor.getSidePage(side) * 16;
        if (floor > 0 && floor < 17)
        {
            final TextureManager renderengine = Minecraft.getInstance().textureManager;
            mat.push();
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
            mat.translate(x, y, dz);
            final double amount = wide ? 0.25 : 0;
            buffer.pos(0.25 + amount, 0.25, 0).tex(0, 0).color(r, g, b, a).endVertex();
            buffer.pos(0.25 + amount, 0, 0).tex(0, 1).color(r, g, b, a).endVertex();
            buffer.pos(0, 0, 0).tex(1, 1).color(r, g, b, a).endVertex();
            buffer.pos(0, 0.25, 0).tex(1, 0).color(r, g, b, a).endVertex();

            mat.pop();
        }
    }

    public float[] locationFromNumber(final int number)
    {
        final float[] ret = new float[4];

        // if (number > 9 || number < 0) return ret;
        final int index = 16 + number;
        int dx, dz;
        dx = index % 10;
        dz = index / 10;
        // System.out.println(dx+" "+dz);

        ret[0] = dx / 10f;
        ret[2] = dz / 10f;

        ret[1] = (1 + dx) / 10f;
        ret[3] = (1 + dz) / 10f;

        return ret;
    }

    @Override
    public void render(T tileentity, float partialTicks, MatrixStack mat, IRenderTypeBuffer buffer, int combinedLightIn,
            int combinedOverlayIn)
    {
        // TODO Auto-generated method stub
        final ControllerTile monitor = (ControllerTile) tileentity;

        for (int i = 0; i < 6; i++)
        {
            final Direction dir = Direction.byIndex(i);

            if (!monitor.isSideOn(dir)) continue;

            mat.push();

            IVertexBuilder buff = buffer.getBuffer(RENDERTYPE);

            if (dir == Direction.EAST)
            {
                mat.translate(1, 0, 0);
                mat.rotate(Vector3f.ZP.rotationDegrees(270.0F));
            }
            else if (dir == Direction.SOUTH)
            {
                mat.translate(1, 0, 1);
                mat.rotate(Vector3f.ZP.rotationDegrees(180.0F));
            }
            else if (dir == Direction.WEST)
            {
                mat.translate(0, 0, 1);
                mat.rotate(Vector3f.ZP.rotationDegrees(90.0F));
            }
            final int a = 64;
            if (monitor.editFace[dir.ordinal()]) this.drawEditOverlay(mat, buff, monitor, dir);
            else if (monitor.floorDisplay[dir.ordinal()])
            {
                // Draw the white background
                mat.push();
                mat.translate(-0.5, -0.095, 0);
                Color colour = new Color(255, 255, 255, 255);
                drawOverLay(mat, buff, monitor, 1, colour, dir, true, 0);
                mat.pop();

                mat.push();
                mat.translate(-0.11, -0.1, 0);
                this.drawNumber(mat, buff, monitor.currentFloor - 1, 1);
                mat.pop();
                mat.push();
                mat.translate(-0.5, -0.095, 0);

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
                mat.pop();
            }
            else if (monitor.callFaces[dir.ordinal()])
            {
                // Draw the white background
                mat.push();
                mat.translate(-0.5, -0.095, 0);
                Color colour = new Color(255, 255, 255, 255);
                drawOverLay(mat, buff, monitor, 1, colour, dir, true, 0);
                mat.pop();

                mat.push();
                mat.translate(-0.11, -0.1, 0);
                this.drawNumber(mat, buff, monitor.floor - 1, 1);
                mat.pop();
                mat.push();
                mat.translate(-0.5, -0.095, 0);

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
                mat.pop();
            }
            else
            {
                // Draw numbers on top
                if (monitor.liftID == null) this.drawFloorNumbers(mat, buff, monitor.getSidePage(dir));
                else
                {
                    if (monitor.lift == null)
                        monitor.lift = EntityLift.getLiftFromUUID(monitor.liftID, tileentity.getWorld());
                    final int page = monitor.getSidePage(dir);
                    if (monitor.lift != null) for (int floor = 0; floor < 16; floor++)
                        if (monitor.lift.hasFloors[floor + page * 16])
                            this.drawNumber(mat, buff, floor + page * 16, floor);
                }

                if (monitor.lift != null)
                {
                    final Color mapped = new Color(255, 255, 255, a);
                    Color colour = new Color(0, 255, 0, a);
                    drawOverLay(mat, buff, monitor, monitor.floor, colour, dir, false, 0);
                    colour = new Color(255, 255, 0, a);
                    drawOverLay(mat, buff, monitor, monitor.lift.getDestinationFloor(), colour, dir, false, 1);
                    colour = new Color(0, 128, 255, a);
                    drawOverLay(mat, buff, monitor, monitor.lift.getCurrentFloor(), colour, dir, false, 2);
                    for (int j = monitor.getSidePage(dir) * 16; j < 16 + monitor.getSidePage(dir) * 16; j++)
                        if (monitor.lift.hasFloors[j]) drawOverLay(mat, buff, monitor, j + 1, mapped, dir, false, 3);
                }
                else
                {
                    // Draw background slots
                    final Color colour = new Color(255, 255, 255, 255);
                    for (int j = monitor.getSidePage(dir) * 16; j < 16 + monitor.getSidePage(dir) * 16; j++)
                        drawOverLay(mat, buff, monitor, j + 1, colour, dir, false, 0);
                }
            }
            mat.pop();
        }
    }
}
