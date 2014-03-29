package thut.tech.common.items;

import java.util.ArrayList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thut.api.maths.Vector3;
import thut.core.common.blocks.BlockFluid;
import thut.tech.common.TechCore;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class ItemSpout extends Item {

	public ItemSpout() {
		super();
        this.setHasSubtypes(true);
		this.setUnlocalizedName("spout");
		this.setCreativeTab(TechCore.tabThut);
	}
	
    public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World worldObj, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
    	boolean ret = false;
    	int toDrain = 0;
    	ForgeDirection face = ForgeDirection.values()[side];
    	Vector3 hit = new Vector3(x, y, z);
    	Vector3 next = hit.offset(face);
    	
    	boolean full = !player.isSneaking();
		ArrayList<ItemStack> tanks = getTanks(player);
		if(tanks.size()==0) return ret;
		
    	if(hit.getBlock(worldObj) instanceof BlockFluid && hit.getBlockMetadata(worldObj) != 15)
    	{
    		Fluid f = ((BlockFluid)hit.getBlock(worldObj)).getFluid();
    		int meta = hit.getBlockMetadata(worldObj);
    		if(meta!=15)
	    		for(ItemStack stack : tanks)
	    		{
	    			ItemTank tank = (ItemTank) stack.getItem();
	    			Fluid f1 = tank.getFluid(stack).getFluid();
	    			if(f.getUnlocalizedName().equals(f1.getUnlocalizedName()))
	    			{
	    				int inTank = tank.getAmount(stack);
	    				float factor = 62.5f;
	    				int metaDiff = full?15-meta:1;
	    				toDrain = (int) (factor * metaDiff);
	    				FluidStack s = tank.drain(stack, toDrain, true);
	    				hit.setBlock(worldObj, hit.getBlock(worldObj), (int) (s.amount/62.5 + meta));
	    				break;
	    			}
	    		}
    	}
    	else if(next.getBlockMaterial(worldObj).isReplaceable())
    	{
    		for(ItemStack stack : tanks)
    		{
    			ItemTank tank = (ItemTank) stack.getItem();
    			Fluid f1 = tank.getFluid(stack).getFluid();
				int inTank = tank.getAmount(stack);
				float factor = 62.5f;
				int metaDiff = full?16:1;
				toDrain = (int) (factor * metaDiff);
				next.setBlock(worldObj, f1.getBlock(), metaDiff-1);
	        	System.out.println(tank.drain(stack, toDrain, true).amount);
				break;
    		}
    	}
    	
    	return ret;
    }
    
    public ArrayList<ItemStack> getTanks(EntityPlayer player)
    {
    	ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
    	
    	for(ItemStack stack : player.inventory.mainInventory)
    	{
    		if(stack!=null && stack.getItem() instanceof ItemTank)
    		{
    			ItemTank tank = (ItemTank) stack.getItem();
    			if(tank.getFluid(stack)!=null)
    				ret.add(stack);
    		}
    	}
    	
    	return ret;
    }
	
	@SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister)
    {
        this.itemIcon = par1IconRegister.registerIcon(TechCore.ID+":"+"spout");
    }
	
}
