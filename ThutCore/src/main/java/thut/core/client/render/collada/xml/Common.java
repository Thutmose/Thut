package thut.core.client.render.collada.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;

import com.google.common.collect.Lists;

public class Common
{
    public static class Source
    {
        @XmlAttribute
        public String           id;
        public String_Array     float_array;
        public String_Array     Name_array;
        public Technique_Common technique_common;
    }

    public static class String_Array
    {
        @XmlAttribute
        public String id;
        @XmlAttribute
        public String count;
        @XmlValue
        public String value;
    }

    public static class Technique_Common
    {
        public Accessor accessor;
    }

    public static class Accessor
    {
        @XmlAttribute
        public String source;
        @XmlAttribute
        public int    count;
        @XmlAttribute
        public int    stride;
        @XmlElement(name = "param")
        List<Param>   param = Lists.newArrayList();
    }

    public static class Param
    {
        @XmlAttribute
        public String name;
        @XmlAttribute
        public String type;
    }

    public static class Input
    {
        @XmlAttribute
        public String semantic;
        @XmlAttribute
        public String source;
        @XmlAttribute
        public String offset;
    }
}
