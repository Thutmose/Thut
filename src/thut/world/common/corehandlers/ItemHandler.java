package thut.world.common.corehandlers;

import static thut.api.Items.*;

//import static thutconcrete.common.blocks.Blocks.*;
import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.oredict.OreDictionary;
import thut.api.Blocks;
import thut.api.Items;
import thut.world.common.blocks.fluids.solids.BlockSolidLava;
import thut.world.common.blocks.world.BlockWorldGen;
import thut.world.common.items.*;
import thut.world.common.items.resources.ItemDusts;
import thut.world.common.items.tools.ItemGrinder;
import thut.world.common.items.tools.ItemPaintBrush;
import thut.world.common.items.tools.ItemStamper;

public class ItemHandler {

	// For shorter referencing to the config handler
	private ConfigHandler config;

	// Empty fields for holding items
	public static Item[] items;
	public static List<Item> itemList = new ArrayList<Item>();
	
	public static ItemStack[] brushes = new ItemStack[17];
	
	public ItemHandler(ConfigHandler handler){
		config = handler;
		// Initalizes all mod items
		initItems();
	}

	public void initItems(){
		int id = config.IDItem;
		Item grinderItem = new ItemGrinder(id++);
//		itemList.add(grinderItem);
//		Item stamperItem = new ItemStamper(id++);
//		itemList.add(stamperItem);
		Item dusts = new ItemDusts(id++);
		itemList.add(dusts);
//		items = itemList.toArray(new Item[0]);

		registerItems();
		
//		for(int i = 0; i<17;i++)
//		{
//			Item painter = new ItemPaintBrush(id++, i);
//			ItemStack stack = new ItemStack(painter, 1,0);
//			brushes[i] = stack;
//		}
		ItemPaintBrush.emptyBrushID = id;
		
		
		Items.items = ItemHandler.items;
		Items.itemList = ItemHandler.itemList;
	//	Items.brushes = ItemHandler.brushes;
		
//		 trass = new ItemStack(dusts, 1, 3);
//		 dust = new ItemStack(dusts, 1, 0);
//		 sulfur  = new ItemStack(dusts, 1, 5);
//		 grinder = new ItemStack(grinderItem,1);
//	
//		 boneMeal = new ItemStack(Item.dyePowder,1,15);
//		
//		 sand = new ItemStack(Block.sand);
//		 gravel = new ItemStack(Block.gravel);
//		 cobble = new ItemStack(Block.cobblestone);
//		 water = new ItemStack(Item.bucketWater);
//		
//		 trassOre = new ItemStack(Blocks.worldGen,1,1);
//		 limestoneOre = new ItemStack(Blocks.worldGen,1,2);
//		 chalkOre = new ItemStack(Blocks.worldGen,1,0);
//	
//		 solidLava0 = new ItemStack(Blocks.getSolidLava(0),1,0);
//		 solidLava1 = new ItemStack(Blocks.getSolidLava(1),1,0);
//		 solidLava2 = new ItemStack(Blocks.getSolidLava(2),1,0);
//	
//		 stamper = new ItemStack(stamperItem);
		
		
	}
	
	public void registerItems(){
//		for(Item item: items){
//			GameRegistry.registerItem(item, item.getUnlocalizedName().substring(5));
//		}
	}
	
}
