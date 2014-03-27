package thut.world.common.blocks.fluids.solids;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import thut.api.ThutBlocks;
import thut.api.ThutItems;
import thut.api.maths.Vector3;
import thut.core.client.render.RenderFluid;
import thut.core.common.blocks.BlockFluid;
import thut.world.common.WorldCore;
import thut.world.common.blocks.fluids.liquids.BlockLava;
import thut.world.common.corehandlers.ConfigHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.oredict.OreDictionary;

public class BlockSolidLava extends BlockFluid
{
	public HashMap<Block, Integer> dropChances = new HashMap<Block, Integer>();
	public int typeid;
	public static int resistance = 5;
	public static float hardness = 1;
	public static double oreProb = 0;
	Integer[][] data;
	ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
	
	public BlockSolidLava(int par2) {
		super(getFluidType("solidRock") ,Material.rock);
		typeid = par2;
		setBlockName("solidLava" + typeid);
		this.rate = 1;
		if(typeid!=3)
			setCreativeTab(WorldCore.tabThut);
		ThutBlocks.solidLavas[typeid] = this;
		this.setTickRandomly(false);
		this.setStepSound(soundTypeStone);
		this.placeamount = 1;
		setSolid();
	}

	public static BlockSolidLava getInstance(int colorid)
	{
		return (BlockSolidLava) ThutBlocks.solidLavas[colorid];
	}
	
	public void setData()
	{
		if(!fluidBlocks.containsKey(BlockSolidLava.getInstance(typeid)))
		{
			fluidBlocks.put(BlockSolidLava.getInstance(typeid), new FluidInfo());
			FluidInfo info = fluidBlocks.get(BlockSolidLava.getInstance(typeid));
			info.viscosity = 15;
			info.randomFactor = 15;
			info.fallOfEdge = false;
			info.combinationBlocks.put(BlockSolidLava.getInstance(typeid), BlockSolidLava.getInstance(typeid));
		}
	}
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
    {
		super.setBlockBoundsBasedOnState(par1IBlockAccess, par2, par3, par4);
    	this.setBoundsByMeta(par1IBlockAccess.getBlockMetadata(par2, par3, par4));
    	this.setResistanceByMeta(par1IBlockAccess.getBlockMetadata(par2, par3, par4));
    }
	
	@Override
    public float getExplosionResistance(Entity par1Entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ)
    {
        return getBlastResistanceByMeta(world.getBlockMetadata(x, y, z));
    }
	

