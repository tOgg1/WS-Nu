package org.ntnunotif.wsnu.services.eventhandling;

/**
 * The class contains information regarding a change in a subscription.
 *
 * Contained in an object of this class is a registration reference, as well as an enum-variable indicating
 * what type of change has occured.
 */
public final class SubscriptionEvent {

    private final String subscriptionReference;
    private final Type type;

    /**
     * Main and only constructor.
     * @param subscriptionReference Reference to the subscription-key.
     * @param type The type of change.
     */
    public SubscriptionEvent(String subscriptionReference, Type type) {
        this.subscriptionReference = subscriptionReference;
        this.type = type;
    }

    /**
     * @return The subscription reference.
     */
    public String getSubscriptionReference() {
        return subscriptionReference;
    }

    /**
     * @return The type of change.
     */
    public Type getType() {
        return type;
    }

    /**
     * Type-enum, containing all possible change types of a subscription.
     */
    public static enum Type{
        UNSUBSCRIBE,
        RENEW,
        PAUSE,
        RESUME
    }
}
