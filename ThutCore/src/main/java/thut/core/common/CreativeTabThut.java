package thut.core.common;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thut.api.ThutBlocks;

public class CreativeTabThut extends CreativeTabs
{

    public static CreativeTabThut tabThut = new CreativeTabThut();;
    public static ItemStack       tabStack;

    public CreativeTabThut()
    {
        super("tabThut");
    }

    @Override
    public ItemStack getTabIconItem()
    {
        if (tabStack == null)
        {
            if (ThutBlocks.solidLavas[0] != null)
                tabStack = new ItemStack(Item.getItemFromBlock(ThutBlocks.solidLavas[0]));
            else if (ThutBlocks.rebar != null) tabStack = new ItemStack(Item.getItemFromBlock(ThutBlocks.rebar));
            else tabStack = new ItemStack(Item.getItemFromBlock(Blocks.STONE));
        }
        return tabStack;
    }

}
