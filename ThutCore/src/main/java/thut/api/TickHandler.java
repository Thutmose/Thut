package thut.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thut.api.block.IOwnableTE;
import thut.api.maths.Vector3;
import thut.api.network.PacketHandler;

public class TickHandler
{

    public static class BlockChange
    {
        public int     dimension;
        public Vector3 location;
        public Block   blockTo;
        public Block   blockFrom;
        public int     metaTo = 0;
        public int     flag   = 3;

        public BlockChange(Vector3 location, int dim, Block blockTo)
        {
            dimension = dim;
            this.location = location.copy();
            this.blockTo = blockTo;
        }

        public BlockChange(Vector3 location, int dim, Block blockTo, int meta)
        {
            dimension = dim;
            this.location = location.copy();
            this.blockTo = blockTo;
            this.metaTo = meta;
        }

        public boolean changeBlock(World world)
        {
            boolean ret = location.setBlock(world, blockTo, metaTo, flag);
            return ret;
        }

        @Override
        public boolean equals(Object o)
        {
            if (o instanceof BlockChange)
            {
                BlockChange b = (BlockChange) o;
                return dimension == b.dimension && location.sameBlock(b.location);
            }

            return false;
        }

        @Override
        public String toString()
        {
            return blockTo + " " + dimension + " " + location;
        }

        @Override
        public int hashCode()
        {
            // TODO Auto-generated method stub
            return super.hashCode();
        }

    }

    private static TickHandler                 instance;

    public static int                          maxChanges = 200;

    static Map<Thread, ArrayList<BlockChange>> lists      = Maps.newConcurrentMap();

    public static void addBlockChange(BlockChange b1, int dimension)
    {

        if (b1.location.y > 255) return;

        getInstance();
        ArrayList<BlockChange> blocks = getList();
        blocks.addAll(TickHandler.getListForDimension(dimension));
        for (BlockChange b : blocks)
        {
            if (b.equals(b1)) return;
        }
        getInstance();
        TickHandler.getListForDimension(dimension).add(b1);
    }

    public static void addBlockChange(Vector3 location, int dimension, Block blockTo)
    {
        addBlockChange(location, dimension, blockTo, 0);
    }

    public static void addBlockChange(Vector3 location, int dimension, Block blockTo, int meta)
    {
        addBlockChange(new BlockChange(location, dimension, blockTo, meta), dimension);
    }

    public static void cleanup()
    {
        Thread thread = Thread.currentThread();
        lists.remove(thread);
        System.gc();
    }

    public static TickHandler getInstance()
    {
        if (instance == null) new TickHandler();
        return instance;
    }

    private static ArrayList<BlockChange> getList()
    {
        Thread thread = Thread.currentThread();
        if (lists.containsKey(thread))
        {
            ArrayList<BlockChange> ret = lists.get(thread);
            ret.clear();
            return ret;

        }
        ArrayList<BlockChange> ret;
        ret = Lists.newArrayList();
        lists.put(thread, ret);
        return ret;
    }

    public static Vector<BlockChange> getListForDimension(int dim)
    {
        Vector<BlockChange> ret = getInstance().blocks.get(dim);
        if (ret == null)
        {
            ret = new Vector<BlockChange>();
            getInstance().blocks.put(dim, ret);
        }
        return ret;
    }

    public static Vector<BlockChange> getListForWorld(World world)
    {
        Vector<BlockChange> ret = getInstance().blocks.get(world.provider.getDimension());
        if (ret == null)
        {
            ret = new Vector<BlockChange>();
            getInstance().blocks.put(world.provider.getDimension(), ret);
        }
        return ret;
    }

    public HashMap<Integer, Vector<BlockChange>> blocks      = new HashMap<Integer, Vector<BlockChange>>();

    /** This is a map of dimension to worldcache, it can be used for thread-safe
     * world access */
    public HashMap<Integer, WorldCache>          worldCaches = new HashMap<Integer, WorldCache>();

    HashMap<Integer, HashSet<Long>>              toRefresh   = new HashMap<>();

    public TickHandler()
    {
        MinecraftForge.EVENT_BUS.register(this);
        new PacketHandler();
        instance = this;
    }

    @SubscribeEvent
    public void ChunkLoadEvent(net.minecraftforge.event.world.ChunkEvent.Load evt)
    {
        if (evt.getWorld().isRemote) return;
        // Add the chunk to the corresponding world cache.
        WorldCache world = getWorldCache(evt.getWorld().provider.getDimension());
        if (world == null)
        {
            world = new WorldCache(evt.getWorld());
            synchronized (worldCaches)
            {
                worldCaches.put(evt.getWorld().provider.getDimension(), world);
            }
        }
        world.addChunk(evt.getChunk());
    }

    @SubscribeEvent
    public void ChunkUnLoadEvent(net.minecraftforge.event.world.ChunkEvent.Unload evt)
    {
        if (evt.getWorld().isRemote) return;
        // Remove the chunk from the cache
        WorldCache world = getWorldCache(evt.getWorld().provider.getDimension());
        if (world != null)
        {
            world.removeChunk(evt.getChunk());
        }
    }

    public static Map<UUID, Integer> playerTickTracker = Maps.newHashMap();

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void PlayerTick(PlayerTickEvent event)
    {
        if (event.phase == Phase.END && playerTickTracker.containsKey(event.player.getUniqueID()))
        {
            Integer time = playerTickTracker.remove(event.player.getUniqueID());
            if (time < event.player.ticksExisted - 10)
            {
                Minecraft.getMinecraft().gameSettings.viewBobbing = true;
            }
        }
    }

    public WorldCache getWorldCache(int dimension)
    {
        synchronized (worldCaches)
        {
            return worldCaches.get(dimension);
        }
    }

    @SubscribeEvent
    public void placeEvent(PlaceEvent event)
    {
        TileEntity te = event.getWorld().getTileEntity(event.getPos());
        if (te != null && te instanceof IOwnableTE)
        {
            IOwnableTE ownable = (IOwnableTE) te;
            ownable.setPlacer(event.getPlayer());
        }
    }

    @SubscribeEvent
    public void WorldLoadEvent(Load evt)
    {
        if (evt.getWorld().isRemote) return;
        synchronized (worldCaches)
        {
            // Initialize a world cache for this dimension
            worldCaches.put(evt.getWorld().provider.getDimension(), new WorldCache(evt.getWorld()));
        }
    }

    @SubscribeEvent
    public void worldTickEvent(WorldTickEvent evt)
    {
        if (evt.phase != Phase.END || !blocks.containsKey(evt.world.provider.getDimension())
                || blocks.get(evt.world.provider.getDimension()).size() == 0 || evt.world.isRemote)
            return;

        int num = 0;
        ArrayList<BlockChange> removed = Lists.newArrayList();
        Vector<BlockChange> blocks = this.blocks.get(evt.world.provider.getDimension());
        ArrayList<BlockChange> toRemove = Lists.newArrayList(blocks);

        // remove the blocks needed for that world.
        for (int i = 0; i < toRemove.size(); i++)
        {
            BlockChange b = toRemove.get(i);
            b.changeBlock(evt.world);
            removed.add(b);
            num++;
            if (num >= maxChanges * 5) break;
        }
        for (BlockChange b : removed)
            blocks.remove(b);
        removed.clear();
    }

    @SubscribeEvent
    public void WorldUnloadEvent(Unload evt)
    {
        if (evt.getWorld().provider.getDimension() == 0)
        {
            blocks.clear();
        }
        synchronized (worldCaches)
        {
            // Remove world cache for dimension
            worldCaches.remove(evt.getWorld().provider.getDimension());
        }
    }
}
