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
        return this.genetics;
    }

    @Override
    public Set<Alleles> getEpigenes()
    {
        if (this.epigenes == null)
        {
            this.epigenes = Sets.newHashSet();
            for (final Alleles a : this.genetics.values())
                if (a.getExpressed().getEpigeneticRate() > 0) this.epigenes.add(a);
        }
        return this.epigenes;
    }

    @Override
    public void setFromParents(final IMobGenetics parent1, final IMobGenetics parent2)
    {
        final Map<ResourceLocation, Alleles> genetics1 = parent1.getAlleles();
        final Map<ResourceLocation, Alleles> genetics2 = parent2.getAlleles();
        for (final Alleles a1 : genetics1.values())
        {
            // Get the key from here.
            Gene gene1 = a1.getExpressed();
            final Alleles a2 = genetics2.get(gene1.getKey());
            if (a2 != null)
            {
                // Get expressed gene for checking epigenetic rate first.
                Gene gene2 = a2.getExpressed();

                // Get the genes based on if epigenes or not.
                gene1 = gene1.getEpigeneticRate() > this.rand.nextFloat() ? gene1
                        : a1.getAlleles()[this.rand.nextInt(2)];
                gene2 = gene2.getEpigeneticRate() > this.rand.nextFloat() ? gene2
                        : a2.getAlleles()[this.rand.nextInt(2)];

                // Apply mutations if needed.
                if (gene1.getMutationRate() > this.rand.nextFloat()) gene1 = gene1.mutate(parent1, parent2);
                if (gene2.getMutationRate() > this.rand.nextFloat()) gene2 = gene2.mutate(parent1, parent2);

                // Make the new allele.
                final Alleles allele = new Alleles(gene1, gene2);
                this.getAlleles().put(gene1.getKey(), allele);
            }
        }
    }
}
