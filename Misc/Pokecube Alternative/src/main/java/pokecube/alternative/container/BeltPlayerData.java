package pokecube.alternative.container;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import pokecube.core.handlers.PlayerDataHandler;
import pokecube.core.handlers.PlayerDataHandler.PlayerData;

public class BeltPlayerData extends PlayerData implements IPokemobBelt
{
    public static BeltPlayerData getBelt(Entity player)
    {
        return PlayerDataHandler.getInstance().getPlayerData(player.getCachedUniqueIdString())
                .getData(BeltPlayerData.class);
    }

    public static void save(Entity player)
    {
        PlayerDataHandler.getInstance().save(player.getCachedUniqueIdString(), "pokealternative-belt");
    }

    public BeltPlayerData()
    {
    }

    @Override
    public String getIdentifier()
    {
        return "pokealternative-belt";
    }

    @Override
    public String dataFileName()
    {
        return "BeltInventory";
    }

    @Override
    public boolean shouldSync()
    {
        return true;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        for (int n = 0; n < 6; n++)
        {
            ItemStack i = getCube(n);
            if (i != null)
            {
                NBTTagCompound tag = new NBTTagCompound();
                i.writeToNBT(tag);
                nbt.setTag("slot" + n, tag);
            }
        }
        nbt.setInteger("selectedSlot", getSlot());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        NBTTagCompound compound = nbt;
        for (int n = 0; n < 6; n++)
        {
            NBTBase temp = compound.getTag("slot" + n);
            if (temp instanceof NBTTagCompound)
            {
                NBTTagCompound tag = (NBTTagCompound) temp;
                setCube(n, ItemStack.loadItemStackFromNBT(tag));
            }
        }
        setSlot(compound.getInteger("selectedSlot"));
    }

    ItemStack[] cubes = new ItemStack[6];
    int         slot  = 0;

    @Override
    public ItemStack getCube(int index)
    {
        return cubes[index];
    }

    @Override
    public void setCube(int index, ItemStack stack)
    {
        cubes[index] = stack;
    }

    @Override
    public int getSlot()
    {
        return slot;
    }

    @Override
    public void setSlot(int index)
    {
        slot = index;
    }

}
