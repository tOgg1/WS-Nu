package org.ntnunotif.wsnu.examples;

import org.ntnunotif.wsnu.base.internal.Hub;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.ntnunotif.wsnu.base.util.InternalMessage.*;

/**
 * Created by tormod on 25.03.14.
 */
public class RemoteConsumer implements ConsumerListener {
    private NotificationConsumer consumer;
    private Hub hub;

    public RemoteConsumer() {
        consumer = new NotificationConsumer();
        hub = consumer.quickBuild("MyConsumer");

        try {
            InputStream subscribe = new FileInputStream("Examples/res/server_test_subscribe.xml");
            InternalMessage subMessage = new InternalMessage(STATUS_FAULT|STATUS_ENDPOINTREF_IS_SET|STATUS_HAS_MESSAGE|STATUS_MESSAGE_IS_INPUTSTREAM, subscribe);
            subMessage.getRequestInformation().setEndpointReference("http://151.236.10.120:8080/numberProducer");
            hub.acceptLocalMessage(subMessage);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        RemoteConsumer consumer = new RemoteConsumer();
    }

    @Override
    public void notify(NotificationEvent event) {
    }
}
