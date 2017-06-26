package thut.tech.common.handlers;

import static thut.api.ThutBlocks.lift;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import thut.tech.Reference;
import thut.tech.common.items.ItemLinker;

public class ItemHandler
{
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        new ItemLinker();
        ItemLinker.instance.setRegistryName(Reference.MOD_ID, "devicelinker");
        ItemLinker.instance.setUnlocalizedName("devicelinker");
        event.getRegistry().register(ItemLinker.instance);
        ItemBlock i = new BlockHandler.ItemLiftBlock(lift);
        i.setRegistryName(lift.getRegistryName());
        event.getRegistry().register(i);
    }

    public static void registerRecipes()
    {// TODO 1.12 recipes
     // GameRegistry.addRecipe(new ItemStack(ItemLinker.instance), "xyx", " x
     // ", " ", 'x', Items.IRON_INGOT, 'y',
     // Items.REDSTONE);
     // GameRegistry.addRecipe(new ItemStack(lift, 1, 0), "xyx", "zxz",
     // "zzz", 'x', Items.IRON_INGOT, 'y',
     // Items.REDSTONE, 'z', new ItemStack(Blocks.STONE));
     // GameRegistry.addRecipe(new ItemStack(lift, 1, 1), "xyx", "yxy",
     // "xyx", 'x', Items.IRON_INGOT, 'y',
     // Items.REDSTONE);
     // GameRegistry.addShapelessRecipe(TechCore.getInfoBook(), new
     // ItemStack(ItemLinker.instance), Items.BOOK);
        ItemLinker.liftblocks = new ItemStack(lift, 1, 0);
    }
}
