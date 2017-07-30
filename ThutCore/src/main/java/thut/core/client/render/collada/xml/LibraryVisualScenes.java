package thut.core.client.render.collada.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.google.common.collect.Lists;

@XmlRootElement(name = "library_visual_scenes")
public class LibraryVisualScenes
{
    public static class Visual_Scene
    {
        @XmlAttribute
        public String     id;
        @XmlAttribute
        public String     name;

        @XmlElement(name = "node")
        public List<Node> node = Lists.newArrayList();
    }

    public static class Node
    {
        @XmlAttribute
        public String     id;
        @XmlAttribute
        public String     name;
        @XmlAttribute
        public String     type;

        @XmlElement(name = "translate")
        public Var        translate;

        @XmlElement(name = "rotate")
        public List<Var>  rotate = Lists.newArrayList();

        @XmlElement(name = "scale")
        public Var        scale;

        @XmlElement(name = "node")
        public List<Node> node;
        @XmlElement(name = "matrix")
        public Var        matrix;
        @XmlElement(name = "extra")
        public Extra      extra;
    }

    public static class Var
    {
        @XmlAttribute
        public String sid;
        @XmlValue
        public String value;
    }

    public static class Extra
    {
        public Technique_Scene technique;
    }

    public static class Technique_Scene
    {
        @XmlAttribute
        public String profile;
        public String connect;
        public String layer;
        public String roll;
        public String tip_x;
        public String tip_y;
        public String tip_z;
    }

    @XmlElement(name = "visual_scene")
    public List<Visual_Scene> visual_scene = Lists.newArrayList();
}
