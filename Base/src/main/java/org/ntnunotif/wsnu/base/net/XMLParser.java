package org.ntnunotif.wsnu.base.net;

import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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
            "org.oasis_open.docs.wsrf.r_2",
            "org.xmlsoap.schemas.soap.envelope"
    };

    /**
     * classLoader is the default loader for java classes.
     */
    private static ClassLoader classLoader = org.oasis_open.docs.wsn.b_2.ObjectFactory.class.getClassLoader();

    /**
     * The schema that should be used in validation.
     */
    private static Schema schema = null;

    private static final String[] builtInSchemaLocations = {
            "/schemas/org.w3._2001._12.soap_envelope.xsd",
            "/schemas/org.oasis_open.docs.wsn.b_2.xsd",
            "/schemas/org.oasis_open.docs.wsn.br_2.xsd",
            "/schemas/org.oasis_open.docs.wsn.t_1.xsd",
            "/schemas/org.oasis_open.docs.wsrf.bf_2.xsd",
            "/schemas/org.oasis_open.docs.wsrf.r_2.xsd",
            "/schemas/org.xmlsoap.schemas.soap.envelope.xsd"
    };

    private static int _stopParsingAtSeverity = 2;

    private static final List<String> externalSchemaLocations = new ArrayList<>();

    private static boolean _skippingSchemaValidation = false;

    // Ensure schemas are parsed on load, if needed
    static {
        if (!_skippingSchemaValidation) {
            try {
                updateSchema();
            } catch (JAXBException e) {
                Log.e("XMLParser", "Could not load schemas for validation properly.");
            }
        }
    }

    /**
     * This class should never be instantiated.
     */
    private XMLParser() {
    }

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
            System.arraycopy(classPaths, 0, newPaths, 0, classPaths.length);
            newPaths[newPaths.length - 1] = classPath;
            classPaths = newPaths;
        }
    }

    public static void registerSchemaLocation(String systemID) throws JAXBException {
        Log.d("XMLParser", "External schema location added");
        externalSchemaLocations.add(systemID);
        updateSchema();
    }

    /**
     * gets the {@link javax.xml.bind.Unmarshaller} with context given by context paths.
     *
     * @return the apropriate <code>Unmarshaller</code>
     * @throws JAXBException {@link javax.xml.bind.JAXBContext#newInstance(String, ClassLoader)}
     */
    private static Unmarshaller getUnmarshaller() throws JAXBException {
        Unmarshaller unmarshaller = getJaxbContext().createUnmarshaller();
        if (!_skippingSchemaValidation) {
            if (unmarshaller.getSchema() == null) {

                // Check if schemas are okay
                if (schema == null)
                    updateSchema();

                // If they still are not okay, something has gone terribly wrong.
                if (schema == null) {
                    Log.w("XMLParser", "Schema creation failed, unable to validate.");
                } else {
                    unmarshaller.setSchema(schema);
                    unmarshaller.setEventHandler(new NuValidationEventHandler(_stopParsingAtSeverity));
                }
            }
        }
        return unmarshaller;
    }

    /**
     * gets current jaxbContext. Ensures that it is updated with current classpaths. Used for parsing xml to java
     * objects.
     *
     * @return the current jaxbContext
     * @throws JAXBException if new instance of jaxbContext fails for some reason.
     */
    private static JAXBContext getJaxbContext() throws JAXBException {
        synchronized (XMLParser.class) {
            if (jaxbContext == null) {
                String cp = null;
                for (String s : classPaths)
                    cp = cp == null ? s : cp + ":" + s;
                jaxbContext = JAXBContext.newInstance(cp, classLoader);
            }
            return jaxbContext;
        }
    }

    private static Schema updateSchema() throws JAXBException {
        synchronized (XMLParser.class) {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            StreamSource[] streamSources = new StreamSource[builtInSchemaLocations.length + externalSchemaLocations.size()];

            for (int i = 0; i < builtInSchemaLocations.length; i++) {
                streamSources[i] = new StreamSource(XMLParser.class.getResourceAsStream(builtInSchemaLocations[i]));
            }

            for (int i = builtInSchemaLocations.length, j = 0; i < streamSources.length; i++, j++) {
                streamSources[i] = new StreamSource(externalSchemaLocations.get(j));
            }

            try {
                schema = factory.newSchema(streamSources);
            } catch (SAXException e) {
                Log.e("XMLParser", "Could not generate schema for validation, reason given: " + e.getMessage());
            }
        }
        return schema;
    }

    /**
     * get the {@link javax.xml.bind.Marshaller} with context given by context paths. Used to convert java objects to
     * xml.
     *
     * @return the apropriate <code>Marshaller</code>
     * @throws JAXBException {@link javax.xml.bind.JAXBContext#newInstance(String, ClassLoader)}
     */
    private static Marshaller getMarshaller() throws JAXBException {
        Marshaller marshaller = getJaxbContext().createMarshaller();
        if (!_skippingSchemaValidation) {
            if (marshaller.getSchema() == null) {

                // Check if schemas are okay
                if (schema == null)
                    updateSchema();

                // If they still are not okay, something has gone terribly wrong.
                if (schema == null) {
                    Log.w("XMLParser", "Unable to create schemas, schema validation not performed");
                } else {
                    marshaller.setSchema(schema);
                    marshaller.setEventHandler(new NuValidationEventHandler(_stopParsingAtSeverity));
                }
            }
        }
        return marshaller;
    }

    /**
     * Parses the {@link java.io.InputStream}, and returns the parsed tree structure
     *
     * @param inputStream The {@link java.io.InputStream} to parse.
     * @return The apropriate object.
     * @throws JAXBException {@link javax.xml.bind.JAXBContext#newInstance(String, ClassLoader)}
     */
    public static InternalMessage parse(InputStream inputStream) throws JAXBException {
        Log.d("XMLParser", "Parsing message from InputStream");
        XMLInputFactory factory = XMLInputFactory.newFactory();
        try {
            XMLStreamReader streamReader = factory.createXMLStreamReader(inputStream);
            return parse(streamReader);
        } catch (XMLStreamException e) {
            Log.e("XMLParser", "Could not create XMLStream: " + e.getMessage());
            e.printStackTrace();
            throw new JAXBException("Could not create XMLStream to read from");
        }
    }

    /**
     * Parses the {@link javax.xml.stream.XMLStreamReader}, and returns the parsed tree structure
     *
     * @param xmlStreamReader The {@link javax.xml.stream.XMLStreamReader} to parse.
     * @return The apropriate object.
     * @throws JAXBException {@link javax.xml.bind.JAXBContext#newInstance(String, ClassLoader)}
     */
    public static InternalMessage parse(XMLStreamReader xmlStreamReader) throws JAXBException {
        Log.d("XMLParser", "Parsing message from XMLStreamReader");
        XMLParser p = new XMLParser();
        WSStreamFilter filter = p.new WSStreamFilter();
        XMLInputFactory factory = XMLInputFactory.newFactory();

        try {
            xmlStreamReader = factory.createFilteredReader(xmlStreamReader, filter);
        } catch (XMLStreamException e) {
            Log.e("XMLParser", "Could not create XMLStream with filter: " + e.getMessage());
            throw new JAXBException("Could not create XMLStream to read from");
        }
        try {
            Unmarshaller unmarshaller = getUnmarshaller();
            InternalMessage msg = new InternalMessage(InternalMessage.STATUS_OK, unmarshaller.unmarshal(xmlStreamReader));
            msg.getRequestInformation().setNamespaceContext(filter.getNamespaceContext());
            return msg;
        } catch (JAXBException e) {
            Log.e("XMLParser", "Could not unmarshal:" + e.toString());
            throw e;
        }
    }

    /**
     * Converts the given object to XML and writes its content to the stream.
     *
     * @param object       the object to parse to XML
     * @param outputStream the stream to write to
     * @throws JAXBException if JAXBContext could not be created or any unexpected events happens during writing.
     *                       {@link javax.xml.bind.JAXBContext#newInstance(String, ClassLoader)}
     *                       {@link javax.xml.bind.Marshaller#marshal(Object, java.io.OutputStream)}
     */
    public static void writeObjectToStream(Object object, OutputStream outputStream) throws JAXBException {
        getMarshaller().marshal(object, outputStream);
    }

    public static boolean isSkippingSchemaValidation() {
        return _skippingSchemaValidation;
    }

    public static void setSkippingSchemaValidation(boolean _skippingSchemaValidation) {
        XMLParser._skippingSchemaValidation = _skippingSchemaValidation;
        if (!_skippingSchemaValidation && schema == null) {
            try {
                updateSchema();
            } catch (JAXBException e) {
                Log.e("XMLParser", "Could not create the schemas necessary for correct validation");
            }
        }
    }

    public static int getStopParsingAtSeverity() {
        return _stopParsingAtSeverity;
    }

    public static void set_stopParsingAtSeverity(int value) {
        _stopParsingAtSeverity = value;
    }


    private class WSStreamFilter implements StreamFilter {
        NuNamespaceContext namespaceContext = new NuNamespaceContext();

        @Override
        public boolean accept(XMLStreamReader reader) {
            if (reader.isStartElement()) {
                for (int i = 0; i < reader.getNamespaceCount(); i++) {
                    String prefix = reader.getNamespacePrefix(i);
                    namespaceContext.put(prefix, reader.getNamespaceURI(i));
                }
            }
            return true;
        }

        public NuNamespaceContext getNamespaceContext() {
            return namespaceContext;
        }
    }

    private static class NuValidationEventHandler implements ValidationEventHandler {
        final int _severityStop;

        NuValidationEventHandler(final int severityStop) {
            _severityStop = severityStop;
        }

        @Override
        public boolean handleEvent(ValidationEvent event) {
            if (event.getSeverity() >= _severityStop) {
                Log.e("XMLParser.NuValidationEventHandler", "A too severe event occurred during parsing, message given:"
                        + event.getMessage() + " at line: " + event.getLocator().getLineNumber() + ", column: " +
                        event.getLocator().getColumnNumber() + (event.getLocator().getNode() == null ? "" :
                        (" (node " + event.getLocator().getNode().getNodeName() + ")")));
                return false;
            }
            Log.w("XMLParser.NuValidationEventHandler", "A" + (event.getSeverity() == ValidationEvent.WARNING ?
                    " warning " : "n error") + " with message " + event.getMessage() + " occurred under parsing " +
                    "(severity level " + event.getSeverity() + ")");
            return true;
        }
    }
}
