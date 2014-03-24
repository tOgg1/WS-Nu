package base;

import junit.framework.TestCase;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.ForwardingHub;
import org.ntnunotif.wsnu.base.internal.UnpackingRequestInformationConnector;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.services.implementations.subscriptionmanager.SimpleSubscriptionManager;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.w3._2001._12.soap_envelope.Body;
import org.w3._2001._12.soap_envelope.Envelope;

import java.io.FileInputStream;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 * Created by tormod on 23.03.14.
 */
public class UnpackingRequestInformationConnectorTest extends TestCase {

    ForwardingHub hub;
    UnpackingRequestInformationConnector connector;
    SimpleSubscriptionManager manager;
    Unsubscribe request;

    public void setUp() throws Exception {
        super.setUp();
        hub = new ForwardingHub();
        manager = new SimpleSubscriptionManager(hub);
        connector = new UnpackingRequestInformationConnector(manager);
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
        InternalMessage message = new InternalMessage(STATUS_OK| STATUS_ENDPOINTREF_IS_SET, env);
        message.getRequestInformation().setEndpointReference("tormodhaugland.com");
        connector.acceptMessage(message);
    }

    @Test
    public void testAcceptMessageWithHub() throws Exception {
        hub.registerService(connector);

        HttpClient client = new HttpClient();
        client.setFollowRedirects(false);
        client.start();

        Request request = client.newRequest("http://localhost:8080?subscription=909aryg9a8223n8a89j23f");
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(new FileInputStream("IntegrationTesting/res/server_test_unsubscribe.xml")));

        ContentResponse response = request.send();
    }
}
