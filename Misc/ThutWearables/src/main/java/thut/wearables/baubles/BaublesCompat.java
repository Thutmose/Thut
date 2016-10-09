package thut.wearables.baubles;

import baubles.api.IBauble;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;

public class BaublesCompat
{
    public static boolean botania = false;

    public BaublesCompat()
    {
    }

    @SubscribeEvent
    public void onItemCapabilityAttach(AttachCapabilitiesEvent.Item event)
    {
        if (botania)
        {
            MinecraftForge.EVENT_BUS.unregister(this);
            return;
        }
        if (event.getItem() instanceof IBauble
                && !event.getCapabilities().containsKey(new ResourceLocation("wearable_compat:bauble")))
        {
            event.addCapability(new ResourceLocation("wearable_compat:bauble"), new WearableBauble());
        }
    }

    public static class WearableBauble implements IActiveWearable, ICapabilityProvider
    {
        @Override
        public EnumWearable getSlot(ItemStack stack)
        {
            if (!(stack.getItem() instanceof IBauble)) { return null; }
            IBauble bauble = (IBauble) stack.getItem();
            switch (bauble.getBaubleType(stack))
            {
            case AMULET:
                return EnumWearable.NECK;
            case BELT:
                return EnumWearable.WAIST;
            case RING:
                return EnumWearable.FINGER;
            default:
                break;
            }
            return EnumWearable.WRIST;
        }

        @SideOnly(Side.CLIENT)
        @Override
        public void renderWearable(EnumWearable slot, EntityLivingBase wearer, ItemStack stack, float partialTicks)
        {
            System.out.println(stack);
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == WEARABLE_CAP;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            if (WEARABLE_CAP != null && capability == WEARABLE_CAP) return (T) this;
            return null;
        }

        @Override
        public void onPutOn(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
        {
            IBauble bauble = (IBauble) itemstack.getItem();
            bauble.onEquipped(itemstack, player);
        }

        @Override
        public void onTakeOff(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
        {
            IBauble bauble = (IBauble) itemstack.getItem();
            bauble.onUnequipped(itemstack, player);
        }

        @Override
        public void onUpdate(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
        {
            IBauble bauble = (IBauble) itemstack.getItem();
            bauble.onWornTick(itemstack, player);
        }

    }
}
