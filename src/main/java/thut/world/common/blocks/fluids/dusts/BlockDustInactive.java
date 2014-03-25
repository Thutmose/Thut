package thut.world.common.blocks.fluids.dusts;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import thut.api.ThutBlocks;
import thut.api.maths.Vector3;
import thut.world.common.WorldCore;
import thut.world.common.blocks.fluids.BlockFluid;
import thut.world.common.blocks.fluids.liquids.BlockLava;
import thut.world.common.blocks.world.BlockWorldGen;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.*;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;


public class BlockDustInactive extends BlockFluid
{
	static Integer[][] data;
	private static int thisID;
	long time = 0;
	
	@SideOnly(Side.CLIENT)
	private IIcon iconFloatingDust;

    public BlockDustInactive()
    {
    	super(getFluidType("dust")  ,Material.ground);
		setBlockName("soliddust");
	//	setCreativeTab(ConcreteCore.tabThut);
		setHardness(0.1f);
		setResistance(0.0f);
		ThutBlocks.inactiveDust = this;
		this.setTickRandomly(false);
		this.hasFloatState = true;
    }
  
	public void setData(){
//		if(data==null)
//		{TODO inactive dust combinations
//		List<Integer> combinationList = new ArrayList<Integer>();
//
//		combinationList.add(Blocks.inactiveDust.blockID+4096*Blocks.dust.blockID);
//		combinationList.add(Block.waterStill.blockID+4096*Blocks.dust.blockID);
//		combinationList.add(Block.waterMoving.blockID+4096*Blocks.dust.blockID);
//		combinationList.add(Blocks.dust.blockID+4096*Blocks.dust.blockID);
//		combinationList.add(4096*Blocks.inactiveDust.blockID);
//		
//		for(int i = 0;i<4;i++){
//			combinationList.add(BlockLava.getInstance(i).blockID+4096*BlockLava.getInstance(i).blockID);
//		}
//		
//		List<Integer> replaces = new ArrayList<Integer>();
//		replaces.addAll(defaultReplacements);
//		List<Integer> toRemove = new ArrayList<Integer>();
//		for(Integer i:replaces)
//		{
//			if(blocksList[i] instanceof BlockLeaves)
//				toRemove.add(i);
//		}
//		replaces.removeAll(toRemove);
//		Integer n = 30;
//		replaces.remove(n);
//		
//		data = new Integer[][]{
//				{
//					0,//ID that this returns when meta hits -1, 
//					2,//the viscosity factor,
//					this.thisID,//a secondary ID that this can turn into used for hardening,
//					1,//The hardening differential that prevents things staying liquid forever.,
//					0,//a randomness coefficient, this is multiplied by a random 0-10 then added to the hardening differential and viscosity.,
//					0,//The will fall of edges factor, this is 0 or 1,
//					0,//0 = not colourable, 1 = colourable.
//					1,//1 = replaces air, 0= doesn't replace air
//				},
//				{},
//				combinationList.toArray(new Integer[0]),
//				replaces.toArray(new Integer[0])
//			};
//			fluidBlocks.put(this.thisID,data);
//		}
	}
    
//	public int tickRate(World worldObj)
//	{
//		return 4000;
//	}
//    
    ///////////////////////////////////////////////////////////////////Block Ticking Stuff Above Here///////////////////////////////////////
    
    @SideOnly(Side.CLIENT)

    /**
     * When this method is called, your block should register all the icons it needs with the given IIconRegister. This
     * is the only chance you get to register icons.
     */
    public void registerBlockIcons(IIconRegister par1IIconRegister)
    {
        this.blockIcon = par1IIconRegister.registerIcon(WorldCore.TEXTURE_PATH + "dust");
        this.iconFloatingDust = par1IIconRegister.registerIcon(WorldCore.TEXTURE_PATH + "dustCloud");
    }
    
    @Override
    @SideOnly(Side.CLIENT)

