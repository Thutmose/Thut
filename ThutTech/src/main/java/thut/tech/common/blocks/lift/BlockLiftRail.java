package thut.tech.common.blocks.lift;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

//import appeng.api.me.tiles.IGridTileEntity;

import scala.collection.Iterator;
import thut.api.ThutBlocks;
import thut.tech.common.TechCore;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

public class BlockLiftRail extends Block implements ITileEntityProvider//, IRebar
{

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
    
    public static boolean isRail(World world, BlockPos pos)
    {
        return world.getBlockState(pos).getBlock() == ThutBlocks.liftRail;
    }
}
