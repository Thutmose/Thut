package thut.tech.common.handlers;

import java.lang.reflect.Constructor;

import com.google.common.collect.ObjectArrays;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.tech.Reference;
import thut.tech.common.blocks.lift.BlockLift;
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

    public static void registerBlocks(FMLPreInitializationEvent e)
    {
        Block lift = new BlockLift().setRegistryName(Reference.MOD_ID, "lift");
        GameRegistry.registerTileEntity(TileEntityLiftAccess.class, "liftaccesste");
        register(lift, ItemLiftBlock.class, lift.getRegistryName().toString());
    }

    public static void register(Object o, Class<? extends ItemBlock> clazz, String name)
    {
        Block block = (Block) o;
        if (clazz != null)
        {
            ItemBlock i = null;
            Class<?>[] ctorArgClasses = new Class<?>[1];
            ctorArgClasses[0] = Block.class;
            try
            {
                Constructor<? extends ItemBlock> itemCtor = clazz.getConstructor(ctorArgClasses);
                i = itemCtor.newInstance(ObjectArrays.concat(block, new Object[0]));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            // block registration has to happen first
            GameRegistry.register(block.getRegistryName() == null ? block.setRegistryName(name) : block);
            if (i != null) GameRegistry.register(i.setRegistryName(name));
        }
    }
}
