package thut.core.common.blocks;

import thut.api.maths.Vector3;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public abstract class BlockGas extends BlockFluidFinite
{
	boolean breathable = false;

	public BlockGas(Fluid fluid) {
		super(fluid, Material.air);
		setLightOpacity(0);
	}
	
}
