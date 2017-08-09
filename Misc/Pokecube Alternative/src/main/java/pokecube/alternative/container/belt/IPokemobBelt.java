package pokecube.alternative.container.belt;

import java.util.UUID;

import net.minecraft.item.ItemStack;

public interface IPokemobBelt
{
    ItemStack getCube(int index);

    void setCube(int index, ItemStack stack);

    int getSlot();

    void setSlot(int index);

    void setSlotID(int index, UUID id);

    UUID getSlotID(int index);

}
