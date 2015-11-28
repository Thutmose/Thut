package dorfgen.worldgen;

import dorfgen.WorldGenerator;
import dorfgen.conversion.DorfMap.Region;
import dorfgen.conversion.Interpolator.CachedBicubicInterpolator;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.MapGenCaves;

public class MapGenUGRegions extends MapGenCaves {

	public CachedBicubicInterpolator	heightInterpolator	= new CachedBicubicInterpolator();
	
    protected void makeCaves(long seed, int chunkx, int chunkz, Block[] blocks, double x, double y, double z)
    {
        this.makeCave(seed, chunkx, chunkz, blocks, x, y, z, 1.0F + this.rand.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5D);
    }

    protected void makeCave(long seed, int chunkx, int chunkz, Block[] blocks, double x, double y, double z, float p_151541_12_, float p_151541_13_, float p_151541_14_, int p_151541_15_, int p_151541_16_, double p_151541_17_)
    {
    	if(true)return;//TODO improve this
//        double d4 = (double)(chunkx * 16 + 8);
//        double d5 = (double)(chunkz * 16 + 8);
//        float f3 = 0.0F;
//        float f4 = 0.0F;
//        Random random = new Random(seed);
//
//        if (p_151541_16_ <= 0)
//        {
//            int j1 = this.range * 16 - 16;
//            p_151541_16_ = j1 - random.nextInt(j1 / 4);
//        }
//
//        boolean flag2 = false;
//
//        if (p_151541_15_ == -1)
//        {
//            p_151541_15_ = p_151541_16_ / 2;
//            flag2 = true;
//        }
//
//        int k1 = random.nextInt(p_151541_16_ / 2) + p_151541_16_ / 4;
//
//        for (boolean flag = random.nextInt(6) == 0; p_151541_15_ < p_151541_16_; ++p_151541_15_)
//        {
//            double d6 = 1.5D + (double)(MathHelper.sin((float)p_151541_15_ * (float)Math.PI / (float)p_151541_16_) * p_151541_12_ * 1.0F);
//            double d7 = d6 * p_151541_17_;
//            float f5 = MathHelper.cos(p_151541_14_);
//            float f6 = MathHelper.sin(p_151541_14_);
//            x += (double)(MathHelper.cos(p_151541_13_) * f5);
//            y += (double)f6;
//            z += (double)(MathHelper.sin(p_151541_13_) * f5);
//
//            if (flag)
//            {
//                p_151541_14_ *= 0.92F;
//            }
//            else
//            {
//                p_151541_14_ *= 0.7F;
//            }
//
//            p_151541_14_ += f4 * 0.1F;
//            p_151541_13_ += f3 * 0.1F;
//            f4 *= 0.9F;
//            f3 *= 0.75F;
//            f4 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
//            f3 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
//
//            if (!flag2 && p_151541_15_ == k1 && p_151541_12_ > 1.0F && p_151541_16_ > 0)
//            {
//                this.makeCave(random.nextLong(), chunkx, chunkz, blocks, x, y, z, random.nextFloat() * 0.5F + 0.5F, p_151541_13_ - ((float)Math.PI / 2F), p_151541_14_ / 3.0F, p_151541_15_, p_151541_16_, 1.0D);
//                this.makeCave(random.nextLong(), chunkx, chunkz, blocks, x, y, z, random.nextFloat() * 0.5F + 0.5F, p_151541_13_ + ((float)Math.PI / 2F), p_151541_14_ / 3.0F, p_151541_15_, p_151541_16_, 1.0D);
//                return;
//            }
//
//            if (flag2 || random.nextInt(4) != 0)
//            {
//                double d8 = x - d4;
//                double d9 = z - d5;
//                double d10 = (double)(p_151541_16_ - p_151541_15_);
//                double d11 = (double)(p_151541_12_ + 2.0F + 16.0F);
//
//                if (d8 * d8 + d9 * d9 - d10 * d10 > d11 * d11)
//                {
//                    return;
//                }
//
//                if (x >= d4 - 16.0D - d6 * 2.0D && z >= d5 - 16.0D - d6 * 2.0D && x <= d4 + 16.0D + d6 * 2.0D && z <= d5 + 16.0D + d6 * 2.0D)
//                {
//                    int i4 = MathHelper.floor_double(x - d6) - chunkx * 16 - 1;
//                    int l1 = MathHelper.floor_double(x + d6) - chunkx * 16 + 1;
//                    int j4 = MathHelper.floor_double(y - d7) - 1;
//                    int i2 = MathHelper.floor_double(y + d7) + 1;
//                    int k4 = MathHelper.floor_double(z - d6) - chunkz * 16 - 1;
//                    int j2 = MathHelper.floor_double(z + d6) - chunkz * 16 + 1;
//
//                    if (i4 < 0)
//                    {
//                        i4 = 0;
//                    }
//
//                    if (l1 > 16)
//                    {
//                        l1 = 16;
//                    }
//
//                    if (j4 < 1)
//                    {
//                        j4 = 1;
//                    }
//
//                    if (i2 > 248)
//                    {
//                        i2 = 248;
//                    }
//
//                    if (k4 < 0)
//                    {
//                        k4 = 0;
//                    }
//
//                    if (j2 > 16)
//                    {
//                        j2 = 16;
//                    }
//
//                    boolean flag3 = false;
//                    int k2;
//                    int j3;
//
//                    for (k2 = i4; !flag3 && k2 < l1; ++k2)
//                    {
//                        for (int l2 = k4; !flag3 && l2 < j2; ++l2)
//                        {
//                            for (int i3 = i2 + 1; !flag3 && i3 >= j4 - 1; --i3)
//                            {
//                                j3 = (k2 * 16 + l2) * 256 + i3;
//
//                                if (i3 >= 0 && i3 < 256)
//                                {
//                                    Block block = blocks[j3];
//
//                                    if (isOceanBlock(blocks, j3, k2, i3, l2, chunkx, chunkz))
//                                    {
//                                        flag3 = true;
//                                    }
//
//                                    if (i3 != j4 - 1 && k2 != i4 && k2 != l1 - 1 && l2 != k4 && l2 != j2 - 1)
//                                    {
//                                        i3 = j4;
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    if (!flag3)
//                    {
//                        for (k2 = i4; k2 < l1; ++k2)
//                        {
//                            double d13 = ((double)(k2 + chunkx * 16) + 0.5D - x) / d6;
//
//                            for (j3 = k4; j3 < j2; ++j3)
//                            {
//                                double d14 = ((double)(j3 + chunkz * 16) + 0.5D - z) / d6;
//                                int k3 = (k2 * 16 + j3) * 256 + i2;
//                                boolean flag1 = false;
//
//                                if (d13 * d13 + d14 * d14 < 1.0D)
//                                {
//                                    for (int l3 = i2 - 1; l3 >= j4; --l3)
//                                    {
//                                        double d12 = ((double)l3 + 0.5D - y) / d7;
//
//                                        if (d12 > -0.7D && d13 * d13 + d12 * d12 + d14 * d14 < 1.0D)
//                                        {
//                                            Block block1 = blocks[k3];
//
//                                            if (isTopBlock(blocks, k3, k2, l3, j3, chunkx, chunkz))
//                                            {
//                                                flag1 = true;
//                                            }
//                                            digBlock(blocks, k3, k2, l3, j3, chunkx, chunkz, flag1);
//                                        }
//
//                                        --k3;
//                                    }
//                                }
//                            }
//                        }
//
//                        if (flag2)
//                        {
//                            break;
//                        }
//                    }
//                }
//            }
//        }
    }

