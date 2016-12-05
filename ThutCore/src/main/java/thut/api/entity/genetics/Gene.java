package thut.api.entity.genetics;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public interface Gene
{
    /** This method should return the new gene which results from mixing other
     * with this gene. */
    Gene interpolate(Gene other);

    /** This method should return a mutated gene, if the gene does not mutate,
     * mutate could return this. */
    Gene mutate();

    /** @return the value of this gene. */
    <T> T getValue();

    /** @param value
     *            Sets the value of the gene. */
    <T> void setValue(T value);

    /** This is how frequently the expressed gene is used instead of the
     * parent's genes.
     * 
     * @return value from 0-1 of how often it uses expressed gene. */
    default float getEpigeneticRate()
    {
        return 0;
    }

    /** @return nbttag compount for saving. */
    NBTTagCompound save();

    /** Loads the data from tag.
     * 
     * @param tag */
    void load(NBTTagCompound tag);

    /** @return key to correspond to this class of Gene. This should return the
     *         same value for every instance of this class. */
    ResourceLocation getKey();
}
