package thut.api.entity.blockentity;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

public class BlockEntityWorld extends World
{
    final World        world;
    final IBlockEntity blockEntity;
    final Entity       entity;

    public BlockEntityWorld(IBlockEntity lift, World world)
    {
        super(world.getSaveHandler(), world.getWorldInfo(), world.provider, world.theProfiler, world.isRemote);
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
                        blockEntity.getTiles()[i][j][k].setWorldObj(world);
                        blockEntity.getTiles()[i][j][k].setPos(pos);
                        blockEntity.getTiles()[i][j][k].validate();
                    }
                }
    }

    @Nullable
    @Override
    public EntityPlayer getClosestPlayer(double posX, double posY, double posZ, double distance, boolean spectator)
    {
        return world.getClosestPlayer(posX, posY, posZ, distance, spectator);
    }

    @Nullable
    @Override
    public EntityPlayer getNearestAttackablePlayer(double posX, double posY, double posZ, double maxXZDistance,
            double maxYDistance, @Nullable Function<EntityPlayer, Double> playerToDouble,
            @Nullable Predicate<EntityPlayer> p_184150_12_)
    {
        return world.getNearestAttackablePlayer(posX, posY, posZ, maxXZDistance, maxYDistance, playerToDouble,
                p_184150_12_);
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos)
    {
        if (blockEntity.getTiles() == null) { return world.getTileEntity(pos); }
        int i = pos.getX() - MathHelper.floor_double(entity.posX + blockEntity.getMin().getX());
        int j = (int) (pos.getY() - Math.round(entity.posY + blockEntity.getMin().getY()));
        int k = pos.getZ() - MathHelper.floor_double(entity.posZ + blockEntity.getMin().getZ());
        if (i >= blockEntity.getTiles().length || j >= blockEntity.getTiles()[0].length
                || k >= blockEntity.getTiles()[0][0].length || i < 0 || j < 0
                || k < 0) { return world.getTileEntity(pos); }
        if (blockEntity.getTiles()[i][j][k] != null)
        {
            blockEntity.getTiles()[i][j][k].setPos(pos.toImmutable());
            blockEntity.getTiles()[i][j][k].setWorldObj(this);
        }
        return blockEntity.getTiles()[i][j][k];
    }

    @Override
    public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn)
    {
        int i = pos.getX() - MathHelper.floor_double(entity.posX + blockEntity.getMin().getX());
        int j = (int) (pos.getY() - Math.round(entity.posY + blockEntity.getMin().getY()));
        int k = pos.getZ() - MathHelper.floor_double(entity.posZ + blockEntity.getMin().getZ());
        if (blockEntity.getTiles() == null) return;
        if (i >= blockEntity.getTiles().length || j >= blockEntity.getBlocks()[0].length
                || k >= blockEntity.getTiles()[0][0].length || i < 0 || j < 0 || k < 0) { return; }
        blockEntity.getTiles()[i][j][k] = tileEntityIn;
        tileEntityIn.setWorldObj(this);
        tileEntityIn.setPos(pos.toImmutable());
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue)
    {
        return 15 << 20 | 15 << 4;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos)
    {
        int i = pos.getX() - MathHelper.floor_double(entity.posX + blockEntity.getMin().getX());
        int j = (int) (pos.getY() - Math.round(entity.posY + blockEntity.getMin().getY()));
        int k = pos.getZ() - MathHelper.floor_double(entity.posZ + blockEntity.getMin().getZ());
        if (blockEntity.getBlocks() == null) { return world.getBlockState(pos); }
        if (i >= blockEntity.getBlocks().length || j >= blockEntity.getBlocks()[0].length
                || k >= blockEntity.getBlocks()[0][0].length || i < 0 || j < 0
                || k < 0) { return world.getBlockState(pos); }
        IBlockState state = blockEntity.getBlocks()[i][j][k];
        if (state == null) return world.getBlockState(pos);
        return state;
    }

    @Override
    public boolean isAirBlock(BlockPos pos)
    {
        IBlockState state = getBlockState(pos);
        return state.getBlock().isAir(state, this, pos);
    }

    @Override
    public Biome getBiome(BlockPos pos)
    {
        return world.getBiome(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction)
    {
        return world.getStrongPower(pos, direction);
    }

    @Override
    public WorldType getWorldType()
    {
        return world.getWorldType();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default)
    {
        return getBlockState(pos).isSideSolid(this, pos, side);
    }

    @Override
    protected IChunkProvider createChunkProvider()
    {
        return null;
    }

    /** Gets the chunk at the specified location. */
    public Chunk getChunkFromChunkCoords(int chunkX, int chunkZ)
    {// TODO make this instead check if in region and make fake chunks
     // accordingly.
     // System.out.println(chunkX+" "+chunkZ);

        Chunk ret = new Chunk(this, chunkX, chunkZ);
        for (int i = 0; i < 16; i++)
            for (int j = 0; j < 16; j++)
                for (int k = 0; k < 16; k++)
                {
                    ret.setBlockState(new BlockPos(i, j, k), Blocks.STONE.getDefaultState());
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
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags)
    {
        int i = pos.getX() - MathHelper.floor_double(entity.posX + blockEntity.getMin().getX());
        int j = (int) (pos.getY() - Math.round(entity.posY + blockEntity.getMin().getY()));
        int k = pos.getZ() - MathHelper.floor_double(entity.posZ + blockEntity.getMin().getZ());
        if (blockEntity.getBlocks() == null) return false;
        if (i >= blockEntity.getBlocks().length || j >= blockEntity.getBlocks()[0].length
                || k >= blockEntity.getBlocks()[0][0].length || i < 0 || j < 0 || k < 0) { return false; }
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
}