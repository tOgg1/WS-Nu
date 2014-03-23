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
import org.ntnunotif.wsnu.base.internal.DefaultHub;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.base.internal.InternalMessage;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.services.notificationconsumer.NotificationConsumer;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by tormod on 3/14/14.
 */
public class ApplicationServerWithHubTest extends TestCase {

    private ApplicationServer _server;
    private DefaultHub _hub;

    private NotificationConsumer _consumer;
    private ConsumerListener _listener;
    private UnpackingConnector _consumerConnector;

    private ArrayList<InputStream> _sendMessages;
    private ArrayList<InternalMessage> _messages;

    private boolean _stackFlag;

    public void setUp() throws Exception {
        super.setUp();

        _stackFlag = false;

        _sendMessages = new ArrayList<>();
        _messages = new ArrayList<>();

        _server = ApplicationServer.getInstance();
        _hub = new DefaultHub();
        _server.start(_hub);

        _consumer = new NotificationConsumer(_hub);
        _listener = new ConsumerListener() {
            @Override
            public void notify(NotificationEvent event) {
                _stackFlag = true;
            }
        };
        _consumer.addConsumerListener(_listener);
        _consumerConnector = new UnpackingConnector(_consumer);

        _hub.registerService(_consumerConnector);

        InputStream sendStream_1_1 = new FileInputStream("IntegrationTesting/res/server_test_notify.xml");
        InputStream sendStream_1_2 = new FileInputStream("IntegrationTesting/res/server_test_notify.xml");
        _sendMessages.add(sendStream_1_1);
        _messages.add(XMLParser.parse(sendStream_1_2));

        InputStream sendStream_2_1 = new FileInputStream("IntegrationTesting/res/server_test_soap.xml");
        InputStream sendStream_2_2 = new FileInputStream("IntegrationTesting/res/server_test_soap.xml");
        _sendMessages.add(sendStream_2_1);
        _messages.add(XMLParser.parse(sendStream_2_2));
    }

    public void tearDown() throws Exception {
        super.tearDown();
        _server.stop();
    }

    @Test
    public void testSendingInvalidMessage() throws Exception {
        SslContextFactory sslFactory = new SslContextFactory();

        HttpClient client = new HttpClient(sslFactory);
        client.setFollowRedirects(true);
        client.start();

        // Send a soap-request, expect nothing
        Request request = client.newRequest("http://localhost:8080/");
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(_sendMessages.get(1)));

        ContentResponse response = request.send();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.getStatus());

        // Send a notify-request, expect something
        request = client.newRequest("http://localhost:8080/");
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(_sendMessages.get(0)));

        response = request.send();

        assertTrue(_stackFlag);
        assertEquals(HttpStatus.OK_200, response.getStatus());
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
