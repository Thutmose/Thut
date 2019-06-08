package thut.core.client.render.tabula.components;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.annotation.Nullable;

import org.w3c.dom.NamedNodeMap;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.core.client.render.animation.AnimationRegistry.IPartRenamer;

/** Container for Tabula animations.
 *
 * @author Gegy1000
 * @since 0.1.0 */
@OnlyIn(Dist.CLIENT)
public class Animation
{
    public final UUID                                     id         = UUID.randomUUID();

    public String                                         name       = "";
    public String                                         identifier = "";
    public int                                            length     = -1;
    /** This is used for sorting animations for determining which components
     * should take priority when multiple animations are specified for a single
     * part. */
    public int                                            priority   = 10;

    public boolean                                        loops      = true;

    private Set<String>                                   checked    = Sets.newHashSet();

    public TreeMap<String, ArrayList<AnimationComponent>> sets       = new TreeMap<String, ArrayList<AnimationComponent>>(
            Ordering.natural());

    public ArrayList<AnimationComponent> getComponents(String key)
    {
        if (!checked.contains(key))
        {
            ArrayList<AnimationComponent> comps = null;
            for (String s : sets.keySet())
            {
                if (s.startsWith("*") && key.matches(s.substring(1)))
                {
                    comps = sets.get(s);
                    break;
                }
            }
            if (comps != null)
            {
                sets.put(key, comps);
            }
            checked.add(key);
        }
        return sets.get(key);
    }

    public int getLength()
    {
        if (length == -1) initLength();
        return length;
    }

    public Animation init(NamedNodeMap map, @Nullable IPartRenamer renamer)
    {
        return this;
    }

    public void initLength()
    {
        length = -1;
        for (Entry<String, ArrayList<AnimationComponent>> entry : sets.entrySet())
        {
            for (AnimationComponent component : entry.getValue())
            {
                length = Math.max(length, component.startKey + component.length);
            }
        }
    }

    @Override
    public String toString()
    {
        return name + "|" + identifier + "|" + loops + "|" + getLength();
    }
}
