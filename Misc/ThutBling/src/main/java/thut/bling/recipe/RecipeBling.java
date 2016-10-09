package thut.bling.recipe;

import java.util.List;
import java.util.Locale;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import thut.bling.ItemBling;
import thut.wearables.EnumWearable;
import thut.wearables.IWearable;

public class RecipeBling implements IRecipe
{
    private ItemStack toRemove = null;
    private ItemStack output   = null;

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        return output;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return output;
    }

    @Override
    public int getRecipeSize()
    {
        return 10;
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv)
    {
        ItemStack[] ret = new ItemStack[inv.getSizeInventory()];
        if (toRemove != null) ret[0] = toRemove;
        return ret;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn)
    {
        output = null;
        toRemove = null;
        boolean wearable = false;
        boolean dye = false;
        boolean gem = false;
        ItemStack dyeStack = null;
        ItemStack worn = null;
        ItemStack gemStack = null;
        int n = 0;
        craft:
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null)
            {
                n++;
                if (stack.getItem() instanceof ItemBling)
                {
                    if (wearable) return false;
                    wearable = true;
                    worn = stack;
                    continue;
                }
                List<ItemStack> dyes = OreDictionary.getOres("dye");
                boolean isDye = false;
                for (ItemStack dye1 : dyes)
                {
                    if (OreDictionary.itemMatches(dye1, stack, false))
                    {
                        isDye = true;
                        break;
                    }
                }
                if (isDye)
                {
                    if (dye) return false;
                    dye = true;
                    dyeStack = stack;
                    continue;
                }
                for (ItemStack key : RecipeLoader.instance.knownTextures.keySet())
                {
                    System.out.println(key.getTagCompound() + " " + stack.getTagCompound());
                    if (RecipeLoader.isSameStack(key, stack))
                    {
                        if (gem) return false;
                        gem = true;
                        gemStack = key;
                        continue craft;
                    }
                }
            }
        }
        if (n > 2 || !wearable) return false;
        if (dye)
        {
            output = worn.copy();
            System.out.println("test");
            if (!output.hasTagCompound()) output.setTagCompound(new NBTTagCompound());
            int[] ids = OreDictionary.getOreIDs(dyeStack);
            int colour = dyeStack.getItemDamage();
            for (int i : ids)
            {
                String name = OreDictionary.getOreName(i);
                if (name.startsWith("dye") && name.length() > 3)
                {
                    String val = name.replace("dye", "").toUpperCase(Locale.ENGLISH);
                    try
                    {
                        EnumDyeColor type = EnumDyeColor.valueOf(val);
                        colour = type.getDyeDamage();
                        break;
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            output.getTagCompound().setInteger("dyeColour", colour);
        }
        else if (gem)
        {
            output = worn.copy();
            System.out.println("test");
            if (!output.hasTagCompound()) output.setTagCompound(new NBTTagCompound());
            if (output.getTagCompound().hasKey("gem"))
            {
                output = null;
                return false;
            }
            String tex = RecipeLoader.instance.knownTextures.get(gemStack);
            if (tex == null)
            {
                tex = "minecraft:textures/blocks/stone.png";
            }
            output.getTagCompound().setString("gem", tex);
            NBTTagCompound tag = new NBTTagCompound();
            gemStack.writeToNBT(tag);
            output.getTagCompound().setTag("gemTag", tag);
        }
        else if (!(gem || dye) && worn.hasTagCompound() && worn.getTagCompound().hasKey("gem"))
        {
            output = worn.copy();
            output.getTagCompound().removeTag("gem");
            if (output.getTagCompound().hasKey("gemTag"))
            {
                NBTTagCompound tag = output.getSubCompound("gemTag", false);
                toRemove = ItemStack.loadItemStackFromNBT(tag);
            }
            if (toRemove == null) output = null;
        }
        else
        {
            IWearable wear = (IWearable) worn.getItem();
            EnumWearable slot = wear.getSlot(worn);
            output = ItemBling.defaults.get(slot).copy();
            System.out.println(ItemBling.defaults);
            if (RecipeLoader.isSameStack(worn, output)) output = null;
        }
        return output != null;
    }
}
