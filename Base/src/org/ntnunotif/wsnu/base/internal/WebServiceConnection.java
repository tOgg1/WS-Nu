package org.ntnunotif.wsnu.base.internal;

/**
 * Created by tormod on 3/3/14.
 */
public interface WebServiceConnection {

    /**
     * Accept a message from a hub. This function forwards the message to its connected Web Service.
     * @param message
     */
    public void acceptMessage(Object message);

    /**
     * Return the type of the Web Service connected to this connection.
     * @return The type as a flag
     */
    public int getServiceType();

    /**
     * Return the functionality this Web Service offers.
     * @return The type as a flag
     */
    public int getServiceFunctionality();
}
