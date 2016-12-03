package thut.core.common.genetics.genes;

import net.minecraft.nbt.NBTTagCompound;
import thut.api.entity.genetics.Gene;

public abstract class GeneBoolean implements Gene
{
    protected Boolean value = Boolean.FALSE;

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue()
    {
        return (T) value;
    }

    @Override
    public <T> void setValue(T value)
    {
        this.value = (Boolean) value;
    }

    @Override
    public NBTTagCompound save()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("V", value);
        return tag;
    }

    @Override
    public void load(NBTTagCompound tag)
    {
        value = tag.getBoolean("V");
    }

    @Override
    public boolean isEpigenetic()
    {
        return false;
    }

}
