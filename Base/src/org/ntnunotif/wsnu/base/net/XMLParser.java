package org.ntnunotif.wsnu.base.net;

import org.ntnunotif.wsnu.base.internal.InternalMessage;
import org.ntnunotif.wsnu.base.topics.WSNamespaceContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.NamespaceSupport;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The <code>XMLParser</code> is a static tool utility for parsing XML documents to and from Java objects.
 *
 * @author Inge Edward Halsaunet
 */
public class XMLParser {

    /**
     * Remember the jaxbContext between parse tasks.
     */
    private static JAXBContext jaxbContext = null;

    /**
     * <code>classPaths</code> hold all package names for the realized Java objects. The package must contain a class
     * <code>ObjectFactory</code> that can produce all parseable classes in that package.
     */
    private static String[] classPaths = {
            "org.w3._2001._12.soap_envelope",
            "org.oasis_open.docs.wsn.b_2",
            "org.oasis_open.docs.wsn.br_2",
            "org.oasis_open.docs.wsn.t_1",
            "org.oasis_open.docs.wsrf.bf_2",
            "org.oasis_open.docs.wsrf.r_2"};

    /**
     * classLoader is the default loader for java classes.
     */
    private  static ClassLoader classLoader = org.oasis_open.docs.wsn.b_2.ObjectFactory.class.getClassLoader();

    /**
     * This class should never instantiated.
     */
    private XMLParser() {}

    /**
     * Extends <code>XMLParser</code>s capabilities. <code>registerReturnObjectPackageWithObjectFactory</code> registers
     * a new package name to the parser. This package must contain java classes that should be built during parsing. A
     * <code>ObjectFactory</code> class must be present in this package.
     *
     * @param classPath fully qualified package name
     */
    public static void registerReturnObjectPackageWithObjectFactory(String classPath) {
        synchronized (XMLParser.class) {
            jaxbContext = null;
            String[] newPaths = new String[classPaths.length + 1];
            for (int i = 0; i < classPaths.length; i++) {
                newPaths[i] = classPaths[i];
            }
            newPaths[newPaths.length - 1] = classPath;
        }
    }

    /**
     * gets the {@link javax.xml.bind.Unmarshaller} with context given by context paths.
     *
     * @return the apropriate <code>Unmarshaller</code>
     *
     * @throws JAXBException {@link javax.xml.bind.JAXBContext#newInstance(String, ClassLoader)}
     */
    private static Unmarshaller getUnmarshaller() throws JAXBException {
       return getJaxbContext().createUnmarshaller();
    }

    /**
     * gets current jaxbContext. Ensures that it is updated with current classpaths. Used for parsing xml to java
     * objects.
     *
     * @return the current jaxbContext
     *
     * @throws JAXBException if new instance of jaxbContext fails for some reason.
     */
    private static JAXBContext getJaxbContext() throws JAXBException {
        synchronized (XMLParser.class) {
            if (jaxbContext == null) {
                String cp = null;
                for (String s: classPaths)
                    cp = cp == null ? s : cp + ":" + s;
                jaxbContext = JAXBContext.newInstance(cp, classLoader);
            }
            return jaxbContext;
        }
    }

    /**
     * get the {@link javax.xml.bind.Marshaller} with context given by context paths. Used to convert java objects to
     * xml.
     *
     * @return the apropriate <code>Marshaller</code>
     *
     * @throws JAXBException {@link javax.xml.bind.JAXBContext#newInstance(String, ClassLoader)}
     */
    private static Marshaller getMarshaller() throws JAXBException{
        return getJaxbContext().createMarshaller();
    }

    /**
     * Parses the {@link javax.xml.soap.Node}, and returns the parsed tree structure
     *
     * @param node The {@link javax.xml.soap.Node} to parse.
     *
     * @return The apropriate object.
     *
     * @throws JAXBException {@link javax.xml.bind.JAXBContext#newInstance(String, ClassLoader)}
     */
    //public static Object parse(Node node) throws JAXBException {
    //    return getUnmarshaller().unmarshal(node);
    //}

    /**
     * Parses the {@link java.io.InputStream}, and returns the parsed tree structure
     *
     * @param inputStream The {@link java.io.InputStream} to parse.
     *
     * @return The apropriate object.
     *
     * @throws JAXBException {@link javax.xml.bind.JAXBContext#newInstance(String, ClassLoader)}
     */
    public static InternalMessage parse(InputStream inputStream) throws JAXBException {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        try {
            XMLStreamReader streamReader = factory.createXMLStreamReader(inputStream);
            return parse(streamReader);
        } catch (XMLStreamException e) {
            // TODO
            e.printStackTrace();
            throw new JAXBException("Could not create XMLStream to read from");
        }
    }

    /**
     * Parses the {@link javax.xml.stream.XMLStreamReader}, and returns the parsed tree structure
     *
     * @param xmlStreamReader The {@link javax.xml.stream.XMLStreamReader} to parse.
     *
     * @return The apropriate object.
     *
     * @throws JAXBException {@link javax.xml.bind.JAXBContext#newInstance(String, ClassLoader)}
     */
    public static InternalMessage parse(XMLStreamReader xmlStreamReader) throws JAXBException {
        NamespaceContext nsc = xmlStreamReader.getNamespaceContext();
        NamespaceSupport namespaceSupport = new NamespaceSupport();
        InternalMessage msg = new InternalMessage(InternalMessage.STATUS_OK, getUnmarshaller().unmarshal(xmlStreamReader));
        msg.setNamespaceContext(nsc);
        return msg;
    }

    /**
     * Converts the given object to XML and writes its content to the stream.
     *
     * @param object the object to parse to XML
     * @param outputStream the stream to write to
     * @throws JAXBException if JAXBContext could not be created or any unexpected events happens during writing.
     *      {@link javax.xml.bind.JAXBContext#newInstance(String, ClassLoader)}
     *      {@link javax.xml.bind.Marshaller#marshal(Object, java.io.OutputStream)}
     */
    public static void writeObjectToStream(Object object, OutputStream outputStream) throws JAXBException {
        getMarshaller().marshal(object, outputStream);
    }


    private class WSXMLFilter extends XMLFilterImpl {
        WSNamespaceContext namespaceContext = new WSNamespaceContext();
        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes atts) throws SAXException {
            if (qName != null) {
                String prefix = qName.substring(0, qName.indexOf(':'));
                namespaceContext.put(prefix, uri);
            }
            super.startElement(uri, localName, qName, atts);
        }
    }


}
