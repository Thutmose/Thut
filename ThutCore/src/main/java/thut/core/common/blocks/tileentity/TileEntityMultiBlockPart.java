package thut.core.common.blocks.tileentity;

import static thut.api.ThutBlocks.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;

public class TileEntityMultiBlockPart extends TileEntity implements ISidedInventory
{
    TileEntityMultiCore core;
    BlockPos            corePos;

    public IBlockState revertID = brick_block.getDefaultState();
    public int         type     = 0;

    public void setCore(TileEntityMultiCore core)
    {
        corePos = core.getPos();
        this.core = core;
    }

    public TileEntityMultiCore getCore()
    {
        if (core == null && corePos!=null) core = (TileEntityMultiCore) worldObj.getTileEntity(corePos);

        return core;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.corePos = new BlockPos(compound.getInteger("cx"), compound.getInteger("cy"), compound.getInteger("cz"));
        int name = compound.getInteger("revert");
        int revertMeta = compound.getInteger("revertMeta");
        revertID = Block.getBlockById(name).getStateFromMeta(revertMeta);
        type = compound.getInteger("type");
        if (revertID == null) revertID = brick_block.getDefaultState();
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger("cx", this.corePos.getX());
        compound.setInteger("cy", this.corePos.getY());
        compound.setInteger("cz", this.corePos.getZ());
        compound.setInteger("revert", Block.getIdFromBlock(revertID.getBlock()));
        compound.setInteger("revertMeta", revertID.getBlock().getMetaFromState(revertID));
        compound.setInteger("type", type);
    }

    public void revert()
    {
        worldObj.setBlockState(corePos, revertID);
    }

    @Override
    public void invalidate()
    {
        if (core != null) core.invalidateMultiblock();
        super.invalidate();
    }

    @Override
    public int getSizeInventory()
    {
        return core != null ? core.getSizeInventory() : 0;
    }

    @Override
    public ItemStack getStackInSlot(int i)
    {
        return core != null ? core.getStackInSlot(i) : null;
    }

    @Override
    public ItemStack decrStackSize(int i, int j)
    {
        return core != null ? core.decrStackSize(i, j) : null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i)
    {
        return core != null ? core.getStackInSlotOnClosing(i) : null;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack)
    {
        if (core != null) core.setInventorySlotContents(i, itemstack);
    }

    @Override
    public int getInventoryStackLimit()
    {
        return core != null ? core.getInventoryStackLimit() : 0;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer)
    {
        return worldObj.getTileEntity(corePos) != this ? false : entityplayer.getDistanceSq(corePos) <= 64.0;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        return core.isItemValidForSlot(i, itemstack);
    }

    /** Overriden in a sign to provide the text. */
    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
        NBTTagCompound nbttagcompound = pkt.getNbtCompound();
        this.readFromNBT(nbttagcompound);
    }

    @Override
    public void openInventory(EntityPlayer player)
    {
        if (core != null) core.openInventory(player);
    }

    @Override
    public void closeInventory(EntityPlayer player)
    {
        if (core != null) core.closeInventory(player);
    }

    @Override
    public int getField(int id)
    {
        if (core != null) return core.getField(id);
        return 0;
    }

    @Override
    public void setField(int id, int value)
    {
        if (core != null) core.setField(id, value);
    }

    @Override
    public int getFieldCount()
    {
        if (core != null) return core.getFieldCount();
        return 0;
    }

    @Override
    public void clear()
    {
        if (core != null) core.clear();
    }

    @Override
    public String getCommandSenderName()
    {
        if (core != null) return core.getCommandSenderName();
        return null;
    }

    @Override
    public boolean hasCustomName()
    {
        if (core != null) return core.hasCustomName();
        return false;
    }

    @Override
    public IChatComponent getDisplayName()
    {
        if (core != null) return core.getDisplayName();
        return null;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side)
    {
        if (core != null) return core.getSlotsForFace(side);
        return null;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction)
    {
        if (core != null) return core.canInsertItem(index, itemStackIn, direction);
        return false;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction)
    {
        if (core != null) return core.canExtractItem(index, stack, direction);
        return false;
    }
}
