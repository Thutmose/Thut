package thut.api.terrain;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

public class CapabilityTerrain
{
    @CapabilityInject(ITerrainProvider.class)
    public static final Capability<ITerrainProvider> TERRAIN_CAP = null;

    public static interface ITerrainProvider
    {
        TerrainSegment getTerrainSegement(BlockPos blockLocation);

        void setTerrainSegment(TerrainSegment segment, int chunkY);

        TerrainSegment getTerrainSegment(int chunkY);

        BlockPos getChunkPos();
    }

    public static class DefaultProvider
            implements ITerrainProvider, ICapabilityProvider, INBTSerializable<CompoundNBT>
    {
        private BlockPos         pos;
        private final Chunk      chunk;
        private TerrainSegment[] segments = new TerrainSegment[16];

        public DefaultProvider()
        {
            this.chunk = null;
        }

        public DefaultProvider(Chunk chunk)
        {
            this.chunk = chunk;
        }

        @Override
        public TerrainSegment getTerrainSegement(BlockPos blockLocation)
        {
            int chunkY = (blockLocation.getY() / 16) & 15;
            TerrainSegment segment = getTerrainSegment(chunkY);
            segment.getCentre().addTo(0, 256 * (blockLocation.getY() / 256), 0);
            return segment;
        }

        @Override
        public void setTerrainSegment(TerrainSegment segment, int chunkY)
        {
            chunkY &= 15;
            segments[chunkY] = segment;
        }

        @Override
        public TerrainSegment getTerrainSegment(int chunkY)
        {
            chunkY &= 15;
            TerrainSegment ret = segments[chunkY];
            if (ret == null)
            {
                ret = segments[chunkY] = new TerrainSegment(getChunkPos().getX(), chunkY, getChunkPos().getZ());
                ret.chunk = chunk;
            }
            return ret;
        }

        @Override
        public BlockPos getChunkPos()
        {
            if (pos == null)
            {
                pos = new BlockPos(chunk.x, 0, chunk.z);
            }
            return pos;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, Direction facing)
        {
            return capability == CapabilityTerrain.TERRAIN_CAP;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(Capability<T> capability, Direction facing)
        {
            if (hasCapability(capability, facing)) return (T) this;
            return null;
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            CompoundNBT nbt = new CompoundNBT();
            for (int i = 0; i < 16; i++)
            {
                TerrainSegment t = this.getTerrainSegment(i);
                if (t == null) continue;
                t.checkToSave();
                if (!t.toSave)
                {
                    continue;
                }
                CompoundNBT terrainTag = new CompoundNBT();
                t.saveToNBT(terrainTag);
                nbt.setTag("" + i, terrainTag);
            }
            ListNBT biomeList = new ListNBT();
            for (BiomeType t : BiomeType.values())
            {
                CompoundNBT tag = new CompoundNBT();
                tag.putString("name", t.name);
                tag.setInteger("id", t.getType());
                biomeList.appendTag(tag);
            }
            nbt.setTag("ids", biomeList);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt)
        {
            BlockPos pos = this.getChunkPos();
            int x = pos.getX();
            int z = pos.getZ();
            Map<Integer, Integer> idReplacements = Maps.newHashMap();
            ListNBT tags = (ListNBT) nbt.getTag("ids");
            for (int i = 0; i < tags.size(); i++)
            {
                CompoundNBT tag = tags.getCompound(i);
                String name = tag.getString("name");
                int id = tag.getInteger("id");
                BiomeType type = BiomeType.getBiome(name, false);
                if (type.getType() != id)
                {
                    idReplacements.put(id, type.getType());
                }
            }
            boolean hasReplacements = !idReplacements.isEmpty();
            for (int i = 0; i < 16; i++)
            {
                CompoundNBT terrainTag = null;
                try
                {
                    terrainTag = nbt.getCompound(i + "");
                }
                catch (Exception e)
                {

                }
                TerrainSegment t = null;
                if (terrainTag != null && !terrainTag.hasNoTags() && !TerrainSegment.noLoad)
                {
                    t = new TerrainSegment(x, i, z);
                    if (hasReplacements) t.idReplacements = idReplacements;
                    TerrainSegment.readFromNBT(t, terrainTag);
                    this.setTerrainSegment(t, i);
                    t.idReplacements = null;
                }
                if (t == null)
                {
                    t = new TerrainSegment(x, i, z);
                    this.setTerrainSegment(t, i);
                }
            }
        }

    }

    public static class Storage implements Capability.IStorage<ITerrainProvider>
    {

        @Override
        public INBT writeNBT(Capability<ITerrainProvider> capability, ITerrainProvider instance, Direction side)
        {
            if (instance instanceof DefaultProvider) return ((DefaultProvider) instance).serializeNBT();
            return null;
        }

        @Override
        public void readNBT(Capability<ITerrainProvider> capability, ITerrainProvider instance, Direction side,
                INBT base)
        {
            if (instance instanceof DefaultProvider && base instanceof CompoundNBT)
                ((DefaultProvider) instance).deserializeNBT((CompoundNBT) base);
        }
    }
}
