package thut.tech.client.render;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import thut.api.entity.blockentity.render.RenderBlockEntity;
import thut.tech.common.entity.EntityLift;

public class RenderLift extends RenderBlockEntity<EntityLift>
{
    public RenderLift(final EntityRendererManager manager)
    {
        super(manager);
    }

    @Override
    public void doRender(final EntityLift entity, final double x, final double y, final double z, final float entityYaw,
            final float partialTicks)
    {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}
