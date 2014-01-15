package thut.world.common;

import thut.api.Blocks;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;

public class CreativeTabWorldgen extends CreativeTabs{

	public CreativeTabWorldgen() {
		super("tabConcrete");
	}

	@SideOnly(Side.CLIENT)
	public int getTabIconItemIndex(){
        return Blocks.solidLavas[0].blockID;
    }

	@SideOnly(Side.CLIENT)
    public String getTabLabel(){
        return "Concrete";
    }

	@SideOnly(Side.CLIENT)
    public String getTranslatedTabLabel(){
        return this.getTabLabel();
    }
}
