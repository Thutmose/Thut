package pokecube.alternative.container.belt;

import java.lang.ref.WeakReference;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import pokecube.alternative.network.PacketHandler;
import pokecube.alternative.network.PacketSyncBelt;
import thut.lib.CompatWrapper;

public class InventoryPokemon implements IInventory
{

    public final IPokemobBelt          cap;
    private Container                  eventHandler;
    public WeakReference<EntityPlayer> player;

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
        return slotIndex >= this.getSizeInventory() ? CompatWrapper.nullStack : cap.getCube(slotIndex);
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
        if (CompatWrapper.isValid(cap.getCube(slotIndex)))
        {
            ItemStack itemStack = cap.getCube(slotIndex);
            cap.setCube(slotIndex, CompatWrapper.nullStack);
            return itemStack;
        }
        return CompatWrapper.nullStack;
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        if (CompatWrapper.isValid(this.getStackInSlot(index)))
        {
            ItemStack itemstack;
            if (CompatWrapper.getStackSize(this.getStackInSlot(index)) <= count)
            {
                itemstack = this.getStackInSlot(index);
                this.setInventorySlotContents(index, CompatWrapper.nullStack);
                this.markDirty();
                return itemstack;
            }
            itemstack = this.getStackInSlot(index).splitStack(count);

            if (!CompatWrapper.isValid(itemstack))
            {
                this.setInventorySlotContents(index, CompatWrapper.nullStack);
            }
            else
            {
                // Just to show that changes happened
                this.setInventorySlotContents(index, this.getStackInSlot(index));
            }

            this.markDirty();
            return itemstack;
        }
        return CompatWrapper.nullStack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (index < 0 || index >= this.getSizeInventory()) return;
        if (CompatWrapper.getStackSize(stack) > this.getInventoryStackLimit())
            CompatWrapper.setStackSize(stack, this.getInventoryStackLimit());
        if (!CompatWrapper.isValid(stack)) stack = CompatWrapper.nullStack;
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
            cap.setCube(i, CompatWrapper.nullStack);
        }
    }

    public void saveCapability()
    {
        syncToClients();
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

    public boolean func_191420_l()
    {
        // TODO Auto-generated method stub
        return false;
    }

}
