package thut.core.client.render.smd;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import thut.core.client.render.model.IAnimationChanger;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;
import thut.core.client.render.model.IPartTexturer;
import thut.core.client.render.model.IRetexturableModel;
import thut.core.client.render.tabula.components.Animation;

public class SMDModel implements IModelCustom, IModel, IRetexturableModel, IFakeExtendedPart
{
    public Skeleton                                   skeleton;
    public Triangles                                  triangles;
    public HashMap<String, SkeletonAnimation>         poses        = new HashMap<>();

    private final HashMap<String, IExtendedModelPart> nullPartsMap = Maps.newHashMap();
    private final HashMap<String, IExtendedModelPart> subPartsMap  = Maps.newHashMap();
    private final Set<String>                         nullHeadSet  = Sets.newHashSet();
    private final HeadInfo                            info         = new HeadInfo();

    private int                                       vertexID     = 0;

    public SMDModel()
    {
        nullPartsMap.put(getName(), this);
    }

    public void animate()
    {
        if (skeleton.pose == null) return;
        // skeleton.pose.nextFrame();
        skeleton.applyPose();
    }

    public int getNextVertexID()
    {
        return vertexID++;
    }

    public void render()
    {
        animate();
        triangles.render();
    }

    public void setAnimation(String animation)
    {
        if (poses.containsKey(animation))
        {
            skeleton.setPose(poses.get(animation));
        }
    }

    @Override
    public void setAnimationChanger(IAnimationChanger changer)
    {
        // Not sure if i want to do this.
    }

    @Override
    public void setTexturer(IPartTexturer texturer)
    {
        // Not sure if i want to do this.
    }

    @Override
    public HashMap<String, IExtendedModelPart> getParts()
    {
        // SMD Renders whole thing at once, so no part rendering.
        return nullPartsMap;
    }

    @Override
    public void preProcessAnimations(Collection<Animation> animations)
    {
        // SMD handles animations differently, so nothing here.
    }

    @Override
    public void renderAll()
    {
        render();
    }

    @Override
    public void renderAllExcept(String... excludedGroupNames)
    {
        // SMD Renders whole thing at once, so no part rendering.
        render();
    }

    @Override
    public void renderOnly(String... groupNames)
    {
        // SMD Renders whole thing at once, so no part rendering.
        render();
    }

    @Override
    public void renderPart(String partName)
    {
        // SMD Renders whole thing at once, so no part rendering.
        render();
    }

    @Override
    public Set<String> getHeadParts()
    {
        return nullHeadSet;
    }

    @Override
    public HeadInfo getHeadInfo()
    {
        return info;
    }

    @Override
    public String getName()
    {
        return "main";
    }

    @Override
    public HashMap<String, IExtendedModelPart> getSubParts()
    {
        return subPartsMap;
    }

    @Override
    public String getType()
    {
        return "smd";
    }

}
