package thut.core.common;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thut.api.ThutBlocks;

public class CreativeTabThut extends CreativeTabs{

	public static CreativeTabThut tabThut = new CreativeTabThut();;
	
	public CreativeTabThut() {
		super("tabConcrete");
	}

	@Override
	public ItemStack getTabIconItem() {

		if(ThutBlocks.solidLavas[0]==null)
			return new ItemStack(Item.getItemFromBlock(Blocks.STONE));
		
		if(ThutBlocks.rebar!=null)
			return new ItemStack(Item.getItemFromBlock(ThutBlocks.rebar));
		
		return new ItemStack(Item.getItemFromBlock(ThutBlocks.solidLavas[0]));
	}

}
