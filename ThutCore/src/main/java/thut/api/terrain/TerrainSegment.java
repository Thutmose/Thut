package thut.api.terrain;

import static thut.api.terrain.BiomeDatabase.contains;
import static thut.api.terrain.BiomeType.LAKE;
import static thut.api.terrain.BiomeType.VILLAGE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import thut.api.maths.Vector3;

public class TerrainSegment
{

    public static interface ISubBiomeChecker
    {
        /** This should return -1 if it is not a relevant biome for this biome
         * checker.
         * 
         * @param world
         * @param v
         * @param segment
         * @param chunk
         * @param caveAdjusted
         * @return */
        int getSubBiome(World world, Vector3 v, TerrainSegment segment, Chunk chunk, boolean caveAdjusted);
    }

    public static interface ITerrainEffect
    {
        /** Called when the terrain effect is assigned to the terrain segment
         * 
         * @param x
         *            chunkX of terrainsegment
         * @param y
         *            chunkY of terrainsegment
         * @param z
         *            chunkZ of terrainsegement */
        void bindToTerrain(int x, int y, int z);

        void doEffect(EntityLivingBase entity, boolean firstEntry);

        // Does not currently work TODO make this work
        void readFromNBT(NBTTagCompound nbt);

        // Does not currently work TODO make this work
        void writeToNBT(NBTTagCompound nbt);
    }

    public static final int              GRIDSIZE       = 4;
    public static ISubBiomeChecker       defaultChecker = new ISubBiomeChecker()
                                                        {
                                                            @Override
                                                            public int getSubBiome(World world, Vector3 v,
                                                                    TerrainSegment segment, Chunk chunk,
                                                                    boolean caveAdjusted)
                                                            {
                                                                if (caveAdjusted)
                                                                {
                                                                    if (world.provider.doesWaterVaporize()) return -1;
                                                                    boolean sky = false;
                                                                    Vector3 temp1 = Vector3.getNewVector();
                                                                    int x0 = segment.chunkX * 16,
                                                                            y0 = segment.chunkY * 16,
                                                                            z0 = segment.chunkZ * 16;
                                                                    int dx = ((v.intX() - x0) / GRIDSIZE) * GRIDSIZE;
                                                                    int dy = ((v.intY() - y0) / GRIDSIZE) * GRIDSIZE;
                                                                    int dz = ((v.intZ() - z0) / GRIDSIZE) * GRIDSIZE;
                                                                    int x1 = x0 + dx, y1 = y0 + dy, z1 = z0 + dz;
                                                                    outer:
                                                                    for (int i = x1; i < x1 + GRIDSIZE; i++)
                                                                        for (int j = y1; j < y1 + GRIDSIZE; j++)
                                                                            for (int k = z1; k < z1 + GRIDSIZE; k++)
                                                                            {
                                                                                temp1.set(i, j, k);
                                                                                if (segment.isInTerrainSegment(temp1.x,
                                                                                        temp1.y, temp1.z))
                                                                                {
                                                                                    double y = temp1.getMaxY(world);
                                                                                    sky = y <= temp1.y;
                                                                                }
                                                                                if (sky) break outer;
                                                                            }
                                                                    if (sky) return -1;

                                                                    if (!sky && count(world, Blocks.WATER, v, 1) > 2)
                                                                        return BiomeType.CAVE_WATER.getType();
                                                                    else if (!sky) return BiomeType.CAVE.getType();
                                                                }
                                                                else
                                                                {
                                                                    int biome = 0;

                                                                    Biome b = v.getBiome(chunk,
                                                                            world.getBiomeProvider());
                                                                    biome = BiomeDatabase.getBiomeType(b);

                                                                    boolean notLake = BiomeDatabase.contains(b,
                                                                            Type.OCEAN)
                                                                            || BiomeDatabase.contains(b, Type.SWAMP)
                                                                            || BiomeDatabase.contains(b, Type.RIVER)
                                                                            || BiomeDatabase.contains(b, Type.WATER)
                                                                            || BiomeDatabase.contains(b, Type.BEACH);

                                                                    int water = v.blockCount2(world, Blocks.WATER, 3);
                                                                    if (water > 4)
                                                                    {
                                                                        if (!notLake)
                                                                        {
                                                                            biome = LAKE.getType();
                                                                        }
                                                                        return biome;
                                                                    }
                                                                    if (world.villageCollectionObj != null)
                                                                    {
                                                                        Village village = world.villageCollectionObj
                                                                                .getNearestVillage(new BlockPos(
                                                                                        MathHelper.floor_double(v.x),
                                                                                        MathHelper.floor_double(v.y),
                                                                                        MathHelper.floor_double(v.z)),
                                                                                        2);
                                                                        if (village != null)
                                                                        {
                                                                            biome = VILLAGE.getType();
                                                                        }
                                                                    }

                                                                    return biome;
                                                                }
                                                                return 0;
                                                            }

                                                        };
    public static List<ISubBiomeChecker> biomeCheckers  = Lists.newArrayList();
    public static boolean                noLoad         = false;
    static Map<Integer, Integer>         idReplacements = Maps.newHashMap();

