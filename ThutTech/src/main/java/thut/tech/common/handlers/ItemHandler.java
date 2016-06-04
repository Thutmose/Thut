package thut.tech.common.handlers;

import static thut.api.ThutBlocks.lift;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import thut.tech.ThutTechReference;
import thut.tech.common.TechCore;
import thut.tech.common.items.ItemLinker;

public class ItemHandler
{
    public static void registerItems()
    {
        new ItemLinker();
        ItemLinker.instance.setRegistryName(ThutTechReference.MOD_ID, "devicelinker");
        ItemLinker.instance.setUnlocalizedName("devicelinker");
        GameRegistry.register(ItemLinker.instance);
    }

    public static void registerRecipes()
    {
        GameRegistry.addRecipe(new ItemStack(ItemLinker.instance), "xyx", " x ", "   ", 'x', Items.IRON_INGOT, 'y', Items.REDSTONE);
        GameRegistry.addRecipe(new ItemStack(lift, 1, 0), "xyx", "zxz", "zzz", 'x', Items.IRON_INGOT, 'y', Items.REDSTONE, 'z',
                new ItemStack(Blocks.STONE));
        GameRegistry.addRecipe(new ItemStack(lift, 1, 1), "xyx", "yxy", "xyx", 'x', Items.IRON_INGOT, 'y', Items.REDSTONE);
        GameRegistry.addShapelessRecipe(TechCore.getInfoBook(), new ItemStack(ItemLinker.instance), Items.BOOK);
    }
}
