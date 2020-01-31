package thut.api.entity.blockentity.world.server;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.network.IPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.server.ServerWorld;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.entity.blockentity.world.client.IBlockEntityWorld;

public class ServerWorldEntity extends ServerWorld implements IBlockEntityWorld<ServerWorld>
{
    private static class NoopChunkStatusListener implements IChunkStatusListener
    {

        @Override
        public void start(final ChunkPos arg0)
        {
        }

        @Override
        public void statusChanged(final ChunkPos arg0, final ChunkStatus arg1)
        {
        }

        @Override
        public void stop()
        {
        }
    }

    final ServerWorld world;
    IBlockEntity      mob;
    public boolean    creating;

    public ServerWorldEntity(final ServerWorld world)
    {
        super(world.getServer(), world.getServer().getBackgroundExecutor(), world.getSaveHandler(), world
                .getWorldInfo(), world.dimension.getType(), world.getServer().getProfiler(),
                new NoopChunkStatusListener());
        this.world = world;
    }

    @Override
    public void addBlockEvent(final BlockPos pos, final Block blockIn, final int eventID, final int eventParam)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().addBlockEvent(pos, blockIn, eventID, eventParam);
    }

    @Override
    public void addTileEntities(final Collection<TileEntity> tileEntityCollection)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().addTileEntities(tileEntityCollection);
    }

    @Override
    public boolean addTileEntity(final TileEntity tile)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().addTileEntity(tile);
    }

    @Override
    public boolean areCollisionShapesEmpty(final AxisAlignedBB p_217351_1_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().areCollisionShapesEmpty(p_217351_1_);
    }

    @Override
    public boolean areCollisionShapesEmpty(final Entity p_217345_1_)
    {
        return this.getWrapped().areCollisionShapesEmpty(p_217345_1_);
    }

    @Override
    public boolean checkBlockCollision(final AxisAlignedBB bb)
    {
        return this.getWrapped().checkBlockCollision(bb);
    }

    @Override
    public boolean checkNoEntityCollision(final Entity p_217346_1_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().checkNoEntityCollision(p_217346_1_);
    }

    @Override
    public boolean checkNoEntityCollision(final Entity entityIn, final VoxelShape shape)
    {
        return this.getWrapped().checkNoEntityCollision(entityIn, shape);
    }

    @Override
    public void chunkCheck(final Entity p_217464_1_)
    {
        this.getWrapped().chunkCheck(p_217464_1_);
    }

    @Override
    public boolean chunkExists(final int p_217354_1_, final int p_217354_2_)
    {
        return this.getWrapped().chunkExists(p_217354_1_, p_217354_2_);
    }

    @Override
    public boolean containsAnyLiquid(final AxisAlignedBB bb)
    {
        return this.getWrapped().containsAnyLiquid(bb);
    }

    @Override
    public boolean destroyBlock(final BlockPos pos, final boolean dropBlock)
    {
        return this.getWrapped().destroyBlock(pos, dropBlock);
    }

    @Override
    public BlockState findBlockstateInArea(final AxisAlignedBB area, final Block blockIn)
    {
        return this.getWrapped().findBlockstateInArea(area, blockIn);
    }

    @Override
    public BlockRayTraceResult func_217296_a(final Vec3d p_217296_1_, final Vec3d p_217296_2_,
            final BlockPos p_217296_3_, final VoxelShape p_217296_4_, final BlockState p_217296_5_)
    {
        return this.getWrapped().func_217296_a(p_217296_1_, p_217296_2_, p_217296_3_, p_217296_4_, p_217296_5_);
    }

    @Override
    public boolean func_217350_a(final BlockState p_217350_1_, final BlockPos p_217350_2_,
            final ISelectionContext p_217350_3_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().func_217350_a(p_217350_1_, p_217350_2_, p_217350_3_);
    }

    @Override
    public BlockPos func_217383_a(final int p_217383_1_, final int p_217383_2_, final int p_217383_3_,
            final int p_217383_4_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().func_217383_a(p_217383_1_, p_217383_2_, p_217383_3_, p_217383_4_);
    }

    @Override
    public void func_217390_a(final Consumer<Entity> p_217390_1_, final Entity p_217390_2_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().func_217390_a(p_217390_1_, p_217390_2_);
    }

    @Override
    public void func_217391_K()
    {
        // TO DO Auto-generated method stub
        this.getWrapped().func_217391_K();
    }

    @Override
    public void func_217393_a(final BlockPos p_217393_1_, final BlockState p_217393_2_, final BlockState p_217393_3_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().func_217393_a(p_217393_1_, p_217393_2_, p_217393_3_);
    }

    @Override
    public boolean func_217400_a(final BlockPos p_217400_1_, final Entity p_217400_2_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().func_217400_a(p_217400_1_, p_217400_2_);
    }

    @Override
    public void func_217441_a(final Chunk p_217441_1_, final int p_217441_2_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().func_217441_a(p_217441_1_, p_217441_2_);
    }

    @Override
    public PointOfInterestManager func_217443_B()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().func_217443_B();
    }

    @Override
    public int getActualHeight()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getActualHeight();
    }

    @Override
    public Biome getBiome(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getBiome(pos);
    }

    @Override
    public Biome getBiomeBody(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getBiomeBody(pos);
    }

    @Override
    public IBlockEntity getBlockEntity()
    {
        return this.mob;
    }

    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        final BlockState state = this.getBlock(pos);
        if (state == null) return this.world.getBlockState(pos);
        return state;
    }

    @Override
    public float getBrightness(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getBrightness(pos);
    }

    @Override
    public IChunk getChunk(final BlockPos p_217349_1_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getChunk(p_217349_1_);
    }

    @Override
    public Chunk getChunk(final int chunkX, final int chunkZ)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getChunk(chunkX, chunkZ);
    }

    @Override
    public IChunk getChunk(final int p_217348_1_, final int p_217348_2_, final ChunkStatus p_217348_3_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getChunk(p_217348_1_, p_217348_2_, p_217348_3_);
    }

    @Override
    public IChunk getChunk(final int p_217353_1_, final int p_217353_2_, final ChunkStatus p_217353_3_,
            final boolean p_217353_4_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getChunk(p_217353_1_, p_217353_2_, p_217353_3_, p_217353_4_);
    }

    @Override
    public ChunkStatus getChunkStatus()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getChunkStatus();
    }

    @Override
    public Stream<VoxelShape> getCollisionShapes(final Entity p_217352_1_, final AxisAlignedBB p_217352_2_,
            final Set<Entity> p_217352_3_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getCollisionShapes(p_217352_1_, p_217352_2_, p_217352_3_);
    }

    @Override
    public int getCombinedLight(final BlockPos pos, final int minLight)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getCombinedLight(pos, minLight);
    }

    @Override
    public IFluidState getFluidState(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getFluidState(pos);
    }

    @Override
    public int getRedstonePower(final BlockPos pos, final Direction facing)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getRedstonePower(pos, facing);
    }

    @Override
    public int getRedstonePowerFromNeighbors(final BlockPos pos)
    {
        return this.getWrapped().getRedstonePowerFromNeighbors(pos);
    }

    @Override
    public int getStrongPower(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getStrongPower(pos);
    }

    @Override
    public int getStrongPower(final BlockPos pos, final Direction direction)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getStrongPower(pos, direction);
    }

    @Override
    public TileEntity getTileEntity(final BlockPos pos)
    {
        final TileEntity tile = this.getTile(pos);
        if (tile == null) return this.world.getTileEntity(pos);
        return tile;
    }

    @Override
    public World getWorld()
    {
        return this.getWrapped().getWorld();
    }

    @Override
    public WorldBorder getWorldBorder()
    {
        if (this.getWrapped() == null) return super.getWorldBorder();
        return this.getWrapped().getWorldBorder();
    }

    @Override
    public ServerWorld getWrapped()
    {
        return this.world;
    }

    @Override
    public boolean hasBlockState(final BlockPos p_217375_1_, final Predicate<BlockState> p_217375_2_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().hasBlockState(p_217375_1_, p_217375_2_);
    }

    @Override
    public boolean isAirBlock(final BlockPos pos)
    {
        return this.getWrapped().isAirBlock(pos);
    }

    @Override
    public boolean isBlockModifiable(final PlayerEntity player, final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isBlockModifiable(player, pos);
    }

    @Override
    public boolean isBlockPowered(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isBlockPowered(pos);
    }

    @Override
    public boolean isBlockPresent(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isBlockPresent(pos);
    }

    @Override
    public boolean isCollisionBoxesEmpty(final Entity entityIn, final AxisAlignedBB aabb)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isCollisionBoxesEmpty(entityIn, aabb);
    }

    @Override
    public boolean isCollisionBoxesEmpty(final Entity entityIn, final AxisAlignedBB aabb,
            final Set<Entity> entitiesToIgnore)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isCollisionBoxesEmpty(entityIn, aabb, entitiesToIgnore);
    }

    @Override
    public boolean isMaterialInBB(final AxisAlignedBB bb, final Material materialIn)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isMaterialInBB(bb, materialIn);
    }

    @Override
    public boolean isRemote()
    {
        // TO DO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSidePowered(final BlockPos pos, final Direction side)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isSidePowered(pos, side);
    }

    @Override
    public void neighborChanged(final BlockPos pos, final Block blockIn, final BlockPos fromPos)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().neighborChanged(pos, blockIn, fromPos);
    }

    @Override
    public void notifyBlockUpdate(final BlockPos pos, final BlockState oldState, final BlockState newState,
            final int flags)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().notifyBlockUpdate(pos, oldState, newState, flags);
    }

    @Override
    public void notifyNeighbors(final BlockPos pos, final Block blockIn)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().notifyNeighbors(pos, blockIn);
    }

    @Override
    public void notifyNeighborsOfStateChange(final BlockPos pos, final Block blockIn)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().notifyNeighborsOfStateChange(pos, blockIn);
    }

    @Override
    public void notifyNeighborsOfStateExcept(final BlockPos pos, final Block blockType, final Direction skipSide)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().notifyNeighborsOfStateExcept(pos, blockType, skipSide);
    }

    @Override
    public BlockRayTraceResult rayTraceBlocks(final RayTraceContext p_217299_1_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().rayTraceBlocks(p_217299_1_);
    }

    @Override
    public boolean removeBlock(final BlockPos p_217377_1_, final boolean p_217377_2_)
    {
        return this.getWrapped().removeBlock(p_217377_1_, p_217377_2_);
    }

    @Override
    public void removeTileEntity(final BlockPos pos)
    {
        this.getWrapped().removeTileEntity(pos);
    }

    @Override
    public void sendBlockBreakProgress(final int breakerId, final BlockPos pos, final int progress)
    {
        this.getWrapped().sendBlockBreakProgress(breakerId, pos, progress);
    }

    @Override
    public void sendPacketToServer(final IPacket<?> packetIn)
    {
        this.getWrapped().sendPacketToServer(packetIn);
    }

    @Override
    public void setBlockEntity(final IBlockEntity mob)
    {
        IBlockEntityWorld.super.setBlockEntity(mob);
        this.mob = mob;
    }

    @Override
    public boolean setBlockState(final BlockPos pos, final BlockState state)
    {
        return this.getWrapped().setBlockState(pos, state);
    }

    /**
     * Sets the block state at a given location. Flag 1 will cause a block
     * update. Flag 2 will send the change to clients (you almost always want
     * this). Flag 4 prevents the block from being re-rendered, if this is a
     * client world. Flags can be added together.
     */
    @Override
    public boolean setBlockState(final BlockPos pos, final BlockState newState, final int flags)
    {
        if (this.setBlock(pos, newState)) return true;
        else return this.world.setBlockState(pos, newState, flags);
    }

    @Override
    public void setTileEntity(final BlockPos pos, @Nullable final TileEntity tileEntityIn)
    {
        if (this.setTile(pos, tileEntityIn)) return;
        this.getWrapped().setTileEntity(pos, tileEntityIn);
    }

    @Override
    public void updateComparatorOutputLevel(final BlockPos pos, final Block blockIn)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().updateComparatorOutputLevel(pos, blockIn);
    }

    @Override
    public void updateEntity(final Entity p_217479_1_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().updateEntity(p_217479_1_);
    }
}