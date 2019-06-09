package thut.api.entity.blockentity;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface IBlockEntity
{
    static Set<ResourceLocation>         BLOCKBLACKLIST = Sets.newHashSet();
    static Set<String>                   TEBLACKLIST    = Sets.newHashSet();
    static BiMap<Class<?>, ITileRemover> CUSTOMREMOVERS = HashBiMap.create();
    List<ITileRemover>                   SORTEDREMOVERS = Lists.newArrayList();

    static final ITileRemover            DEFAULTREMOVER = new ITileRemover()
                                                        {

                                                            @Override
                                                            public void preBlockRemoval(TileEntity tileIn)
                                                            {
                                                                tileIn.remove();
                                                            }

                                                            @Override
                                                            public void postBlockRemoval(TileEntity tileIn)
                                                            {
                                                            }
                                                        };

    public static void addRemover(ITileRemover remover, Class<?> clas)
    {
        CUSTOMREMOVERS.put(clas, remover);
        SORTEDREMOVERS.add(remover);
        Collections.sort(SORTEDREMOVERS, new Comparator<ITileRemover>()
        {
            @Override
            public int compare(ITileRemover o1, ITileRemover o2)
            {
                return o1.getPriority() - o2.getPriority();
            }
        });
    }

    public static interface ITileRemover
    {
        void preBlockRemoval(TileEntity tileIn);

        void postBlockRemoval(TileEntity tileIn);

        default int getPriority()
        {
            return 0;
        }
    }

    public static ITileRemover getRemover(TileEntity tile)
    {
        ITileRemover ret = CUSTOMREMOVERS.get(tile.getClass());
        if (ret != null) return ret;
        for (ITileRemover temp : SORTEDREMOVERS)
        {
            Class<?> key = CUSTOMREMOVERS.inverse().get(temp);
            if (key.isInstance(tile)) return temp;
        }
        return DEFAULTREMOVER;
    }

    public static class BlockEntityFormer
    {
        public static RayTraceResult rayTraceInternal(Vec3d start, Vec3d end, IBlockEntity toTrace)
        {
            RayTraceContext context = new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER,
                    RayTraceContext.FluidMode.NONE, (Entity) toTrace);
            return toTrace.getFakeWorld().func_217299_a(context);
        }

        public static void removeBlocks(World world, BlockPos min, BlockPos max, BlockPos pos)
        {
            int xMin = min.getX();
            int zMin = min.getZ();
            int xMax = max.getX();
            int zMax = max.getZ();
            int yMin = min.getY();
            int yMax = max.getY();
            MutableBlockPos temp = new MutableBlockPos();
            for (int i = xMin; i <= xMax; i++)
                for (int j = yMin; j <= yMax; j++)
                    for (int k = zMin; k <= zMax; k++)
                    {
                        temp.setPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k);
                        TileEntity tile = world.getTileEntity(temp);
                        ITileRemover tileHandler = null;
                        if (tile != null)
                        {
                            tileHandler = getRemover(tile);
                            tileHandler.preBlockRemoval(tile);
                        }
                    }
            for (int i = xMin; i <= xMax; i++)
                for (int j = yMin; j <= yMax; j++)
                    for (int k = zMin; k <= zMax; k++)
                    {
                        temp.setPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k);
                        TileEntity tile = world.getTileEntity(temp);
                        ITileRemover tileHandler = null;
                        if (tile != null)
                        {
                            tileHandler = getRemover(tile);
                        }
                        world.setBlockState(temp, Blocks.AIR.getDefaultState(), 2);
                        if (tileHandler != null) tileHandler.postBlockRemoval(tile);
                    }
            for (int i = xMin; i <= xMax; i++)
                for (int j = yMin; j <= yMax; j++)
                    for (int k = zMin; k <= zMax; k++)
                    {
                        temp.setPos(pos.getX() + i, pos.getY() + j, pos.getZ() + k);
                        world.setBlockState(temp, Blocks.AIR.getDefaultState(), 3);
                    }
        }

        public static BlockState[][][] checkBlocks(World world, BlockPos min, BlockPos max, BlockPos pos)
        {
            int xMin = min.getX();
            int zMin = min.getZ();
            int xMax = max.getX();
            int zMax = max.getZ();
            int yMin = min.getY();
            int yMax = max.getY();
            BlockState[][][] ret = new BlockState[(xMax - xMin) + 1][(yMax - yMin) + 1][(zMax - zMin) + 1];
            boolean valid = false;
            BlockPos temp;
            for (int i = xMin; i <= xMax; i++)
                for (int j = yMin; j <= yMax; j++)
                    for (int k = zMin; k <= zMax; k++)
                    {
                        temp = pos.add(i, j, k);
                        BlockState state = world.getBlockState(temp);
                        if (BLOCKBLACKLIST.contains(state.getBlock().getRegistryName())) return null;
                        valid = valid || !state.getBlock().isAir(state, world, pos);
                        ret[i - xMin][j - yMin][k - zMin] = state;
                    }
            return valid ? ret : null;
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
                        // TODO Apply transformation onto this pos based on
                        // whether the entity is rotated, and then also call the
                        // block's rotate method as well before placing the
                        // BlockState.
                        BlockPos pos = new BlockPos(i + xMin + entity.posX, j + yMin + entity.posY,
                                k + zMin + entity.posZ);
                        BlockState state = toRevert.getFakeWorld().getBlockState(pos);
                        TileEntity tile = toRevert.getFakeWorld().getTileEntity(pos);
                        if (state != null)
                        {
                            entity.getEntityWorld().setBlockState(pos, state);
                            if (tile != null)
                            {
                                TileEntity newTile = entity.getEntityWorld().getTileEntity(pos);
                                if (newTile != null) newTile.read(tile.write(new CompoundNBT()));
                            }
                        }
                    }
            List<Entity> possibleInside = entity.getEntityWorld().getEntitiesWithinAABBExcludingEntity(entity,
                    entity.getBoundingBox());
            for (Entity e : possibleInside)
            {
                e.setPosition(e.posX, e.posY + 0.25, e.posZ);
            }
        }

        public static TileEntity[][][] checkTiles(World world, BlockPos min, BlockPos max, BlockPos pos)
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
                        TileEntity old = world.getTileEntity(temp);
                        if (old != null)
                        {
                            CompoundNBT tag = new CompoundNBT();
                            tag = old.write(tag);
                            ret[i - xMin][j - yMin][k - zMin] = TileEntity.create(tag);
                        }
                    }
            return ret;
        }

        public static <T extends Entity> T makeBlockEntity(World world, BlockPos min, BlockPos max, BlockPos pos,
                Class<T> clas)
        {
            T ret = null;
            try
            {
                ret = clas.getConstructor(World.class).newInstance(world);
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
            BlockState[][][] blocks = checkBlocks(world, min, max, pos);
            if (blocks == null) return null;
            entity.setBlocks(blocks);
            entity.setTiles(checkTiles(world, min, max, pos));
            entity.setMin(min);
            entity.setMax(max);
            removeBlocks(world, min, max, pos);
            world.func_217376_c(ret);
            return ret;
        }
    }

    void setBlocks(BlockState[][][] blocks);

    BlockState[][][] getBlocks();

    void setTiles(TileEntity[][][] tiles);

    TileEntity[][][] getTiles();

    BlockPos getMin();

    BlockPos getMax();

    void setMin(BlockPos pos);

    void setMax(BlockPos pos);

    BlockEntityWorld getFakeWorld();

    void setFakeWorld(BlockEntityWorld world);

    void setSize(EntitySize size);

    default boolean shouldHide(BlockPos pos)
    {
        TileEntity tile = getFakeWorld().getTileEntity(pos);
        if (tile != null && !BlockEntityUpdater.isWhitelisted(tile)) { return true; }
        return false;
    }

}
