package thut.tech.client.render;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.entity.blockentity.RenderBlockEntity;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.handlers.ConfigHandler;

public class RenderLift extends RenderBlockEntity<LivingEntity>
{
    public static RenderLift hackyRenderer;

    public RenderLift(RenderManager manager)
    {
        super(manager);
        if (ConfigHandler.hackyRender)
        {
            MinecraftForge.EVENT_BUS.register(RenderLift.class);
            hackyRenderer = this;
        }
    }

    @Override
    public void doRender(LivingEntity entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        if (!ConfigHandler.hackyRender) super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @SubscribeEvent
    public static void hackyRender(RenderWorldLastEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        Entity cameraEntity = mc.getRenderViewEntity();
        BlockPos renderingVector = cameraEntity.getPosition();
        Frustum frustum = new Frustum();
        float partialTicks = event.getPartialTicks();
        double viewX = cameraEntity.lastTickPosX + (cameraEntity.posX - cameraEntity.lastTickPosX) * partialTicks;
        double viewY = cameraEntity.lastTickPosY + (cameraEntity.posY - cameraEntity.lastTickPosY) * partialTicks;
        double viewZ = cameraEntity.lastTickPosZ + (cameraEntity.posZ - cameraEntity.lastTickPosZ) * partialTicks;
        frustum.setPosition(viewX, viewY, viewZ);
        WorldClient client = mc.world;
        List<Entity> entities = client.loadedEntityList;
        RenderManager renderManager = Minecraft.getInstance().getRenderManager();
        for (Entity entity : entities)
            if (entity != null && entity instanceof EntityLift && entity != mc.player
                    && entity.isInRangeToRender3d(renderingVector.getX(), renderingVector.getY(),
                            renderingVector.getZ())
                    && (entity.ignoreFrustumCheck || frustum.isBoundingBoxInFrustum(entity.getBoundingBox()))
                    && entity.isEntityAlive() && entity.getRecursivePassengers().isEmpty())
                try
                {
                EntityLift lift = (EntityLift) entity;
                double d0 = lift.lastTickPosX + (lift.posX - lift.lastTickPosX) * (double) partialTicks - renderManager.viewerPosX;
                double d1 = lift.lastTickPosY + (lift.posY - lift.lastTickPosY) * (double) partialTicks - renderManager.viewerPosY;
                double d2 = lift.lastTickPosZ + (lift.posZ - lift.lastTickPosZ) * (double) partialTicks - renderManager.viewerPosZ;
                float f = lift.prevRotationYaw + (lift.rotationYaw - lift.prevRotationYaw) * partialTicks;
                ConfigHandler.hackyRender = false;
                hackyRenderer.doRender(lift, d0, d1, d2, f, partialTicks);
                ConfigHandler.hackyRender = true;
                }
                catch (Throwable t)
                {

                }
    }
}
