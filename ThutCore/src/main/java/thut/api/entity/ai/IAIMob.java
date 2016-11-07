package thut.api.entity.ai;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import thut.api.entity.ai.AIThreadManager.AIStuff;

public interface IAIMob
{
    @CapabilityInject(IAIMob.class)
    public static final Capability<IAIMob> THUTMOBAI = null;
    
    AIStuff getAI();
    
    boolean selfManaged();
}
