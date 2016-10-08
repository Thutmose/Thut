package thut.bling.bag;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thut.bling.ThutBling;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;

public class ItemBag extends Item implements IActiveWearable
{

    public ItemBag()
    {
        super();
    }

    @Override
    public EnumWearable getSlot(ItemStack stack)
    {
        return EnumWearable.BACK;
    }

    @Override
    public void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks)
    {
        ThutBling.proxy.renderWearable(slot, wearer, stack, partialTicks);
    }

    @Override
    public void onPutOn(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTakeOff(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpdate(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
    {
        // TODO Auto-generated method stub

    }

}
