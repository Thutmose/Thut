package thut.core.common.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import thut.api.maths.ExplosionCustom;
import thut.core.common.ThutCore;
import thut.core.common.blocks.fluids.BlockDust;
import thut.core.common.blocks.fluids.BlockMelt;
import thut.core.common.blocks.fluids.BlockSolidMelt;
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
        MinecraftForge.EVENT_BUS.register(this);
        populateSettings();
        applySettings();
        save();
    }

    @Override
    protected void applySettings()
    {
        ExplosionCustom.MAX_RADIUS = explosionRadius;
    }

    public void preinit()
    {
        if (spout) items.add(new ItemSpout());
        if (tank) items.add(new ItemTank());
        items.add(new ItemDusts());

        for (Item item : items)
        {
            GameRegistry.registerItem(item, item.getUnlocalizedName().substring(5));
        }
        FluidRegistry.registerFluid(BlockDust.DUST);
        FluidRegistry.registerFluid(BlockMelt.MELT);
        FluidRegistry
                .registerFluid(new Fluid("thutcore:solidmelt", new ResourceLocation(ThutCore.modid, "blocks/solidmelt"),
                        new ResourceLocation(ThutCore.modid, "blocks/solidmelt")));

        GameRegistry.registerBlock(new BlockDust().setRegistryName(new ResourceLocation(ThutCore.modid, "dust"))
                .setUnlocalizedName("dust"));
        GameRegistry.registerBlock(new BlockMelt().setRegistryName(new ResourceLocation(ThutCore.modid, "melt"))
                .setUnlocalizedName("melt"));
        GameRegistry.registerBlock(new BlockSolidMelt()
                .setRegistryName(new ResourceLocation(ThutCore.modid, "solidmelt")).setUnlocalizedName("solidmelt"));

    }
}
