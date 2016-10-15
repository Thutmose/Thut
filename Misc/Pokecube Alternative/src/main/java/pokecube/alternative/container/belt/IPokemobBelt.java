package pokecube.alternative.container.belt;

import net.minecraft.item.ItemStack;

public interface IPokemobBelt
{
    ItemStack getCube(int index);

    void setCube(int index, ItemStack stack);

    boolean isOut(int index);

    void setOut(int index, boolean bool);

    int getSlot();

    void setSlot(int index);

}
