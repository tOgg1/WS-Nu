package org.ntnunotif.wsnu.base.net;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.Node;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

/**
 * Created by tormod on 3/3/14.
 */
public class XMLParser {

    private static String[] classPaths = { "org.oasis_open.docs.wsn.b_2",
            "org.oasis_open.docs.wsn.br_2",
            "org.oasis_open.docs.wsn.t_1",
            "org.oasis_open.docs.wsrf.bf_2",
            "org.oasis_open.docs.wsrf.r_2"};

    private  static ClassLoader[] factories = {org.oasis_open.docs.wsn.b_2.ObjectFactory.class.getClassLoader(),
            org.oasis_open.docs.wsn.br_2.ObjectFactory.class.getClassLoader(),
            org.oasis_open.docs.wsn.t_1.ObjectFactory.class.getClassLoader(),
            org.oasis_open.docs.wsrf.bf_2.ObjectFactory.class.getClassLoader(),
            org.oasis_open.docs.wsrf.r_2.ObjectFactory.class.getClassLoader()};

    private XMLParser() {}

    public static void registerReturnObjectPackageAndObjectFactory(String classPath, ClassLoader factoryLoader) {
        synchronized (XMLParser.class) {
            // TODO
        }
    }

    private static Unmarshaller getUnmarshaller() throws JAXBException {
        String cp = null;
        for (String s: classPaths)
            cp = cp == null ? s : cp + ":" + s;
        // TODO need all object factories
       return JAXBContext.newInstance(cp, factories[0]).createUnmarshaller();
    }

    public static Object parse(Node node) throws JAXBException {
        return getUnmarshaller().unmarshal(node);
    }

    public static Object parse(InputStream inputStream) throws JAXBException {
        return getUnmarshaller().unmarshal(inputStream);
    }

    public static Object parse(XMLStreamReader xmlStreamReader) throws JAXBException {
        return getUnmarshaller().unmarshal(xmlStreamReader);
    }
}
