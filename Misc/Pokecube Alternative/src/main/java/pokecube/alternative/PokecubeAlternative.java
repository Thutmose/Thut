package pokecube.alternative;

import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.alternative.container.belt.BeltPlayerData;
import pokecube.alternative.container.card.CardPlayerData;
import pokecube.alternative.event.TrainerCardHandler;
import pokecube.alternative.network.PacketHandler;
import pokecube.alternative.network.PacketSyncEnabled;
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
        Config.instance = new Config(PokecubeMod.core.getPokecubeConfig(event).getConfigFile());
        Config.instance.isEnabled = Config.instance.use;
        PokecubeAlternative.proxy.preInit(event);
        PokecubePlayerDataHandler.dataMap.add(CardPlayerData.class);
        if (!Config.instance.isEnabled) return;
        PacketHandler.init();
        PokecubePlayerDataHandler.dataMap.add(BeltPlayerData.class);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        /** This runs on a seperate config from the rest, so has its own handler
         * irrespective of the enabled state. */
        MinecraftForge.EVENT_BUS.register(new TrainerCardHandler());
        if (!Config.instance.isEnabled) return;
        PokecubeAlternative.proxy.init(event);
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, proxy);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        if (!Config.instance.isEnabled) return;
        PokecubeAlternative.proxy.postInit(event);
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        if (!Config.instance.isEnabled) return;
        event.registerServerCommand(new CommandConfig("pokealtsettings", Config.instance));
        Config.instance.isEnabled = Config.instance.use;
    }

    @SubscribeEvent
    /** This is done here, as if the mod is disabled, the normal event handler
     * isn't even registered.
     * 
     * @param event */
    public void PlayerLoggedInEvent(PlayerLoggedInEvent event)
    {
        if (!Config.instance.isEnabled) if (event.player instanceof EntityPlayerMP) PacketHandler.INSTANCE
                .sendTo(new PacketSyncEnabled(Config.instance.isEnabled), (EntityPlayerMP) event.player);
    }

    @NetworkCheckHandler
    public boolean checkRemote(Map<String, String> args, Side side)
    {
        if (!args.containsKey(Reference.MODID))
        {
            if (side == Side.SERVER)
            {
                if (PokecubeMod.debug) PokecubeMod.log("Alternative not found on server, Disabling for this session.");
                Config.instance.isEnabled = false;
            }
            if (side == Side.CLIENT && Config.instance.use)
            {
                if (PokecubeMod.debug) PokecubeMod.log(
                        "Client does not have alternative, which is enabled, This is bad, won't allow connection.");
                return false;
            }
        }
        return true;
    }
}