    public static int count(World world, Block b, Vector3 v, int range)
    {
        Vector3 temp = Vector3.getNewVector();
        temp.set(v);
        int ret = 0;
        for (int i = -range; i <= range; i++)
            for (int j = -range; j <= range; j++)
                for (int k = -range; k <= range; k++)
                {

                    boolean bool = true;
                    int i1 = MathHelper.floor_double((v.intX() + i)) >> 4;
                    // int j1 = MathHelper.floor_double(v.intY()+i / 16.0D);
                    int k1 = MathHelper.floor_double((v.intZ() + i)) >> 4;

                    bool = i1 == v.intX() >> 4 && k1 == v.intZ() >> 4;// &&j==chunkY;

                    if (bool)
                    {
                        temp.set(v).addTo(i, j, k);
                        if (temp.getBlock(world) == b || (b == null && temp.getBlock(world) == null))
                        {
                            ret++;
                        }
                    }
                }
        return ret;
    }

    static Biome getBiome(Type not, Type... types)
    {
        Biome ret = null;
        biomes:
        for (ResourceLocation key : Biome.REGISTRY.getKeys())
        {
            Biome b = Biome.REGISTRY.getObject(key);
            if (b == null) continue;
            if (not != null && contains(b, not)) continue;
            for (Type t : types)
            {
                if (!BiomeDatabase.contains(b, t)) continue biomes;
            }
            ret = b;
        }
        return ret;
    }

    public static boolean isInTerrainColumn(Vector3 t, Vector3 point)
    {
        boolean ret = true;
        int i = MathHelper.floor_double(point.intX() / 16.0D);
        int k = MathHelper.floor_double(point.intZ() / 16.0D);

        ret = i == t.intX() && k == t.intZ();
        return ret;
    }

