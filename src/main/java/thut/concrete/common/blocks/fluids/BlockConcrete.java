package thut.concrete.common.blocks.fluids;

import static thut.api.ThutBlocks.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import thut.api.blocks.IRebar;
import thut.concrete.common.ConcreteCore;
import thut.concrete.common.blocks.tileentity.worldBlocks.TileEntityBlockFluid;
import thut.world.common.blocks.fluids.BlockFluid;
import thut.world.common.blocks.fluids.BlockFluid.FluidInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
public class BlockConcrete extends BlockFluid implements ITileEntityProvider//, IAntiPoisonBlock
{
	
	public static int resistance = 10;
	public static float hardness = 30;
	Integer[][] data;
	
	public BlockConcrete() {
		super(new Fluid("solid"),Material.rock);
		setBlockName("concrete");
		this.rate = 10;
		concrete = this;
		setCreativeTab(ConcreteCore.tabThut);
		setSolid();
		this.stampable = true;
		this.setTickRandomly(false);
		this.setStepSound(soundTypeStone);
	}
	public void setData()
	{
		if(fluidBlocks.get(this)==null){
			fluidBlocks.put(this, new FluidInfo());
			FluidInfo info = fluidBlocks.get(this);
			info.viscosity = 15;
			info.randomFactor = 15;
			info.fallOfEdge = false;
			info.combinationBlocks.put(this, this);
			}
	}
	
	
	@Override
	public boolean isNormalCube(IBlockAccess world, int x, int y, int z) {
		return world.getBlockMetadata(x, y, z) == 15;
	}
    /**
     * Returns the ID of the items to drop on destruction.
     */
    public Item getItemDropped(int par1, Random par2Random, int par3)
    {
        return Item.getItemFromBlock(rebar);
    }
    
    
    @Override
    public int quantityDropped(int meta, int fortune, Random random)
    {
        return 1;
    }
	
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int x, int y, int z)
    {
    	this.setBoundsByMeta(par1IBlockAccess.getBlockMetadata(x, y, z));
    	this.setResistanceByMeta(par1IBlockAccess.getBlockMetadata(x, y, z));
    }

    
	@Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
    {
        int l = par1World.getBlockMetadata(par2, par3, par4);
        float f = 0.0625F;
        return AxisAlignedBB.getAABBPool().getAABB((double)par2 + this.minX, (double)par3 + this.minY, (double)par4 + this.minZ,
        								(double)par2 + this.maxX, (double)((float)par3 + (float)l * f), (double)par4 + this.maxZ);
    }
	
	@Override
    public float getExplosionResistance(Entity par1Entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ)
    {
        return getBlastResistanceByMeta(world.getBlockMetadata(x, y, z));
    }
	
	@Override
	public void updateTick(World worldObj, int x, int y, int z, Random par5Random){}
	
	
	public void onBlockClicked(World worldObj, int x, int y, int z, EntityPlayer player){
		this.setResistanceByMeta(worldObj.getBlockMetadata(x, y, z));
	}
	
	public void setResistanceByMeta(int meta){
		int j = meta;
        float f = (float)((1 + j)) / 16.0F;
        this.setResistance(f*resistance);
        this.setHardness(f*hardness);
	}
	public float getBlastResistanceByMeta(int meta){
		int j = meta;
        float f = (float)((1 + j)) / 16.0F;
        return (f*resistance);
	}
	public float getHardnessByMeta(int meta){
		int j = meta;
        float f = (float)((1 + j)) / 16.0F;
        return (f*hardness);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon("concrete:dryConcrete_"+8);
		this.theIcon = par1IconRegister.registerIcon("concrete:" + "rebarRusty");
		this.iconArray = new IIcon[16];
    	for (int i = 0; i < this.iconArray.length; ++i)
        {
            this.iconArray[i] = par1IconRegister.registerIcon("concrete:" + "dryConcrete_"+i);
        }
	}
	
	@SideOnly(Side.CLIENT)
	public IIcon theIcon;
	
    
	public boolean[] sides(IBlockAccess worldObj, int x, int y, int z) {
		boolean[] side = new boolean[6];
    	int[][]sides = {{1,0,0},{-1,0,0},{0,0,1},{0,0,-1},{0,1,0},{0,-1,0}};
		for(int i = 0; i<6; i++){
			Block block = worldObj.getBlock(x+sides[i][0], y+sides[i][1], z+sides[i][2]);
			side[i] = (block instanceof IRebar);
		}
		return side;
	}

	@Override
	public IIcon getIcon(IBlockAccess worldObj, int x, int y, int z, int side)
	{
		TileEntityBlockFluid te = (TileEntityBlockFluid) worldObj.getTileEntity(x, y, z);
		return iconArray[te.metaArray[side]];
	}
	@Override
    public boolean recolourBlock(World world, int x, int y, int z, ForgeDirection side, int colour)
    {
    	TileEntityBlockFluid te = (TileEntityBlockFluid) world.getTileEntity(x, y, z);
    	int old = te.metaArray[side.ordinal()];
    	if(old == colour)
    		return false;
    	te.metaArray[side.ordinal()] = colour;
    	te.sendUpdate();
    	return true;
    }

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
	    return new TileEntityBlockFluid();
	}
}
