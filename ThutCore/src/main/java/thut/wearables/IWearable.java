package thut.wearables;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IWearable
{
    EnumWearable getSlot(ItemStack stack);

    @SideOnly(Side.CLIENT)
    /** This is called after doing the main transforms needed to get the gl
     * calls to the correct spot.
     * 
     * @param wearer
     *            - The entity wearing the stack
     * @param stack
     *            - The stack being worn */
    void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks);
}
