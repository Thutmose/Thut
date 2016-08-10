package dorfgen.worldgen;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;

public class WorldProviderFinite extends WorldProvider{

	public WorldProviderFinite() {
		
	}

    @Override
    public DimensionType getDimensionType()
    {
        return DimensionType.OVERWORLD;
    }

}
