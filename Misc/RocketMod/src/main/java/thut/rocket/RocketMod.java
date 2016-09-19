package thut.rocket;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = RocketMod.MODID, name = "Rocket Mod", version = RocketMod.VERSION, acceptableRemoteVersions = "*")
public class RocketMod
{
    public static final String MODID   = "rocketmod";
    public static final String VERSION = "1.0.0";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    
}
