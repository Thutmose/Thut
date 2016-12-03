package thut.core.common.genetics.genes;

import net.minecraft.nbt.NBTTagCompound;
import thut.api.entity.genetics.Gene;

public abstract class GeneInteger implements Gene
{
    protected Integer value = new Integer(0);

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue()
    {
        return (T) value;
    }

    @Override
    public <T> void setValue(T value)
    {
        this.value = (Integer) value;
    }

    @Override
    public NBTTagCompound save()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("V", value);
        return tag;
    }

    @Override
    public void load(NBTTagCompound tag)
    {
        value = tag.getInteger("V");
    }

    @Override
    public boolean isEpigenetic()
    {
        return false;
    }

}
