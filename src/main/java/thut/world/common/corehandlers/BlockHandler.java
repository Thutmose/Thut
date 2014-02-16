package thut.world.common.corehandlers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import thut.api.ThutBlocks;
import thut.world.common.blocks.*;
import thut.world.common.blocks.fluids.*;
import thut.world.common.blocks.fluids.dusts.*;
import thut.world.common.blocks.fluids.gases.BlockCO2Cool;
import thut.world.common.blocks.fluids.gases.BlockCO2Warm;
import thut.world.common.blocks.fluids.liquids.*;
import thut.world.common.blocks.fluids.solids.*;
import thut.world.common.blocks.world.BlockVolcano;
import thut.world.common.blocks.world.BlockWorldGen;
import thut.world.common.items.blocks.ItemWorldGenBlock;

public class BlockHandler {

	private ConfigHandler config;
	public static Block[] blocks;
	public static Map<Block, ItemBlock> itemBlocks = new HashMap<Block, ItemBlock>();
	private static List<Block> blockList = new ArrayList<Block>();

	public BlockHandler(ConfigHandler configHandler){
		config = configHandler;
		initBlocks();
	}

	public void initBlocks(){
		
		blockList.add(new BlockVolcano());
		blockList.add(new BlockMisc());
		blockList.add(new BlockDust());
		
		if(config.deleteAsh)
		{
			;
		}
		else
		{
			blockList.add(new BlockDustInactive());
		}
		
		for(int i = 0; i<3; i++){
			blockList.add(new BlockLava(i));
			blockList.add(new BlockSolidLava(i));
		}
		
		blockList.add(new BlockLava(3));
		blockList.add(new BlockSolidLava(3));
		
		blockList.add(new BlockCO2Cool());
		blockList.add(new BlockCO2Warm());
		
		blocks = blockList.toArray(new Block[0]);

		registerBlocks();
		
		BlockWorldGen worldGenBlock = new BlockWorldGen();
		
		GameRegistry.registerBlock(worldGenBlock, ItemWorldGenBlock.class, "worldGenBlock");
		
		blockList.add(worldGenBlock);
		blocks = blockList.toArray(new Block[0]);
		
		changeFlamibility();
		initFluids();
	}

	public void registerBlocks(){
		for(Block block : blocks)
		{
			GameRegistry.registerBlock(block, block.getLocalizedName().substring(5));
		}
		
	}
	
	public void changeFlamibility()
	{
//		for(Block b:ThutBlocks.getAllBlocks())TODO find out how to do this again
//		{
//			if(b!=null&&b.getMaterial() == Material.wood&&!b.getLocalizedName().toLowerCase().contains("chest"))
//			{
//				b.;
//			}
//			if(b instanceof BlockDoor && b.blockMaterial != Material.iron)
//			{
//				b.setBurnProperties(b.blockID, 5, 20);
//			}
//		}
	}
	
	public void initFluids()
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
