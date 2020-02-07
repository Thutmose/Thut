package thut.tech.common.handlers;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thut.tech.Reference;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.lift.ControllerTile;
import thut.tech.common.entity.EntityLift;

@Mod.EventBusSubscriber
public class EnergyHandler
{
    /** Pretty standard storable EnergyStorage. */
    public static class ProviderLift extends EnergyStorage implements ICapabilitySerializable<CompoundNBT>
    {
        private final LazyOptional<IEnergyStorage> holder = LazyOptional.of(() -> this);

        public ProviderLift()
        {
            super(TechCore.config.maxLiftEnergy, TechCore.config.maxLiftEnergy);
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt)
        {
            this.energy = nbt.getInt("E");
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing)
        {
            return CapabilityEnergy.ENERGY.orEmpty(capability, this.holder);
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            final CompoundNBT tag = new CompoundNBT();
            tag.putInt("E", this.getEnergyStored());
            return tag;
        }
    }

    /**
     * This is essentially a wrapper for the lift's energy storage capability.
     * This allows interfacing with the lift's energy via any of the connected
     * controllers.
     */
    public static class ProviderLiftController implements ICapabilityProvider, IEnergyStorage
    {
        private final LazyOptional<IEnergyStorage> holder = LazyOptional.of(() -> this);
        final ControllerTile                 tile;
        IEnergyStorage                             lift   = null;

        public ProviderLiftController(ControllerTile tile)
        {
            this.tile = tile;
        }

        @Override
        public boolean canExtract()
        {
            this.updateLift();
            if (this.lift != null) return this.lift.canExtract();
            return false;
        }

        @Override
        public boolean canReceive()
        {
            this.updateLift();
            if (this.lift != null) return this.lift.canReceive();
            return false;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate)
        {
            this.updateLift();
            if (this.lift != null) return this.lift.extractEnergy(maxExtract, simulate);
            return 0;
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing)
        {
            return CapabilityEnergy.ENERGY.orEmpty(capability, this.holder);
        }

        @Override
        public int getEnergyStored()
        {
            this.updateLift();
            if (this.lift != null) return this.lift.getEnergyStored();
            return 0;
        }

        @Override
        public int getMaxEnergyStored()
        {
            this.updateLift();
            if (this.lift != null) return this.lift.getMaxEnergyStored();
            return 0;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate)
        {
            this.updateLift();
            if (this.lift != null) return this.lift.receiveEnergy(maxReceive, simulate);
            return 0;
        }

        private void updateLift()
        {
            if (this.tile.getLift() == null) this.lift = null;
            else this.lift = this.tile.getLift().getCapability(CapabilityEnergy.ENERGY, null).orElse(null);
        }
    }

    private static final ResourceLocation ENERGY = new ResourceLocation(Reference.MOD_ID, "energy");

    @SubscribeEvent
    /** Adds the energy capability to the lift mobs. */
    public static void onEntityCapabilityAttach(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof EntityLift) event.addCapability(EnergyHandler.ENERGY, new ProviderLift());
    }

    @SubscribeEvent
    /** Adds the energy capability to the lift controllers. */
    public static void onTileCapabilityAttach(AttachCapabilitiesEvent<TileEntity> event)
    {
        if (event.getObject() instanceof ControllerTile) event.addCapability(EnergyHandler.ENERGY,
                new ProviderLiftController((ControllerTile) event.getObject()));
    }
}
