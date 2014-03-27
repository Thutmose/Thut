package thut.world.common.items.tools;

import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.world.World;
import thut.api.explosion.ExplosionCustom;
import thut.api.maths.Vector3;
import thut.core.common.blocks.BlockFluid;
import thut.world.common.WorldCore;
import thut.world.common.blocks.fluids.solids.BlockSolidLava;
import thut.world.common.corehandlers.ItemHandler;
import thut.world.common.items.resources.ItemDusts;

public class ItemGrinder extends Item {

	public static final int MAX_USES = 128;
	
	public ItemGrinder() {
		super();
		this.maxStackSize = 1;
		this.setMaxDamage(MAX_USES);
		this.setCreativeTab(WorldCore.tabThut);
		this.setUnlocalizedName("smoother");
	}

    public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
    	
    	if(world.isRemote) return true;
    	
    	Vector3 look = new Vector3(par2EntityPlayer.getLookVec());
    	EntityLiving mew = null;
    	
    	try {
			mew = (EntityLiving) WorldCore.test
					.getConstructor(new Class[] { World.class })
					.newInstance(new Object[] { world });
		} catch (Exception e) {
		}
    	if(mew!=null)
    	{
			mew.setHealth(mew.getMaxHealth());
			mew.setLocationAndAngles(
					(double) x + 0.5F, (double) y + 1.5F,
					(double) z + 0.5F,
					world.rand.nextFloat() * 360.0F, 0.0F);
			world.spawnEntityInWorld(mew);
    	}
    		
    	
    	return true;
    	
//        if (!par2EntityPlayer.canPlayerEdit(x, y, z, side, par1ItemStack))//TODO reimplement grinder
//        {
//            return false;
//        }
//        else
//        {
//        	int blockid = par3World.getBlockId(x, y, z);
//        	Block block = Block.blocksList[blockid];
//        	int minmeta = 30;
//        	int maxmeta = 0;
//        	
//        	if(block instanceof IStampableBlock && par2EntityPlayer.isSneaking())
//        	{
//        		int meta = par3World.getBlockMetadata(x, y, z);
//        		((IStampableBlock)block).setBlockIcon(blockid, meta, side, par3World, x, y, z, side);
//        	}
//        	
//        	for(int locx = -1; locx < 2; locx++)
//        	{
//        		for(int locz = -1; locz < 2; locz++)
//        		{
//        			blockid = par3World.getBlockId(x + locx, y, z + locz);
//        			if(block instanceof BlockFluid)
//        			{
//        				if(par3World.getBlockMetadata(x + locx, y, z + locz) < minmeta)
//        				{
//        					minmeta = par3World.getBlockMetadata(x + locx, y, z + locz);
//        				}
//        			}
//        		}
//        	}
//        	
//        	int modifyx = 0;
//        	int modifyz = 0;
//        	boolean found = false;
//        	int totalPieces = 0;
//        	
//        	for(int i = 0; i < 9; i++)
//        	{
//        		found = false;
//        		maxmeta = 0;
//        		
//            	for(int locx = -1; locx < 2; locx++)
//            	{
//            		for(int locz = -1; locz < 2; locz++)
//            		{
//            			blockid = par3World.getBlockId(x + locx, y, z + locz);
//            			if(Block.blocksList[blockid] instanceof BlockFluid)
//            			{
//            				if((par3World.getBlockMetadata(x + locx, y, z + locz)) > maxmeta && (par3World.getBlockMetadata(x + locx, y, z + locz)) != 15)
//            				{
//            					found = true;
//            					modifyx = x + locx;
//            					modifyz = z + locz;
//            					maxmeta = par3World.getBlockMetadata(x + locx, y, z + locz);
//            				}
//            			}
//            		}
//            	}
//            	
//            	if(found)
//            	{
//	            	if(MAX_USES - (par1ItemStack.getItemDamage()) >= maxmeta - minmeta)
//	            	{
//	            		par3World.setBlockMetadataWithNotify(modifyx, y, modifyz, minmeta, 3);
//	            		par1ItemStack.damageItem(maxmeta - minmeta, par2EntityPlayer);
//	            		
//	            		blockid = par3World.getBlockId(modifyx, y, modifyz);
//	            		
//	            		if(Block.blocksList[blockid] instanceof BlockSolidLava)
//	            		{
//	            			totalPieces += maxmeta - minmeta;
//	            		}
//	            	}
//	            	else
//	            	{
//	            		par3World.setBlockMetadataWithNotify(modifyx, y, modifyz, MAX_USES - (par1ItemStack.getItemDamage()), 3);
//	            		par1ItemStack.damageItem(MAX_USES - (par1ItemStack.getItemDamage()) + 1, par2EntityPlayer);
//	            		
//	            		blockid = par3World.getBlockId(modifyx, y, modifyz);
//	            		
//	            		if(Block.blocksList[blockid] instanceof BlockSolidLava)
//	            		{
//	            			totalPieces += MAX_USES - (par1ItemStack.getItemDamage());
//	            		}
//	            		
//	            		break;
//	            	}
//            	}
//        	}
//        	
//        	int dustid = -1;
//        	
//        	for(Item item : ItemHandler.items)
//        	{
//        		if(item instanceof ItemDusts)
//        		{
//        			dustid = item.itemID;
//        		}
//        	}
//        	
//        	if(dustid == -1)
//        	{
//        		return true;
//        	}
//        	
//        	if(!par3World.isRemote)
//        	{
//
//            	Random rdusts = new Random();
//            	int randnum = rdusts.nextInt(totalPieces+1);
//            	EntityItem dusts = new EntityItem(par3World, x + hitX, y + hitY, z + hitZ, new ItemStack(dustid, randnum, 0));
//            	if(randnum>0)
//            	{
//            		par3World.spawnEntityInWorld(dusts);
//            	}
//        	}
//        	
//        	return true;
//        }
        
        
    }
    @Override
	@SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister)
    {
        this.itemIcon = par1IconRegister.registerIcon(WorldCore.TEXTURE_PATH+"ItemSmoother");
    }
}
