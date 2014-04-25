package org.ntnunotif.wsnu.base.net;

import org.junit.*;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.w3._2001._12.soap_envelope.*;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.io.*;

/**
 * Created by Inge on 07.03.14.
 */
public class XMLParserTest {

    private static final String notifyResourcePlace = "/parse_test_notify.xml";
    private static final String soapResourcePlace = "/server_test_soap.xml";
    private static final String subscribeResourcePlace = "/server_test_subscribe.xml";

    private InputStream notifyTestStream = null;
    private InputStream soapTestStream = null;
    private InputStream subscribeTestStream = null;

    @BeforeClass
    public static void setUpClass() {
        Log.setEnableDebug(false);
        Log.setEnableWarnings(false);
        Log.setEnableErrors(false);
    }

    @Before
    public void setup() {
        try {
            notifyTestStream = getClass().getResourceAsStream(notifyResourcePlace);
            soapTestStream = getClass().getResourceAsStream(soapResourcePlace);
            subscribeTestStream = getClass().getResourceAsStream(subscribeResourcePlace);
        } catch (Exception e) {
            Log.e("XMLParser", "Could not read test files");
            e.printStackTrace();
        }
    }

    @After
    public void breakDown() throws IOException{
        if (notifyTestStream != null)
            notifyTestStream.close();
        if (soapTestStream != null)
            soapTestStream.close();
    }

    @Test
    public void testNotificationParsing() throws Exception {
        Object parsedObj = XMLParser.parse(notifyTestStream);
        Assert.assertNotNull("Parser returned null object", parsedObj);
        Assert.assertEquals("Parser returned wrong object type", Notify.class, ((InternalMessage) parsedObj).getMessage().getClass());
        // This is dependent of content of file:
        Notify message = (Notify) ((InternalMessage)parsedObj).getMessage();
        Assert.assertEquals("Any elements of file were too many", 0, message.getAny().size());
        Assert.assertEquals("Wrong number of NotificationMessages detected", 1, message.getNotificationMessage().size());
    }

    @Test
    public void testSoapParsing() throws Exception {
        Object parsedObject = XMLParser.parse(soapTestStream).getMessage();
        Assert.assertNotNull("Parsed object was null", parsedObject);
        JAXBElement element = (JAXBElement) parsedObject;
        // TODO Complete test
        Envelope env =(Envelope) ((JAXBElement) parsedObject).getValue();
    }

    @Test
    public void testToXmlParse() throws Exception {
        Object parsedObject1 = XMLParser.parse(notifyTestStream).getMessage();
        Object parsedObject2 = XMLParser.parse(soapTestStream).getMessage();
        Object parsedObject3 = XMLParser.parse(subscribeTestStream).getMessage();
        // The following code is a consequence of questions of how things are parsed, and meant as a demonstration only.
        try {
            if (parsedObject3 instanceof JAXBElement) {
                // This was discovered through printing of class names
                JAXBElement<Envelope> jaxEl1 = (JAXBElement<Envelope>) parsedObject3;
                Envelope env = jaxEl1.getValue();
                Body body = env.getBody();
                // This was discovered by printing class names. Might also be discovered by xml inspection.
                Subscribe subscribe = (Subscribe)body.getAny().get(0);
                FilterType ft = subscribe.getFilter();
                for (Object o : ft.getAny()) {
                    JAXBElement el = (JAXBElement) o;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // TODO write to real tests:
        // Writing the three parsed objects to file for manual inspection:
        FileOutputStream fileOutputStream = new FileOutputStream(getClass().getResource("/parser_n2xml.xml").getFile());
        XMLParser.writeObjectToStream(parsedObject1, fileOutputStream);
        fileOutputStream.close();
        fileOutputStream = new FileOutputStream(getClass().getResource("/parser_s2xml.xml").getFile());
        XMLParser.writeObjectToStream(parsedObject2, fileOutputStream);
        fileOutputStream.close();
        fileOutputStream = new FileOutputStream(getClass().getResource("/parser_sub2xml.xml").getFile());
        XMLParser.writeObjectToStream(parsedObject3, fileOutputStream);
        fileOutputStream.close();
    }

    @Test
    public void testMarshallingFaultInEnvelope() throws Exception {
        Envelope envelope = new Envelope();
        Body body = new Body();
        Header header = new Header();

        envelope.setBody(body);
        envelope.setHeader(header);

        Fault fault = new Fault();
        fault.setFaultcode(new QName("Server"));
        fault.setFaultstring("Something went wrong");
        body.getAny().add(new ObjectFactory().createFault(fault));
        FileOutputStream fileOut = new FileOutputStream(getClass().getResource("/test_fault.xml").getFile());
        XMLParser.writeObjectToStream(new ObjectFactory().createEnvelope(envelope), fileOut);

    }
}
