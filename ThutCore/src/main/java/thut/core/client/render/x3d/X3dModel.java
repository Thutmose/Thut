package thut.core.client.render.x3d;

import static java.lang.Math.toDegrees;

import java.io.InputStream;
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
import net.minecraft.util.ResourceLocation;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.model.IAnimationChanger;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;
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
    public String                              name;

    public X3dModel()
    {

    }

    public X3dModel(ResourceLocation l)
    {
        this();
        loadModel(l);
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
            InputStream stream = res.getInputStream();
            X3dXML xml = new X3dXML(stream);
            makeObjects(xml);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
                texName = texName.substring(0, texName.lastIndexOf("_png"));
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
            if (material != null && isDef)
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

    HashMap<String, IExtendedModelPart> makeObjects(X3dXML xml) throws Exception
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
    public void preProcessAnimations(Collection<Animation> animations)
    {
        for (Animation animation : animations)
        {
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
            o.renderAll();
        }
    }

    @Override
    public void renderAllExcept(String... excludedGroupNames)
    {
        for (String s : parts.keySet())
        {
            boolean skipPart = false;
            for (String excludedGroupName : excludedGroupNames)
            {
                if (excludedGroupName.equalsIgnoreCase(s))
                {
                    skipPart = true;
                }
            }
            if (!skipPart)
            {
                parts.get(s).renderAll();
            }
        }
    }

    @Override
    public void renderOnly(String... groupNames)
    {
        for (String s : groupNames)
            if (parts.containsKey(s)) parts.get(s).renderAll();
    }

    @Override
    public void renderPart(String partName)
    {
        if (parts.containsKey(partName)) parts.get(partName).renderPart(partName);
    }

    @Override
    public void setAnimationChanger(IAnimationChanger changer)
    {
        for (IExtendedModelPart part : parts.values())
        {
            if (part instanceof IRetexturableModel) ((IRetexturableModel) part).setAnimationChanger(changer);
            ;
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
}
