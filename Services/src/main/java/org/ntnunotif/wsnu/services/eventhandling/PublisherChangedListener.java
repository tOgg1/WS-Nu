package org.ntnunotif.wsnu.services.eventhandling;

/**
 * A listener meant to be implemented by implementing classes of
 * {@link org.ntnunotif.wsnu.services.implementations.notificationbroker.AbstractNotificationBroker}.
 *
 * Has one method, {@link #publisherChanged(PublisherRegistrationEvent)}, which sends a
 * {@link org.ntnunotif.wsnu.services.eventhandling.PublisherRegistrationEvent} object.
 *
 * The main intention behind having this class is making it easy for Brokers to be notified when a publisher registration
 * has changed in any form. (Per the 2006 WS-N specification the only change can be destruction).
 */
public interface PublisherChangedListener {

    /**
     * The method indicating that a publisher registration has changed.
     * @param event The event-object containing information regarding the actual change.
     */
    public void publisherChanged(PublisherRegistrationEvent event);
}
