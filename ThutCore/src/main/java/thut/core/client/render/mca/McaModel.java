package thut.core.client.render.mca;

import java.io.InputStream;
import java.util.HashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.x3d.X3dModel;

public class McaModel extends X3dModel
{
    public McaModel(ResourceLocation l)
    {
        super();
        loadModel(l);
    }

    public void loadModel(ResourceLocation model)
    {
        try
        {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(model);
            InputStream stream = res.getInputStream();
            McaXML xml = new McaXML(stream);
            makeObjects(xml);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    HashMap<String, IExtendedModelPart> makeObjects(McaXML xml) throws Exception
    {
        return parts;
    }
}
