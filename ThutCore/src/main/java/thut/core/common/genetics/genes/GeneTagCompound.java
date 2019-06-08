package thut.core.common.genetics.genes;

import net.minecraft.nbt.CompoundNBT;
import thut.api.entity.genetics.Gene;

public abstract class GeneTagCompound implements Gene
{
    protected CompoundNBT value = new CompoundNBT();

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getValue()
    {
        return (T) value;
    }

    @Override
    public <T> void setValue(T value)
    {
        this.value = (CompoundNBT) value;
    }

    @Override
    public CompoundNBT save()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.setTag("V", value);
        return tag;
    }

    @Override
    public void load(CompoundNBT tag)
    {
        value = tag.getCompound("V");
    }

}
