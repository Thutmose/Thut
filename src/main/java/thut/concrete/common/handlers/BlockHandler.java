package thut.concrete.common.handlers;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;
import thut.api.ThutBlocks;
import thut.api.blocks.BlockFluid;
import thut.api.blocks.multiparts.parts.PartRebar;
import thut.concrete.common.blocks.fluids.BlockAsphalt;
import thut.concrete.common.blocks.fluids.BlockAsphaltConcrete;
import thut.concrete.common.blocks.fluids.BlockConcrete;
import thut.concrete.common.blocks.fluids.BlockLiquidConcrete;
import thut.concrete.common.blocks.fluids.BlockLiquidREConcrete;
import thut.concrete.common.blocks.fluids.BlockREConcrete;
import thut.concrete.common.blocks.technical.BlockKiln;
import thut.concrete.common.blocks.technical.BlockMixer;
import thut.concrete.common.blocks.technical.BlockRebar;
import thut.concrete.common.blocks.tileentity.crafting.TileEntityKiln;
import thut.concrete.common.blocks.tileentity.crafting.TileEntityMixer;
import thut.concrete.common.blocks.tileentity.worldBlocks.TileEntityBlockFluid;
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
		blockList.add(new BlockAsphaltConcrete());
		blockList.add(new BlockAsphalt());

		blockList.add(new BlockKiln());
		blockList.add(new BlockMixer());
		GameRegistry.registerTileEntity(TileEntityKiln.class, "multikilncore");
		GameRegistry.registerTileEntity(TileEntityMixer.class, "mixerte");
		GameRegistry.registerTileEntity(TileEntityBlockFluid.class, "paintableTEfluid");
//		
		Block rebar;
		blockList.add(rebar = new BlockRebar());
		
		ThutBlocks.addRebarPart(rebar, "tc_rebar");
		
		for(Block b: blockList)
		{
			GameRegistry.registerBlock(b, b.getLocalizedName().substring(5));
		}
	}
}
