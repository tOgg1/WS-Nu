package org.ntnunotif.wsnu.services.eventhandling;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by tormod on 3/15/14.
 */
public class NotificationEventTest {
    private static NotificationConsumer _consumer;
    private static Notify _notification;
    private static NotificationEvent _event;
    private static ConsumerListener _listener;

    @BeforeClass
    public static void setUp() throws Exception {
        Log.setEnableDebug(false);
        Log.setEnableWarnings(false);
        Log.setEnableErrors(false);

        _consumer = new NotificationConsumer(new SoapForwardingHub());
        _notification = (Notify)XMLParser.parse(NotificationEventTest.class.getResourceAsStream("/server_test_notify.xml")).getMessage();
        _event = new NotificationEvent(new Object(), _notification);
        _listener = new ConsumerListener() {
            @Override
            public void notify(NotificationEvent event) {
            }
        };
        _consumer.addConsumerListener(_listener);
    }

    @Test
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetRaw() throws Exception {
        Notify notification = _event.getRaw();
        assertEquals(notification, _notification);
    }

    @Test
    public void testGetXML() throws Exception {
        String notification = _event.getXML();
        assertTrue(notification.length() != 0);
    }

    @Test
    public void testGetMessage() throws Exception {
        List<NotificationMessageHolderType.Message> messages = _event.getMessage();
        assertEquals(messages.size(), 1);
    }
}
