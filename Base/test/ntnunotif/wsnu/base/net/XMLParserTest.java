package ntnunotif.wsnu.base.net;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.w3._2001._12.soap_envelope.*;

import javax.xml.bind.JAXBElement;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Inge on 07.03.14.
 */
public class XMLParserTest {

    private static final String notifyFilePlace = "Base/testres/parse_test_notify.xml";
    private static final String soapFilePlace = "Base/testres/server_test_soap.xml";
    private static final String subscribeFilePlace = "Base/testres/server_test_subscribe.xml";

    private InputStream notifyTestStream = null;
    private InputStream soapTestStream = null;
    private InputStream subscribeTestStream = null;

    @Before
    public void setup() {
        try {
            notifyTestStream = new FileInputStream(notifyFilePlace);
            soapTestStream = new FileInputStream(soapFilePlace);
            subscribeTestStream = new FileInputStream(subscribeFilePlace);
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
        System.out.println(element.getDeclaredType());
        System.out.println(element.getName());
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
                    // printing of element content information
                    System.out.println(el.getName());
                    System.out.println(el.getDeclaredType());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // TODO write to real tests:
        // Writing the three parsed objects to file for manual inspection:
        FileOutputStream fileOutputStream = new FileOutputStream("Base/testres/parser_n2xml.xml");
        XMLParser.writeObjectToStream(parsedObject1, fileOutputStream);
        fileOutputStream.close();
        fileOutputStream = new FileOutputStream("Base/testres/parser_s2xml.xml");
        XMLParser.writeObjectToStream(parsedObject2, fileOutputStream);
        fileOutputStream.close();
        fileOutputStream = new FileOutputStream("Base/testres/parser_sub2xml.xml");
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
        body.getAny().add(new ObjectFactory().createFault(fault));

        FileOutputStream fileOut = new FileOutputStream("Base/testres/test_fault.xml");
        XMLParser.writeObjectToStream(new ObjectFactory().createEnvelope(envelope), fileOut);

    }
}
