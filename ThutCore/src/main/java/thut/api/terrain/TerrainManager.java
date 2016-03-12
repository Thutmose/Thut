package thut.api.terrain;

import java.util.HashMap;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.relauncher.Side;
import thut.api.maths.Vector3;

public class TerrainManager {

	public static final String TERRAIN = "pokecubeTerrainData";
	
	private static TerrainManager terrain;
	
    public static void clear()
	{
		terrain = null;
	}
	
	public static TerrainManager getInstance()
	{
		if(terrain == null)
		{
			terrain = new TerrainManager();
		}
		return terrain;
	}

	public HashMap<Integer, WorldTerrain> map = new HashMap<Integer, WorldTerrain>();
	
	public TerrainManager()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void ChunkLoadEvent(ChunkDataEvent.Load evt)
	{
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)
			return;
		try
		{
			NBTTagCompound nbt = evt.getData();
			NBTTagCompound terrainData = nbt.getCompoundTag(TERRAIN);

			TerrainManager.getInstance().getTerrain(evt.world).loadTerrain(terrainData);
			// evt.getChunk().setChunkModified();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@SubscribeEvent
	public void ChunkSaveEvent(ChunkDataEvent.Save evt)
	{
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)
			return;
		try
		{
			NBTTagCompound nbt = evt.getData();
			NBTTagCompound terrainData = new NBTTagCompound();
			TerrainManager.getInstance().getTerrain(evt.world).saveTerrain(terrainData, evt.getChunk().xPosition,
					evt.getChunk().zPosition);
			nbt.setTag(TERRAIN, terrainData);
			if (!evt.getChunk().isLoaded())
			{
				TerrainManager.getInstance().getTerrain(evt.world).removeTerrain(evt.getChunk().xPosition,
						evt.getChunk().zPosition);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public WorldTerrain getTerrain(int id)
	{
		if(map.get(id)==null)
		{
			map.put(id, new WorldTerrain(id));
		}
		
		return map.get(id);
	}
	
	public WorldTerrain getTerrain(World worldObj)
	{
		int id = worldObj.provider.getDimensionId();
		return getTerrain(id);
	}
	
	public TerrainSegment getTerrain(World world, BlockPos p) {
		return getTerrain(world, p.getX(), p.getY(), p.getZ());
	}
	
	
	public TerrainSegment getTerrain(World worldObj, double x, double y, double z)
	{
        int i = MathHelper.floor_double(x / 16.0D);
        int j = MathHelper.floor_double(y / 16.0D);
        int k = MathHelper.floor_double(z / 16.0D);

        TerrainSegment ret = getTerrain(worldObj).getTerrain(i, j, k);
        ret.initBiomes(worldObj);
		return ret;
	}
	
	public TerrainSegment getTerrainForEntity(Entity e)
	{
		if(e==null)
			return null;
		
		return getTerrain(e.worldObj, e.posX, e.posY, e.posZ);
	}
	
	public TerrainSegment getTerrian(World worldObj, Vector3 v)
	{
		return getTerrain(worldObj, v.x, v.y, v.z);
	}
	
	@SubscribeEvent
	public void PlayerLoggout(PlayerLoggedOutEvent evt)
	{
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			TerrainManager.clear();
		}
	}
	
	@SubscribeEvent
	public void WorldLoadEvent(Load evt)
	{
		TerrainManager.getInstance().getTerrain(evt.world);
	}

	@SubscribeEvent
	public void WorldUnloadEvent(Unload evt)
	{
		if (evt.world.provider.getDimensionId() == 0 && FMLCommonHandler.instance().getEffectiveSide() != Side.CLIENT)
		{
			TerrainManager.clear();
		}
	}

}
