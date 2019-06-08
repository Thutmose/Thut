package thut.api.entity.genetics;

import java.util.Random;

import net.minecraft.nbt.CompoundNBT;

public class Alleles
{
    final Gene[] alleles = new Gene[2];
    final Random rand    = new Random();
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
        Gene a = alleles[0].getMutationRate() > rand.nextFloat() ? alleles[0].mutate() : alleles[0];
        Gene b = alleles[1].getMutationRate() > rand.nextFloat() ? alleles[1].mutate() : alleles[1];
        expressed = a.interpolate(b);
    }

    public void load(CompoundNBT tag) throws Exception
    {
        expressed = GeneRegistry.load(tag.getCompound("expressed"));
        getAlleles()[0] = GeneRegistry.load(tag.getCompound("gene1"));
        getAlleles()[1] = GeneRegistry.load(tag.getCompound("gene2"));
    }

    public CompoundNBT save()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.put("expressed", GeneRegistry.save(getExpressed()));
        tag.put("gene1", GeneRegistry.save(getAlleles()[0]));
        tag.put("gene2", GeneRegistry.save(getAlleles()[1]));
        return tag;
    }
}
