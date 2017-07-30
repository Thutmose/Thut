package thut.core.client.render.model;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import net.minecraft.util.ResourceLocation;
import thut.core.client.render.animation.ModelHolder;
import thut.core.client.render.collada.DAEModel;
import thut.core.client.render.x3d.X3dModel;

public class ModelFactory
{
    private static final Map<String, Class<? extends IModel>> validExtensions = Maps.newHashMap();

    static
    {
        validExtensions.put("x3d", X3dModel.class);
        // validExtensions.put("smd", SMDModel.class);
        validExtensions.put("dae", DAEModel.class);
    }

    public static void registerIModel(String extension, Class<? extends IModel> clazz)
    {
        validExtensions.put(extension, clazz);
    }

    public static IModel create(ModelHolder model)
    {
        String path = model.model.getResourcePath();
        String ext = path.substring(path.lastIndexOf(".") + 1, path.length());
        Class<? extends IModel> clazz = validExtensions.get(ext);
        if (clazz != null)
        {
            try
            {
                return clazz.getConstructor(ResourceLocation.class).newInstance(model.model);
            }
            catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Set<String> getValidExtensions()
    {
        return validExtensions.keySet();
    }
}
