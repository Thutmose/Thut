package thut.tech.common.handlers;

import java.lang.reflect.Constructor;
import java.util.List;

import com.google.common.collect.ObjectArrays;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.lib.CompatWrapper;
import thut.tech.Reference;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.lift.BlockLift;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.entity.EntityProjectile;

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
        public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List)
        {
            for (int j = 0; j < 2; ++j)
            {
                par3List.add(new ItemStack(par1, 1, j));
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

    @SuppressWarnings("deprecation")
    public static void registerBlocks(FMLPreInitializationEvent e)
    {
        Block lift = new BlockLift().setRegistryName(Reference.MOD_ID, "lift");

        GameRegistry.registerTileEntity(TileEntityLiftAccess.class, "liftaccesste");

        CompatWrapper.registerModEntity(EntityLift.class, "lift", 1, TechCore.instance, 32, 1, true);

        CompatWrapper.registerModEntity(EntityProjectile.class, "projectile", 2, TechCore.instance, 32, 1, true);

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
