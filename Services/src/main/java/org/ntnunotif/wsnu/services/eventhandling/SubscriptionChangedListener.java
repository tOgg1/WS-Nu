package org.ntnunotif.wsnu.services.eventhandling;

/**
 * A listener meant to be implemented by implementing classes of
 * {@link org.ntnunotif.wsnu.services.implementations.notificationbroker.AbstractNotificationBroker} or
 * {@link org.ntnunotif.wsnu.services.implementations.notificationproducer.AbstractNotificationProducer}.
 *
 * Has one method, {@link #subscriptionChanged(SubscriptionEvent)}, which sends a
 * {@link org.ntnunotif.wsnu.services.eventhandling.SubscriptionEvent} object.
 *
 * The main intention behind having this class is making it easy for Producers and Brokers to be notified when a subscription
 * has changed in any form.
 */
public interface SubscriptionChangedListener {

    public void subscriptionChanged(SubscriptionEvent event);
}
