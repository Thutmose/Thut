package pokecube.alternative;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import pokecube.alternative.network.PacketHandler;
import pokecube.alternative.utility.LogHelper;

@Mod(modid = Reference.MODID, name = Reference.MODNAME, version = Reference.VERSION)
public class PokecubeAlternative {

    public static final int GUI = 0;

    @SidedProxy(clientSide="pokecube.alternative.ClientProxy", serverSide="pokecube.alternative.utility.ServerProxy")
    public static CommonProxy proxy;

    @Mod.Instance(value=Reference.MODID)
    public static PokecubeAlternative instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LogHelper.info("PokeCubeExtras Pre-Init Started!");
        PokecubeAlternative.proxy.preInit(event);
        PacketHandler.init();
        LogHelper.info("PokeCubeExtras Pre-Init Complete!");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LogHelper.info("PokeCubeExtras Init Started!");
        PokecubeAlternative.proxy.init(event);
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
        LogHelper.info("PokeCubeExtras Init Complete!");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        LogHelper.info("PokeCubeExtras Post-Init Started!");
        PokecubeAlternative.proxy.postInit(event);
        LogHelper.info("PokeCubeExtras Post-Init Complete!");
    }

}
