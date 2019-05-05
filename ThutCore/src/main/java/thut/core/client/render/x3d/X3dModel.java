package thut.core.client.render.x3d;

import static java.lang.Math.toDegrees;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import thut.api.entity.IMobColourable;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.AnimationHelper;
import thut.core.client.render.animation.CapabilityAnimation.IAnimationHolder;
import thut.core.client.render.model.IAnimationChanger;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.model.IPartTexturer;
import thut.core.client.render.model.IRetexturableModel;
import thut.core.client.render.model.Vertex;
import thut.core.client.render.tabula.components.Animation;
import thut.core.client.render.tabula.components.AnimationComponent;
import thut.core.client.render.x3d.X3dXML.Appearance;
import thut.core.client.render.x3d.X3dXML.IndexedTriangleSet;
import thut.core.client.render.x3d.X3dXML.Transform;

public class X3dModel implements IModelCustom, IModel, IRetexturableModel
{
    public HashMap<String, IExtendedModelPart> parts = new HashMap<String, IExtendedModelPart>();
    Map<String, Material>                      mats  = Maps.newHashMap();
    Set<String>                                heads = Sets.newHashSet();
    final HeadInfo                             info  = new HeadInfo();
    public String                              name;

    public X3dModel()
    {

    }

    public X3dModel(ResourceLocation l)
    {
        this();
        loadModel(l);
    }

    private void addChildren(Set<Transform> allTransforms, Transform transform)
    {
        for (Transform f : transform.transforms)
        {
            if (!f.DEF.contains("ifs_TRANSFORM"))
            {
                allTransforms.add(f);
                addChildren(allTransforms, f);
            }
        }
    }

    private Material getMaterial(X3dXML.Appearance appearance)
    {
        X3dXML.Material mat = appearance.material;
        if (mat == null) return null;
        String matName = mat.DEF;
        boolean isDef = matName != null;
        if (matName == null)
        {
            matName = mat.USE.substring(3);
        }
        else
        {
            matName = matName.substring(3);
        }
        Material material = mats.get(matName);
        if (material == null || isDef)
        {
            String texName;
            if (appearance.tex != null && appearance.tex.DEF != null)
            {
                texName = appearance.tex.DEF.substring(3);
                if (texName.contains("_png")) texName = texName.substring(0, texName.lastIndexOf("_png"));
            }
            else
            {
                texName = null;
            }
            if (material == null)
            {
                material = new Material(matName, texName, mat.getDiffuse(), mat.getSpecular(), mat.getEmissive(),
                        mat.ambientIntensity, mat.shininess, mat.transparency);
            }
            if (isDef)
            {
                if (material.texture == null) material.texture = texName;
                material.ambientIntensity = mat.ambientIntensity;
                material.shininess = mat.shininess;
                material.transparency = mat.transparency;
                material.emissiveColor = mat.getEmissive();
                material.specularColor = mat.getSpecular();
                material.diffuseColor = mat.getDiffuse();
                material.emissiveMagnitude = Math.min(1, (float) (mat.getEmissive().length() / Math.sqrt(3)) / 0.8f);
            }
            mats.put(matName, material);
        }
        return material;
    }

    @Override
    public HashMap<String, IExtendedModelPart> getParts()
    {
        return parts;
    }

