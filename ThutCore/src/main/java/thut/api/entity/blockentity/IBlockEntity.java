package thut.api.entity.blockentity;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameData;

public interface IBlockEntity
{
    public static Set<String> TEWHITELIST = Sets.newHashSet();

    public static class BlockEntityFormer
    {
        private static final Logger LOGGER = LogManager.getLogger();

        public static RayTraceResult rayTraceInternal(Vec3d start, Vec3d end, IBlockEntity toTrace)
        {
            return toTrace.getFakeWorld().rayTraceBlocks(start, end, false, true, false);
        }

        public static void removeBlocks(World worldObj, BlockPos min, BlockPos max, BlockPos pos)
        {
            int xMin = min.getX();
            int zMin = min.getZ();
            int xMax = max.getX();
            int zMax = max.getZ();
            int yMin = min.getY();
            int yMax = max.getY();
            for (int i = xMin; i <= xMax; i++)
                for (int j = yMin; j <= yMax; j++)
                    for (int k = zMin; k <= zMax; k++)
                    {
                        BlockPos temp = pos.add(i, j, k);
                        TileEntity tile = worldObj.getTileEntity(temp);
                        if (tile != null) tile.invalidate();
                        worldObj.setBlockState(temp, Blocks.AIR.getDefaultState(), 2);
                    }
        }

        public static IBlockState[][][] checkBlocks(World worldObj, BlockPos min, BlockPos max, BlockPos pos)
        {
            int xMin = min.getX();
            int zMin = min.getZ();
            int xMax = max.getX();
            int zMax = max.getZ();
            int yMin = min.getY();
            int yMax = max.getY();
            IBlockState[][][] ret = new IBlockState[(xMax - xMin) + 1][(yMax - yMin) + 1][(zMax - zMin) + 1];
            for (int i = xMin; i <= xMax; i++)
                for (int j = yMin; j <= yMax; j++)
                    for (int k = zMin; k <= zMax; k++)
                    {
                        IBlockState state = worldObj.getBlockState(pos.add(i, j, k));
                        ret[i - xMin][j - yMin][k - zMin] = state;
                    }
            return ret;
        }

        public static void RevertEntity(IBlockEntity toRevert)
        {
            int xMin = toRevert.getMin().getX();
            int zMin = toRevert.getMin().getZ();
            int yMin = toRevert.getMin().getY();
            if (toRevert.getBlocks() == null) return;
            int sizeX = toRevert.getBlocks().length;
            int sizeY = toRevert.getBlocks()[0].length;
            int sizeZ = toRevert.getBlocks()[0][0].length;
            Entity entity = (Entity) toRevert;
            for (int i = 0; i < sizeX; i++)
                for (int j = 0; j < sizeY; j++)
                    for (int k = 0; k < sizeZ; k++)
                    {
                        BlockPos pos = new BlockPos(i + xMin + entity.posX, j + yMin + entity.posY,
                                k + zMin + entity.posZ);
                        IBlockState state = toRevert.getFakeWorld().getBlockState(pos);
                        TileEntity tile = toRevert.getFakeWorld().getTileEntity(pos);
                        if (state != null)
                        {
                            entity.worldObj.setBlockState(pos, state);
                            if (tile != null)
                            {
                                TileEntity newTile = entity.worldObj.getTileEntity(pos);
                                if (newTile != null) newTile.readFromNBT(tile.writeToNBT(new NBTTagCompound()));
                            }
                        }
                    }
            List<Entity> possibleInside = entity.worldObj.getEntitiesWithinAABBExcludingEntity(entity,
                    entity.getEntityBoundingBox());
            for (Entity e : possibleInside)
            {
                e.setPosition(e.posX, e.posY + 0.25, e.posZ);
            }
        }

