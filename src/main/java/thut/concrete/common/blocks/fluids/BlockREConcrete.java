package thut.concrete.common.blocks.fluids;

import static thut.api.ThutBlocks.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import thut.api.blocks.IRebar;
import thut.api.render.RenderRebar;
import thut.concrete.client.render.RenderFluid;
import thut.concrete.common.ConcreteCore;
import thut.core.common.blocks.BlockFluid;
import thut.core.common.blocks.BlockFluid.FluidInfo;
import thut.core.common.blocks.tileentity.TileEntityBlockFluid;
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

public class BlockREConcrete extends BlockFluid implements IRebar, ITileEntityProvider//, IAntiPoisonBlock
{
	
//	public static BlockREConcrete instance;
	
	public int colourid;
	public static int resistance = 100;
	public static float hardness = 100;
	public static ConcurrentHashMap<String, Byte> metaData = new ConcurrentHashMap<String, Byte>();
	Integer[][] data;
	boolean[] side = new boolean[6];

	public BlockREConcrete() {
		super(new Fluid("solidRock").setDensity(Integer.MAX_VALUE).setViscosity(
				Integer.MAX_VALUE) ,Material.rock);
		setBlockName("REconcrete");
		setCreativeTab(ConcreteCore.tabThut);
		this.setTickRandomly(true);
		this.rate = 1;
		reConcrete = this;
		this.placeamount = 16;
		this.setStepSound(soundTypeStone);
		this.setResistance(resistance);
		setSolid();
		this.fluid = false;
		this.stampable = true;
		setData();
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
	
	
	//*
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
		
    	AxisAlignedBB aabb = this.getCollisionBoundingBoxFromPool(worldObj, x, y, z);
    	if (aaBB.intersectsWith(aabb))
            list.add(aabb);
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
    	this.setResistanceByMeta(par1IBlockAccess.getBlockMetadata(x, y, z));
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
	public void updateTick(World worldObj, int x, int y, int z, Random par5Random){
		
//		
//		int meta = worldObj.getBlockMetadata(x, y, z);
//		
//		if(meta<10)
//		{
//			for(int i=0;i>meta-5;i++)
//			{
//				if(Math.random()>(1-SOLIDIFY_CHANCE*100))
//				{
//					 worldObj.setBlock(x, y, z, 0, 0, 3);
//				}
//			}
//		}
		
	}
	
    /**
     * Called when this block is set (with meta data).
     */
    public void onSetBlockIDWithMetaData(World worldObj, int x, int y, int z, int meta) 
    {
//		if(meta==0)
//		{
//			worldObj.scheduleBlockUpdate(x, y, z, blockID, 1);
//		}
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
	public IIcon getIcon(Block block) {
		return this.blockIcon;
	}
	
	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
	    return new TileEntityBlockFluid();
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
