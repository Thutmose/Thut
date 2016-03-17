package thut.core.common.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import thut.api.maths.ExplosionCustom;
import thut.core.common.config.ConfigBase;
import thut.core.common.config.Configure;
import thut.core.common.items.ItemDusts;
import thut.core.common.items.ItemSpout;
import thut.core.common.items.ItemTank;

public class ConfigHandler extends ConfigBase
{
    @Configure(category = "items")
    private boolean           spout           = false;
    @Configure(category = "items")
    private boolean           tank            = false;
    @Configure(category = "misc")
    private int               explosionRadius = 127;
    private static List<Item> items           = new ArrayList<Item>();

    public ConfigHandler()
    {
        super(null);
    }

    public ConfigHandler(File configFile)
    {
        super(configFile, new ConfigHandler());
    }

    @Override
    protected void applySettings()
    {
        if (spout) items.add(new ItemSpout());
        if (tank) items.add(new ItemTank());
        items.add(new ItemDusts());

        ExplosionCustom.MAX_RADIUS = explosionRadius;

        for (Item item : items)
        {
            GameRegistry.registerItem(item, item.getUnlocalizedName().substring(5));
        }
    }
}
