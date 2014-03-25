package thut.world.common.blocks.fluids.gases;

import thut.api.maths.Vector3;
import thut.world.common.WorldCore;
import thut.world.common.corehandlers.ConfigHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public abstract class BlockGas extends BlockFluidFinite
{
	boolean breathable = false;

	public BlockGas(Fluid fluid) {
		super(fluid, Material.air);
		FluidRegistry.registerFluid(fluid);
		setLightOpacity(0);
		if(ConfigHandler.debugPrints)
			this.setCreativeTab(WorldCore.tabThut);
	}
	
    /**
     * Determines this block should be treated as an air block
     * by the rest of the code. This method is primarily
     * useful for creating pure logic-blocks that will be invisible
     * to the player and otherwise interact as air would.
     *
     * @param world The current world
     * @param x X Position
     * @param y Y position
     * @param z Z position
     * @return True if the block considered air
     */
    public boolean isAir(IBlockAccess world, int x, int y, int z)
    {
        return true;
    }

    
	@Override
	public void onEntityCollidedWithBlock(World worldObj, int x, int y, int z, Entity entity) 
	{
		if(breathable||!(entity instanceof EntityLivingBase)) return;
		EntityLivingBase e = (EntityLivingBase)entity;
		Vector3 here = new Vector3(x,y,z);
		Vector3 up = here.offset(ForgeDirection.UP);
		if(up.getBlock(worldObj)==null)return;
		e.setAir(e.getAir()-20);
		
		if(up.getBlock(worldObj).isNormalCube(worldObj, x, y, z)&&here.getBlockMetadata(worldObj)>3)
			e.addPotionEffect(new PotionEffect(Potion.confusion.id, 100));//TODO suffocation stuff
		else if(up.getBlock(worldObj)instanceof BlockGas&&up.getBlockMetadata(worldObj)>3&&!((BlockGas)up.getBlock(worldObj)).breathable)
			e.addPotionEffect(new PotionEffect(Potion.confusion.id, 100));//TODO suffocation stuff
	}
    
    /**
     * Called when a block is placed using its ItemBlock. Args: World, X, Y, Z, side, hitX, hitY, hitZ, block metadata
     */
    public int onBlockPlaced(World par1World, int par2, int par3, int par4, int par5, float par6, float par7, float par8, int par9)
    {
        return 7;
    }

	public static Fluid getFluid(int density, String name)
	{
		return new Fluid(name).setDensity(density).setGaseous(true).setViscosity(10);
	}
	
	public int countSides(World worldObj, int x, int y, int z, Block id)
	{
		int ret = 0;
		for(ForgeDirection side: ForgeDirection.VALID_DIRECTIONS)
		{
			if(worldObj.getBlock(x+side.offsetX, y+side.offsetY, z+side.offsetZ)==id)
				ret++;
		}
		return ret;
	}
	
	@Override
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		blockIcon = iconRegister.registerIcon("thutconcrete:blank");
	}
}
