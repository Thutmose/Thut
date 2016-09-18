package pokecube.alternative.capabilities;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public interface IBeltCapability
{
    public static class Storage implements Capability.IStorage<IBeltCapability>
    {
        @Override
        public void readNBT(Capability<IBeltCapability> capability, IBeltCapability instance, EnumFacing side,
                NBTBase nbt)
        {
            if (nbt instanceof NBTTagCompound)
            {
                NBTTagCompound compound = (NBTTagCompound) nbt;
                for (int n = 0; n < 6; n++)
                {
                    NBTBase temp = compound.getTag("slot" + n);
                    if (temp instanceof NBTTagCompound)
                    {
                        NBTTagCompound tag = (NBTTagCompound) temp;
                        instance.setCube(n, ItemStack.loadItemStackFromNBT(tag));
                    }
                }
                instance.setSlot(compound.getInteger("selectedSlot"));
            }
        }

        @Override
        public NBTBase writeNBT(Capability<IBeltCapability> capability, IBeltCapability instance, EnumFacing side)
        {
            NBTTagCompound ret = new NBTTagCompound();
            for (int n = 0; n < 6; n++)
            {
                ItemStack i = instance.getCube(n);
                if (i != null)
                {
                    NBTTagCompound tag = new NBTTagCompound();
                    i.writeToNBT(tag);
                    ret.setTag("slot" + n, tag);
                }
            }
            ret.setInteger("selectedSlot", instance.getSlot());
            return ret;
        }
    }

    ItemStack getCube(int index);

    void setCube(int index, ItemStack stack);

    int getSlot();

    void setSlot(int index);

}
