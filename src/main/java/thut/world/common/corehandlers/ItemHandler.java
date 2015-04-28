package thut.world.common.corehandlers;

import static thut.api.ThutItems.*;

//import static thutconcrete.common.blocks.Blocks.*;
import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import thut.api.ThutBlocks;
import thut.api.ThutItems;
import thut.core.common.items.ItemDusts;
import thut.core.common.items.ItemDusts.Dust;
import thut.world.common.WorldCore;
import thut.world.common.blocks.fluids.dusts.BlockDust;
import thut.world.common.blocks.fluids.dusts.BlockDustInactive;
import thut.world.common.blocks.fluids.solids.BlockSolidLava;
import thut.world.common.blocks.world.BlockWorldGen;
import thut.world.common.items.*;
import thut.world.common.items.tools.ItemGrinder;

public class ItemHandler {

	// For shorter referencing to the config handler
	private ConfigHandler config;

	// Empty fields for holding items
	public static Item[] items;
	public static List<Item> itemList = new ArrayList<Item>();
	
	public static ItemStack[] brushes = new ItemStack[17];
	
	public ItemHandler(ConfigHandler handler){
		config = handler;
		// Initalizes all mod items
		initItems();
	}

	public void initItems(){
		int id = config.IDItem;
		Item grinderItem = new ItemGrinder();
		if(dust==null)
		{
			Item dusts = new ItemDusts();
			dust = new ItemStack(dusts);
			trass = new ItemStack(dusts, 1, 3);
			itemList.add(dusts);
		}
		itemList.add(grinderItem);
		addDusts();
		registerItems();
	}
	
	void addDusts()
	{
		ItemDusts.addDust(new Dust("dust", WorldCore.modid)
		{
		    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
		    {
		    	if(!world.isRemote&&stack.getItemDamage()==0)
		    	{
		    		int x1 = ForgeDirection.getOrientation(side).offsetX+x,y1 = ForgeDirection.getOrientation(side).offsetY+y, z1 = ForgeDirection.getOrientation(side).offsetZ+z;
		    		int meta = world.getBlockMetadata(x1, y1, z1);
		    		Block block = world.getBlock(x1, y1, z1);
		    		
		            if (player.isSneaking()&&ItemDye.applyBonemeal(stack, world, x, y, z, player))
		            {
		                if (!world.isRemote)
		                {
		                    world.playAuxSFX(2005, x, y, z, 0);
		                }

		                return true;
		            }
		    		
		    		if(block instanceof BlockDust||block instanceof BlockDustInactive&&meta!=15)
		    		{
		    			world.setBlockMetadataWithNotify(x1, y1, z1, meta+1, 3);
		    			if(!player.capabilities.isCreativeMode)
		    				stack.splitStack(1);
		                return true;
		    		}
		    		else if(world.getBlock(x1, y1, z1) instanceof BlockDust||world.getBlock(x1, y1, z1) instanceof BlockDustInactive&&meta!=15)
		    		{
		    			world.setBlockMetadataWithNotify(x1, y1, z1, meta+1, 3);
		    			if(!player.capabilities.isCreativeMode)
		    				stack.splitStack(1);
		                return true;
		    		}
		    		else if (block==Blocks.air||block.getMaterial().isReplaceable())
		    		{
		    			world.setBlock(x1, y1, z1, ThutBlocks.dust, Math.min(15, stack.stackSize), 3);
		    			if(!player.capabilities.isCreativeMode)
		    				stack.splitStack(Math.min(stack.stackSize, 16));
		                return true;
		    		}
		    	}
		    	return false;
		    }
		});
		ItemDusts.addDust(new Dust("dustCaCO3", WorldCore.modid));
		ItemDusts.addDust(new Dust("dustCaO", WorldCore.modid));
		ItemDusts.addDust(new Dust("dustTrass", WorldCore.modid));
		ItemDusts.addDust(new Dust("dustCement", WorldCore.modid));
		ItemDusts.addDust(new Dust("dustSulfur", WorldCore.modid));
		
	}
	
	public void registerItems(){
		for(Item item: itemList){
			GameRegistry.registerItem(item, item.getUnlocalizedName().substring(5));
			System.out.println(item.getUnlocalizedName().substring(5));
		}
	}
}
