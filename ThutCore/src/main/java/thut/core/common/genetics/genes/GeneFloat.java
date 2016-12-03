package thut.core.common.genetics.genes;

import net.minecraft.nbt.NBTTagCompound;
import thut.api.entity.genetics.Gene;

public abstract class GeneFloat implements Gene
{
    protected Float value = new Float(0);

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue()
    {
        return (T) value;
    }

    @Override
    public <T> void setValue(T value)
    {
        this.value = (Float) value;
    }

    @Override
    public NBTTagCompound save()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setFloat("V", value);
        return tag;
    }

    @Override
    public void load(NBTTagCompound tag)
    {
        value = tag.getFloat("V");
    }

    @Override
    public boolean isEpigenetic()
    {
        return false;
    }

}
