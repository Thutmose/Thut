package thut.tech.common.tesla;

import net.darkhax.tesla.api.BaseTeslaContainer;
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
            class Provider extends BaseTeslaContainer implements ICapabilitySerializable<NBTTagCompound>
            {
                public Provider()
                {
                    super(5000000, 5000, 5000);
                }

                @Override
                public void deserializeNBT(NBTTagCompound nbt)
                {
                    super.deserializeNBT(nbt);
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
                    return (NBTTagCompound) super.serializeNBT();
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
            class Provider extends BaseTeslaContainer implements ICapabilitySerializable<NBTTagCompound>
            {
                public Provider()
                {
                    super(50000, 5000, 0);
                }

                @Override
                public void deserializeNBT(NBTTagCompound nbt)
                {
                    super.deserializeNBT(nbt);
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
                    return (NBTTagCompound) super.serializeNBT();
                }
            }
            event.addCapability(new ResourceLocation("thuttech:tesla"), new Provider());
        }
    }

    @SubscribeEvent
    public void livingUpdate(EventLiftUpdate evt)
    {
        if (!EntityLift.ENERGYUSE || evt.getTile().lift == null) return;
        ITeslaHolder tile = evt.getTile().getCapability(TESLA_HOLDER, null);
        ITeslaHolder lift = evt.getTile().lift.getCapability(TESLA_HOLDER, null);
        if (tile instanceof BaseTeslaContainer && lift instanceof BaseTeslaContainer)
        {
            BaseTeslaContainer tileCap = (BaseTeslaContainer) tile;
            BaseTeslaContainer liftCap = (BaseTeslaContainer) lift;
            tileCap.givePower(ConfigHandler.controllerProduction, false);
            long tilePower = tileCap.getStoredPower();
            long toAdd = liftCap.givePower(tilePower, true);
            liftCap.givePower(toAdd, false);
            tileCap.takePower(toAdd, false);
            evt.getTile().lift.setEnergy((int) liftCap.getStoredPower());
        }
    }
}
