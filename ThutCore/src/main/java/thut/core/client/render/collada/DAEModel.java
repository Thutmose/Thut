package thut.core.client.render.collada;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import net.minecraft.util.ResourceLocation;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.tabula.components.Animation;

public class DAEModel implements IModel
{

    public DAEModel(ResourceLocation model)
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public HashMap<String, IExtendedModelPart> getParts()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void preProcessAnimations(Collection<Animation> animations)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<String> getHeadParts()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HeadInfo getHeadInfo()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
