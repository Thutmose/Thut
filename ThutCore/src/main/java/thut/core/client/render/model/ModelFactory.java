package thut.core.client.render.model;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import thut.core.client.render.animation.ModelHolder;
import thut.core.client.render.collada.DAEModel;
import thut.core.client.render.smd.SMDModel;
import thut.core.client.render.x3d.X3dModel;

public class ModelFactory
{
    private static final Map<String, Class<? extends IModel>> validExtensions = Maps.newHashMap();

    static
    {
        validExtensions.put("x3d", X3dModel.class);
        validExtensions.put("smd", SMDModel.class);
        validExtensions.put("dae", DAEModel.class);
    }

    public static IModel create(ModelHolder model)
    {
        if (model.model.getResourcePath().endsWith(".x3d")) return new X3dModel(model.model);
        if (model.model.getResourcePath().endsWith(".smd")) return new SMDModel(model.model);
        if (model.model.getResourcePath().endsWith(".dae")) return new DAEModel(model.model);
        return null;
    }

    public static Set<String> getValidExtensions()
    {
        return validExtensions.keySet();
    }
}
