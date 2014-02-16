package thut.world.common.corehandlers;

import static thut.api.ThutItems.*;

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
import thut.api.ThutBlocks;
import thut.api.ThutItems;
import thut.world.common.blocks.fluids.solids.BlockSolidLava;
import thut.world.common.blocks.world.BlockWorldGen;
import thut.world.common.items.*;
import thut.world.common.items.resources.ItemDusts;
import thut.world.common.items.tools.ItemGrinder;

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
		Item grinderItem = new ItemGrinder();
		Item dusts = new ItemDusts();
		dust = new ItemStack(dusts);
		trass = new ItemStack(dusts, 1, 3);
		itemList.add(dusts);
		itemList.add(grinderItem);
		registerItems();
	}
	
	public void registerItems(){
		for(Item item: itemList){
			GameRegistry.registerItem(item, item.getUnlocalizedName().substring(5));
		}
	}
	
}
