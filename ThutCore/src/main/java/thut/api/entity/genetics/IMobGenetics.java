package thut.api.entity.genetics;

import java.util.Map;
import java.util.Set;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public interface IMobGenetics
{
    @CapabilityInject(IMobGenetics.class)
    public static final Capability<IMobGenetics> GENETICS_CAP = null;

    /** This is a map of Name -> Alleles. this is to be used to sort the
     * Alleles. The keys for this should be the same as they key registed in
     * GeneRegistry
     * 
     * @return */
    Map<ResourceLocation, Alleles> getAlleles();

    /** This should return a set of genes which are epigenetic, this allows the
     * holder to edit them before saving them if needed.
     * 
     * @return */
    Set<Alleles> getEpigenes();

    void setFromParents(IMobGenetics parent1, IMobGenetics parent2);

    /** This is called whenever the mob associated with this gene ticks.
     * 
     * @param mob */
    default void onUpdateTick(LivingEntity mob)
    {
        for (Alleles allele : getAlleles().values())
        {
            allele.getExpressed().onUpdateTick(mob);
        }
    }
}
