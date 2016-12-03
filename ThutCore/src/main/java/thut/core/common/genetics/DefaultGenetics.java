package thut.core.common.genetics;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.util.ResourceLocation;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;

public class DefaultGenetics implements IMobGenetics
{

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
            Gene gene1 = a1.getExpressed();
            Alleles a2 = genetics2.get(gene1.getKey());
            if (a2 != null)
            {
                Gene gene2 = a2.getExpressed();
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
                if (a.getExpressed().isEpigenetic())
                {
                    epigenes.add(a);
                }
            }
        }
        return epigenes;
    }
}
