package thut.core.common.blocks;

import net.minecraft.block.material.Material;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;

public abstract class BlockGas extends BlockFluidFinite
{
	boolean breathable = false;

	public BlockGas(Fluid fluid) {
		super(fluid, Material.air);
		setLightOpacity(0);
	}
	
}
