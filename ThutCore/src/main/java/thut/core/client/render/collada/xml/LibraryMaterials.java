package thut.core.client.render.collada.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

@XmlRootElement(name = "library_materials")
public class LibraryMaterials
{
    public static class Material
    {
        @XmlAttribute
        public String id;
        @XmlAttribute
        public String name;
    }

    public static class Instance_Effect
    {
        @XmlAttribute
        public String url;
    }

    @XmlElement(name = "material")
    public List<Material> material = Lists.newArrayList();
}
