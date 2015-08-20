package thut.concrete.common.blocks.fluids;

import static thut.api.ThutBlocks.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import codechicken.lib.vec.BlockCoord;
import thut.api.ThutBlocks;
import thut.api.blocks.BlockFluid;
import thut.api.blocks.IRebar;
import thut.api.blocks.multiparts.parts.PartFluid;
import thut.concrete.common.ConcreteCore;
import thut.concrete.common.blocks.tileentity.worldBlocks.TileEntityBlockFluid;
import cpw.mods.fml.client.FMLClientHandler;
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
		super(new Fluid("solidconcrete").setDensity(3000).setViscosity(Integer.MAX_VALUE),Material.rock);
		setBlockName("concrete");
		concrete = this;
		ThutBlocks.addPart(this, ConcretePart.class);
		ThutBlocks.parts2.put("tc_concrete", ConcretePart.class);
		setCreativeTab(ConcreteCore.tabThut);
		this.setTickRandomly(false);
		this.setStepSound(soundTypeStone);
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
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
    {
        int l = par1World.getBlockMetadata(par2, par3, par4);
        float f = 0.0625F;
        return AxisAlignedBB.getBoundingBox((double)par2 + this.minX, (double)par3 + this.minY, (double)par4 + this.minZ,
        								(double)par2 + this.maxX, (double)((float)par3 + (float)l * f), (double)par4 + this.maxZ);
    }
	
	@Override
    public float getExplosionResistance(Entity par1Entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ)
    {
        return getBlastResistanceByMeta(world.getBlockMetadata(x, y, z));
    }
	
	@Override
	public void updateTick(World worldObj, int x, int y, int z, Random par5Random){}
	
	  /**
	   * Returns true if the given side of this block type should be rendered, if the adjacent block is at the given
	   * coordinates.  Args: blockAccess, x, y, z, side
	   */
	  @SideOnly(Side.CLIENT)
	  public boolean shouldSideBeRendered(IBlockAccess p_149646_1_, int p_149646_2_, int p_149646_3_, int p_149646_4_, int p_149646_5_)
	  {
	      return p_149646_5_ == 0 && this.minY > 0.0D ? true : (p_149646_5_ == 1 && this.maxY < 1.0D ? true : (p_149646_5_ == 2 && this.minZ > 0.0D ? true : (p_149646_5_ == 3 && this.maxZ < 1.0D ? true : (p_149646_5_ == 4 && this.minX > 0.0D ? true : (p_149646_5_ == 5 && this.maxX < 1.0D ? true : !p_149646_1_.getBlock(p_149646_2_, p_149646_3_, p_149646_4_).isOpaqueCube())))));
	  }
	
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
	IIcon[] iconArray;
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

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(IBlockAccess worldObj, int x, int y, int z, int side)
	{
		BlockCoord pos = new BlockCoord(x,y,z);
		TileEntityBlockFluid te = (TileEntityBlockFluid) PartFluid.getFluidTile(FMLClientHandler.instance().getWorldClient(), pos);
		return iconArray[te!=null?te.metaArray[side]:8];//
	}
	@Override
    public boolean recolourBlock(World world, int x, int y, int z, ForgeDirection side, int colour)
    {
		BlockCoord pos = new BlockCoord(x,y,z);
		TileEntityBlockFluid te = (TileEntityBlockFluid) PartFluid.getFluidTile(world, pos);
		System.out.println(te+" "+world.getBlockMetadata(x, y, z));
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
	
    public static class ConcretePart extends PartFluid
    {
    	public ConcretePart()
    	{
    		this(0);
    	}
    	
    	public ConcretePart(int meta)
    	{
    		super(meta);
    		name = "tc_concrete";
    		block = ThutBlocks.concrete;
    		tile = new TileEntityBlockFluid();
    		hasTile = true;
    	}
    }
}
