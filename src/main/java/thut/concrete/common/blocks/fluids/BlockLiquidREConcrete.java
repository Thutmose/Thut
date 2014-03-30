package thut.concrete.common.blocks.fluids;

import static net.minecraftforge.common.util.ForgeDirection.*;
import static thut.api.ThutBlocks.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import thut.api.blocks.*;
import thut.api.render.RenderRebar;
import thut.concrete.client.render.RenderFluid;
import thut.core.common.blocks.BlockFluid;
import thut.core.common.blocks.BlockFluid.FluidInfo;
//import atomicscience.api.IAntiPoisonBlock;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;

public class BlockLiquidREConcrete extends BlockFluid implements IRebar//, IAntiPoisonBlock
{
	public int colourid;
	static Material wetConcrete = (new WetRock(MapColor.stoneColor));
	Integer[][] data;
	boolean[] side = new boolean[6];
    @SideOnly(Side.CLIENT)
    public IIcon theIcon;
    public boolean solidifiable;
    
    public BlockLiquidREConcrete() {
		super(new Fluid("Reconcrete").setDensity(Integer.MAX_VALUE)
				.setViscosity(2000), Material.iron);
		setBlockName("REconcreteLiquid");
		this.setResistance((float) 10.0);
		this.setHardness((float) 10.0);
	//	this.instance = this;
		liquidREConcrete = this;
		this.setTemperature(310);
		this.solidifiable = true;
		this.setTickRandomly(true);
	}
	
	  @Override
	    public boolean isNormalCube(IBlockAccess world, int x, int y, int z)
	    {
	        return false;
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
        return  1;
    }

	public void setData(){
		if(fluidBlocks.get(this)==null)
		{
			FluidInfo info = new FluidInfo();
			HashMap<Block, Block> combinationList = new HashMap<Block, Block>();
			HashMap<Block, Integer> desiccantList = new HashMap<Block, Integer>();

			//Rebar
			combinationList.put(rebar, liquidREConcrete);
			
			//RE Concrete to make this colour
			combinationList.put(liquidREConcrete, liquidREConcrete);
			combinationList.put(reConcrete, liquidREConcrete);
		
			int rate = Math.max(BlockLiquidConcrete.hardenRate,1);
			
			desiccantList.put(air, rate);
			desiccantList.put(dirt, rate);
			desiccantList.put(grass, rate);
			desiccantList.put(sand, rate);
	
			desiccantList.put(reConcrete, rate*3);
			desiccantList.put(misc, rate*3);
		
			desiccantList.put(concrete, rate);
		
			info.returnTo = rebar;
			info.hardenTo = reConcrete;
			info.viscosity = 0;
			info.hardenDiff = 1;
			info.randomFactor = 0;
			info.combinationBlocks = combinationList;
			info.desiccants = desiccantList;
		
			fluidBlocks.put(liquidREConcrete,info);
		}
	}

