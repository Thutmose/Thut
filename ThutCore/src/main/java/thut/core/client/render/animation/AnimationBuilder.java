package thut.core.client.render.animation;

import java.lang.reflect.Field;
import java.util.ArrayList;

import javax.annotation.Nullable;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;

import thut.core.client.render.animation.AnimationRegistry.IPartRenamer;
import thut.core.client.render.tabula.components.Animation;
import thut.core.client.render.tabula.components.AnimationComponent;

public class AnimationBuilder
{
    /** Constructs a new Animation, and assigns components based on the
     * definitions in the XML node.
     * 
     * @param node
     * @param renamer
     * @return */
    public static Animation build(Node node, @Nullable IPartRenamer renamer)
    {
        Animation ret = null;
        if (node.getAttributes().getNamedItem("type") == null) { return null; }
        String animName = node.getAttributes().getNamedItem("type").getNodeValue();

        ret = new Animation();
        ret.name = animName;
        ret.loops = true;
        if (node.getAttributes().getNamedItem("loops") != null)
        {
            ret.loops = Boolean.parseBoolean(node.getAttributes().getNamedItem("loops").getNodeValue());
        }

        NodeList parts = node.getChildNodes();
        Node temp;
        for (int i = 0; i < parts.getLength(); i++)
        {
            Node part = parts.item(i);
            if (part.getNodeName().equals("part"))
            {
                NodeList components = part.getChildNodes();
                String partName = part.getAttributes().getNamedItem("name").getNodeValue();
                if (renamer != null)
                {
                    String[] names = { partName };
                    renamer.convertToIdents(names);
                    partName = names[0];
                }
                ArrayList<AnimationComponent> set = Lists.newArrayList();
                for (int j = 0; j < components.getLength(); j++)
                {
                    Node component = components.item(j);
                    if (component.getNodeName().equals("component"))
                    {
                        AnimationComponent comp = new AnimationComponent();
                        if ((temp = component.getAttributes().getNamedItem("name")) != null)
                        {
                            comp.name = temp.getNodeValue();
                        }
                        if ((temp = component.getAttributes().getNamedItem("rotChange")) != null)
                        {
                            String[] vals = temp.getNodeValue().split(",");
                            comp.rotChange[0] = Double.parseDouble(vals[0]);
                            comp.rotChange[1] = Double.parseDouble(vals[1]);
                            comp.rotChange[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("posChange")) != null)
                        {
                            String[] vals = temp.getNodeValue().split(",");
                            comp.posChange[0] = Double.parseDouble(vals[0]);
                            comp.posChange[1] = Double.parseDouble(vals[1]);
                            comp.posChange[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("scaleChange")) != null)
                        {
                            String[] vals = temp.getNodeValue().split(",");
                            comp.scaleChange[0] = Double.parseDouble(vals[0]);
                            comp.scaleChange[1] = Double.parseDouble(vals[1]);
                            comp.scaleChange[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("rotOffset")) != null)
                        {
                            String[] vals = temp.getNodeValue().split(",");
                            comp.rotOffset[0] = Double.parseDouble(vals[0]);
                            comp.rotOffset[1] = Double.parseDouble(vals[1]);
                            comp.rotOffset[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("posOffset")) != null)
                        {
                            String[] vals = temp.getNodeValue().split(",");
                            comp.posOffset[0] = Double.parseDouble(vals[0]);
                            comp.posOffset[1] = Double.parseDouble(vals[1]);
                            comp.posOffset[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("scaleOffset")) != null)
                        {
                            String[] vals = temp.getNodeValue().split(",");
                            comp.scaleOffset[0] = Double.parseDouble(vals[0]);
                            comp.scaleOffset[1] = Double.parseDouble(vals[1]);
                            comp.scaleOffset[2] = Double.parseDouble(vals[2]);
                        }
                        if ((temp = component.getAttributes().getNamedItem("length")) != null)
                        {
                            comp.length = Integer.parseInt(temp.getNodeValue());
                        }
                        if ((temp = component.getAttributes().getNamedItem("startKey")) != null)
                        {
                            comp.startKey = Integer.parseInt(temp.getNodeValue());
                        }
                        if ((temp = component.getAttributes().getNamedItem("opacityChange")) != null)
                        {
                            comp.opacityChange = Double.parseDouble(temp.getNodeValue());
                        }
                        if ((temp = component.getAttributes().getNamedItem("opacityOffset")) != null)
                        {
                            comp.opacityOffset = Double.parseDouble(temp.getNodeValue());
                        }
                        if ((temp = component.getAttributes().getNamedItem("hidden")) != null)
                        {
                            comp.hidden = Boolean.parseBoolean(temp.getNodeValue());
                        }
                        set.add(comp);
                    }
                }
                if (!set.isEmpty())
                {
                    ret.sets.put(partName, set);
                }
            }
        }
        return ret;
    }

