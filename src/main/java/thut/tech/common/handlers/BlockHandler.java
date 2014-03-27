package thut.tech.common.handlers;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import thut.api.blocks.ItemBlockMeta;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.technical.BlockLift;
import thut.tech.common.blocks.technical.BlockLiftRail;
import thut.tech.common.blocks.tileentity.TileEntityLiftAccess;
import thut.tech.common.entity.EntityLift;
import net.minecraft.block.Block;

public class BlockHandler 
{
	
	public static void registerBlocks()
	{
		Block lift = new BlockLift();
		Block rail = new BlockLiftRail();
		
		GameRegistry.registerTileEntity(TileEntityLiftAccess.class, "liftaccesste");
		
		EntityRegistry.registerModEntity(EntityLift.class, "thuttechlift", 1, TechCore.instance, 32, 3, true);

		GameRegistry.registerBlock(lift, ItemBlockMeta.class, lift.getLocalizedName().substring(5));
		GameRegistry.registerBlock(rail, rail.getUnlocalizedName().substring(5));
	}
}
