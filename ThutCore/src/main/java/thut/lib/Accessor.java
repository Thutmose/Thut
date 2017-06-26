package thut.lib;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class Accessor
{
    public static final Method CREEPER_BOOM;
    public static final Method ENTITY_SETSIZE;

    static
    {
        CREEPER_BOOM = ReflectionHelper.findMethod(EntityCreeper.class,
                "explode", "func_146077_cc");
        CREEPER_BOOM.setAccessible(true);
        ENTITY_SETSIZE = ReflectionHelper.findMethod(Entity.class, "setSize", "func_70105_a", 
                float.class, float.class);
        ENTITY_SETSIZE.setAccessible(true);
    }

    public static void explode(EntityCreeper boom)
    {
        try
        {
            CREEPER_BOOM.invoke(boom);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    public static void size(Entity in, float width, float height)
    {
        try
        {
            ENTITY_SETSIZE.invoke(in, width, height);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }
}
