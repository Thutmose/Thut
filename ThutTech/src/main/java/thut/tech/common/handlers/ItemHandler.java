package thut.tech.common.handlers;

import static thut.api.ThutBlocks.lift;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import thut.tech.Reference;
import thut.tech.common.TechCore;
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
        GameRegistry.addShapedRecipe(new ResourceLocation(Reference.MOD_ID, "linker"),
                new ResourceLocation(Reference.MOD_ID, "thuttech"), new ItemStack(ItemLinker.instance), "xyx", " x ",
                "   ", 'x', Items.IRON_INGOT, 'y', Items.REDSTONE);
        GameRegistry.addShapedRecipe(new ResourceLocation(Reference.MOD_ID, "lift"),
                new ResourceLocation(Reference.MOD_ID, "thuttech"), new ItemStack(lift, 1, 0), "xyx", "zxz", "zzz", 'x',
                Items.IRON_INGOT, 'y', Items.REDSTONE, 'z', new ItemStack(Blocks.STONE));
        GameRegistry.addShapedRecipe(new ResourceLocation(Reference.MOD_ID, "controller"),
                new ResourceLocation(Reference.MOD_ID, "thuttech"), new ItemStack(lift, 1, 1), "xyx", "yxy", "xyx", 'x',
                Items.IRON_INGOT, 'y', Items.REDSTONE);
        GameRegistry.addShapelessRecipe(new ResourceLocation(Reference.MOD_ID, "book"),
                new ResourceLocation(Reference.MOD_ID, "thuttech"), TechCore.getInfoBook(),
                Ingredient.fromItems(ItemLinker.instance, Items.BOOK));
        ItemLinker.liftblocks = new ItemStack(lift, 1, 0);
    }
}
