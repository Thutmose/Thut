package thut.wearables;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public interface IWearableChecker
{
    EnumWearable getSlot(ItemStack stack);

    public void onPutOn(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex);

    public void onTakeOff(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex);

    public void onUpdate(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex);
}
