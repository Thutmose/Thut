package thut.api.entity.blockentity.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.UpgradeData;
import thut.api.entity.blockentity.IBlockEntity;

public class EntityChunk extends Chunk
{
    public static class EntityChunkPrimer extends ChunkPrimer
    {

        public EntityChunkPrimer(final ChunkPos pos)
        {
            super(pos, new UpgradeData(new CompoundNBT()));
        }

    }

    IBlockEntityWorld worldE;

    public EntityChunk(final IBlockEntityWorld worldIn_, final ChunkPos pos)
    {
        super((World) worldIn_, new EntityChunkPrimer(pos));
        this.worldE = worldIn_;
    }

    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        if (!this.worldE.inBounds(pos)) return Blocks.AIR.getDefaultState();
        final IBlockEntity mob = this.worldE.getBlockEntity();
        final Entity entity = (Entity) mob;
        final int i = pos.getX() - MathHelper.floor(entity.posX + mob.getMin().getX());
        final int j = pos.getY() - MathHelper.floor(entity.posY + mob.getMin().getY());
        final int k = pos.getZ() - MathHelper.floor(entity.posZ + mob.getMin().getZ());
        return mob.getBlocks()[i][j][k];
    }

    @Override
    public BlockState setBlockState(final BlockPos pos, final BlockState state, final boolean isMoving)
    {
        if (!this.worldE.inBounds(pos)) return Blocks.AIR.getDefaultState();
        final IBlockEntity mob = this.worldE.getBlockEntity();
        final Entity entity = (Entity) mob;
        final int i = pos.getX() - MathHelper.floor(entity.posX + mob.getMin().getX());
        final int j = pos.getY() - MathHelper.floor(entity.posY + mob.getMin().getY());
        final int k = pos.getZ() - MathHelper.floor(entity.posZ + mob.getMin().getZ());
        mob.getBlocks()[i][j][k] = state;
        return state;
    }

    @Override
    public void addTileEntity(final BlockPos pos, final TileEntity tile)
    {
        if (!this.worldE.inBounds(pos)) return;
        final IBlockEntity mob = this.worldE.getBlockEntity();
        final Entity entity = (Entity) mob;
        final int i = pos.getX() - MathHelper.floor(entity.posX + mob.getMin().getX());
        final int j = pos.getY() - MathHelper.floor(entity.posY + mob.getMin().getY());
        final int k = pos.getZ() - MathHelper.floor(entity.posZ + mob.getMin().getZ());
        mob.getTiles()[i][j][k] = tile;
        if (tile != null)
        {
            tile.setWorld((World) this.worldE);
            final boolean invalid = tile.isRemoved();
            if (!invalid) tile.remove();
            tile.setPos(pos.toImmutable());
            tile.validate();
        }
        return;
    }

    @Override
    public TileEntity getTileEntity(final BlockPos pos)
    {
        return super.getTileEntity(pos);
    }

}
