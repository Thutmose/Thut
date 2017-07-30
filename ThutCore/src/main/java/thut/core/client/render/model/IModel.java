package thut.core.client.render.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import thut.api.maths.Vector3;
import thut.core.client.render.tabula.components.Animation;

public interface IModel
{
    public static class HeadInfo
    {
        public float yawCapMax      = 180;
        public float yawCapMin      = -180;
        public float pitchCapMax    = 40;
        public float pitchCapMin    = -40;
        public int   yawAxis        = 1;
        public int   pitchAxis      = 0;
        public int   headDirection  = 1;
        public int   headDirection1 = 1;
    }

    public HashMap<String, IExtendedModelPart> getParts();

    public void preProcessAnimations(Collection<Animation> animations);

    public Set<String> getHeadParts();

    default void setOffset(Vector3 offset)
    {

    }

    default void setHeadInfo(HeadInfo in)
    {

    }

    HeadInfo getHeadInfo();
}
