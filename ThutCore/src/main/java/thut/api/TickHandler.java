package thut.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Unload;
import thut.api.maths.Vector3;
import thut.api.network.PacketHandler;

public class TickHandler
{

	private static TickHandler instance;

	public TickHandler()
	{
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
		new PacketHandler();
		instance = this;
	}

	public static TickHandler getInstance()
	{

		if (instance == null) new TickHandler();

		return instance;
	}

	public HashMap<Integer, Vector<BlockChange>>	blocks		= new HashMap();
	public static int								maxChanges	= 200;
	public HashMap<Integer, WorldCache> worldCaches = new HashMap<Integer, WorldCache>();

	public WorldCache getWorldCache(int dimension)
	{
		return worldCaches.get(dimension);
	}
	
	@SubscribeEvent
	public void worldTickEvent(WorldTickEvent evt)
	{

		if (evt.phase == Phase.START && !evt.world.isRemote)
		{
			try
			{
				//TODO see if this stuff is needed
				TreeSet ticktreeset = ReflectionHelper.getPrivateValue(WorldServer.class, (WorldServer) evt.world, 5);
				Set tickset = ReflectionHelper.getPrivateValue(WorldServer.class, (WorldServer) evt.world, 4);
				List tickentrylist = ReflectionHelper.getPrivateValue(WorldServer.class, (WorldServer) evt.world, 17);
				int i = ((Set) ticktreeset).size();
				int j = ((Set)tickset).size();
				
				if(i!=j)
				{
                    Vector3 temp = Vector3.getNewVectorFromPool();
                    Vector3 temp1 = Vector3.getNewVectorFromPool();
				    System.out.println(ticktreeset.size()+" "+tickset.size());
                    for(Object o: tickset)
                    {
                        NextTickListEntry next = (NextTickListEntry) o;
                        temp.set(next.position);
                        boolean has = false;
                        for(Object o1: ticktreeset)
                        {
                            NextTickListEntry next1 = (NextTickListEntry) o1;
                            temp1.set(next1.position);
                            if(temp1.sameBlock(temp))
                            {
                                has = true;
                                break;
                            }
                        }
                        if(!has)
                        {
                            Block b = temp.getBlock(evt.world);
                            System.out.println(b+" "+temp+" "+" "+next.getBlock());
                        }
                    }
                    for(Object o: ticktreeset)
                    {
                        NextTickListEntry next = (NextTickListEntry) o;
                        temp.set(next.position);
                        boolean has = false;
                        for(Object o1: tickset)
                        {
                            NextTickListEntry next1 = (NextTickListEntry) o1;
                            temp1.set(next1.position);
                            if(temp1.sameBlock(temp))
                            {
                                has = true;
                                break;
                            }
                        }
                        if(!has)
                        {
                            Block b = temp.getBlock(evt.world);
                            System.out.println(b+" "+temp+" ");
                        }
                    }
				    temp.freeVectorFromPool();
					new Exception().printStackTrace();
					((Set)ticktreeset).clear();;
					((Set)tickset).clear();
					tickentrylist.clear();
					i = 0;
				}
				boolean remove = false;
				if (i > 2000 && remove)
				{
					TreeSet t = (TreeSet) ticktreeset;
					Set h = (Set) tickset;
					List a = (List) tickentrylist;
					int n = 0;

					int[] blocks = new int[4096];
					Vector3 temp = Vector3.getNewVectorFromPool();

					for (j = 0; j < i; ++j)
					{
						NextTickListEntry next = (NextTickListEntry) t.first();

						temp.set(next.position);
						
						Block b = temp.getBlock(evt.world);

						blocks[Block.getIdFromBlock(b)]++;
						if(b.getMaterial() == Material.circuits ||
								(b.getTickRandomly() 
								&& (!
								(b.getClass().getName().contains("pokecube")
								||b.getClass().getName().contains("thut")
								||b==Blocks.grass
								||b==Blocks.tallgrass
								||b.getMaterial() == Material.water
								||b.getMaterial() == Material.leaves))))
							continue;
						if(b!=Blocks.grass)
						n++;
						t.remove(next);
						h.remove(next);
						a.add(next);

					}
					int most = 0;
					int highId = 0;
					for (int m = 0; m < 4096; m++)
					{
						if (blocks[m] > most)
						{
							highId = m;
							most = blocks[m];
						}
						if(blocks[m]>0)
						{
//							System.out.println(blocks[m]+" "+Block.getBlockById(m).getTickRandomly()+" "+Block.getBlockById(m));
						}
					}
					//evt.world.scheduledUpdatesAreImmediate = false;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		if (evt.phase != Phase.START || !blocks.containsKey(evt.world.provider.getDimensionId())
				|| blocks.get(evt.world.provider.getDimensionId()).size() == 0 || evt.world.isRemote)
			return;

		int num = 0;
		ArrayList<BlockChange> removed = new ArrayList<BlockChange>();
		Vector<BlockChange> blocks = this.blocks.get(evt.world.provider.getDimensionId());
		ArrayList<BlockChange> toRemove = new ArrayList(blocks);

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
		if (evt.world.provider.getDimensionId() == 0)
		{
			blocks.clear();
		}
		worldCaches.remove(evt.world.provider.getDimensionId());
	}

	@SubscribeEvent
	public void WorldLoadEvent(Load evt)
	{
		if(evt.world.isRemote)
			return;
		worldCaches.put(evt.world.provider.getDimensionId(), new WorldCache(evt.world));
	}

	@SubscribeEvent
	public void ChunkLoadEvent(net.minecraftforge.event.world.ChunkEvent.Load evt)
	{
		if(evt.world.isRemote)
			return;
		WorldCache world = worldCaches.get(evt.world.provider.getDimensionId());
		if(world==null)
		{
			world = new WorldCache(evt.world);
			worldCaches.put(evt.world.provider.getDimensionId(), world);
		}
		world.addChunk(evt.getChunk());
	}

	@SubscribeEvent
	public void ChunkUnLoadEvent(net.minecraftforge.event.world.ChunkEvent.Unload evt)
	{
		if(evt.world.isRemote)
			return;
		WorldCache world = worldCaches.get(evt.world.provider.getDimensionId());
		if(world!=null)
		{
			world.removeChunk(evt.getChunk());
		}
	}

	public static void addBlockChange(Vector3 location, int dimension, Block blockTo)
	{
		addBlockChange(location, dimension, blockTo, 0);
	}

	public static void addBlockChange(Vector3 location, int dimension, Block blockTo, int meta)
	{
		addBlockChange(new BlockChange(location, dimension, blockTo, meta), dimension);
	}

	public static void addBlockChange(BlockChange b1, int dimension)
	{

		if (b1.location.y > 255) return;

		getInstance();
		ArrayList<BlockChange> blocks = new ArrayList(TickHandler.getListForDimension(dimension));
		for (BlockChange b : blocks)
		{
			if (b.equals(b1)) return;
		}
		getInstance();
		TickHandler.getListForDimension(dimension).add(b1);
	}

	public static Vector<BlockChange> getListForWorld(World worldObj)
	{
		Vector<BlockChange> ret = getInstance().blocks.get(worldObj.provider.getDimensionId());
		if (ret == null)
		{
			ret = new Vector<BlockChange>();
			getInstance().blocks.put(worldObj.provider.getDimensionId(), ret);
		}
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

	public static class BlockChange
	{
		public int		dimension;
		public Vector3	location;
		public Block	blockTo;
		public Block	blockFrom;
		public int		metaTo	= 0;
		public int		flag	= 3;

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

		public boolean changeBlock(World worldObj)
		{
			boolean ret = location.setBlock(worldObj, blockTo, metaTo, flag);
			return ret;
		}

		@Override
		public String toString()
		{
			return blockTo + " " + dimension + " " + location;
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

	}
}
