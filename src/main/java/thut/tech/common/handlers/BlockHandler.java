package thut.tech.common.handlers;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import thut.api.ThutBlocks;
import thut.api.blocks.multiparts.parts.PartRebar;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.lift.BlockLift;
import thut.tech.common.blocks.lift.BlockLiftRail;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.blocks.railgun.BlockRailgun;
import thut.tech.common.blocks.railgun.TileEntityRailgun;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.entity.EntityProjectile;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class BlockHandler {

	public static void registerBlocks() {
		Block lift = new BlockLift();
		Block rail = new BlockLiftRail();

		GameRegistry.registerTileEntity(TileEntityLiftAccess.class,
				"liftaccesste");
		
		Block railgun = new BlockRailgun();
		GameRegistry.registerTileEntity(TileEntityRailgun.class,
				"thuttech:railgun");

		EntityRegistry.registerModEntity(EntityLift.class, "thuttechlift", 1,
				TechCore.instance, 32, 1, true);

		EntityRegistry.registerModEntity(EntityProjectile.class, "thuttechprojectile", 2,
				TechCore.instance, 32, 1, true);

		GameRegistry.registerBlock(railgun, ItemBlock.class, railgun.getLocalizedName().substring(5));
		GameRegistry.registerBlock(lift, ItemLiftBlock.class, lift.getLocalizedName().substring(5));
		GameRegistry.registerBlock(rail, rail.getLocalizedName().substring(5));

		ThutBlocks.addRebarPart(rail, "tt_rail");
//		ThutBlocks.parts2.put("tt_rail", PartRebar.class);
//		ThutBlocks.addPart(rail, PartRebar.class);
	}

	public static class ItemLiftBlock extends ItemBlock {
		public ItemLiftBlock(Block par1) {
			super(par1);
			this.setHasSubtypes(true);
			this.setUnlocalizedName("lift");
		}

		@Override
		public int getMetadata(int damageValue) {
			return damageValue;
		}

		/**
		 * Returns the unlocalized name of this item. This version accepts an
		 * ItemStack so different stacks can have different names based on their
		 * damage or NBT.
		 */
		@Override
		public String getUnlocalizedName(ItemStack stack) {
			return stack.getItemDamage() == 1 ? "tile.control" : "tile.lift";
		}

		@Override
		@SideOnly(Side.CLIENT)
		/**
		 * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
		 */
		public void getSubItems(Item par1, CreativeTabs par2CreativeTabs,
				List par3List) {
			for (int j = 0; j < 2; ++j) {
				par3List.add(new ItemStack(par1, 1, j));
			}
		}

	}
}
