package thut.api.entity.blockentity.world.client;

import java.util.List;

import javax.annotation.Nullable;

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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.api.entity.blockentity.IBlockEntity;

public class ClientWorldEntity extends World implements IBlockEntityWorld<World>
{
    public static ClientWorldEntity instance;

    final World    world;
    IBlockEntity   mob;
    public boolean creating;

    public ClientWorldEntity(World world)
    {
        super(world.getWorldInfo(), world.getDimension().getType(), (worldIn,
                dimensionIn) -> new BlockEntityChunkProvider((ClientWorldEntity) worldIn), world.getProfiler(),
                world.isRemote);
        this.world = world;
    }

    @Override
    public void func_217399_a(MapData p_217399_1_)
    {
        this.world.func_217399_a(p_217399_1_);
    }

    @Override
    public MapData func_217406_a(String p_217406_1_)
    {
        return this.world.func_217406_a(p_217406_1_);
    }

    @Override
    public Biome getBiome(BlockPos pos)
    {
        return this.world.getBiome(pos);
    }

    @Override
    public IBlockEntity getBlockEntity()
    {
        return this.mob;
    }

    @Override
    public BlockState getBlockState(BlockPos pos)
    {
        final BlockState state = this.getBlock(pos);
        if (state == null) return this.world.getBlockState(pos);
        return state;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getCombinedLight(BlockPos pos, int minLight)
    {
        return this.world.getCombinedLight(pos, minLight);
    }

    @Override
    public Entity getEntityByID(int id)
    {
        return this.world.getEntityByID(id);
    }

    @Override
    public int getNextMapId()
    {
        return this.world.getNextMapId();
    }

    @Override
    public ITickList<Block> getPendingBlockTicks()
    {
        return null;
    }

    @Override
    public ITickList<Fluid> getPendingFluidTicks()
    {
        return null;
    }

    @Override
    public List<? extends PlayerEntity> getPlayers()
    {
        return this.world.getPlayers();
    }

    @Override
    public RecipeManager getRecipeManager()
    {
        return this.world.getRecipeManager();
    }

    @Override
    public Scoreboard getScoreboard()
    {
        return this.world.getScoreboard();
    }

    @Override
    public int getStrongPower(BlockPos pos, Direction direction)
    {
        return this.world.getStrongPower(pos, direction);
    }

    @Override
    public NetworkTagManager getTags()
    {
        return this.world.getTags();
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos)
    {
        final TileEntity tile = this.getTile(pos);
        if (tile == null) return this.world.getTileEntity(pos);
        return tile;
    }

    @Override
    public WorldType getWorldType()
    {
        return this.world.getWorldType();
    }

    @Override
    public World getWrapped()
    {
        return this.world;
    }

    @Override
    public boolean isAirBlock(BlockPos pos)
    {
        final BlockState state = this.getBlockState(pos);
        return state.getBlock().isAir(state, this, pos);
    }

    @Override
    public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags)
    {
        this.world.notifyBlockUpdate(pos, oldState, newState, flags);
    }

    @Override
    public void playEvent(PlayerEntity p_217378_1_, int p_217378_2_, BlockPos p_217378_3_, int p_217378_4_)
    {
        this.world.playEvent(p_217378_1_, p_217378_2_, p_217378_3_, p_217378_4_);
    }

    @Override
    public void playMovingSound(PlayerEntity p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_,
            SoundCategory p_217384_4_, float p_217384_5_, float p_217384_6_)
    {
        this.world.playMovingSound(p_217384_1_, p_217384_2_, p_217384_3_, p_217384_4_, p_217384_5_, p_217384_6_);
    }

    @Override
    public void playSound(PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category,
            float volume, float pitch)
    {
        this.world.playSound(player, x, y, z, soundIn, category, volume, pitch);
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress)
    {
        this.world.sendBlockBreakProgress(breakerId, pos, progress);
    }

    @Override
    public void setBlockEntity(IBlockEntity mob)
    {
        IBlockEntityWorld.super.setBlockEntity(mob);
        this.mob = mob;
    }

    /**
     * Sets the block state at a given location. Flag 1 will cause a block
     * update. Flag 2 will send the change to clients (you almost always want
     * this). Flag 4 prevents the block from being re-rendered, if this is a
     * client world. Flags can be added together.
     */
    @Override
    public boolean setBlockState(BlockPos pos, BlockState newState, int flags)
    {
        if (this.setBlock(pos, newState)) return true;
        else return this.world.setBlockState(pos, newState, flags);
    }

    @Override
    public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntityIn)
    {
        if (this.setTile(pos, tileEntityIn)) return;
        this.getWrapped().setTileEntity(pos, tileEntityIn);
    }

}