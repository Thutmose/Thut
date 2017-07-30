package thut.core.client.render.collada.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.google.common.collect.Lists;

@XmlRootElement(name = "library_effects")
public class LibraryEffects
{
    public static class Effect
    {
        @XmlAttribute
        public String        id;
        public ProfileCommon profile_COMMON;
    }

    public static class ProfileCommon
    {
        public Technique_Effect technique;
    }

    public static class Technique_Effect
    {
        @XmlAttribute
        public String sid;
        public Phong  phong;
    }

    public static class Phong
    {
        public Type emission;
        public Type ambient;
        public Type diffuse;
        public Type specular;
        public Type shininess;
        public Type index_of_refraction;
    }

    public static class Type
    {
        @XmlElement(name = "color")
        public Colour      colour;
        @XmlElement(name = "float")
        public FloatHolder float_;
    }

    public static class Colour
    {
        @XmlAttribute
        public String sid;
        @XmlValue
        public String value;
    }

    @XmlRootElement(name = "float")
    public static class FloatHolder
    {
        @XmlAttribute
        public String sid;
        @XmlValue
        public String value;
    }

    @XmlElement(name = "effect")
    public List<Effect> effect = Lists.newArrayList();
}
