package thut.core.common;

import thut.api.ThutBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CreativeTabThut extends CreativeTabs{

	public static CreativeTabThut tabThut = new CreativeTabThut();;
	
	public CreativeTabThut() {
		super("tabConcrete");
	}

	@Override
	public Item getTabIconItem() {

		if(ThutBlocks.solidLavas[0]==null)
			return Item.getItemFromBlock(Blocks.stone);
		
		if(ThutBlocks.rebar!=null)
			return Item.getItemFromBlock(ThutBlocks.rebar);
		
		return Item.getItemFromBlock(ThutBlocks.solidLavas[0]);
	}

}
