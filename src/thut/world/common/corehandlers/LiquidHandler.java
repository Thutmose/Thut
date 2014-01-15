package thut.world.common.corehandlers;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thut.api.Blocks;
import thut.api.Items;
import thut.world.common.blocks.fluids.liquids.BlockLava;
import thut.world.common.blocks.fluids.liquids.BlockWater;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.liquids.IBlockLiquid;
import net.minecraftforge.liquids.LiquidContainerData;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;

public class LiquidHandler {

	@ForgeSubscribe
	public void onBucketFill(FillBucketEvent event) {

		ItemStack result = fillCustomBucket(event.world, event.target);

		if (result == null)
			return;

		event.result = result;
		event.setResult(Result.ALLOW);
	}

	public ItemStack fillCustomBucket(World world, MovingObjectPosition pos) 
	{

		int blockID = world.getBlockId(pos.blockX, pos.blockY, pos.blockZ);
		
		boolean isLava = false;
		for(int i = 0;i<3;i++)
		{
			isLava = isLava||blockID == BlockLava.getInstance(i).blockID;
		}
		
		if ((isLava)
				&& world.getBlockMetadata(pos.blockX, pos.blockY, pos.blockZ) == 0) 
		{

			world.setBlock(pos.blockX, pos.blockY, pos.blockZ, 0);

			return new ItemStack(Item.bucketLava);
		} 
		
		
		else
			return null;
	}

}
