package dorfgen.world.gen;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.ChunkGenerator;

public class DorfWorldType extends WorldType
{

    public DorfWorldType(final String name)
    {
        super(name);
    }

    @Override
    public ChunkGenerator<?> createChunkGenerator(final World world)
    {
        final DorfSettings settings = new DorfSettings();
        final DorfBiomeProvider provider = new DorfBiomeProvider(settings);
        return new DorfChunkGenerator(world, provider, settings);
    }

}
