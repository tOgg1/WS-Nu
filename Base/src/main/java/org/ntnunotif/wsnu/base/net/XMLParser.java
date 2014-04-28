package org.ntnunotif.wsnu.base.net;

import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.namespace.QName;
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
import java.util.Stack;

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

    /**
     * If schema validation is done, this determines which severity level the validation should stop parsing at.
     */
    private static int _stopParsingAtSeverity = ValidationEvent.FATAL_ERROR;

    /**
     * If there exists external schemas, they should be referenced here.
     */
    private static final List<String> externalSchemaLocations = new ArrayList<>();

    /**
     * Tells whether parser should skip schema validation.
     */
    private static boolean _skippingSchemaValidation = true;

    /**
     * Ensure schemas are parsed on load, if needed
     */
    static {
        if (!_skippingSchemaValidation) {
            updateSchema();
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

    /**
     * Register an external schema with this parser.
     *
     * @param systemID the ID of the external schema, registration done through a
     *                 {@link javax.xml.transform.stream.StreamSource}. For more information, see {@link javax.xml.transform.stream.StreamSource#StreamSource(java.lang.String)}
     * @throws JAXBException
     */
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

    /**
     * Updates the {@link javax.xml.validation.Schema} used for validation. This includes both internal schemas and
     * external ones.
     *
     * @return The newly generated schema.
     */
    private static Schema updateSchema() {
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
     * Attempts to parse the given {@link org.ntnunotif.wsnu.base.util.InternalMessage}.
     *
     * @param internalMessage a container for the request to parse
     * @return the internal message given as argument, with filled in additional request information
     * @throws JAXBException if no way of parsing the request is found, or something else fails.
     */
    public static void parse(InternalMessage internalMessage) throws JAXBException {
        Log.d("XMLParser", "Parsing message from InternalMessage");
        Object message = internalMessage.getMessage();
        InternalMessage parsedMessage = null;
        if (message instanceof InputStream) {
            parsedMessage = parse((InputStream) message);
        } else if (message instanceof XMLStreamReader) {
            parsedMessage = parse((XMLStreamReader) message);
        }

        if (parsedMessage == null) {
            Log.e("XMLParser", "Could not unmarshall object of class" + message.getClass());
            throw new JAXBException("No way of unmarshalling " + message.getClass() + " 0found");
        }

        internalMessage.setMessage(parsedMessage.getMessage());

        internalMessage.getRequestInformation().setParseValidationEventInfos(parsedMessage.getRequestInformation().getParseValidationEventInfos());
        internalMessage.getRequestInformation().setNamespaceContext(parsedMessage.getRequestInformation().getNamespaceContext());
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
            NuRequestInformationValidationEventHandler validationEventHandler = null;
            if (_skippingSchemaValidation) {
                validationEventHandler = new NuRequestInformationValidationEventHandler(filter);
                unmarshaller.setEventHandler(validationEventHandler);
            }
            InternalMessage msg = new InternalMessage(InternalMessage.STATUS_OK, unmarshaller.unmarshal(xmlStreamReader));

            if (validationEventHandler != null) {
                msg.getRequestInformation().setParseValidationEventInfos(validationEventHandler.parseValidationEventInfos);
            }

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

    /**
     * Tells if parser is set to skip validation against schemas or not.
     *
     * @return
     */
    public static boolean isSkippingSchemaValidation() {
        return _skippingSchemaValidation;
    }

    /**
     * Sets if parser should validate against schemas or not.
     *
     * @param _skippingSchemaValidation if this parser should skip schema validation.
     */
    public static void setSkippingSchemaValidation(boolean _skippingSchemaValidation) {
        XMLParser._skippingSchemaValidation = _skippingSchemaValidation;
        if (!_skippingSchemaValidation && schema == null) {
            updateSchema();
        }
    }

    /**
     * Gets the severity this parser should stop parsing at.
     *
     * @return the severity level
     * @see javax.xml.bind.ValidationEvent#WARNING
     * @see javax.xml.bind.ValidationEvent#ERROR
     * @see javax.xml.bind.ValidationEvent#FATAL_ERROR
     */
    public static int getStopParsingAtSeverity() {
        return _stopParsingAtSeverity;
    }

    /**
     * Sets the severity this parser should stop parsing at.
     *
     * @param value The severity level. Should be one of {@link javax.xml.bind.ValidationEvent#WARNING},
     *              {@link javax.xml.bind.ValidationEvent#ERROR} or {@link javax.xml.bind.ValidationEvent#FATAL_ERROR}
     */
    public static void setStopParsingAtSeverity(int value) {
        _stopParsingAtSeverity = value;
    }

    /**
     * Stream filter that keeps track of namespaces during parsing from xml.
     */
    private class WSStreamFilter implements StreamFilter {
        NuNamespaceContext namespaceContext = new NuNamespaceContext();
        Stack<QName> elementPath = new Stack<>();
        XMLStreamReader reader;

        @Override
        public boolean accept(XMLStreamReader reader) {
            this.reader = reader;

            if (reader.isStartElement()) {
                elementPath.push(reader.getName());

                for (int i = 0; i < reader.getNamespaceCount(); i++) {
                    String prefix = reader.getNamespacePrefix(i);
                    namespaceContext.put(prefix, reader.getNamespaceURI(i));
                }

            } else if (reader.isEndElement()) {
                elementPath.pop();
            }
            return true;
        }

        public NuNamespaceContext getNamespaceContext() {
            return namespaceContext;
        }
    }

    /**
     * A {@link javax.xml.bind.ValidationEventHandler} used in schema validation.
     */
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

    /**
     * A {@link javax.xml.bind.ValidationEventHandler} used when schema validation is turned off to log validation errors.
     */
    private static class NuRequestInformationValidationEventHandler implements ValidationEventHandler {

        final WSStreamFilter filter;
        final List<NuParseValidationEventInfo> parseValidationEventInfos = new ArrayList<>();

        NuRequestInformationValidationEventHandler(WSStreamFilter filter) {
            this.filter = filter;
        }

        @Override
        public boolean handleEvent(ValidationEvent event) {

            Stack<QName> path = (Stack<QName>) filter.elementPath.clone();
            boolean isStartElement = filter.reader.isStartElement();
            boolean isEndElement = filter.reader.isEndElement();
            int eventType = filter.reader.getEventType();
            QName currentName = isStartElement || isEndElement ? filter.reader.getName() : null;

            NuParseValidationEventInfo info = new NuParseValidationEventInfo(event, event.getSeverity(), currentName,
                    path, eventType, isStartElement, isEndElement);

            parseValidationEventInfos.add(info);

            Log.d("XMLParser.NuRequestInformationValidationEventHandler", "Validation event logged: " + event.toString());
            return true;
        }
    }
}
