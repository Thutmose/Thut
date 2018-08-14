package pokecube.alternative.container.card;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;
import thut.lib.CompatWrapper;

public class CardPlayerData extends PlayerData
{
    public final InventoryBasic inventory = new InventoryBasic("pokecube-alternative.bag", false, 8);

    public CardPlayerData()
    {
    }

    @Override
    public String getIdentifier()
    {
        return "pokealternative-card";
    }

    @Override
    public String dataFileName()
    {
        return "TrainerCard";
    }

    @Override
    public boolean shouldSync()
    {
        return true;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        for (int n = 0; n < inventory.getSizeInventory(); n++)
        {
            ItemStack i = inventory.getStackInSlot(n);
            if (CompatWrapper.isValid(i))
            {
                NBTTagCompound tag = new NBTTagCompound();
                i.writeToNBT(tag);
                nbt.setTag("slot" + n, tag);
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        NBTTagCompound compound = nbt;
        for (int n = 0; n < inventory.getSizeInventory(); n++)
        {
            NBTBase temp = compound.getTag("slot" + n);
            if (temp instanceof NBTTagCompound)
            {
                NBTTagCompound tag = (NBTTagCompound) temp;
                inventory.setInventorySlotContents(n, new ItemStack(tag));
            }
        }
    }

}
