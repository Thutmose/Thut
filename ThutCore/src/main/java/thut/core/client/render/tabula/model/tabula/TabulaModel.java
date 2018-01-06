package thut.core.client.render.tabula.model.tabula;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.tabula.components.Animation;
import thut.core.client.render.tabula.json.JsonTabulaModel;
import thut.core.client.render.tabula.model.IModel;

public class TabulaModel extends JsonTabulaModel implements IModel
{
    private String                            modelName;
    private String                            authorName;

    final HashMap<String, IExtendedModelPart> parts = Maps.newHashMap();
    final Set<String>                         head  = Sets.newHashSet();
    final HeadInfo                            info  = new HeadInfo();

    @Override
    public String getAuthor()
    {
        return authorName;
    }

    @Override
    public String getName()
    {
        return modelName;
    }

    @Override
    public HashMap<String, IExtendedModelPart> getParts()
    {
        return parts;
    }

    @Override
    public void preProcessAnimations(Collection<List<Animation>> collection)
    {
    }

    @Override
    public Set<String> getHeadParts()
    {
        return head;
    }

    @Override
    public HeadInfo getHeadInfo()
    {
        return info;
    }
}
