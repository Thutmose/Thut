package thut.world.common.items.resources;

import java.util.List;

import thut.api.Blocks;
import thut.world.common.WorldCore;
import thut.world.common.blocks.crystals.BlockSulfur;
import thut.world.common.blocks.fluids.dusts.BlockDust;
import thut.world.common.blocks.fluids.dusts.BlockDustInactive;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class ItemDusts extends Item
{
	public Icon[] icons;
	public static String[] types =
	{
		"dust",
		"dustCaCO3",
		"dustCaO",
		"dustTrass",
		"dustCement",
		"dustSulfur",
	};

	public ItemDusts(int par1) {
		super(par1);
		this.setHasSubtypes(true);
		this.setUnlocalizedName("dust");
        this.setMaxDamage(0);
        this.setCreativeTab(WorldCore.tabThut);
	}

    @SideOnly(Side.CLIENT)

    /**
     * Gets an icon index based on an item's damage value
     */
    public Icon getIconFromDamage(int par1)
    {
    	if(par1<icons.length)
    		return icons[par1];
    	return null;
    }
    
    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    public String getUnlocalizedName(ItemStack stack)
    {
    	int i = stack.getItemDamage();
    	return super.getUnlocalizedName() + "." +types[i];
    }
    
	@SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister)
    {
		icons = new Icon[types.length];
		for(int i = 0; i<icons.length;i++)
		{
			icons[i] = par1IconRegister.registerIcon(WorldCore.TEXTURE_PATH+types[i]);
		}
    }
    @SideOnly(Side.CLIENT)

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (int j = 0; j < icons.length; ++j)
        {
            par3List.add(new ItemStack(par1, 1, j));
        }
    }
    
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
    	if(!world.isRemote&&stack.getItemDamage()==5)
    	{
    		int x1 = ForgeDirection.getOrientation(side).offsetX+x,y1 = ForgeDirection.getOrientation(side).offsetY+y, z1 = ForgeDirection.getOrientation(side).offsetZ+z;
    		int id = world.getBlockId(x1, y1, z1);
    		int meta = world.getBlockMetadata(x1, y1, z1);
    		Block block = Block.blocksList[id];
    		
    	//	System.out.println("side "+id);
    		if(block instanceof BlockSulfur)
    		{

    		}
    		if(Block.blocksList[world.getBlockId(x, y, z)] instanceof BlockSulfur)
    			return false;
    		else if (id==0||world.getBlockMaterial(x1, y1, z1).isReplaceable())
    		{
    			world.setBlock(x1, y1, z1, Blocks.sulfur.blockID, ForgeDirection.getOrientation(side).getOpposite().ordinal(), 3);
    			if(!player.capabilities.isCreativeMode)
    				stack.splitStack(1);
    			return true;
    		}
    	}
    	if(!world.isRemote&&stack.getItemDamage()==0)
    	{
    		int x1 = ForgeDirection.getOrientation(side).offsetX+x,y1 = ForgeDirection.getOrientation(side).offsetY+y, z1 = ForgeDirection.getOrientation(side).offsetZ+z;
    		int id = world.getBlockId(x1, y1, z1);
    		int meta = world.getBlockMetadata(x1, y1, z1);
    		Block block = Block.blocksList[id];
    		
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
    		else if(Block.blocksList[world.getBlockId(x1, y1, z1)] instanceof BlockDust||Block.blocksList[world.getBlockId(x1, y1, z1)] instanceof BlockDustInactive&&meta!=15)
    		{
    			world.setBlockMetadataWithNotify(x1, y1, z1, meta+1, 3);

    			System.out.println("case 2"+" "+meta);
    			if(!player.capabilities.isCreativeMode)
    				stack.splitStack(1);
                return true;
    		}
    		else if (id==0||world.getBlockMaterial(x1, y1, z1).isReplaceable())
    		{
    			world.setBlock(x1, y1, z1, Blocks.dust.blockID, Math.min(15, stack.stackSize), 3);
    			if(!player.capabilities.isCreativeMode)
    				stack.splitStack(Math.min(stack.stackSize, 16));
                return true;
    		}
    	}
    	return false;
    }
}
