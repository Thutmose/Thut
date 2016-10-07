package thut.wearables;

import net.minecraft.item.ItemStack;

public interface IWearableChecker
{
    EnumWearable getSlot(ItemStack stack);
}
