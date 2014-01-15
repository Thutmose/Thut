package thut.world.common.items.tools;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import thut.world.common.WorldCore;
import thut.world.common.blocks.fluids.BlockFluid;
import thut.world.common.corehandlers.ItemHandler;

public class ItemPaintBrush extends Item {

	public static final int MAX_USES = 128;
	public final int colour;
	public static int emptyBrushID;
	public ItemPaintBrush(int par1, int colour) 
	{
		super(par1);
		this.maxStackSize = 1;
		this.colour = colour;
		this.setMaxDamage(colour<16?MAX_USES:0);
        this.setHasSubtypes(true);
		this.setCreativeTab(WorldCore.tabThut);
		this.setUnlocalizedName("paintBrush"+colour);
	}
	
	@Override
	public int getMetadata (int damageValue) {
		return colour;
	}
	
	public int getPaintRemaining(int damageValue)
	{
		return damageValue&(MAX_USES-1);
	}
	
	   public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	    {
	        if (!par2EntityPlayer.canPlayerEdit(x, y, z, side, par1ItemStack))
	        {
	            return false;
	        }
	   //     System.out.println(par1ItemStack.getItem().getUnlocalizedName());
	        if(par1ItemStack.getItem().getUnlocalizedName().contains("paintBrush16"))
	        {
	        	return false;
	        }
	        
	        int damageValue = par1ItemStack.getItemDamage();
	        int metaData = 15-par1ItemStack.getItem().getMetadata(damageValue);

	        Block block = Block.blocksList[par3World.getBlockId(x,y,z)];
	    //    System.out.println(metaData+" "+damageValue);
	        boolean recoloured = false;
	        
	        if(damageValue != MAX_USES)
	        {
	        	recoloured = block.recolourBlock(par3World,x,y,z,ForgeDirection.getOrientation(side),metaData);
	        }
	        if(recoloured)
	        {
	        	par1ItemStack.damageItem(1, par2EntityPlayer);
	        	damageValue = par1ItemStack.getItemDamage();
	        }

        	if(damageValue>= MAX_USES)
        	{
        		par2EntityPlayer.inventory.mainInventory[par2EntityPlayer.inventory.currentItem] = ItemHandler.brushes[16].copy();
        		par2EntityPlayer.inventory.mainInventory[par2EntityPlayer.inventory.currentItem].setItemDamage(0);
        	}
	        return recoloured;
	        //*/
	    }
	   
	   
	    
		@SideOnly(Side.CLIENT)
	    public void registerIcons(IconRegister par1IconRegister)
	    {
	        this.itemIcon = par1IconRegister.registerIcon(WorldCore.TEXTURE_PATH+"paintBrush"+colour);
	    }
	
}
