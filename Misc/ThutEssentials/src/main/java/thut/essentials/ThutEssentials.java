package thut.essentials;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import thut.essentials.commands.ColourName;
import thut.essentials.commands.Fly;

@Mod(modid = ThutEssentials.MODID, name = "Thut Essentials", version = ThutEssentials.VERSION, dependencies = "", updateJSON = ThutEssentials.UPDATEURL, acceptableRemoteVersions = "*", acceptedMinecraftVersions = ThutEssentials.MCVERSIONS)
public class ThutEssentials
{
    public static final String              MODID      = "thutessentials";
    public static final String              VERSION    = "0.0.1";
    public static final String              UPDATEURL  = "";

    public final static String              MCVERSIONS = "[1.9.4]";

    public static Map<String, List<String>> commands   = Maps.newHashMap();

    static
    {
        commands.put("colour", Lists.newArrayList("colour"));
        commands.put("tpa", Lists.newArrayList("tpa"));
        commands.put("fly", Lists.newArrayList("fly"));
        commands.put("rules", Lists.newArrayList("rules"));
        commands.put("addrule", Lists.newArrayList("addrule"));
        commands.put("delrule", Lists.newArrayList("delrule"));
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new ColourName());
        event.registerServerCommand(new Fly());
        MinecraftForge.EVENT_BUS.register(this);
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
