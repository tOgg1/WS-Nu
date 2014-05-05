package org.ntnunotif.wsnu.examples.StockBroker;

import org.ntnunotif.wsnu.examples.StockBroker.generated.StockChanged;
import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.ntnunotif.wsnu.services.general.ServiceUtilities;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tormod on 29.04.14.
 */
public class StockConsumer implements ConsumerListener {

    private NotificationConsumer consumer;

    private ArrayList<StockChanged> stocks = new ArrayList<>();

    public StockConsumer() {
        consumer = new NotificationConsumer();
        consumer.addConsumerListener(this);
        consumer.quickBuild("stockConsumer");
        consumer.forceEndpointReference("http://"+ServiceUtilities.getExternalIp() + ":8080/stockConsumer");
        consumer.sendSubscriptionRequest("http://151.236.216.174:8080/stockBroker");

        ServiceUtilities.InputManager inputManager = new ServiceUtilities.InputManager();
        try {
            inputManager.addMethodReroute("exit", "^exit", true, System.class.getDeclaredMethod("exit", Integer.TYPE), this, new ServiceUtilities.Tuple[]{new ServiceUtilities.Tuple(0, 0)});
        } catch (NoSuchMethodException e) {
            // Do nothing
        }
    }

    @Override
    public void notify(NotificationEvent event) {
        List<NotificationMessageHolderType.Message> messages = event.getMessage();

        for (NotificationMessageHolderType.Message message : messages) {
            System.out.println("Got message");
        }
    }

    public static void main(String[] args) {
        StockConsumer consumer = new StockConsumer();
    }
}
