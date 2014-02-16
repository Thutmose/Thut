package thut.api.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemBlockMeta extends ItemBlock {

	Block b;
	
	public ItemBlockMeta(Block p_i45328_1_) {
		super(p_i45328_1_);
		b = p_i45328_1_;
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
	}
    /**
     * Gets an icon index based on an item's damage value
     */
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int par1)
    {
        return this.b.getIcon(2, par1);
    }
    
    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    public String getUnlocalizedName(ItemStack par1ItemStack)
    {
    	if(b instanceof IMetaBlock)
    	{
    		return ((IMetaBlock)b).getUnlocalizedName(par1ItemStack);
    	}
    	
        return this.b.getUnlocalizedName();
    }
    /**
     * Returns the metadata of the block which this Item (ItemBlock) can place
     */
    public int getMetadata(int par1)
    {
        return par1;
    }
	
}
