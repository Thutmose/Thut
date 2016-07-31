package thut.tech.common.tesla;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thut.tech.common.blocks.lift.EventLiftUpdate;
import thut.tech.common.blocks.lift.TileEntityLiftAccess;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.entity.EventLiftConsumePower;
import thut.tech.common.handlers.ConfigHandler;

public class TeslaHandler
{
    @CapabilityInject(ITeslaConsumer.class)
    public static Capability<ITeslaConsumer> TESLA_CONSUMER = null;
    @CapabilityInject(ITeslaProducer.class)
    public static Capability<ITeslaProducer> TESLA_PRODUCER = null;
    @CapabilityInject(ITeslaHolder.class)
    public static Capability<ITeslaHolder>   TESLA_HOLDER   = null;

    public TeslaHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTileEntityCapabilityAttach(AttachCapabilitiesEvent.TileEntity event)
    {
        if (event.getTileEntity() instanceof TileEntityLiftAccess)
        {
            class Provider
                    implements ICapabilitySerializable<NBTTagCompound>, ITeslaConsumer, ITeslaProducer, ITeslaHolder
            {
                /** The amount of stored Tesla power. */
                private long stored;

                /** The maximum amount of Tesla power that can be stored. */
                private long capacity;

                /** The maximum amount of Tesla power that can be accepted. */
                private long inputRate;

                /** The maximum amount of Tesla power that can be extracted */
                private long outputRate;

                public Provider()
                {
                    this.stored = 0;
                    this.capacity = 5000000;
                    this.inputRate = 5000;
                    this.outputRate = 5000;
                }

                @Override
                public void deserializeNBT(NBTTagCompound nbt)
                {
                    this.stored = nbt.getLong("TeslaPower");
                    if (nbt.hasKey("TeslaCapacity")) this.capacity = nbt.getLong("TeslaCapacity");
                    if (nbt.hasKey("TeslaInput")) this.inputRate = nbt.getLong("TeslaInput");
                    if (nbt.hasKey("TeslaOutput")) this.outputRate = nbt.getLong("TeslaOutput");
                    if (this.stored > this.capacity) this.stored = this.capacity;
                }

                @SuppressWarnings("unchecked") // There isnt anything sane we
                                               // can do about this.
                @Override
                public <T> T getCapability(Capability<T> capability, EnumFacing facing)
                {
                    if (TESLA_HOLDER != null && capability == TESLA_HOLDER) return (T) this;
                    if (TESLA_PRODUCER != null && capability == TESLA_PRODUCER && facing == null) return (T) this;
                    if (TESLA_CONSUMER != null && capability == TESLA_CONSUMER) return (T) this;
                    return null;
                }

                @Override
                public boolean hasCapability(Capability<?> capability, EnumFacing facing)
                {
                    return TESLA_HOLDER != null && capability == TESLA_HOLDER
                            || TESLA_PRODUCER != null && capability == TESLA_PRODUCER && facing == null
                            || TESLA_CONSUMER != null && capability == TESLA_CONSUMER;
                }

                @Override
                public NBTTagCompound serializeNBT()
                {
                    final NBTTagCompound dataTag = new NBTTagCompound();
                    dataTag.setLong("TeslaPower", this.stored);
                    dataTag.setLong("TeslaCapacity", this.capacity);
                    dataTag.setLong("TeslaInput", this.inputRate);
                    dataTag.setLong("TeslaOutput", this.outputRate);
                    return dataTag;
                }

                @Override
                public long getStoredPower()
                {

                    return this.stored;
                }

                @Override
                public long getCapacity()
                {
                    return capacity;
                }

                @Override
                public long takePower(long power, boolean simulated)
                {
                    final long removedPower = Math.min(this.stored, Math.min(this.outputRate, power));
                    if (!simulated) this.stored -= removedPower;
                    return removedPower;
                }

                @Override
                public long givePower(long power, boolean simulated)
                {
                    final long acceptedTesla = Math.min(this.capacity - this.stored, Math.min(this.inputRate, power));
                    if (!simulated) this.stored += acceptedTesla;
                    return acceptedTesla;
                }
            }
            event.addCapability(new ResourceLocation("thuttech:tesla"), new Provider());
        }
    }

