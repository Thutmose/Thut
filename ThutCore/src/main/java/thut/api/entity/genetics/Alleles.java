package thut.api.entity.genetics;

import net.minecraft.nbt.NBTTagCompound;

public class Alleles
{
    final Gene[] alleles = new Gene[2];
    Gene         expressed;

    public Alleles()
    {
    }

    public Alleles(Gene gene1, Gene gene2)
    {
        alleles[0] = gene1;
        alleles[1] = gene2;
        if (gene1 == null || gene2 == null) throw new IllegalStateException("Genes cannot be null");
    }

    /** This returns two Allele, one represeting each parent.
     * 
     * @return */
    public Gene[] getAlleles()
    {
        return alleles;
    }

    @SuppressWarnings("unchecked")
    public <T extends Gene> T getExpressed()
    {
        if (expressed == null)
        {
            refreshExpressed();
        }
        return (T) expressed;
    }

    public void refreshExpressed()
    {
        if (alleles[0] == null || alleles[1] == null) throw new IllegalStateException("Genes cannot be null");
        expressed = alleles[0].mutate().interpolate(alleles[1].mutate());
    }

    public void load(NBTTagCompound tag) throws Exception
    {
        expressed = GeneRegistry.load(tag.getCompoundTag("expressed"));
        getAlleles()[0] = GeneRegistry.load(tag.getCompoundTag("gene1"));
        getAlleles()[1] = GeneRegistry.load(tag.getCompoundTag("gene2"));
    }

    public NBTTagCompound save()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("expressed", GeneRegistry.save(getExpressed()));
        tag.setTag("gene1", GeneRegistry.save(getAlleles()[0]));
        tag.setTag("gene2", GeneRegistry.save(getAlleles()[1]));
        return tag;
    }
}
