package ntnunotif.wsnu.base.net;

import org.junit.*;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.oasis_open.docs.wsn.b_2.Notify;

import java.io.*;

/**
 * Created by Inge on 07.03.14.
 */
public class XMLParserTest {

    private static final String notifyFilePlace = "Base/test/ntnunotif/wsnu/base/net/parse_test_notify.xml";
    private static final String soapFilePlace = "Base/test/ntnunotif/wsnu/base/net/server_test_soap.xml";

    private static InputStream notifyTestStream = null;
    private static InputStream soapTestStream = null;

    @BeforeClass
    public static void setup() {
        try {
            notifyTestStream = new FileInputStream(notifyFilePlace);
            soapTestStream = new FileInputStream(soapFilePlace);
        } catch (Exception e) {
            System.err.println("Could not read test files");
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void breakDown() throws IOException{
        if (notifyTestStream != null)
            notifyTestStream.close();
        if (soapTestStream != null)
            soapTestStream.close();
    }

    @Test
    public void testNotificationParsing() throws Exception {
        Object parsedObj = XMLParser.parse(notifyTestStream);
        Assert.assertNotNull("Parser returned null object", parsedObj);
        Assert.assertEquals("Parser returned wrong object type", Notify.class, parsedObj.getClass());
        // This is dependent of content of file:
        Notify message = (Notify)parsedObj;
        Assert.assertEquals("Any elements of file were too many", 0, message.getAny().size());
        Assert.assertEquals("Wrong number of NotificationMessages detected", 1, message.getNotificationMessage().size());
}

    @Test
    public void testSoapParsing() throws Exception {
        Object parsedObject = XMLParser.parse(soapTestStream);
        Assert.assertNotNull("Parsed object was null", parsedObject);
    }
}
