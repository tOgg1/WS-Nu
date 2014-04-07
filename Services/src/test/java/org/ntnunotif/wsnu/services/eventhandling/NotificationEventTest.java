package org.ntnunotif.wsnu.services.eventhandling;

import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import org.junit.Before;
import org.junit.Test;
import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;
import org.oasis_open.docs.wsn.b_2.Notify;

import javax.xml.bind.JAXBElement;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by tormod on 3/15/14.
 */
public class NotificationEventTest {
    private NotificationConsumer _consumer;
    private Notify _notification;
    private NotificationEvent _event;
    private ConsumerListener _listener;

    @Before
    public void setUp() throws Exception {
        _consumer = new NotificationConsumer(new SoapForwardingHub());
        _notification = (Notify)XMLParser.parse(getClass().getResourceAsStream("/server_test_notify.xml")).getMessage();
        _event = new NotificationEvent(this, _notification);
        _listener = new ConsumerListener() {
            @Override
            public void notify(NotificationEvent event) {
                System.out.println("lol");
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
        System.out.println(notification);
        assertTrue(notification.length() != 0);
    }

    @Test
    public void testGetMessage() throws Exception {
        List<NotificationMessageHolderType.Message> messages = _event.getMessage();
        System.out.println(messages.size());
        assertEquals(messages.size(), 1);

        NotificationMessageHolderType.Message message = messages.get(0);
        Object object = message.getAny();
        System.out.println(object.getClass());
        System.out.println(object);
        if(object instanceof ElementNSImpl){
            ElementNSImpl element = (ElementNSImpl)object;
            System.out.println(element.getTypeName());
            System.out.println(element.getLength());
            System.out.println(element.getUserData());
            System.out.println(element.toString());
        }else if(object instanceof JAXBElement){
            JAXBElement element = (JAXBElement)object;
            System.out.println(object);
            System.out.println(element.getName());
            System.out.println(element.getDeclaredType());
            String string = (String)element.getValue();
            System.out.println(string);
        }

    }
}
