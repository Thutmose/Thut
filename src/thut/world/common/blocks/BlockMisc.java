package thut.world.common.blocks;

import java.util.Arrays;
import java.util.List;
import java.util.Random;







import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thut.api.Blocks;
import thut.api.explosion.ExplosionCustom;
import thut.api.explosion.ExplosionCustom.Cruncher;
import thut.api.explosion.Vector3;
import thut.api.utils.IRebar;
import thut.api.utils.IStampableBlock;
import thut.world.common.WorldCore;
import thut.world.common.blocks.fluids.solids.BlockSolidLava;
import thut.world.common.corehandlers.ConfigHandler;
import thut.world.common.ticks.ThreadSafeWorldOperations;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemCoal;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockMisc extends Block implements ITileEntityProvider//, IAntiPoisonBlock
{
	public int light = 0;
	public boolean dusts = false;
	ThreadSafeWorldOperations safe = new ThreadSafeWorldOperations();
	Integer[][] data;
	public BlockMisc(int par1) {
		super(par1, Material.rock);
		
		if(ConfigHandler.debugPrints)
			this.setCreativeTab(WorldCore.tabThut);
		this.setResistance(00);
		this.setHardness(00);
		this.setUnlocalizedName("misc");
		Blocks.misc = this;
		setTickRandomly(true);
	}
    @Override
    public boolean canCreatureSpawn(EnumCreatureType type,World worldObj, int x, int y, int z){
    	return false;
    }
//	@Override
//	public void updateTick(World worldObj, int x, int y, int z, Random par5Random)
//	{ 
//		light ++;
//	}
//	 public int getLightValue(IBlockAccess world, int x, int y, int z)
//     {
//		 if(light>15)light = 0;
//		 return light;
//     }
    /**
     * Updates the blocks bounds based on its current state. Args: world, x, y, z
     */
    public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
    {
    	int meta = par1IBlockAccess.getBlockMetadata(par2, par3, par4);
    	if(meta==2)
    	{
    		this.setBlockBounds(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    	}
    	else
    	{
    		this.setBlockBounds(0f, 0f, 0f, 1f, 1f, 1f);
    	}
    }
	
	
    @SideOnly(Side.CLIENT)

    /**
     * only called by clickMiddleMouseButton , and passed to inventory.setCurrentItem (along with isCreative)
     */
    public int idPicked(World par1World, int par2, int par3, int par4)
    {
    	int meta = par1World.getBlockId(par2, par3, par4);
    	if(meta==1)
    	{
    		return Blocks.solidLavas[0].blockID;
    	}
    	
        return this.blockID;
    }
	
	   /**
     * Called when a user uses the creative pick block button on this block
     *
     * @param target The full target the player is looking at
     * @return A ItemStack to add to the player's inventory, Null if nothing should be added.
     */
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
    {
        int id = idPicked(world, x, y, z);

        if (id == 0)
        {
            return null;
        }

        Item item = Item.itemsList[id];
        if (item == null)
        {
            return null;
        }
        

        return new ItemStack(id, 1, getDamageValue(world, x, y, z));
        
    }

	 
    public boolean onBlockActivated(World worldObj, int x, int y, int z, EntityPlayer player, int side, float par7, float par8, float par9)
    {
    	ItemStack item = player.getHeldItem();
    	int meta = safe.getBlockMetadata(worldObj,x,y,z);
    	
    	if(meta==0)
    	{
	    	if(!worldObj.isRemote&&item!=null&&item.getItem() instanceof ItemDye)
	    	{
		    	int meta1 = (15-item.getItemDamage())+1;
		    	System.out.println("boom");
		    	ExplosionCustom boom = new ExplosionCustom(worldObj, null, x+0.5, y+0.5, z+0.5, meta1*meta1*250);
		    	boom.doExplosionA();
		    	return true;
	    	}
	    	if(!worldObj.isRemote&&item!=null&&item.itemID == Block.cloth.blockID)
	    	{
		    	int meta1 = (15-item.getItemDamage())+1;
		    	System.out.println("boom");
		    //	(new ExplosionCustom()).doExplosion(worldObj, x+0.5, y+0.5, z+0.5, 2*meta1*meta1, true);
		    	ExplosionCustom.doExplosion(new Vector3(x+0.5, y+0.5, z+0.5),worldObj, 128 ,2*meta1*meta1*meta1);
		    	return true;
	    	}
	    	else if(!worldObj.isRemote&&item!=null&&item.getItem() instanceof ItemCoal)
	    	{
	    		worldObj.setBlock(x, y+1, z, blockID, 3, 3);
	    	}
	    	else if(!worldObj.isRemote&&item!=null&&item.itemID== Item.stick.itemID)
	    	{
	    		dusts = !dusts;
	    	}
//	    	else if(!worldObj.isRemote&&item!=null&&item.getItem() instanceof ItemSoup)
//	    	{
//	    		EntityTurret turret = new EntityTurret(worldObj, x+0.5, y+1.0, z+0.5,1);
//	    		worldObj.spawnEntityInWorld(turret);
//	    	}
//	    	else if(!worldObj.isRemote&&item!=null&&item.getItem() instanceof ItemHoe)
//	    	{
//	    		worldObj.setBlock(x, y+1, z, blockID, 2, 3);
//	    	}
    	}
    	
    	if(meta==2)
    	{
    		
    	}
    	
        return false;
        
    }
	 
    
    @SideOnly(Side.CLIENT)

    /**
     * When this method is called, your block should register all the icons it needs with the given IconRegister. This
     * is the only chance you get to register icons.
     */
    public void registerIcons(IconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon(WorldCore.TEXTURE_PATH + "misc");
    }

	  /**
     * Called throughout the code as a replacement for block instanceof BlockContainer
     * Moving this to the Block base class allows for mods that wish to extend vinella
     * blocks, and also want to have a tile entity on that block, may.
     *
     * Return true from this function to specify this block has a tile entity.
     *
     * @param metadata Metadata of the current block
     * @return True if block has a tile entity, false otherwise
     */
    public boolean hasTileEntity(int metadata)
    {
        return metadata>0;
    }

    /**
     * Called throughout the code as a replacement for ITileEntityProvider.createNewTileEntity
     * Return the same thing you would from that function.
     * This will fall back to ITileEntityProvider.createNewTileEntity(World) if this block is a ITileEntityProvider
     *
     * @param metadata The Metadata of the current block
     * @return A instance of a class extending TileEntity
     */
    public TileEntity createTileEntity(World world, int metadata)
    {
        return null;
    }

	 @SideOnly(Side.CLIENT)
    /**
     * Retrieves the block texture to use based on the display side. Args: iBlockAccess, x, y, z, side
     */
    public Icon getBlockTexture(IBlockAccess par1IBlockAccess, int x, int y, int z, int side)
    {
		int meta = par1IBlockAccess.getBlockMetadata(x, y, z);
		if(meta==1)
		{
			return Blocks.solidLavas[0].getBlockTexture(par1IBlockAccess, x, y, z, side);
		}
		else
			return this.blockIcon;
    }
	 
	    @SideOnly(Side.CLIENT)

	    /**
	     * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
	     */
	    public Icon getIcon(int par1, int par2)
	    {
	        return par2==0?this.blockIcon:Blocks.solidLavas[0].getIcon(par1, par2);
	    }
	    
	    public Icon getIcon(int meta)
	    {
	    	return ((BlockSolidLava)(Blocks.solidLavas[0])).iconArray[meta];
	    }

	@Override
	public TileEntity createNewTileEntity(World world) 
	{
		return null;
	}

}
