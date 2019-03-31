package thut.core.client.render.model;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.AnimationHelper;
import thut.core.client.render.animation.CapabilityAnimation.IAnimationHolder;
import thut.core.client.render.tabula.components.Animation;

public interface IModelRenderer<T extends EntityLiving>
{
    public static final String DEFAULTPHASE = "idle";
    static final Vector3       DEFAULTSCALE = Vector3.getNewVector().set(1);

    public static class Vector5
    {
        public Vector4 rotations;
        public int     time;

        public Vector5()
        {
            this.time = 0;
            this.rotations = new Vector4();
        }

        public Vector5(Vector4 rotation, int time)
        {
            this.rotations = rotation;
            this.time = time;
        }

        public Vector5 interpolate(Vector5 v, float time, boolean wrap)
        {
            if (v.time == 0) return this;

            if (Double.isNaN(rotations.x))
            {
                rotations = new Vector4();
            }
            Vector4 rotDiff = rotations.copy();

            if (rotations.x == rotations.z && rotations.z == rotations.y && rotations.y == rotations.w
                    && rotations.w == 0)
            {
                rotations.x = 1;
            }

            if (!v.rotations.equals(rotations))
            {
                rotDiff = v.rotations.subtractAngles(rotations);

                rotDiff = rotations.addAngles(rotDiff.scalarMult(time));
            }
            if (Double.isNaN(rotDiff.x))
            {
                rotDiff = new Vector4(0, 1, 0, 0);
            }
            Vector5 ret = new Vector5(rotDiff, v.time);
            return ret;
        }

        @Override
        public String toString()
        {
            return "|r:" + rotations + "|t:" + time;
        }
    }

    void doRender(T entity, double d, double d1, double d2, float f, float partialTick);

    IPartTexturer getTexturer();

    IAnimationChanger getAnimationChanger();

    void setTexturer(IPartTexturer texturer);

    void setAnimationChanger(IAnimationChanger changer);

    default String getAnimation(Entity entityIn)
    {
        IAnimationHolder holder = AnimationHelper.getHolder(entityIn);
        if (holder != null) return holder.getPendingAnimation();
        return "idle";
    }

    boolean hasAnimation(String phase, Entity entity);

    void renderStatus(T entity, double d, double d1, double d2, float f, float partialTick);

    default void setAnimation(String phase, Entity entity)
    {
        IAnimationHolder holder = AnimationHelper.getHolder(entity);
        if (holder != null) holder.setPendingAnimation(phase);
    }

    void scaleEntity(Entity entity, IModel model, float partialTick);

    HashMap<String, List<Animation>> getAnimations();

    default Vector3 getScale()
    {
        return DEFAULTSCALE;
    }

    default Vector3 getRotationOffset()
    {
        return Vector3.empty;
    }

    @Nullable
    default Vector5 getRotations()
    {
        return null;
    }
}
