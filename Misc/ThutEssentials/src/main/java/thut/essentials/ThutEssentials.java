package thut.essentials;

import net.minecraft.command.CommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import thut.essentials.commands.CommandManager;
import thut.essentials.land.LandEventsHandler;
import thut.essentials.land.LandManager;
import thut.essentials.land.LandSaveHandler;
import thut.essentials.util.ConfigManager;
import thut.essentials.util.PlayerDataHandler;

@Mod(modid = ThutEssentials.MODID, name = "Thut Essentials", version = ThutEssentials.VERSION, dependencies = "", updateJSON = ThutEssentials.UPDATEURL, acceptableRemoteVersions = "*", acceptedMinecraftVersions = ThutEssentials.MCVERSIONS)
public class ThutEssentials
{
    public static final String   MODID      = "thutessentials";
    public static final String   VERSION    = "1.3.1";
    public static final String   UPDATEURL  = "";

    public final static String   MCVERSIONS = "[1.9.4]";

    @Instance(MODID)
    public static ThutEssentials instance;

    public ConfigManager         config;
    private CommandManager       manager;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        config = new ConfigManager(e.getSuggestedConfigurationFile());
        LandEventsHandler teams = new LandEventsHandler();
        MinecraftForge.EVENT_BUS.register(teams);
    }

    @Optional.Method(modid = "pokecube")
    @EventHandler
    public void pokecubeCompat(FMLPreInitializationEvent e)
    {
        new thut.essentials.compat.PokecubeCompat();
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        CommandHandler ch = (CommandHandler) event.getServer().getCommandManager();
        ch.getCommands().put("gm", ch.getCommands().get("gamemode"));
        manager = new CommandManager(event);
        MinecraftForge.EVENT_BUS.register(this);
        LandSaveHandler.loadGlobalData();
    }

    @EventHandler
    public void serverUnload(FMLServerStoppingEvent evt)
    {
        PlayerDataHandler.saveAll();
        PlayerDataHandler.clear();
        LandManager.clearInstance();
        manager.clear();
    }

    @SubscribeEvent
    void commandUseEvent(CommandEvent event)
    {

    }

    @SubscribeEvent
    public void PlayerLoggin(PlayerLoggedInEvent evt)
    {

    }
}
