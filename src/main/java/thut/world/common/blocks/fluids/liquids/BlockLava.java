package thut.world.common.blocks.fluids.liquids;

import static net.minecraftforge.common.util.ForgeDirection.DOWN;
import static net.minecraftforge.common.util.ForgeDirection.EAST;
import static net.minecraftforge.common.util.ForgeDirection.NORTH;
import static net.minecraftforge.common.util.ForgeDirection.SOUTH;
import static net.minecraftforge.common.util.ForgeDirection.UP;
import static net.minecraftforge.common.util.ForgeDirection.WEST;
import static thut.api.ThutBlocks.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import thut.api.ThutBlocks;
import thut.api.maths.Vector3;
import thut.core.common.blocks.BlockFluid;
import thut.world.common.Volcano;
import thut.world.common.WorldCore;
import thut.world.common.blocks.fluids.dusts.BlockDust;
import thut.world.common.blocks.fluids.solids.BlockSolidLava;
import thut.world.common.blocks.tileentity.TileEntityVolcano;
import thut.world.common.blocks.world.BlockVolcano;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;

public class BlockLava extends BlockFluid //implements IHeatSource
	{
	public int typeid;
	Integer[][] data;
	public static int HardenRate;
	private long time = 0;
	
	public static String[] types = 
		{
			"mafic",
			"intermediate",
			"felsic",
			"",
		};

	@SideOnly(Side.CLIENT)
	private IIcon iconFloating;
	
	public BlockLava(int x) {
		super(getLava(x), Material.lava);
		typeid = x;
		if(typeid!=3)
			setCreativeTab(WorldCore.tabThut);
		this.setTemperature(1300);
		setBlockName("Lava" + typeid);
		this.setResistance((float) 5.0);
		this.rate = 0.9;
		this.placeamount = 16;
		this.solidifiable = true;
		ThutBlocks.lavas[typeid] = this;
		this.hasFloatState = true;
		this.setTickRandomly(true);
	}
	
	public static Fluid getLava(int type)
	{
		return (new Fluid(types[type]+" lava")).setDensity(5000).setViscosity(2500);
	}
	 
    @Override
    public void onBlockAdded(World worldObj, int x, int y, int z)
    {
    	TileEntityVolcano.setBiome(worldObj, x, z, WorldCore.volcano);
		tickSides(worldObj, x, y, z, 10);
    }
	
    @SideOnly(Side.CLIENT)

    /**
     * A randomly called display update to be able to add particles or other items for display
     */
    public void randomDisplayTick(World par1World, int par2, int par3, int par4, Random par5Random)
    {
    	if(!isFloating(par1World, par2, par3, par4))
    		Blocks.lava.randomDisplayTick(par1World, par2, par3, par4, par5Random);
    }
	
    /**
     * Returns whether this block is collideable based on the arguments passed in Args: blockMetaData, unknownFlag
     */
    public boolean canCollideCheck(int par1, boolean par2)
    {
        return false;
    }
    
    /**
     * Adds all intersecting collision boxes to a list. (Be sure to only add boxes to the list if they intersect the
     * mask.) Parameters: World, X, Y, Z, mask, list, colliding entity
     */
	  @Override
    public void addCollisionBoxesToList(World worldObj, int x, int y, int z, AxisAlignedBB aaBB, List list, Entity par7Entity)
    {
		  
    }
	
    /**
     * Returns a bounding box from the pool of bounding boxes (this means this box can change after the pool has been
     * cleared to be reused)
     */
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
    {
        return null;
    }

	public static BlockLava getInstance(int colorid)
	{
		return (BlockLava) ThutBlocks.lavas[colorid];
	}

    public boolean isBlockNormalCube(World world, int x, int y, int z)
    {
        return false;
    }
	public void setData()
	{
		if(fluidBlocks.get(getInstance(typeid))==null)
		{	
			FluidInfo info = new FluidInfo();
		HashMap<Block, Block> combinationList = new HashMap<Block, Block>();
		HashMap<Block, Integer> desiccantList = new HashMap<Block, Integer>();
		combinationList.put(air, BlockLava.getInstance(typeid));

		combinationList.put(flowing_water, BlockLava.getInstance(typeid));
		combinationList.put(water, BlockLava.getInstance(typeid));
		combinationList.put(lava, BlockLava.getInstance(typeid));
		combinationList.put(flowing_lava, BlockLava.getInstance(typeid));
		
		for(int i = 0;i<4;i++){
			combinationList.put(BlockLava.getInstance(i), BlockLava.getInstance(typeid));
			combinationList.put(BlockSolidLava.getInstance(i), BlockLava.getInstance(typeid));
		}

		//combinationList.put(Blocks.sulfur, BlockLava.getInstance(typeid));
		
		combinationList.put(dust, BlockLava.getInstance(typeid));
		
		
		int rate = 10*HardenRate*(1+typeid);
		
		desiccantList.put(air, (typeid==3?rate:1));
		desiccantList.put(dirt, rate);
		desiccantList.put(grass, rate);
		desiccantList.put(sand, rate);
		desiccantList.put(sandstone, rate);
		desiccantList.put(gravel, rate);
		desiccantList.put(stone, rate);
		desiccantList.put(water, 100*rate);
		desiccantList.put(flowing_water, 100*rate);
		desiccantList.put(worldGen, 20*rate);
		
		for(int i=0;i<3;i++){
			desiccantList.put(BlockSolidLava.getInstance(i), rate*20);
		}
		
		//ORDER HERE MATTERS
		if(typeid == 0)
		{
			info.hardenDiff = 1;
			info.viscosity = 0;
		}
		else if(typeid==1)
		{
			info.hardenDiff = 2;
			info.viscosity = 1;
		}
		else if(typeid==2)
		{
			info.hardenDiff = 2;
			info.viscosity = 4;
		}
		
		info.hardenTo = BlockSolidLava.getInstance(typeid); //Add harden to
		info.randomFactor = (typeid==0?2:10); //Add random Factor
		info.fallOfEdge = true; //Make this a fluid
		

		
		List<Block> replaces = new ArrayList<Block>();
		replaces.addAll(defaultReplacements);
		replaces.add(log);
		replaces.add(log2);
		replaces.add(pumpkin);
		replaces.add(cactus);
		for(Block b : getAllBlocks())
		{
			if(b.getMaterial() == Material.plants)
				if(!replaces.contains(b))
					replaces.add(b);
			if(b.getMaterial() == Material.cactus)
				if(!replaces.contains(b))
					replaces.add(b);
			if(b.getMaterial() == Material.cloth)
				if(!replaces.contains(b))
					replaces.add(b);
			if(b.getMaterial() == Material.snow)
				if(!replaces.contains(b))
					replaces.add(b);
		}

		for(Block b: replaces)
			combinationList.put(b,BlockLava.getInstance(typeid));
		
		info.combinationBlocks = combinationList;
		info.desiccants = desiccantList;
		
		fluidBlocks.put(BlockLava.getInstance(typeid), info);
		}
	}
	/////////////////////////////////////////////////////////Lighting stuff//////////////////////////////////////////////////////////////
    /**
     * Get a light value for the block at the specified coordinates, normal ranges are between 0 and 15
     *
     * @param world The current world
     * @param x X Position
     * @param y Y position
     * @param z Z position
     * @return The light value
     */
    public int getLightValue(IBlockAccess world, int x, int y, int z)
    {
    	return 0;//world.getBlock(x, y-4, z)==blockID&&!isFloating(world, x, y, z)?15:0;//isFloating(world,x,y,z)?0:15-world.getBlockMetadata(x, y, z);
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

            return isFloating(par1IBlockAccess, par2, par3, par4)? this.iconFloating : this.blockIcon;
            
    }
    @SideOnly(Side.CLIENT)
    /**
     * Goes straight to getLightBrightnessForSkyBlocks for Blocks, does some fancy computing for Fluids
     */
    public int getMixedBrightnessForBlock(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
    {
        return 15728848;
    } 
    
   /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void updateTick(World worldObj, int x, int y, int z, Random par5Random){

		worldObj.theProfiler.startSection("Lava Blocks");
		Vector3 vec = new Vector3(x,y,z);
		
		doFluidTick(worldObj, vec);

		doHardenTick(worldObj, vec);
		
		doFireTick(worldObj, x, y, z, par5Random);
		worldObj.theProfiler.endSection();
	}
	
    public void tickSides(World worldObj, int x, int y, int z, int rate){
    	int[][]sides = {{1,0,0},{-1,0,0},{0,0,1},{0,0,-1},{0,1,0},{0,0,0}};
        for(int i=0;i<sides.length;i++){
        	Vector3 vec = new Vector3(x+sides[i][0], y+sides[i][1], z+sides[i][2]);
        	Block blocki = vec.getBlock(worldObj);
        	if(blocki instanceof BlockFluid && ((BlockFluid)blocki).solidifiable||blocki==water)
        	{
        		worldObj.scheduleBlockUpdate(x+sides[i][0], y+sides[i][1], z+sides[i][2],blocki,rate);
        	}
        }
   }
	
	@Override
	public void doHardenTick(World worldObj, Vector3 vec)
	{
		
		Vector3 down = vec.offset(DOWN);
		
		Block below = down.getBlock(worldObj);
		int meta = down.getBlockMetadata(worldObj);
		
		if(below==grass)
		{
			down.setBlock(worldObj, dirt,0,3);
		}
		
		if(down.getBlock(worldObj) instanceof BlockFluid && meta !=15||below == ThutBlocks.volcano)
		{
			return;
		}
		
		if(below == water||below == flowing_water||down.isAir(worldObj)||below == this)
		{
			return;
		}
		
		{
			if(!merge(worldObj, vec, down))
				vec.setBlock(worldObj, ThutBlocks.solidLavas[typeid], vec.getBlockMetadata(worldObj));
				//super.doHardenTick(worldObj, vec);
			return;
		}
		
	}
	
	@SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister par1IIconRegister)
    {
        this.blockIcon = par1IIconRegister.registerIcon(WorldCore.TEXTURE_PATH+"lava");
        this.iconFloating = par1IIconRegister.registerIcon(WorldCore.TEXTURE_PATH + "floatingLava");
    }
	

	

    @Override
    public int quantityDropped(int meta, int fortune, Random random)
    {
        return 0;
    }
    
    
    
    private void tryToCatchBlockOnFire(World par1World, int x, int par3, int par4, int par5, Random par6Random, int par7, ForgeDirection face)
    {
        int j1 = 0;
        Block block = par1World.getBlock(x, par3, par4);
        if (block != null)
        {
            j1 = block.getFlammability(par1World, x, par3, par4, face);
        }

        if (par6Random.nextInt(par5) < j1)
        {
            boolean flag = par1World.getBlock(x, par3, par4) == tnt;

            if (par6Random.nextInt(par7 + 10) < 5 && !par1World.canLightningStrikeAt(x, par3, par4))
            {
                int k1 = par7 + par6Random.nextInt(5) / 4;

                if (k1 > 15)
                {
                    k1 = 15;
                }

                par1World.setBlock(x, par3, par4, fire, k1, 3);
            }
            else
            {
                par1World.setBlockToAir(x, par3, par4);
            }

            if (flag)
            {
                tnt.onBlockDestroyedByPlayer(par1World, x, par3, par4, 1);
            }
        }
    }
    
    
    
    
    /**
     * Returns true if at least one block next to this one can burn.
     */
    private boolean canNeighborBurn(World par1World, int par2, int par3, int par4)
    {
        return canBlockCatchFire(par1World, par2 + 1, par3, par4, WEST ) ||
               canBlockCatchFire(par1World, par2 - 1, par3, par4, EAST ) ||
               canBlockCatchFire(par1World, par2, par3 - 1, par4, UP   ) ||
               canBlockCatchFire(par1World, par2, par3 + 1, par4, DOWN ) ||
               canBlockCatchFire(par1World, par2, par3, par4 - 1, SOUTH) ||
               canBlockCatchFire(par1World, par2, par3, par4 + 1, NORTH);
    }

    /**
     * Gets the highest chance of a neighbor block encouraging this block to catch fire
     */
    private int getChanceOfNeighborsEncouragingFire(World par1World, int par2, int par3, int par4)
    {
        byte b0 = 0;

        if (!par1World.isAirBlock(par2, par3, par4))
        {
            return 0;
        }
        else
        {
            int l = this.getChanceToEncourageFire(par1World, par2 + 1, par3, par4, b0, WEST);
            l = this.getChanceToEncourageFire(par1World, par2 - 1, par3, par4, l, EAST);
            l = this.getChanceToEncourageFire(par1World, par2, par3 - 1, par4, l, UP);
            l = this.getChanceToEncourageFire(par1World, par2, par3 + 1, par4, l, DOWN);
            l = this.getChanceToEncourageFire(par1World, par2, par3, par4 - 1, l, SOUTH);
            l = this.getChanceToEncourageFire(par1World, par2, par3, par4 + 1, l, NORTH);
            return l;
        }
    }

    /**
     * Side sensitive version that calls the block function.
     * 
     * @param world The current world
     * @param x X Position
     * @param y Y Position
     * @param z Z Position
     * @param face The side the fire is coming from
     * @return True if the face can catch fire.
     */
    public boolean canBlockCatchFire(IBlockAccess world, int x, int y, int z, ForgeDirection face)
    {
        Block block = world.getBlock(x, y, z);
        if (block != null)
        {
            return block.isFlammable(world, x, y, z, face);
        }
        return false;
    }

    /**
     * Side sensitive version that calls the block function.
     * 
     * @param world The current world
     * @param x X Position
     * @param y Y Position
     * @param z Z Position
     * @param oldChance The previous maximum chance.
     * @param face The side the fire is coming from
     * @return The chance of the block catching fire, or oldChance if it is higher
     */
    public int getChanceToEncourageFire(World world, int x, int y, int z, int oldChance, ForgeDirection face)
    {
        int newChance = 0;
        Block block = world.getBlock(x, y, z);
        if (block != null)
        {
            newChance = block.getFireSpreadSpeed(world, x, y, z, face);
        }
        return (newChance > oldChance ? newChance : oldChance);
    }
    
    public void onEntityCollidedWithBlock(World worldObj,int x,int y, int z, Entity entity)
    {
    	if(entity instanceof EntityItem)
    	{
    		entity.setDead();
    	}
    }
    
    
    public void doFireTick(World worldObj, int x, int y, int z, Random par5Random)
    {
		  if (worldObj.getGameRules().getGameRuleBooleanValue("doFireTick"))
	        {
	            Block base = worldObj.getBlock(x, y - 1, z);
	            boolean flag = (base != null && base.isFireSource(worldObj, x, y - 1, z, UP));

              int l = 15-worldObj.getBlockMetadata(x, y, z);

            
              boolean flag1 = worldObj.isBlockHighHumidity(x, y, z);
              byte b0 = 0;

              if (flag1)
              {
                  b0 = -50;
              }

              this.tryToCatchBlockOnFire(worldObj, x + 1, y, z, 300 + b0, par5Random, l, WEST );
              this.tryToCatchBlockOnFire(worldObj, x - 1, y, z, 300 + b0, par5Random, l, EAST );
              this.tryToCatchBlockOnFire(worldObj, x, y - 1, z, 250 + b0, par5Random, l, UP   );
              this.tryToCatchBlockOnFire(worldObj, x, y + 1, z, 250 + b0, par5Random, l, DOWN );
              this.tryToCatchBlockOnFire(worldObj, x, y, z - 1, 300 + b0, par5Random, l, SOUTH);
              this.tryToCatchBlockOnFire(worldObj, x, y, z + 1, 300 + b0, par5Random, l, NORTH);

              for (int i1 = x - 1; i1 <= x + 1; ++i1)
              {
                  for (int j1 = z - 1; j1 <= z + 1; ++j1)
                  {
                      for (int k1 = y - 1; k1 <= y + 4; ++k1)
                      {
                          if (i1 != x || k1 != y || j1 != z)
                          {
                              int l1 = 100;

                              if (k1 > y + 1)
                              {
                                  l1 += (k1 - (y + 1)) * 100;
                              }

                              int i2 = this.getChanceOfNeighborsEncouragingFire(worldObj, i1, k1, j1);

                              if (i2 > 0)
                              {
                                  int j2 = (i2 + 40 + worldObj.difficultySetting.ordinal() * 7) / (l + 30);

                                  if (flag1)
                                  {
                                      j2 /= 2;
                                  }

                                  if (j2 > 0 && par5Random.nextInt(l1) <= j2 && (!worldObj.isRaining() || !worldObj.canLightningStrikeAt(i1, k1, j1)) && !worldObj.canLightningStrikeAt(i1 - 1, k1, z) && !worldObj.canLightningStrikeAt(i1 + 1, k1, j1) && !worldObj.canLightningStrikeAt(i1, k1, j1 - 1) && !worldObj.canLightningStrikeAt(i1, k1, j1 + 1))
                                  {
                                      int k2 = l + par5Random.nextInt(5) / 4;

                                      if (k2 > 15)
                                      {
                                          k2 = 15;
                                      }

                                      worldObj.setBlock(i1, k1, j1, fire, k2, 3);
                                  }
                              }
                          }
                      }
                  }
              }
	        }
		
    }



//	@Override
//	public float getTemperature() {
//		return 1400;
//	}
//
//
//	@Override
//	public void setTemperature(float celsius) {
//		// TODO Auto-generated method stub
//		
//	}
    
	}


