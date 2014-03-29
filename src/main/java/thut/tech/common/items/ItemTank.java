package thut.tech.common.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thut.core.common.blocks.BlockFluid;
import thut.tech.common.TechCore;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

public class ItemTank extends Item implements IFluidContainerItem
{
	public ItemTank() {
		super();
        this.setHasSubtypes(false);
		this.setUnlocalizedName("tank");
		this.setCreativeTab(TechCore.tabThut);
	}
	
	@SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister)
    {
        this.itemIcon = par1IconRegister.registerIcon(TechCore.ID+":"+"tank");
    }
	
    public ItemStack onItemRightClick(ItemStack itemstack, World worldObj, EntityPlayer player)
    {
    	int index = player.inventory.currentItem;
    	if(index > 0)
    	{
    		int n = index - 1;
    		ItemStack stack = player.inventory.getStackInSlot(n);
    		if(stack!=null)
    		if(Block.getBlockFromItem(stack.getItem()) instanceof BlockFluid)
    		{
    			BlockFluid fluid = (BlockFluid) Block.getBlockFromItem(stack.getItem());
    			FluidStack fstack = new FluidStack(fluid.getFluid(), stack.stackSize * 1000);
    			int added = fill(itemstack, fstack, true)/1000;
    			
    			stack.splitStack(added);
    		//	System.out.println();
    		}
    	}
    	
    	
    	return itemstack;
    }
	
	public int getAmount(ItemStack container)
	{
		if(getFluid(container)!=null) return getFluid(container).amount;
		return 0;
	}
	
	@Override
	public FluidStack getFluid(ItemStack container) {
		
		if(!container.hasTagCompound()) return null;
		String name = container.getTagCompound().getString("fluidName");
		int amount = container.getTagCompound().getInteger("fluidAmount");
		if(FluidRegistry.getFluid(name)==null) return null;
		
		FluidStack ret = new FluidStack(FluidRegistry.getFluidID(name), amount, container.getTagCompound());
		return ret;
	}

	@Override
	public int getCapacity(ItemStack container) {
		if(container.getItem() == this)
			return 64000;
		return 0;
	}

	@Override
	public int fill(ItemStack container, FluidStack resource, boolean doFill) {
		int amount = resource.amount;
		int exist = getAmount(container);
		exist = Math.min(exist, 64000);
		
		int ret = 0;
		if(amount + exist <= 64000)
			ret = amount;
		else
			ret = 64000 - exist;
		
		
		if(doFill)
		{
			if(!container.hasTagCompound())
			{
				container.setTagCompound(new NBTTagCompound());
				container.getTagCompound().setString("fluidName", FluidRegistry.getFluidName(resource));
				container.getTagCompound().setInteger("fluidAmount", 0);
			}
			String name = container.getTagCompound().getString("fluidName");
			int amt = container.getTagCompound().getInteger("fluidAmount");
			System.out.println(name);
			if(name==null||name.trim().isEmpty())
			{
				container.getTagCompound().setString("fluidName", name = FluidRegistry.getFluidName(resource));
				container.getTagCompound().setInteger("fluidAmount", amt = 0);
			}
			else if(FluidRegistry.getFluidID(name)!=resource.fluidID)
			{
				return 0;
			}
			
			container.getTagCompound().setInteger("fluidAmount", amt+amount);
			container.setStackDisplayName("Tank of "+name+" "+(amt+amount));
		}
		
		return ret;
	}

	@Override
	public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain) {
		int exist = getAmount(container);
		int amount = Math.min(maxDrain, exist);
		
		if(getFluid(container)==null) return null;
		
		String name = container.getTagCompound().getString("fluidName");
		
		if(doDrain)
		{
			container.getTagCompound().setInteger("fluidAmount", exist - amount);
			container.setStackDisplayName("Tank of "+name+" "+(exist - amount));
			if((exist - amount)<=0)
			{
				container.setTagCompound(null);
				container.setStackDisplayName("Empty Tank");
			}
		}
		
		return new FluidStack(FluidRegistry.getFluidID(name), amount, container.getTagCompound());
	}
}
