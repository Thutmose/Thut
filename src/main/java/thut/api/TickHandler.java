package thut.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import net.minecraft.block.Block;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent.Unload;
import thut.api.maths.Vector3;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class TickHandler
{

	private static TickHandler instance;

	public TickHandler()
	{
		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
		instance = this;
	}

	public static TickHandler getInstance()
	{

		if (instance == null) new TickHandler();

		return instance;
	}

	public HashMap<Integer, Vector<BlockChange>>	blocks		= new HashMap();
	public static int								maxChanges	= 200;

	@SubscribeEvent
	public void worldTickEvent(WorldTickEvent evt)
	{

		if (evt.phase == Phase.START && !evt.world.isRemote)
		{
			try
			{
				Object o = ReflectionHelper.getPrivateValue(WorldServer.class, (WorldServer) evt.world, 5);
				Object o1 = ReflectionHelper.getPrivateValue(WorldServer.class, (WorldServer) evt.world, 4);
				Object o2 = ReflectionHelper.getPrivateValue(WorldServer.class, (WorldServer) evt.world, 15);
				int i = ((Set) o).size();
				int j = ((Set)o1).size();
				
				if(i!=j)
				{
					new Exception().printStackTrace();
					((Set)o).clear();
					((Set)o1).clear();
				}

				if (i > 1000)
				{
					TreeSet t = (TreeSet) o;
					Set h = (Set) o1;
					List a = (List) o2;
					int n = 0;

					int[] blocks = new int[4096];

					for (j = 0; j < i; ++j)
					{
						NextTickListEntry next = (NextTickListEntry) t.first();

						Block b = evt.world.getBlock(next.xCoord, next.yCoord, next.zCoord);
						blocks[Block.getIdFromBlock(b)]++;

						n++;
						t.remove(next);
						h.remove(next);
						a.add(next);
						if (n > i / 20) break;

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
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		if (evt.phase != Phase.END || !blocks.containsKey(evt.world.provider.dimensionId)
				|| blocks.get(evt.world.provider.dimensionId).size() == 0 || evt.world.isRemote)
			return;

		int num = 0;
		ArrayList<BlockChange> removed = new ArrayList<BlockChange>();
		Vector<BlockChange> blocks = this.blocks.get(evt.world.provider.dimensionId);
		ArrayList<BlockChange> toRemove = new ArrayList(blocks);

		for (int i = 0; i < toRemove.size(); i++)
		{
			BlockChange b = toRemove.get(i);
			b.changeBlock(evt.world);
			removed.add(b);
			num++;
			if (num >= maxChanges) break;
		}
		for (BlockChange b : removed)
			blocks.remove(b);

		removed.clear();
	}

	@SubscribeEvent
	public void WorldUnloadEvent(Unload evt)
	{
		if (evt.world.provider.dimensionId == 0)
		{
			blocks.clear();
		}
	}

	public static void addBlockChange(Vector3 location, World worldObj, Block blockTo)
	{
		addBlockChange(location, worldObj, blockTo, 0);
	}

	public static void addBlockChange(Vector3 location, World worldObj, Block blockTo, int meta)
	{
		addBlockChange(new BlockChange(location, worldObj, blockTo, meta), worldObj);
	}

	public static void addBlockChange(BlockChange b1, World worldObj)
	{

		if (b1.location.y > 255) return;

		ArrayList<BlockChange> blocks = new ArrayList(getInstance().getListForWorld(worldObj));
		for (BlockChange b : blocks)
		{
			if (b.equals(b1)) return;
		}
		getInstance().getListForWorld(worldObj).add(b1);
	}

	public static Vector<BlockChange> getListForWorld(World worldObj)
	{
		Vector<BlockChange> ret = getInstance().blocks.get(worldObj.provider.dimensionId);
		if (ret == null)
		{
			ret = new Vector<BlockChange>();
			getInstance().blocks.put(worldObj.provider.dimensionId, ret);
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
		public int		flag	= 2;

		public BlockChange(Vector3 location, World worldObj, Block blockTo)
		{
			dimension = worldObj.provider.dimensionId;
			this.location = location.copy();
			this.blockTo = blockTo;
		}

		public BlockChange(Vector3 location, World worldObj, Block blockTo, int meta)
		{
			dimension = worldObj.provider.dimensionId;
			this.location = location.copy();
			this.blockTo = blockTo;
			this.metaTo = meta;
		}

		public boolean changeBlock(World worldObj)
		{
			boolean ret = location.setBlock(worldObj, blockTo, metaTo, flag);
			worldObj.scheduledUpdatesAreImmediate = true;
			blockTo.updateTick(worldObj, location.intX(), location.intY(), location.intZ(), worldObj.rand);
			worldObj.scheduledUpdatesAreImmediate = false;

			return ret;
		}

		public String toString()
		{
			return blockTo + " " + dimension + " " + location;
		}

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
