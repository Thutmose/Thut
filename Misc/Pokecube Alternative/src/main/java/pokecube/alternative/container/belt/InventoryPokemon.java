package pokecube.alternative.container.belt;

import java.lang.ref.WeakReference;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import pokecube.alternative.network.PacketHandler;
import pokecube.alternative.network.PacketSyncBelt;

public class InventoryPokemon implements IInventory
{

    public final IPokemobBelt          cap;
    private Container                  eventHandler;
    public WeakReference<EntityPlayer> player;
    public boolean                     blockEvents = false;

    public InventoryPokemon(EntityPlayer player)
    {
        cap = BeltPlayerData.getBelt(player);
        this.player = new WeakReference<EntityPlayer>(player);
    }

    public Container getEventHandler()
    {
        return eventHandler;
    }

    public void setEventHandler(Container eventHandler)
    {
        this.eventHandler = eventHandler;
    }

    @Override
    public int getSizeInventory()
    {
        return 6;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex)
    {
        return slotIndex >= this.getSizeInventory() ? null : cap.getCube(slotIndex);
    }

    @Override
    public String getName()
    {
        return "";
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return null;
    }

    @Override
    public ItemStack removeStackFromSlot(int slotIndex)
    {
        if (cap.getCube(slotIndex) != null)
        {
            ItemStack itemStack = cap.getCube(slotIndex);
            cap.setCube(slotIndex, null);
            return itemStack;
        }
        return null;
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        if (this.getStackInSlot(index) != null)
        {
            ItemStack itemstack;

            if (this.getStackInSlot(index).stackSize <= count)
            {
                itemstack = this.getStackInSlot(index);
                this.setInventorySlotContents(index, null);
                this.markDirty();
                return itemstack;
            }
            itemstack = this.getStackInSlot(index).splitStack(count);

            if (this.getStackInSlot(index).stackSize <= 0)
            {
                this.setInventorySlotContents(index, null);
            }
            else
            {
                // Just to show that changes happened
                this.setInventorySlotContents(index, this.getStackInSlot(index));
            }

            this.markDirty();
            return itemstack;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (index < 0 || index >= this.getSizeInventory()) return;

        if (stack != null && stack.stackSize > this.getInventoryStackLimit())
            stack.stackSize = this.getInventoryStackLimit();

        if (stack != null && stack.stackSize == 0) stack = null;
        cap.setCube(index, stack);
        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }

    @Override
    public void markDirty()
    {
        try
        {
            player.get().inventory.markDirty();
        }
        catch (Exception e)
        {

        }
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
        System.out.println("close");
        saveCapability();
    }

    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack stack)
    {
        return false;
    }

    @Override
    public int getField(int id)
    {
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {

    }

    @Override
    public int getFieldCount()
    {
        return 0;
    }

    @Override
    public void clear()
    {
        for (int i = 0; i < 6; i++)
        {
            cap.setCube(i, null);
        }
    }

    public void saveCapability()
    {
        syncToClients();
    }

    @Deprecated
    public void readCapability()
    {
        // IBeltCapability cap =
        // player.get().getCapability(EventHandlerCommon.BELTAI_CAP, null);
        // for(int c = 0; c < 6; c++) {
        // this.stackList[c] = cap.getCube(c);
        // }
    }

    public void syncToClients()
    {
        try
        {
            System.out.println("Sync");
            if (player.get().isServerWorld())
            {
                System.out.println("Send");
                IPokemobBelt cap = BeltPlayerData.getBelt(player.get());
                PacketHandler.INSTANCE.sendToAll(new PacketSyncBelt(cap, player.get().getEntityId()));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
