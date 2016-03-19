package dorfgen.worldgen;

import net.minecraft.world.WorldProvider;

public class WorldProviderFinite extends WorldProvider{

	public WorldProviderFinite() {
		
	}

	@Override
	public String getDimensionName() {
		return "Dorven Realm";
	}

	@Override
	public String getInternalNameSuffix() {
		return "dorfs";
	}

}
