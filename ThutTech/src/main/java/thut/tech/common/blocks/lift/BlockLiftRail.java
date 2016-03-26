package thut.tech.common.blocks.lift;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import thut.api.ThutBlocks;
import thut.tech.common.TechCore;

public class BlockLiftRail extends Block implements ITileEntityProvider//, IRebar
{

	public static boolean isRail(World world, BlockPos pos)
    {
        return world.getBlockState(pos).getBlock() == ThutBlocks.liftRail;
    }

    public BlockLiftRail() 
	{
		super(Material.iron);
		ThutBlocks.liftRail = this;
		this.setUnlocalizedName("liftRail");
		setCreativeTab(TechCore.tabThut);
		this.setBlockBounds(0, 0, 0, 1, 1, 1);
		setHardness((float) 10.0);
		setResistance(10.0f);
	}
    
    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        return new TileEntityLiftAccess();
    }
}
