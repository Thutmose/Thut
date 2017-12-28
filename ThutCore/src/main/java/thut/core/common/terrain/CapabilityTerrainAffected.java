package thut.core.common.terrain;

import java.util.Collection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thut.api.terrain.TerrainEffectEvent;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.api.terrain.TerrainSegment.ITerrainEffect;
import thut.reference.Reference;

public class CapabilityTerrainAffected
{
    private static final ResourceLocation            TERRAINEFFECTCAP = new ResourceLocation(Reference.MOD_ID,
            "terrainEffects");
    @CapabilityInject(ITerrainAffected.class)
    public static final Capability<ITerrainAffected> TERRAIN_CAP      = null;

    public static interface ITerrainAffected
    {
        void onTerrainTick();

        EntityLivingBase getAttached();

        void attach(EntityLivingBase mob);
    }

    public static class DefaultAffected implements ITerrainAffected, ICapabilityProvider
    {
        private EntityLivingBase           theMob;
        private TerrainSegment             terrain;
        private Collection<ITerrainEffect> effects;

        public void onTerrainEntry(TerrainSegment entered)
        {
            if (entered == terrain || theMob == null) return;
            terrain = entered;
            effects = terrain.getEffects();

            for (ITerrainEffect effect : effects)
            {
                TerrainEffectEvent event = new TerrainEffectEvent(theMob, effect.getIdenitifer(), true);
                if (!MinecraftForge.EVENT_BUS.post(event)) effect.doEffect(theMob, true);
            }
        }

        @Override
        public void onTerrainTick()
        {
            if (theMob == null) return;
            TerrainSegment current = TerrainManager.getInstance().getTerrainForEntity(theMob);
            if (current != terrain)
            {
                onTerrainEntry(current);
                return;
            }
            if (effects == null) return;
            for (ITerrainEffect effect : effects)
            {
                TerrainEffectEvent event = new TerrainEffectEvent(theMob, effect.getIdenitifer(), false);
                if (!MinecraftForge.EVENT_BUS.post(event)) effect.doEffect(theMob, false);
            }
        }

        @Override
        public EntityLivingBase getAttached()
        {
            return theMob;
        }

        @Override
        public void attach(EntityLivingBase mob)
        {
            theMob = mob;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == TERRAIN_CAP;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return hasCapability(capability, facing) ? TERRAIN_CAP.cast(this) : null;
        }

    }

    public CapabilityTerrainAffected()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onEntityCapabilityAttach(AttachCapabilitiesEvent<Entity> event)
    {
        if (!(event.getObject() instanceof EntityLivingBase) || event.getCapabilities().containsKey(TERRAINEFFECTCAP))
            return;
        DefaultAffected effects = new DefaultAffected();
        effects.attach((EntityLivingBase) event.getObject());
        event.addCapability(TERRAINEFFECTCAP, effects);
    }

    @SubscribeEvent
    public void EntityUpdate(LivingUpdateEvent evt)
    {
        ITerrainAffected effects = evt.getEntityLiving().getCapability(TERRAIN_CAP, null);
        if (effects != null)
        {
            effects.onTerrainTick();
        }
    }
}