    /**
     * Retrieves the block texture to use based on the display side. Args: iBlockAccess, x, y, z, side
     */
    public IIcon getIcon(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
    {
            Material material = par1IBlockAccess.getBlock(par2, par3 - 1, par4).getMaterial();
            Block id = par1IBlockAccess.getBlock(par2, par3 - 1, par4);
            int meta = par1IBlockAccess.getBlockMetadata(par2, par3 - 1, par4);

            return isFloating(par1IBlockAccess, par2, par3, par4)? this.iconFloatingDust : this.blockIcon;
            
    }
    
//    /**
//     * Returns the ID of the items to drop on destruction.
//     */TODO drop
//    @Override
//    public Item itemDropped(Block par1, Random par2Random, int par3)
//    {
//        return ThutItems.dust;
//    }
    
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
    {
    	if(this.isFloating(par1World, par2, par3, par4))
    	{
    		par1World.setBlockToAir(par2, par3, par4);
    		return null;
    	}
    	int meta = par1World.getBlockMetadata(par2, par3, par4);
        int l = 15-par1World.getBlockMetadata(par2, par3, par4);
        Block block = par1World.getBlock(par2, par3 - 1, par4);
        float f = 0.0625F;
        if(!(new Vector3(par2,par3-1,par4).isFluid(par1World)||
        		par1World.isAirBlock(par2, par3-1, par4)||(block instanceof BlockFluid&&meta!=0))){
        return AxisAlignedBB.getAABBPool().getAABB((double)par2 + this.minX, (double)par3 + this.minY, (double)par4 + this.minZ, (double)par2 + this.maxX, (double)((float)par3 + (float)l * f), (double)par4 + this.maxZ);
        }
        else{
        	return AxisAlignedBB.getAABBPool().getAABB(0, 0, 0, 0, 0, 0);
        }
    }
    
    /**
     * Adds all intersecting collision boxes to a list. (Be sure to only add boxes to the list if they intersect the
     * mask.) Parameters: World, X, Y, Z, mask, list, colliding entity
     */
	  @Override
    public void addCollisionBoxesToList(World worldObj, int x, int y, int z, AxisAlignedBB aaBB, List list, Entity par7Entity)
    {
		if(hasFloatState && isFloating(worldObj, x, y, z)) return;
		Vector3 vec1 = new Vector3(x,y-1,z);
		if(vec1.isFluid(worldObj)&&vec1.getBlock(worldObj)!=this) 
		{
			return;
		}
		for(AxisAlignedBB box : getBoxes(worldObj, x, y, z))
		{
			if(aaBB.intersectsWith(box))
				list.add(box);
		}
    }
	  
    /**
     * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
     * their own) Args: x, y, z, neighbor blockID
     */
    public void onNeighborBlockChange(World worldObj, int x, int y, int z, Block id) 
    {
    	this.setSolid();
		if(id!=this)
		{
			worldObj.setBlock(x, y, z, ThutBlocks.dust, worldObj.getBlockMetadata(x, y, z), 3);
		}
    }
    
	@Override
	public void updateTick(World worldObj, int x, int y, int z, Random r)
	{
//		int meta = worldObj.getBlockMetadata(x, y, z);
//
//		int idUp = worldObj.getBlock(x, y+1, z);
//		int metaUp = worldObj.getBlockMetadata(x, y+1, z);
//		if(meta==15)
//		worldObj.scheduleBlockUpdate(x, y, z, idUp, 5);
	}
    
    ////////////////////////////////////////////Plant stuff////////////////////////////////////////////////////////////////
    

    /**
     * Determines if this block can support the passed in plant, allowing it to be planted and grow.
     * Some examples:
     *   Reeds check if its a reed, or if its sand/dirt/grass and adjacent to water
     *   Cacti checks if its a cacti, or if its sand
     *   Nether types check for soul sand
     *   Crops check for tilled soil
     *   Caves check if it's a colid surface
     *   Plains check if its grass or dirt
     *   Water check if its still water
     *
     * @param world The current world
     * @param x X Position
     * @param y Y Position
     * @param z Z position
     * @param direction The direction relative to the given position the plant wants to be, typically its UP
     * @param plant The plant that wants to check
     * @return True to allow the plant to be planted/stay.
     */
    public boolean canSustainPlant(World world, int x, int y, int z, ForgeDirection direction, IPlantable plant)
    {
       return world.getBlockMetadata(x, y, z)==15;
    }

    /**
     * Checks if this soil is fertile, typically this means that growth rates
     * of plants on this soil will be slightly sped up.
     * Only vanilla case is tilledField when it is within range of water.
     *
     * @param world The current world
     * @param x X Position
     * @param y Y Position
     * @param z Z position
     * @return True if the soil should be considered fertile.
     */
    public boolean isFertile(World world, int x, int y, int z)
    {
    	 return world.getBlockMetadata(x, y, z)==15;
    }
    
}