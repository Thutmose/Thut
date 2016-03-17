package thut.core.client.render.model;

import java.util.Collection;
import java.util.HashMap;

import thut.core.client.render.tabula.components.Animation;

public interface IModel
{
    public HashMap<String, IExtendedModelPart> getParts();

    public void preProcessAnimations(Collection<Animation> animations);
}
