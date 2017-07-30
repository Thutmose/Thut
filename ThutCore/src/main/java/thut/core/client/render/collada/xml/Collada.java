package thut.core.client.render.collada.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

@XmlRootElement(name = "COLLADA")
public class Collada
{
    public static Collada create(InputStream stream) throws Exception
    {
        JAXBContext jc = JAXBContext.newInstance(Collada.class);
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        XMLReader xmlReader = spf.newSAXParser().getXMLReader();
        xmlReader.setEntityResolver(new EntityResolver()
        {
            @Override
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
            {
                return new InputSource(new StringReader(""));
            }
        });
        InputSource inputSource = new InputSource(new InputStreamReader(stream));
        SAXSource source = new SAXSource(xmlReader, inputSource);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        Collada collada = (Collada) unmarshaller.unmarshal(source);
        return collada;
    }

    @XmlAttribute
    public String              xmlns;
    @XmlAttribute
    public String              version;

    public Asset               asset;
    public LibraryEffects      library_effects;
    public LibraryMaterials    library_materials;
    public LibraryGeometries   library_geometries;
    public LibraryAnimations   library_animations;
    public LibraryControllers  library_controllers;
    public LibraryVisualScenes library_visual_scenes;
    public Scene               scene;
}
