package thut.breakdeny;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Config
{
    public static Class<?>[] whitelist;

    public Config(FMLPreInitializationEvent e)
    {
        loadConfig(e);
    }

    void loadConfig(FMLPreInitializationEvent e)
    {
        Configuration config = new Configuration(e.getSuggestedConfigurationFile());
        config.load();
        String[] classes = config.getStringList("whitelist", Configuration.CATEGORY_GENERAL, new String[0],
                "List of class names, if a block is assignable from this class, it will be whitelisted for breaking.");
        List<Class<?>> classList = Lists.newArrayList();
        for(String s: classes)
        {
            try
            {
                Class<?> c = Class.forName(s);
                classList.add(c);
            }
            catch (ClassNotFoundException e1)
            {
                e1.printStackTrace();
            }
        }
        whitelist = classList.toArray(new Class<?>[0]);
        config.save();
    }

    public static boolean isWhitelisted(Object o)
    {
        for (Class<?> c : whitelist)
        {
            if (c.isInstance(o)) return true;
        }
        return false;
    }
}
