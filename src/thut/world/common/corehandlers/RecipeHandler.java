package thut.world.common.corehandlers;

import static thut.api.Blocks.*;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import thut.api.Blocks;
import thut.api.Items;
import thut.world.common.blocks.crystals.BlockSulfur;
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
		initCookable();
	}
	
	public void registerSpecialRecipes()
	{
		boolean dust = false;
		for(ItemStack sulfur:OreDictionary.getOres("dustSulfur"))
		{
			if(sulfur!=null&&!sulfur.isItemEqual(((BlockSulfur)Blocks.sulfur).drop))
			{
				((BlockSulfur)Blocks.sulfur).drop = sulfur.copy().splitStack(1);
				dust = true;
				break;
			}
		}
		if(!dust)
			for(ItemStack sulfur:OreDictionary.getOres("oreSulfur"))
			{
				if(sulfur!=null&&!sulfur.isItemEqual(((BlockSulfur)Blocks.sulfur).drop))
				{
					((BlockSulfur)Blocks.sulfur).drop = sulfur.copy().splitStack(1);
					break;
				}
			}
	}

	public void initCookable()
	{
		Items.cookable.add(Items.chalkOre);
		Items.cookable.add(Items.limestoneOre);
		Items.cookable.add(Items.boneMeal);
		
		for(Item item : Item.itemsList)
		{
			if(item!=null&&item.getUnlocalizedName().toLowerCase().contains("bone"))
			{
				Items.cookable.add(new ItemStack(item.itemID,1,0));
			}
		}
		
		for(Item item : Item.itemsList)
		{
			if(item!=null&&item.getUnlocalizedName().toLowerCase().contains("limestone"))
			{
				Items.cookable.add(new ItemStack(item.itemID,1,0));
			}
		}
		System.out.println(Items.cookable.toString());
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
