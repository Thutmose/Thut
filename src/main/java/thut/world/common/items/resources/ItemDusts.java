package thut.world.common.items.resources;

import java.util.List;

import thut.api.ThutBlocks;
import thut.world.common.WorldCore;
import thut.world.common.blocks.fluids.dusts.BlockDust;
import thut.world.common.blocks.fluids.dusts.BlockDustInactive;
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
	public IIcon[] icons;
	public static String[] types =
	{
		"dust",
		"dustCaCO3",
		"dustCaO",
		"dustTrass",
		"dustCement",
		"dustSulfur",
	};

	public ItemDusts() {
		super();
		this.setHasSubtypes(true);
		this.setUnlocalizedName("dust");
        this.setMaxDamage(0);
        this.setCreativeTab(WorldCore.tabThut);
	}

    @SideOnly(Side.CLIENT)

    /**
     * Gets an icon index based on an item's damage value
     */
    @Override
    public IIcon getIconFromDamage(int par1)
    {
    	if(par1<icons.length)
    		return icons[par1];
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
    	return super.getUnlocalizedName() + "." +types[i];
    }

    @Override
	@SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IIconRegister)
    {
		icons = new IIcon[types.length];
		for(int i = 0; i<icons.length;i++)
		{
			icons[i] = par1IIconRegister.registerIcon(WorldCore.TEXTURE_PATH+types[i]);
		}
    }
    @SideOnly(Side.CLIENT)
    @Override
    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (int j = 0; j < icons.length; ++j)
        {
            par3List.add(new ItemStack(par1, 1, j));
        }
    }
    
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
    	if(!world.isRemote&&stack.getItemDamage()==0)
    	{
    		int x1 = ForgeDirection.getOrientation(side).offsetX+x,y1 = ForgeDirection.getOrientation(side).offsetY+y, z1 = ForgeDirection.getOrientation(side).offsetZ+z;
    		int meta = world.getBlockMetadata(x1, y1, z1);
    		Block block = world.getBlock(x1, y1, z1);
    		
            if (player.isSneaking()&&ItemDye.applyBonemeal(stack, world, x, y, z, player))
            {
                if (!world.isRemote)
                {
                    world.playAuxSFX(2005, x, y, z, 0);
                }

                return true;
            }
    		
    		if(block instanceof BlockDust||block instanceof BlockDustInactive&&meta!=15)
    		{
    			world.setBlockMetadataWithNotify(x1, y1, z1, meta+1, 3);
    			System.out.println("case 1");
    			if(!player.capabilities.isCreativeMode)
    				stack.splitStack(1);
                return true;
    		}
    		else if(world.getBlock(x1, y1, z1) instanceof BlockDust||world.getBlock(x1, y1, z1) instanceof BlockDustInactive&&meta!=15)
    		{
    			world.setBlockMetadataWithNotify(x1, y1, z1, meta+1, 3);

    			System.out.println("case 2"+" "+meta);
    			if(!player.capabilities.isCreativeMode)
    				stack.splitStack(1);
                return true;
    		}
    		else if (block==Blocks.air||block.getMaterial().isReplaceable())
    		{
    			world.setBlock(x1, y1, z1, ThutBlocks.dust, Math.min(15, stack.stackSize), 3);
    			if(!player.capabilities.isCreativeMode)
    				stack.splitStack(Math.min(stack.stackSize, 16));
                return true;
    		}
    	}
    	return false;
    }
}
