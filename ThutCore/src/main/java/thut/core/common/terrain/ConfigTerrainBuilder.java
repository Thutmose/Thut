package thut.core.common.terrain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainSegment;

public class ConfigTerrainBuilder
{
    public static Predicate<IBlockState> getState(String arguments)
    {
        String[] args = arguments.split(" ");

        String[] resource = args[0].split(":");
        final String modid = resource[0];
        final String blockName = resource[1];
        String keyTemp = null;
        String valTemp = null;

        if (args.length > 1)
        {
            String[] state = args[1].split("=");
            keyTemp = state[0];
            valTemp = state[1];
        }
        final String key = keyTemp;
        final String val = valTemp;
        return new Predicate<IBlockState>()
        {
            final Pattern                  modidPattern = Pattern.compile(modid);
            final Pattern                  blockPattern = Pattern.compile(blockName);
            Map<ResourceLocation, Boolean> checks       = Maps.newHashMap();

            @Override
            public boolean apply(IBlockState input)
            {
                if (input == null || input.getBlock() == null) return false;
                Block block = input.getBlock();
                ResourceLocation name = block.getRegistryName();
                if (checks.containsKey(name) && !checks.get(name)) return false;
                else if (!checks.containsKey(name))
                {
                    if (!modidPattern.matcher(name.getResourceDomain()).matches())
                    {
                        checks.put(name, false);
                        return false;
                    }
                    if (!blockPattern.matcher(name.getResourcePath()).matches())
                    {
                        checks.put(name, false);
                        return false;
                    }
                    checks.put(name, true);
                }
                if (key == null) return true;
                for (IProperty<?> prop : input.getPropertyNames())
                {
                    if (prop.getName().equals(key))
                    {
                        Object inputVal = input.getValue(prop);
                        return inputVal.toString().equalsIgnoreCase(val);
                    }
                }
                return false;
            }
        };
    }

    private static void addToList(List<Predicate<IBlockState>> list, String... conts)
    {
        if (conts == null) return;
        if (conts.length < 1) return;
        for (String s : conts)
        {
            Predicate<IBlockState> b = getState(s);
            if (b != null)
            {
                list.add(b);
            }
        }

    }

    public static void process(String[] values)
    {
        Map<String, ArrayList<String>> types = Maps.newHashMap();
        for (String s : values)
        {
            try
            {
                String[] args = s.split("->");
                String id = args[0];
                String val = args[1];
                ArrayList<String> list = types.get(id);
                if (list == null) types.put(id, list = Lists.newArrayList());
                list.add(val);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        for (String type : types.keySet())
        {
            BiomeType subbiome = BiomeType.getBiome(type, true);
            generateConfigTerrain(types.get(type).toArray(new String[0]), subbiome);
        }
    }

    private static void generateConfigTerrain(String[] blocks, BiomeType subbiome)
    {
        List<Predicate<IBlockState>> list = Lists.newArrayList();
        addToList(list, blocks);
        if (!list.isEmpty())
        {
            ConfigTerrainChecker checker = new ConfigTerrainChecker(list, subbiome);
            TerrainSegment.biomeCheckers.add(checker);
        }
    }

}
