package thut.tech.common.items;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import thut.tech.common.TechCore;

public class RecipeReset extends SpecialRecipe
{
    public static final IRecipeSerializer<RecipeReset> SERIALIZER = IRecipeSerializer.register("thuttech:resetlinker",
            new SpecialRecipeSerializer<>(RecipeReset::new));

    public RecipeReset(final ResourceLocation idIn)
    {
        super(idIn);
    }

    @Override
    public ItemStack getCraftingResult(final CraftingInventory inv)
    {
        int n = 0;
        boolean matched = false;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            final ItemStack stack = inv.getStackInSlot(i);
            if (stack.getItem() == TechCore.LINKER) matched = true;
            n++;
        }
        if (n != 1) matched = false;
        if (matched) return new ItemStack(TechCore.LINKER);
        return ItemStack.EMPTY;
    }

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return RecipeReset.SERIALIZER;
    }

    @Override
    public boolean matches(final CraftingInventory inv, final World worldIn)
    {
        return !this.getCraftingResult(inv).isEmpty();
    }

}
