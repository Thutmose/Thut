package thut.tech.common.handlers;

import static net.minecraft.init.Blocks.stone;
import static net.minecraft.init.Items.iron_ingot;
import static net.minecraft.init.Items.redstone;
import static thut.api.ThutBlocks.lift;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import thut.tech.common.TechCore;
import thut.tech.common.items.ItemLinker;

public class ItemHandler
{
    public static void registerItems()
    {
        new ItemLinker();
        GameRegistry.registerItem(ItemLinker.instance, "devicelinker");
    }

    public static void registerRecipes()
    {
        GameRegistry.addRecipe(new ItemStack(ItemLinker.instance), "xyx", " x ", "   ", 'x', iron_ingot, 'y', redstone);
        GameRegistry.addRecipe(new ItemStack(lift, 1, 0), "xyx", "zxz", "zzz", 'x', iron_ingot, 'y', redstone, 'z',
                new ItemStack(stone));
        GameRegistry.addRecipe(new ItemStack(lift, 1, 1), "xyx", "yxy", "xyx", 'x', iron_ingot, 'y', redstone);
        GameRegistry.addShapelessRecipe(TechCore.getInfoBook(), new ItemStack(ItemLinker.instance), Items.book);
    }
}