	/////////////////////////////////////////Block Bounds Stuff//////////////////////////////////////////////////////////
    /**
     * Adds all intersecting collision boxes to a list. (Be sure to only add boxes to the list if they intersect the
     * mask.) Parameters: World, X, Y, Z, mask, list, colliding entity
     */
	@Override
    public void addCollisionBoxesToList(World worldObj, int x, int y, int z, AxisAlignedBB aaBB, List list, Entity par7Entity)
    {
		side = sides(worldObj,x,y,z);
		
		if(!(side[0]||side[1]||side[2]||side[3]||side[4]||side[5]))
			side = new boolean[] {true, true, true, true, false, false};
		
    	AxisAlignedBB aabb;
    	int n = 5;
    	
        for (ForgeDirection fside : ForgeDirection.VALID_DIRECTIONS)
        {
                AxisAlignedBB coll = getBoundingBoxForSide(fside).offset(x, y, z);
                if (aaBB.intersectsWith(coll)&&this.side[n])
                        list.add(coll);
                n--;
        }

		
    }

	

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int x, int y, int z)
    {
		side = sides(par1IBlockAccess,x,y,z);
		
		if(!(side[0]||side[1]||side[2]||side[3]||side[4]||side[5]))
			side = new boolean[] {true, true, true, true, false, false};
		setBlockBounds(0.35F, 0.35F, 0.35F, 0.65F, 0.65F, 0.65F);

    	this.setBoundsByMeta(par1IBlockAccess.getBlockMetadata(x, y, z));
    }

    public AxisAlignedBB getBoundingBoxForSide(ForgeDirection fside)
    {
            switch (fside)
            {
                    case UP:
                    {
                            return AxisAlignedBB.getBoundingBox(0.35F, 0.4F, 0.35F, 0.65F, 1F, 0.65F);
                    }
                    case DOWN:
                    {
                            return AxisAlignedBB.getBoundingBox(0.35F, 0.0F, 0.35F, 0.65F, 0.6F, 0.65F);
                    }
                    case NORTH:
                    {
                            return AxisAlignedBB.getBoundingBox(0.35F, 0.35F, 0.0F, 0.65F, 0.65F, 0.6F);
                    }
                    case SOUTH:
                    {
                            return AxisAlignedBB.getBoundingBox(0.35F, 0.35F, 0.4F, 0.65F, 0.65F, 1F);
                    }
                    case EAST:
                    {
                            return AxisAlignedBB.getBoundingBox(0.4F, 0.35F, 0.35F, 1F, 0.65F, 0.65F);
                    }
                    case WEST:
                    {
                            return AxisAlignedBB.getBoundingBox(0.0F, 0.35F, 0.35F, 0.60F, 0.65F, 0.65F);
                    }
                    default:
                    {
                            return AxisAlignedBB.getBoundingBox(0f, 0f, 0f, 1f, 1f, 1f);
                    }
            }
    }
    
    private void setBlockBoundsForSide(int x, int y, int z, ForgeDirection side)
    {
            switch (side)
	        {
		            case UP:
		            {
		                    setBlockBounds(0.35F, 0.4F, 0.35F, 0.65F, 1F, 0.65F);
		                    break;
		            }
		            case DOWN:
		            {
		                    setBlockBounds(0.35F, 0.0F, 0.35F, 0.65F, 0.6F, 0.65F);
		                    break;
		            }
		            case NORTH:
		            {
		                    setBlockBounds(0.35F, 0.35F, 0.0F, 0.65F, 0.65F, 0.6F);
		                    break;
		            }
		            case SOUTH:
		            {
		                    setBlockBounds(0.35F, 0.35F, 0.4F, 0.65F, 0.65F, 1F);
		                    break;
		            }
		            case EAST:
		            {
		                    setBlockBounds(0.4F, 0.35F, 0.35F, 1F, 0.65F, 0.65F);
		                    break;
		            }
		            case WEST:
		            {
		                    setBlockBounds(0.0F, 0.35F, 0.35F, 0.60F, 0.65F, 0.65F);
		                    break;
		            }
		            default:
		            {
		                    setBlockBounds(0f, 0f, 0f, 1f, 1f, 1f);
		                    break;
		            }
            }
    }
    
    
    
	
	
   /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;
	@SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon("concrete:wetConcrete_"+8);
    	this.theIcon = par1IconRegister.registerIcon("concrete:" + "rebar");
    	this.iconArray = new IIcon[16];
    	for (int i = 0; i < this.iconArray.length; ++i)
        {
            this.iconArray[i] = par1IconRegister.registerIcon("concrete:" + "wetConcrete_"+i);
        }
    	
    }

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
	public IIcon getIcon(Block block) {
		return this.blockIcon;
	}
	 /**
     * The type of render function that is called for this block
     */
    @Override
    public int getRenderType()
    {
        return RenderFluid.ID;
    }

	@Override
	public boolean[] getInventorySides() {
		// TODO Auto-generated method stub
		return null;
	}
	 
}