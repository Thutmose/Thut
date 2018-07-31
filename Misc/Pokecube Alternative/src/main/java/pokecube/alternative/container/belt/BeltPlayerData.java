package pokecube.alternative.container.belt;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.entity.helper.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.entity.trainers.TypeTrainer;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.interfaces.IPokemob;
import thut.core.common.handlers.PlayerDataHandler.PlayerData;
import thut.lib.CompatWrapper;

public class BeltPlayerData extends PlayerData implements IPokemobBelt, IHasPokemobs, INBTSerializable<NBTTagCompound>
{
    public static class CapWrapper implements ICapabilitySerializable<NBTTagCompound>
    {
        public BeltPlayerData data;

        @Override
        public NBTTagCompound serializeNBT()
        {
            NBTTagCompound nbt = new NBTTagCompound();
            if (data != null) data.writeToNBT(nbt);
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt)
        {
            if (data != null) data.readFromNBT(nbt);
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return data != null && capability == CapabilityHasPokemobs.HASPOKEMOBS_CAP;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            if (hasCapability(capability, facing)) return CapabilityHasPokemobs.HASPOKEMOBS_CAP.cast(data);
            return null;
        }

        public void setData(BeltPlayerData data)
        {
            this.data = data;
            data.wrapper = this;
        }
    }

    public static BeltPlayerData getBelt(Entity player)
    {
        return PokecubePlayerDataHandler.getInstance().getPlayerData(player.getCachedUniqueIdString())
                .getData(BeltPlayerData.class);
    }

    public static void save(Entity player)
    {
        PokecubePlayerDataHandler.getInstance().save(player.getCachedUniqueIdString(), "pokealternative-belt");
    }

    TypeTrainer       type;
    public CapWrapper wrapper;

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

    /** These methods are for capability syncing. */
    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        readFromNBT(nbt);
    }

    /** These methods are for playerdata saving/loading. */
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
        nbt.setString("type", getType().name);
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
        setType(TypeTrainer.getTrainer(nbt.getString("type")));
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

    @Override
    public void setPokemob(int slot, ItemStack cube)
    {
        setCube(slot, cube);
    }

    @Override
    public ItemStack getPokemob(int slot)
    {
        return getCube(slot);
    }

    @Override
    public int getNextSlot()
    {
        return getSlot();
    }

    @Override
    public void setNextSlot(int value)
    {
        setSlot(value);
    }

    @Override
    public void resetPokemob()
    {
        // Nope
    }

    @Override
    public EntityLivingBase getTarget()
    {
        // Nope
        return null;
    }

    @Override
    public void lowerCooldowns()
    {
        // Nope
    }

    @Override
    public void throwCubeAt(Entity target)
    {
        // Nope
    }

    @Override
    public void setTarget(EntityLivingBase target)
    {
        // Nope
    }

    @Override
    public TypeTrainer getType()
    {
        if (type == null) type = TypeTrainer.getTrainer(null);
        return type;
    }

    @Override
    public void setType(TypeTrainer type)
    {
        this.type = type;
    }

    @Override
    public void onDefeated(Entity defeater)
    {
        // Nope
    }

    @Override
    public void onAddMob()
    {
        // Nope
    }

    @Override
    public long getCooldown()
    {
        // Nope
        return 0;
    }

    @Override
    public void setCooldown(long value)
    {
        // Nope
    }

    @Override
    public int getAttackCooldown()
    {
        // Nope
        return 0;
    }

    @Override
    public void setAttackCooldown(int value)
    {
        // Nope
    }

    @Override
    public void setOutMob(IPokemob mob)
    {
        // Nope
    }

    @Override
    public IPokemob getOutMob()
    {
        // Nope
        return null;
    }

    @Override
    public void setOutID(UUID mob)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public UUID getOutID()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean canBattle(EntityLivingBase target)
    {
        // Nope
        return true;
    }

    @Override
    public byte getGender()
    {
        // Nope
        return 0;
    }

    @Override
    public void setGender(byte value)
    {
        // Nope
    }

    @Override
    public boolean canMegaEvolve()
    {
        // Nope
        return true;
    }

    @Override
    public void setCanMegaEvolve(boolean flag)
    {
        // Nope
    }

    @Override
    public void setLevelMode(LevelMode type)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public LevelMode getLevelMode()
    {
        // TODO Auto-generated method stub
        return LevelMode.YES;
    }
}
