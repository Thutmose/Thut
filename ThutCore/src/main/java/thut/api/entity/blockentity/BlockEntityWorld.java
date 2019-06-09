package thut.api.entity.blockentity;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockEntityWorld extends World
{
    final World                  world;
    final IBlockEntity           blockEntity;
    final Entity                 entity;
    public boolean               creating;

    private Map<BlockPos, Chunk> chunks     = Maps.newHashMap();
    private BlockPos             lastOrigin = null;

    public BlockEntityWorld(IBlockEntity lift, World world)
    {
        super(world.getSaveHandler(), world.getWorldInfo(), world.provider, world.profiler, world.isRemote);
        this.world = world;
        this.blockEntity = lift;
        this.entity = (Entity) lift;
        int xMin = blockEntity.getMin().getX();
        int zMin = blockEntity.getMin().getZ();
        int yMin = blockEntity.getMin().getY();
        if (blockEntity.getBlocks() == null)
        {
            entity.setDead();
            return;
        }
        int sizeX = blockEntity.getBlocks().length;
        int sizeY = blockEntity.getBlocks()[0].length;
        int sizeZ = blockEntity.getBlocks()[0][0].length;
        for (int i = 0; i < sizeX; i++)
            for (int j = 0; j < sizeY; j++)
                for (int k = 0; k < sizeZ; k++)
                {
                    if (blockEntity.getTiles()[i][j][k] != null)
                    {
                        BlockPos pos = new BlockPos(i + xMin + entity.posX, j + yMin + entity.posY,
                                k + zMin + entity.posZ);
                        blockEntity.getTiles()[i][j][k].setWorld(world);
                        blockEntity.getTiles()[i][j][k].setPos(pos);
                        blockEntity.getTiles()[i][j][k].validate();
                    }
                }
    }

    @Nullable
    @Override
    public PlayerEntity getClosestPlayer(double posX, double posY, double posZ, double distance, boolean spectator)
    {
        return world.getClosestPlayer(posX, posY, posZ, distance, spectator);
    }

    @Nullable
    @Override
    public PlayerEntity getNearestAttackablePlayer(double posX, double posY, double posZ, double maxXZDistance,
            double maxYDistance, @Nullable Function<PlayerEntity, Double> playerToDouble,
            @Nullable Predicate<PlayerEntity> p_184150_12_)
    {
        return world.getNearestAttackablePlayer(posX, posY, posZ, maxXZDistance, maxYDistance, playerToDouble,
                p_184150_12_);
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos)
    {
        if (blockEntity.getTiles() == null) { return world.getTileEntity(pos); }
        int i = pos.getX() - MathHelper.floor(entity.posX + blockEntity.getMin().getX());
        int j = (int) (pos.getY() - Math.round(entity.posY + blockEntity.getMin().getY()));
        int k = pos.getZ() - MathHelper.floor(entity.posZ + blockEntity.getMin().getZ());
        if (!inBounds(pos)) { return world.getTileEntity(pos); }
        TileEntity tile = blockEntity.getTiles()[i][j][k];
        if (tile != null)
        {
            tile.setWorld(this);
            boolean invalid = tile.isInvalid();
            if (!invalid) tile.invalidate();
            tile.setPos(pos.toImmutable());
            tile.validate();
        }
        return tile;
    }

    @Override
    public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn)
    {
        if (blockEntity.getTiles() == null) return;
        int i = pos.getX() - MathHelper.floor(entity.posX + blockEntity.getMin().getX());
        int j = (int) (pos.getY() - Math.round(entity.posY + blockEntity.getMin().getY()));
        int k = pos.getZ() - MathHelper.floor(entity.posZ + blockEntity.getMin().getZ());
        if (!inBounds(pos)) { return; }
        blockEntity.getTiles()[i][j][k] = tileEntityIn;
        tileEntityIn.setWorld(this);
        tileEntityIn.setPos(pos.toImmutable());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getCombinedLight(BlockPos pos, int lightValue)
    {
        return 15 << 20 | 15 << 4;
    }

    @Override
    public BlockState getBlockState(BlockPos pos)
    {
        if (blockEntity.getBlocks() == null) { return world.getBlockState(pos); }
        int i = pos.getX() - MathHelper.floor(entity.posX + blockEntity.getMin().getX());
        int j = (int) (pos.getY() - Math.round(entity.posY + blockEntity.getMin().getY()));
        int k = pos.getZ() - MathHelper.floor(entity.posZ + blockEntity.getMin().getZ());
        if (!inBounds(pos)) { return world.getBlockState(pos); }
        BlockState state = blockEntity.getBlocks()[i][j][k];
        if (state == null) return world.getBlockState(pos);
        return state;
    }

    private boolean inBounds(BlockPos pos)
    {
        int i = pos.getX() - MathHelper.floor(entity.posX + blockEntity.getMin().getX());
        int j = (int) (pos.getY() - Math.round(entity.posY + blockEntity.getMin().getY()));
        int k = pos.getZ() - MathHelper.floor(entity.posZ + blockEntity.getMin().getZ());
        if (i >= blockEntity.getBlocks().length || j >= blockEntity.getBlocks()[0].length
                || k >= blockEntity.getBlocks()[0][0].length || i < 0 || j < 0 || k < 0) { return false; }
        return true;
    }

    private boolean intersects(AxisAlignedBB other)
    {
        BlockPos pos = entity.getPosition();
        AxisAlignedBB thisBox = new AxisAlignedBB(blockEntity.getMin().add(pos).add(-1, -1, -1),
                blockEntity.getMax().add(pos).add(1, 1, 1));
        return thisBox.intersects(other);
    }

    @Override
    public boolean isAirBlock(BlockPos pos)
    {
        BlockState state = getBlockState(pos);
        return state.getBlock().isAir(state, this, pos);
    }

    @Override
    public Biome getBiome(BlockPos pos)
    {
        return world.getBiome(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, Direction direction)
    {
        return world.getStrongPower(pos, direction);
    }

    @Override
    public WorldType getWorldType()
    {
        return world.getWorldType();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, Direction side, boolean _default)
    {
        return getBlockState(pos).isSideSolid(this, pos, side);
    }

    @Override
    protected IChunkProvider createChunkProvider()
    {
        return null;
    }

    /** Gets the chunk at the specified location. */
    @Override
    public Chunk getChunk(int chunkX, int chunkZ)
    {
        AxisAlignedBB chunkBox = new AxisAlignedBB(chunkX * 16, 0, chunkZ * 16, chunkX * 16 + 15, world.getHeight(),
                chunkZ * 16 + 15);
        if (!intersects(chunkBox)) return world.getChunk(chunkX, chunkZ);

        if (lastOrigin == null || !lastOrigin.equals(entity.getPosition()))
        {
            lastOrigin = entity.getPosition();
            chunks.clear();
        }
        MutableBlockPos pos = new MutableBlockPos();
        pos.setPos(chunkX, 0, chunkZ);
        BlockPos immut = pos.toImmutable();
        if (chunks.containsKey(immut)) return chunks.get(immut);
        Chunk ret = new Chunk(this, chunkX, chunkZ);
        chunks.put(immut, ret);
        for (int i = 0; i < 16; i++)
            for (int j = 0; j < 256; j++)
                for (int k = 0; k < 16; k++)
                {
                    int x = chunkX * 16 + i;
                    int y = j;
                    int z = chunkZ * 16 + k;
                    pos.setPos(x, y, z);
                    BlockState state = getBlockState(pos);
                    if (state.getBlock() == Blocks.AIR) continue;
                    ExtendedBlockStorage storage = ret.getBlockStorageArray()[j >> 4];
                    if (storage == null)
                    {
                        storage = new ExtendedBlockStorage(j >> 4 << 4, this.world.dimension.hasSkyLight());
                        ret.getBlockStorageArray()[j >> 4] = storage;
                    }
                    storage.set(i & 15, j & 15, k & 15, state);
                    TileEntity tile = getTileEntity(pos);
                    if (tile != null) ret.addTileEntity(tile);
                }
        return ret;
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty)
    {
        return false;
    }

    /** Sets the block state at a given location. Flag 1 will cause a block
     * update. Flag 2 will send the change to clients (you almost always want
     * this). Flag 4 prevents the block from being re-rendered, if this is a
     * client world. Flags can be added together. */
    @Override
    public boolean setBlockState(BlockPos pos, BlockState newState, int flags)
    {
        int i = pos.getX() - MathHelper.floor(entity.posX + blockEntity.getMin().getX());
        int j = (int) (pos.getY() - Math.round(entity.posY + blockEntity.getMin().getY()));
        int k = pos.getZ() - MathHelper.floor(entity.posZ + blockEntity.getMin().getZ());
        if (blockEntity.getBlocks() == null) return false;
        if (!inBounds(pos)) { return false; }
        blockEntity.getBlocks()[i][j][k] = newState;
        return true;
    }

    public World getWorld()
    {
        return world;
    }

    public IBlockEntity getEntity()
    {
        return blockEntity;
    }

    @Override
    public ITickList<Block> getPendingBlockTicks()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ITickList<Fluid> getPendingFluidTicks()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void playEvent(PlayerEntity p_217378_1_, int p_217378_2_, BlockPos p_217378_3_, int p_217378_4_)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<? extends PlayerEntity> getPlayers()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void playSound(PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category,
            float volume, float pitch)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void playMovingSound(PlayerEntity p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_,
            SoundCategory p_217384_4_, float p_217384_5_, float p_217384_6_)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Entity getEntityByID(int id)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MapData func_217406_a(String p_217406_1_)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void func_217399_a(MapData p_217399_1_)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int func_217395_y()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Scoreboard getScoreboard()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RecipeManager getRecipeManager()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NetworkTagManager getTags()
    {
        // TODO Auto-generated method stub
        return null;
    }
}