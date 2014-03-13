package org.ntnunotif.wsnu.services.NotificationConsumer;

import org.ntnunotif.wsnu.services.eventhandling.ConsumerListener;
import org.ntnunotif.wsnu.services.eventhandling.NotificationEvent;
import org.oasis_open.docs.wsn.b_2.Notify;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import java.util.ArrayList;

/**
 * Created by tormod on 3/11/14.
 */
public class NotificationConsumer implements org.oasis_open.docs.wsn.bw_2.NotificationConsumer {

    private ArrayList<ConsumerListener> _listeners;

    public NotificationConsumer() {
        _listeners = new ArrayList<>();
    }

    @Override
    @WebMethod(operationName = "Notify")
    public void notify(@WebParam(partName = "Notify", name = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/b-2") Notify notify) {
        NotificationEvent event  = new NotificationEvent(notify);

        for(ConsumerListener listener : _listeners){
            listener.notify(event);
        }
    }

    public void addConsumerListener(ConsumerListener listener){
        _listeners.add(listener);
    }

    public void removeConsumerListener(ConsumerListener listener){
        _listeners.remove(listener);
    }
}
