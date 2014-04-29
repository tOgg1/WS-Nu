package org.ntnunotif.wsnu.examples.StockBroker;

import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.ntnunotif.wsnu.services.implementations.notificationconsumer.NotificationConsumer;
import org.oasis_open.docs.wsn.b_2.NotificationMessageHolderType;

import java.util.List;

/**
 * Created by tormod on 29.04.14.
 */
public class StockConsumer implements ConsumerListener {
    private NotificationConsumer consumer;

    public StockConsumer() {
        consumer = new NotificationConsumer();
    }

    @Override
    public void notify(NotificationEvent event) {
        List<NotificationMessageHolderType.Message> messages = event.getMessage();

        for (NotificationMessageHolderType.Message message : messages) {
            Object any = message.getAny();
            try{

            }catch(ClassCastException e){
                continue;
            }
        }
    }
}
