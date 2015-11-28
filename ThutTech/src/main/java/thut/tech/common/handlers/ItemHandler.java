package thut.tech.common.handlers;

import static net.minecraft.init.Items.iron_ingot;
import static thut.api.ThutItems.*;
import static thut.api.ThutBlocks.*;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import thut.api.ThutItems;
import thut.tech.common.items.ItemLinker;

public class ItemHandler 
{
	private static List<Item> items = new ArrayList<Item>();
	
	public static void registerItems()
	{
		new ItemLinker();
		GameRegistry.registerItem(ItemLinker.instance, "devicelinker");
	}
	
	public static void registerRecipes()
	{
		GameRegistry.addRecipe(new ItemStack(ItemLinker.instance),"xyx"," x ","   ", 'x', iron_ingot, 'y', redstone);
		GameRegistry.addRecipe(new ItemStack(lift,1,0),"xyx","zxz","zzz", 'x', iron_ingot, 'y', redstone, 'z', new ItemStack(stone));
		GameRegistry.addRecipe(new ItemStack(lift,1,1),"xyx","yxy","xyx", 'x', iron_ingot, 'y', redstone);
		
//		GameRegistry.addRecipe(new ItemStack(liftRail,4),"xy ","yxy"," yx", 'x', iron_ingot, 'y', redstone);
		
	}
}
