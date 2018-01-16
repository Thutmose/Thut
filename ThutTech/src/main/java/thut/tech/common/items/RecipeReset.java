package thut.tech.common.items;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import thut.lib.CompatWrapper;
import thut.lib.IDefaultRecipe;

public class RecipeReset implements IDefaultRecipe
{

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        return !getCraftingResult(inv).isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        int n = 0;
        boolean matched = false;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (CompatWrapper.isValid(stack))
            {
                if (stack.getItem() == ItemLinker.instance)
                {
                    matched = true;
                }
                n++;
            }
        }
        if (n != 1) matched = false;
        if (matched) return getRecipeOutput();
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return new ItemStack(ItemLinker.instance);
    }

    ResourceLocation registryName;

    @Override
    public IRecipe setRegistryName(ResourceLocation name)
    {
        registryName = name;
        return this;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return registryName;
    }

    @Override
    public Class<IRecipe> getRegistryType()
    {
        return IRecipe.class;
    }

}
