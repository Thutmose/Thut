package thut.essentials.land;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class LandChunk implements Comparable<LandChunk>
{

    public static LandChunk getChunkCoordFromWorldCoord(BlockPos pos, int dimension)
    {
        return getChunkCoordFromWorldCoord(pos.getX(), pos.getY(), pos.getZ(), dimension);
    }

    public static LandChunk getChunkCoordFromWorldCoord(int x, int y, int z, int dim)
    {
        int i = MathHelper.floor_double(x / 16.0D);
        int j = MathHelper.floor_double(y / 16.0D);
        int k = MathHelper.floor_double(z / 16.0D);
        return new LandChunk(i, j, k, dim);
    }

    public int x;
    public int y;
    public int z;
    public int dim;

    public LandChunk(BlockPos pos, int dimension)
    {
        this(pos.getX(), pos.getY(), pos.getZ(), dimension);
    }

    public LandChunk(int x, int y, int z, int dim)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof LandChunk))
        {
            return false;
        }
        else
        {
            LandChunk BlockPos = (LandChunk) obj;
            return x == BlockPos.x && y == BlockPos.y && this.z == BlockPos.z && this.dim == BlockPos.dim;
        }
    }

    @Override
    public int hashCode()
    {
        return x + z << 8 + y << 16 + this.dim << 24;
    }

    public int compareTo(LandChunk p_compareTo_1_)
    {
        return y == p_compareTo_1_.y
                ? (this.z == p_compareTo_1_.z ? x - p_compareTo_1_.x
                        : this.dim == p_compareTo_1_.dim ? this.z - p_compareTo_1_.z : this.dim - p_compareTo_1_.dim)
                : this.y - p_compareTo_1_.y;
    }
}
