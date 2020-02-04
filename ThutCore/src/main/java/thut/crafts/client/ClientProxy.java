package thut.crafts.client;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import thut.api.entity.blockentity.render.RenderBlockEntity;
import thut.api.maths.Vector3;
import thut.crafts.CommonProxy;
import thut.crafts.ThutCrafts;
import thut.crafts.entity.CraftController;
import thut.crafts.entity.EntityCraft;
import thut.crafts.network.PacketCraftControl;

public class ClientProxy extends CommonProxy
{
    KeyBinding UP;
    KeyBinding DOWN;
    KeyBinding ROTATERIGHT;
    KeyBinding ROTATELEFT;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void clientTick(final TickEvent.PlayerTickEvent event)
    {
        if (event.phase == Phase.START || event.player != Minecraft.getInstance().player) return;
        control:
            if (event.player.isPassenger() && Minecraft.getInstance().currentScreen == null)
            {
                final Entity e = event.player.getRidingEntity();
                if (e instanceof EntityCraft)
                {
                    final ClientPlayerEntity player = (ClientPlayerEntity) event.player;
                    final CraftController controller = ((EntityCraft) e).controller;
                    if (controller == null) break control;
                    controller.backInputDown = player.movementInput.backKeyDown;
                    controller.forwardInputDown = player.movementInput.forwardKeyDown;
                    controller.leftInputDown = player.movementInput.leftKeyDown;
                    controller.rightInputDown = player.movementInput.rightKeyDown;
                    controller.upInputDown = this.UP.isKeyDown();
                    controller.downInputDown = this.DOWN.isKeyDown();
                    if (ThutCrafts.conf.canRotate)
                    {
                        controller.rightRotateDown = this.ROTATERIGHT.isKeyDown();
                        controller.leftRotateDown = this.ROTATELEFT.isKeyDown();
                    }
                    PacketCraftControl.sendControlPacket(e, controller);
                }
            }
    }

    @Override
    public PlayerEntity getPlayer()
    {
        return Minecraft.getInstance().player;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void RenderBounds(final RenderWorldLastEvent event)
    {
        ItemStack held;
        final PlayerEntity player = Minecraft.getInstance().player;
        if (!(held = player.getHeldItemMainhand()).isEmpty() || !(held = player.getHeldItemOffhand()).isEmpty())
        {
            if (held.getItem() != ThutCrafts.CRAFTMAKER) return;
            final Vec3d lookedAt = player.getEyePosition(event.getPartialTicks())
                    .add(player.getLook(event.getPartialTicks()));
            // TODO default this to the block the player is looking at instead!
            final BlockPos pos = new BlockPos(lookedAt);
            if (held.getTag() != null && held.getTag().contains("min"))
            {
                BlockPos min = Vector3.readFromNBT(held.getTag().getCompound("min"), "").getPos();
                BlockPos max = pos;
                AxisAlignedBB box = new AxisAlignedBB(min, max);
                min = new BlockPos(box.minX, box.minY, box.minZ);
                max = new BlockPos(box.maxX, box.maxY, box.maxZ).add(1, 1, 1);
                box = new AxisAlignedBB(min, max);
                
                box = new AxisAlignedBB(min).grow(1);
                
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
    public void setup(final FMLCommonSetupEvent event)
    {
        super.setup(event);
    }

    @Override
    public void setupClient(final FMLClientSetupEvent event)
    {
        this.UP = new KeyBinding("crafts.key.up", GLFW.GLFW_KEY_SPACE, "keys.crafts");
        this.DOWN = new KeyBinding("crafts.key.down", GLFW.GLFW_KEY_LEFT_CONTROL, "keys.crafts");

        final KeyConflictContext inGame = KeyConflictContext.IN_GAME;
        this.UP.setKeyConflictContext(inGame);
        this.DOWN.setKeyConflictContext(inGame);

        this.ROTATERIGHT = new KeyBinding("crafts.key.left", GLFW.GLFW_KEY_RIGHT_BRACKET, "keys.crafts");
        this.ROTATELEFT = new KeyBinding("crafts.key.right", GLFW.GLFW_KEY_LEFT_BRACKET, "keys.crafts");
        this.ROTATELEFT.setKeyConflictContext(inGame);
        this.ROTATERIGHT.setKeyConflictContext(inGame);

        ClientRegistry.registerKeyBinding(this.UP);
        ClientRegistry.registerKeyBinding(this.DOWN);
        ClientRegistry.registerKeyBinding(this.ROTATELEFT);
        ClientRegistry.registerKeyBinding(this.ROTATERIGHT);

        RenderingRegistry.registerEntityRenderingHandler(EntityCraft.CRAFTTYPE,
                (manager) -> new RenderBlockEntity<>(
                        manager));
    }
}
