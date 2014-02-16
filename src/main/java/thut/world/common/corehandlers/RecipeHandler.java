package thut.world.common.corehandlers;

import static thut.api.ThutBlocks.*;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import thut.api.ThutBlocks;
import thut.api.ThutItems;
import thut.world.common.blocks.fluids.liquids.BlockLava;
import thut.world.common.blocks.fluids.solids.BlockSolidLava;
import thut.world.common.blocks.world.BlockWorldGen;
import cpw.mods.fml.common.registry.GameRegistry;

public class RecipeHandler
{
	
	// Empty fields for holding items
	public static Item[] items = ItemHandler.items;
	public static List<Item> itemList = ItemHandler.itemList;
	
	public static ItemStack[] brushes = ItemHandler.brushes;

	private static final String[] dyeNames = { "dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyePurple", "dyeCyan", "dyeLightGray", "dyeGray", "dyePink", "dyeLime", "dyeYellow", "dyeLightBlue", "dyeMagenta", "dyeOrange", "dyeWhite" };

	public RecipeHandler(ConfigHandler config) {
		registerOres();
	}
	
	public void registerRecipes()
	{
		registerSpecialRecipes();
		registerShapedRecipes();
		registerShapeless();
	}
	
	public void registerSpecialRecipes()
	{
		boolean dust = false;
	}

	public void registerShapedRecipes()
	{


	}
	
	public void registerOres()
	{

	}
	
	
	public void registerShapeless()
	{
		
	}
	
}
