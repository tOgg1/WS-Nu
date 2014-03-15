package integration;

import junit.framework.TestCase;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.GenericConnector;
import org.ntnunotif.wsnu.base.internal.InternalHub;
import org.ntnunotif.wsnu.base.internal.InternalMessage;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by tormod on 3/14/14.
 */
public class ApplicationServerWithHubTest extends TestCase {

    private ApplicationServer _server;
    private InternalHub _hub;

    private NotificationConsumer _consumer;
    private GenericConnector _consumerConnector;

    private ArrayList<InputStream> _sendMessages;
    private ArrayList<InternalMessage> _messages;

    public void setUp() throws Exception {
        super.setUp();

        _sendMessages = new ArrayList<>();
        _messages = new ArrayList<>();

        _server = ApplicationServer.getInstance();
        _hub = new InternalHub();
        _server.start(_hub);

        InputStream sendStream = new FileInputStream("IntegrationTesting/res/server_test_notify.xml");
        _sendMessages.add(sendStream);
        _messages.add(XMLParser.parse(sendStream));

        sendStream = new FileInputStream("IntegrationTesting/res/server_test_soap.xml");
        _sendMessages.add(sendStream);
        _messages.add(XMLParser.parse(sendStream));
    }

    @Test
    public void testSendingInvalidMessage() throws Exception {
        SslContextFactory sslFactory = new SslContextFactory();

        HttpClient client = new HttpClient(sslFactory);
        client.setFollowRedirects(true);
        client.start();

        // Send response
        Request request = client.newRequest("http://localhost:8080/");
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(_sendMessages.get(0)));

        ContentResponse response = request.send();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Test
    public void testSendingNotification() throws Exception {
        assertTrue(true);

    }

    @Test
    public void testReceivingNotification() throws Exception {
        assertTrue(true);

    }

    @Test
    public void testSubscribing() throws Exception {
        assertTrue(true);

    }
}
