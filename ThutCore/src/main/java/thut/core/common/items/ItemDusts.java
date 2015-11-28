package thut.core.common.items;

import java.util.HashMap;
import java.util.List;

import thut.api.ThutBlocks;
import thut.api.ThutItems;
import thut.core.common.ThutCore;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemDusts extends Item
{
	public static HashMap<Integer, Dust> dusts = new HashMap<Integer, Dust>();
	private static int lastDust = 0;
	
	public static void addDust(Dust dust)
	{
		dusts.put(lastDust++, dust);
	}

	public ItemDusts() {
		super();
		ThutItems.dusts = this;
		
		this.setHasSubtypes(true);
		this.setUnlocalizedName("dusts");
        this.setMaxDamage(0);
        this.setCreativeTab(ThutCore.tabThut);
	}

    
    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
    	int i = stack.getItemDamage();
    	Dust dust = dusts.get(i);
    	return dust!=null?"item."+dust.name:super.getUnlocalizedName(stack);
    }

    @SideOnly(Side.CLIENT)
    @Override
    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (int j = 0; j < lastDust; ++j)
        {
            par3List.add(new ItemStack(par1, 1, j));
        }
    }
    
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
    	if(!world.isRemote)
    	{
    		int i = stack.getItemDamage();
    		Dust dust = dusts.get(i);
    		if(dust!=null)
    			return dust.onItemUseFirst(stack, player, world, pos, side, hitX, hitY, hitZ);
    	}
    	return false;
    }
    
    public static class Dust
    {
    	public final String name;
    	public final String modid;
    	public Dust(String name, String modid)
    	{
    		this.name = name;
    		this.modid = modid;
    	}
        public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
        {
    		return false;
    	}
    }
}
