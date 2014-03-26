package org.ntnunotif.wsnu.services.eventhandling;

/**
 * Listener to listen to NotificationConsumers or NotificationBrokers.
 * @author Tormod Haugland
 * Created by tormod on 3/13/14.
 */
public interface ConsumerListener {
    /**
     * Passes the notification-event forward.
     * @param event
     */
    public void notify(NotificationEvent event);
}
