package thut.api.blocks.multiparts.parts;

import java.util.ArrayList;
import java.util.Arrays;

import scala.collection.Iterator;
import thut.api.ThutBlocks;
import thut.api.blocks.IRebar;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import codechicken.multipart.minecraft.McMetaPart;
import codechicken.multipart.minecraft.McSidedMetaPart;

public class PartRebar extends McMetaPart
{

	public TileEntity te;

	public PartRebar()
	{
	}

	public PartRebar(int meta)
	{
		super(meta);
		if (getBlock() != null) te = getBlock().createTileEntity(getWorld(), 0);
	}

	@Override
	public Cuboid6 getBounds()
	{

		return new Cuboid6(0.35, 0.35, 0.35, 0.65, 0.65, 0.65);
		
	}

	@Override
	public Iterable<Cuboid6> getCollisionBoxes()
	{
		ArrayList list = new ArrayList();
		if (getBlock() instanceof IRebar && tile()!=null)
		{
			boolean[] sides = ((IRebar) getBlock()).sides(getWorld(), getPos().x, getPos().y, getPos().z);
			
			boolean none = !(sides[0]||sides[1]||sides[2]||sides[3]||sides[4]||sides[5]);
			
			for (ForgeDirection fside : ForgeDirection.VALID_DIRECTIONS)
			{
				int n = fside.ordinal();
				if (sides[n])
				{
					list.add(getBoundingBoxForSide(fside));
				}
			}
			//System.out.println(list.size()+" "+Arrays.toString(sides));
		}
		else
		{
			list.add(getBounds());
		}
		return list;
	}

	public Cuboid6 getBoundingBoxForSide(ForgeDirection fside)
	{
		switch (fside)
		{
		case UP:
		{
			return new Cuboid6(0.35F, 0.4F, 0.35F, 0.65F, 0.95f, 0.65F);
		}
		case DOWN:
		{
			return new Cuboid6(0.35F, 0.05f, 0.35F, 0.65F, 0.6F, 0.65F);
		}
		case NORTH:
		{
			return new Cuboid6(0.35F, 0.35F, 0.05f, 0.65F, 0.65F, 0.6F);
		}
		case SOUTH:
		{
			return new Cuboid6(0.35F, 0.35F, 0.4F, 0.65F, 0.65F, 0.95f);
		}
		case EAST:
		{
			return new Cuboid6(0.4F, 0.35F, 0.35F, 0.95f, 0.65F, 0.65F);
		}
		case WEST:
		{
			return new Cuboid6(0.05f, 0.35F, 0.35F, 0.605f, 0.65F, 0.65F);
		}
		default:
		{
			return new Cuboid6(0.05f, 0.05f, 0.05f, 0.95f, 0.95f, 0.95f);
		}
		}
	}

	@Override
	public void save(NBTTagCompound tag)
	{
		super.save(tag);
		NBTTagCompound teTag = new NBTTagCompound();
		if (te != null) te.writeToNBT(teTag);
		tag.setTag("teTag", teTag);
	}

	public static boolean isRebar(World world, BlockCoord pos)
	{
		if (world.getBlock(pos.x, pos.y, pos.z) == ThutBlocks.liftRail) return true;
		TileMultipart tile = TileMultipart.getOrConvertTile(world, pos);
		if (tile != null)
		{
			Iterator<TMultiPart> it = tile.partList().iterator();
			while (it.hasNext())
			{
				TMultiPart p = it.next();
				if (p instanceof PartRebar) { return true; }
			}
		}

		return false;
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition part, ItemStack item)
	{
		World world = world();
		if (world.isRemote) return true;
		BlockCoord pos = new BlockCoord(part.blockX, part.blockY, part.blockZ);
		getBlock().onBlockActivated(world, part.blockX, part.blockY, part.blockZ, player, part.sideHit,
				(float) part.hitVec.xCoord, (float) part.hitVec.yCoord, (float) part.hitVec.zCoord);

		return true;
	}

	@Override
	public void load(NBTTagCompound tag)
	{
		super.load(tag);
		if (getBlock() != null) te = getBlock().createTileEntity(getWorld(), meta);
		NBTTagCompound teTag = tag.getCompoundTag("teTag");
		if (te != null) te.readFromNBT(teTag);
	}

	@Override
	public Block getBlock()
	{
		for (Block b : ThutBlocks.parts.keySet())
		{
			if (ThutBlocks.parts.get(b) == getClass()) { return b; }
		}
		return null;
	}

	@Override
	public String getType()
	{
		for (String s : ThutBlocks.parts2.keySet())
		{
			if (ThutBlocks.parts2.get(s) == getClass()) { return s; }
		}
		return null;
	}

}
