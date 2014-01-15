package thut.world.common.blocks.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thut.api.Blocks;
import thut.api.explosion.ExplosionCustom;
import thut.api.utils.Vector3;
import thut.world.common.Volcano;
import thut.world.common.WorldCore;
import thut.world.common.blocks.tileentity.TileEntityVolcano;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class BlockVolcano extends Block implements ITileEntityProvider
{
	public BlockVolcano(int par1) {
		super(par1, Material.ground);
		setUnlocalizedName("volcano");
		setHardness(100000.0f);
		setResistance(10000000.0f);
		Blocks.volcano = this;
		this.setCreativeTab(WorldCore.tabThut);
		this.setBlockUnbreakable();
	}
	
	 public void onBlockAdded(World worldObj, int par2, int par3, int par4) 
	 {
		 Volcano.getVolcano(par2, par4, worldObj);
		 TileEntityVolcano te = (TileEntityVolcano)worldObj.getBlockTileEntity(par2, par3, par4);
		 te.z = par4;
	 }
	 
		
	public boolean onBlockActivated(World worldObj, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
    {
		if(!worldObj.isRemote&&player.getHeldItem()==null)
		{
			TileEntityVolcano te = (TileEntityVolcano)worldObj.getBlockTileEntity(x, y, z);
			te.doop = true;
		}
		if(!worldObj.isRemote&&player.getHeldItem()!=null&&player.getHeldItem().itemID==Item.stick.itemID)
		{
			TileEntityVolcano te = (TileEntityVolcano)worldObj.getBlockTileEntity(x, y, z);
			te.setDormancy(!te.dormant);
		}
		if(!worldObj.isRemote&&player.getHeldItem()!=null&&player.getHeldItem().itemID==Item.shovelIron.itemID)
		{
			TileEntityVolcano te = (TileEntityVolcano)worldObj.getBlockTileEntity(x, y, z);
			te.typeid = (te.typeid+1)%3;
			
			player.addChatMessage(te.types[te.typeid]+te.getState());
		}
		{
			
		}
		return false;
    }

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityVolcano();
	}
 
	@SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon("bedrock");
    }
	
	
}