package thut.core.client.render.collada.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

import thut.core.client.render.collada.xml.Common.Input;
import thut.core.client.render.collada.xml.Common.Source;

@XmlRootElement(name = "library_controllers")
public class LibraryControllers
{
    public static class Controller
    {
        @XmlAttribute
        public String id;
        @XmlAttribute
        public String name;

        public Skin   skin;
    }

    public static class Skin
    {
        public String         bind_shape_matrix;
        @XmlElement(name = "source")
        public List<Source>   source = Lists.newArrayList();

        public Joints         joints;
        public Vertex_Weights vertex_weights;
    }

    public static class Joints
    {
        @XmlElement(name = "input")
        public List<Input> input = Lists.newArrayList();
    }

    public static class Vertex_Weights
    {
        @XmlAttribute
        public int         count;

        @XmlElement(name = "input")
        public List<Input> input = Lists.newArrayList();
        public String      vcount;
        public String      v;
    }

    @XmlElement(name = "controller")
    public List<Controller> controller = Lists.newArrayList();
}
