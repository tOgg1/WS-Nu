package org.ntnunotif.wsnu.services.eventhandling;

/**
 * Created by tormod on 23.04.14.
 */
public final class PublisherRegistrationEvent {

    private final String registrationReference;
    private final Type type;

    public PublisherRegistrationEvent(String registrationReference, Type type) {
        this.registrationReference = registrationReference;
        this.type = type;
    }

    public String getRegistrationReference() {
        return registrationReference;
    }

    public Type getType() {
        return type;
    }

    public static enum Type{

    }
}
