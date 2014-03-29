package thut.core.common.items;

import java.util.HashMap;
import java.util.List;

import thut.api.ThutBlocks;
import thut.core.common.ThutCore;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

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
		this.setHasSubtypes(true);
		this.setUnlocalizedName("dust");
        this.setMaxDamage(0);
        this.setCreativeTab(ThutCore.tabThut);
	}

    @SideOnly(Side.CLIENT)

    /**
     * Gets an icon index based on an item's damage value
     */
    @Override
    public IIcon getIconFromDamage(int par1)
    {
    	if(dusts.get(par1)!=null)
    		return dusts.get(par1).icon;
    	return null;
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
    	return dust!=null?dust.name:super.getUnlocalizedName(stack);
    }

    @Override
	@SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IIconRegister)
    {
		for(int i = 0; i<lastDust;i++)
		{
			dusts.get(i).icon = par1IIconRegister.registerIcon(dusts.get(i).modid+":"+dusts.get(i).name);
		}
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
    
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
    	if(!world.isRemote)
    	{
    		int i = stack.getItemDamage();
    		Dust dust = dusts.get(i);
    		if(dust!=null)
    			return dust.onItemUseFirst(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
    	}
    	return false;
    }
    
    public static class Dust
    {
    	public final String name;
    	public final String modid;
    	public IIcon icon;
    	public Dust(String name, String modid)
    	{
    		this.name = name;
    		this.modid = modid;
    	}
    	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    	{
    		return false;
    	}
    }
}