    protected void func_151538_a(World world, int x, int z, int chunkX, int chunkZ, Block[] blocks)
    {
        int i1 = 0;
        boolean[] regions = new boolean[6];
        for(int i = 0; i<=5; i++)
        {
        	Region region = WorldGenerator.instance.dorfs.getUgRegionForCoords(x, i, z);
        	if(region!=null)
        	{
        		regions[i] = true;
        		i1++;
        	}
        }
        
        for (int j1 = 0; j1 < i1; ++j1)
        {
            int d0 = (x * 16 + this.rand.nextInt(16));
            double d1 =  -1;//(double)this.rand.nextInt(this.rand.nextInt(120) + 8);
            int d2 = (z * 16 + this.rand.nextInt(16));
            int k1 = 1;
            
            int h = 0;
            
        	int xAbs = d0 - WorldGenerator.shift.getX();
        	int zAbs =  d2 - WorldGenerator.shift.getZ();
        	if(xAbs >=0 && xAbs <WorldGenerator.instance.dorfs.elevationMap.length && zAbs >=0 && zAbs <WorldGenerator.instance.dorfs.elevationMap[0].length)
        	{
    	    	h = heightInterpolator.interpolateHeight(WorldGenerator.scale, xAbs, zAbs, WorldGenerator.instance.dorfs.elevationMap);
        	}
        	else
        	{
        		h = 64;//world.getGroundAboveSeaLevel(new BlockPos(d0, d2));//TODO
        	}
        	h /= 2;
            
            for(int i = 0; i<=5; i++)
            {
            	if(regions[i])
            	{
            		regions[i] = false;
            		d1 = h * (5 - i)/5d + 8;
            		break;
            	}
            }
            
            if(d1<0)
            	continue;

            if (this.rand.nextInt(4) == 0)
            {
                this.makeCaves(this.rand.nextLong(), chunkX, chunkZ, blocks, d0, d1, d2);
                k1 += this.rand.nextInt(4);
            }

            for (int l1 = 0; l1 < k1; ++l1)
            {
                float f = this.rand.nextFloat() * (float)Math.PI * 2.0F;
                float f1 = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float f2 = this.rand.nextFloat() * 2.0F + this.rand.nextFloat();

                if (this.rand.nextInt(10) == 0)
                {
                    f2 *= this.rand.nextFloat() * this.rand.nextFloat() * 3.0F + 1.0F;
                }

                this.makeCave(this.rand.nextLong(), chunkX, chunkZ, blocks, d0, d1, d2, f2, f, f1, 0, 0, 1.0D);
            }
        }
    }

