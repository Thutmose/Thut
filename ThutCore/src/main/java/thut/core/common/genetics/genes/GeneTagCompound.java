package thut.core.common.genetics.genes;

import net.minecraft.nbt.NBTTagCompound;
import thut.api.entity.genetics.Gene;

public abstract class GeneTagCompound implements Gene
{
    protected NBTTagCompound value = new NBTTagCompound();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue()
    {
        return (T) value;
    }

    @Override
    public <T> void setValue(T value)
    {
        this.value = (NBTTagCompound) value;
    }

    @Override
    public NBTTagCompound save()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("V", value);
        return tag;
    }

    @Override
    public void load(NBTTagCompound tag)
    {
        value = tag.getCompoundTag("V");
    }

}
