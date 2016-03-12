package thut.core.common.blocks.tileentity;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityBlockFluid extends TileEntity {

    public static int getColour(int dyeIndex)
    {
        int ret = ItemDye.dyeColors[dyeIndex];
        int alpha = 0xFF000000;
        ret += alpha;
        return ret;
    }
    
	public int DEFAULTCOLOUR = EnumDyeColor.SILVER.getMapColor().colorValue + 0xFF000000;
	
	public int[] colourArray = { DEFAULTCOLOUR, DEFAULTCOLOUR, DEFAULTCOLOUR, DEFAULTCOLOUR, DEFAULTCOLOUR, DEFAULTCOLOUR };
	public float[][] corner;

    public float flowDir;

    /**
     * Overriden in a sign to provide the text.
     */
	@SuppressWarnings("rawtypes")
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
	public void readFromNBT(NBTTagCompound par1) {
		super.readFromNBT(par1);
		colourArray = par1.getIntArray("metaArray");
		flowDir = par1.getFloat("flow");
		if(par1.hasKey("corners"))
		{
		    corner = new float[2][2];
		    NBTTagCompound cornertag = par1.getCompoundTag("corners");
            corner[0][0] = cornertag.getFloat("00");
            corner[0][1] = cornertag.getFloat("01");
            corner[1][1] = cornertag.getFloat("11");
            corner[1][0] = cornertag.getFloat("10");
		}
		
		if(colourArray.length==0)
		{
			colourArray = new int[]{ DEFAULTCOLOUR, DEFAULTCOLOUR, DEFAULTCOLOUR, DEFAULTCOLOUR, DEFAULTCOLOUR, DEFAULTCOLOUR };
		}
	}
    
    @Override
	public void writeToNBT(NBTTagCompound par1) {
		super.writeToNBT(par1);
		par1.setIntArray("metaArray", colourArray);
		par1.setFloat("flow", flowDir);
		if(corner!=null)
		{
		    NBTTagCompound cornertag = new NBTTagCompound();
            cornertag.setFloat("00", corner[0][0]);
            cornertag.setFloat("01", corner[0][1]);
            cornertag.setFloat("11", corner[1][1]);
            cornertag.setFloat("10", corner[1][0]);
            par1.setTag("corners", cornertag);
		}
	}
}
