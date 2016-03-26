package thut.tech.common.blocks.door;

import java.util.Set;

import javax.vecmath.Vector3f;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityDoor extends TileEntity implements ITickable
{
    public boolean        isCore   = true;
    public boolean        madeDoor = false;
    public IBlockState    state;
    double                width    = -1;
    double                height   = -1;
    public Vector3f       shift    = new Vector3f();
    EnumFacing            normal;
    public TileEntityDoor core     = null;
    private BlockPos      corePos  = null;
    Set<TileEntityDoor>   parts    = Sets.newHashSet();

    AxisAlignedBB box = null;

    private void addPart(TileEntityDoor part)
    {
        part.madeDoor = true;
        parts.add(part);
    }

    public void createDoor()
    {
        if (!madeDoor) // TODO make check for cuboid valid of blocks to use as
                       // the door.
            for (int k = -1; k <= 1; k++)
        {
            for (int i = -1; i <= 1; i++)
            {
                for (int j = 0; j <= 2; j++)
                {
                    BlockPos pos = new BlockPos(i + getPos().getX(), j + getPos().getY(), k + getPos().getZ());
                    if (!pos.equals(getPos()))
                    {
                        IBlockState state = worldObj.getBlockState(pos);
                        if (state != null && state.getBlock().getMaterial().isSolid())
                        {
                            worldObj.setBlockState(pos, BlockDoor.instance.getDefaultState());
                            TileEntityDoor tile = (TileEntityDoor) worldObj.getTileEntity(pos);
                            tile.state = state;
                            tile.madeDoor = true;
                            tile.setCore(this);
                        }
                    }
                }
            }
        }
        madeDoor = true;
    }

    private AxisAlignedBB getBounds()
    {
        AxisAlignedBB ret = box;
        if (state != null && state.getBlock() != BlockDoor.instance)
        {
            box = state.getBlock().getCollisionBoundingBox(worldObj, getPos(), state);
            ret = box;
        }
        else if (ret == null)
        {
            ret = new AxisAlignedBB(getPos(), getPos().add(1, 1, 1));
            box = ret;
        }
        return ret;
    }

    public AxisAlignedBB getBounds(float partialTick)
    {
        AxisAlignedBB ret = box;

        if (core != null && core != this) { return core.getBounds(partialTick); }

        if (state != null && state.getBlock() != BlockDoor.instance)
        {
            box = state.getBlock().getCollisionBoundingBox(worldObj, getPos(), state);
            Vector3f shift = getShiftForPart(this, partialTick);
            for (TileEntityDoor door : parts)
            {
                box = box.union(door.getBounds());
            }
            ret = box.offset(shift.x, shift.y, shift.z);
        }
        else if (ret == null)
        {
            ret = new AxisAlignedBB(getPos(), getPos().add(1, 1, 1));
            for (TileEntityDoor door : parts)
            {
                ret = ret.union(door.getBounds());
            }
            box = ret;
        }
        return ret;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new S35PacketUpdateTileEntity(this.getPos(), 3, nbttagcompound);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public net.minecraft.util.AxisAlignedBB getRenderBoundingBox()
    {
        net.minecraft.util.AxisAlignedBB bb = INFINITE_EXTENT_AABB;
        return bb;
    }

    public Vector3f getShiftForPart(TileEntityDoor part, float partialTick)
    {
        if (core != null && core != this) return core.getShiftForPart(part, partialTick);
        if (madeDoor)
        {
            float num = 3 * MathHelper.cos((worldObj.getTotalWorldTime() + partialTick) / 30f);
            shift.x = num;
        }
        return shift;
    }

    public double getWidth()
    {
        // TODO use this to detemine if the block is out of bounds to stop
        // rendering.
        return width;
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
        NBTTagCompound nbttagcompound = pkt.getNbtCompound();
        this.readFromNBT(nbttagcompound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        if (compound.hasKey("corex"))
        {
            corePos = new BlockPos(compound.getInteger("corex"), compound.getInteger("corey"),
                    compound.getInteger("corez"));
        }
        if (compound.hasKey("state"))
        {
            NBTTagCompound tag = compound.getCompoundTag("state");
            ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
            Block block = Block.getBlockFromItem(stack.getItem());
            state = block.getStateFromMeta(stack.getItemDamage());
        }
        madeDoor = compound.getBoolean("made");
    }

    public void setCore(TileEntityDoor core)
    {
        if (core == this) core = null;
        this.core = core;
        isCore = core == null;
        if (core != null) core.addPart(this);
    }

    @Override
    public void update()
    {
        if (corePos != null && core == null)
        {
            if (worldObj.getTileEntity(corePos) instanceof TileEntityDoor)
            {
                core = (TileEntityDoor) worldObj.getTileEntity(corePos);
                this.setCore(core);
            }
            corePos = null;

        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        if (core != null)
        {
            compound.setInteger("corex", core.pos.getX());
            compound.setInteger("corey", core.pos.getY());
            compound.setInteger("corez", core.pos.getZ());
        }
        if (state != null)
        {
            NBTTagCompound tag = new NBTTagCompound();
            ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
            stack.writeToNBT(tag);
            compound.setTag("state", tag);
        }
        compound.setBoolean("made", madeDoor);
    }
}
