package thut.tech.common.handlers;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.api.ThutBlocks;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;

public class BlockHandler
{

    public static class ItemLiftBlock extends ItemBlock
    {
        public ItemLiftBlock(Block par1)
        {
            super(par1);
            this.setHasSubtypes(true);
            this.setUnlocalizedName("lift");
        }

        @Override
        public int getMetadata(int damageValue)
        {
            return damageValue;
        }

        @Override
        @SideOnly(Side.CLIENT)
        /** returns a list of items with the same ID, but different meta (eg:
         * dye returns 16 items) */
        public void getSubItems(CreativeTabs par2CreativeTabs, NonNullList<ItemStack> par3List)
        {
            for (int j = 0; j < 2; ++j)
            {
                par3List.add(new ItemStack(this, 1, j));
            }
        }

        /** Returns the unlocalized name of this item. This version accepts an
         * ItemStack so different stacks can have different names based on their
         * damage or NBT. */
        @Override
        public String getUnlocalizedName(ItemStack stack)
        {
            return stack.getItemDamage() == 1 ? "tile.liftcontroller" : "tile.lift";
        }

    }

    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        Block lift = ThutBlocks.lift;
        GameRegistry.registerTileEntity(TileEntityLiftAccess.class, "liftaccesste");
        event.getRegistry().register(lift);
    }
}
