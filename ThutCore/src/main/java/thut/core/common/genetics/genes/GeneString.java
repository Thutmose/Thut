package thut.core.common.genetics.genes;

import net.minecraft.nbt.CompoundNBT;
import thut.api.entity.genetics.Gene;

public abstract class GeneString implements Gene
{
    protected String value = "";

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue()
    {
        return (T) value;
    }

    @Override
    public <T> void setValue(T value)
    {
        this.value = (String) value;
    }

    @Override
    public CompoundNBT save()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("V", value);
        return tag;
    }

    @Override
    public void load(CompoundNBT tag)
    {
        value = tag.getString("V");
    }

}
