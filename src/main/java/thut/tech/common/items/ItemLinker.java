package thut.tech.common.items;

import thut.api.ThutBlocks;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.tileentity.TileEntityLiftAccess;
import thut.tech.common.entity.EntityLift;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

public class ItemLinker extends Item
{
	public static Item instance;
	
	public ItemLinker() 
	{
		super();
        this.setHasSubtypes(true);
		this.setUnlocalizedName("deviceLinker");
		this.setCreativeTab(TechCore.tabThut);
		instance = this;
	}
	
    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    public ItemStack onItemRightClick(ItemStack itemstack, World worldObj, EntityPlayer player)
    {
       	
    	if(itemstack.stackTagCompound == null)
    	{
    		return itemstack;
    	}
//       	int liftID = itemstack.stackTagCompound.getInteger("lift");
//       	EntityLift lift = EntityLift.lifts.get(liftID);
//       	if(lift!=null)
//       	{
//       		boolean move = lift.toMoveY;
//       		boolean up = lift.up;
//       		
//       		if(!worldObj.isRemote)
//       		{
//	       		if(player.isSneaking())
//	       		{
//	       			lift.toMoveY = !lift.toMoveY;
//	       		}
//	       		else
//	       		{
//		       		lift.up = !lift.up;
//	       		}
//       			PacketDispatcher.sendPacketToPlayer(PacketLift.getPacket(lift, lift.toMoveY?1:0, lift.up?1:0), (Player) player);
//       		}
//       		else
//       		{
//	       		if(player.isSneaking())
//	       		{
//	       			move = !lift.toMoveY;
//	       		}
//	       		else
//	       		{
//		       		up = !lift.up;
//	       		}
//       		}
//       		String message = "Lift "+(move?"Moving "+(up?"Up":"Down"):"Stopped");
//       		if(worldObj.isRemote)
//       			player.addChatMessage(message);
//       	}
        return itemstack;
    }
	
    public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World worldObj, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
    	boolean ret = false;
    	
    	if(itemstack.stackTagCompound == null)
    	{
//    		itemstack.setTagCompound(new NBTTagCompound());
//	       	TileEntity te = worldObj.getTileEntity(x, y, z);
//	       	if(!(te instanceof IDataSource))
//	       	{
	       		return false;
//	       	}
//	       	itemstack.stackTagCompound.setInteger("id", ((IDataSource)te).getID());
//    		player.addChatMessage("Device ID: "+Integer.toString(((IDataSource)te).getID()));
//    		
//			return true;
    	}
       	else
       	{
	       	Block id = worldObj.getBlock(x, y, z);
	       	int meta = worldObj.getBlockMetadata(x, y, z);
	       	
	 	
	       	int liftID = itemstack.stackTagCompound.getInteger("lift");
	       	EntityLift lift = EntityLift.lifts.get(liftID);
	       	
			if(player.isSneaking()&&lift!=null&&id==ThutBlocks.lift&&meta==1)
			{
				TileEntityLiftAccess te = (TileEntityLiftAccess)worldObj.getTileEntity(x, y, z);
				te.setLift(lift);
				te.setFloor(te.getButtonFromClick(side, hitX, hitY, hitZ));
				if(worldObj.isRemote)
				player.addChatMessage(new ChatComponentText("Set this Floor to "+te.floor));
				return true;
			}       	
			if(lift!=null&&id==ThutBlocks.lift&&meta==1)
			{
				TileEntityLiftAccess te = (TileEntityLiftAccess)worldObj.getTileEntity(x, y, z);
				if(side!=te.side)
				{
					te.setSide(side);
					return true;
				}
			}
       	}
    	return false;
    }
    
    public void setLift(EntityLift lift, ItemStack stack)
    {
       	if(stack.stackTagCompound == null)
    	{
    		stack.setTagCompound(new NBTTagCompound() );
    	}
       	stack.stackTagCompound.setInteger("lift", lift.id);
    }
    
    /**
     * If this function returns true (or the item is damageable), the ItemStack's NBT tag will be sent to the client.
     */
    public boolean getShareTag()
    {
        return true;
    }
    
	@SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister)
    {
        this.itemIcon = par1IconRegister.registerIcon(TechCore.ID+":"+"liftController");
    }
}
