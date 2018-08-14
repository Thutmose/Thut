package thut.tech.common.blocks.lift;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

public class TileIDFixer implements IFixableData
{
    private static final Map<String, String> OLD_TO_NEW_ID_MAP = Maps.<String, String> newHashMap();
    static
    {
        OLD_TO_NEW_ID_MAP.put("minecraft:liftaccesste", "thuttech:liftaccesste");
    }

    @Override
    public int getFixVersion()
    {
        return 88888;
    }

    @Override
    public NBTTagCompound fixTagCompound(NBTTagCompound compound)
    {
        String s = OLD_TO_NEW_ID_MAP.get(compound.getString("id"));
        if (s != null)
        {
            compound.setString("id", s);
        }
        return compound;
    }

}
