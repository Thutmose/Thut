package thut.api.terrain;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;

public interface ITerrainEffect
{
	/**
	 * Called when the terrain effect is assigned to the terrain segment
	 * @param x chunkX of terrainsegment
	 * @param y chunkY of terrainsegment
	 * @param z chunkZ of terrainsegement
	 */
	void bindToTerrain(int x, int y, int z);
	
	void doEffect(EntityLivingBase entity, boolean firstEntry);
	
	//Does not currently work TODO make this work
	void readFromNBT(NBTTagCompound nbt);
	//Does not currently work TODO make this work
	void writeToNBT(NBTTagCompound nbt);
}
