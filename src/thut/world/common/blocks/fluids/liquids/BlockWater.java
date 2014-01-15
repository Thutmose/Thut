package thut.world.common.blocks.fluids.liquids;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import thut.api.Blocks;
import thut.world.common.WorldCore;
import thut.world.common.corehandlers.ConfigHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.liquids.ILiquid;

public class BlockWater extends BlockFluidFinite
{
	Integer[][] data;
	
	public BlockWater(int par1) {
		super(par1, new Fluid("water"),Material.water);
		setUnlocalizedName("b16fWater");
		this.setTickRandomly(true);
		setCreativeTab(WorldCore.tabThut);
	//	Blocks.water = this;
		this.quantaPerBlock = 16;
		this.quantaPerBlockFloat = 16;
	}
	
	@SideOnly(Side.CLIENT)
    public void registerIcons(IconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon(WorldCore.TEXTURE_PATH+"water");
    }
	
//    /**
//     * Called whenever the block is added into the world. Args: world, x, y, z
//     */
//    public void onBlockAdded(World par1World, int par2, int par3, int par4) 
//    {
//    	par1World.setBlockMetadataWithNotify(par2, par3, par4, 15, 3);
//    }
	
}
