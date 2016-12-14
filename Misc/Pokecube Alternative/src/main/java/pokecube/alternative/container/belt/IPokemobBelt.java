package pokecube.alternative.container.belt;

import net.minecraft.item.ItemStack;

public interface IPokemobBelt
{
    ItemStack getCube(int index);

    void setCube(int index, ItemStack stack);

    int getSlot();

    void setSlot(int index);

}