    protected boolean isOceanBlock(Block[] data, int index, int x, int y, int z, int chunkX, int chunkZ)
    {
        return data[index] == Blocks.flowing_water || data[index] == Blocks.water;
    }

    //Exception biomes to make sure we generate like vanilla
//    private boolean isExceptionBiome(BiomeGenBase biome)
//    {
//        if (biome == BiomeGenBase.mushroomIsland) return true;
//        if (biome == BiomeGenBase.beach) return true;
//        if (biome == BiomeGenBase.desert) return true;
//        return false;
//    }

//    //Determine if the block at the specified location is the top block for the biome, we take into account
//    //Vanilla bugs to make sure that we generate the map the same way vanilla does.
//    private boolean isTopBlock(Block[] data, int index, int x, int y, int z, int chunkX, int chunkZ)
//    {
//    	int xAbs = x + chunkX * 16 - WorldGenerator.shift.getX();
//    	int zAbs =  z + chunkZ * 16 - WorldGenerator.shift.getZ();
//    	if(xAbs >=0 && xAbs <WorldGenerator.instance.dorfs.elevationMap.length && zAbs >=0 && zAbs <WorldGenerator.instance.dorfs.elevationMap[0].length)
//    	{
//	    	int h = heightInterpolator.interpolateHeight(WorldGenerator.scale, xAbs, zAbs, WorldGenerator.instance.dorfs.elevationMap);
//	    	return h <= y;
//    	}
//    	
//        BiomeGenBase biome = worldObj.getBiomeGenForCoords(new BlockPos(x + chunkX * 16, 0, z + chunkZ * 16));
//        return (isExceptionBiome(biome) ? data[index] == Blocks.grass : data[index] == biome.topBlock);
//    }
//
//    /**
//     * Digs out the current block, default implementation removes stone, filler, and top block
//     * Sets the block to lava if y is less then 10, and air other wise.
//     * If setting to air, it also checks to see if we've broken the surface and if so 
//     * tries to make the floor the biome's top block
//     * 
//     * @param data Block data array
//     * @param index Pre-calculated index into block data
//     * @param x local X position
//     * @param y local Y position
//     * @param z local Z position
//     * @param chunkX Chunk X position
//     * @param chunkZ Chunk Y position
//     * @param foundTop True if we've encountered the biome's top block. Ideally if we've broken the surface.
//     */
//    protected void digBlock(Block[] data, int index, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop)
//    {
//        BiomeGenBase biome = worldObj.getBiomeGenForCoords(x + chunkX * 16, z + chunkZ * 16);
//        Block top    = (isExceptionBiome(biome) ? Blocks.grass : biome.topBlock);
//        Block filler = (isExceptionBiome(biome) ? Blocks.dirt  : biome.fillerBlock);
//        Block block  = data[index];
//        Block grass = Blocks.stone;
//        
//        int h = 0;
//        int scale = WorldGenerator.scale;
//        int[][] map = WorldGenerator.instance.dorfs.elevationMap;
//		int x1 = x + chunkX * 16 - WorldGenerator.shift.getX();
//		int z1 = z + chunkZ * 16 - WorldGenerator.shift.getZ();
//        
//        if(x1>0&&z1>0&&x1/scale < map.length && z1/scale < map[0].length)
//        {
//        	h = map[x1/scale][z1/scale];
//        }
//        boolean ceiling = false;
//        for(int i = 1; i<=h-y; i++)
//        {
//        	if(index+i >= data.length)
//        	{
//        		System.out.println(y+" "+h+" "+index);
//        		ceiling = true;
//        		break;
//        	}
//        	if(data[index + i] != null)
//        	{
//        		ceiling = true;
//        		break;
//        	}
//        }
//        
//        if (block == Blocks.stone || block == filler || block == top || block==grass)
//        {
//            if (y < 10)
//            {
//                data[index] = Blocks.lava;
//            }
//            else
//            {
//                data[index] = null;
//
//                  if (foundTop && data[index - 1] == filler)
//                  {
//                      data[index - 1] = top;
//                  }
//                  else 
//                  if (ceiling && (data[index - 1] == Blocks.dirt || data[index-1] == Blocks.stone))
//                  {
//                      data[index - 1] = grass;
//                  }
//            }
//        }
//    }
}
