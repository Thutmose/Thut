package thut.core.common.blocks.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityMultiBlockPartFluids extends TileEntityMultiBlockPart implements IFluidHandler
{
    TileEntityMultiCoreFluids fluidCore;

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid)
    {
        return fluidCore != null ? fluidCore.canDrain(from, fluid) : false;
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid)
    {
        return fluidCore != null ? fluidCore.canFill(from, fluid) : false;
    }

    public boolean canUpdate()
    {
        return false;
    }

    @Override
    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain)
    {
        return fluidCore != null ? fluidCore.drain(from, resource, doDrain) : null;
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain)
    {
        return fluidCore != null ? fluidCore.drain(from, maxDrain, doDrain) : null;
    }

    @Override
    public int fill(EnumFacing from, FluidStack resource, boolean doFill)
    {
        return fluidCore != null ? fluidCore.fill(from, resource, doFill) : 0;
    }

    @Override
    public TileEntityMultiCoreFluids getCore()
    {
        if (fluidCore == null) fluidCore = (TileEntityMultiCoreFluids) worldObj.getTileEntity(corePos);

        return fluidCore;
    }

    @Override
    public int getSizeInventory()
    {
        return fluidCore != null ? fluidCore.getSizeInventory() : 0;
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from)
    {
        return fluidCore != null ? fluidCore.getTankInfo(from) : null;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);
    }

    public void setCore(TileEntityMultiCoreFluids core)
    {
        fluidCore = core;
        this.core = core;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
    }
}
