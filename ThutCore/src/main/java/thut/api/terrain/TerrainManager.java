package thut.api.terrain;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thut.api.maths.Vector3;
import thut.api.network.PacketHandler;
import thut.api.terrain.CapabilityTerrain.DefaultProvider;

public class TerrainManager
{

    public static final String           TERRAIN    = "pokecubeTerrainData";
    public static final ResourceLocation TERRAINCAP = new ResourceLocation("thutcore", "terrain");
    public ITerrainProvider              provider   = new ITerrainProvider()
                                                    {
                                                    };

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
                CapabilityTerrain.DefaultProvider::new);
    }

    @SubscribeEvent
    public void onCapabilityAttach(AttachCapabilitiesEvent<Chunk> event)
    {
        if (event.getCapabilities().containsKey(TERRAINCAP)) return;
        Chunk chunk = event.getObject();
        DefaultProvider terrain = new DefaultProvider(chunk);
        event.addCapability(TERRAINCAP, terrain);
    }

    @SubscribeEvent
    public void ChunkWatchEvent(ChunkWatchEvent.Watch evt)
    {
        if (evt.getPlayer().getEntityWorld().isRemote || evt.getChunkInstance() == null) return;
        PacketHandler.sendTerrainToClient(evt.getPlayer().getEntityWorld(), evt.getChunkInstance().getPos(),
                evt.getPlayer());
    }

    public TerrainSegment getTerrain(World world, BlockPos p)
    {
        return provider.getTerrain(world, p);
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
