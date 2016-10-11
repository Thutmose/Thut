package thut.wearables.baubles;

import java.util.List;

import baubles.api.IBauble;
import baubles.common.container.ContainerPlayerExpanded;
import baubles.common.container.InventoryBaubles;
import baubles.common.container.SlotBauble;
import baubles.common.lib.PlayerHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.ContainerWearables;
import thut.wearables.inventory.ContainerWearables.WornSlot;
import thut.wearables.inventory.PlayerWearables;

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

    @SubscribeEvent
    public void openContainer(PlayerOpenContainerEvent event)
    {
        Container cont = event.getEntityPlayer().openContainer;
        if (!(cont instanceof ContainerWearables || cont instanceof ContainerPlayerExpanded)) return;
        // System.out.println(event.getEntityPlayer().openContainer);
        List<IContainerListener> listeners = ReflectionHelper.getPrivateValue(Container.class, cont, "listeners");
        if (listeners.size() == 1)
        {
            final EntityPlayerMP player = (EntityPlayerMP) listeners.get(0);
            if (cont instanceof ContainerWearables) listeners.add(new IContainerListener()
            {

                @Override
                public void updateCraftingInventory(Container containerToSend, List<ItemStack> itemsList)
                {
                }

                @Override
                public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack)
                {
                    if (!(containerToSend.getSlot(slotInd) instanceof WornSlot)) return;
                    int slotId = slotInd - 41;
                    int[] idMap = { -1, -1, -1, -1, -1, -1, 1, 0, 2, -1, 3, -1, -1 };
                    slotId = idMap[slotId];
                    if (slotId == -1) return;
                    if (stack != null)
                    {
                        // Putting into slot
                        if (stack.getItem() instanceof IBauble)
                        {
                            IInventory baubles = new InventoryBaubles(player);
                            if (!player.worldObj.isRemote)
                            {
                                ((InventoryBaubles) baubles).stackList = PlayerHandler
                                        .getPlayerBaubles(player).stackList;
                            }
                            baubles.setInventorySlotContents(slotId, stack.copy());
                        }
                    }
                    else
                    {
                        // Taking out of slot
                        stack = player.inventory.getItemStack();
                        if (stack.getItem() instanceof IBauble)
                        {
                            IInventory baubles = new InventoryBaubles(player);
                            if (!player.worldObj.isRemote)
                            {
                                ((InventoryBaubles) baubles).stackList = PlayerHandler
                                        .getPlayerBaubles(player).stackList;
                            }
                            baubles.setInventorySlotContents(slotId, null);
                        }
                    }

                }

                @Override
                public void sendProgressBarUpdate(Container containerIn, int varToUpdate, int newValue)
                {
                }

                @Override
                public void sendAllWindowProperties(Container containerIn, IInventory inventory)
                {
                }
            });
            else
            {
                listeners.add(new IContainerListener()
                {

                    @Override
                    public void updateCraftingInventory(Container containerToSend, List<ItemStack> itemsList)
                    {
                    }

                    @Override
                    public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack)
                    {
                        if (!(containerToSend.getSlot(slotInd) instanceof SlotBauble)) return;
                        int slotId = slotInd - 9;
                        int[] idMap = { 8, 0, 1, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1 };
                        slotId = idMap[slotId];
                        System.out.println(slotId);
                        if (slotId == -1) return;
                        PlayerWearables wearables = ThutWearables.getWearables(player);
                        wearables.setInventorySlotContents(slotId, stack);
                        ThutWearables.syncWearables(player);
                    }

                    @Override
                    public void sendProgressBarUpdate(Container containerIn, int varToUpdate, int newValue)
                    {
                    }

                    @Override
                    public void sendAllWindowProperties(Container containerIn, IInventory inventory)
                    {
                    }
                });
            }
        }
        // System.out.println(listeners);
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
            // System.out.println(stack);
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
            // IBauble bauble = (IBauble) itemstack.getItem();
            // bauble.onEquipped(itemstack, player);
        }

        @Override
        public void onTakeOff(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
        {
            // IBauble bauble = (IBauble) itemstack.getItem();
            // bauble.onUnequipped(itemstack, player);
        }

        @Override
        public void onUpdate(EntityLivingBase player, ItemStack itemstack, EnumWearable slot, int subIndex)
        {
            // IBauble bauble = (IBauble) itemstack.getItem();
            // bauble.onWornTick(itemstack, player);
        }

    }
}
