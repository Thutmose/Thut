package thut.concrete.common.blocks.technical;

import static net.minecraft.init.Blocks.brick_block;
import static net.minecraft.init.Blocks.stonebrick;
import static thut.api.ThutBlocks.*;

import java.util.Arrays;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thut.api.blocks.tileentity.TileEntityMultiBlockPartFluids;
import thut.api.blocks.tileentity.TileEntityMultiCoreFluids;
import thut.concrete.common.ConcreteCore;
import thut.concrete.common.blocks.tileentity.crafting.TileEntityMixer;
import thut.concrete.common.blocks.tileentity.crafting.TileEntityMixer;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockMixer extends Block implements ITileEntityProvider
{

	public BlockMixer() {
		super(Material.iron);
		setBlockName("blockMixer");
		setStepSound(soundTypeStone);
		setHardness(3.5f);
		setCreativeTab(ConcreteCore.tabThut);
		mixer = this;
	}
	
	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		int meta = world.getBlockMetadata(x, y, z);
		if(meta==0||meta==1)
			return meta == 0 ? 0 : 15; 
		return 0;
	}
	
	 public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
	    {
		 
		 	int meta = world.getBlockMetadata(x, y, z);

	        if(player.isSneaking())
	            return false;
	         
			TileEntity t = world.getTileEntity(x, y, z);
			
			if((t instanceof TileEntityMultiBlockPartFluids))
			{
				TileEntityMultiBlockPartFluids dummy = (TileEntityMultiBlockPartFluids)t;
				
				if(dummy != null && dummy.getCore() != null)
				{
					TileEntityMultiCoreFluids core = dummy.getCore();
					return core.getBlockType().onBlockActivated(world, core.xCoord, core.yCoord, core.zCoord, player, par6, par7, par8, par9);
				}
				
				return true;
			}
			TileEntityMixer tileEntity = (TileEntityMixer)t;
	         
	        if(tileEntity != null)
	        {
	            if(!tileEntity.getIsValid())
	            {
	                if(tileEntity.checkIfProperlyFormed())
	                {
	                    tileEntity.convertDummies();
	                }
	            }
	            // Check if the multi-block structure has been formed.
	            System.out.println(Arrays.toString(tileEntity.tankCapacities()));
	            if(tileEntity.getIsValid())//TODO gui in core
	                player.openGui(ConcreteCore.instance, 0, world, x, y, z);
	        }
	         
	        return true;
	    }
	
	@Override
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		blockIcon = iconRegister.registerIcon("concrete:brick");
	}
	
    @SideOnly(Side.CLIENT)
    /**
     * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
     */
    public IIcon getIcon(int par1, int par2)
    {
        return blockIcon;
    } 
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack itemStack)
	{
		TileEntity t = world.getTileEntity(x, y, z);
		
		if(!(t instanceof TileEntityMixer))
			return;
		
		int meta = world.getBlockMetadata(x, y, z);
		TileEntityMixer te = (TileEntityMixer)t;
		ForgeDirection side =  getFacingfromEntity(entity);
		
	}
    /**
     * Called when a block is placed using its ItemBlock. Args: World, X, Y, Z, side, hitX, hitY, hitZ, block metadata
     */
    public int onBlockPlaced(World par1World, int par2, int par3, int par4, int par5, float par6, float par7, float par8, int par9)
    {
        return par9;
    }
	
	public ForgeDirection getFacingfromEntity(EntityLivingBase e)
	{
		ForgeDirection side = null;
		double angle = e.rotationYaw%360;
			
		if(angle>315||angle<=45)
		{
			return ForgeDirection.SOUTH;
		}
		if(angle>45&&angle<=135)
		{
			return ForgeDirection.WEST;
		}
		if(angle>135&&angle<=225)
		{
			return ForgeDirection.NORTH;
		}
		if(angle>225&&angle<=315)
		{
			return ForgeDirection.EAST;
		}
		
		return side;
	}
	
	@Override
	 @SideOnly(Side.CLIENT)
    /**
     * Retrieves the block texture to use based on the display side. Args: iBlockAccess, x, y, z, side
     */
    public IIcon getIcon(IBlockAccess par1IBlockAccess, int x, int y, int z, int side)
    {
		 return blockIcon;
    }

    public boolean hasTileEntity(int metadata)
    {
        return true;
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
        if (metadata==0||metadata==1)
        {
        	return createNewTileEntity(world,0);
        }
        if(metadata == 2||metadata==3)
        	return new TileEntityMultiBlockPartFluids();
        return null;
    }
	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block par5, int par6)
	{
		TileEntity t = world.getTileEntity(x, y, z);
		if(t instanceof TileEntityMultiBlockPartFluids)
		{
			TileEntityMultiBlockPartFluids p = (TileEntityMultiBlockPartFluids)t;
			if(p.getCore()!=null)
				p.getCore().invalidateMultiblock();
		}
		
		t = world.getTileEntity(x, y, z);
		if(!(t instanceof TileEntityMixer))
			return;
		
		TileEntityMixer tileEntity = (TileEntityMixer)t;
		
		if(tileEntity != null)
			tileEntity.invalidateMultiblock();
		
		dropItems(world, x, y, z);
		
		super.breakBlock(world, x, y, z, par5, par6);
	}
	
	private static int getSideFromFacing(int facing)
	{
		return ForgeDirection.getOrientation(facing+2).ordinal();
	}
	
	@Override
	public Item getItemDropped(int par1, Random par2Random, int par3)
	{
		System.out.println(par1+" "+par3);
		if(par1==2)
			return Item.getItemFromBlock(brick_block);
		if(par1==3)
			return Item.getItemFromBlock(stonebrick);
		return super.getItemDropped(par1, par2Random, par3);
	}	 
	
	@SideOnly(Side.CLIENT)

    /**
     * only called by clickMiddleMouseButton , and passed to inventory.setCurrentItem (along with isCreative)
     */
	@Override
	 public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		int meta = world.getBlockMetadata(x, y, z);
		if(meta==2)
		 return new ItemStack(brick_block);
		if(meta==3)
			return new ItemStack(stonebrick);
		return new ItemStack(this);
    }
	
	private void dropItems(World world, int x, int y, int z)
	{
		Random prng = new Random();
		
		TileEntityMixer tileEntity = (TileEntityMixer)world.getTileEntity(x, y, z);
		if(tileEntity == null)
			return;
		
		for(int slot = 0; slot < tileEntity.getSizeInventory(); slot++)
		{
			ItemStack item = tileEntity.getStackInSlot(slot);
			
			if(item != null && item.stackSize > 0)
			{
				float rx = prng.nextFloat() * 0.8f + 0.1f;
				float ry = prng.nextFloat() * 0.8f + 0.1f;
				float rz = prng.nextFloat() * 0.8f + 0.1f;
				
				EntityItem entityItem = new EntityItem(world, x + rx, y + ry, z + rz, item.copy());
				world.spawnEntityInWorld(entityItem);
				item.stackSize = 0;
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileEntityMixer();
	}

}
