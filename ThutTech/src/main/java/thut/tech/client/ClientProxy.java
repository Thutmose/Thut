package thut.tech.client;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import thut.api.maths.Vector3;
import thut.tech.client.render.ControllerRenderer;
import thut.tech.client.render.RenderLift;
import thut.tech.common.CommonProxy;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.lift.ControllerTile;
import thut.tech.common.entity.EntityLift;

public class ClientProxy extends CommonProxy
{
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void RenderBounds(final RenderWorldLastEvent event)
    {
        ItemStack held;
        final PlayerEntity player = Minecraft.getInstance().player;
        if (!(held = player.getHeldItemMainhand()).isEmpty() || !(held = player.getHeldItemOffhand()).isEmpty())
        {
            if (held.getItem() != TechCore.LIFT) return;
            final Vec3d lookedAt = player.getEyePosition(event.getPartialTicks())
                    .add(player.getLook(event.getPartialTicks()));
            // TODO default this to the block the player is looking at instead!
            BlockPos pos = new BlockPos(lookedAt);
            if (!player.world.getBlockState(pos).getMaterial().isSolid())
            {
                final Vec3d loc = player.getPositionVector().add(0, player.getEyeHeight(), 0)
                        .add(player.getLookVec().scale(2));
                pos = new BlockPos(loc);
            }

            if (held.getTag() != null && held.getTag().contains("min"))
            {
                BlockPos min = Vector3.readFromNBT(held.getTag().getCompound("min"), "").getPos();
                BlockPos max = pos;
                AxisAlignedBB box = new AxisAlignedBB(min, max);
                min = new BlockPos(box.minX, box.minY, box.minZ);
                max = new BlockPos(box.maxX, box.maxY, box.maxZ).add(1, 1, 1);
                box = new AxisAlignedBB(min, max);

                final MatrixStack mat = event.getMatrixStack();

                final List<Pair<Vector3f, Vector3f>> lines = Lists.newArrayList();

                lines.add(Pair.of(new Vector3f((float) box.minX, (float) box.minY, (float) box.minZ),
                        new Vector3f((float) box.maxX, (float) box.minY, (float) box.minZ)));
                lines.add(Pair.of(new Vector3f((float) box.minX, (float) box.maxY, (float) box.minZ),
                        new Vector3f((float) box.maxX, (float) box.maxY, (float) box.minZ)));
                lines.add(Pair.of(new Vector3f((float) box.minX, (float) box.minY, (float) box.maxZ),
                        new Vector3f((float) box.maxX, (float) box.minY, (float) box.maxZ)));
                lines.add(Pair.of(new Vector3f((float) box.minX, (float) box.maxY, (float) box.maxZ),
                        new Vector3f((float) box.maxX, (float) box.maxY, (float) box.maxZ)));

                lines.add(Pair.of(new Vector3f((float) box.minX, (float) box.minY, (float) box.minZ),
                        new Vector3f((float) box.minX, (float) box.minY, (float) box.maxZ)));
                lines.add(Pair.of(new Vector3f((float) box.maxX, (float) box.minY, (float) box.minZ),
                        new Vector3f((float) box.maxX, (float) box.minY, (float) box.maxZ)));
                lines.add(Pair.of(new Vector3f((float) box.minX, (float) box.maxY, (float) box.minZ),
                        new Vector3f((float) box.minX, (float) box.maxY, (float) box.maxZ)));
                lines.add(Pair.of(new Vector3f((float) box.maxX, (float) box.maxY, (float) box.minZ),
                        new Vector3f((float) box.maxX, (float) box.maxY, (float) box.maxZ)));

                lines.add(Pair.of(new Vector3f((float) box.minX, (float) box.minY, (float) box.minZ),
                        new Vector3f((float) box.minX, (float) box.maxY, (float) box.minZ)));
                lines.add(Pair.of(new Vector3f((float) box.maxX, (float) box.minY, (float) box.minZ),
                        new Vector3f((float) box.maxX, (float) box.maxY, (float) box.minZ)));
                lines.add(Pair.of(new Vector3f((float) box.minX, (float) box.minY, (float) box.maxZ),
                        new Vector3f((float) box.minX, (float) box.maxY, (float) box.maxZ)));
                lines.add(Pair.of(new Vector3f((float) box.maxX, (float) box.minY, (float) box.maxZ),
                        new Vector3f((float) box.maxX, (float) box.maxY, (float) box.maxZ)));

                mat.push();

                final Vec3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo()
                        .getProjectedView();
                mat.translate(-projectedView.x, -projectedView.y, -projectedView.z);

                final Matrix4f positionMatrix = mat.getLast().getPositionMatrix();

                final IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
                final IVertexBuilder builder = buffer.getBuffer(RenderType.LINES);
                for (final Pair<Vector3f, Vector3f> line : lines)
                    thut.core.client.ClientProxy.line(builder, positionMatrix, line.getLeft(), line.getRight(), 1, 0, 0,
                            1f);
                mat.pop();
            }
        }
    }

    @Override
    public void setupClient(final FMLClientSetupEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
        RenderingRegistry.registerEntityRenderingHandler(EntityLift.TYPE, RenderLift::new);
        ClientRegistry.bindTileEntityRenderer(ControllerTile.TYPE, ControllerRenderer::new);
    }
}
