package thut.wearables.inventory;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import thut.wearables.EnumWearable;

public class PlayerWearables implements IWearableInventory, IInventory
{
    private static class WearableSlot
    {
        final EnumWearable type;
        final ItemStack[]  slots;

        WearableSlot(EnumWearable type)
        {
            this.type = type;
            this.slots = new ItemStack[type.slots];
        }

        ItemStack getStack(int slot)
        {
            return slots[slot];
        }

        ItemStack getStack()
        {
            for (int i = 0; i < slots.length; i++)
                if (slots[i] != null) return slots[i];
            return null;
        }

        void setStack(int slot, ItemStack stack)
        {
            slots[slot] = stack;
        }

        ItemStack removeStack()
        {
            for (int i = 0; i < slots.length; i++)
                if (slots[i] != null)
                {
                    ItemStack stack = slots[i];
                    slots[i] = null;
                    return stack;
                }
            return null;
        }

        boolean addStack(ItemStack stack)
        {
            for (int i = 0; i < slots.length; i++)
                if (slots[i] == null)
                {
                    slots[i] = stack;
                    return true;
                }
            return false;
        }

        NBTTagCompound saveToNBT()
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setByte("type", (byte) type.ordinal());
            for (int n = 0; n < slots.length; n++)
            {
                ItemStack i = slots[n];
                if (i != null)
                {
                    NBTTagCompound tag1 = new NBTTagCompound();
                    i.writeToNBT(tag1);
                    tag.setTag("slot" + n, tag1);
                }
            }
            return tag;
        }

        void loadFromNBT(NBTTagCompound tag)
        {
            for (int n = 0; n < slots.length; n++)
            {
                NBTBase temp = tag.getTag("slot" + n);
                if (temp instanceof NBTTagCompound)
                {
                    NBTTagCompound tag1 = (NBTTagCompound) temp;
                    slots[n] = ItemStack.loadItemStackFromNBT(tag1);
                }
            }
        }

        public ItemStack removeStack(int subIndex)
        {
            if (slots[subIndex] != null)
            {
                ItemStack stack = slots[subIndex];
                slots[subIndex] = null;
                return stack;
            }
            return null;
        }
    }

    private Map<EnumWearable, WearableSlot> slots = Maps.newHashMap();

    public PlayerWearables()
    {
        for (EnumWearable type : EnumWearable.values())
            slots.put(type, new WearableSlot(type));
    }

    @Override
    public ItemStack getWearable(EnumWearable type, int slot)
    {
        return slots.get(type).getStack(slot);
    }

    @Override
    public ItemStack getWearable(EnumWearable type)
    {
        return slots.get(type).getStack();
    }

    @Override
    public Set<ItemStack> getWearables()
    {
        Set<ItemStack> ret = Sets.newHashSet();
        for (WearableSlot slot : slots.values())
        {
            for (int i = 0; i < slot.slots.length; i++)
            {
                if (slot.slots[i] != null) ret.add(slot.slots[i]);
            }
        }
        return ret;
    }

    @Override
    public boolean setWearable(EnumWearable type, ItemStack stack, int slot)
    {
        WearableSlot wSlot = slots.get(type);
        if (stack == null)
        {
            if (wSlot.getStack(slot) == null) return false;
            wSlot.setStack(slot, stack);
            return true;
        }
        if (wSlot.getStack(slot) != null) return false;
        wSlot.setStack(slot, stack);
        return true;
    }

    @Override
    public boolean setWearable(EnumWearable type, ItemStack stack)
    {
        WearableSlot wSlot = slots.get(type);
        if (stack == null)
        {
            if (wSlot.getStack() == null) return false;
            wSlot.removeStack();
            return true;
        }
        return wSlot.addStack(stack);
    }

    public String dataFileName()
    {
        return "wearables";
    }

    public void writeToNBT(NBTTagCompound tag)
    {
        for (EnumWearable slot : slots.keySet())
        {
            NBTTagCompound compound = slots.get(slot).saveToNBT();
            tag.setTag(slot.ordinal() + "", compound);
        }
    }

    public void readFromNBT(NBTTagCompound tag)
    {
        for (EnumWearable type : EnumWearable.values())
            slots.put(type, new WearableSlot(type));
        for (EnumWearable slot : slots.keySet())
        {
            NBTTagCompound compound = tag.getCompoundTag(slot.ordinal() + "");
            slots.get(slot).loadFromNBT(compound);
        }
    }

    @Override
    public String getName()
    {
        return "wearables";
    }

    @Override
    public boolean hasCustomName()
    {
        return false;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return new TextComponentTranslation("pokecube.wearables");
    }

    @Override
    public int getSizeInventory()
    {
        return 13;
    }

    @Override
    public ItemStack getStackInSlot(int index)
    {
        return slots.get(EnumWearable.getWearable(index)).getStack(EnumWearable.getSubIndex(index));
    }

    @Override
    public ItemStack decrStackSize(int index, int count)
    {
        return removeStackFromSlot(index);
    }

    @Override
    public ItemStack removeStackFromSlot(int index)
    {
        return slots.get(EnumWearable.getWearable(index)).removeStack(EnumWearable.getSubIndex(index));
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        slots.get(EnumWearable.getWearable(index)).setStack(EnumWearable.getSubIndex(index), stack);
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }

    @Override
    public void markDirty()
    {
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
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return EnumWearable.getSlot(stack) == EnumWearable.getWearable(index);
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
    }

}
