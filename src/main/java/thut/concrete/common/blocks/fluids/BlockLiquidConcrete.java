package thut.concrete.common.blocks.fluids;

import static thut.api.ThutBlocks.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import thut.api.ThutBlocks;
import thut.api.maths.Vector3;
import thut.concrete.common.ConcreteCore;
import thut.core.common.blocks.BlockFluid;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.World;
import net.minecraft.item.*;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

public class BlockLiquidConcrete extends BlockFluid
{
	public static int hardenRate = 5;
	static Material wetConcrete = (new WetRock(MapColor.stoneColor));
	
	Integer[][] data;
    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;
	public BlockLiquidConcrete() {
		super(new Fluid("liquidRock").setDensity(4000).setViscosity(2000), wetConcrete);
		setBlockName("concreteLiquid");
		setCreativeTab(ConcreteCore.tabThut);
		this.setResistance((float) 10.0);
		this.setHardness((float) 1.0);
		this.rate = 0.9;
		ThutBlocks.liquidConcrete = this;
		this.setTemperature(310);
		this.solidifiable = true;
		this.setTickRandomly(true);
		this.placeamount = 16;
	}
	
	/////////////////////////////////////////Block Bounds Stuff//////////////////////////////////////////////////////////
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
    {
    	this.setBoundsByMeta(par1IBlockAccess.getBlockMetadata(par2, par3, par4));
    }
    /**
     * Returns a bounding box from the pool of bounding boxes (this means this box can change after the pool has been
     * cleared to be reused)
     */
	@Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
    {
        return null;
    }
    /**
     * Adds all intersecting collision boxes to a list. (Be sure to only add boxes to the list if they intersect the
     * mask.) Parameters: World, X, Y, Z, mask, list, colliding entity
     */
	  @Override
    public void addCollisionBoxesToList(World worldObj, int x, int y, int z, AxisAlignedBB aaBB, List list, Entity par7Entity)
    {
		  
    }
	  @Override
    public boolean isNormalCube(IBlockAccess world, int x, int y, int z)
    {
        return false;
    }
 
    @Override
    public void onBlockAdded(World worldObj, int x, int y, int z)
    {
		if(data==null){
			setData();
		}
		tickSides(worldObj, x, y, z, 10);
    }
    
    public void tickSides(World worldObj, int x, int y, int z, int rate){
    	int[][]sides = {{1,0,0},{-1,0,0},{0,0,1},{0,0,-1},{0,1,0},{0,0,0}};
        for(int i=0;i<sides.length;i++){
        	Vector3 vec = new Vector3(x+sides[i][0], y+sides[i][1], z+sides[i][2]);
        	Block blocki = vec.getBlock(worldObj);
        	if(blocki instanceof BlockFluid && ((BlockFluid)blocki).solidifiable||blocki==ThutBlocks.water)
        	{
        		worldObj.scheduleBlockUpdate(x+sides[i][0], y+sides[i][1], z+sides[i][2],blocki,rate);
        	}
        }
   }
   /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onEntityCollidedWithBlock(World par1World, int par2, int par3, int par4, Entity entity) {
		entity.motionX*=0.5;
		entity.motionZ*=0.5;
		if(par1World.getBlockMetadata(par2, par3, par4)<7)
		entity.motionY*=0.5;
	}
	
	
	public void setData(){
		if(fluidBlocks.get(this)==null)
		{
			FluidInfo info = new FluidInfo();
			HashMap<Block, Block> combinationList = new HashMap<Block, Block>();
			HashMap<Block, Integer> desiccantList = new HashMap<Block, Integer>();

			combinationList.put(ThutBlocks.rebar, ThutBlocks.liquidREConcrete);
			combinationList.put(flowing_water, ThutBlocks.liquidConcrete);
			combinationList.put(water, ThutBlocks.liquidConcrete);
			combinationList.put(air, ThutBlocks.liquidConcrete);
			//Normal Concrete to make this colour
			
			combinationList.put(ThutBlocks.liquidConcrete, ThutBlocks.liquidConcrete);
			
			combinationList.put(ThutBlocks.concrete, ThutBlocks.liquidConcrete);
			//RE Concrete to make this colour
			combinationList.put(ThutBlocks.liquidREConcrete, ThutBlocks.liquidREConcrete);
			combinationList.put(ThutBlocks.reConcrete, ThutBlocks.liquidREConcrete);
	
	
			desiccantList.put(air, hardenRate);
			desiccantList.put(dirt, hardenRate);
			desiccantList.put(grass, hardenRate);
			desiccantList.put(sand, hardenRate);
	
			desiccantList.put(ThutBlocks.reConcrete, hardenRate*4);
			desiccantList.put(ThutBlocks.misc, hardenRate*4);
			
			desiccantList.put(ThutBlocks.concrete, hardenRate*4);
			
			List<Block> replaces = new ArrayList<Block>();
			replaces.addAll(defaultReplacements);
			
			for(Block b: replaces)
				combinationList.put(b,ThutBlocks.liquidConcrete);
			
			info.viscosity = 0;
			info.desiccants = desiccantList;
			info.combinationBlocks = combinationList;
			info.hardenTo = concrete;
			info.fallOfEdge = true;
			info.hardenDiff = 0;
			
			fluidBlocks.put(ThutBlocks.liquidConcrete,info);
		}
	}
	
    @Override
    public int quantityDropped(int meta, int fortune, Random random)
    {
        return 0;
    }
    
	
	@SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister par1IconRegister)
    {
        this.iconArray = new IIcon[16];
        this.blockIcon = par1IconRegister.registerIcon("concrete:" + "wetConcrete_"+8);
        for (int i = 0; i < this.iconArray.length; ++i)
        {
            this.iconArray[i] = par1IconRegister.registerIcon("concrete:" + "wetConcrete_"+i);
        }
    }

		 
}
