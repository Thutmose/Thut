package thut.core.client.render.smd;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
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

    public SMDModel(ResourceLocation model)
    {
        this();
        try
        {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(model);
            SMDParser parser = new SMDParser(res.getInputStream());
            res.close();

            List<String> anims = Lists.newArrayList("idle");
            for (String s : anims)
            {
                String anim = model.toString().replace(".smd", "/" + s + ".smd");
                ResourceLocation animation = new ResourceLocation(anim);
                try
                {
                    res = Minecraft.getMinecraft().getResourceManager().getResource(animation);
                    parser.parseAnimation(res.getInputStream(), s);
                    res.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            this.triangles = parser.model.triangles;
            this.skeleton = parser.model.skeleton;
            this.poses = parser.model.poses;
            this.vertexID = parser.model.vertexID;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void animate()
    {
        if (skeleton.pose == null) return;
        skeleton.pose.nextFrame();
        skeleton.applyPose();
    }

    public int getNextVertexID()
    {
        return vertexID++;
    }

    public void render()
    {
        GL11.glScaled(0.01, 0.01, 0.01);
        this.setAnimation("default");
        GlStateManager.disableTexture2D();
        animate();
        triangles.render();
        GlStateManager.enableTexture2D();
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
