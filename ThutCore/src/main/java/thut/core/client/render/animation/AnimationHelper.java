package thut.core.client.render.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.CapabilityAnimation.IAnimationHolder;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.tabula.components.Animation;
import thut.core.client.render.tabula.components.AnimationComponent;

/** This class applies the tabula style animations to models consisting of
 * IExtendedModelPart parts.
 * 
 * @author Thutmose */
public class AnimationHelper
{
    private final static Map<UUID, Holder> holderMap = Maps.newHashMap();

    private static class Holder implements IAnimationHolder
    {
        Map<UUID, Integer> stepsMap = Maps.newHashMap();
        Set<Animation>     playing  = Sets.newHashSet();
        private String     pending  = "idle";
        private String     current  = "idle";

        @Override
        public int getStep(Animation animation)
        {
            if (stepsMap.containsKey(animation.id)) return stepsMap.get(animation.id);
            return 0;
        }

        @Override
        public void setStep(Animation animation, int step)
        {
            stepsMap.put(animation.id, step);
        }

        @Override
        public void clean()
        {
            stepsMap.clear();
            pending = current = "idle";
            this.playing.clear();
        }

        @Override
        public void setPendingAnimation(String name)
        {
            this.pending = name;
        }

        @Override
        public String getPendingAnimation()
        {
            return this.pending;
        }

        @Override
        public Set<Animation> getPlaying()
        {
            return playing;
        }

        @Override
        public void setCurrentAnimation(String name)
        {
            this.current = name;
        }

        @Override
        public String getCurrentAnimation()
        {
            return this.current;
        }
    }

    public static IAnimationHolder getHolder(Entity mob)
    {
        if (mob.hasCapability(CapabilityAnimation.CAPABILITY, null))
            return mob.getCapability(CapabilityAnimation.CAPABILITY, null);
        if (holderMap.containsKey(mob.getUniqueID())) return holderMap.get(holderMap.get(mob.getUniqueID()));
        else
        {
            Holder holder = new Holder();
            holderMap.put(mob.getUniqueID(), holder);
            return holder;
        }
    }

    public static boolean doAnimation(List<Animation> list, Entity entity, String partName, IExtendedModelPart part,
            float partialTick, float limbSwing)
    {
        boolean animate = false;
        IAnimationHolder holder = getHolder(entity);
        if (holder != null) for (Animation animation : list)
        {
            animate = animate(animation, holder, partName, part, partialTick, limbSwing, entity.ticksExisted)
                    || animate;
        }
        return animate;
    }

    public static boolean animate(Animation animation, IAnimationHolder animate, String partName,
            IExtendedModelPart part, float partialTick, float limbSwing, int tick)
    {
        ArrayList<AnimationComponent> components = animation.getComponents(partName);
        boolean animated = false;
        Vector3 temp = Vector3.getNewVector();
        float x = 0, y = 0, z = 0;
        float sx = 1, sy = 1, sz = 1;
        int aniTick = animate.getStep(animation);
        if (aniTick == 0) aniTick = tick;
        float time1 = aniTick;
        float time2 = 0;
        int animationLength = animation.getLength();
        float limbSpeedFactor = 1f;
        time1 = (time1 + partialTick) % animationLength;
        time2 = (limbSwing * limbSpeedFactor) % animationLength;
        animate.setStep(animation, tick);
        if (components != null) for (AnimationComponent component : components)
        {
            float time = component.limbBased ? time2 : time1;
            if (time >= component.startKey)
            {
                animated = true;
                float componentTimer = time - component.startKey;
                if (componentTimer > component.length)
                {
                    componentTimer = component.length;
                }
                temp.addTo(component.posChange[0] / component.length * componentTimer + component.posOffset[0],
                        component.posChange[1] / component.length * componentTimer + component.posOffset[1],
                        component.posChange[2] / component.length * componentTimer + component.posOffset[2]);
                x += (float) (component.rotChange[0] / component.length * componentTimer + component.rotOffset[0]);
                y += (float) (component.rotChange[1] / component.length * componentTimer + component.rotOffset[1]);
                z += (float) (component.rotChange[2] / component.length * componentTimer + component.rotOffset[2]);

                sx += (float) (component.scaleChange[0] / component.length * componentTimer + component.scaleOffset[0]);
                sy += (float) (component.scaleChange[1] / component.length * componentTimer + component.scaleOffset[1]);
                sz += (float) (component.scaleChange[2] / component.length * componentTimer + component.scaleOffset[2]);

                // Apply hidden like this so last hidden state is kept
                part.setHidden(component.hidden);
            }
        }
        if (animated)
        {
            part.setPreTranslations(temp);
            part.setPreScale(temp.set(sx, sy, sz));
            Vector4 angle = null;
            if (z != 0)
            {
                angle = new Vector4(0, 0, 1, z);
            }
            if (x != 0)
            {
                if (angle != null)
                {
                    angle = angle.addAngles(new Vector4(1, 0, 0, x));
                }
                else
                {
                    angle = new Vector4(1, 0, 0, x);
                }
            }
            if (y != 0)
            {
                if (angle != null)
                {
                    angle = angle.addAngles(new Vector4(0, 1, 0, y));
                }
                else
                {
                    angle = new Vector4(0, 1, 0, y);
                }
            }
            if (angle != null) part.setPreRotations(angle);
        }
        return animated;
    }
}
