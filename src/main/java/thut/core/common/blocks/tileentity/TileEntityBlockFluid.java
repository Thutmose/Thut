package thut.core.common.blocks.tileentity;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class TileEntityBlockFluid extends TileEntity {

	public int[] metaArray = { 8, 8, 8, 8, 8, 8 };

	public boolean canUpdate() {
		return false;
	}

	public void writeToNBT(NBTTagCompound par1) {
		super.writeToNBT(par1);
		par1.setIntArray("metaArray", metaArray);
	}

	public void readFromNBT(NBTTagCompound par1) {
		super.readFromNBT(par1);
		metaArray = par1.getIntArray("metaArray");
	}
	
    /**
     * Overriden in a sign to provide the text.
     */
	@Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 3, nbttagcompound);
    }
    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
    	NBTTagCompound nbttagcompound = pkt.func_148857_g();
    	this.readFromNBT(nbttagcompound);
    }

	public void sendUpdate() {
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public void setIcon(int side, int meta)// , Icon icon
	{
		metaArray[side] = meta;
	}

	public int[] getMetaArray() {
		return metaArray;
	}

}
