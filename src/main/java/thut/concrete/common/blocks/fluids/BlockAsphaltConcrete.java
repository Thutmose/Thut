package thut.concrete.common.blocks.fluids;

import static thut.api.ThutBlocks.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import thut.api.ThutCore;
import thut.api.blocks.BlockFluid;
import thut.api.blocks.IRebar;
import thut.concrete.common.ConcreteCore;
import thut.concrete.common.blocks.tileentity.worldBlocks.TileEntityBlockFluid;

public class BlockAsphaltConcrete extends BlockFluid implements ITileEntityProvider {

	public BlockAsphaltConcrete() {
		super(new Fluid("asphaltConcrete").setDensity(5000).setViscosity(10000),Material.rock);
		setBlockName("asphaltConcrete");
		solidAsphalt = this;
		setCreativeTab(ThutCore.tabThut);
	}
	

	@SideOnly(Side.CLIENT)
	IIcon[] iconArray;
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon("concrete:dryConcrete_"+15);
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
		TileEntityBlockFluid te = new TileEntityBlockFluid();
		te.metaArray = new int[] {15,15,15,15,15,15};
		
	    return te;
	}
	
	public int getFlowDifferential()
	{
		return 5 + new Random().nextInt(5);
	}
}
