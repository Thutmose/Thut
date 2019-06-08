package thut.core.common.genetics.genes;

import net.minecraft.nbt.CompoundNBT;
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
    public CompoundNBT save()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putFloat("V", value);
        return tag;
    }

    @Override
    public void load(CompoundNBT tag)
    {
        value = tag.getFloat("V");
    }

    @Override
    public String toString()
    {
        return "" + value;
    }

}
