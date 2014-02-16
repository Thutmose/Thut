package thut.world.common.items.blocks;

import static thut.world.common.blocks.world.BlockWorldGen.names;

import java.util.List;

import thut.world.common.WorldCore;
import thut.world.common.blocks.world.BlockWorldGen;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemWorldGenBlock extends ItemBlock{
	
	public ItemWorldGenBlock(Block par1)
    {
        super(par1);
    //    this.setMaxDamage(0);
        this.setHasSubtypes(true);
		this.setUnlocalizedName("Block");
		this.setCreativeTab(WorldCore.tabThut);
	
    }
	
	@Override
	public int getMetadata (int damageValue) {
		return damageValue;
	}
	
	   /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    @Override
    public String getUnlocalizedName(ItemStack par1ItemStack)
    {
        int i = par1ItemStack.getItemDamage()%names.length;
        return super.getUnlocalizedName() + "." + names[i];
    }

    @Override
    @SideOnly(Side.CLIENT)

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (int j = 0; j < names.length; ++j)
        {
            par3List.add(new ItemStack(par1, 1, j));
        }
    }

	
}
