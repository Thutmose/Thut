package thut.core.client.render.collada.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

import thut.core.client.render.collada.xml.Common.Input;
import thut.core.client.render.collada.xml.Common.Source;

@XmlRootElement(name = "library_animations")
public class LibraryAnimations
{
    public static class Animation
    {
        @XmlAttribute
        public String       id;
        @XmlElement(name = "source")
        public List<Source> source = Lists.newArrayList();

        public Sampler      sampler;
        public Channel      channel;
    }

    public static class Sampler
    {
        @XmlAttribute
        public String      id;

        @XmlElement(name = "input")
        public List<Input> input = Lists.newArrayList();
    }

    public static class Channel
    {
        @XmlAttribute
        public String source;
        @XmlAttribute
        public String target;
    }

    @XmlElement(name = "animation")
    public List<Animation> animation = Lists.newArrayList();
}