        public static TileEntity[][][] checkTiles(World worldObj, BlockPos min, BlockPos max, BlockPos pos)
        {
            int xMin = min.getX();
            int zMin = min.getZ();
            int xMax = max.getX();
            int zMax = max.getZ();
            int yMin = min.getY();
            int yMax = max.getY();
            TileEntity[][][] ret = new TileEntity[(xMax - xMin) + 1][(yMax - yMin) + 1][(zMax - zMin) + 1];
            for (int i = xMin; i <= xMax; i++)
                for (int j = yMin; j <= yMax; j++)
                    for (int k = zMin; k <= zMax; k++)
                    {
                        BlockPos temp = pos.add(i, j, k);
                        IBlockState state = worldObj.getBlockState(temp);
                        if (((state.getBlock()) instanceof ITileEntityProvider))
                        {
                            TileEntity old = worldObj.getTileEntity(temp);
                            if (old != null)
                            {
                                NBTTagCompound tag = new NBTTagCompound();
                                tag = old.writeToNBT(tag);
                                ret[i - xMin][j - yMin][k - zMin] = makeTile(tag);
                            }
                        }
                    }
            return ret;
        }

        @SuppressWarnings("deprecation")
        public static TileEntity makeTile(NBTTagCompound compound)
        {
            TileEntity tileentity = null;
            String s = compound.getString("id");
            Class<? extends TileEntity> oclass = null;

            try
            {
                oclass = (Class<? extends TileEntity>)GameData.getTileEntityRegistry().getObject(new ResourceLocation(s));

                if (oclass != null)
                {
                    tileentity = (TileEntity)oclass.newInstance();
                }
            }
            catch (Throwable throwable1)
            {
                LOGGER.error("Failed to create block entity " + s, throwable1);
                net.minecraftforge.fml.common.FMLLog.log(org.apache.logging.log4j.Level.ERROR, throwable1,
                        "A TileEntity %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
                        s, oclass.getName());
            }

            if (tileentity != null)
            {
                try
                {
                    tileentity.readFromNBT(compound);
                }
                catch (Throwable throwable)
                {
                    LOGGER.error("Failed to load data for block entity " + s, throwable);
                    net.minecraftforge.fml.common.FMLLog.log(org.apache.logging.log4j.Level.ERROR, throwable,
                            "A TileEntity %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
                            s, oclass.getName());
                    tileentity = null;
                }
            }
            else
            {
                LOGGER.warn("Skipping BlockEntity with id " + s);
            }

            return tileentity;
        }

        public static <T extends Entity> T makeBlockEntity(World worldObj, BlockPos min, BlockPos max, BlockPos pos,
                Class<T> clas)
        {
            T ret = null;
            try
            {
                ret = clas.getConstructor(World.class).newInstance(worldObj);
            }
            catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e)
            {
                e.printStackTrace();
                return null;
            }
            if (!(ret instanceof IBlockEntity))
                throw new ClassCastException("Cannot cast " + clas + " to IBlockEntity");

            // This enforces that min is the lower corner, and max is the upper.
            AxisAlignedBB box = new AxisAlignedBB(min, max);
            min = new BlockPos(box.minX, box.minY, box.minZ);
            max = new BlockPos(box.maxX, box.maxY, box.maxZ);
            IBlockEntity entity = (IBlockEntity) ret;
            ret.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            entity.setBlocks(checkBlocks(worldObj, min, max, pos));
            entity.setTiles(checkTiles(worldObj, min, max, pos));
            entity.setMin(min);
            entity.setMax(max);
            removeBlocks(worldObj, min, max, pos);
            worldObj.spawnEntityInWorld(ret);
            // Enforce that max radius is kept if this mob is larger.
            World.MAX_ENTITY_RADIUS = Math.max(World.MAX_ENTITY_RADIUS, (box.maxX - box.minX) / 2);
            World.MAX_ENTITY_RADIUS = Math.max(World.MAX_ENTITY_RADIUS, (box.maxY - box.minY) / 2);
            World.MAX_ENTITY_RADIUS = Math.max(World.MAX_ENTITY_RADIUS, (box.maxZ - box.minZ) / 2);
            return ret;
        }
    }

    void setBlocks(IBlockState[][][] blocks);

    IBlockState[][][] getBlocks();

    void setTiles(TileEntity[][][] tiles);

    TileEntity[][][] getTiles();

    BlockPos getMin();

    BlockPos getMax();

    void setMin(BlockPos pos);

    void setMax(BlockPos pos);

    BlockEntityWorld getFakeWorld();

    void setFakeWorld(BlockEntityWorld world);

}
