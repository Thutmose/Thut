package pokecube.alternative;

import java.io.File;
import java.util.logging.Level;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.interfaces.PokecubeMod;
import thut.core.common.config.ConfigBase;
import thut.core.common.config.Configure;

public class Config extends ConfigBase
{
    public static Config    instance;

    private static String[] DEFAULTBADGES   = { //@formatter:off
            "steel", 
            "ghost", 
            "flying", 
            "ground", 
            "ice", 
            "fighting", 
            "fire", 
            "bug" 
            };//@formatter:on

    public boolean          isEnabled       = true;

    @Configure(category = "client")
    public float            scale           = 1.0f;
    @Configure(category = "client")
    public int              shift           = 0;
    @Configure(category = "client")
    public String           beltOffset      = "0 0 -0.6";
    @Configure(category = "client")
    public String           beltOffsetSneak = "0.0 0.13125 -0.105";
    @Configure(category = "client")
    public boolean          cooldownMeter   = true;

    @Configure(category = "misc")
    public boolean          autoThrow       = true;
    @Configure(category = "misc")
    public boolean          trainerCard     = false;
    @Configure(category = "misc", needsMcRestart = true)
    public boolean          use             = true;
    @Configure(category = "misc")
    public String[]         badgeOrder      = { //@formatter:off
            "steel", 
            "ghost", 
            "flying", 
            "ground", 
            "ice", 
            "fighting", 
            "fire", 
            "bug" 
            };//@formatter:on

    public final float[]    offset          = new float[3];
    public final float[]    sneak           = new float[3];

    public Config()
    {
        super(null);
    }

    public Config(File file)
    {
        super(file, new Config());
        instance = this;
        MinecraftForge.EVENT_BUS.register(this);
        populateSettings();
        applySettings();
        save();
    }

    @Override
    protected void applySettings()
    {
        String[] args = beltOffsetSneak.split(" ");
        for (int i = 0; i < 3; i++)
        {
            sneak[i] = Float.parseFloat(args[i]);
        }
        args = beltOffset.split(" ");
        for (int i = 0; i < 3; i++)
        {
            offset[i] = Float.parseFloat(args[i]);
        }
        if (badgeOrder.length != DEFAULTBADGES.length)
        {
            badgeOrder = DEFAULTBADGES;
            PokecubeMod.log(Level.WARNING, "badgeOrder must contain 8 badges!");
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs)
    {
        if (eventArgs.getModID().equals(Reference.MODID))
        {
            populateSettings();
            applySettings();
            save();
        }
    }
}
