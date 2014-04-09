package org.ntnunotif.wsnu.integrationtesting;

import org.ntnunotif.wsnu.base.internal.SoapForwardingHub;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.services.implementations.notificationproducer.SimpleNotificationProducer;
import org.oasis_open.docs.wsn.b_2.Notify;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Inge on 09.04.2014.
 */
public class TestSimpleProducer {
    private static final String PRODUCER_ID = "mySimpleProducer";

    public static void main(String[] args) throws Exception {
        System.out.println("Loading notify messages that shall be sent");

        // List of notifies the producer should send.
        List<Notify> notifies = new ArrayList<>();

        // Fill the list of notifies
        InternalMessage internalMessage;
        System.out.println("complex 1");
        internalMessage = XMLParser.parse(TestSimpleProducer.class.getResourceAsStream("/notify_complex_1.xml"));
        notifies.add((Notify)internalMessage.getMessage());
        System.out.println("complex 2");
        internalMessage = XMLParser.parse(TestSimpleProducer.class.getResourceAsStream("/notify_complex_2.xml"));
        notifies.add((Notify)internalMessage.getMessage());
        System.out.println("simple 1");
        internalMessage = XMLParser.parse(TestSimpleProducer.class.getResourceAsStream("/notify_simple_1.xml"));
        notifies.add((Notify)internalMessage.getMessage());
        System.out.println("simple 2");
        internalMessage = XMLParser.parse(TestSimpleProducer.class.getResourceAsStream("/notify_simple_2.xml"));
        notifies.add((Notify)internalMessage.getMessage());
        System.out.println("simple 3");
        internalMessage = XMLParser.parse(TestSimpleProducer.class.getResourceAsStream("/notify_simple_3.xml"));
        notifies.add((Notify)internalMessage.getMessage());

        System.out.println("Starting a simplistic producer: " + PRODUCER_ID);

        // Starting the producer
        SimpleNotificationProducer producer = new SimpleNotificationProducer();
        SoapForwardingHub hub = producer.quickBuild(PRODUCER_ID);

        System.out.println("The qualified address for this producer is:");
        System.out.println(hub.getInetAdress() + "/" + PRODUCER_ID);

        // Wait a little, so consumers can register
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            System.out.println("The thread was interrupted while sleeping!");
        }

        // Send all notifies 4 times with a pause between each send
        for (int i = 0; i < 4; i++) {
            for (Notify notify: notifies) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    System.out.println("The thread was interrupted while sleeping!");
                }
                producer.sendNotification(notify);
            }
        }

    }
}
