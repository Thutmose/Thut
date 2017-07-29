package thut.core.client.render.model;

import thut.core.client.render.animation.ModelHolder;
import thut.core.client.render.collada.DAEModel;
import thut.core.client.render.smd.SMDModel;
import thut.core.client.render.x3d.X3dModel;

public class ModelFactory
{

    public static IModel create(ModelHolder model)
    {
        if (model.model.getResourcePath().endsWith(".x3d")) return new X3dModel(model.model);
        if (model.model.getResourcePath().endsWith(".smd")) return new SMDModel(model.model);
        if (model.model.getResourcePath().endsWith(".dae")) return new DAEModel(model.model);
        return null;
    }

}
