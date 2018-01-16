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
import net.minecraftforge.registries.GameData;
import thut.tech.Reference;
import thut.tech.common.TechCore;
import thut.tech.common.items.ItemLinker;
import thut.tech.common.items.RecipeReset;

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
    {
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
                Ingredient.fromItem(ItemLinker.instance), Ingredient.fromItem(Items.BOOK));
        ItemLinker.liftblocks = new ItemStack(lift, 1, 0);
        GameData.register_impl(
                new RecipeReset().setRegistryName(new ResourceLocation(Reference.MOD_ID, "linker_reset")));

    }
}