    @SubscribeEvent
    public void onEntityCapabilityAttach(AttachCapabilitiesEvent.Entity event)
    {
        if (event.getEntity() instanceof EntityLift)
        {
            class Provider implements ICapabilitySerializable<NBTTagCompound>, ITeslaConsumer, ITeslaHolder
            {
                /** The amount of stored Tesla power. */
                private long stored;

                /** The maximum amount of Tesla power that can be stored. */
                private long capacity;

                /** The maximum amount of Tesla power that can be accepted. */
                private long inputRate;

                public Provider()
                {
                    this.stored = 0;
                    this.capacity = 5000000;
                    this.inputRate = 5000;
                }

                @Override
                public void deserializeNBT(NBTTagCompound nbt)
                {
                    this.stored = nbt.getLong("TeslaPower");
                    if (nbt.hasKey("TeslaCapacity")) this.capacity = nbt.getLong("TeslaCapacity");
                    if (nbt.hasKey("TeslaInput")) this.inputRate = nbt.getLong("TeslaInput");
                    if (this.stored > this.capacity) this.stored = this.capacity;
                }

                @SuppressWarnings("unchecked") // There isnt anything sane we
                                               // can do about this.
                @Override
                public <T> T getCapability(Capability<T> capability, EnumFacing facing)
                {
                    if (TESLA_HOLDER != null && capability == TESLA_HOLDER) return (T) this;
                    if (TESLA_CONSUMER != null && capability == TESLA_CONSUMER) return (T) this;
                    return null;
                }

                @Override
                public boolean hasCapability(Capability<?> capability, EnumFacing facing)
                {
                    return TESLA_HOLDER != null && capability == TESLA_HOLDER
                            || TESLA_CONSUMER != null && capability == TESLA_CONSUMER;
                }

                @Override
                public NBTTagCompound serializeNBT()
                {
                    final NBTTagCompound dataTag = new NBTTagCompound();
                    dataTag.setLong("TeslaPower", this.stored);
                    dataTag.setLong("TeslaCapacity", this.capacity);
                    dataTag.setLong("TeslaInput", this.inputRate);
                    return dataTag;
                }

                @Override
                public long getStoredPower()
                {

                    return this.stored;
                }

                @Override
                public long getCapacity()
                {
                    return capacity;
                }

                @Override
                public long givePower(long power, boolean simulated)
                {
                    final long acceptedTesla = Math.min(this.capacity - this.stored, Math.min(this.inputRate, power));
                    if (!simulated) this.stored += acceptedTesla;
                    return acceptedTesla;
                }
            }
            event.addCapability(new ResourceLocation("thuttech:tesla"), new Provider());
        }
    }

    @SubscribeEvent
    public void liftPowerUseUpdate(EventLiftConsumePower evt)
    {
        if (!EntityLift.ENERGYUSE || evt.lift == null) return;
        ITeslaHolder lift = evt.lift.getCapability(TESLA_HOLDER, null);
        if (lift instanceof ITeslaConsumer)
        {
            ITeslaConsumer liftCap = (ITeslaConsumer) lift;
            liftCap.givePower(-evt.toConsume, false);
        }
    }

    @SubscribeEvent
    public void livingUpdate(EventLiftUpdate evt)
    {
        if (!EntityLift.ENERGYUSE || evt.getTile().lift == null) return;
        ITeslaHolder tile = evt.getTile().getCapability(TESLA_HOLDER, null);
        ITeslaHolder lift = evt.getTile().lift.getCapability(TESLA_HOLDER, null);
        if (tile instanceof ITeslaProducer && tile instanceof ITeslaHolder && lift instanceof ITeslaConsumer
                && lift instanceof ITeslaHolder)
        {
            ITeslaConsumer tileCap = (ITeslaConsumer) tile;
            ITeslaConsumer liftCap = (ITeslaConsumer) lift;
            tileCap.givePower(ConfigHandler.controllerProduction, false);
            long tilePower = ((ITeslaHolder) tile).getStoredPower();
            long toAdd = liftCap.givePower(tilePower, true);
            toAdd = ((ITeslaProducer) tile).takePower(toAdd, false);
            liftCap.givePower(toAdd, false);
            evt.getTile().lift.setEnergy((int) ((ITeslaHolder) lift).getStoredPower());
        }
    }
}
