package thut.world.common.blocks.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thut.api.ThutBlocks;
import thut.api.explosion.ExplosionCustom;
import thut.world.common.Volcano;
import thut.world.common.WorldCore;
import thut.world.common.blocks.tileentity.TileEntityVolcano;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class BlockVolcano extends Block implements ITileEntityProvider
{
	public BlockVolcano() {
		super(Material.ground);
		setBlockName("volcano");
		setHardness(100000.0f);
		setResistance(10000000.0f);
		ThutBlocks.volcano = this;
		this.setCreativeTab(WorldCore.tabThut);
		this.setBlockUnbreakable();
	}
	
	 public void onBlockAdded(World worldObj, int par2, int par3, int par4) 
	 {
		 Volcano.getVolcano(par2, par4, worldObj);
		 TileEntityVolcano te = (TileEntityVolcano)worldObj.getTileEntity(par2, par3, par4);
		 te.z = par4;
	 }
	 
		
	public boolean onBlockActivated(World worldObj, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
		if(!worldObj.isRemote&&player.getHeldItem()==null)
		{
			TileEntityVolcano te = (TileEntityVolcano)worldObj.getTileEntity(x, y, z);
			te.doop = true;
		}
			
		return false;
    }

 
	@SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon("bedrock");
    }

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileEntityVolcano();
	}
	
	
}