    public static void readFromNBT(TerrainSegment t, NBTTagCompound nbt)
    {
        if (noLoad) return;
        int[] biomes = nbt.getIntArray("biomes");
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) if (nbt.hasKey("ids"))
        {
            idReplacements.clear();
            NBTTagList tags = (NBTTagList) nbt.getTag("ids");
            for (int i = 0; i < tags.tagCount(); i++)
            {
                NBTTagCompound tag = tags.getCompoundTagAt(i);
                String name = tag.getString("name");
                int id = tag.getInteger("id");
                BiomeType type = BiomeType.getBiome(name, false);
                if (type.getType() != id)
                {
                    idReplacements.put(id, type.getType());
                }
            }
            boolean replacements = false;
            for (int i = 0; i < biomes.length; i++)
            {
                if (idReplacements.containsKey(biomes[i]))
                {
                    biomes[i] = idReplacements.get(biomes[i]);
                    replacements = true;
                }
            }
            if (replacements)
                System.out.println("Replacement subbiomes found for " + t.chunkX + " " + t.chunkY + " " + t.chunkZ);
        }
        t.toSave = nbt.getBoolean("toSave");
        t.init = false;
        t.setBiome(biomes);
    }

    public final int                chunkX;

    public final int                chunkY;

    public final int                chunkZ;

    public final BlockPos           pos;

    private Chunk                   chunk;

    public boolean                  toSave  = false;

    public boolean                  isSky   = false;

    public boolean                  init    = true;

    Vector3                         temp    = Vector3.getNewVector();

    Vector3                         temp1   = Vector3.getNewVector();

    Vector3                         mid     = Vector3.getNewVector();

    int[]                           biomes  = new int[GRIDSIZE * GRIDSIZE * GRIDSIZE];

    HashMap<String, ITerrainEffect> effects = new HashMap<String, ITerrainEffect>();

    public TerrainSegment(int x, int y, int z)
    {
        chunkX = x;
        chunkY = y;
        chunkZ = z;
        pos = new BlockPos(x, y, z);
        mid.set(this.chunkX * 16 + 8, this.chunkY * 16 + 8, this.chunkZ * 16 + 8);
    }

    public void addEffect(ITerrainEffect effect, String name)
    {
        effect.bindToTerrain(chunkX, chunkY, chunkZ);
        effects.put(name, effect);
    }

    public int adjustedCaveBiome(World world, Vector3 v)
    {
        return getBiome(world, v, true);
    }

    public int adjustedNonCaveBiome(World world, Vector3 v)
    {
        return getBiome(world, v, false);
    }

    void checkToSave()
    {
        int subCount = biomes.length;
        for (int i = 0; i < subCount; i++)
        {
            int temp1 = biomes[i];
            if (temp1 > 255 && temp1 != BiomeType.SKY.getType())
            {
                toSave = true;
                return;
            }
        }
        toSave = false;
    }

    /** Applies all of the effects onto the mob
     * 
     * @param hungrymob */
    public void doEffects(String effect, EntityLivingBase entity, boolean firstEntry)
    {
        if (effects.containsKey(effect))
        {
            effects.get(effect).doEffect(entity, firstEntry);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        boolean ret = false;
        if (o instanceof TerrainSegment)
        {
            ret = ((TerrainSegment) o).chunkX == chunkX && ((TerrainSegment) o).chunkY == chunkY
                    && ((TerrainSegment) o).chunkZ == chunkZ;
        }

        return ret;
    }

    public double getAverageSlope(World world, Vector3 point, int range)
    {
        double slope = 0;

        double prevY = point.getMaxY(world);

        double dy = 0;
        double dz = 0;
        temp1.set(temp);
        temp.set(point);
        int count = 0;
        for (int i = -range; i <= range; i++)
        {
            dz = 0;
            for (int j = -range; j <= range; j++)
            {
                if (isInTerrainColumn(point, temp.addTo(i, 0, j)))
                    dy += Math.abs((point.getMaxY(world, point.intX() + i, point.intZ() + j) - prevY));
                dz++;
                count++;
                temp.set(point);
            }
            slope += (dy / dz);
        }
        temp.set(temp1);

        return slope / count;
    }

    public int getBiome(int x, int y, int z)
    {
        int ret = 0;
        int relX = (x & 15) / GRIDSIZE, relY = (y & 15) / GRIDSIZE, relZ = (z & 15) / GRIDSIZE;

        if (relX < 4 && relY < 4 && relZ < 4)
        {
            ret = biomes[relX + GRIDSIZE * relY + GRIDSIZE * GRIDSIZE * relZ];

        }
        if (ret > 255) toSave = true;

        return ret;
    }

    public int getBiome(Vector3 v)
    {
        return getBiome(v.intX(), v.intY(), v.intZ());
    }

    private int getBiome(World world, Vector3 v, boolean caveAdjust)
    {
        if (chunk == null || chunk.xPosition != chunkX || chunk.zPosition != chunkZ)
            chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        if (chunk == null)
        {
            Thread.dumpStack();
            return 0;
        }
        if (!biomeCheckers.isEmpty())
        {
            for (ISubBiomeChecker checker : biomeCheckers)
            {
                int biome = checker.getSubBiome(world, v, this, chunk, caveAdjust);
                if (biome != -1) return biome;
            }
        }
        return defaultChecker.getSubBiome(world, v, this, chunk, caveAdjust);
    }

    public Vector3 getCentre()
    {
        return mid;
    }

    public BlockPos getChunkCoords()
    {
        return pos;
    }

    public ITerrainEffect geTerrainEffect(String name)
    {
        return effects.get(name);
    }

    public void initBiomes(World world)
    {
        if (init)
        {
            refresh(world);
            init = false;
        }
    }

    public boolean isInTerrainSegment(double x, double y, double z)
    {
        boolean ret = true;
        int i = MathHelper.floor_double(x / 16.0D);
        int j = MathHelper.floor_double(y / 16.0D);
        int k = MathHelper.floor_double(z / 16.0D);

        ret = i == chunkX && k == chunkZ && j == chunkY;
        return ret;
    }

    public void refresh(World world)
    {
        long time = System.nanoTime();
        chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        for (int i = 0; i < GRIDSIZE; i++)
            for (int j = 0; j < GRIDSIZE; j++)
                for (int k = 0; k < GRIDSIZE; k++)
                {
                    temp.set(chunkX * 16 + i * 16 / GRIDSIZE, chunkY * 16 + j * 16 / GRIDSIZE,
                            chunkZ * 16 + k * 16 / GRIDSIZE);
                    int biome = adjustedCaveBiome(world, temp);
                    int biome2 = adjustedNonCaveBiome(world, temp);
                    if (biome > 255 || biome2 > 255) toSave = true;
                    if (biome == -1) biome = biome2;
                    biomes[i + GRIDSIZE * j + GRIDSIZE * GRIDSIZE * k] = biome;
                }
        double dt = (System.nanoTime() - time) / 10e9;
        if (dt > 0.01) System.out.println("subBiome refresh took " + dt);
    }

    public void saveToNBT(NBTTagCompound nbt)
    {
        if (!(toSave)) return;
        nbt.setIntArray("biomes", biomes);
        nbt.setInteger("x", chunkX);
        nbt.setInteger("y", chunkY);
        nbt.setInteger("z", chunkZ);
        nbt.setBoolean("toSave", toSave);
        NBTTagList biomeList = new NBTTagList();
        for (BiomeType t : BiomeType.values())
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("name", t.name);
            tag.setInteger("id", t.getType());
            biomeList.appendTag(tag);
        }
        nbt.setTag("ids", biomeList);
    }

    public void setBiome(BlockPos p, int type)
    {
        this.setBiome(p.getX(), p.getY(), p.getZ(), type);
    }

    public void setBiome(int x, int y, int z, int biome)
    {
        int relX = (x & 15) / GRIDSIZE, relY = (y & 15) / GRIDSIZE, relZ = (z & 15) / GRIDSIZE;
        biomes[relX + GRIDSIZE * relY + GRIDSIZE * GRIDSIZE * relZ] = biome;
        if (biome > 255) toSave = true;

    }

    public int getBiomeLocal(int x, int y, int z)
    {
        int relX = x % GRIDSIZE;
        int relY = y % GRIDSIZE;
        int relZ = z % GRIDSIZE;
        return biomes[relX + GRIDSIZE * relY + GRIDSIZE * GRIDSIZE * relZ];
    }

    public void setBiomeLocal(int x, int y, int z, int biome)
    {
        int relX = x % GRIDSIZE;
        int relY = y % GRIDSIZE;
        int relZ = z % GRIDSIZE;
        biomes[relX + GRIDSIZE * relY + GRIDSIZE * GRIDSIZE * relZ] = biome;
    }

    public void setBiome(int[] biomes)
    {
        if (biomes.length == this.biomes.length) this.biomes = biomes;
        else
        {
            for (int i = 0; i < biomes.length; i++)
            {
                if (i >= this.biomes.length) return;
                this.biomes[i] = biomes[i];
            }
        }
    }

    public void setBiome(Vector3 v, int i)
    {
        setBiome(v.intX(), v.intY(), v.intZ(), i);
    }

    @Override
    public String toString()
    {
        String ret = "Terrian Segment " + chunkX + "," + chunkY + "," + chunkZ + " Centre:" + getCentre();
        String eol = System.getProperty("line.separator");
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
            {
                String line = "[";
                for (int k = 0; k < 4; k++)
                {
                    line = line + biomes[i + GRIDSIZE * j + GRIDSIZE * GRIDSIZE * k];
                    if (k != 3) line = line + ", ";
                }
                line = line + "]";
                ret = ret + eol + line;
            }

        return ret;
    }

    @Override
    public int hashCode()
    {
        return (this.chunkX + this.chunkZ << 8) << 8 + this.chunkY;
    }
}
