package pokecube.alternative.container.belt;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;
import thut.lib.CompatWrapper;

public class BeltPlayerData extends PlayerData implements IPokemobBelt
{
    public static BeltPlayerData getBelt(Entity player)
    {
        return PokecubePlayerDataHandler.getInstance().getPlayerData(player.getCachedUniqueIdString())
                .getData(BeltPlayerData.class);
    }

    public static void save(Entity player)
    {
        PokecubePlayerDataHandler.getInstance().save(player.getCachedUniqueIdString(), "pokealternative-belt");
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
            if (CompatWrapper.isValid(i))
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
                setCube(n, CompatWrapper.fromTag(tag));
            }
        }
        setSlot(compound.getInteger("selectedSlot"));
    }

    List<ItemStack> cubes   = CompatWrapper.makeList(6);
    int             slot    = 0;
    UUID[]          slotIDs = new UUID[6];

    @Override
    public ItemStack getCube(int index)
    {
        return cubes.get(index);
    }

    @Override
    public void setCube(int index, ItemStack stack)
    {
        cubes.set(index, stack);
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

    @Override
    public void setSlotID(int index, UUID id)
    {
        slotIDs[index] = id;
    }

    @Override
    public UUID getSlotID(int index)
    {
        return slotIDs[index];
    }
}
