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

package org.ntnunotif.wsnu.services.implementations.notificationbroker;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.internal.UnpackingConnector;
import org.ntnunotif.wsnu.base.net.ApplicationServer;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.ntnunotif.wsnu.services.general.WsnUtilities;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.GenericNotificationProducer;
import org.ntnunotif.wsnu.services.implementations.publisherregistrationmanager.SimplePublisherRegistrationManager;
import org.ntnunotif.wsnu.services.implementations.subscriptionmanager.SimpleSubscriptionManager;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import static junit.framework.TestCase.assertTrue;
import static org.ntnunotif.wsnu.base.util.InternalMessage.STATUS_OK;

/**
 *
 */
public class GenericNotificationBrokerTest {

    private static Hub hub;

    private static GenericNotificationBroker broker;
    private static SimplePublisherRegistrationManager publisher_manager;
    private static SimpleSubscriptionManager sub_manager;
    private static NotificationConsumer consumer;
    private static GenericNotificationProducer producer;
    private static ConsumerListener listener;
    private static boolean flag;

    @BeforeClass
    public static void setUpClass() throws Exception {
        listener = new ConsumerListener() {
            @Override
            public void notify(NotificationEvent event) {
                flag = true;
            }
        };

        broker = new GenericNotificationBroker();
        hub = broker.quickBuild("myBroker");

        producer = new GenericNotificationProducer(hub);
        producer.setEndpointReference("myProducer");
        publisher_manager = new SimplePublisherRegistrationManager(hub);
        sub_manager = new SimpleSubscriptionManager(hub);
        consumer = new NotificationConsumer(hub);
        consumer.addConsumerListener(listener);

        UnpackingConnector connector_one = new UnpackingConnector(publisher_manager);
        UnpackingConnector connector_two = new UnpackingConnector(sub_manager);
        UnpackingConnector connector_three = new UnpackingConnector(consumer);
        UnpackingConnector connector_four = new UnpackingConnector(producer);

        hub.registerService(connector_one);
        hub.registerService(connector_two);
        hub.registerService(connector_three);
        hub.registerService(connector_four);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        ApplicationServer.getInstance().stop();
    }

    @Before
    public void setUp(){
        flag = false;
        broker.subscriptions.clear();
    }

    @Test
    public void testSubscribe() throws Exception {
        InternalMessage message = consumer.sendSubscriptionRequest("http://127.0.0.1:8080");
        assertTrue((message.statusCode & STATUS_OK) > 0);
    }

    @Test
    public void testNotify() throws Exception {
        InternalMessage message = broker.sendSubscriptionRequest("http://127.0.0.1:8080/myProducer/");
        assertTrue((message.statusCode & STATUS_OK) > 0);
        message = consumer.sendSubscriptionRequest("http://127.0.0.1:8080/myBroker/");
        assertTrue((message.statusCode & STATUS_OK) > 0);

        producer.sendNotification(WsnUtilities.createNotify(
                new JAXBElement<>(new QName("lol"), String.class, "Hey"),
                "127.0.0.1:8080/myBroker"));
        Thread.sleep(100);
        assertTrue(flag);
    }

    @Test
    public void testRegisterPublisher() throws Exception {
        InternalMessage message = consumer.sendPublisherRegistrationRequest("http://127.0.0.1:8080");
        System.out.println(message.getMessage());
        assertTrue((message.statusCode & STATUS_OK) > 0);
    }

    @Test
    public void testGetCurrentMessage() throws Exception {
        InternalMessage message = broker.sendSubscriptionRequest("http://127.0.0.1:8080/myProducer/");
        assertTrue((message.statusCode & STATUS_OK) > 0);
        message = consumer.sendSubscriptionRequest("http://127.0.0.1:8080/myBroker/");
        assertTrue((message.statusCode & STATUS_OK) > 0);

        producer.sendNotification(WsnUtilities.createNotify(
                new JAXBElement<>(new QName("lol"), String.class, "Hey"),
                "127.0.0.1:8080/myBroker"));

        TopicExpressionType type = new TopicExpressionType();
        type.setDialect("http://www.w3.org/TR/1999/REC-xpath-19991116");
        type.getContent().add("sometopic");
        InternalMessage curMessage = consumer.sendGetCurrentMessage("http://127.0.0.1:8080/myBroker/", type);

    }
}
