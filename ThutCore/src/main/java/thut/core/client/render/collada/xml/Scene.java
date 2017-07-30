package thut.core.client.render.collada.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

@XmlRootElement(name = "scene")
public class Scene
{
    public static class Instance
    {
        @XmlAttribute
        public String url;
    }

    @XmlElement(name = "instance_visual_scene")
    public List<Instance> instance_visual_scene = Lists.newArrayList();
}
