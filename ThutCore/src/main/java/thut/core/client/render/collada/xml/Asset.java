package thut.core.client.render.collada.xml;

import java.util.Map;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import com.google.common.collect.Maps;

@XmlRootElement(name = "asset")
public class Asset
{
    @XmlRootElement(name = "contributor")
    public static class Contributor
    {
        public String author;
        public String authoring_tool;
    }

    public static class Unit
    {
        public String             name;
        @XmlAnyAttribute
        public Map<QName, String> values = Maps.newHashMap();
    }

    public Contributor contributor;
    public String      created;
    public String      modified;
    public Unit        unit;
    public String      up_axis;
}
