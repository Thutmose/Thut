package thut.api.entity.blockentity.world;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import thut.api.entity.blockentity.IBlockEntity;

public class WorldEntity extends World implements IBlockEntityWorld
{

    final World    world;
    IBlockEntity   mob;
    public boolean creating;

    public WorldEntity(final World world)
    {
        super(world.getWorldInfo(), world.dimension.getType(), (w, d) -> new BlockEntityChunkProvider((WorldEntity) w),
                world.getProfiler(), world.isRemote);
        this.world = world;
    }

    @Override
    public IBlockEntity getBlockEntity()
    {
        return this.mob;
    }

    @Override
    public boolean setBlockState(final BlockPos pos, final BlockState newState, final int flags)
    {
        return this.setBlock(pos, newState);
    }

    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        final BlockState state = this.getBlock(pos);
        if (state == null) return this.world.getBlockState(pos);
        return state;
    }

    @Override
    public TileEntity getTileEntity(final BlockPos pos)
    {
        final TileEntity tile = this.getTile(pos);
        if (tile == null) return this.world.getTileEntity(pos);
        return tile;
    }

    @Override
    public void setBlockEntity(final IBlockEntity mob)
    {
        IBlockEntityWorld.super.setBlockEntity(mob);
        this.mob = mob;
    }

    @Override
    public IFluidState getFluidState(final BlockPos pos)
    {
        return this.world.getFluidState(pos);
    }

    @Override
    public ITickList<Block> getPendingBlockTicks()
    {
        return this.world.getPendingBlockTicks();
    }

    @Override
    public ITickList<Fluid> getPendingFluidTicks()
    {
        return this.world.getPendingFluidTicks();
    }

    @Override
    public void playEvent(final PlayerEntity player, final int type, final BlockPos pos, final int data)
    {

    }

    @Override
    public List<? extends PlayerEntity> getPlayers()
    {
        return this.world.getPlayers();
    }

    @Override
    public void notifyBlockUpdate(final BlockPos pos, final BlockState oldState, final BlockState newState,
            final int flags)
    {

    }

    @Override
    public void playSound(final PlayerEntity player, final double x, final double y, final double z,
            final SoundEvent soundIn, final SoundCategory category, final float volume, final float pitch)
    {

    }

    @Override
    public void playMovingSound(final PlayerEntity p_217384_1_, final Entity p_217384_2_, final SoundEvent p_217384_3_,
            final SoundCategory p_217384_4_, final float p_217384_5_, final float p_217384_6_)
    {

    }

    @Override
    public Entity getEntityByID(final int id)
    {
        return this.world.getEntityByID(id);
    }

    @Override
    public MapData getMapData(final String mapName)
    {
        return this.world.getMapData(mapName);
    }

    @Override
    public void registerMapData(final MapData mapDataIn)
    {
    }

    @Override
    public int getNextMapId()
    {
        return 0;
    }

    @Override
    public void sendBlockBreakProgress(final int breakerId, final BlockPos pos, final int progress)
    {

    }

    @Override
    public Scoreboard getScoreboard()
    {
        return this.world.getScoreboard();
    }

    @Override
    public RecipeManager getRecipeManager()
    {
        return this.world.getRecipeManager();
    }

    @Override
    public NetworkTagManager getTags()
    {
        return this.world.getTags();
    }

}