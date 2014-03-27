package thut.concrete.common.blocks.fluids;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import thut.api.ThutBlocks;
import thut.concrete.common.ConcreteCore;
import thut.core.common.blocks.BlockFluid;

public class BlockLiquidAsphalt extends BlockFluid {

	public BlockLiquidAsphalt() {
		super(getFluidType("liquidRock"), new WetRock(MapColor.stoneColor));
		setBlockName("asphaltLiquid");
		setCreativeTab(ConcreteCore.tabThut);
		this.setResistance((float) 10.0);
		this.setHardness((float) 1.0);
		this.rate = 0.9;
		ThutBlocks.liquidAsphalt = this;
		this.setTemperature(400);
		this.solidifiable = true;
		this.setTickRandomly(true);
		this.placeamount = 16;
	}
	
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon("concrete:liquidAsphalt");
	}
}
