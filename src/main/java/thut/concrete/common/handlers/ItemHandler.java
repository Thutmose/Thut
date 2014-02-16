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
	
	public static void registerRecipes()
	{
		ThutItems.cookable.add(chalkOre);
		ThutItems.cookable.add(limestoneOre);
		ThutItems.cookable.add(carbonate);
		ThutItems.cookable.add(boneMeal);
		
		for(Item item : ThutItems.getItems())
		{
			if(item!=null&&item.getUnlocalizedName().toLowerCase().contains("bone"))
			{
				cookable.add(new ItemStack(item,1,0));
			}
		}
		
		for(Item item : getItems())
		{
			if(item!=null&&item.getUnlocalizedName().toLowerCase().contains("limestone"))
			{
				cookable.add(new ItemStack(item,1,0));
			}
		}
		
		for(int i = 0; i<16; i++)
		{
			GameRegistry.addRecipe(brushes[16].copy()," x "," y "," z ", 'x',new ItemStack(wool,1,i), 'y', iron_ingot, 'z', stick);
		}
		
		GameRegistry.addRecipe(new ItemStack(rebar,4),"x  "," x ","  x", 'x', iron_ingot);
		
		for (ItemStack steel : OreDictionary.getOres("ingotSteel")) 
		{
			GameRegistry.addRecipe(new ItemStack(rebar,16),"x  "," x ","  x", 'x', steel);
		}
		
		for (ItemStack steel : OreDictionary.getOres("ingotRefinedIron")) 
		{
			GameRegistry.addRecipe(new ItemStack(rebar,5),"x  "," x ","  x", 'x', steel);
		}
		
		GameRegistry.addRecipe(new ItemStack(limekiln,1,0),"xxx","x x","yyy", 'x', new ItemStack(brick_block,1), 'y', new ItemStack(stone,1));
		
		GameRegistry.addShapelessRecipe(eightLiquidConcrete,cement, ThutItems.gravel, ThutItems.gravel, ThutItems.gravel, ThutItems.gravel, ThutItems.sand, ThutItems.sand, ThutItems.sand, new ItemStack(water_bucket));

		GameRegistry.addShapelessRecipe(cement, lime, trass);
		GameRegistry.addShapelessRecipe(cement, lime, ThutItems.dust, ThutItems.dust, ThutItems.dust, ThutItems.dust, ThutItems.dust, ThutItems.dust, ThutItems.dust, ThutItems.dust);
		
		GameRegistry.addShapelessRecipe(trass, trassOre);
		GameRegistry.addShapelessRecipe(ThutItems.dust, solidLava0);
		GameRegistry.addShapelessRecipe(ThutItems.dust, solidLava1);
		GameRegistry.addShapelessRecipe(ThutItems.dust, solidLava2);
		
		for (ItemStack item : OreDictionary.getOres("rebar")) 
		{
			GameRegistry.addShapelessRecipe(new ItemStack(rebar,1,0), item);
		}
		
		for(Item i : getItems())
		{
			if(i!=null&&i!=twoRebar.getItem()&&i.getUnlocalizedName().toLowerCase().contains("rebar"))
			{
				GameRegistry.addShapelessRecipe(new ItemStack(rebar,1,0), new ItemStack(i,1,0));
			}
		}
		for(int i = 0; i<16; i++)
		{
			for (ItemStack dye : OreDictionary.getOres(dyeNames[i])) 
			{
				for(ItemStack brush : brushes)
				{
					GameRegistry.addShapelessRecipe(brushes[i].copy(), brush.copy(), dye.copy() );
				}
			}
		}
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
