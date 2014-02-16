package thut.world.common.corehandlers;

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thut.world.common.blocks.fluids.liquids.BlockLava;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;

public class LiquidHandler {

	@SubscribeEvent
	public void onBucketFill(FillBucketEvent event) {

		ItemStack result = fillCustomBucket(event.world, event.target);

		if (result == null)
			return;

		event.result = result;
		event.setResult(Result.ALLOW);
	}

	public ItemStack fillCustomBucket(World world, MovingObjectPosition pos) 
	{

		Block blockID = world.getBlock(pos.blockX, pos.blockY, pos.blockZ);
		
		boolean isLava = false;
		for(int i = 0;i<3;i++)
		{
			isLava = isLava||blockID == BlockLava.getInstance(i);
		}
		
		if ((isLava)
				&& world.getBlockMetadata(pos.blockX, pos.blockY, pos.blockZ) == 0) 
		{

			world.setBlock(pos.blockX, pos.blockY, pos.blockZ, Blocks.air);

			return new ItemStack(Items.lava_bucket);
		} 
		
		
		else
			return null;
	}

}
