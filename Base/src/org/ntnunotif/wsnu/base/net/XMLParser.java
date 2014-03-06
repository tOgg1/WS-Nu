package org.ntnunotif.wsnu.base.net;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.Node;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

/**
 * The <code>XMLParser</code> is a static tool utility for parsing XML documents into Java objects.
 *
 * @author Inge Edward Halsaunet
 */
public class XMLParser {

    /**
     * <code>classPaths</code> hold all package names for the realized Java objects. The package must contain a class
     * <code>ObjectFactory</code> that can produce all parseable classes in that package.
     */
    private static String[] classPaths = { "org.oasis_open.docs.wsn.b_2",
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
        String cp = null;
        synchronized (XMLParser.class) {
            for (String s: classPaths)
                cp = cp == null ? s : cp + ":" + s;
        }
       return JAXBContext.newInstance(cp, classLoader).createUnmarshaller();
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
    public static Object parse(Node node) throws JAXBException {
        return getUnmarshaller().unmarshal(node);
    }

    /**
     * Parses the {@link java.io.InputStream}, and returns the parsed tree structure
     *
     * @param inputStream The {@link java.io.InputStream} to parse.
     *
     * @return The apropriate object.
     *
     * @throws JAXBException {@link javax.xml.bind.JAXBContext#newInstance(String, ClassLoader)}
     */
    public static Object parse(InputStream inputStream) throws JAXBException {
        return getUnmarshaller().unmarshal(inputStream);
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
    public static Object parse(XMLStreamReader xmlStreamReader) throws JAXBException {
        return getUnmarshaller().unmarshal(xmlStreamReader);
    }
}
