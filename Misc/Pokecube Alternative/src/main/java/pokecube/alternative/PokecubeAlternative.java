package pokecube.alternative;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import pokecube.alternative.container.belt.BeltPlayerData;
import pokecube.alternative.container.card.CardPlayerData;
import pokecube.alternative.network.PacketHandler;
import pokecube.core.handlers.PokecubePlayerDataHandler;

@Mod(modid = Reference.MODID, name = Reference.MODNAME, version = Reference.VERSION)
public class PokecubeAlternative
{
    @SidedProxy(clientSide = "pokecube.alternative.ClientProxy", serverSide = "pokecube.alternative.utility.ServerProxy")
    public static CommonProxy         proxy;

    @Mod.Instance(value = Reference.MODID)
    public static PokecubeAlternative instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        PokecubeAlternative.proxy.preInit(event);
        PacketHandler.init();
        PokecubePlayerDataHandler.dataMap.add(BeltPlayerData.class);
        PokecubePlayerDataHandler.dataMap.add(CardPlayerData.class);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        PokecubeAlternative.proxy.init(event);
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        PokecubeAlternative.proxy.postInit(event);
    }

}
