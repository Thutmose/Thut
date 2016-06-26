package thut.core.common.handlers;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import thut.api.maths.ExplosionCustom;
import thut.core.common.config.ConfigBase;
import thut.core.common.config.Configure;

public class ConfigHandler extends ConfigBase
{
//    @Configure(category = "items")
//    private boolean           spout           = false;
//    @Configure(category = "items")
//    private boolean           tank            = false;
    @Configure(category = "misc")
    private int               explosionRadius = 127;
//    private static List<Item> items           = new ArrayList<Item>();

    public ConfigHandler()
    {
        super(null);
    }

    public ConfigHandler(File configFile)
    {
        super(configFile, new ConfigHandler());
        MinecraftForge.EVENT_BUS.register(this);
        populateSettings();
        applySettings();
        save();
    }

    @Override
    protected void applySettings()
    {
//        if (spout) items.add(new ItemSpout());
//        if (tank) items.add(new ItemTank());
//        items.add(new ItemDusts());

        ExplosionCustom.MAX_RADIUS = explosionRadius;

//        for (Item item : items)
//        {
//            GameRegistry.register(item);
//        }
    }
}
