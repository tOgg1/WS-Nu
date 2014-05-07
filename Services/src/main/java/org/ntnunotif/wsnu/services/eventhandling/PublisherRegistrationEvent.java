package org.ntnunotif.wsnu.services.eventhandling;

/**
 * The class contains information regarding a change in a publisher registration.
 *
 * Contained in an object of this class is a registration reference, as well as an enum-variable indicating
 * what type of change has occured. (Per the 2006 WS-N specification the only change can be destruction).
 */
public final class PublisherRegistrationEvent {

    private final String registrationReference;
    private final Type type;

    /**
     * Main and only constructor.
     * @param registrationReference Reference to the registration-key.
     * @param type The type of change.
     */
    public PublisherRegistrationEvent(String registrationReference, Type type) {
        this.registrationReference = registrationReference;
        this.type = type;
    }

    /**
     * @return The registration reference variable as a string.
     */
    public String getRegistrationReference() {
        return registrationReference;
    }

    /**
     * @return The type of change.
     */
    public Type getType() {
        return type;
    }

    /**
     * Type-enum, containing all possible change types of a publisher registration.
     * (Per the 2006 WS-N specification the only change can be destruction).
     */
    public static enum Type{
        DESTROYED
    }
}
