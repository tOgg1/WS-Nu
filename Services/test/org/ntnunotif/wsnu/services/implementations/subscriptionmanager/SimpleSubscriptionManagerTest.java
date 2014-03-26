package org.ntnunotif.wsnu.services.implementations.subscriptionmanager;

import junit.framework.TestCase;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.ForwardingHub;
import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.base.internal.UnpackingRequestInformationConnector;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.SimpleNotificationProducer;

import javax.swing.text.AbstractDocument;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.channels.FileLockInterruptionException;

/**
 * Created by tormod on 3/19/14.
 */

public class SimpleSubscriptionManagerTest extends TestCase {

    private Hub hub;
    private UnpackingRequestInformationConnector connector;
    private SimpleSubscriptionManager manager;
    private SimpleNotificationProducer producer;

    public void setUp() throws Exception {
        super.setUp();
        producer = new SimpleNotificationProducer();
        hub = producer.quickBuild();

        manager = new SimpleSubscriptionManager(hub);
        connector = new UnpackingRequestInformationConnector(manager);

        hub.registerService(connector);
    }

    public void tearDown() throws Exception {

    }

    @Test
    public void testUnsubscribe() throws Exception {

        String subscription = producer.generateSubscriptionKey();
        String requestUrl = producer.generateSubscriptionURL(subscription);

        manager.addSubscriber(subscription, System.currentTimeMillis());

        InputStream stream = new FileInputStream("Services/res/server_test_unsubscribe.xml");

        HttpClient client = new HttpClient();
        client.setFollowRedirects(false);
        client.start();

        // Test without requestURL
        Request request = client.newRequest("http://localhost:8080/"+"?subscription="+subscription);
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(stream));

        ContentResponse response = request.send();
        String responseContent = response.getContentAsString();
        assertEquals(200, response.getStatus());
        assertNotNull(responseContent);

        manager.addSubscriber(subscription, System.currentTimeMillis());

        request = client.newRequest("http://"+requestUrl);
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(new FileInputStream("Services/res/server_test_unsubscribe.xml")));

        response = request.send();
        responseContent = response.getContentAsString();
        assertEquals(200, response.getStatus());
        assertNotNull(responseContent);
    }

    @Test
    public void testSubscriptionDoesntExist() throws Exception {
        String subscription = producer.generateSubscriptionKey();
        String requestUrl = producer.generateSubscriptionURL(subscription);

        HttpClient client = new HttpClient();
        client.setFollowRedirects(false);
        client.start();

        Request request = client.newRequest("http://"+requestUrl);
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(new FileInputStream("Services/res/server_test_unsubscribe.xml")));

        ContentResponse response = request.send();
        String responseContent = response.getContentAsString();
        assertEquals(500, response.getStatus());
    }

    @Test
    public void testRenew() throws Exception {

        String subscription = producer.generateSubscriptionKey();
        String requestUrl = producer.generateSubscriptionURL(subscription);

        manager.addSubscriber(subscription, System.currentTimeMillis());

        HttpClient client = new HttpClient();
        client.setFollowRedirects(false);
        client.start();

        Request request = client.newRequest("http://"+requestUrl);
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(new FileInputStream("Services/res/server_test_renew.xml")));

        ContentResponse response = request.send();
        String responseContent = response.getContentAsString();
        assertEquals(200, response.getStatus());
    }
}
