package thut.core.client.render.collada.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

import thut.core.client.render.collada.xml.Common.Input;
import thut.core.client.render.collada.xml.Common.Source;

@XmlRootElement(name = "library_geometries")
public class LibraryGeometries
{
    public static class Geometry
    {
        @XmlAttribute
        public String id;
        @XmlAttribute
        public String name;

        public Mesh   mesh;
    }

    public static class Mesh
    {
        @XmlElement(name = "source")
        public List<Source>   source   = Lists.newArrayList();
        public Vertices       vertices;
        @XmlElement(name = "source")
        public List<PolyList> polylist = Lists.newArrayList();
    }

    public static class PolyList
    {
        @XmlAttribute
        public String      material;
        @XmlAttribute
        public String      count;

        @XmlElement(name = "input")
        public List<Input> input = Lists.newArrayList();

        public String      vcount;
        public String      p;
    }

    public static class Vertices
    {
        @XmlAttribute
        public String id;
        public Input  input;
    }

    @XmlElement(name = "geometry")
    public List<Geometry> geometry = Lists.newArrayList();
}
