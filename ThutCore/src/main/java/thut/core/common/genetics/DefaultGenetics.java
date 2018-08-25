package thut.core.common.genetics;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.util.ResourceLocation;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;

public class DefaultGenetics implements IMobGenetics
{
    Random                         rand     = new Random();
    Map<ResourceLocation, Alleles> genetics = Maps.newHashMap();
    Set<Alleles>                   epigenes;

    public DefaultGenetics()
    {
    }

    @Override
    public Map<ResourceLocation, Alleles> getAlleles()
    {
        return genetics;
    }

    @Override
    public void setFromParents(IMobGenetics parent1, IMobGenetics parent2)
    {
        Map<ResourceLocation, Alleles> genetics1 = parent1.getAlleles();
        Map<ResourceLocation, Alleles> genetics2 = parent2.getAlleles();
        for (Alleles a1 : genetics1.values())
        {
            // Get the key from here.
            Gene gene1 = a1.getExpressed();
            Alleles a2 = genetics2.get(gene1.getKey());
            if (a2 != null)
            {
                // Get expressed gene for checking epigenetic rate first.
                Gene gene2 = a2.getExpressed();

                // Get the genes based on if epigenes or not.
                gene1 = gene1.getEpigeneticRate() < rand.nextFloat() ? gene1 : a1.getAlleles()[rand.nextInt(2)];
                gene2 = gene2.getEpigeneticRate() < rand.nextFloat() ? gene2 : a2.getAlleles()[rand.nextInt(2)];

                // Apply mutations if needed.
                if (gene1.getMutationRate() > rand.nextFloat()) gene1 = gene1.mutate(parent1, parent2);
                if (gene2.getMutationRate() > rand.nextFloat()) gene2 = gene2.mutate(parent1, parent2);

                // Make the new allele.
                Alleles allele = new Alleles(gene1, gene2);
                getAlleles().put(gene1.getKey(), allele);
            }
        }
    }

    @Override
    public Set<Alleles> getEpigenes()
    {
        if (epigenes == null)
        {
            epigenes = Sets.newHashSet();
            for (Alleles a : genetics.values())
            {
                if (a.getExpressed().getEpigeneticRate() > 0)
                {
                    epigenes.add(a);
                }
            }
        }
        return epigenes;
    }
}
