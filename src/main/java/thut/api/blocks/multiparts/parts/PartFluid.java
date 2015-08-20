package thut.api.blocks.multiparts.parts;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;

import scala.collection.Iterator;
import thut.api.ThutBlocks;
import thut.api.blocks.BlockFluid;
import thut.api.blocks.multiparts.IPartMeta;
import thut.api.maths.Vector3;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Cuboid6;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TRandomUpdateTick;
import codechicken.multipart.TickScheduler;
import codechicken.multipart.TileMultipart;
import codechicken.multipart.minecraft.McMetaPart;
import cpw.mods.fml.common.FMLCommonHandler;

public class PartFluid extends McMetaPart implements IPartMeta {

	public PartFluid() {
	}

	public PartFluid(int meta) {
		this.meta = (byte) meta;
	}

	public String name;
	public Block block;
	protected boolean hasTile = false;

	public TileEntity tile;

	@Override
	public Cuboid6 getBounds() {
		return getBounds(meta);
	}

	public Cuboid6 getBounds(int meta) {
		float f = (meta + 1) / 16f;
		return new Cuboid6(0, 0, 0, 1, f, 1);
	}

    @Override
    public Iterable<Cuboid6> getCollisionBoxes()
    {
        return (Iterable<Cuboid6>) (doesTick()?Collections.emptyList():Arrays.asList(getBounds()));
    }
	
	@Override
	public Block getBlock() {
		return block;
	}

	@Override
	public String getType() {
		return name;
	}

	@Override
	public boolean activate(EntityPlayer player, MovingObjectPosition part,
			ItemStack item) {
		World world = world();
		if (world.isRemote)
			return true;
		BlockCoord pos = new BlockCoord(part.blockX, part.blockY, part.blockZ);
		if(tile!=null)
			tile.setWorldObj(world);
		boolean activate = block.onBlockActivated(world, part.blockX, part.blockY, part.blockZ,
				player, part.sideHit, (float) part.hitVec.xCoord,
				(float) part.hitVec.yCoord, (float) part.hitVec.zCoord);
		if(!activate && item!=null)
		{
			item.getItem().onItemUse(item, player, world, part.blockX, part.blockY, part.blockZ, part.sideHit, (float) part.hitVec.xCoord,
				(float) part.hitVec.yCoord, (float) part.hitVec.zCoord);
			sendDescUpdate();
		}
		return true;
	}

	@Override
	public void update() {
		super.update();
		if (world().isRemote || Math.random()>0.05)
			return;
		World world = world();

		TileEntity thisTile = tile();
		BlockCoord pos = getPos();
		block.updateTick(world, thisTile.xCoord, thisTile.yCoord,
				thisTile.zCoord, world.rand);
	}
	
	@Override
	public boolean doesTick()
	{
		return ((BlockFluid)block).getFluid().getViscosity() != Integer.MAX_VALUE;
	}

	@Override
	public void onAdded() {
		super.onAdded();
	}

	//
	@Override
	public void onWorldJoin() {
		super.onWorldJoin();
	}

    @Override
    public void save(NBTTagCompound tag)
    {
    	super.save(tag);
    	if(hasTile && tile!=null)
    	{
    		NBTTagCompound tetag = new NBTTagCompound();
    		tile.writeToNBT(tetag);
    		tag.setTag("tetag", tetag);
    	}
    }
    
    @Override
    public void load(NBTTagCompound tag)
    {
    	super.load(tag);    	
    	if(hasTile && tile!=null)
    	{
    		tile.readFromNBT(tag.getCompoundTag("tetag"));
    	}
    }
    
    @Override
    public void writeDesc(MCDataOutput packet)
    {
    	super.writeDesc(packet);
    	if(hasTile && tile!=null)
    	{
    		NBTTagCompound tag = new NBTTagCompound();
    		tile.writeToNBT(tag);
    		packet.writeNBTTagCompound(tag);
    	}
    }
    
    @Override
    public void readDesc(MCDataInput packet)
    {
    	super.readDesc(packet);
    	if(hasTile && tile!=null)
    	{
    		NBTTagCompound tag =packet.readNBTTagCompound();
    	//	System.out.println(tag);
    		tile.readFromNBT(tag);
    	}
    }


	@Override
	public TileEntity getTileEntity() {
		return getFluidTile(world(), getPos());
	}

	public static boolean isFluid(World world, BlockCoord pos) {
		if (world.getBlock(pos.x, pos.y, pos.z) instanceof BlockFluid)
			return true;
		TileMultipart tile = TileMultipart.getOrConvertTile(world, pos);
		if (tile != null) {
			Iterator<TMultiPart> it = tile.partList().iterator();
			while (it.hasNext()) {
				TMultiPart p = it.next();
				if (p instanceof PartFluid) {
					return true;
				}
			}
		}

		return false;
	}

	public static TileEntity getFluidTile(World world, BlockCoord pos) {
		if (world.getBlock(pos.x, pos.y, pos.z) instanceof BlockFluid)
			return world.getTileEntity(pos.x, pos.y, pos.z);
		TileMultipart tile = TileMultipart.getOrConvertTile(world, pos);
		if (tile != null) {
			Iterator<TMultiPart> it = tile.partList().iterator();
			while (it.hasNext()) {
				TMultiPart p = it.next();
				if (p instanceof PartFluid) {
					PartFluid fluid = (PartFluid) p;
					Block b = fluid.getBlock();
					if (fluid.tile == null)
					{
						fluid.tile = ((ITileEntityProvider)b).createNewTileEntity(world, 0);
						fluid.tile.setWorldObj(world);
					}
					return fluid.tile;
				}
			}
		}
		return null;
	}

	public static Block getBlock(World world, BlockCoord pos) {
		TileMultipart tile = TileMultipart.getOrConvertTile(world, pos);
		if (tile != null) {
			Iterator<TMultiPart> it = tile.partList().iterator();
			while (it.hasNext()) {
				TMultiPart p = it.next();
				if (p instanceof PartFluid) {
					PartFluid fluid = (PartFluid) p;
					return fluid.getBlock();
				}
			}
		}
		return world.getBlock(pos.x, pos.y, pos.z);
	}
}