    public void loadModel(ResourceLocation model)
    {
        try
        {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(model);
            X3dXML xml = new X3dXML(res.getInputStream());
            res.close();
            makeObjects(xml);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public HashMap<String, IExtendedModelPart> makeObjects(X3dXML xml) throws Exception
    {
        Map<String, Set<String>> childMap = Maps.newHashMap();
        Set<Transform> allTransforms = Sets.newHashSet();
        for (Transform f : xml.model.scene.transforms)
        {
            allTransforms.add(f);
            addChildren(allTransforms, f);
        }
        for (Transform t : allTransforms)
        {
            String[] offset = t.translation.split(" ");
            Vector3 translation = Vector3.getNewVector().set(Float.parseFloat(offset[0]), Float.parseFloat(offset[1]),
                    Float.parseFloat(offset[2]));
            offset = t.scale.split(" ");
            Vertex scale = new Vertex(Float.parseFloat(offset[0]), Float.parseFloat(offset[1]),
                    Float.parseFloat(offset[2]));
            offset = t.rotation.split(" ");
            Vector4 rotations = new Vector4(Float.parseFloat(offset[0]), Float.parseFloat(offset[1]),
                    Float.parseFloat(offset[2]), (float) toDegrees(Float.parseFloat(offset[3])));
            Set<String> children = t.getChildNames();
            t = t.getIfsTransform();
            // Probably a lamp or camera in this case?
            if (t == null) continue;
            X3dXML.Group group = t.group;
            String name = t.getGroupName();
            List<Shape> shapes = Lists.newArrayList();
            for (X3dXML.Shape shape : group.shapes)
            {
                IndexedTriangleSet triangleSet = shape.triangleSet;
                Shape renderShape = new Shape(triangleSet.getOrder(), triangleSet.getVertices(),
                        triangleSet.getNormals(), triangleSet.getTexture());
                shapes.add(renderShape);
                Appearance appearance = shape.appearance;
                Material material = getMaterial(appearance);
                if (material != null) renderShape.setMaterial(material);
            }
            X3dObject o = new X3dObject(name);
            o.shapes = shapes;
            o.rotations.set(rotations.x, rotations.y, rotations.z, rotations.w);
            o.offset.set(translation);
            o.scale = scale;
            parts.put(name, o);
            childMap.put(name, children);
        }
        for (Map.Entry<String, Set<String>> entry : childMap.entrySet())
        {
            String key = entry.getKey();

            if (parts.get(key) != null)
            {
                IExtendedModelPart part = parts.get(key);
                for (String s : entry.getValue())
                {
                    if (parts.get(s) != null && parts.get(s) != part) part.addChild(parts.get(s));
                }
            }
        }
        return parts;
    }

    @Override
    public void preProcessAnimations(Collection<List<Animation>> animations)
    {
        for (List<Animation> list : animations)
        {
            for (Animation animation : list)
                for (String s : animation.sets.keySet())
                {
                    ArrayList<AnimationComponent> components = animation.sets.get(s);
                    for (AnimationComponent comp : components)
                    {
                        comp.posOffset[0] /= -16;
                        comp.posOffset[1] /= -16;
                        comp.posOffset[2] /= -16;
                        comp.posChange[0] /= -16;
                        comp.posChange[1] /= -16;
                        comp.posChange[2] /= -16;
                    }
                }
        }
    }

    @Override
    public void renderAll()
    {
        for (IExtendedModelPart o : parts.values())
        {
            if (o.getParent() == null) o.renderAll();
        }
    }

    @Override
    public void renderAllExcept(String... excludedGroupNames)
    {
        for (IExtendedModelPart o : parts.values())
        {
            if (o.getParent() == null) o.renderAllExcept(excludedGroupNames);
        }
    }

    @Override
    public void renderOnly(String... groupNames)
    {
        for (IExtendedModelPart o : parts.values())
        {
            if (o.getParent() == null) o.renderOnly(groupNames);
        }
    }

    @Override
    public void renderPart(String partName)
    {
        for (IExtendedModelPart o : parts.values())
        {
            if (o.getParent() == null) o.renderPart(partName);
        }
    }

    @Override
    public void setAnimationChanger(IAnimationChanger changer)
    {
        for (IExtendedModelPart part : parts.values())
        {
            if (part instanceof IRetexturableModel) ((IRetexturableModel) part).setAnimationChanger(changer);
        }
    }

    @Override
    public void setTexturer(IPartTexturer texturer)
    {
        for (IExtendedModelPart part : parts.values())
        {
            if (part instanceof IRetexturableModel) ((IRetexturableModel) part).setTexturer(texturer);
        }
    }

    @Override
    public Set<String> getHeadParts()
    {
        return heads;
    }

    @Override
    public HeadInfo getHeadInfo()
    {
        return info;
    }

    private void updateSubParts(Entity entity, IModelRenderer<?> renderer, String currentPhase, float partialTick,
            IExtendedModelPart parent, float headYaw, float headPitch, float limbSwing)
    {
        if (parent == null) return;
        HeadInfo info = getHeadInfo();

        parent.resetToInit();
        boolean anim = renderer.getAnimations().containsKey(currentPhase);
        if (anim)
        {
            if (AnimationHelper.doAnimation(renderer.getAnimations().get(currentPhase), entity, parent.getName(),
                    parent, partialTick, limbSwing))
            {
            }
        }
        if (info != null && isHead(parent.getName()))
        {
            float ang;
            float ang2 = -info.headPitch;
            float head = info.headYaw + 180;
            float diff = 0;
            if (info.yawDirection != -1) head *= -1;
            diff = (head) % 360;
            diff = (diff + 360) % 360;
            diff = (diff - 180) % 360;
            diff = Math.max(diff, info.yawCapMin);
            diff = Math.min(diff, info.yawCapMax);
            ang = diff;
            ang2 = Math.max(ang2, info.pitchCapMin);
            ang2 = Math.min(ang2, info.pitchCapMax);
            Vector4 dir;
            if (info.yawAxis == 0)
            {
                dir = new Vector4(info.yawDirection, 0, 0, ang);
            }
            else if (info.yawAxis == 2)
            {
                dir = new Vector4(0, 0, info.yawDirection, ang);
            }
            else
            {
                dir = new Vector4(0, info.yawDirection, 0, ang);
            }
            Vector4 dir2;
            if (info.pitchAxis == 2)
            {
                dir2 = new Vector4(0, 0, info.yawDirection, ang2);
            }
            else if (info.pitchAxis == 1)
            {
                dir2 = new Vector4(0, info.yawDirection, 0, ang2);
            }
            else
            {
                dir2 = new Vector4(info.yawDirection, 0, 0, ang2);
            }
            parent.setPostRotations(dir);
            parent.setPostRotations2(dir2);
        }

        int red = 255, green = 255, blue = 255;
        int brightness = entity.getBrightnessForRender();
        int alpha = 255;
        int[] rgbab = parent.getRGBAB();
        if (entity instanceof IMobColourable)
        {
            IMobColourable poke = (IMobColourable) entity;
            rgbab[0] = poke.getRGBA()[0];
            rgbab[1] = poke.getRGBA()[1];
            rgbab[2] = poke.getRGBA()[2];
            rgbab[3] = poke.getRGBA()[3];
        }
        else
        {
            rgbab[0] = red;
            rgbab[1] = green;
            rgbab[2] = blue;
            rgbab[3] = alpha;
            rgbab[4] = brightness;
        }
        rgbab[4] = brightness;
        IAnimationChanger animChanger = renderer.getAnimationChanger();
        if (animChanger != null)
        {
            int default_ = new Color(rgbab[0], rgbab[1], rgbab[2], rgbab[3]).getRGB();
            int rgb = animChanger.getColourForPart(parent.getName(), entity, default_);
            if (rgb != default_)
            {
                Color col = new Color(rgb);
                rgbab[0] = col.getRed();
                rgbab[1] = col.getGreen();
                rgbab[2] = col.getBlue();
            }
        }
        parent.setRGBAB(rgbab);
        for (String partName : parent.getSubParts().keySet())
        {
            IExtendedModelPart part = parent.getSubParts().get(partName);
            updateSubParts(entity, renderer, currentPhase, partialTick, part, headYaw, headPitch, limbSwing);
        }
    }

    protected void updateAnimation(Entity entity, IModelRenderer<?> renderer, String currentPhase, float partialTicks,
            float headYaw, float headPitch, float limbSwing)
    {
        for (String partName : getParts().keySet())
        {
            IExtendedModelPart part = getParts().get(partName);
            updateSubParts(entity, renderer, currentPhase, partialTicks, part, headYaw, headPitch, limbSwing);
        }
    }

    private boolean isHead(String partName)
    {
        return getHeadParts().contains(partName);
    }

    @Override
    public void applyAnimation(Entity entity, IAnimationHolder animate, IModelRenderer<?> renderer, float partialTicks,
            float limbSwing)
    {
        updateAnimation(entity, renderer, renderer.getAnimation(entity), partialTicks, getHeadInfo().headYaw,
                getHeadInfo().headYaw, limbSwing);
    }
}
