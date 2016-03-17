package thut.core.client.render.model;

import net.minecraft.entity.EntityLiving;

public interface IModelRenderer<T extends EntityLiving>
{
    void doRender(T entity, double d, double d1, double d2, float f, float partialTick);

    IPartTexturer getTexturer();

    boolean hasPhase(String phase);

    void renderStatus(T entity, double d, double d1, double d2, float f, float partialTick);

    void setPhase(String phase);
}
