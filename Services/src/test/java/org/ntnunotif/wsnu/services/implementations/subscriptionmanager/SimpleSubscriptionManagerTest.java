//-----------------------------------------------------------------------------
// Copyright (C) 2014 Tormod Haugland and Inge Edward Haulsaunet
//
// This file is part of WS-Nu.
//
// WS-Nu is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// WS-Nu is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with WS-Nu. If not, see <http://www.gnu.org/licenses/>.
//-----------------------------------------------------------------------------

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
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.NotificationProducerImpl;

import java.io.InputStream;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

/**
 *
 */

public class SimpleSubscriptionManagerTest{

    private static SoapForwardingHub hub;
    private static UnpackingConnector connector;
    private static SimpleSubscriptionManager manager;
    private static NotificationProducerImpl producer;

    @BeforeClass
    public static void setUp() throws Exception {
        producer = new NotificationProducerImpl();
        hub = producer.quickBuild("simpleProducer");

        manager = new SimpleSubscriptionManager(hub);
        connector = new UnpackingConnector(manager);

        producer.setSubscriptionManager(manager);

        hub.registerService(connector);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        ApplicationServer.getInstance().stop();
        hub.stop();
    }

    @Test
    public void testUnsubscribe() throws Exception {

        String subscription = producer.generateSubscriptionKey();
        String requestUrl = producer.generateHashedURLFromKey("subscription", subscription);

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
        request = client.newRequest(requestUrl);
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(getClass().getResourceAsStream("/server_test_unsubscribe.xml")));

        response = request.send();
        responseContent = response.getContentAsString();
        assertEquals("Test with request URL", 200, response.getStatus());
        assertNotNull("ResponseContent was null: No content was returned", responseContent);
    }

    @Test
    public void testSubscriptionDoesntExist() throws Exception {
        String subscription = producer.generateSubscriptionKey();
        String requestUrl = producer.generateHashedURLFromKey("subscription", subscription);

        HttpClient client = new HttpClient();
        client.setFollowRedirects(false);
        client.start();

        Request request = client.newRequest(requestUrl);
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(getClass().getResourceAsStream("/server_test_unsubscribe.xml")));

        ContentResponse response = request.send();
        assertEquals("Response was wrong for url http://" + requestUrl, 500, response.getStatus());
    }

    @Test
    public void testRenew() throws Exception {

        String subscription = producer.generateSubscriptionKey();
        String requestUrl = producer.generateHashedURLFromKey("subscription", subscription);

        manager.addSubscriber(subscription, System.currentTimeMillis());

        HttpClient client = new HttpClient();
        client.setFollowRedirects(false);
        client.start();

        Request request = client.newRequest(requestUrl);
        request.method(HttpMethod.POST);
        request.content(new InputStreamContentProvider(getClass().getResourceAsStream("/server_test_renew.xml")));

        ContentResponse response = request.send();
        assertEquals("Response status was wrong", 200, response.getStatus());
    }
}
