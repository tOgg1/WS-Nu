package org.ntnunotif.wsnu.services.eventhandling;

import java.util.EventListener;

/**
 * Listener to listen to NotificationConsumers or NotificationBrokers.
 * @author Tormod Haugland
 * Created by tormod on 3/13/14.
 */
public interface ConsumerListener extends EventListener {

    /**
     * Passes the notification-event forward.
     * @param event
     */
    public void notify(NotificationEvent event);
}
