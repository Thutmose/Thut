package thut.tech.common;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import thut.core.common.Proxy;
import thut.tech.common.network.PacketLift;

public class CommonProxy implements Proxy
{
    @Override
    public void setup(FMLCommonSetupEvent event)
    {
        TechCore.packets.registerMessage(PacketLift.class, PacketLift::new);
    }
}
