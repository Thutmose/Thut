package thut.core.client.render.animation;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thut.core.client.render.tabula.components.Animation;

public class CapabilityAnimation
{
    public static interface IAnimationHolder
    {
        /** This is the animation about to be run.
         * 
         * @param name */
        void setPendingAnimation(String name);

        /** Gets the animation about to be run.
         * 
         * @return */
        String getPendingAnimation();

        /** This is the animation that is currently being run.
         * 
         * @param name */
        void setCurrentAnimation(String name);

        /** Gets the animation currently being run.
         * 
         * @return */
        String getCurrentAnimation();

        /** the last tick this animation was run. Should return 0 if the
         * animation hasn't been run.
         * 
         * @param animation
         * @return */
        int getStep(Animation animation);

        /** Sets the last tick this animation was run. Can set to 0 to count
         * this animation as cleared.
         * 
         * @param animation
         * @param step */
        void setStep(Animation animation, int step);

        Set<Animation> getPlaying();

        /** should clear the ticks animations were run on */
        void clean();
    }

    public static class DefaultImpl implements IAnimationHolder, ICapabilityProvider
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
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == CAPABILITY;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            if (hasCapability(capability, facing)) return CAPABILITY.cast(this);
            return null;
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

    private static final Set<Class<? extends Entity>> ANIMATE = Sets.newHashSet();
    private static final ResourceLocation             ANIM    = new ResourceLocation("thutcore:animations");

    public static void setup()
    {
        CapabilityManager.INSTANCE.register(IAnimationHolder.class, new Capability.IStorage<IAnimationHolder>()
        {
            @Override
            public NBTBase writeNBT(Capability<IAnimationHolder> capability, IAnimationHolder instance, EnumFacing side)
            {
                return null;
            }

            @Override
            public void readNBT(Capability<IAnimationHolder> capability, IAnimationHolder instance, EnumFacing side,
                    NBTBase nbt)
            {
            }
        }, DefaultImpl::new);
        MinecraftForge.EVENT_BUS.register(CapabilityAnimation.class);
    }

    public static void registerAnimateClass(Class<? extends Entity> clazz)
    {
        ANIMATE.add(clazz);
    }

    @CapabilityInject(IAnimationHolder.class)
    public static final Capability<IAnimationHolder> CAPABILITY = null;

    @SubscribeEvent
    public static void attachCap(AttachCapabilitiesEvent<Entity> event)
    {
        if (ANIMATE.contains(event.getObject().getClass()))
        {
            event.addCapability(ANIM, new DefaultImpl());
        }
    }
}
