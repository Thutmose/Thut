package pokecube.capturerestrict;

import java.io.File;
import java.util.HashMap;

import com.google.common.collect.Maps;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.events.CaptureEvent;
import pokecube.core.events.PostPostInit;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.pokecubes.EntityPokecube;
import thut.core.common.config.ConfigBase;
import thut.core.common.config.Configure;

@Mod(modid = CaptureRestrict.MODID, name = "Pokecube Capture Restrictor", version = CaptureRestrict.VERSION, dependencies = "required-after:pokecube", acceptableRemoteVersions = "*", acceptedMinecraftVersions = CaptureRestrict.MCVERSIONS)
public class CaptureRestrict
{
    public static final String             MODID      = "pokecube_capturedeny";
    public static final String             VERSION    = "@VERSION@";

    public final static String             MCVERSIONS = "[1.9.4]";
    private Config                         config;

    private HashMap<PokedexEntry, Integer> overrides  = Maps.newHashMap();

    @EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        config = new Config(PokecubeCore.core.getPokecubeConfig(e).getConfigFile());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void canCapture(CaptureEvent.Pre evt)
    {
        int level = evt.caught.getLevel();
        boolean legendary = evt.caught.getPokedexEntry().legendary;
        int max = overrides.containsKey(evt.caught.getPokedexEntry()) ? overrides.get(evt.caught.getPokedexEntry())
                : legendary ? config.maxCaptureLevelLegendary : config.maxCaptureLevelNormal;
        if (level > max)
        {
            evt.setCanceled(true);
            evt.setResult(Result.DENY);
            Entity catcher = ((EntityPokecube) evt.pokecube).shootingEntity;
            if (catcher instanceof EntityPlayer)
            {
                ((EntityPlayer) catcher).addChatMessage(new TextComponentTranslation("pokecube.denied"));
            }
            evt.pokecube.entityDropItem(((EntityPokecube) evt.pokecube).getEntityItem(), (float) 0.5);
            evt.pokecube.setDead();
        }
    }

    @SubscribeEvent
    public void postpostInit(PostPostInit evt)
    {
        for (String s : config.maxCaptureLevelOverrides)
        {
            String[] args = s.split(":");
            try
            {
                int level = Integer.parseInt(args[1]);
                PokedexEntry entry = Database.getEntry(args[0]);
                if (entry == null)
                {
                    PokecubeMod.log(args[0] + " not found in database");
                }
                else
                {
                    overrides.put(entry, level);
                }
            }
            catch (Exception e)
            {
                PokecubeMod.log("Error with " + s);
                e.printStackTrace();
            }
        }
    }

    public static class Config extends ConfigBase
    {
        @Configure(category = "misc")
        int      maxCaptureLevelNormal    = 100;
        @Configure(category = "misc")
        int      maxCaptureLevelLegendary = 100;
        @Configure(category = "misc")
        String[] maxCaptureLevelOverrides = { "mew:100" };

        public Config()
        {
            super(null);
        }

        public Config(File configFile)
        {
            super(configFile, new Config());
            MinecraftForge.EVENT_BUS.register(this);
            populateSettings();
            applySettings();
            save();
        }

        @Override
        protected void applySettings()
        {
        }
    }
}
