package thut.concrete.common.blocks.fluids;

import static thut.api.ThutBlocks.concrete;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraftforge.fluids.Fluid;
import thut.concrete.common.ConcreteCore;
import thut.world.common.blocks.fluids.BlockFluid;

public class BlockAsphalt extends BlockFluid {

	public BlockAsphalt() {
		super(new Fluid("solid"),Material.rock);
		setBlockName("asphalt");
		this.rate = 10;
		concrete = this;
		setCreativeTab(ConcreteCore.tabThut);
		setSolid();
		this.stampable = true;
		this.setTickRandomly(false);
		this.setStepSound(soundTypeStone);
	}
	
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister)
	{
		this.blockIcon = par1IconRegister.registerIcon("concrete:asphalt");
	}

}
