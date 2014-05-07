package org.ntnunotif.wsnu.services.eventhandling;

import org.ntnunotif.wsnu.base.util.RequestInformation;
import org.oasis_open.docs.wsn.b_2.Notify;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to make <code>NotificationEvent</code> dispatching easier, centralized and equal all over.
 */
public class NotificationEventSupport {
    /**
     * The listeners to events
     */
    private final List<ConsumerListener> _listeners;
    /**
     * The source object that fires the event
     */
    private final Object _source;

    /**
     * Initializes the support class. All events dispatched from this support instance will place the object as the
     * source of the event.
     *
     * @param _source The source of events.
     */
    public NotificationEventSupport(Object _source) {
        this._source = _source;
        this._listeners = new ArrayList<>();
    }

    /**
     * Adds a {@link org.ntnunotif.wsnu.services.eventhandling.ConsumerListener} that listen for events. One listener
     * can be added multiple times, and will receive the event once per time it is added.
     *
     * @param listener the listener to add
     */
    public void addNotificationListener(ConsumerListener listener) {
        this._listeners.add(listener);
    }

    /**
     * Removes a {@link org.ntnunotif.wsnu.services.eventhandling.ConsumerListener} from the listeners for the event. If
     * the listener is registered multiple times, only the first occurrence of the listener will be removed.
     *
     * @param listener the listener to remove
     */
    public void removeNotificationListener(ConsumerListener listener) {
        this._listeners.remove(listener);
    }

    /**
     * Fires a prebuilt event. No checks are done on the event, so it can contain anything or be from any source.
     *
     * @param event the event to send
     */
    public void fireNotificationEvent(NotificationEvent event) {
        for (ConsumerListener l : _listeners)
            l.notify(event);
    }

    /**
     * Builds an event and fires it. The source of the event will be the source registered with this support.
     *
     * @param _notification      the {@link org.oasis_open.docs.wsn.b_2.Notify} to fire
     * @param requestInformation the {@link org.ntnunotif.wsnu.base.util.RequestInformation} to include.
     */
    public void fireNotificationEvent(Notify _notification, RequestInformation requestInformation) {
        NotificationEvent event = new NotificationEvent(_source, _notification, requestInformation);
        fireNotificationEvent(event);
    }

    /**
     * Builds an event and fires it. The source of the event will be the source registered with this support. No
     * {@link org.ntnunotif.wsnu.base.util.RequestInformation} will be included.
     *
     * @param _notification the {@link org.oasis_open.docs.wsn.b_2.Notify} to fire
     */
    public void fireNotificationEvent(Notify _notification) {
        NotificationEvent event = new NotificationEvent(_source, _notification);
        fireNotificationEvent(event);
    }
}
