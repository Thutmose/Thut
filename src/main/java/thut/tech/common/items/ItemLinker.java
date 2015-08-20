package thut.tech.common.items;

import java.util.UUID;

import scala.actors.threadpool.Arrays;
import thut.api.ThutBlocks;
import thut.api.maths.ExplosionCustom;
import thut.api.maths.Vector3;
import thut.api.maths.ExplosionCustom.ExplosionStuff;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.entity.EntityProjectile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;

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
    	if(worldObj.isRemote || itemstack.getItemDamage()!=10)
    		return itemstack;
    	
    	Vector3 here = Vector3.getNewVectorFromPool().set(player);
    	BiomeGenBase b = here.getBiome(worldObj);
    	
		Vector3 direction = Vector3.getNewVectorFromPool().set(player.getLookVec());
		Vector3 location2 = Vector3.getNextSurfacePoint(worldObj, here, direction, 255);
        ExplosionCustom boom = new ExplosionCustom(worldObj, player, here, 200);
    	System.out.println(Arrays.toString(BiomeDictionary.getTypesForBiome(b))+" "+location2);
        ExplosionCustom.explosions.add(new ExplosionStuff(boom, 100, 200, worldObj, here));
    	
    	
//    	EntityProjectile p = new EntityProjectile(worldObj, here.x, here.y, here.z, Blocks.stone);
//    	here.set(player.getLookVec()).scalarMultBy(0);
//    	here.setVelocities(p);
//    	worldObj.spawnEntityInWorld(p);
//    	
        return itemstack;
    }
	
    public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World worldObj, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
    	boolean ret = false;

    	if(itemstack.stackTagCompound == null)
    	{
	       	return false;
    	}
       	else
       	{
	       	Block id = worldObj.getBlock(x, y, z);
	       	int meta = worldObj.getBlockMetadata(x, y, z);
	       	
			if(id==ThutBlocks.lift&&meta==1 && !player.isSneaking())
			{
				TileEntityLiftAccess te = (TileEntityLiftAccess)worldObj.getTileEntity(x, y, z);
				te.setSide(side, true);
				return true;
			}
	 	
	       	UUID liftID;
			try {
				liftID = UUID.fromString(itemstack.stackTagCompound.getString("lift"));
			} catch (Exception e) {
				return false;
			}
	       	
	       	
	       	EntityLift lift = EntityLift.getLiftFromUUID(liftID);
	       	
			if(player.isSneaking()&&lift!=null&&id==ThutBlocks.lift&&meta==1)
			{
				TileEntityLiftAccess te = (TileEntityLiftAccess)worldObj.getTileEntity(x, y, z);
				te.setLift(lift);
				int floor = te.getButtonFromClick(side, hitX, hitY, hitZ);
				te.setFloor(floor);
				if(worldObj.isRemote)
				player.addChatMessage(new ChatComponentText("Set this Floor to "+floor));
				return true;
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
       	stack.stackTagCompound.setString("lift", lift.id.toString());
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
        this.itemIcon = par1IconRegister.registerIcon("thuttech:liftController");
    }
}
