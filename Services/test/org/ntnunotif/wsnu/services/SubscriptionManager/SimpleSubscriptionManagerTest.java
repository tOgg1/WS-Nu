package org.ntnunotif.wsnu.services.SubscriptionManager;

import junit.framework.TestCase;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.GenericConnector;
import org.ntnunotif.wsnu.base.internal.InternalHub;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by tormod on 3/19/14.
 */
public class SimpleSubscriptionManagerTest extends TestCase {

    private InternalHub hub;
    private GenericConnector connector;
    private SimpleSubscriptionManager manager;

    public void setUp() throws Exception {
        super.setUp();
        hub = new InternalHub();
        manager = new SimpleSubscriptionManager();
        connector = new GenericConnector(manager);

        hub.registerService(connector);
    }

    public void tearDown() throws Exception {

    }

    @Test
    public void testUnsubscribe() throws Exception {
        InputStream stream = new FileInputStream("Services/res/server_test_unsubscribe.xml");

        HttpClient client = new HttpClient();
        client.setFollowRedirects(false);
        client.start();

        Request request = client.newRequest("http://localhost:8080");
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(stream));

        ContentResponse response = request.send();
        System.out.println(response.getStatus());
        String responseContent = response.getContentAsString();
        System.out.println(responseContent);
    }
}
