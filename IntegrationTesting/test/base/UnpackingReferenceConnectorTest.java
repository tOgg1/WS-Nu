package base;

import junit.framework.TestCase;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.ForwardingHub;
import org.ntnunotif.wsnu.base.internal.UnpackingReferenceConnector;
import org.ntnunotif.wsnu.base.util.EndpointReference;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.services.notificationproducer.SimpleNotificationProducer;
import org.ntnunotif.wsnu.services.subscriptionmanager.SimpleSubscriptionManager;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.w3._2001._12.soap_envelope.Body;
import org.w3._2001._12.soap_envelope.Envelope;

import java.io.FileInputStream;

/**
 * Created by tormod on 23.03.14.
 */
public class UnpackingReferenceConnectorTest extends TestCase {

    ForwardingHub hub;
    UnpackingReferenceConnector connector;
    SimpleSubscriptionManager manager;
    Unsubscribe request;

    public void setUp() throws Exception {
        super.setUp();
        hub = new ForwardingHub();
        manager = new SimpleSubscriptionManager();
        connector = new UnpackingReferenceConnector(manager);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testAcceptMessageWithoutHub() throws Exception {
        request = new Unsubscribe();

        Envelope env = new Envelope();
        Body body = new Body();
        body.getAny().add(request);
        env.setBody(body);
        InternalMessage message = new InternalMessage(InternalMessage.STATUS_OK|InternalMessage.STATUS_ENDPOINTREF_IS_SET, env);
        message.setEndpointReference("tormodhaugland.com");
        connector.acceptMessage(message);

    }

    @Test
    public void testAcceptMessageWithHub() throws Exception {
        hub.registerService(connector);

        HttpClient client = new HttpClient();
        client.setFollowRedirects(false);
        client.start();

        Request request = client.newRequest("http://localhost:8080");
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(new FileInputStream("IntegrationTesting/res/server_test_unsubscribe.xml")));

        ContentResponse response = request.send();
    }
}
