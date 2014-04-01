package thut.concrete.common.handlers;

import static net.minecraft.init.Items.iron_ingot;
import static thut.api.ThutItems.*;
import static thut.api.ThutBlocks.*;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import thut.api.ThutItems;
import thut.concrete.common.items.tools.ItemPaintBrush;

public class ItemHandler 
{
	private static List<Item> items = new ArrayList<Item>();

	private static final String[] dyeNames = { "dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyePurple", "dyeCyan", "dyeLightGray", "dyeGray", "dyePink", "dyeLime", "dyeYellow", "dyeLightBlue", "dyeMagenta", "dyeOrange", "dyeWhite" };

	public static void registerItems()
	{
		for(int i = 0; i<17;i++)
		{
			Item painter = new ItemPaintBrush(i);
			items.add(painter);
			ItemStack stack = new ItemStack(painter, 1,0);
			ThutItems.brushes[i] = stack;
		}
		
		for(Item item: items){
			GameRegistry.registerItem(item, item.getUnlocalizedName().substring(5));
		}
	}
	
	public static void registerStacks()
	{
		 cement = new ItemStack(ThutItems.dust.getItem(), 1, 4);
		 carbonate  = new ItemStack(ThutItems.dust.getItem(), 1, 1);
		 lime = new ItemStack(ThutItems.dust.getItem(), 1, 2);
		 boneMeal = new ItemStack(dye, 1, 15);
		 ThutItems.gravel = new ItemStack(Blocks.gravel);
		 ThutItems.sand = new ItemStack(Blocks.sand);
		 ThutItems.cobble = new ItemStack(Blocks.cobblestone);
		 ThutItems.water = new ItemStack(water_bucket);
		 
		solidLava0 = new ItemStack(solidLavas[0],1,0);
		solidLava1 = new ItemStack(solidLavas[1],1,0);
		solidLava2 = new ItemStack(solidLavas[2],1,0);
		
	//	concreteBucket = new ItemStack(ItemBucketConcrete.instance);
		trassOre = new ItemStack(worldGen,1,1);
		limestoneOre = new ItemStack(worldGen,1,2);
		chalkOre = new ItemStack(worldGen,1,0);
		twoRebar = new ItemStack(rebar,2,0);
		
		eightLiquidConcrete = new ItemStack(liquidConcrete, 8, 0);
		
		 registerOres();
	}
	
	public static void registerOres()
	{
		OreDictionary.registerOre("dustRock", ThutItems.dust);
		OreDictionary.registerOre("dustCaO", lime);
		OreDictionary.registerOre("dustCaCO3", carbonate);
		OreDictionary.registerOre("dustCalciumOxide",lime);
		OreDictionary.registerOre("dustCalciumCarbonate", carbonate);
		OreDictionary.registerOre("dustRockFine", trass);
		OreDictionary.registerOre("fertilizer", ThutItems.dust);
		OreDictionary.registerOre("fertilizer", trass);
		OreDictionary.registerOre("dustCement", cement);
		OreDictionary.registerOre("rebar", new ItemStack(rebar,1,0));
		OreDictionary.registerOre("oreChalk",new ItemStack(worldGen,1,0));
		OreDictionary.registerOre("oreTrass",new ItemStack(worldGen,1,1));
		OreDictionary.registerOre("oreLimestone",new ItemStack(worldGen,1,2));
	}
}
