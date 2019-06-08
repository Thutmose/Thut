package thut.tech.common.handlers;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.tech.Reference;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.entity.EntityLift;

public class EnergyHandler
{
    private static final ResourceLocation ENERGY = new ResourceLocation(Reference.MOD_ID, "energy");

    /** Pretty standard storable EnergyStorage. */
    public static class ProviderLift extends EnergyStorage implements ICapabilitySerializable<NBTTagCompound>
    {
        public ProviderLift()
        {
            super(ConfigHandler.maxLiftEnergy, ConfigHandler.maxLiftEnergy);
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == CapabilityEnergy.ENERGY;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return (capability == CapabilityEnergy.ENERGY) ? CapabilityEnergy.ENERGY.cast(this) : null;
        }

        @Override
        public NBTTagCompound serializeNBT()
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("E", getEnergyStored());
            return tag;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt)
        {
            this.energy = nbt.getInteger("E");
        }
    }

    /** This is essentially a wrapper for the lift's energy storage capability.
     * This allows interfacing with the lift's energy via any of the connected
     * controllers. */
    public static class ProviderLiftController implements ICapabilityProvider, IEnergyStorage
    {
        final TileEntityLiftAccess tile;
        IEnergyStorage             lift = null;

        public ProviderLiftController(TileEntityLiftAccess tile)
        {
            this.tile = tile;
        }

        private void updateLift()
        {
            if (tile.lift == null)
            {
                lift = null;
            }
            else
            {
                lift = tile.lift.getCapability(CapabilityEnergy.ENERGY, null);
            }
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == CapabilityEnergy.ENERGY;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return hasCapability(capability, facing) ? CapabilityEnergy.ENERGY.cast(this) : null;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate)
        {
            updateLift();
            if (lift != null) return lift.receiveEnergy(maxReceive, simulate);
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate)
        {
            updateLift();
            if (lift != null) return lift.extractEnergy(maxExtract, simulate);
            return 0;
        }

        @Override
        public int getEnergyStored()
        {
            updateLift();
            if (lift != null) return lift.getEnergyStored();
            return 0;
        }

        @Override
        public int getMaxEnergyStored()
        {
            updateLift();
            if (lift != null) return lift.getMaxEnergyStored();
            return 0;
        }

        @Override
        public boolean canExtract()
        {
            updateLift();
            if (lift != null) return lift.canExtract();
            return false;
        }

        @Override
        public boolean canReceive()
        {
            updateLift();
            if (lift != null) return lift.canReceive();
            return false;
        }
    }

    @SubscribeEvent
    /** Adds the energy capability to the lift mobs. */
    public void onEntityCapabilityAttach(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof EntityLift)
        {
            event.addCapability(ENERGY, new ProviderLift());
        }
    }

    @SubscribeEvent
    /** Adds the energy capability to the lift controllers. */
    public void onTileCapabilityAttach(AttachCapabilitiesEvent<TileEntity> event)
    {
        if (event.getObject() instanceof TileEntityLiftAccess)
        {
            event.addCapability(ENERGY, new ProviderLiftController((TileEntityLiftAccess) event.getObject()));
        }
    }
}
