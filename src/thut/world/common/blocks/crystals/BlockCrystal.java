package thut.world.common.blocks.crystals;

import static net.minecraftforge.common.ForgeDirection.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import codechicken.lib.vec.BlockCoord;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import codechicken.multipart.minecraft.McBlockPart;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thut.api.utils.Vector3;
import thut.world.client.ClientProxy;
import thut.world.client.render.RenderCrystals;
import thut.world.common.WorldCore;
import thut.world.common.corehandlers.ConfigHandler;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public abstract class BlockCrystal extends Block// implements IMultiPartBlock
{
	public ItemStack drop;
	public static int renderID;
	public static int count = 0;
	String name;
	public BlockCrystal(int par1) {
		super(par1, Material.glass);
		this.setCreativeTab(WorldCore.tabThut);
		this.setBlockBounds(0, 0, 0, 1, 0.125f, 1);
		this.setLightValue(1);
		this.setLightOpacity(10);
	}
	
	@Override
	public Block setUnlocalizedName(String name)
	{
		this.name = name;
		return super.setUnlocalizedName(name);
	}
	
    /**
     * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
     * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
     */
    public boolean isOpaqueCube()
    {
        return false;
    }
    /**
     * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
     */
    public boolean renderAsNormalBlock()
    {
        return false;
    }
	
    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderType() {
    	return 0;
    }
    
    /**
     * Checks if the block is a solid face on the given side, used by placement logic.
     *
     * @param world The current world
     * @param x X Position
     * @param y Y position
     * @param z Z position
     * @param side The side to check
     * @return True if the block is solid on the specified side.
     */
    public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side)
    {
        return false;
    }

    @SideOnly(Side.CLIENT)
    /**
     * Determines if this block should render in this pass.
     *TODO
     * @param pass The pass in question
     * @return True to render
     */
    public boolean canRenderInPass(int pass)
    {
    	ClientProxy.renderPass = pass;
        return true;
    }
    
	@SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon(WorldCore.TEXTURE_PATH+name);
    }
	
    /**
     * Returns Returns true if the given side of this block type should be rendered (if it's solid or not), if the
     * adjacent block is at the given coordinates. Args: blockAccess, x, y, z, side
     */
    public boolean isBlockSolid(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
    {
        return false;
    }
    
    /**
     * Updates the blocks bounds based on its current state. Args: world, x, y, z
     */
    public void setBlockBoundsBasedOnState(IBlockAccess worldObj, int x, int y, int z)
    {
    	int m = worldObj.getBlockMetadata(x, y, z);
    	
    	if(m==0)
    		setBlockBounds(0, 0, 0, 1, 0.125f, 1);
    	if(m==1)
    		setBlockBounds(0, 1-0.125f, 0, 1, 1, 1);   	
    	if(m==2)
    		setBlockBounds(0, 0, 0, 1, 1, 0.125f); 	
    	if(m==3)
    		setBlockBounds(0, 0, 1-0.125f, 1, 1, 1);  
    	if(m==4)
    		setBlockBounds(0, 0, 0, 0.125f, 1, 1);  
    	if(m==5)
    		setBlockBounds(1-0.125f, 0, 0, 1, 1, 1);
    	
    }
    
    /**
     * Called when a block is placed using its ItemBlock. Args: World, X, Y, Z, side, hitX, hitY, hitZ, block metadata
     */
    public int onBlockPlaced(World par1World, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta)
    {

		this.setLightValue(1);
		this.setLightOpacity(10);
        return ForgeDirection.getOrientation(side).getOpposite().ordinal();
    }
    
    /**
     * Called when the block is placed in the world.
     */
    public void onBlockPlacedBy(World worldObj, int x, int y, int z, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) 
    {
    	int meta = worldObj.getBlockMetadata(x, y, z);
    }  
    /**
     * Called whenever the block is added into the world. Args: world, x, y, z
     */
    public void onBlockAdded(World worldObj, int x, int y, int z) 
    {
    	int meta = worldObj.getBlockMetadata(x, y, z);
    	BlockCoord pos = new BlockCoord(x, y, z);
    	CrystalPart part = (CrystalPart) CrystalPart.placement(worldObj, pos, meta);
    	
    	TileEntity te = worldObj.getBlockTileEntity(x, y, z);
    	TileMultipart tile;
    	if(te==null)
    	{
    		tile = TileMultipart.getOrConvertTile(worldObj, pos);
    	}
    	if(te instanceof TileMultipart)
    	{
    		tile = (TileMultipart)te;
    		if(tile.canPlacePart(worldObj, pos, part))
    		{
    			tile.addPart(worldObj, pos, part);
    		}
    	}
    	
    }

	 @SideOnly(Side.CLIENT)

    /**
     * Returns true if the given side of this block type should be rendered, if the adjacent block is at the given
     * coordinates.  Args: blockAccess, x, y, z, side
     */
    public boolean shouldSideBeRendered(IBlockAccess worldObj, int x, int y, int z, int dir)
    {
		ForgeDirection side = ForgeDirection.getOrientation(dir);
		
		int meta= worldObj.getBlockMetadata(x-side.offsetX, y-side.offsetY, z-side.offsetZ);
		
		return true;
    }
	 
	
}
