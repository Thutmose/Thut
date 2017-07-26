package thut.api.terrain;

import java.io.IOException;
import java.util.HashMap;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;
import thut.api.network.PacketHandler;
import thut.api.network.PacketHandler.MessageClient;

public class TerrainManager
{

    public static final String    TERRAIN = "pokecubeTerrainData";

    private static TerrainManager terrain;

    public static void clear()
    {
        terrain.map.clear();
    }

    public static TerrainManager getInstance()
    {
        if (terrain == null)
        {
            terrain = new TerrainManager();
        }
        return terrain;
    }

    public HashMap<Integer, WorldTerrain> map = new HashMap<Integer, WorldTerrain>();

    private TerrainManager()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void ChunkLoadEvent(ChunkDataEvent.Load evt)
    {
        PokecubeMod.log(evt.getChunk().xPosition + " " + evt.getChunk().zPosition);
        try
        {
            WorldTerrain terrain = TerrainManager.getInstance().getTerrain(evt.getWorld());
            NBTTagCompound nbt = evt.getData();
            if (!nbt.hasKey(TERRAIN))
            {
                // Init the segement if it didn't exist.
                return;
            }
            NBTTagCompound terrainData = nbt.getCompoundTag(TERRAIN);
            int x = terrainData.getInteger("xCoord");
            int z = terrainData.getInteger("zCoord");
            if (evt.getChunk().x != x || evt.getChunk().z != z)
            {

                System.err.println("loaded: " + x + " " + z + " instead of " + evt.getChunk().x + " " + evt.getChunk().z
                        + " " + terrainData);
                return;
            }
            terrain.loadTerrain(terrainData);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void ChunkSaveEvent(ChunkDataEvent.Save evt)
    {
        WorldTerrain terrain = TerrainManager.getInstance().getTerrain(evt.getWorld());
        if (terrain.world == null || terrain.world.isRemote) return;
        try
        {
            NBTTagCompound nbt = evt.getData();
            NBTTagCompound terrainData = new NBTTagCompound();
            terrain.saveTerrain(terrainData, evt.getChunk().x, evt.getChunk().z);
            nbt.setTag(TERRAIN, terrainData);
            if (!evt.getChunk().isLoaded())
            {
                TerrainManager.getInstance().getTerrain(evt.getWorld()).removeTerrain(evt.getChunk().x,
                        evt.getChunk().z);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void ChunkWatchEvent(ChunkWatchEvent.Watch evt)
    {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) return;
        NBTTagCompound terrainData = new NBTTagCompound();
        TerrainManager.getInstance().getTerrain(evt.getPlayer().world).saveTerrain(terrainData, evt.getChunk().x,
                evt.getChunk().z);
        MessageClient message = new MessageClient(MessageClient.TERRAINSYNC, terrainData);
        PacketHandler.packetPipeline.sendTo(message, evt.getPlayer());
    }

    public WorldTerrain getTerrain(int id)
    {
        if (map.get(id) == null)
        {
            map.put(id, new WorldTerrain(id));
        }
        return map.get(id);
    }

    public WorldTerrain getTerrain(World world)
    {
        int id = world.provider.getDimension();
        return getTerrain(id);
    }

    public TerrainSegment getTerrain(World world, BlockPos p)
    {
        return getTerrain(world, p.getX(), p.getY(), p.getZ());
    }

    public TerrainSegment getTerrain(World world, double x, double y, double z)
    {
        int i = MathHelper.floor(x / 16.0D);
        int j = MathHelper.floor(y / 16.0D);
        int k = MathHelper.floor(z / 16.0D);

        TerrainSegment ret = getTerrain(world).getTerrain(i, j, k);
        if (!world.isRemote) ret.initBiomes(world);
        return ret;
    }

    public TerrainSegment getTerrainForEntity(Entity e)
    {
        if (e == null) return null;

        return getTerrain(e.world, e.posX, e.posY, e.posZ);
    }

    public TerrainSegment getTerrian(World world, Vector3 v)
    {
        return getTerrain(world, v.x, v.y, v.z);
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
        WorldTerrain terrain = TerrainManager.getInstance().getTerrain(evt.getWorld());
        if (terrain.world == null) terrain.world = evt.getWorld();
    }

    @SubscribeEvent
    public void WorldSaveEvent(Save evt) throws IOException
    {

    }

}
