package thut.world.common.worldgen;

import net.minecraft.world.biome.BiomeGenBase;

public class BiomeVolcano extends BiomeGenBase
{

	public BiomeVolcano(int par1) {
		super(par1);
        this.spawnableMonsterList.clear();
        this.spawnableCreatureList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCaveCreatureList.clear();
        this.setColor(16711680).setTemperatureRainfall(2.0F, 1.0F);
        this.biomeName = "volcano";
	}

}
