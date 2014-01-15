package thut.world.common.items.tools;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thut.api.utils.IStampableBlock;
import thut.world.common.WorldCore;
import thut.world.common.blocks.fluids.BlockFluid;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class ItemStamper extends Item
{
	public static Item instance;
	public ItemStack stack;
	
	public ItemStamper(int par1) 
	{
		super(par1);
        this.setHasSubtypes(true);
		this.setUnlocalizedName("wallstamper");
		this.setCreativeTab(WorldCore.tabThut);
		instance = this;
	}
	
    public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World worldObj, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
    	
    	Block k = Block.blocksList[worldObj.getBlockId(x,y,z)];
    	if(k!=null)
    	{
    		System.out.println(y+" "+k.getUnlocalizedName()+" "
    	+OreDictionary.getOreName(OreDictionary.getOreID(new ItemStack(worldObj.getBlockId(x,y,z),1,worldObj.getBlockMetadata(x, y, z))))+" "
    				+k.getLocalizedName());
    	}
    	
    	if(player.isSneaking()&&worldObj!=null)
    	{
           	if(itemstack.stackTagCompound == null)
        	{
        		itemstack.setTagCompound(new NBTTagCompound() );
        	}
           	
           	for(ItemStack blocks: player.inventory.mainInventory)
           	{
	        	if(blocks==null)
	        	{
	        		return false;
	        	}
	        	
	        	int blockID = worldObj.getBlockId(x, y, z);
	        	int blockMeta = worldObj.getBlockMetadata(x, y, z);
	        	
	        	ItemStack block = new ItemStack(blockID, 1, blockMeta);
	        	Block b = Block.blocksList[blockID];
	        	
	        	if(b!=null&&(player.capabilities.isCreativeMode||b.renderAsNormalBlock()&&block.getItem().getUnlocalizedName().equals(blocks.getItem().getUnlocalizedName())))
	        	{
	        		
	        		if(!(b instanceof IStampableBlock))
	        		{
		        		itemstack.stackTagCompound.setInteger("blockID", blockID);
		        		itemstack.stackTagCompound.setInteger("blockMeta", blockMeta);
		        		itemstack.stackTagCompound.setInteger("side", side);
		        		if(!player.capabilities.isCreativeMode)
		        			player.inventory.consumeInventoryItem(blockID);
	        		}
	        		else
	        		{
	        			IStampableBlock b1 = (IStampableBlock)b;
		        		itemstack.stackTagCompound.setInteger("blockID", b1.sideIconBlockId(worldObj, x, y, z, side));
		        		itemstack.stackTagCompound.setInteger("blockMeta", b1.sideIconMetadata(worldObj, x, y, z, side));
		        		itemstack.stackTagCompound.setInteger("side", b1.sideIconSide(worldObj, x, y, z, side));
	        		}
	        		
	        		return true;
	        	}
           	}
    	}
    	
    	
       	if(itemstack.stackTagCompound == null)
    	{
    		itemstack.setTagCompound(new NBTTagCompound() );
    		itemstack.stackTagCompound.setInteger("blockID", 0);
    		itemstack.stackTagCompound.setInteger("blockMeta", -1);
    		itemstack.stackTagCompound.setInteger("side", -1);
    		return false;
    	}
       	
       	Block b = Block.blocksList[worldObj.getBlockId(x, y, z)];
       	if(b!=null&&b instanceof IStampableBlock
       			&&itemstack.stackTagCompound!=null)
       	{
       		int id = itemstack.stackTagCompound.getInteger("blockID");
       		int meta = itemstack.stackTagCompound.getInteger("blockMeta");
       		int storedSide = itemstack.stackTagCompound.getInteger("side");
       		
       		if(!(id==0||meta==-1||side==-1))
       		{
           		//Icon icon = Block.blocksList[id].getIcon(storedSide, meta);
           		((IStampableBlock)b).setBlockIcon(id, meta, side, worldObj, x, y, z, storedSide);
       			return true;
       		}
       	}
       	
       	return false;
       	
    }
    
	
	   /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    public String getUnlocalizedName(ItemStack stack)
    {
        return super.getUnlocalizedName();
    }
    
    
    
    /**
     * If this function returns true (or the item is damageable), the ItemStack's NBT tag will be sent to the client.
     */
    public boolean getShareTag()
    {
        return true;
    }
    
	@SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister)
    {
        this.itemIcon = par1IconRegister.registerIcon(WorldCore.TEXTURE_PATH+"wallstamper");
    }

    
}
