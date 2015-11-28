package thut.core.common.items;

import java.util.ArrayList;

import thut.api.ThutItems;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.core.common.blocks.BlockFluid;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

public class ItemSpout extends Item {

	public ItemSpout() {
		super();
        this.setHasSubtypes(true);
		this.setUnlocalizedName("spout");
		this.setCreativeTab(ThutCore.tabThut);
		ThutItems.spout = this;
	}
	
	//TODO move this to real method
    public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World worldObj, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
    	boolean ret = false;
    	int toDrain = 0;
    	Vector3 hit = Vector3.getNewVectorFromPool().set(pos);
    	Vector3 next = hit.offset(side);

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
	    			IFluidContainerItem tank = (IFluidContainerItem) stack.getItem();
	    			Fluid f1 = tank.getFluid(stack).getFluid();
	    			if(f.getUnlocalizedName().equals(f1.getUnlocalizedName()))
	    			{
	    				int inTank = tank.getFluid(stack).amount;
	    				float factor = 62.5f;
	    				int metaDiff = full?15-meta:1;
	    				toDrain = (int) (factor * metaDiff);
	    				FluidStack s = tank.drain(stack, toDrain, !player.capabilities.isCreativeMode);
	    				hit.setBlock(worldObj, hit.getBlock(worldObj), (int) (s.amount/62.5 + meta));
	    				break;
	    			}
	    		}
    	}
    	else if(next.getBlockMaterial(worldObj).isReplaceable())
    	{
    		for(ItemStack stack : tanks)
    		{
    			IFluidContainerItem tank = (IFluidContainerItem) stack.getItem();
    			Fluid f1 = tank.getFluid(stack).getFluid();
    			if(!f1.canBePlacedInWorld())
    			{
    				continue;
    			}
    			Block b = f1.getBlock();
    			if(b instanceof BlockFluidBase)
    			{
    				BlockFluidBase block = (BlockFluidBase) b;
					int inTank = tank.getFluid(stack).amount;
					int maxMeta = block.getMaxRenderHeightMeta();
					
					int metaDiff = full?16:1;
					
					toDrain = maxMeta ==0? 1000 : (int) (metaDiff * 1000f / (maxMeta+1));
					next.setBlock(worldObj, b, metaDiff-1, 3);
		        	tank.drain(stack, toDrain, !player.capabilities.isCreativeMode);
		        	break;
    			}
    			else
    			{
    				tank.drain(stack, 1000, !player.capabilities.isCreativeMode);
    				next.setBlock(worldObj, b, 0, 3);
    			}
    		}
    	}
    	
    	return ret;
    }
    
    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World worldObj, EntityPlayer player)
    {
//        Vector3 location = Vector3.getNewVectorFromPool().set(player).addTo(0, 1.62, 0);
//        location.addTo(player.getLookVec().xCoord * 2, player.getLookVec().yCoord * 2, player.getLookVec().zCoord * 2);
//        
//        System.out.println("test");
//
//        ArrayList<ItemStack> tanks = getTanks(player);
//        if(tanks.size()==0) return itemstack;
//        
//        if(location.getBlockMaterial(worldObj).isReplaceable())
//        {
//            for(ItemStack stack : tanks)
//            {
//                IFluidContainerItem tank = (IFluidContainerItem) stack.getItem();
//                Fluid f1 = tank.getFluid(stack).getFluid();
//                if(!f1.canBePlacedInWorld())
//                {
//                    continue;
//                }
//                Block b = f1.getBlock();
//                if(b instanceof BlockFluidBase)
//                {
//                    BlockFluidBase block = (BlockFluidBase) b;
//                    int inTank = tank.getFluid(stack).amount;
//                    int maxMeta = block.getMaxRenderHeightMeta();
//
//                    boolean full = !player.isSneaking();
//                    int toDrain = 0;
//                    int metaDiff = full?16:1;
//                    
//                    toDrain = maxMeta ==0? 1000 : (int) (metaDiff * 1000f / (maxMeta));
//                    System.out.println(toDrain+" "+maxMeta);
//                    location.setBlock(worldObj, b, metaDiff-1, 3);
//                    tank.drain(stack, toDrain, !player.capabilities.isCreativeMode);
//                    break;
//                }
//                else
//                {
//                    tank.drain(stack, 1000, !player.capabilities.isCreativeMode);
//                    location.setBlock(worldObj, b, 0, 3);
//                }
//            }
//        }
        
        return itemstack;
    }
    
    public ArrayList<ItemStack> getTanks(EntityPlayer player)
    {
    	ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
    	
    	for(ItemStack stack : player.inventory.mainInventory)
    	{
    		if(stack!=null && stack.getItem() instanceof IFluidContainerItem)
    		{
    			IFluidContainerItem tank = (IFluidContainerItem) stack.getItem();
    			if(tank.getFluid(stack)!=null)
    				ret.add(stack);
    		}
    	}
    	
    	return ret;
    }
}
