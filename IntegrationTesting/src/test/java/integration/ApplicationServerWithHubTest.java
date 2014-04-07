package integration;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;

import java.io.InputStream;
import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by tormod on 3/14/14.
 */
public class ApplicationServerWithHubTest{

    private static ApplicationServer _server;
    private static SoapForwardingHub _hub;

    private static NotificationConsumer _consumer;
    private static ConsumerListener _listener;
    private static UnpackingConnector _consumerConnector;

    private static ArrayList<InputStream> _sendMessages;
    private static ArrayList<InternalMessage> _messages;

    private static boolean _stackFlag;

    @BeforeClass
    public static void setUp() throws Exception {
        _stackFlag = false;

        _sendMessages = new ArrayList<>();
        _messages = new ArrayList<>();

        _hub = new SoapForwardingHub();

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

        InputStream sendStream_1_1 = ApplicationServerWithHubTest.class.getResourceAsStream("/server_test_notify.xml");
        InputStream sendStream_1_2 = ApplicationServerWithHubTest.class.getResourceAsStream("/server_test_notify.xml");
        _sendMessages.add(sendStream_1_1);
        _messages.add(XMLParser.parse(sendStream_1_2));

        InputStream sendStream_2_1 = ApplicationServerWithHubTest.class.getResourceAsStream("/server_test_soap.xml");
        InputStream sendStream_2_2 = ApplicationServerWithHubTest.class.getResourceAsStream("/server_test_soap.xml");
        _sendMessages.add(sendStream_2_1);
        _messages.add(XMLParser.parse(sendStream_2_2));
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

        assertEquals(200, response.getStatus());

        // Send a notify-request, expect something
        request = client.newRequest("http://localhost:8080/");
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(_sendMessages.get(0)));

        response = request.send();
        System.out.println(response.getContentAsString());

        assertEquals(200, response.getStatus());
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
