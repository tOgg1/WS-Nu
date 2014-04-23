package org.ntnunotif.wsnu.services.eventhandling;

/**
 * Created by tormod on 23.04.14.
 */
public class SubscriptionEvent {

    private final String subscriptionReference;
    private final Type type;

    public SubscriptionEvent(String subscriptionReference, Type type) {
        this.subscriptionReference = subscriptionReference;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public String getSubscriptionReference() {
        return subscriptionReference;
    }

    public static enum Type{
        UNSUBSCRIBE,
        RENEW,
        PAUSE,
        RESUMSE
    }
}
