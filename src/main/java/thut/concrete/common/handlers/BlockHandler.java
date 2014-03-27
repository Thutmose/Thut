package thut.concrete.common.handlers;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;
import thut.concrete.common.blocks.fluids.BlockAsphalt;
import thut.concrete.common.blocks.fluids.BlockConcrete;
import thut.concrete.common.blocks.fluids.BlockLiquidAsphalt;
import thut.concrete.common.blocks.fluids.BlockLiquidConcrete;
import thut.concrete.common.blocks.fluids.BlockLiquidREConcrete;
import thut.concrete.common.blocks.fluids.BlockREConcrete;
import thut.concrete.common.blocks.technical.BlockMultiFurnace;
import thut.concrete.common.blocks.technical.BlockRebar;
import thut.concrete.common.blocks.tileentity.crafting.TileEntityMultiBlockPart;
import thut.concrete.common.blocks.tileentity.crafting.TileEntityMultiFurnace;
import thut.concrete.common.blocks.tileentity.worldBlocks.TileEntityBlockFluid;
import thut.world.common.blocks.fluids.BlockFluid;
import net.minecraft.block.Block;

public class BlockHandler 
{
	private static List<Block> blockList = new ArrayList<Block>();
	
	public static void registerBlocks()
	{
		blockList.add(new BlockLiquidConcrete());
		blockList.add(new BlockConcrete());
		blockList.add(new BlockLiquidREConcrete());
		blockList.add(new BlockREConcrete());
		blockList.add(new BlockLiquidAsphalt());
		blockList.add(new BlockAsphalt());
		
		
		blockList.add(new BlockMultiFurnace());
		GameRegistry.registerTileEntity(TileEntityMultiFurnace.class, "multikilncore");
		GameRegistry.registerTileEntity(TileEntityMultiBlockPart.class, "multikilnpart");
		GameRegistry.registerTileEntity(TileEntityBlockFluid.class, "paintableTE");
		
		blockList.add(new BlockRebar());
		
		for(Block b: blockList)
		{
			GameRegistry.registerBlock(b, b.getLocalizedName().substring(5));
		}
		initFluids();
	}
	
	public static void initFluids()
	{
		for(Block b: blockList)
		{
			if(b instanceof BlockFluid)
			{
				((BlockFluid)b).setData();
			}
		}
	}
}