	@Override
	@SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister par1IconRegister)
    {
		this.blockIcon = par1IconRegister.registerIcon(WorldCore.TEXTURE_PATH+"solidLava" + typeid);
    }
	
	@Override
    /**
     * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
     * their own) Args: x, y, z, neighbor blockID
     */
    public void onNeighborBlockChange(World worldObj, int x, int y, int z, Block id) 
    {
    	if(!(id==Blocks.flowing_lava||id==Blocks.lava)) return;
		int meta = worldObj.getBlockMetadata(x, y, z);
		if(meta==15) return;
		if(id==Blocks.flowing_lava||id==Blocks.lava)
		{
			if(meta==0) 
				(new Vector3(x,y,z)).setAir(worldObj);
			else
				(new Vector3(x,y,z)).setBlock(worldObj, BlockLava.getInstance(typeid));
		}
	
    }
	
	@Override
	public void updateTick(World worldObj, int x, int y, int z, Random par5Random)
	{
		int meta = worldObj.getBlockMetadata(x, y, z);
		if(meta==15) return;
		for(ForgeDirection side :ForgeDirection.VALID_DIRECTIONS)
		{
			Block id = worldObj.getBlock(x+side.offsetX, y+side.offsetY, z+side.offsetZ);
			if(id==Blocks.flowing_lava||id==Blocks.lava)
			{
				if(meta==0) 
					(new Vector3(x,y,z)).setBlock(worldObj, Blocks.air);
				else
				(new Vector3(x,y,z)).setBlock(worldObj, BlockLava.getInstance(typeid));
			}
		}
	}
	
	@Override
    /**
     * This returns a complete list of items dropped from this block.
     *
     * @param world The current world
     * @param x X Position
     * @param y Y Position
     * @param z Z Position
     * @param metadata Current metadata
     * @param fortune Breakers fortune level
     * @return A ArrayList containing all items this block drops
     */
    public ArrayList<ItemStack> getDrops(World worldObj, int x, int y, int z, int metadata, int fortune)
    {
    	if (!worldObj.isRemote)
        {
            ArrayList<ItemStack> items = new ArrayList<ItemStack>();
            ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
            
            if(drops.size()==0)
            {
            	initDrops();
            }
            double rand = Math.random();
            
            if(metadata==15&&rand<oreProb)
            {
            	items = drops;
            }
            else
            {
            	int dropAmount = metadata==15? (int)(Math.random()*(metadata+1)):metadata+1;
            	items.add(new ItemStack(this, dropAmount, 0));
            	items.add(new ItemStack(ThutItems.dust.getItem(), dropAmount, 0));
            }
            
            int i = (new Random()).nextInt(items.size());

            ItemStack item = items.get(i);
            ret.add(item);
            return ret;
        }
        return new ArrayList<ItemStack>();
    }
    
    private void initDrops()
    {
//        for(Integer i : BlockSolidLava.getInstance(typeid).turnto)//TODO get the drops list done
//		{
//			int id = i & 4095;
//			int probability = i>>12;
//			int meta = i>>22;
//			for(int j = 0; j<probability; j++)
//			{
//				drops.add(new ItemStack(Block.blocksList[id], 1, meta));
//			}
//		}
    }
    
    
    /**
     * Drops the block items with a specified chance of dropping the specified items
     */
    public void dropBlockAsItemWithChance(World worldObj, int x, int y, int z, int thismeta, float par6, int par7)
    {
        if (!worldObj.isRemote)
        {
//            ArrayList<ItemStack> items = new ArrayList<ItemStack>();//TODO drops for lava
//            
//            if(drops.size()==0)
//            {
//            	initDrops();
//            }
//            double rand = Math.random();
//            
//            if(thismeta==15&&rand<oreProb)
//            {
//            	items = drops;
//            }
//            else
//            {
//            	int dropAmount = thismeta==15? (int)(Math.random()*(thismeta+1)):thismeta+1;
//            	items.add(new ItemStack(Block.blocksList[blockID], dropAmount, 0));
//            	items.add(new ItemStack(Items.dust.itemID, dropAmount, 0));
//            }
//            
//            int i = (new Random()).nextInt(items.size());
//
//            ItemStack item = items.get(i);
//            
//            this.dropBlockAsItem_do(worldObj, x, y, z, item);
            
        }
    }
    
	public int tickRate(World worldObj)
	{
		return 20;
	}
	
	public void onBlockClicked(World worldObj, int x, int y, int z, EntityPlayer player){
	//	String message = Integer.toString(worldObj.getBlockMetadata(x, y, z));
	//	player.addChatMessage(message);
		this.setResistanceByMeta(worldObj.getBlockMetadata(x, y, z));
	}
	
	protected void setResistanceByMeta(int meta){
		int j = meta;
        float f = (float)((1 + j)) / 16.0F;
        this.setResistance(f*resistance*(1+typeid));
        this.setHardness(f*hardness);
	}
	protected float getBlastResistanceByMeta(int meta){
		int j = meta;
        float f = (float)((1 + j)) / 16.0F;
        return (f*resistance*(1+typeid));
	}
	protected float getHardnessByMeta(int meta){
		int j = meta;
        float f = (float)((1 + j)) / 16.0F;
        return (f*hardness);
	}
	   
	    public boolean checkSides(World worldObj, int x, int y, int z){
	    	int[][]sides = {{1,0,0},{-1,0,0},{0,0,1},{0,0,-1},{0,1,0},{0,-1,0}};
	        for(int i=0;i<6;i++){
	        	int meta = worldObj.getBlockMetadata(x+sides[i][0], y+sides[i][1], z+sides[i][2]);
	        	Block block = worldObj.getBlock(x+sides[i][0], y+sides[i][1], z+sides[i][2]);

	        	if(block instanceof BlockSolidLava && meta!=0)
	        	{
	        		return false;
	        	}
	        	if(block==Blocks.air||block.getMaterial().isLiquid())
	        	{
	        		return false;
	        	}
	        }
	        return true;
	   }
	    
	    /**
	     * Return whether this block can drop from an explosion.
	     */
	    public boolean canDropFromExplosion(Explosion par1Explosion)
	    {
	        return false;
	    }
	    
	    /**
	     * Called when the block is destroyed by an explosion.
	     * Useful for allowing the block to take into account tile entities,
	     * metadata, etc. when exploded, before it is removed.
	     *
	     * @param world The current world
	     * @param x X Position
	     * @param y Y Position
	     * @param z Z Position
	     * @param Explosion The explosion instance affecting the block
	     */
	    public void onBlockExploded(World world, int x, int y, int z, Explosion explosion)
	    {
	        world.setBlockToAir(x, y, z);
	    }
}
