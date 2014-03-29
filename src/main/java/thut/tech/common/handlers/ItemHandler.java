package thut.tech.common.handlers;

import static net.minecraft.init.Items.iron_ingot;
import static thut.api.ThutItems.*;
import static thut.api.ThutBlocks.*;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import thut.api.ThutItems;
import thut.tech.common.items.ItemLinker;
import thut.tech.common.items.ItemSpout;
import thut.tech.common.items.ItemTank;

public class ItemHandler 
{
	private static List<Item> items = new ArrayList<Item>();
	
	public static void registerItems()
	{
		items.add(new ItemLinker());
		items.add(new ItemSpout());
		items.add(new ItemTank());
		
		for(Item item: items){
			GameRegistry.registerItem(item, item.getUnlocalizedName().substring(5));
		}
	}
	
	public static void registerRecipes()
	{
		for (ItemStack item : OreDictionary.getOres("rebar")) 
		{
			GameRegistry.addShapelessRecipe(new ItemStack(liftRail,1,0), item, new ItemStack(redstone));
		}
		
		GameRegistry.addRecipe(new ItemStack(ItemLinker.instance),"xyx"," x ","   ", 'x', iron_ingot, 'y', redstone);
		GameRegistry.addRecipe(new ItemStack(lift,1,0),"xyx","zxz","zzz", 'x', iron_ingot, 'y', redstone, 'z', new ItemStack(stone));
		GameRegistry.addRecipe(new ItemStack(lift,1,1),"xyx","yxy","xyx", 'x', iron_ingot, 'y', redstone);
		
		GameRegistry.addRecipe(new ItemStack(liftRail,4),"xy ","yxy"," yx", 'x', iron_ingot, 'y', redstone);
		
	}
}
