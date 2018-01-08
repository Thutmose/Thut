package pokecube.alternative;

import java.util.Map;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.alternative.container.belt.BeltPlayerData;
import pokecube.alternative.container.card.CardPlayerData;
import pokecube.alternative.network.PacketHandler;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.interfaces.PokecubeMod;
import thut.core.common.commands.CommandConfig;

@Mod(modid = Reference.MODID, name = Reference.MODNAME, acceptableRemoteVersions = "*", guiFactory = "pokecube.alternative.client.gui.config.ModGuiFactory", version = Reference.VERSION)
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
        Config.instance = new Config(PokecubeMod.core.getPokecubeConfig(event).getConfigFile());
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

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandConfig("pokealtsettings", Config.instance));
        Config.instance.isEnabled = Config.instance.use;
    }

    @NetworkCheckHandler
    public boolean checkRemote(Map<String, String> args, Side side)
    {
        if (!args.containsKey(Reference.MODID))
        {
            if (side == Side.CLIENT) Config.instance.isEnabled = false;
            if (side == Side.SERVER && Config.instance.use) return false;
        }
        return true;
    }
}
