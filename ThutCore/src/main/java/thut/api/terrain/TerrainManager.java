package thut.api.terrain;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import thut.api.maths.Vector3;
import thut.api.network.PacketHandler;
import thut.api.terrain.CapabilityTerrain.DefaultProvider;

public class TerrainManager
{

    public static final String           TERRAIN    = "pokecubeTerrainData";
    public static final ResourceLocation TERRAINCAP = new ResourceLocation("thutcore", "terrain");

    private static TerrainManager        terrain;

    public static void clear()
    {
    }

    public static TerrainManager getInstance()
    {
        if (terrain == null)
        {
            terrain = new TerrainManager();
        }
        return terrain;
    }

    private TerrainManager()
    {
        MinecraftForge.EVENT_BUS.register(this);
        CapabilityManager.INSTANCE.register(CapabilityTerrain.ITerrainProvider.class, new CapabilityTerrain.Storage(),
                CapabilityTerrain.DefaultProvider.class);
    }

    @SubscribeEvent
    public void onCapabilityAttach(AttachCapabilitiesEvent<Chunk> event)
    {
        if (event.getCapabilities().containsKey(TERRAINCAP)) return;
        Chunk chunk = event.getObject();
        DefaultProvider terrain = new DefaultProvider(chunk);
        event.addCapability(TERRAINCAP, terrain);
    }

    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public void ChunkLoadEvent(ChunkDataEvent.Load evt)
    {
        try
        {
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
            new WorldTerrain(evt.getWorld().provider.getDimension()).loadTerrain(terrainData);
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
        PacketHandler.sendTerrainToClient(evt.getPlayer().getEntityWorld(), evt.getChunk(), evt.getPlayer());
    }

    public TerrainSegment getTerrain(World world, BlockPos p)
    {
        return world.getChunkFromBlockCoords(p).getCapability(CapabilityTerrain.TERRAIN_CAP, null)
                .getTerrainSegement(p);
    }

    public TerrainSegment getTerrain(World world, double x, double y, double z)
    {
        BlockPos pos = new BlockPos(x, y, z);
        TerrainSegment ret = getTerrain(world, pos);
        if (!world.isRemote) ret.initBiomes(world);
        return ret;
    }

    public TerrainSegment getTerrainForEntity(Entity e)
    {
        if (e == null) return null;
        return getTerrain(e.getEntityWorld(), e.posX, e.posY, e.posZ);
    }

    public TerrainSegment getTerrian(World world, Vector3 v)
    {
        return getTerrain(world, v.x, v.y, v.z);
    }
}
