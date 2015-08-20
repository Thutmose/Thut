package thut.concrete.common.blocks.fluids;



import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

import static net.minecraft.init.Blocks.dirt;
import static net.minecraft.init.Blocks.flowing_water;
import static net.minecraft.init.Blocks.grass;
import static net.minecraft.init.Blocks.water;
import static net.minecraft.util.EnumFacing.DOWN;
import static thut.api.ThutBlocks.*;

import java.util.Random;

import thut.api.ThutBlocks;
import thut.api.blocks.BlockFluid;
import thut.api.maths.Vector3;
import thut.concrete.common.ConcreteCore;

public class BlockAsphalt extends BlockFluid {

	public BlockAsphalt() {
		super(new Fluid("asphalt").setDensity(4000).setViscosity(2000).setTemperature(400), Material.rock);
		setBlockName("asphalt");
		//setCreativeTab(ConcreteCore.tabThut);
		this.setResistance((float) 10.0);
		this.setHardness((float) 1.0);
//		this.rate = 0.9;
		liquidAsphalt = this;
		//hardenTo = asphaltConcrete;
		this.setTemperature(400);
		//this.solidifiable = true;
		this.setTickRandomly(true);
//		this.placeamount = 16;
	}
	
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon("concrete:liquidAsphalt");
	}
	
	public void doHardenTick(World worldObj, Vector3 vec)
	{
		Vector3 down = vec.offset(DOWN);

		Block below = down.getBlock(worldObj);
		int meta = down.getBlockMetadata(worldObj);

		if (below == grass)
		{
			down.setBlock(worldObj, dirt, 0, 2);
		}

		if (down.getBlock(worldObj) instanceof BlockFluid && meta != 15 || below == ThutBlocks.volcano) { return; }

		if (below == water || below == flowing_water || down.isAir(worldObj) || below == this) { return; }

		vec.setBlock(worldObj, ThutBlocks.concrete, vec.getBlockMetadata(worldObj), 2);
		return;

	}
	
	public int getFlowDifferential()
	{
		return 3 + new Random().nextInt(3);
	}
}