    /** Merges animation data from from to to.
     * 
     * @param from
     * @param to */
    public static void merge(Animation from, Animation to)
    {
        to.initLength();
        from.initLength();
        int x = to.length;
        int y = from.length;
        merge:
        if (y != x)
        {
            int a;
            a = (x > y) ? x : y;
            while (true)
            {
                if (a % x == 0 && a % y == 0) break;
                ++a;
            }
            int f = a / y;
            int t = a / x;

            boolean validLoop = false;
            from:
            for (String s : from.sets.keySet())
            {
                ArrayList<AnimationComponent> comps = from.sets.get(s);
                for (AnimationComponent comp : comps)
                {
                    if (isValidForMultiple(comp))
                    {
                        validLoop = true;
                        break from;
                    }
                }
            }

            if (!validLoop) break merge;
            // Start at 1 for here, as we already have to's sets.
            for (int i = 1; i < t; i++)
            {
                int length = i * x;
                int n = 0;
                sets:
                for (String s : to.sets.keySet())
                {
                    ArrayList<AnimationComponent> comps = to.sets.get(s);
                    ArrayList<AnimationComponent> newComps = Lists.newArrayList();
                    for (AnimationComponent comp : comps)
                    {
                        if (!isValidForMultiple(comp) && n++ != 0)
                        {
                            continue sets;
                        }
                    }
                    for (AnimationComponent comp : comps)
                    {
                        AnimationComponent newComp = new AnimationComponent();
                        for (Field field : AnimationComponent.class.getDeclaredFields())
                        {
                            try
                            {
                                if (field.getName().contains("Offset")) continue;
                                field.set(newComp, field.get(comp));
                            }
                            catch (IllegalArgumentException | IllegalAccessException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        newComp.startKey += length;
                        newComps.add(newComp);
                    }
                    comps.addAll(newComps);
                }
            }
            // Start at 1 for here, as we already have to's sets.
            for (int i = 0; i < f; i++)
            {
                int length = i * y;
                int n = 0;
                sets:
                for (String s : from.sets.keySet())
                {
                    ArrayList<AnimationComponent> comps = from.sets.get(s);
                    ArrayList<AnimationComponent> toComps = to.sets.get(s);
                    if (toComps == null)
                    {
                        // Making a new list to merge in.
                        to.sets.put(s, toComps = Lists.newArrayList());
                    }
                    else
                    {
                        // Already have animations for this part, we skip it.
                        continue;
                    }
                    ArrayList<AnimationComponent> newComps = Lists.newArrayList();
                    for (AnimationComponent comp : comps)
                    {
                        if (!isValidForMultiple(comp) && n++ != 0)
                        {
                            continue sets;
                        }
                    }
                    for (AnimationComponent comp : comps)
                    {
                        AnimationComponent newComp = new AnimationComponent();
                        for (Field field : AnimationComponent.class.getDeclaredFields())
                        {
                            try
                            {
                                field.set(newComp, field.get(comp));
                            }
                            catch (IllegalArgumentException | IllegalAccessException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        newComp.startKey += length;
                        newComps.add(newComp);
                    }
                    toComps.addAll(newComps);
                }
            }
            to.initLength();
            return;
        }

        // Prioritize to, if to already has animations for that part,
        // skip it.
        for (String s1 : from.sets.keySet())
            if (!to.sets.containsKey(s1))
            {
                to.sets.put(s1, from.sets.get(s1));
            }
    }

    /** @param comp
     * @return Does this component apply any animations, or just offset the
     *         position. If the latter, returns false. */
    private static boolean isValidForMultiple(AnimationComponent comp)
    {
        if (comp.startKey != 0) return true;
        if (!empty(comp.posChange)) return true;
        if (!empty(comp.rotChange)) return true;
        if (!empty(comp.scaleChange)) return true;
        return false;
    }

    /** Checls if the given array contains all zeros.
     * 
     * @param in
     * @return */
    private static boolean empty(double[] in)
    {
        for (double d : in)
            if (d != 0) return false;
        return true;
    }
}
