package thut.api.entity.ai;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import thut.api.entity.ai.AIThreadManager.AIStuff;

public interface IAIMob
{
    /** This capability allows adding the AI to an existing mob. Care must be
     * taken to ensure that the mob's original AI is disabled, and that
     * appropriate IAIRunnables are made and added to the AIStuff for the
     * capability. */
    @CapabilityInject(IAIMob.class)
    public static final Capability<IAIMob> THUTMOBAI = null;

    /** returns the AIStuff for this mob. This is the object that should manage
     * all of the IAIRunnables and ILogicRunnables.
     * 
     * @return */
    AIStuff getAI();

    /** Does the implementer of this interface manage the AI by itself, if this
     * returns true, then the tickhandler will ignore this object. It is best to
     * manage the AI yourself, as then you can ensure that it is called at the
     * most appropriate time.
     * 
     * @return */
    boolean selfManaged();

    void setWrapped(boolean wrapped);

    boolean vanillaWrapped();

    public static class Default implements IAIMob
    {
        boolean wrapped = false;

        @Override
        public AIStuff getAI()
        {
            return null;
        }

        @Override
        public boolean selfManaged()
        {
            return false;
        }

        @Override
        public void setWrapped(boolean wrapped)
        {
            this.wrapped = wrapped;
        }

        @Override
        public boolean vanillaWrapped()
        {
            return wrapped;
        }

    }
}
