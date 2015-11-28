package thut.core.common.blocks.tileentity;

import static thut.api.ThutBlocks.*;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityMultiBlockPartFluids extends TileEntityMultiBlockPart implements IFluidHandler
{
    TileEntityMultiCoreFluids fluidCore;

    public boolean canUpdate()
    {
        return false;
    }

    public void setCore(TileEntityMultiCoreFluids core)
    {
        fluidCore = core;
        this.core = core;
    }

    public TileEntityMultiCoreFluids getCore()
    {
        if (fluidCore == null) fluidCore = (TileEntityMultiCoreFluids) worldObj.getTileEntity(corePos);

        return fluidCore;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound)
    {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound)
    {
        super.writeToNBT(tagCompound);
    }

    @Override
    public int getSizeInventory()
    {
        return fluidCore != null ? fluidCore.getSizeInventory() : 0;
    }

    @Override
    public int fill(EnumFacing from, FluidStack resource, boolean doFill)
    {
        return fluidCore != null ? fluidCore.fill(from, resource, doFill) : 0;
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
    public boolean canFill(EnumFacing from, Fluid fluid)
    {
        return fluidCore != null ? fluidCore.canFill(from, fluid) : false;
    }

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid)
    {
        return fluidCore != null ? fluidCore.canDrain(from, fluid) : false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from)
    {
        return fluidCore != null ? fluidCore.getTankInfo(from) : null;
    }
}
