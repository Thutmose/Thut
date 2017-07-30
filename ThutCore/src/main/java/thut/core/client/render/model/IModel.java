package thut.core.client.render.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import thut.api.maths.Vector3;
import thut.core.client.render.tabula.components.Animation;

public interface IModel
{
    public static class HeadInfo
    {
        /** This should be updated to match the mob, incase the IModel needs to
         * do custom rendering itself. */
        public float headYaw;
        /** This should be updated to match the mob, incase the IModel needs to
         * do custom rendering itself. */
        public float headPitch;

        /** This is the current ticksExisted for the object being rendered.. */
        public int   currentTick    = 0;
        /** This is the ticksExisted before this render tick for the object
         * being rendered */
        public int   lastTick       = 0;

        public float yawCapMax      = 180;
        public float yawCapMin      = -180;
        public float pitchCapMax    = 40;
        public float pitchCapMin    = -40;
        public int   yawAxis        = 1;
        public int   pitchAxis      = 0;
        public int   yawDirection   = 1;
        public int   pitchDirection = 1;
    }

    public static ImmutableSet<String> emptyAnims = ImmutableSet.of();

    public HashMap<String, IExtendedModelPart> getParts();

    public void preProcessAnimations(Collection<Animation> animations);

    public Set<String> getHeadParts();

    default Set<String> getBuiltInAnimations()
    {
        return emptyAnims;
    }

    default void setOffset(Vector3 offset)
    {

    }

    default void setHeadInfo(HeadInfo in)
    {

    }

    HeadInfo getHeadInfo();
}
