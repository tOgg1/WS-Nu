package org.ntnunotif.wsnu.services.eventhandling;

/**
 * Created by tormod on 3/13/14.
 */
public interface ConsumerListener {
    /**
     * Passes the notification-event forward.
     * @param event
     */
    public void notify(NotificationEvent event);
}
