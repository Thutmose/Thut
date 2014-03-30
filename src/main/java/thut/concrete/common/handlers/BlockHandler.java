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
import thut.concrete.common.blocks.technical.BlockMixer;
import thut.concrete.common.blocks.technical.BlockKiln;
import thut.concrete.common.blocks.technical.BlockRebar;
import thut.concrete.common.blocks.tileentity.crafting.TileEntityMixer;
import thut.concrete.common.blocks.tileentity.crafting.TileEntityKiln;
import thut.core.common.blocks.BlockFluid;
import thut.core.common.blocks.tileentity.TileEntityBlockFluid;
import thut.core.common.blocks.tileentity.TileEntityMultiBlockPart;
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
		blockList.add(new BlockAsphalt());
		blockList.add(new BlockLiquidAsphalt());

		blockList.add(new BlockKiln());
		blockList.add(new BlockMixer());
		GameRegistry.registerTileEntity(TileEntityKiln.class, "multikilncore");
		GameRegistry.registerTileEntity(TileEntityMixer.class, "mixerte");
		
		blockList.add(new BlockRebar());
		
		for(Block b: blockList)
		{
			GameRegistry.registerBlock(b, b.getUnlocalizedName().substring(5));
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
