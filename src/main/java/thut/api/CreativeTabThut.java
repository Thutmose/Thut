package thut.api;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CreativeTabThut extends CreativeTabs{

	public static CreativeTabThut tabThut = new CreativeTabThut();;
	
	public CreativeTabThut() {
		super("tabConcrete");
	}

	@SideOnly(Side.CLIENT)
    public String getTabLabel(){
        return "Concrete";
    }

	@SideOnly(Side.CLIENT)
    public String getTranslatedTabLabel(){
        return this.getTabLabel();
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
