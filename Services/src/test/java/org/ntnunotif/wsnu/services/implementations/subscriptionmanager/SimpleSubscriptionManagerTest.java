package org.ntnunotif.wsnu.services.implementations.subscriptionmanager;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.InputStreamContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.SimpleNotificationProducer;

import java.io.InputStream;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

/**
 * Created by tormod on 3/19/14.
 */

public class SimpleSubscriptionManagerTest{

    private static SoapForwardingHub hub;
    private static UnpackingConnector connector;
    private static SimpleSubscriptionManager manager;
    private static SimpleNotificationProducer producer;

    @BeforeClass
    public static void setUp() throws Exception {
        producer = new SimpleNotificationProducer();
        hub = producer.quickBuild("simpleProducer");

        manager = new SimpleSubscriptionManager(hub);
        connector = new UnpackingConnector(manager);

        producer.setSubscriptionManager(manager);

        hub.registerService(connector);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        hub.stop();
    }

    @Test
    public void testUnsubscribe() throws Exception {

        String subscription = producer.generateSubscriptionKey();
        String requestUrl = producer.generateSubscriptionURL(subscription);

        manager.addSubscriber(subscription, System.currentTimeMillis());

        InputStream stream = getClass().getResourceAsStream("/server_test_unsubscribe.xml");

        HttpClient client = new HttpClient();
        client.setFollowRedirects(false);
        client.start();

        // Test without requestURL
        Request request = client.newRequest("http://localhost:8080/"+"?subscription="+subscription);
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(stream));

        ContentResponse response = request.send();
        String responseContent = response.getContentAsString();
        assertEquals("Test without request URL", 200, response.getStatus());
        assertNotNull(responseContent);

        manager.addSubscriber(subscription, System.currentTimeMillis());

        // Test with requestURL
        request = client.newRequest("http://"+requestUrl);
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(getClass().getResourceAsStream("/server_test_unsubscribe.xml")));

        response = request.send();
        responseContent = response.getContentAsString();
        assertEquals("Test with request URL", 200, response.getStatus());
        System.out.println(responseContent);
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
        request.content(new InputStreamContentProvider(getClass().getResourceAsStream("/server_test_unsubscribe.xml")));

        ContentResponse response = request.send();
        String responseContent = response.getContentAsString();
        System.out.println(responseContent);
        assertEquals("Response was wrong for url http://" + requestUrl, 500, response.getStatus());
    }

    @Test
    public void testRenew() throws Exception {

        String subscription = producer.generateSubscriptionKey();
        String requestUrl = producer.generateSubscriptionURL(subscription);

        manager.addSubscriber(subscription, System.currentTimeMillis());

        System.out.println(subscription);

        HttpClient client = new HttpClient();
        client.setFollowRedirects(false);
        client.start();

        Request request = client.newRequest("http://"+requestUrl);
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(getClass().getResourceAsStream("/server_test_renew.xml")));

        System.out.println(requestUrl);

        ContentResponse response = request.send();
        String responseContent = response.getContentAsString();
        System.out.println(responseContent);
        assertEquals(200, response.getStatus());
    }
}
